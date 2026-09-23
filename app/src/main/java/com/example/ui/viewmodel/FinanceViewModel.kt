package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.CreditCardEntity
import com.example.data.local.TransactionEntity
import com.example.data.model.Categories
import com.example.data.model.CategoryItem
import com.example.data.model.TransactionType
import com.example.data.preferences.AppThemeMode
import com.example.data.preferences.AppThemeColor
import com.example.data.preferences.UserPreferences
import com.example.data.repository.CategoryRepository
import com.example.data.repository.CreditCardRepository
import com.example.data.repository.TransactionRepository
import com.example.util.CreditCardBillingHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class CategorySpend(
    val category: CategoryItem,
    val total: Double,
    val percentage: Float,
    val count: Int
)

data class DailySpend(
    val dayOfMonth: Int,
    val amount: Double,           // Daily expense (vermelho)
    val incomeAmount: Double = 0.0 // Daily income (verde)
)

data class UpcomingScheduleData(
    val daysAhead: Int = 15,
    val totalExpenses: Double = 0.0,
    val totalIncomes: Double = 0.0,
    val netBalance: Double = 0.0,
    val transactions: List<TransactionEntity> = emptyList(),
    val expenseCount: Int = 0,
    val incomeCount: Int = 0
)

data class MonthPeriod(
    val year: Int,
    val month: Int // 0-based Calendar.MONTH
) {
    val startEpoch: Long
        get() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

    val endEpoch: Long
        get() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            return cal.timeInMillis
        }

    val daysInMonth: Int
        get() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
}

data class MonthlyTrendPoint(
    val year: Int,
    val month: Int,
    val label: String,
    val totalExpense: Double,
    val totalIncome: Double,
    val netBalance: Double
)

data class CategoryMonthlyAverage(
    val category: CategoryItem,
    val averageMonthlyAmount: Double,
    val totalAmount: Double,
    val currentMonthAmount: Double,
    val diffFromAveragePercent: Float, // +15% or -10%
    val monthCountWithSpend: Int
)

data class PeriodComparisonData(
    val currentPeriodLabel: String,
    val previousPeriodLabel: String,
    val currentExpense: Double,
    val previousExpense: Double,
    val currentIncome: Double,
    val previousIncome: Double,
    val expenseDiffPercent: Float, // e.g. +12.5% or -5.0%
    val incomeDiffPercent: Float,
    val categoryDiffs: List<CategoryComparisonItem> = emptyList()
)

data class CategoryComparisonItem(
    val category: CategoryItem,
    val currentAmount: Double,
    val previousAmount: Double,
    val diffAmount: Double,
    val diffPercent: Float
)

data class AdvancedAnalytics(
    val trendMonths: List<MonthlyTrendPoint> = emptyList(),
    val categoryAverages: List<CategoryMonthlyAverage> = emptyList(),
    val comparison: PeriodComparisonData? = null,
    val monthsAnalyzedCount: Int = 6
)

data class MonthlyAnalytics(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,           // Total gasto competência (o que gastou no mês)
    val totalCashOutflow: Double = 0.0,       // Total saído de fato no mês (dinheiro/débito + faturas de cartão pagas no mês)
    val previousBalance: Double = 0.0,        // Saldo acumulado dos meses anteriores
    val balance: Double = 0.0,                // Saldo operacional do mês atual (entradas - saídas de fato)
    val accumulatedBalance: Double = 0.0,     // Saldo final acumulado (previousBalance + balance)
    val accrualBalance: Double = 0.0,         // Balanço de competência (entradas - total gasto)
    val savingsRate: Float = 0f,
    val categoryExpenses: List<CategorySpend> = emptyList(),
    val categoryIncomes: List<CategorySpend> = emptyList(),
    val dailyExpenses: List<DailySpend> = emptyList(),
    val pendingCardExpenses: Double = 0.0     // Gastos no cartão deste mês cuja fatura sairá nos meses seguintes
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository
    private val cardRepository: CreditCardRepository
    private val categoryRepository: CategoryRepository
    private val userPreferences: UserPreferences = UserPreferences.getInstance(application)
    val p2pSyncManager: com.example.data.p2p.P2PSyncManager

    val themeMode: StateFlow<AppThemeMode> = userPreferences.themeMode
    val themeColor: StateFlow<AppThemeColor> = userPreferences.themeColor
    val monthlyBudgetLimit: StateFlow<Double> = userPreferences.monthlyBudgetLimit
    val hideBalances: StateFlow<Boolean> = userPreferences.hideBalances
    val defaultCurrency: StateFlow<String> = userPreferences.defaultCurrency
    val p2pSyncEnabled: StateFlow<Boolean> = userPreferences.p2pSyncEnabled
    val p2pSyncKey: StateFlow<String> = userPreferences.p2pSyncKey

    val p2pSyncStatus: StateFlow<com.example.data.p2p.P2PSyncStatus>

    // All registered cards
    val creditCards: StateFlow<List<CreditCardEntity>>

    // Custom Categories
    val customCategories: StateFlow<List<CategoryItem>>

    // Combined Categories
    val allExpenseCategories: StateFlow<List<CategoryItem>>
    val allIncomeCategories: StateFlow<List<CategoryItem>>

    private val backupManager: com.example.data.backup.LocalBackupManager
    private val _backupFiles = MutableStateFlow<List<java.io.File>>(emptyList())
    val backupFiles: StateFlow<List<java.io.File>> = _backupFiles.asStateFlow()

    private val _backupStatusMessage = MutableStateFlow<String?>(null)
    val backupStatusMessage: StateFlow<String?> = _backupStatusMessage.asStateFlow()

    val backupInterval: StateFlow<String> = userPreferences.backupInterval
    val lastBackupTimestamp: StateFlow<Long> = userPreferences.lastBackupTimestamp
    val githubRepo: StateFlow<String> = userPreferences.githubRepo

    private val updateManager = com.example.util.GitHubUpdateManager()
    private val _updateCheckStatus = MutableStateFlow<com.example.util.UpdateCheckResult?>(null)
    val updateCheckStatus: StateFlow<com.example.util.UpdateCheckResult?> = _updateCheckStatus.asStateFlow()

    private val _showTopUpdateBanner = MutableStateFlow(false)
    val showTopUpdateBanner: StateFlow<Boolean> = _showTopUpdateBanner.asStateFlow()

    private val _isCheckingUpdates = MutableStateFlow(false)
    val isCheckingUpdates: StateFlow<Boolean> = _isCheckingUpdates.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Int?>(null)
    val downloadProgress: StateFlow<Int?> = _downloadProgress.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    fun getInstalledVersionName(): String {
        return try {
            val app = getApplication<Application>()
            val pInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                app.packageManager.getPackageInfo(
                    app.packageName,
                    android.content.pm.PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                app.packageManager.getPackageInfo(app.packageName, 0)
            }
            pInfo.versionName ?: com.example.BuildConfig.VERSION_NAME
        } catch (e: Exception) {
            com.example.BuildConfig.VERSION_NAME
        }
    }

    val installedVersionName: StateFlow<String> = MutableStateFlow(getInstalledVersionName()).asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        p2pSyncManager = com.example.data.p2p.P2PSyncManager(application, db, userPreferences)
        repository = TransactionRepository(db.transactionDao(), userPreferences, p2pSyncManager)
        cardRepository = CreditCardRepository(db.creditCardDao(), userPreferences, p2pSyncManager)
        categoryRepository = CategoryRepository(db.customCategoryDao(), p2pSyncManager)
        backupManager = com.example.data.backup.LocalBackupManager(application, db, userPreferences)
        refreshBackupList()
        checkScheduledBackup()

        p2pSyncStatus = p2pSyncManager.syncStatus

        creditCards = cardRepository.allCards
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        customCategories = categoryRepository.allCustomCategories
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allExpenseCategories = customCategories.map { customList ->
            Categories.expenseCategories + customList.filter { it.type == TransactionType.EXPENSE }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Categories.expenseCategories)

        allIncomeCategories = customCategories.map { customList ->
            Categories.incomeCategories + customList.filter { it.type == TransactionType.INCOME }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Categories.incomeCategories)

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            cardRepository.seedInitialCardsIfEmpty()

            // Restore P2P if previously enabled
            val savedKey = userPreferences.p2pSyncKey.value
            if (userPreferences.p2pSyncEnabled.value && savedKey.isNotBlank()) {
                p2pSyncManager.startSync(savedKey)
            }

            // Automatic background check for new releases on startup
            checkForUpdates(isAutoCheck = true)
        }
    }

    // Selected Month & Year
    private val nowCal = Calendar.getInstance()
    private val _currentPeriod = MutableStateFlow(
        MonthPeriod(nowCal.get(Calendar.YEAR), nowCal.get(Calendar.MONTH))
    )
    val currentPeriod: StateFlow<MonthPeriod> = _currentPeriod.asStateFlow()

    // Filters for transaction list
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _typeFilter = MutableStateFlow<TransactionType?>(null)
    val typeFilter: StateFlow<TransactionType?> = _typeFilter.asStateFlow()

    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter: StateFlow<String?> = _categoryFilter.asStateFlow()

    // Selected month's raw transactions
    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyTransactions: StateFlow<List<TransactionEntity>> = _currentPeriod
        .flatMapLatest { period ->
            repository.getTransactionsBetween(period.startEpoch, period.endEpoch)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered transactions for the transactions list tab
    val filteredMonthlyTransactions: StateFlow<List<TransactionEntity>> = combine(
        monthlyTransactions,
        _searchQuery,
        _typeFilter,
        _categoryFilter
    ) { list, query, type, cat ->
        list.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.title.contains(query, ignoreCase = true) ||
                item.category.contains(query, ignoreCase = true) ||
                item.note.contains(query, ignoreCase = true)

            val matchesType = type == null || item.type == type.name
            val matchesCategory = cat == null || item.category.equals(cat, ignoreCase = true)

            matchesQuery && matchesType && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All raw transactions flow for cross-month calculations (carry-over and card payment dates)
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Upcoming scheduled transactions range (7, 15, 30 days)
    private val _upcomingDaysRange = MutableStateFlow(15)
    val upcomingDaysRange: StateFlow<Int> = _upcomingDaysRange.asStateFlow()

    fun setUpcomingDaysRange(days: Int) {
        _upcomingDaysRange.value = days
    }

    val upcomingScheduleData: StateFlow<UpcomingScheduleData> = combine(
        allTransactions,
        _upcomingDaysRange
    ) { transactions, daysAhead ->
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStart = now.timeInMillis

        val endCal = Calendar.getInstance().apply {
            timeInMillis = todayStart
            add(Calendar.DAY_OF_YEAR, daysAhead)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val endMillis = endCal.timeInMillis

        val upcoming = transactions
            .filter { !it.isDeleted && it.timestamp in todayStart..endMillis }
            .sortedBy { it.timestamp }

        val expenses = upcoming.filter { it.type == TransactionType.EXPENSE.name }
        val incomes = upcoming.filter { it.type == TransactionType.INCOME.name }

        val totalExp = expenses.sumOf { it.amount }
        val totalInc = incomes.sumOf { it.amount }

        UpcomingScheduleData(
            daysAhead = daysAhead,
            totalExpenses = totalExp,
            totalIncomes = totalInc,
            netBalance = totalInc - totalExp,
            transactions = upcoming,
            expenseCount = expenses.size,
            incomeCount = incomes.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UpcomingScheduleData())

    // Analytics state computed automatically with carry-over and cash outflow vs accrual
    val monthlyAnalytics: StateFlow<MonthlyAnalytics> = combine(
        allTransactions,
        monthlyTransactions,
        _currentPeriod,
        customCategories,
        creditCards
    ) { allTx, monthTx, period, customCats, cards ->
        val cardMap = cards.associateBy { it.id }

        // 1. Calculate Carry-over (Saldo Anterior Acumulado de todos os meses antes de period.startEpoch)
        // Regra de saída de dinheiro:
        // - Se for receita (INCOME): entra na data da transação
        // - Se for despesa (EXPENSE):
        //     - Se for em cartão de crédito (cardId != null e card encontrado): o dinheiro sai no mês/ano do vencimento da fatura
        //     - Se for à vista/dinheiro/débito (sem cartão): o dinheiro sai na data da transação
        var historicalIncomeBefore = 0.0
        var historicalOutflowBefore = 0.0

        for (tx in allTx) {
            if (tx.isDeleted) continue

            if (tx.type == TransactionType.INCOME.name) {
                if (tx.timestamp < period.startEpoch) {
                    historicalIncomeBefore += tx.amount
                }
            } else {
                val card = tx.cardId?.let { cardMap[it] }
                if (card != null) {
                    val (dueYear, dueMonth) = CreditCardBillingHelper.calculatePaymentMonthAndYear(tx.timestamp, card)
                    // Verifica se a fatura venceu antes do mês atual
                    if (dueYear < period.year || (dueYear == period.year && dueMonth < period.month)) {
                        historicalOutflowBefore += tx.amount
                    }
                } else {
                    if (tx.timestamp < period.startEpoch) {
                        historicalOutflowBefore += tx.amount
                    }
                }
            }
        }
        val previousBalance = historicalIncomeBefore - historicalOutflowBefore

        // 2. Transações e gastos deste mês (Competência / O que gastou no mês)
        var incomeSum = 0.0
        var expenseAccrualSum = 0.0
        var pendingCardExpensesSum = 0.0

        val expenseByCat = mutableMapOf<String, Double>()
        val expenseCountByCat = mutableMapOf<String, Int>()

        val incomeByCat = mutableMapOf<String, Double>()
        val incomeCountByCat = mutableMapOf<String, Int>()

        val dailyExpenseMap = mutableMapOf<Int, Double>()
        val dailyIncomeMap = mutableMapOf<Int, Double>()

        val cal = Calendar.getInstance()
        for (item in monthTx) {
            if (item.type == TransactionType.INCOME.name) {
                incomeSum += item.amount
                incomeByCat[item.category] = (incomeByCat[item.category] ?: 0.0) + item.amount
                incomeCountByCat[item.category] = (incomeCountByCat[item.category] ?: 0) + 1

                cal.timeInMillis = item.timestamp
                val day = cal.get(Calendar.DAY_OF_MONTH)
                dailyIncomeMap[day] = (dailyIncomeMap[day] ?: 0.0) + item.amount
            } else {
                expenseAccrualSum += item.amount
                expenseByCat[item.category] = (expenseByCat[item.category] ?: 0.0) + item.amount
                expenseCountByCat[item.category] = (expenseCountByCat[item.category] ?: 0) + 1

                cal.timeInMillis = item.timestamp
                val day = cal.get(Calendar.DAY_OF_MONTH)
                dailyExpenseMap[day] = (dailyExpenseMap[day] ?: 0.0) + item.amount

                val card = item.cardId?.let { cardMap[it] }
                if (card != null) {
                    val (dueYear, dueMonth) = CreditCardBillingHelper.calculatePaymentMonthAndYear(item.timestamp, card)
                    if (dueYear > period.year || (dueYear == period.year && dueMonth > period.month)) {
                        pendingCardExpensesSum += item.amount
                    }
                }
            }
        }

        // 3. Saídas de fato neste mês (Caixa / O que saiu de dinheiro neste mês)
        // Inclui:
        // - Despesas sem cartão realizadas neste mês
        // - Despesas de cartão (de qualquer mês) cuja fatura VENCE neste mês atual
        var cashOutflowThisMonth = 0.0
        for (tx in allTx) {
            if (tx.isDeleted || tx.type != TransactionType.EXPENSE.name) continue

            val card = tx.cardId?.let { cardMap[it] }
            if (card != null) {
                val (dueYear, dueMonth) = CreditCardBillingHelper.calculatePaymentMonthAndYear(tx.timestamp, card)
                if (dueYear == period.year && dueMonth == period.month) {
                    cashOutflowThisMonth += tx.amount
                }
            } else {
                if (tx.timestamp >= period.startEpoch && tx.timestamp <= period.endEpoch) {
                    cashOutflowThisMonth += tx.amount
                }
            }
        }

        // Saldo operacional do mês considerando caixa real (Entradas - Saídas de fato)
        val monthBalance = incomeSum - cashOutflowThisMonth
        val accumulatedBalance = previousBalance + monthBalance
        val accrualBalance = incomeSum - expenseAccrualSum

        val savingsRate = if (incomeSum > 0) {
            ((monthBalance / incomeSum).toFloat().coerceIn(-1f, 1f))
        } else {
            0f
        }

        val catExpenses = expenseByCat.map { (catName, sum) ->
            val catItem = Categories.getCategoryByName(catName, TransactionType.EXPENSE, customCats)
            val pct = if (expenseAccrualSum > 0) (sum / expenseAccrualSum).toFloat() else 0f
            CategorySpend(
                category = catItem,
                total = sum,
                percentage = pct,
                count = expenseCountByCat[catName] ?: 1
            )
        }.sortedByDescending { it.total }

        val catIncomes = incomeByCat.map { (catName, sum) ->
            val catItem = Categories.getCategoryByName(catName, TransactionType.INCOME, customCats)
            val pct = if (incomeSum > 0) (sum / incomeSum).toFloat() else 0f
            CategorySpend(
                category = catItem,
                total = sum,
                percentage = pct,
                count = incomeCountByCat[catName] ?: 1
            )
        }.sortedByDescending { it.total }

        val daysInMonth = period.daysInMonth
        val dailyList = (1..daysInMonth).map { day ->
            DailySpend(
                dayOfMonth = day,
                amount = dailyExpenseMap[day] ?: 0.0,
                incomeAmount = dailyIncomeMap[day] ?: 0.0
            )
        }

        MonthlyAnalytics(
            totalIncome = incomeSum,
            totalExpense = expenseAccrualSum,
            totalCashOutflow = cashOutflowThisMonth,
            previousBalance = previousBalance,
            balance = monthBalance,
            accumulatedBalance = accumulatedBalance,
            accrualBalance = accrualBalance,
            savingsRate = savingsRate,
            categoryExpenses = catExpenses,
            categoryIncomes = catIncomes,
            dailyExpenses = dailyList,
            pendingCardExpenses = pendingCardExpensesSum
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthlyAnalytics())

    // Advanced Multi-Month & Period Comparison Analytics (for the dedicated Charts screen)
    val advancedAnalytics: StateFlow<AdvancedAnalytics> = combine(
        allTransactions,
        _currentPeriod,
        customCategories
    ) { transactions, period, customCats ->
        val activeTx = transactions.filter { !it.isDeleted }

        // 1. Generate last 6 months trend points ending at the current period
        val trendPoints = mutableListOf<MonthlyTrendPoint>()
        val cal = Calendar.getInstance()
        val monthShortNames = arrayOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")

        for (i in 5 downTo 0) {
            cal.set(Calendar.YEAR, period.year)
            cal.set(Calendar.MONTH, period.month)
            cal.add(Calendar.MONTH, -i)
            val pYear = cal.get(Calendar.YEAR)
            val pMonth = cal.get(Calendar.MONTH)

            val pStart = Calendar.getInstance().apply {
                set(Calendar.YEAR, pYear)
                set(Calendar.MONTH, pMonth)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val pEnd = Calendar.getInstance().apply {
                set(Calendar.YEAR, pYear)
                set(Calendar.MONTH, pMonth)
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val inPeriodTx = activeTx.filter { it.timestamp in pStart..pEnd }
            val exp = inPeriodTx.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
            val inc = inPeriodTx.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }

            trendPoints.add(
                MonthlyTrendPoint(
                    year = pYear,
                    month = pMonth,
                    label = "${monthShortNames[pMonth]}/${(pYear % 100).toString().padStart(2, '0')}",
                    totalExpense = exp,
                    totalIncome = inc,
                    netBalance = inc - exp
                )
            )
        }

        // 2. Calculate Category Monthly Averages over the last 6 months
        val monthsAnalyzed = 6
        val categoryExpensesLastMonths = mutableMapOf<String, Double>()
        val categoryMonthsActive = mutableMapOf<String, MutableSet<String>>()
        val currentMonthCategoryExpenses = mutableMapOf<String, Double>()

        val sixMonthsAgoStart = Calendar.getInstance().apply {
            set(Calendar.YEAR, period.year)
            cal.set(Calendar.MONTH, period.month)
            add(Calendar.MONTH, -5)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        for (tx in activeTx) {
            if (tx.type != TransactionType.EXPENSE.name) continue
            if (tx.timestamp in sixMonthsAgoStart..period.endEpoch) {
                categoryExpensesLastMonths[tx.category] = (categoryExpensesLastMonths[tx.category] ?: 0.0) + tx.amount

                cal.timeInMillis = tx.timestamp
                val monthKey = "${cal.get(Calendar.YEAR)}_${cal.get(Calendar.MONTH)}"
                val set = categoryMonthsActive.getOrPut(tx.category) { mutableSetOf() }
                set.add(monthKey)
            }
            if (tx.timestamp in period.startEpoch..period.endEpoch) {
                currentMonthCategoryExpenses[tx.category] = (currentMonthCategoryExpenses[tx.category] ?: 0.0) + tx.amount
            }
        }

        val categoryAverages = categoryExpensesLastMonths.map { (catName, totalSpend) ->
            val catItem = Categories.getCategoryByName(catName, TransactionType.EXPENSE, customCats)
            val avg = totalSpend / monthsAnalyzed
            val cur = currentMonthCategoryExpenses[catName] ?: 0.0
            val diffPct = if (avg > 0) (((cur - avg) / avg) * 100).toFloat() else 0f
            CategoryMonthlyAverage(
                category = catItem,
                averageMonthlyAmount = avg,
                totalAmount = totalSpend,
                currentMonthAmount = cur,
                diffFromAveragePercent = diffPct,
                monthCountWithSpend = categoryMonthsActive[catName]?.size ?: 0
            )
        }.sortedByDescending { it.averageMonthlyAmount }

        // 3. Period Comparison: Current Month vs Previous Month
        val prevCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, period.year)
            set(Calendar.MONTH, period.month)
            add(Calendar.MONTH, -1)
        }
        val prevYear = prevCal.get(Calendar.YEAR)
        val prevMonth = prevCal.get(Calendar.MONTH)

        val prevStart = Calendar.getInstance().apply {
            set(Calendar.YEAR, prevYear)
            set(Calendar.MONTH, prevMonth)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val prevEnd = Calendar.getInstance().apply {
            set(Calendar.YEAR, prevYear)
            set(Calendar.MONTH, prevMonth)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val curTx = activeTx.filter { it.timestamp in period.startEpoch..period.endEpoch }
        val prevTx = activeTx.filter { it.timestamp in prevStart..prevEnd }

        val curExpense = curTx.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
        val prevExpense = prevTx.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
        val curIncome = curTx.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
        val prevIncome = prevTx.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }

        val expDiffPct = if (prevExpense > 0) (((curExpense - prevExpense) / prevExpense) * 100).toFloat() else 0f
        val incDiffPct = if (prevIncome > 0) (((curIncome - prevIncome) / prevIncome) * 100).toFloat() else 0f

        val allCategoriesSet = (curTx.map { it.category } + prevTx.map { it.category }).toSet()
        val catDiffs = allCategoriesSet.mapNotNull { catName ->
            val curAmt = curTx.filter { it.type == TransactionType.EXPENSE.name && it.category.equals(catName, ignoreCase = true) }.sumOf { it.amount }
            val prevAmt = prevTx.filter { it.type == TransactionType.EXPENSE.name && it.category.equals(catName, ignoreCase = true) }.sumOf { it.amount }

            if (curAmt > 0 || prevAmt > 0) {
                val catItem = Categories.getCategoryByName(catName, TransactionType.EXPENSE, customCats)
                val diffAmt = curAmt - prevAmt
                val pct = if (prevAmt > 0) (((curAmt - prevAmt) / prevAmt) * 100).toFloat() else 100f
                CategoryComparisonItem(
                    category = catItem,
                    currentAmount = curAmt,
                    previousAmount = prevAmt,
                    diffAmount = diffAmt,
                    diffPercent = pct
                )
            } else null
        }.sortedByDescending { kotlin.math.abs(it.diffAmount) }

        val comparison = PeriodComparisonData(
            currentPeriodLabel = "${monthShortNames[period.month]}/${period.year}",
            previousPeriodLabel = "${monthShortNames[prevMonth]}/${prevYear}",
            currentExpense = curExpense,
            previousExpense = prevExpense,
            currentIncome = curIncome,
            previousIncome = prevIncome,
            expenseDiffPercent = expDiffPct,
            incomeDiffPercent = incDiffPct,
            categoryDiffs = catDiffs
        )

        AdvancedAnalytics(
            trendMonths = trendPoints,
            categoryAverages = categoryAverages,
            comparison = comparison,
            monthsAnalyzedCount = monthsAnalyzed
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdvancedAnalytics())

    // Aggregated cards with their monthly invoice expenses
    val cardsWithExpenses: StateFlow<List<CardWithExpenses>> = combine(
        creditCards,
        allTransactions,
        _currentPeriod
    ) { cards, transactions, period ->
        cards.map { card ->
            // Transactions whose invoice is due in the current period (month/year)
            val cardTxList = transactions.filter {
                !it.isDeleted &&
                it.cardId == card.id &&
                it.type == TransactionType.EXPENSE.name &&
                CreditCardBillingHelper.isPaymentDueInPeriod(it.timestamp, card, period.year, period.month)
            }
            val totalExpense = cardTxList.sumOf { it.amount }
            val progress = if (card.limitAmount > 0) {
                (totalExpense / card.limitAmount).toFloat().coerceIn(0f, 1f)
            } else 0f
            val remaining = (card.limitAmount - totalExpense).coerceAtLeast(0.0)

            CardWithExpenses(
                card = card,
                totalExpenseThisMonth = totalExpense,
                monthlyTransactions = cardTxList,
                limitProgress = progress,
                remainingLimit = remaining
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Month Navigation
    fun previousMonth() {
        val current = _currentPeriod.value
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, current.year)
        cal.set(Calendar.MONTH, current.month)
        cal.add(Calendar.MONTH, -1)
        _currentPeriod.value = MonthPeriod(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
    }

    fun nextMonth() {
        val current = _currentPeriod.value
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, current.year)
        cal.set(Calendar.MONTH, current.month)
        cal.add(Calendar.MONTH, 1)
        _currentPeriod.value = MonthPeriod(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
    }

    fun goToCurrentMonth() {
        val cal = Calendar.getInstance()
        _currentPeriod.value = MonthPeriod(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
    }

    // Filters
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTypeFilter(type: TransactionType?) {
        _typeFilter.value = type
    }

    fun setCategoryFilter(category: String?) {
        _categoryFilter.value = category
    }

    // CRUD
    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        timestamp: Long,
        note: String = "",
        cardId: Long? = null,
        isInstallment: Boolean = false,
        totalInstallments: Int = 1,
        isRecurring: Boolean = false,
        recurringMonths: Int = 12,
        recurringIntervalMonths: Int = 1,
        isIndefinite: Boolean = true
    ) {
        viewModelScope.launch {
            if (isRecurring) {
                repository.createRecurring(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    startTimestamp = timestamp,
                    note = note,
                    cardId = cardId,
                    monthsCount = recurringMonths,
                    intervalMonths = recurringIntervalMonths,
                    isIndefinite = isIndefinite
                )
            } else if (isInstallment && totalInstallments > 1 && type == TransactionType.EXPENSE) {
                repository.createInstallments(
                    title = title,
                    totalAmount = amount,
                    category = category,
                    startTimestamp = timestamp,
                    note = note,
                    cardId = cardId,
                    totalInstallments = totalInstallments
                )
            } else {
                repository.insert(
                    TransactionEntity(
                        title = title.trim(),
                        amount = amount,
                        type = type.name,
                        category = category,
                        timestamp = timestamp,
                        note = note.trim(),
                        cardId = if (type == TransactionType.EXPENSE) cardId else null,
                        isRecurring = isRecurring
                    )
                )
            }
        }
    }

    fun deleteRecurringGroup(groupId: String) {
        viewModelScope.launch {
            repository.deleteRecurringGroup(groupId)
        }
    }

    fun anticipateInstallments(
        groupId: String,
        numberOfInstallments: Int,
        targetTimestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repository.anticipateInstallments(
                groupId = groupId,
                numberOfInstallments = numberOfInstallments,
                targetTimestamp = targetTimestamp
            )
        }
    }

    suspend fun getInstallmentGroupDetails(groupId: String): List<TransactionEntity> {
        return repository.getInstallmentsByGroupSync(groupId)
    }

    fun deleteInstallmentGroup(groupId: String) {
        viewModelScope.launch {
            repository.deleteInstallmentGroup(groupId)
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.update(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.delete(transaction)
        }
    }

    fun deleteTransactionById(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    // Credit Card Management
    fun addCreditCard(
        name: String,
        lastFourDigits: String = "",
        colorHex: String = "#8A05BE",
        limitAmount: Double = 0.0,
        closingDay: Int = 10,
        dueDay: Int = 17
    ) {
        viewModelScope.launch {
            cardRepository.insert(
                CreditCardEntity(
                    name = name.trim(),
                    lastFourDigits = lastFourDigits.trim(),
                    colorHex = colorHex,
                    limitAmount = limitAmount,
                    closingDay = closingDay,
                    dueDay = dueDay
                )
            )
        }
    }

    fun updateCreditCard(card: CreditCardEntity) {
        viewModelScope.launch {
            cardRepository.update(card)
        }
    }

    fun deleteCreditCard(card: CreditCardEntity) {
        viewModelScope.launch {
            cardRepository.delete(card)
        }
    }

    // Category Management (Entradas e Saídas)
    fun addCategory(
        name: String,
        type: TransactionType,
        iconName: String = "category",
        colorHex: String = "#42A5F5"
    ) {
        viewModelScope.launch {
            categoryRepository.addCategory(
                name = name,
                type = type,
                iconName = iconName,
                colorHex = colorHex
            )
        }
    }

    fun updateCategory(
        id: Long,
        name: String,
        type: TransactionType,
        iconName: String,
        colorHex: String,
        oldName: String? = null
    ) {
        viewModelScope.launch {
            categoryRepository.updateCategory(
                id = id,
                name = name,
                type = type,
                iconName = iconName,
                colorHex = colorHex
            )
            if (!oldName.isNullOrBlank() && oldName != name.trim()) {
                repository.updateCategoryName(oldName, name.trim(), type.name)
            }
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(id)
        }
    }

    // Settings & Preferences
    fun setThemeMode(mode: AppThemeMode) {
        userPreferences.setThemeMode(mode)
    }

    fun setThemeColor(color: AppThemeColor) {
        userPreferences.setThemeColor(color)
    }

    fun setMonthlyBudgetLimit(limit: Double) {
        userPreferences.setMonthlyBudgetLimit(limit)
    }

    fun setHideBalances(hide: Boolean) {
        userPreferences.setHideBalances(hide)
    }

    fun setDefaultCurrency(curr: String) {
        userPreferences.setDefaultCurrency(curr)
    }

    fun resetToSampleData() {
        viewModelScope.launch {
            userPreferences.setInitialDataSeeded(false)
            repository.deleteAll()
            cardRepository.deleteAll()
            repository.seedInitialDataIfEmpty()
            cardRepository.seedInitialCardsIfEmpty()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            // Keep seeded flag as true so empty state is preserved permanently across app restarts
            userPreferences.setInitialDataSeeded(true)
            repository.deleteAll()
            cardRepository.deleteAll()
            p2pSyncManager.broadcastClearAll()
        }
    }

    // P2P Peer-to-Peer Synchronization
    fun enableP2PSync(syncKey: String) {
        val cleanKey = syncKey.trim().uppercase()
        if (cleanKey.isBlank()) return
        userPreferences.setP2PSyncKey(cleanKey)
        userPreferences.setP2PSyncEnabled(true)
        p2pSyncManager.startSync(cleanKey)
    }

    fun disableP2PSync() {
        userPreferences.setP2PSyncEnabled(false)
        p2pSyncManager.stopSync(preserveState = false)
    }

    fun connectToPeerDirect(ip: String, port: Int) {
        p2pSyncManager.connectToPeerDirect(ip, port)
    }

    fun triggerP2PFullSync() {
        p2pSyncManager.triggerFullSyncNow()
    }

    fun generateRandomSyncKey(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val part1 = (1..3).map { chars.random() }.joinToString("")
        val part2 = (1..3).map { chars.random() }.joinToString("")
        return "FIN-$part1$part2"
    }

    fun refreshBackupList() {
        _backupFiles.value = backupManager.listBackups()
    }

    fun createManualBackup() {
        viewModelScope.launch {
            val result = backupManager.createBackup()
            if (result.isSuccess) {
                _backupStatusMessage.value = "Backup criado com sucesso!"
                refreshBackupList()
            } else {
                _backupStatusMessage.value = "Erro ao criar backup: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun saveBackupToUri(uri: android.net.Uri) {
        viewModelScope.launch {
            val result = backupManager.writeBackupToUri(uri)
            if (result.isSuccess) {
                _backupStatusMessage.value = "Backup exportado e salvo com sucesso no celular!"
            } else {
                _backupStatusMessage.value = "Erro ao exportar backup: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun restoreBackupFromUri(uri: android.net.Uri) {
        viewModelScope.launch {
            val result = backupManager.restoreFromUri(uri)
            if (result.isSuccess) {
                _backupStatusMessage.value = "Backup restaurado com sucesso a partir do arquivo!"
                refreshBackupList()
                if (p2pSyncManager.isSyncActive()) {
                    p2pSyncManager.triggerFullSyncNow()
                }
            } else {
                _backupStatusMessage.value = "Erro ao restaurar backup do arquivo: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun restoreBackup(file: java.io.File) {
        viewModelScope.launch {
            val result = backupManager.restoreBackup(file)
            if (result.isSuccess) {
                _backupStatusMessage.value = "Backup restaurado com sucesso!"
                refreshBackupList()
                if (p2pSyncManager.isSyncActive()) {
                    p2pSyncManager.triggerFullSyncNow()
                }
            } else {
                _backupStatusMessage.value = "Erro ao restaurar backup: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun deleteBackup(file: java.io.File) {
        viewModelScope.launch {
            backupManager.deleteBackup(file)
            refreshBackupList()
            _backupStatusMessage.value = "Backup excluído."
        }
    }

    fun setBackupInterval(interval: String) {
        userPreferences.setBackupInterval(interval)
        checkScheduledBackup()
    }

    fun clearBackupStatusMessage() {
        _backupStatusMessage.value = null
    }

    private fun checkScheduledBackup() {
        viewModelScope.launch {
            val interval = userPreferences.getBackupInterval()
            if (interval == "MANUAL") return@launch
            val lastTs = userPreferences.getLastBackupTimestamp()
            val now = System.currentTimeMillis()
            val diffMs = now - lastTs
            val intervalMs = when (interval) {
                "DAILY" -> 24 * 60 * 60 * 1000L
                "WEEKLY" -> 7 * 24 * 60 * 60 * 1000L
                else -> Long.MAX_VALUE
            }
            if (diffMs >= intervalMs) {
                backupManager.createBackup()
                refreshBackupList()
            }
        }
    }

    fun dismissTopUpdateBanner() {
        _showTopUpdateBanner.value = false
    }

    fun checkForUpdates(isAutoCheck: Boolean = false) {
        viewModelScope.launch {
            if (!isAutoCheck) {
                _isCheckingUpdates.value = true
                _updateCheckStatus.value = null
            }
            val currentVer = getInstalledVersionName()
            val result = updateManager.checkForUpdates(
                com.example.util.GitHubUpdateManager.DEFAULT_REPO,
                installedVersion = currentVer
            )
            if (isAutoCheck) {
                if (result is com.example.util.UpdateCheckResult.UpdateAvailable) {
                    _updateCheckStatus.value = result
                    _showTopUpdateBanner.value = true
                }
            } else {
                _updateCheckStatus.value = result
                if (result is com.example.util.UpdateCheckResult.UpdateAvailable) {
                    _showTopUpdateBanner.value = true
                }
                _isCheckingUpdates.value = false
            }
        }
    }

    fun downloadAndInstallApk(context: android.content.Context, downloadUrl: String) {
        viewModelScope.launch {
            _isDownloading.value = true
            _downloadProgress.value = 0
            val result = updateManager.downloadAndInstallApk(context, downloadUrl) { progress ->
                _downloadProgress.value = progress
            }
            _isDownloading.value = false
            _downloadProgress.value = null
            
            result.onSuccess { apkFile ->
                val installResult = updateManager.triggerInstall(context, apkFile)
                installResult.onFailure { error ->
                    _updateCheckStatus.value = com.example.util.UpdateCheckResult.Error(
                        error.localizedMessage ?: "Erro ao iniciar instalação do APK"
                    )
                }
            }.onFailure { error ->
                _updateCheckStatus.value = com.example.util.UpdateCheckResult.Error("Falha ao baixar o arquivo: ${error.localizedMessage}")
            }
        }
    }

    fun clearUpdateStatus() {
        _updateCheckStatus.value = null
    }
}
