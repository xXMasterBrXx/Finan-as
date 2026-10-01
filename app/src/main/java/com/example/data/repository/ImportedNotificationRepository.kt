package com.example.data.repository

import com.example.data.local.CreditCardDao
import com.example.data.local.ImportedNotificationDao
import com.example.data.local.ImportedNotificationEntity
import com.example.data.local.TransactionDao
import com.example.data.local.TransactionEntity
import com.example.data.preferences.UserPreferences
import com.example.util.BankNotificationParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

class ImportedNotificationRepository(
    private val importedNotificationDao: ImportedNotificationDao,
    private val transactionDao: TransactionDao,
    private val creditCardDao: CreditCardDao,
    private val userPreferences: UserPreferences
) {

    private val repoMutex = Mutex()

    val pendingNotifications: Flow<List<ImportedNotificationEntity>> =
        importedNotificationDao.getPendingNotifications()

    val pendingCount: Flow<Int> = importedNotificationDao.getPendingCount()

    val allNotifications: Flow<List<ImportedNotificationEntity>> =
        importedNotificationDao.getAllImportedNotifications()

    suspend fun processNotification(
        packageName: String,
        title: String,
        text: String,
        subtext: String? = null
    ): ImportedNotificationEntity? = repoMutex.withLock {
        withContext(Dispatchers.IO) {
            if (!userPreferences.isAutoImportNotificationsEnabled()) return@withContext null

            val parsed = BankNotificationParser.parse(packageName, title, text, subtext) ?: return@withContext null

            // Check if bank is disabled in user preferences
            val disabledBanks = userPreferences.getDisabledBankPackages()
            if (disabledBanks.contains(packageName)) return@withContext null

            // Try matching a credit card
            val allCards = creditCardDao.getAllCardsSync()
            var matchedCardId: Long? = null
            if (!parsed.cardLastFourDigits.isNullOrBlank()) {
                matchedCardId = allCards.firstOrNull { it.lastFourDigits == parsed.cardLastFourDigits }?.id
            }
            if (matchedCardId == null && parsed.type == "EXPENSE") {
                matchedCardId = allCards.firstOrNull {
                    it.name.contains(parsed.bankName, ignoreCase = true) ||
                    parsed.bankName.contains(it.name, ignoreCase = true)
                }?.id
            }

            // =========================================================================
            // RIGID DEDUPLICATION & MULTI-NOTIFICATION MERGING (5-minute window + PENDING)
            // =========================================================================
            val minTime = System.currentTimeMillis() - 300_000L
            val isCurrentWallet = BankNotificationParser.isWalletPackage(packageName)

            val recentList = importedNotificationDao.getRecentNotifications(minTime)
            val pendingList = importedNotificationDao.getPendingNotificationsSync()
            val candidateList = (recentList + pendingList).distinctBy { it.id }

            val duplicateCandidate = candidateList.firstOrNull { candidate ->
                val amountDiff = Math.abs(candidate.amount - parsed.amount)
                val sameAmount = amountDiff < 0.01
                val sameType = candidate.type.equals(parsed.type, ignoreCase = true)
                if (!sameAmount || !sameType) return@firstOrNull false

                val isCandidateWallet = BankNotificationParser.isWalletPackage(candidate.packageName)
                val samePackage = candidate.packageName == packageName
                val sameBank = candidate.bankName.equals(parsed.bankName, ignoreCase = true)
                val similarMerchant = areMerchantsSimilar(candidate.merchant, parsed.merchant)
                val candidateIsPending = candidate.status == "PENDING"
                val withinTimeWindow = Math.abs(candidate.timestamp - parsed.timestamp) < 300_000L

                // If within 5 min (or still pending to approve/confirm), and:
                // - comes from the same bank / package
                // - OR either one is Google Wallet / Samsung Wallet and the other is Bank
                // - OR merchants match or are similar / generic
                (withinTimeWindow || candidateIsPending) &&
                    (samePackage || sameBank || isCurrentWallet || isCandidateWallet || similarMerchant)
            }

            if (duplicateCandidate != null) {
                // If incoming notification brings richer/more specific info, update the existing entry
                val candidateHasGenericMerchant = isGenericMerchant(duplicateCandidate.merchant)
                val incomingHasSpecificMerchant = isValidSpecificMerchant(parsed.merchant)
                val incomingHasCardDigits = !parsed.cardLastFourDigits.isNullOrBlank() && duplicateCandidate.cardLastFourDigits.isNullOrBlank()
                val incomingHasMatchedCard = matchedCardId != null && duplicateCandidate.matchedCardId == null
                val isWalletUpgrade = BankNotificationParser.isWalletPackage(duplicateCandidate.packageName) && !isCurrentWallet

                if ((incomingHasSpecificMerchant && candidateHasGenericMerchant) || incomingHasCardDigits || incomingHasMatchedCard || isWalletUpgrade) {
                    val betterMerchant = if (incomingHasSpecificMerchant) parsed.merchant else duplicateCandidate.merchant
                    val betterCardDigits = parsed.cardLastFourDigits ?: duplicateCandidate.cardLastFourDigits
                    val betterCardId = matchedCardId ?: duplicateCandidate.matchedCardId
                    val betterBankName = if (!isCurrentWallet) parsed.bankName else duplicateCandidate.bankName

                    val updated = duplicateCandidate.copy(
                        bankName = betterBankName,
                        merchant = betterMerchant,
                        cardLastFourDigits = betterCardDigits,
                        matchedCardId = betterCardId,
                        rawText = "${duplicateCandidate.rawText} | ${parsed.rawText}"
                    )
                    importedNotificationDao.update(updated)

                    if (duplicateCandidate.importedTransactionId != null) {
                        val existingTx = transactionDao.getById(duplicateCandidate.importedTransactionId)
                        if (existingTx != null) {
                            transactionDao.update(
                                existingTx.copy(
                                    title = betterMerchant,
                                    cardId = betterCardId ?: existingTx.cardId,
                                    note = "Importado de $betterBankName"
                                )
                            )
                        }
                    }
                }
                // Discard duplicate: do not create another pending notification or insert duplicate!
                return@withContext null
            }

            // Also verify against recent entries in the transactions table
            val recentTxs = transactionDao.getRecentTransactions(minTime)
            val duplicateTx = recentTxs.firstOrNull { tx ->
                Math.abs(tx.amount - parsed.amount) < 0.01 &&
                tx.type.equals(parsed.type, ignoreCase = true) &&
                (isCurrentWallet || areMerchantsSimilar(tx.title, parsed.merchant))
            }
            if (duplicateTx != null) {
                if (matchedCardId != null && duplicateTx.cardId == null) {
                    transactionDao.update(duplicateTx.copy(cardId = matchedCardId))
                }
                return@withContext null
            }

            // =========================================================================
            // PROCESS IMPORT: AUTO or CONFIRM
            // =========================================================================
            val mode = userPreferences.getAutoImportMode() // "AUTO" or "CONFIRM"

            if (mode == "AUTO") {
                val newTx = TransactionEntity(
                    title = parsed.merchant,
                    amount = parsed.amount,
                    type = parsed.type,
                    category = parsed.category,
                    timestamp = parsed.timestamp,
                    note = if (isCurrentWallet) "Importado via Google Carteira" else "Importado automaticamente de ${parsed.bankName}",
                    cardId = matchedCardId,
                    syncUuid = UUID.randomUUID().toString()
                )
                val txId = transactionDao.insert(newTx)

                val entity = ImportedNotificationEntity(
                    packageName = parsed.packageName,
                    bankName = parsed.bankName,
                    rawTitle = parsed.rawTitle,
                    rawText = parsed.rawText,
                    amount = parsed.amount,
                    type = parsed.type,
                    merchant = parsed.merchant,
                    category = parsed.category,
                    cardLastFourDigits = parsed.cardLastFourDigits,
                    matchedCardId = matchedCardId,
                    timestamp = parsed.timestamp,
                    status = "AUTO_IMPORTED",
                    importedTransactionId = txId
                )
                val entityId = importedNotificationDao.insert(entity)
                return@withContext entity.copy(id = entityId)
            } else {
                val entity = ImportedNotificationEntity(
                    packageName = parsed.packageName,
                    bankName = parsed.bankName,
                    rawTitle = parsed.rawTitle,
                    rawText = parsed.rawText,
                    amount = parsed.amount,
                    type = parsed.type,
                    merchant = parsed.merchant,
                    category = parsed.category,
                    cardLastFourDigits = parsed.cardLastFourDigits,
                    matchedCardId = matchedCardId,
                    timestamp = parsed.timestamp,
                    status = "PENDING"
                )
                val entityId = importedNotificationDao.insert(entity)
                return@withContext entity.copy(id = entityId)
            }
        }
    }

    suspend fun cleanupDuplicatePendingNotifications() = withContext(Dispatchers.IO) {
        repoMutex.withLock {
            val pendingList = importedNotificationDao.getPendingNotificationsSync()
            if (pendingList.size <= 1) return@withLock

            val toDeleteIds = mutableListOf<Long>()
            val keptList = mutableListOf<ImportedNotificationEntity>()

            for (item in pendingList) {
                val duplicate = keptList.firstOrNull { kept ->
                    val amountDiff = Math.abs(kept.amount - item.amount)
                    val sameAmount = amountDiff < 0.01
                    val sameType = kept.type.equals(item.type, ignoreCase = true)
                    if (!sameAmount || !sameType) return@firstOrNull false

                    val timeDiff = Math.abs(kept.timestamp - item.timestamp)
                    val closeTime = timeDiff < 600_000L // 10 minutes
                    val sameBank = kept.bankName.equals(item.bankName, ignoreCase = true) || kept.packageName == item.packageName
                    val similarMerchant = areMerchantsSimilar(kept.merchant, item.merchant)

                    closeTime || sameBank || similarMerchant
                }

                if (duplicate != null) {
                    val keptHasGeneric = isGenericMerchant(duplicate.merchant)
                    val itemHasSpecific = isValidSpecificMerchant(item.merchant)
                    val itemHasCard = item.matchedCardId != null && duplicate.matchedCardId == null
                    val itemHasDigits = !item.cardLastFourDigits.isNullOrBlank() && duplicate.cardLastFourDigits.isNullOrBlank()

                    if ((itemHasSpecific && keptHasGeneric) || itemHasCard || itemHasDigits) {
                        val updated = duplicate.copy(
                            merchant = if (itemHasSpecific) item.merchant else duplicate.merchant,
                            matchedCardId = item.matchedCardId ?: duplicate.matchedCardId,
                            cardLastFourDigits = item.cardLastFourDigits ?: duplicate.cardLastFourDigits
                        )
                        importedNotificationDao.update(updated)
                        keptList.remove(duplicate)
                        keptList.add(updated)
                    }

                    toDeleteIds.add(item.id)
                } else {
                    keptList.add(item)
                }
            }

            if (toDeleteIds.isNotEmpty()) {
                importedNotificationDao.deleteByIds(toDeleteIds)
            }
        }
    }

    fun isGenericMerchant(merchant: String): Boolean {
        if (merchant.isBlank()) return true
        val lower = BankNotificationParser.removeAccents(merchant.lowercase(Locale.getDefault())).trim()
        val genericTerms = listOf(
            "compra no cartao", "compra no cartão", "compra no credito", "compra no crédito",
            "compra no debito", "compra no débito", "compra aprovada", "compra realizada",
            "gasto no cartao", "gasto no cartão", "cartao de credito", "cartão de crédito",
            "cartao de debito", "cartão de débito", "cartao final", "cartão final",
            "transferencia", "transferência", "pix", "transferencia / pix", "transferência / pix",
            "estabelecimento", "pagamento", "notificacao bancaria", "notificação bancária",
            "compra", "transacao aprovada", "transação aprovada", "transacao realizada",
            "transação realizada", "nova transacao", "nova transação"
        )
        if (genericTerms.any { lower.contains(it) }) return true

        // Also check if merchant equals or contains known bank names
        val isBankName = BankNotificationParser.BANK_PACKAGE_MAP.values.any { bankName ->
            val normBank = BankNotificationParser.removeAccents(bankName.lowercase(Locale.getDefault()))
            lower == normBank || lower.startsWith("$normBank ") || lower.endsWith(" $normBank")
        }
        if (isBankName) return true

        return false
    }

    private fun isValidSpecificMerchant(merchant: String): Boolean {
        return !isGenericMerchant(merchant)
    }

    private fun areMerchantsSimilar(m1: String, m2: String): Boolean {
        if (m1.isBlank() || m2.isBlank()) return true
        val n1 = BankNotificationParser.removeAccents(m1.lowercase(Locale.getDefault())).trim()
        val n2 = BankNotificationParser.removeAccents(m2.lowercase(Locale.getDefault())).trim()
        if (n1 == n2) return true
        if (n1.contains(n2) || n2.contains(n1)) return true

        // If either one is generic, they match (one is generic alert, other is specific merchant)
        if (isGenericMerchant(m1) || isGenericMerchant(m2)) return true

        val words1 = n1.split(" ", "-", "*", "/", ".").map { it.trim() }.filter { it.length > 2 }.toSet()
        val words2 = n2.split(" ", "-", "*", "/", ".").map { it.trim() }.filter { it.length > 2 }.toSet()
        if (words1.isNotEmpty() && words2.isNotEmpty()) {
            if (words1.intersect(words2).isNotEmpty()) return true
        }
        return false
    }

    suspend fun confirmAndImport(
        notificationId: Long,
        customMerchant: String? = null,
        customAmount: Double? = null,
        customCategory: String? = null,
        customCardId: Long? = null
    ): Long = withContext(Dispatchers.IO) {
        val notification = importedNotificationDao.getById(notificationId) ?: return@withContext 0L
        val merchant = customMerchant?.takeIf { it.isNotBlank() } ?: notification.merchant
        val amount = customAmount?.takeIf { it > 0.0 } ?: notification.amount
        val category = customCategory?.takeIf { it.isNotBlank() } ?: notification.category
        val cardId = customCardId ?: notification.matchedCardId

        val newTx = TransactionEntity(
            title = merchant,
            amount = amount,
            type = notification.type,
            category = category,
            timestamp = notification.timestamp,
            note = "Importado de notificação de ${notification.bankName}",
            cardId = cardId,
            syncUuid = UUID.randomUUID().toString()
        )
        val txId = transactionDao.insert(newTx)

        val updatedEntity = notification.copy(
            merchant = merchant,
            amount = amount,
            category = category,
            matchedCardId = cardId,
            status = "IMPORTED",
            importedTransactionId = txId
        )
        importedNotificationDao.update(updatedEntity)
        txId
    }

    suspend fun discardNotification(notificationId: Long) = withContext(Dispatchers.IO) {
        importedNotificationDao.updateStatus(notificationId, "DISCARDED")
    }

    suspend fun importAllPending(): Int = withContext(Dispatchers.IO) {
        val pendingList = importedNotificationDao.getPendingNotificationsSync()
        var importedCount = 0
        for (item in pendingList) {
            val newTx = TransactionEntity(
                title = item.merchant,
                amount = item.amount,
                type = item.type,
                category = item.category,
                timestamp = item.timestamp,
                note = "Importado de notificação de ${item.bankName}",
                cardId = item.matchedCardId,
                syncUuid = UUID.randomUUID().toString()
            )
            val txId = transactionDao.insert(newTx)
            importedNotificationDao.update(
                item.copy(status = "IMPORTED", importedTransactionId = txId)
            )
            importedCount++
        }
        importedCount
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        importedNotificationDao.clearAll()
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        importedNotificationDao.deleteById(id)
    }
}
