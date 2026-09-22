package com.example.data.repository

import com.example.data.local.TransactionDao
import com.example.data.local.TransactionEntity
import com.example.data.model.Categories
import com.example.data.model.TransactionType
import com.example.data.p2p.P2PSyncManager
import com.example.data.preferences.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val userPreferences: UserPreferences,
    var p2pSyncManager: P2PSyncManager? = null
) {

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    fun getTransactionsBetween(startEpoch: Long, endEpoch: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsBetween(startEpoch, endEpoch)
    }

    suspend fun insert(transaction: TransactionEntity): Long = withContext(Dispatchers.IO) {
        val id = transactionDao.insert(transaction)
        val inserted = transaction.copy(id = id)
        p2pSyncManager?.broadcastTransactionUpsert(inserted)
        id
    }

    suspend fun insertAll(transactions: List<TransactionEntity>) = withContext(Dispatchers.IO) {
        transactionDao.insertAll(transactions)
        transactions.forEach {
            p2pSyncManager?.broadcastTransactionUpsert(it)
        }
    }

    fun getInstallmentsByGroup(groupId: String): Flow<List<TransactionEntity>> {
        return transactionDao.getInstallmentsByGroup(groupId)
    }

    suspend fun getInstallmentsByGroupSync(groupId: String): List<TransactionEntity> = withContext(Dispatchers.IO) {
        transactionDao.getInstallmentsByGroupSync(groupId)
    }

    suspend fun deleteInstallmentGroup(groupId: String) = withContext(Dispatchers.IO) {
        transactionDao.markInstallmentGroupDeleted(groupId)
        p2pSyncManager?.broadcastTransactionDelete(syncUuid = "", groupId = groupId)
    }

    suspend fun createInstallments(
        title: String,
        totalAmount: Double,
        category: String,
        startTimestamp: Long,
        note: String,
        cardId: Long?,
        totalInstallments: Int
    ): String = withContext(Dispatchers.IO) {
        val groupId = java.util.UUID.randomUUID().toString()
        val count = totalInstallments.coerceAtLeast(1)
        val rawBase = totalAmount / count
        val baseAmount = Math.round(rawBase * 100.0) / 100.0
        val remainder = Math.round((totalAmount - (baseAmount * count)) * 100.0) / 100.0

        val list = mutableListOf<TransactionEntity>()
        for (i in 1..count) {
            val cal = Calendar.getInstance().apply {
                timeInMillis = startTimestamp
                add(Calendar.MONTH, i - 1)
            }
            val installmentAmount = if (i == 1) (baseAmount + remainder) else baseAmount
            list.add(
                TransactionEntity(
                    title = title.trim(),
                    amount = installmentAmount,
                    type = TransactionType.EXPENSE.name,
                    category = category,
                    timestamp = cal.timeInMillis,
                    note = note.trim(),
                    cardId = cardId,
                    isInstallment = true,
                    installmentNumber = i,
                    totalInstallments = count,
                    installmentGroupId = groupId,
                    isAnticipated = false
                )
            )
        }
        transactionDao.insertAll(list)
        list.forEach { p2pSyncManager?.broadcastTransactionUpsert(it) }
        groupId
    }

    suspend fun createRecurring(
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        startTimestamp: Long,
        note: String,
        cardId: Long?,
        monthsCount: Int = 12,
        intervalMonths: Int = 1,
        isIndefinite: Boolean = true
    ): String = withContext(Dispatchers.IO) {
        val groupId = java.util.UUID.randomUUID().toString()
        val step = intervalMonths.coerceIn(1, 12)
        val count = if (isIndefinite) 60 else monthsCount.coerceIn(1, 60)
        val list = mutableListOf<TransactionEntity>()
        for (i in 0 until count) {
            val cal = Calendar.getInstance().apply {
                timeInMillis = startTimestamp
                add(Calendar.MONTH, i * step)
            }
            list.add(
                TransactionEntity(
                    title = title.trim(),
                    amount = amount,
                    type = type.name,
                    category = category,
                    timestamp = cal.timeInMillis,
                    note = note.trim(),
                    cardId = if (type == TransactionType.EXPENSE) cardId else null,
                    isRecurring = true,
                    recurringGroupId = groupId
                )
            )
        }
        transactionDao.insertAll(list)
        list.forEach { p2pSyncManager?.broadcastTransactionUpsert(it) }
        groupId
    }

    suspend fun deleteRecurringGroup(groupId: String) = withContext(Dispatchers.IO) {
        transactionDao.markRecurringGroupDeleted(groupId)
        p2pSyncManager?.broadcastTransactionDelete(syncUuid = "", groupId = groupId)
    }

    fun getRecurringByGroup(groupId: String): Flow<List<TransactionEntity>> {
        return transactionDao.getRecurringByGroup(groupId)
    }

    suspend fun getRecurringByGroupSync(groupId: String): List<TransactionEntity> = withContext(Dispatchers.IO) {
        transactionDao.getRecurringByGroupSync(groupId)
    }

    suspend fun anticipateInstallments(
        groupId: String,
        numberOfInstallments: Int,
        targetTimestamp: Long = System.currentTimeMillis()
    ) = withContext(Dispatchers.IO) {
        val allGroup = transactionDao.getInstallmentsByGroupSync(groupId)
        val calTarget = Calendar.getInstance().apply { timeInMillis = targetTimestamp }
        val targetMonth = calTarget.get(Calendar.MONTH)
        val targetYear = calTarget.get(Calendar.YEAR)

        val futureInstallments = allGroup.filter { item ->
            val itemCal = Calendar.getInstance().apply { timeInMillis = item.timestamp }
            val itemMonth = itemCal.get(Calendar.MONTH)
            val itemYear = itemCal.get(Calendar.YEAR)
            (itemYear > targetYear) || (itemYear == targetYear && itemMonth > targetMonth)
        }.sortedBy { it.installmentNumber }

        val toAnticipate = futureInstallments.take(numberOfInstallments)
        toAnticipate.forEach { item ->
            val updated = item.copy(
                timestamp = targetTimestamp,
                isAnticipated = true,
                updatedAt = System.currentTimeMillis()
            )
            transactionDao.update(updated)
            p2pSyncManager?.broadcastTransactionUpsert(updated)
        }
    }

    suspend fun update(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        val updated = transaction.copy(updatedAt = System.currentTimeMillis())
        transactionDao.update(updated)
        p2pSyncManager?.broadcastTransactionUpsert(updated)
    }

    suspend fun delete(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        val tombstone = transaction.copy(isDeleted = true, updatedAt = System.currentTimeMillis())
        transactionDao.update(tombstone)
        p2pSyncManager?.broadcastTransactionDelete(syncUuid = transaction.syncUuid)
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        val all = transactionDao.getAllRawTransactions().firstOrNull { it.id == id }
        if (all != null) {
            delete(all)
        } else {
            transactionDao.deleteById(id)
        }
    }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        transactionDao.deleteAllTransactions()
    }

    suspend fun resetToSampleData() = withContext(Dispatchers.IO) {
        transactionDao.deleteAllTransactions()
        userPreferences.setInitialDataSeeded(false)
        seedInitialDataIfEmpty()
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        if (!userPreferences.isInitialDataSeeded()) {
            userPreferences.setInitialDataSeeded(true)
            val calendar = Calendar.getInstance()
            val now = calendar.timeInMillis

            // Helper to get time at specific days relative to today
            fun timeAtDaysAgo(daysAgo: Int, hour: Int = 12): Long {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_MONTH, -daysAgo)
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                return cal.timeInMillis
            }

            val sampleTransactions = listOf(
                TransactionEntity(
                    title = "Salário Mensal",
                    amount = 5500.00,
                    type = TransactionType.INCOME.name,
                    category = Categories.Salary.name,
                    timestamp = timeAtDaysAgo(15, 9),
                    note = "Depósito CLT da empresa"
                ),
                TransactionEntity(
                    title = "Projeto Freelance UI/UX",
                    amount = 1450.00,
                    type = TransactionType.INCOME.name,
                    category = Categories.Freelance.name,
                    timestamp = timeAtDaysAgo(5, 16),
                    note = "Design de app mobile"
                ),
                TransactionEntity(
                    title = "Supermercado do Mês",
                    amount = 680.40,
                    type = TransactionType.EXPENSE.name,
                    category = Categories.Food.name,
                    timestamp = timeAtDaysAgo(14, 11),
                    note = "Compras de mantimentos e hortifrúti"
                ),
                TransactionEntity(
                    title = "Aluguel & Condomínio",
                    amount = 1750.00,
                    type = TransactionType.EXPENSE.name,
                    category = Categories.Housing.name,
                    timestamp = timeAtDaysAgo(12, 10),
                    note = "Pagamento do boleto"
                ),
                TransactionEntity(
                    title = "Combustível Posto Ipiranga",
                    amount = 220.00,
                    type = TransactionType.EXPENSE.name,
                    category = Categories.Transport.name,
                    timestamp = timeAtDaysAgo(9, 14),
                    note = "Tanque cheio"
                ),
                TransactionEntity(
                    title = "Conta de Luz & Internet",
                    amount = 295.50,
                    type = TransactionType.EXPENSE.name,
                    category = Categories.Bills.name,
                    timestamp = timeAtDaysAgo(8, 15),
                    note = "Fatura Cemig + Fibra 500MB"
                ),
                TransactionEntity(
                    title = "Jantar Restaurante Italiano",
                    amount = 185.00,
                    type = TransactionType.EXPENSE.name,
                    category = Categories.Food.name,
                    timestamp = timeAtDaysAgo(6, 21),
                    note = "Massa com a família"
                ),
                TransactionEntity(
                    title = "Cinema & Lazer Fim de Semana",
                    amount = 110.00,
                    type = TransactionType.EXPENSE.name,
                    category = Categories.Leisure.name,
                    timestamp = timeAtDaysAgo(3, 19),
                    note = "Ingressos e pipoca"
                ),
                TransactionEntity(
                    title = "Farmácia & Vitaminas",
                    amount = 145.80,
                    type = TransactionType.EXPENSE.name,
                    category = Categories.Health.name,
                    timestamp = timeAtDaysAgo(2, 11),
                    note = "Suplementos e remédios"
                ),
                TransactionEntity(
                    title = "Curso de Kotlin & Compose",
                    amount = 199.90,
                    type = TransactionType.EXPENSE.name,
                    category = Categories.Education.name,
                    timestamp = timeAtDaysAgo(1, 14),
                    note = "Aperfeiçoamento profissional"
                )
            )
            transactionDao.insertAll(sampleTransactions)
        }
    }

    suspend fun updateCategoryName(oldCategory: String, newCategory: String, type: String) = withContext(Dispatchers.IO) {
        transactionDao.updateCategoryName(oldCategory, newCategory, type)
    }
}

