package com.example.data.repository

import com.example.data.local.CreditCardDao
import com.example.data.local.NotificationDao
import com.example.data.local.NotificationItemEntity
import com.example.data.local.TransactionDao
import com.example.data.preferences.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NotificationRepository(
    private val notificationDao: NotificationDao,
    private val transactionDao: TransactionDao,
    private val creditCardDao: CreditCardDao,
    private val userPreferences: UserPreferences
) {

    val allNotifications: Flow<List<NotificationItemEntity>> = notificationDao.getAllNotifications()
    val unreadCount: Flow<Int> = notificationDao.getUnreadCount()

    suspend fun insert(notification: NotificationItemEntity): Long = withContext(Dispatchers.IO) {
        notificationDao.insert(notification)
    }

    suspend fun markAsRead(id: Long) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllAsRead() = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead()
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        notificationDao.deleteById(id)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        notificationDao.clearAll()
    }

    /**
     * Checks database events against user preferences and generates new notifications if needed.
     * Returns a list of freshly created notifications so the caller (NotificationHelper) can dispatch system alerts.
     */
    suspend fun checkAndGeneratePendingNotifications(): List<NotificationItemEntity> = withContext(Dispatchers.IO) {
        if (!userPreferences.notificationsEnabled.value) {
            return@withContext emptyList()
        }

        val newlyCreated = mutableListOf<NotificationItemEntity>()
        val calNow = Calendar.getInstance()
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayEnd = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val advanceDays = userPreferences.notificationAdvanceDays.value.coerceAtLeast(0)
        val targetDayEnd = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, advanceDays)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        val dateFormatter = SimpleDateFormat("dd/MM", Locale("pt", "BR"))

        // 1. Check Recurring Transactions / Scheduled bills
        if (userPreferences.notifyRecurring.value) {
            val transactions = transactionDao.getTransactionsBetweenSync(todayStart, targetDayEnd)
            for (tx in transactions) {
                if (tx.isDeleted) continue
                // We notify for recurring or upcoming planned expenses/incomes
                val isRelevant = tx.isRecurring || tx.isInstallment || tx.timestamp >= todayStart

                if (isRelevant) {
                    val alreadyNotified = notificationDao.countSimilarToday(
                        type = "RECURRING",
                        refId = tx.id,
                        startOfDay = todayStart,
                        endOfDay = todayEnd
                    ) > 0

                    if (!alreadyNotified) {
                        val txCal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                        val isToday = txCal.get(Calendar.DAY_OF_YEAR) == calNow.get(Calendar.DAY_OF_YEAR) &&
                                txCal.get(Calendar.YEAR) == calNow.get(Calendar.YEAR)

                        val dayText = if (isToday) "Vence hoje" else "Vence em ${dateFormatter.format(tx.timestamp)}"
                        val isExpense = tx.type.equals("EXPENSE", ignoreCase = true)
                        val title = if (isExpense) "🔔 Conta a Pagar: ${tx.title}" else "💰 Recebimento Agendado: ${tx.title}"
                        val message = "$dayText (${currencyFormatter.format(tx.amount)}). Categoria: ${tx.category}."

                        val notification = NotificationItemEntity(
                            title = title,
                            message = message,
                            type = "RECURRING",
                            timestamp = System.currentTimeMillis(),
                            isRead = false,
                            referenceId = tx.id,
                            actionRoute = "transactions",
                            severity = if (isToday && isExpense) "URGENT" else "INFO"
                        )
                        val id = notificationDao.insert(notification)
                        newlyCreated.add(notification.copy(id = id))
                    }
                }
            }
        }

        // 2. Check Credit Cards (Closing day and Due day)
        val cards = creditCardDao.getAllCardsSync().filter { !it.isDeleted }
        val currentDayOfMonth = calNow.get(Calendar.DAY_OF_MONTH)

        for (card in cards) {
            // Check Closing Day
            if (userPreferences.notifyCardClosing.value && card.closingDay > 0) {
                val diffClosing = card.closingDay - currentDayOfMonth
                val isTargetClosing = if (advanceDays == 0) diffClosing == 0 else diffClosing in 0..advanceDays

                if (isTargetClosing) {
                    val alreadyNotified = notificationDao.countSimilarToday(
                        type = "CARD_CLOSING",
                        refId = card.id,
                        startOfDay = todayStart,
                        endOfDay = todayEnd
                    ) > 0

                    if (!alreadyNotified) {
                        val whenText = if (diffClosing == 0) "fecha HOJE (Dia ${card.closingDay})" else "fecha em $diffClosing dia(s) (Dia ${card.closingDay})"
                        val notif = NotificationItemEntity(
                            title = "💳 Fatura do Cartão Fechando",
                            message = "A fatura do seu cartão ${card.name} $whenText. Aproveite as melhores datas para novas compras!",
                            type = "CARD_CLOSING",
                            timestamp = System.currentTimeMillis(),
                            isRead = false,
                            referenceId = card.id,
                            actionRoute = "cards",
                            severity = "INFO"
                        )
                        val id = notificationDao.insert(notif)
                        newlyCreated.add(notif.copy(id = id))
                    }
                }
            }

            // Check Due Day
            if (userPreferences.notifyCardDue.value && card.dueDay > 0) {
                val diffDue = card.dueDay - currentDayOfMonth
                val isTargetDue = if (advanceDays == 0) diffDue == 0 else diffDue in 0..advanceDays

                if (isTargetDue) {
                    val alreadyNotified = notificationDao.countSimilarToday(
                        type = "CARD_DUE",
                        refId = card.id,
                        startOfDay = todayStart,
                        endOfDay = todayEnd
                    ) > 0

                    if (!alreadyNotified) {
                        val whenText = if (diffDue == 0) "vence HOJE (Dia ${card.dueDay})" else "vence em $diffDue dia(s) (Dia ${card.dueDay})"
                        val notif = NotificationItemEntity(
                            title = "⚠️ Vencimento de Cartão: ${card.name}",
                            message = "A fatura do cartão ${card.name} $whenText. Evite juros e encargos efetuando o pagamento.",
                            type = "CARD_DUE",
                            timestamp = System.currentTimeMillis(),
                            isRead = false,
                            referenceId = card.id,
                            actionRoute = "cards",
                            severity = if (diffDue == 0) "URGENT" else "WARNING"
                        )
                        val id = notificationDao.insert(notif)
                        newlyCreated.add(notif.copy(id = id))
                    }
                }
            }
        }

        // 3. Check Monthly Budget Limit
        val budgetLimit = userPreferences.monthlyBudgetLimit.value
        if (userPreferences.notifyBudgetLimit.value && budgetLimit > 0.0) {
            val monthStart = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val currentMonthTransactions = transactionDao.getTransactionsBetweenSync(monthStart, todayEnd)
            val totalExpense = currentMonthTransactions
                .filter { !it.isDeleted && it.type.equals("EXPENSE", ignoreCase = true) }
                .sumOf { it.amount }

            val percentage = (totalExpense / budgetLimit) * 100.0
            if (percentage >= 80.0) {
                val alreadyNotified = notificationDao.countSimilarToday(
                    type = "BUDGET_ALERT",
                    refId = null,
                    startOfDay = todayStart,
                    endOfDay = todayEnd
                ) > 0

                if (!alreadyNotified) {
                    val severity = if (percentage >= 100.0) "URGENT" else "WARNING"
                    val title = if (percentage >= 100.0) "🚨 Limite de Orçamento Ultrapassado!" else "⚠️ Alerta de Orçamento (${percentage.toInt()}%)"
                    val message = "Você já gastou ${currencyFormatter.format(totalExpense)} do limite mensal de ${currencyFormatter.format(budgetLimit)}."

                    val notif = NotificationItemEntity(
                        title = title,
                        message = message,
                        type = "BUDGET_ALERT",
                        timestamp = System.currentTimeMillis(),
                        isRead = false,
                        referenceId = null,
                        actionRoute = "dashboard",
                        severity = severity
                    )
                    val id = notificationDao.insert(notif)
                    newlyCreated.add(notif.copy(id = id))
                }
            }
        }

        // 4. Daily Reminder (if enabled)
        if (userPreferences.notifyDailyReminder.value) {
            val alreadyNotified = notificationDao.countSimilarToday(
                type = "SYSTEM_REMINDER",
                refId = null,
                startOfDay = todayStart,
                endOfDay = todayEnd
            ) > 0

            if (!alreadyNotified) {
                val notif = NotificationItemEntity(
                    title = "✍️ BUMoney: Lembrete Diário",
                    message = "Não se esqueça de registrar seus gastos e ganhos de hoje para manter seu controle financeiro impecável!",
                    type = "SYSTEM_REMINDER",
                    timestamp = System.currentTimeMillis(),
                    isRead = false,
                    referenceId = null,
                    actionRoute = "dashboard",
                    severity = "INFO"
                )
                val id = notificationDao.insert(notif)
                newlyCreated.add(notif.copy(id = id))
            }
        }

        newlyCreated
    }
}
