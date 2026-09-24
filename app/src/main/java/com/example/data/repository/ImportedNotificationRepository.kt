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
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

class ImportedNotificationRepository(
    private val importedNotificationDao: ImportedNotificationDao,
    private val transactionDao: TransactionDao,
    private val creditCardDao: CreditCardDao,
    private val userPreferences: UserPreferences
) {

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
    ): ImportedNotificationEntity? = withContext(Dispatchers.IO) {
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
        // DEDUPLICATION & GOOGLE WALLET + CARD MERGING (3-minute window)
        // =========================================================================
        val minTime = System.currentTimeMillis() - 180_000L
        val isCurrentWallet = BankNotificationParser.isWalletPackage(packageName)

        if (userPreferences.isIgnoreNotificationDuplicatesEnabled()) {
            val recentSameAmount = importedNotificationDao.getRecentByAmount(parsed.amount, minTime)
            for (recent in recentSameAmount) {
                val isRecentWallet = BankNotificationParser.isWalletPackage(recent.packageName)
                val sameType = recent.type.equals(parsed.type, ignoreCase = true)
                val isSimilarMerchant = areMerchantsSimilar(recent.merchant, parsed.merchant)

                // Match condition: Same amount within 3 minutes and either one is a Wallet OR merchants match
                if (sameType && (isCurrentWallet || isRecentWallet || isSimilarMerchant)) {
                    // Case 1: The previously recorded item was from Google Wallet, and now the actual Bank notification arrived.
                    // The Bank notification brings richer data (real bank name, card digits, specific card match).
                    // We enrich the existing transaction/notification and discard the duplicate.
                    if (isRecentWallet && !isCurrentWallet) {
                        val betterMerchant = if (isValidSpecificMerchant(parsed.merchant)) parsed.merchant else recent.merchant
                        val betterCardId = matchedCardId ?: recent.matchedCardId
                        val betterCardDigits = parsed.cardLastFourDigits ?: recent.cardLastFourDigits

                        val updatedRecent = recent.copy(
                            bankName = parsed.bankName,
                            merchant = betterMerchant,
                            cardLastFourDigits = betterCardDigits,
                            matchedCardId = betterCardId,
                            rawText = "${recent.rawText} | ${parsed.rawText}"
                        )
                        importedNotificationDao.update(updatedRecent)

                        if (recent.importedTransactionId != null) {
                            val existingTx = transactionDao.getById(recent.importedTransactionId)
                            if (existingTx != null) {
                                transactionDao.update(
                                    existingTx.copy(
                                        title = betterMerchant,
                                        cardId = betterCardId ?: existingTx.cardId,
                                        note = "Importado de ${parsed.bankName} (via Google Carteira)"
                                    )
                                )
                            }
                        }
                        return@withContext null
                    }

                    // Case 2: The Bank notification was recorded first, and Google Wallet notification arrived second.
                    // Or both are duplicate notifications from the same bank.
                    // Safely ignore the incoming notification as a duplicate!
                    return@withContext null
                }
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

    private fun areMerchantsSimilar(m1: String, m2: String): Boolean {
        if (m1.isBlank() || m2.isBlank()) return true
        val n1 = BankNotificationParser.removeAccents(m1.lowercase(Locale.getDefault())).trim()
        val n2 = BankNotificationParser.removeAccents(m2.lowercase(Locale.getDefault())).trim()
        if (n1 == n2) return true
        if (n1.contains(n2) || n2.contains(n1)) return true

        val words1 = n1.split(" ", "-", "*", "/", ".").map { it.trim() }.filter { it.length > 2 }.toSet()
        val words2 = n2.split(" ", "-", "*", "/", ".").map { it.trim() }.filter { it.length > 2 }.toSet()
        if (words1.isNotEmpty() && words2.isNotEmpty()) {
            if (words1.intersect(words2).isNotEmpty()) return true
        }
        return false
    }

    private fun isValidSpecificMerchant(merchant: String): Boolean {
        if (merchant.isBlank()) return false
        val lower = merchant.lowercase(Locale.getDefault())
        if (lower == "gasto no cartao" || lower == "gasto no cartão" ||
            lower == "transferencia / pix" || lower == "transferência / pix" ||
            lower == "estabelecimento" || lower == "pagamento") {
            return false
        }
        return true
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
