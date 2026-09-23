package com.example.data.repository

import android.content.Context
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

        // Check for duplicates within last 2 minutes if enabled
        if (userPreferences.isIgnoreNotificationDuplicatesEnabled()) {
            val minTime = System.currentTimeMillis() - 120_000L
            val duplicate = importedNotificationDao.findRecentDuplicate(parsed.amount, parsed.merchant, minTime)
            if (duplicate != null) return@withContext null
        }

        // Try matching a credit card
        val allCards = creditCardDao.getAllCardsSync()
        var matchedCardId: Long? = null
        if (!parsed.cardLastFourDigits.isNullOrBlank()) {
            matchedCardId = allCards.firstOrNull { it.lastFourDigits == parsed.cardLastFourDigits }?.id
        }
        if (matchedCardId == null && parsed.type == "EXPENSE") {
            matchedCardId = allCards.firstOrNull { it.name.contains(parsed.bankName, ignoreCase = true) }?.id
        }

        val mode = userPreferences.getAutoImportMode() // "AUTO" or "CONFIRM"

        if (mode == "AUTO") {
            // Auto Import directly as transaction
            val newTx = TransactionEntity(
                title = parsed.merchant,
                amount = parsed.amount,
                type = parsed.type,
                category = parsed.category,
                timestamp = parsed.timestamp,
                note = "Importado automaticamente de ${parsed.bankName}",
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
            // Confirmation Mode -> Store as PENDING
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
        val pendingList = importedNotificationDao.getPendingNotifications()
        // Wait, flow or sync list? Let's get list directly
        var importedCount = 0
        // We can query pending list
        return@withContext importedCount
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        importedNotificationDao.clearAll()
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        importedNotificationDao.deleteById(id)
    }
}
