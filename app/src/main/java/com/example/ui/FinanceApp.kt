package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.CreditCardEntity
import com.example.data.local.TransactionEntity
import com.example.data.model.CategoryItem
import com.example.data.model.TransactionType
import com.example.data.preferences.AppThemeColor
import com.example.ui.components.AnticipateInstallmentsDialog
import com.example.ui.components.BankImportSettingsSheet
import com.example.ui.components.CardDialog
import com.example.ui.components.CardsScreen
import com.example.ui.components.CategoryDialog
import com.example.ui.components.ChartsScreen
import com.example.ui.components.ImportedNotificationsBottomSheet
import com.example.ui.components.LiquidGlassBottomBar
import com.example.ui.components.MonthSelector
import com.example.ui.components.MonthlyExpenseCharts
import com.example.ui.components.NotificationsSheet
import com.example.ui.components.P2PStatusBadge
import com.example.ui.components.P2PSyncDialog
import com.example.ui.components.SettingsScreen
import com.example.ui.components.SummaryCards
import com.example.ui.components.TopUpdateBanner
import com.example.ui.components.TransactionDialog
import com.example.ui.components.TransactionItem
import com.example.ui.components.TransactionList
import com.example.ui.components.UpcomingScheduleCard
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceApp(
    viewModel: FinanceViewModel = viewModel(),
    onPromptBiometric: (onSuccess: () -> Unit) -> Unit = {},
    onCheckBiometricAvailability: () -> com.example.util.BiometricAvailability = { com.example.util.BiometricAvailability.AVAILABLE }
) {
    val coroutineScope = rememberCoroutineScope()
    val period by viewModel.currentPeriod.collectAsStateWithLifecycle()
    val isBiometricEnabled by viewModel.isBiometricAuthEnabled.collectAsStateWithLifecycle()
    val analytics by viewModel.monthlyAnalytics.collectAsStateWithLifecycle()
    val advancedAnalytics by viewModel.advancedAnalytics.collectAsStateWithLifecycle()
    val filteredTransactions by viewModel.filteredMonthlyTransactions.collectAsStateWithLifecycle()
    val monthlyTransactions by viewModel.monthlyTransactions.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val themeColor by viewModel.themeColor.collectAsStateWithLifecycle()
    val customThemeColorHex by viewModel.customThemeColorHex.collectAsStateWithLifecycle()
    val monthlyBudgetLimit by viewModel.monthlyBudgetLimit.collectAsStateWithLifecycle()
    val hideBalances by viewModel.hideBalances.collectAsStateWithLifecycle()
    val upcomingScheduleData by viewModel.upcomingScheduleData.collectAsStateWithLifecycle()
    val creditCards by viewModel.creditCards.collectAsStateWithLifecycle()
    val cardsWithExpenses by viewModel.cardsWithExpenses.collectAsStateWithLifecycle()
    val customCategories by viewModel.customCategories.collectAsStateWithLifecycle()
    val p2pSyncStatus by viewModel.p2pSyncStatus.collectAsStateWithLifecycle()
    val backupFiles by viewModel.backupFiles.collectAsStateWithLifecycle()
    val backupStatusMessage by viewModel.backupStatusMessage.collectAsStateWithLifecycle()
    val backupInterval by viewModel.backupInterval.collectAsStateWithLifecycle()
    val lastBackupTimestamp by viewModel.lastBackupTimestamp.collectAsStateWithLifecycle()
    val githubRepo by viewModel.githubRepo.collectAsStateWithLifecycle()
    val updateCheckStatus by viewModel.updateCheckStatus.collectAsStateWithLifecycle()
    val showTopUpdateBanner by viewModel.showTopUpdateBanner.collectAsStateWithLifecycle()
    val isCheckingUpdates by viewModel.isCheckingUpdates.collectAsStateWithLifecycle()
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()
    val isDownloading by viewModel.isDownloading.collectAsStateWithLifecycle()
    val installedVersionName by viewModel.installedVersionName.collectAsStateWithLifecycle()
    val notifications by viewModel.allNotifications.collectAsStateWithLifecycle()
    val unreadNotificationCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val notifyRecurring by viewModel.notifyRecurring.collectAsStateWithLifecycle()
    val notifyCardClosing by viewModel.notifyCardClosing.collectAsStateWithLifecycle()
    val notifyCardDue by viewModel.notifyCardDue.collectAsStateWithLifecycle()
    val notifyBudgetLimit by viewModel.notifyBudgetLimit.collectAsStateWithLifecycle()
    val notifyDailyReminder by viewModel.notifyDailyReminder.collectAsStateWithLifecycle()
    val notificationAdvanceDays by viewModel.notificationAdvanceDays.collectAsStateWithLifecycle()
    val notificationHour by viewModel.notificationHour.collectAsStateWithLifecycle()
    val notificationMinute by viewModel.notificationMinute.collectAsStateWithLifecycle()

    val pendingImportedNotifications by viewModel.pendingImportedNotifications.collectAsStateWithLifecycle()
    val allImportedNotifications by viewModel.allImportedNotifications.collectAsStateWithLifecycle()
    val pendingImportedCount by viewModel.pendingImportedCount.collectAsStateWithLifecycle()
    val isNotificationListenerGranted by viewModel.isNotificationListenerGranted.collectAsStateWithLifecycle()
    val isNotificationListenerConnected by viewModel.isNotificationListenerConnected.collectAsStateWithLifecycle()

    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Resumo, 1: Extrato, 2: Gráficos, 3: Cartões, 4: Ajustes
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showImportedNotificationsSheet by remember { mutableStateOf(false) }
    var showBankImportSettingsSheet by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var initialCardIdForDialog by remember { mutableStateOf<Long?>(null) }

    val notificationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.checkNotificationsNow()
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (!com.example.util.NotificationManagerHelper.hasNotificationPermission(context)) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    var transactionToAnticipate by remember { mutableStateOf<TransactionEntity?>(null) }
    var groupForAnticipate by remember { mutableStateOf<List<TransactionEntity>>(emptyList()) }

    var showP2PDialog by remember { mutableStateOf(false) }

    val openAnticipateModal: (TransactionEntity) -> Unit = { tx ->
        if (tx.isInstallment && !tx.installmentGroupId.isNullOrBlank()) {
            coroutineScope.launch {
                val group = viewModel.getInstallmentGroupDetails(tx.installmentGroupId)
                groupForAnticipate = group
                transactionToAnticipate = tx
            }
        }
    }

    var showCardDialog by remember { mutableStateOf(false) }
    var cardToEdit by remember { mutableStateOf<CreditCardEntity?>(null) }

    var showCategoryDialog by remember { mutableStateOf(false) }
    var categoryDialogType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var categoryToEdit by remember { mutableStateOf<CategoryItem?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    if (selectedTab == 4) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 2.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .clickable { selectedTab = 0 }
                                    .testTag("top_settings_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Voltar ao início",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Configurações",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedTab = 4
                                    }
                                    .testTag("top_settings_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Wallet,
                                        contentDescription = "Acessar Configurações",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "BUMoney",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.2.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.clickable {
                                    selectedTab = 4
                                }
                            )
                        }
                    }
                },
                actions = {
                    if (selectedTab != 4) {
                        IconButton(
                            onClick = {
                                viewModel.refreshNotificationListenerStatus()
                                showImportedNotificationsSheet = true
                            },
                            modifier = Modifier.testTag("bank_import_bell_icon")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (pendingImportedCount > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ) {
                                            Text(
                                                text = if (pendingImportedCount > 99) "99+" else pendingImportedCount.toString(),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = "Importador de Notificações Bancárias",
                                    tint = if (pendingImportedCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = { showNotificationsSheet = true },
                            modifier = Modifier.testTag("notifications_bell_icon")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotificationCount > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError
                                        ) {
                                            Text(
                                                text = if (unreadNotificationCount > 99) "99+" else unreadNotificationCount.toString(),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (unreadNotificationCount > 0) Icons.Filled.NotificationsActive else Icons.Filled.Notifications,
                                    contentDescription = "Central de Notificações",
                                    tint = if (unreadNotificationCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(
                            onClick = { viewModel.setHideBalances(!hideBalances) },
                            modifier = Modifier.testTag("toggle_privacy_icon")
                        ) {
                            Icon(
                                imageVector = if (hideBalances) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (hideBalances) "Mostrar saldos" else "Ocultar saldos",
                                tint = if (hideBalances) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        P2PStatusBadge(
                            status = p2pSyncStatus,
                            onClick = { showP2PDialog = true },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Update Notification Banner / Popup
                TopUpdateBanner(
                    updateStatus = updateCheckStatus,
                    isVisible = showTopUpdateBanner,
                    isDownloading = isDownloading,
                    downloadProgress = downloadProgress,
                    onUpdateClick = { downloadUrl ->
                        viewModel.downloadAndInstallApk(context, downloadUrl)
                    },
                    onDismiss = {
                        viewModel.dismissTopUpdateBanner()
                    }
                )

                // Month Selector Header - show on Dashboard, Extrato, Gráficos & Cartões
                if (selectedTab != 4) {
                    MonthSelector(
                        period = period,
                        onPreviousMonth = { viewModel.previousMonth() },
                        onNextMonth = { viewModel.nextMonth() },
                        onGoToCurrentMonth = { viewModel.goToCurrentMonth() },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            initialOffsetX = { width -> (width * 0.12f).toInt() }
                        ) + fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)))
                            .togetherWith(
                                slideOutHorizontally(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    targetOffsetX = { width -> (-width * 0.12f).toInt() }
                                ) + fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
                            )
                    } else {
                        (slideInHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            initialOffsetX = { width -> (-width * 0.12f).toInt() }
                        ) + fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)))
                            .togetherWith(
                                slideOutHorizontally(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    targetOffsetX = { width -> (width * 0.12f).toInt() }
                                ) + fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
                            )
                    }
                },
                label = "screen_transition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> {
                        // Dashboard View: Summary Cards, Expense Charts, Recent Activity
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("dashboard_lazy_column"),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Summary Balance, Income and Expense
                            item {
                                SummaryCards(
                                    analytics = analytics,
                                    hideBalances = hideBalances,
                                    monthlyBudgetLimit = monthlyBudgetLimit
                                )
                            }

                            // Visão resumida dos próximos gastos/entradas programadas
                            item {
                                UpcomingScheduleCard(
                                    data = upcomingScheduleData,
                                    hideBalances = hideBalances,
                                    availableCards = creditCards,
                                    customCategories = customCategories,
                                    onRangeSelected = { viewModel.setUpcomingDaysRange(it) },
                                    onEditTransaction = { item ->
                                        transactionToEdit = item
                                        initialCardIdForDialog = item.cardId
                                        showAddDialog = true
                                    }
                                )
                            }

                            // Recent Transactions in this month preview
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Últimas Transações",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    if (monthlyTransactions.isNotEmpty()) {
                                        Surface(
                                            onClick = { selectedTab = 1 },
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ) {
                                            Text(
                                                text = "Ver todas (${monthlyTransactions.size})",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            val recentList = monthlyTransactions.take(5)
                            if (recentList.isEmpty()) {
                                item {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    ) {
                                        Text(
                                            text = "Nenhum lançamento registrado neste mês. Use o botão abaixo para adicionar.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                            } else {
                                items(
                                    items = recentList,
                                    key = { "recent_${it.id}" }
                                ) { item ->
                                    val matchedCard = creditCards.find { it.id == item.cardId }
                                    TransactionItem(
                                        transaction = item,
                                        onEdit = {
                                            transactionToEdit = item
                                            initialCardIdForDialog = item.cardId
                                            showAddDialog = true
                                        },
                                        onDelete = { viewModel.deleteTransaction(item) },
                                        onAnticipate = openAnticipateModal,
                                        hideBalances = hideBalances,
                                        cardName = matchedCard?.name,
                                        card = matchedCard,
                                        customCategories = customCategories
                                    )
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(140.dp)) // Extra space for floating pill & FAB
                            }
                        }
                    }

                    1 -> {
                        // Detailed Transactions View with Filters & Search
                        TransactionList(
                            transactions = filteredTransactions,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            selectedType = typeFilter,
                            onSelectType = { viewModel.setTypeFilter(it) },
                            selectedCategory = categoryFilter,
                            onSelectCategory = { viewModel.setCategoryFilter(it) },
                            onEditTransaction = {
                                transactionToEdit = it
                                initialCardIdForDialog = it.cardId
                                showAddDialog = true
                            },
                            onDeleteTransaction = { viewModel.deleteTransaction(it) },
                            onAnticipateTransaction = openAnticipateModal,
                            onDeleteInstallmentGroup = { viewModel.deleteInstallmentGroup(it) },
                            hideBalances = hideBalances,
                            availableCards = creditCards,
                            customCategories = customCategories
                        )
                    }

                    2 -> {
                        // Dedicated Charts Screen: Categories, Period Comparison, Category Monthly Averages, 6M Trends, Daily Evolution
                        ChartsScreen(
                            monthlyAnalytics = analytics,
                            advancedAnalytics = advancedAnalytics,
                            hideBalances = hideBalances
                        )
                    }

                    3 -> {
                        // Credit Cards Screen: Card Management & Invoices
                        CardsScreen(
                            cardsWithExpenses = cardsWithExpenses,
                            hideBalances = hideBalances,
                            currentPeriod = period,
                            onAddNewCard = {
                                cardToEdit = null
                                showCardDialog = true
                            },
                            onEditCard = {
                                cardToEdit = it
                                showCardDialog = true
                            },
                            onDeleteCard = { viewModel.deleteCreditCard(it) },
                            onAddExpenseForCard = { card ->
                                transactionToEdit = null
                                initialCardIdForDialog = card.id
                                showAddDialog = true
                            },
                            onEditTransaction = {
                                transactionToEdit = it
                                initialCardIdForDialog = it.cardId
                                showAddDialog = true
                            },
                            onDeleteTransaction = { viewModel.deleteTransaction(it) },
                            onToggleInvoicePaid = { cardId, year, month ->
                                viewModel.toggleInvoicePaid(cardId, year, month)
                            }
                        )
                    }

                    4 -> {
                        // Settings & Preferences Panel
                        SettingsScreen(
                            themeMode = themeMode,
                            onThemeModeChange = { viewModel.setThemeMode(it) },
                            themeColor = themeColor,
                            onThemeColorChange = { viewModel.setThemeColor(it) },
                            customColorHex = customThemeColorHex,
                            onCustomColorChange = { viewModel.setCustomThemeColor(it) },
                            monthlyBudgetLimit = monthlyBudgetLimit,
                            onBudgetLimitChange = { viewModel.setMonthlyBudgetLimit(it) },
                            hideBalances = hideBalances,
                            onHideBalancesChange = { viewModel.setHideBalances(it) },
                            isBiometricAuthEnabled = isBiometricEnabled,
                            onToggleBiometricAuth = { targetState ->
                                onPromptBiometric {
                                    viewModel.setBiometricAuthEnabled(targetState)
                                }
                            },
                            onLockAppNow = { viewModel.lockApp() },
                            biometricAvailabilityMessage = onCheckBiometricAvailability().message,
                            customCategories = customCategories,
                            onAddNewCategory = { type ->
                                categoryDialogType = type
                                categoryToEdit = null
                                showCategoryDialog = true
                            },
                            onEditCategory = { cat ->
                                categoryToEdit = cat
                                categoryDialogType = cat.type
                                showCategoryDialog = true
                            },
                            onDeleteCategory = { id ->
                                viewModel.deleteCategory(id)
                            },
                            p2pSyncStatus = p2pSyncStatus,
                            onOpenP2PSync = { showP2PDialog = true },
                            backupFiles = backupFiles,
                            backupInterval = backupInterval,
                            lastBackupTimestamp = lastBackupTimestamp,
                            backupStatusMessage = backupStatusMessage,
                            onCreateBackup = { viewModel.createManualBackup() },
                            onRestoreBackup = { viewModel.restoreBackup(it) },
                            onDeleteBackup = { viewModel.deleteBackup(it) },
                            onSaveBackupToUri = { uri -> viewModel.saveBackupToUri(uri) },
                            onRestoreBackupFromUri = { uri -> viewModel.restoreBackupFromUri(uri) },
                            onBackupIntervalChange = { viewModel.setBackupInterval(it) },
                            onClearBackupStatusMessage = { viewModel.clearBackupStatusMessage() },
                            onResetToSampleData = { viewModel.resetToSampleData() },
                            onClearAllData = { viewModel.clearAllData() },
                            installedVersionName = installedVersionName,
                            githubRepo = githubRepo,
                            updateCheckStatus = updateCheckStatus,
                            isCheckingUpdates = isCheckingUpdates,
                            downloadProgress = downloadProgress,
                            isDownloading = isDownloading,
                            onCheckForUpdates = { viewModel.checkForUpdates() },
                            onDownloadAndInstallApk = { url ->
                                viewModel.downloadAndInstallApk(context, url)
                            },
                            onClearUpdateStatus = { viewModel.clearUpdateStatus() },
                            // Notifications
                            notificationsEnabled = notificationsEnabled,
                            onNotificationsEnabledChange = { viewModel.setNotificationsEnabled(it) },
                            notifyRecurring = notifyRecurring,
                            onNotifyRecurringChange = { viewModel.setNotifyRecurring(it) },
                            notifyCardClosing = notifyCardClosing,
                            onNotifyCardClosingChange = { viewModel.setNotifyCardClosing(it) },
                            notifyCardDue = notifyCardDue,
                            onNotifyCardDueChange = { viewModel.setNotifyCardDue(it) },
                            notifyBudgetLimit = notifyBudgetLimit,
                            onNotifyBudgetLimitChange = { viewModel.setNotifyBudgetLimit(it) },
                            notifyDailyReminder = notifyDailyReminder,
                            onNotifyDailyReminderChange = { viewModel.setNotifyDailyReminder(it) },
                            notificationAdvanceDays = notificationAdvanceDays,
                            onNotificationAdvanceDaysChange = { viewModel.setNotificationAdvanceDays(it) },
                            notificationHour = notificationHour,
                            notificationMinute = notificationMinute,
                            onNotificationTimeChange = { h, m -> viewModel.setNotificationTime(h, m) },
                            onSendTestNotification = { viewModel.sendTestNotification() },
                            onOpenBankImportSettings = { showBankImportSettingsSheet = true },
                            onNavigateBack = { selectedTab = 0 }
                        )
                    }
                }
            }
        }

        // Solid Dock Navigation Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            if (selectedTab == 0 || selectedTab == 1 || selectedTab == 2) {
                FloatingActionButton(
                    onClick = {
                        transactionToEdit = null
                        initialCardIdForDialog = null
                        showAddDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(end = 20.dp, bottom = 8.dp)
                        .testTag("add_transaction_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Novo Lançamento",
                        tint = Color.White
                    )
                }
            } else if (selectedTab == 3) {
                FloatingActionButton(
                    onClick = {
                        cardToEdit = null
                        showCardDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(end = 20.dp, bottom = 8.dp)
                        .testTag("add_card_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Novo Cartão",
                        tint = Color.White
                    )
                }
            }

            LiquidGlassBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                transactionCount = monthlyTransactions.size,
                cardCount = creditCards.size,
                themeMode = themeMode,
                modifier = Modifier.testTag("bottom_navigation_bar")
            )
        }
    }
}

    // Modal BottomSheet for Adding or Editing Transaction
    if (showAddDialog) {
        TransactionDialog(
            transactionToEdit = transactionToEdit,
            availableCards = creditCards,
            customCategories = customCategories,
            initialCardId = initialCardIdForDialog,
            onDismiss = {
                showAddDialog = false
                transactionToEdit = null
                initialCardIdForDialog = null
            },
            onAddNewCategory = { type ->
                categoryDialogType = type
                categoryToEdit = null
                showCategoryDialog = true
            },
            onEditCategory = { cat ->
                categoryToEdit = cat
                categoryDialogType = cat.type
                showCategoryDialog = true
            },
            onSave = { title, amount, type, category, timestamp, note, cardId, isInstallment, totalInstallments, isRecurring, recurringMonths, recurringIntervalMonths, isIndefinite ->
                val editing = transactionToEdit
                if (editing != null) {
                    viewModel.updateTransactionDetailed(
                        existing = editing,
                        title = title,
                        amount = amount,
                        type = type,
                        category = category,
                        timestamp = timestamp,
                        note = note,
                        cardId = cardId,
                        isInstallment = isInstallment,
                        totalInstallments = totalInstallments,
                        isRecurring = isRecurring,
                        recurringMonths = recurringMonths,
                        recurringIntervalMonths = recurringIntervalMonths,
                        isIndefinite = isIndefinite
                    )
                } else {
                    viewModel.addTransaction(
                        title = title,
                        amount = amount,
                        type = type,
                        category = category,
                        timestamp = timestamp,
                        note = note,
                        cardId = cardId,
                        isInstallment = isInstallment,
                        totalInstallments = totalInstallments,
                        isRecurring = isRecurring,
                        recurringMonths = recurringMonths,
                        recurringIntervalMonths = recurringIntervalMonths,
                        isIndefinite = isIndefinite
                    )
                }
                showAddDialog = false
                transactionToEdit = null
                initialCardIdForDialog = null
            }
        )
    }

    // Modal BottomSheet for Anticipating Installments
    transactionToAnticipate?.let { tx ->
        AnticipateInstallmentsDialog(
            transaction = tx,
            groupInstallments = groupForAnticipate,
            currentMonthTimestamp = period.startEpoch,
            onDismiss = {
                transactionToAnticipate = null
                groupForAnticipate = emptyList()
            },
            onConfirmAnticipate = { groupId, count, targetTs ->
                viewModel.anticipateInstallments(
                    groupId = groupId,
                    numberOfInstallments = count,
                    targetTimestamp = targetTs
                )
                transactionToAnticipate = null
                groupForAnticipate = emptyList()
            }
        )
    }

    // Dialog for Adding or Editing Credit Card
    if (showCardDialog) {
        CardDialog(
            cardToEdit = cardToEdit,
            onDismiss = {
                showCardDialog = false
                cardToEdit = null
            },
            onSave = { name, lastFourDigits, colorHex, limit, closingDay, dueDay ->
                val editing = cardToEdit
                if (editing != null) {
                    viewModel.updateCreditCard(
                        editing.copy(
                            name = name,
                            lastFourDigits = lastFourDigits,
                            colorHex = colorHex,
                            limitAmount = limit,
                            closingDay = closingDay,
                            dueDay = dueDay
                        )
                    )
                } else {
                    viewModel.addCreditCard(
                        name = name,
                        lastFourDigits = lastFourDigits,
                        colorHex = colorHex,
                        limitAmount = limit,
                        closingDay = closingDay,
                        dueDay = dueDay
                    )
                }
                showCardDialog = false
                cardToEdit = null
            }
        )
    }

    // Dialog for Adding or Editing Custom Category
    if (showCategoryDialog) {
        CategoryDialog(
            initialType = categoryDialogType,
            categoryToEdit = categoryToEdit,
            onDismiss = {
                showCategoryDialog = false
                categoryToEdit = null
            },
            onSave = { name, type, iconName, colorHex ->
                val editing = categoryToEdit
                if (editing != null && editing.dbId > 0) {
                    viewModel.updateCategory(
                        id = editing.dbId,
                        name = name,
                        type = type,
                        iconName = iconName,
                        colorHex = colorHex,
                        oldName = editing.name
                    )
                } else {
                    viewModel.addCategory(
                        name = name,
                        type = type,
                        iconName = iconName,
                        colorHex = colorHex
                    )
                }
                showCategoryDialog = false
                categoryToEdit = null
            }
        )
    }

    // Modal Dialog for P2P Peer-to-Peer Synchronization
    if (showP2PDialog) {
        P2PSyncDialog(
            status = p2pSyncStatus,
            onEnableSync = { key, role ->
                viewModel.enableP2PSync(key, role)
            },
            onDisableSync = {
                viewModel.disableP2PSync()
            },
            onTriggerFullSync = {
                viewModel.triggerP2PFullSync()
            },
            onConnectDirect = { ip, port ->
                viewModel.connectToPeerDirect(ip, port)
            },
            onGenerateKey = {
                viewModel.generateRandomSyncKey()
            },
            onDismiss = {
                showP2PDialog = false
            }
        )
    }

    // Modal Sheet for Notifications Center
    NotificationsSheet(
        isOpen = showNotificationsSheet,
        onDismiss = { showNotificationsSheet = false },
        notifications = notifications,
        unreadCount = unreadNotificationCount,
        onMarkAsRead = { id -> viewModel.markNotificationAsRead(id) },
        onMarkAllAsRead = { viewModel.markAllNotificationsAsRead() },
        onDeleteNotification = { id -> viewModel.deleteNotification(id) },
        onClearAll = { viewModel.clearAllNotifications() },
        onNavigateToSection = { route ->
            when (route) {
                "cards" -> selectedTab = 3
                "transactions" -> selectedTab = 1
                "dashboard" -> selectedTab = 0
                "settings" -> selectedTab = 4
                else -> {}
            }
        },
        onOpenSettings = {
            selectedTab = 4
        },
        onSendTestNotification = {
            viewModel.sendTestNotification()
        }
    )

    // Modal Sheet for Pending Bank Notification Imports
    if (showImportedNotificationsSheet) {
        ImportedNotificationsBottomSheet(
            pendingNotifications = pendingImportedNotifications,
            allNotifications = allImportedNotifications,
            creditCards = creditCards,
            isPermissionGranted = isNotificationListenerGranted,
            isListenerConnected = isNotificationListenerConnected,
            onRequestPermission = {
                viewModel.openNotificationListenerSettings()
            },
            onScanActiveNotifications = {
                viewModel.scanActiveBankNotifications()
            },
            onDismissRequest = { showImportedNotificationsSheet = false },
            onConfirmImport = { id, merchant, amount, category, cardId ->
                viewModel.confirmAndImportNotification(id, merchant, amount, category, cardId)
            },
            onDiscard = { id ->
                viewModel.discardImportedNotification(id)
            },
            onImportAll = {
                viewModel.importAllPendingNotifications()
            },
            onClearHistory = {
                viewModel.clearImportedNotificationHistory()
            },
            onSimulateNotification = {
                viewModel.simulateBankNotification(
                    "com.nu.production",
                    "Nubank",
                    "Compra de R$ 89,90 aprovada no iFood com o cartão final 1234."
                )
            },
            onSimulatePreset = { pkg, title, text ->
                viewModel.simulateBankNotification(pkg, title, text)
            },
            onOpenSettings = {
                showImportedNotificationsSheet = false
                showBankImportSettingsSheet = true
            }
        )
    }

    // Modal Sheet for Bank Import Settings Configuration
    if (showBankImportSettingsSheet) {
        BankImportSettingsSheet(
            userPreferences = com.example.data.preferences.UserPreferences.getInstance(LocalContext.current),
            onDismissRequest = { showBankImportSettingsSheet = false },
            onSimulateNotification = { pkg, title, text ->
                viewModel.simulateBankNotification(pkg, title, text)
            }
        )
    }
}

