package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CreditCardOff
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import com.example.data.model.CategoryItem
import com.example.data.model.TransactionType
import com.example.data.p2p.P2PConnectionState
import com.example.data.p2p.P2PSyncStatus
import com.example.data.preferences.AppThemeColor
import com.example.data.preferences.AppThemeMode
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.Formatters
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit,
    themeColor: AppThemeColor,
    onThemeColorChange: (AppThemeColor) -> Unit,
    customColorHex: Long = 0xFFB76E79L,
    onCustomColorChange: (Long) -> Unit = {},
    monthlyBudgetLimit: Double,
    onBudgetLimitChange: (Double) -> Unit,
    hideBalances: Boolean,
    onHideBalancesChange: (Boolean) -> Unit,
    customCategories: List<CategoryItem> = emptyList(),
    onAddNewCategory: (TransactionType) -> Unit = {},
    onEditCategory: (CategoryItem) -> Unit = {},
    onDeleteCategory: (Long) -> Unit = {},
    p2pSyncStatus: P2PSyncStatus? = null,
    onOpenP2PSync: () -> Unit = {},
    backupFiles: List<File> = emptyList(),
    backupInterval: String = "MANUAL",
    lastBackupTimestamp: Long = 0L,
    backupStatusMessage: String? = null,
    onCreateBackup: () -> Unit = {},
    onRestoreBackup: (File) -> Unit = {},
    onDeleteBackup: (File) -> Unit = {},
    onSaveBackupToUri: ((Uri) -> Unit)? = null,
    onRestoreBackupFromUri: ((Uri) -> Unit)? = null,
    onBackupIntervalChange: (String) -> Unit = {},
    onClearBackupStatusMessage: () -> Unit = {},
    onResetToSampleData: () -> Unit,
    onClearAllData: () -> Unit,
    installedVersionName: String = "0.1.2",
    githubRepo: String = com.example.util.GitHubUpdateManager.DEFAULT_REPO,
    onGithubRepoChange: (String) -> Unit = {},
    updateCheckStatus: com.example.util.UpdateCheckResult? = null,
    isCheckingUpdates: Boolean = false,
    downloadProgress: Int? = null,
    isDownloading: Boolean = false,
    onCheckForUpdates: () -> Unit = {},
    onDownloadAndInstallApk: (String) -> Unit = {},
    onClearUpdateStatus: () -> Unit = {},
    // Notification Configuration
    notificationsEnabled: Boolean = true,
    onNotificationsEnabledChange: (Boolean) -> Unit = {},
    notifyRecurring: Boolean = true,
    onNotifyRecurringChange: (Boolean) -> Unit = {},
    notifyCardClosing: Boolean = true,
    onNotifyCardClosingChange: (Boolean) -> Unit = {},
    notifyCardDue: Boolean = true,
    onNotifyCardDueChange: (Boolean) -> Unit = {},
    notifyBudgetLimit: Boolean = true,
    onNotifyBudgetLimitChange: (Boolean) -> Unit = {},
    notifyDailyReminder: Boolean = false,
    onNotifyDailyReminderChange: (Boolean) -> Unit = {},
    notificationAdvanceDays: Int = 1,
    onNotificationAdvanceDaysChange: (Int) -> Unit = {},
    notificationHour: Int = 9,
    notificationMinute: Int = 0,
    onNotificationTimeChange: (Int, Int) -> Unit = { _, _ -> },
    onSendTestNotification: () -> Unit = {},
    onOpenBankImportSettings: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    // Dialog & Sheet States
    var showResetDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showThemeSheet by remember { mutableStateOf(false) }
    var showColorSheet by remember { mutableStateOf(false) }
    var showCustomColorPicker by remember { mutableStateOf(false) }
    var showIntervalDialog by remember { mutableStateOf(false) }
    var showCategoriesSheet by remember { mutableStateOf(false) }
    var showBackupsSheet by remember { mutableStateOf(false) }
    var showUpdatesSheet by remember { mutableStateOf(false) }
    var showNotificationSettingsSheet by remember { mutableStateOf(false) }
    var showAdvanceDaysDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    var budgetInput by remember(monthlyBudgetLimit) {
        mutableStateOf(if (monthlyBudgetLimit > 0) String.format(Locale.US, "%.2f", monthlyBudgetLimit) else "")
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null && onSaveBackupToUri != null) {
            onSaveBackupToUri(uri)
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null && onRestoreBackupFromUri != null) {
            onRestoreBackupFromUri(uri)
        }
    }

    val displayVersion = installedVersionName.ifBlank {
        com.example.BuildConfig.VERSION_NAME
    }

    val p2pStatusText = if (p2pSyncStatus?.isEnabled == true) {
        if (p2pSyncStatus.state == P2PConnectionState.CONNECTED) {
            "🟢 Conectado (${p2pSyncStatus.connectedPeers.size})"
        } else {
            "🟡 Ativo"
        }
    } else {
        "Configurar"
    }

    val themeModeLabel = when (themeMode) {
        AppThemeMode.SYSTEM -> "Automático"
        AppThemeMode.LIGHT -> "Claro Puro"
        AppThemeMode.LIGHT_WARM -> "Claro Suave"
        AppThemeMode.DARK -> "Escuro Noturno"
        AppThemeMode.DARK_OLED -> "Escuro OLED"
    }

    val backupIntervalLabel = when (backupInterval) {
        "DAILY" -> "Diário"
        "WEEKLY" -> "Semanal"
        else -> "Manual"
    }

    val budgetDisplay = if (monthlyBudgetLimit > 0) {
        Formatters.formatCurrency(monthlyBudgetLimit)
    } else {
        "Não definido"
    }

    val lastBackupStr = if (lastBackupTimestamp > 0) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.format(Date(lastBackupTimestamp))
    } else {
        "Nunca"
    }

    val query = searchQuery.trim().lowercase()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("settings_screen_container"),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Search Bar matching the reference UI
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = "Buscar nas configurações",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpar busca",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_search_field")
        )

        // Banner for Backup status if message exists
        if (backupStatusMessage != null) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = backupStatusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onClearBackupStatusMessage, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // ==========================================
        // 1. SINCRONIZAÇÃO
        // ==========================================
        val showSyncGroup = query.isEmpty() ||
                "sincronização".contains(query) ||
                "p2p".contains(query) ||
                "par".contains(query) ||
                "chave".contains(query) ||
                "backup automático".contains(query) ||
                "frequência".contains(query)

        if (showSyncGroup) {
            SettingsGroup(title = "SINCRONIZAÇÃO") {
                SettingsGroupRow(
                    icon = Icons.Default.Sync,
                    title = "Conexões P2P com Chave",
                    subtitle = if (p2pSyncStatus?.isEnabled == true) "Sincronização ativa entre aparelhos" else "Sincronize gastos sem servidores",
                    valueText = p2pStatusText,
                    onClick = onOpenP2PSync,
                    testTag = "settings_open_p2p_button"
                )

                SettingsRowDivider()

                SettingsGroupRow(
                    icon = Icons.Default.Schedule,
                    title = "Backup Automático",
                    subtitle = "Frequência programada de cópias de segurança",
                    valueText = backupIntervalLabel,
                    onClick = { showIntervalDialog = true },
                    testTag = "settings_backup_interval_row"
                )
            }
        }

        // ==========================================
        // 2. APARÊNCIA
        // ==========================================
        val showAppearanceGroup = query.isEmpty() ||
                "aparência".contains(query) ||
                "tema".contains(query) ||
                "escuro".contains(query) ||
                "oled".contains(query) ||
                "claro".contains(query) ||
                "cor".contains(query) ||
                "estilo".contains(query)

        if (showAppearanceGroup) {
            SettingsGroup(title = "APARÊNCIA") {
                SettingsGroupRow(
                    icon = Icons.Default.Brightness4,
                    title = "Modo de Tema",
                    subtitle = "Tema visual e preferência de contraste",
                    valueText = themeModeLabel,
                    onClick = { showThemeSheet = true },
                    testTag = "settings_theme_mode_row"
                )

                SettingsRowDivider()

                SettingsGroupRow(
                    icon = Icons.Default.Palette,
                    title = "Cor de Destaque",
                    subtitle = "Cores clássicas e paleta personalizada",
                    valueText = if (themeColor == AppThemeColor.CUSTOM) "Personalizada (${String.format("#%06X", 0xFFFFFF and customColorHex.toInt())})" else themeColor.displayName,
                    leadingBadge = {
                        if (themeColor == AppThemeColor.CUSTOM) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(customColorHex or 0xFF000000L))
                                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), CircleShape)
                            )
                        } else {
                            val colorHex = if (isSystemInDarkTheme()) themeColor.primaryDarkHex else themeColor.primaryLightHex
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorHex))
                            )
                        }
                    },
                    onClick = { showColorSheet = true },
                    testTag = "settings_theme_color_row"
                )
            }
        }

        // ==========================================
        // 3. GERAL
        // ==========================================
        val showGeneralGroup = query.isEmpty() ||
                "geral".contains(query) ||
                "metas".contains(query) ||
                "orçamento".contains(query) ||
                "teto".contains(query) ||
                "saldo".contains(query) ||
                "privacidade".contains(query) ||
                "categoria".contains(query) ||
                "categorias".contains(query)

        if (showGeneralGroup) {
            SettingsGroup(title = "GERAL") {
                SettingsGroupRow(
                    icon = Icons.Default.TrackChanges,
                    title = "Metas e Teto Mensal",
                    subtitle = "Limite de gastos programado para o mês",
                    valueText = budgetDisplay,
                    onClick = { showBudgetDialog = true },
                    testTag = "set_budget_limit_button"
                )

                SettingsRowDivider()

                SettingsGroupRow(
                    icon = if (hideBalances) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    title = "Ocultar Valores e Saldos",
                    subtitle = "Proteção visual em locais públicos",
                    trailing = {
                        Switch(
                            checked = hideBalances,
                            onCheckedChange = onHideBalancesChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("settings_hide_balances_switch")
                        )
                    },
                    onClick = { onHideBalancesChange(!hideBalances) }
                )

                SettingsRowDivider()

                SettingsGroupRow(
                    icon = Icons.Default.Category,
                    title = "Categorias Personalizadas",
                    subtitle = "Gerencie tags de despesas e receitas",
                    valueText = if (customCategories.isEmpty()) "Padrão" else "${customCategories.size} criadas",
                    onClick = { showCategoriesSheet = true },
                    testTag = "settings_categories_row"
                )
            }
        }

        // ==========================================
        // 3.5 SISTEMA DE NOTIFICAÇÕES & ALERTAS
        // ==========================================
        val showNotificationGroup = query.isEmpty() ||
                "notificação".contains(query) ||
                "notificações".contains(query) ||
                "alerta".contains(query) ||
                "alertas".contains(query) ||
                "lembrete".contains(query) ||
                "aviso".contains(query) ||
                "vencimento".contains(query) ||
                "fatura".contains(query) ||
                "cartão".contains(query) ||
                "recorrência".contains(query)

        if (showNotificationGroup) {
            SettingsGroup(title = "NOTIFICAÇÕES & ALERTAS") {
                val statusText = if (notificationsEnabled) {
                    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", notificationHour, notificationMinute)
                    "Ativadas • $timeFormatted"
                } else {
                    "Desativadas"
                }

                SettingsGroupRow(
                    icon = if (notificationsEnabled) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                    title = "Notificações & Lembretes",
                    subtitle = if (notificationsEnabled) "Vencimentos, faturas, tetos e horário configurados" else "Alertas automáticos desativados",
                    valueText = statusText,
                    onClick = { showNotificationSettingsSheet = true },
                    testTag = "settings_notifications_row"
                )

                SettingsRowDivider()

                SettingsGroupRow(
                    icon = Icons.Default.Receipt,
                    title = "Importação por Notificação Bancária",
                    subtitle = "Reconhecimento automático de compras e Pix nos bancos",
                    valueText = "Configurar",
                    onClick = onOpenBankImportSettings,
                    testTag = "settings_bank_import_row"
                )
            }
        }

        // ==========================================
        // 4. GERENCIAMENTO DE DADOS
        // ==========================================
        val showDataGroup = query.isEmpty() ||
                "gerenciamento de dados".contains(query) ||
                "dados".contains(query) ||
                "backup".contains(query) ||
                "exportar".contains(query) ||
                "importar".contains(query) ||
                "restaurar".contains(query) ||
                "limpar".contains(query) ||
                "apagar".contains(query)

        if (showDataGroup) {
            SettingsGroup(title = "GERENCIAMENTO DE DADOS") {
                SettingsGroupRow(
                    icon = Icons.Default.FileDownload,
                    title = "Exportar Backup Criptografado",
                    subtitle = "Gera um arquivo .finbackup seguro",
                    valueText = "Exportar",
                    onClick = {
                        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                        createDocumentLauncher.launch("finanflow_backup_$timeStamp.finbackup")
                    },
                    testTag = "settings_export_backup_row"
                )

                SettingsRowDivider()

                SettingsGroupRow(
                    icon = Icons.Default.FolderOpen,
                    title = "Importar Backup Externo",
                    subtitle = "Carregar dados a partir de arquivo",
                    valueText = "Selecionar",
                    onClick = {
                        openDocumentLauncher.launch(arrayOf("*/*"))
                    },
                    testTag = "settings_import_backup_row"
                )

                SettingsRowDivider()

                SettingsGroupRow(
                    icon = Icons.Default.Security,
                    title = "Criar Backup Local Agora",
                    subtitle = "Último: $lastBackupStr",
                    valueText = "Criar",
                    onClick = onCreateBackup,
                    testTag = "settings_create_backup_row"
                )

                SettingsRowDivider()

                SettingsGroupRow(
                    icon = Icons.Default.History,
                    title = "Backups Salvos no Aparelho",
                    subtitle = "${backupFiles.size} cópias locais disponíveis",
                    valueText = "${backupFiles.size}",
                    onClick = { showBackupsSheet = true },
                    testTag = "settings_backups_history_row"
                )

                SettingsRowDivider()

                SettingsGroupRow(
                    icon = Icons.Default.RestartAlt,
                    title = "Restaurar Dados de Exemplo",
                    subtitle = "Substitui registros por conjunto de teste",
                    valueText = "Amostra",
                    onClick = { showResetDialog = true },
                    testTag = "reset_sample_data_button"
                )

                SettingsRowDivider()

                SettingsGroupRow(
                    icon = Icons.Default.DeleteSweep,
                    title = "Limpar Todos os Lançamentos",
                    subtitle = "Exclui definitivamente entradas e saídas",
                    valueText = "Apagar",
                    titleColor = ExpenseRed,
                    iconTint = ExpenseRed,
                    onClick = { showClearDialog = true },
                    testTag = "clear_all_data_button"
                )
            }
        }

        // ==========================================
        // 5. SOBRE
        // ==========================================
        val showAboutGroup = query.isEmpty() ||
                "sobre".contains(query) ||
                "versão".contains(query) ||
                "atualização".contains(query) ||
                "github".contains(query) ||
                "projeto".contains(query)

        if (showAboutGroup) {
            SettingsGroup(title = "SOBRE") {
                SettingsGroupRow(
                    icon = Icons.Default.Info,
                    title = "Versão do BUMoney",
                    subtitle = "Controle Financeiro Offline-first Room",
                    valueText = displayVersion,
                    onClick = {},
                    testTag = "app_version_badge"
                )

                SettingsRowDivider()

                val updateLabel = when {
                    isCheckingUpdates -> "Buscando..."
                    updateCheckStatus is com.example.util.UpdateCheckResult.UpdateAvailable -> "Nova Versão!"
                    else -> "Verificar"
                }

                SettingsGroupRow(
                    icon = Icons.Default.CloudDownload,
                    title = "Atualizações do Aplicativo",
                    subtitle = "Verifique releases oficiais do GitHub",
                    valueText = updateLabel,
                    onClick = { showUpdatesSheet = true },
                    testTag = "check_updates_button"
                )

                SettingsRowDivider()

                SettingsGroupRow(
                    icon = Icons.Default.Code,
                    title = "Repositório no GitHub",
                    subtitle = "xXMasterBrXx/Finan-as",
                    valueText = "GitHub",
                    onClick = {
                        try {
                            val browserIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/xXMasterBrXx/Finan-as")
                            ).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(browserIntent)
                        } catch (_: Exception) {}
                    },
                    testTag = "settings_github_repo_row"
                )
            }
        }

        Spacer(modifier = Modifier.height(110.dp))
    }

    // ==========================================
    // MODAIS E DIÁLOGOS
    // ==========================================

    // BottomSheet: Escolha de Tema
    if (showThemeSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showThemeSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Aparência & Temas",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Selecione o estilo visual preferido:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val themeOptions = listOf(
                    ThemeItemInfo(
                        mode = AppThemeMode.SYSTEM,
                        title = "Automático (Sistema)",
                        description = "Segue as preferências do seu aparelho",
                        icon = Icons.Default.BrightnessAuto,
                        previewBg = Color(0xFF64748B),
                        previewSurface = Color(0xFF94A3B8),
                        testTag = "theme_system_chip"
                    ),
                    ThemeItemInfo(
                        mode = AppThemeMode.LIGHT,
                        title = "Claro Puro",
                        description = "Branco clássico e nítido",
                        icon = Icons.Default.LightMode,
                        previewBg = Color(0xFFF8FAFC),
                        previewSurface = Color(0xFFFFFFFF),
                        testTag = "theme_light_chip"
                    ),
                    ThemeItemInfo(
                        mode = AppThemeMode.LIGHT_WARM,
                        title = "Claro Suave (Agradável)",
                        description = "Tom areia e creme suave aos olhos",
                        icon = Icons.Default.Brightness4,
                        previewBg = Color(0xFFF6F3EB),
                        previewSurface = Color(0xFFFCFAF6),
                        testTag = "theme_warm_light_chip"
                    ),
                    ThemeItemInfo(
                        mode = AppThemeMode.DARK,
                        title = "Escuro Noturno",
                        description = "Azul marinho e slate moderno",
                        icon = Icons.Default.DarkMode,
                        previewBg = Color(0xFF0B131E),
                        previewSurface = Color(0xFF152232),
                        testTag = "theme_dark_chip"
                    ),
                    ThemeItemInfo(
                        mode = AppThemeMode.DARK_OLED,
                        title = "Escuro OLED",
                        description = "Preto puro #000000 com economia de bateria",
                        icon = Icons.Default.DarkMode,
                        previewBg = Color(0xFF000000),
                        previewSurface = Color(0xFF0D0D0D),
                        testTag = "theme_oled_chip"
                    )
                )

                themeOptions.forEach { opt ->
                    val isSelected = (themeMode == opt.mode)
                    Surface(
                        onClick = {
                            onThemeModeChange(opt.mode)
                            showThemeSheet = false
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(opt.testTag)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(opt.previewBg)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(opt.previewSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = opt.icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = opt.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = opt.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selecionado",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // BottomSheet: Escolha de Cor de Destaque
    if (showColorSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showColorSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cor de Destaque",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Personalize com degradês sofisticados ou crie sua própria cor.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Card de Cor Personalizada
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            showCustomColorPicker = true
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (themeColor == AppThemeColor.CUSTOM)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(
                        width = if (themeColor == AppThemeColor.CUSTOM) 2.dp else 1.dp,
                        color = if (themeColor == AppThemeColor.CUSTOM)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Swatch circle da cor personalizada
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(customColorHex or 0xFF000000L))
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (themeColor == AppThemeColor.CUSTOM) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Criar Cor Personalizada",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (themeColor == AppThemeColor.CUSTOM) {
                                    Surface(
                                        shape = RoundedCornerShape(100.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                    ) {
                                        Text(
                                            text = "ATIVO",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Paleta livre, sliders HSV e código Hex (${String.format("#%06X", 0xFFFFFF and customColorHex.toInt())})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showCustomColorPicker = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (themeColor == AppThemeColor.CUSTOM)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = if (themeColor == AppThemeColor.CUSTOM)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Ajustar",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Paleta",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }

                Text(
                    text = "Cores Clássicas",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                val colorsToShow = listOf(
                    AppThemeColor.EMERALD,
                    AppThemeColor.BLUE,
                    AppThemeColor.PURPLE,
                    AppThemeColor.MINT,
                    AppThemeColor.ORANGE,
                    AppThemeColor.RED,
                    AppThemeColor.PINK
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    for (col in colorsToShow) {
                        val isColorSelected = (themeColor == col)
                        val colorHex = if (isSystemInDarkTheme()) col.primaryDarkHex else col.primaryLightHex

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    onThemeColorChange(col)
                                    showColorSheet = false
                                }
                                .padding(vertical = 4.dp, horizontal = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorHex))
                                    .border(
                                        width = if (isColorSelected) 3.dp else 1.5.dp,
                                        color = if (isColorSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                                    .testTag("theme_color_${col.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isColorSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.35f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = col.displayName,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = col.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isColorSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isColorSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog: Paleta Personalizada (Color Picker Studio)
    if (showCustomColorPicker) {
        CustomColorPickerDialog(
            initialColorHex = customColorHex,
            onDismissRequest = { showCustomColorPicker = false },
            onColorSelected = { selectedHex ->
                onCustomColorChange(selectedHex)
                showColorSheet = false
                showCustomColorPicker = false
            }
        )
    }

    // BottomSheet: Frequência de Backup
    if (showIntervalDialog) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showIntervalDialog = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Backup Automático",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Defina com que frequência o BUMoney deve gerar cópias de segurança criptografadas:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                listOf(
                    Triple("MANUAL", "Manual (apenas quando solicitado)", "Crie backups quando preferir"),
                    Triple("DAILY", "Diário", "Gera uma cópia de segurança a cada dia"),
                    Triple("WEEKLY", "Semanal", "Gera uma cópia a cada 7 dias")
                ).forEach { (code, title, desc) ->
                    val selected = backupInterval == code
                    Surface(
                        onClick = {
                            onBackupIntervalChange(code)
                            showIntervalDialog = false
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // BottomSheet: Antecedência dos Avisos
    if (showAdvanceDaysDialog) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAdvanceDaysDialog = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Antecedência dos Avisos",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Defina com quantos dias de antecedência você deseja receber o primeiro alerta de contas e faturas:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                listOf(
                    0 to "No próprio dia do vencimento",
                    1 to "1 dia antes (Recomendado)",
                    2 to "2 dias antes",
                    3 to "3 dias antes",
                    5 to "5 dias antes",
                    7 to "7 dias antes (1 semana)"
                ).forEach { (days, label) ->
                    val selected = notificationAdvanceDays == days
                    Surface(
                        onClick = {
                            onNotificationAdvanceDaysChange(days)
                            showAdvanceDaysDialog = false
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // BottomSheet: Horário das Notificações
    if (showTimePickerDialog) {
        var tempHour by remember { mutableStateOf(notificationHour) }
        var tempMinute by remember { mutableStateOf(notificationMinute) }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { showTimePickerDialog = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Horário das Notificações",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Start)
                )
                Text(
                    text = "Escolha o horário em que os alertas diários e lembretes serão enviados:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%02d:%02d", tempHour, tempMinute),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                    )
                }

                Text(
                    text = "Horários rápidos:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(8 to 0, 9 to 0, 12 to 0, 18 to 0, 20 to 0).forEach { (h, m) ->
                        val isSel = (tempHour == h && tempMinute == m)
                        Surface(
                            onClick = {
                                tempHour = h
                                tempMinute = m
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format(Locale.getDefault(), "%02d:%02d", h, m),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Hora", style = MaterialTheme.typography.labelSmall)
                        Text("${tempHour}h", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                    Slider(
                        value = tempHour.toFloat(),
                        onValueChange = { tempHour = it.toInt() },
                        valueRange = 0f..23f,
                        steps = 22
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Minuto", style = MaterialTheme.typography.labelSmall)
                        Text("${tempMinute} min", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                    Slider(
                        value = tempMinute.toFloat(),
                        onValueChange = { tempMinute = (it / 5).toInt() * 5 },
                        valueRange = 0f..55f,
                        steps = 10
                    )
                }

                Button(
                    onClick = {
                        onNotificationTimeChange(tempHour, tempMinute)
                        showTimePickerDialog = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Salvar Horário")
                }
            }
        }
    }

    // ModalBottomSheet: Configurações de Notificações & Alertas
    if (showNotificationSettingsSheet) {
        val notifSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showNotificationSettingsSheet = false },
            sheetState = notifSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (notificationsEnabled) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Notificações & Alertas",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Gerencie seus avisos e horários de lembrete",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = { showNotificationSettingsSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                // Master Switch Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (notificationsEnabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ativar Notificações",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (notificationsEnabled) "O aplicativo enviará avisos nos horários configurados" else "Todos os alertas automáticos estão pausados",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = onNotificationsEnabledChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("sheet_switch_master_notifications")
                        )
                    }
                }

                if (notificationsEnabled) {
                    // Section 1: Tipos de Alerta
                    SettingsGroup(title = "TIPOS DE ALERTA") {
                        // Contas Fixas & Recorrências
                        SettingsGroupRow(
                            icon = Icons.Default.EventRepeat,
                            title = "Contas Fixas & Recorrências",
                            subtitle = "Lembrete no dia e véspera do lançamento",
                            trailing = {
                                Switch(
                                    checked = notifyRecurring,
                                    onCheckedChange = onNotifyRecurringChange,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("switch_notify_recurring")
                                )
                            },
                            onClick = { onNotifyRecurringChange(!notifyRecurring) }
                        )

                        SettingsRowDivider()

                        // Fechamento de Fatura do Cartão
                        SettingsGroupRow(
                            icon = Icons.Default.CreditCard,
                            title = "Fechamento de Faturas",
                            subtitle = "Aviso do melhor dia para novas compras",
                            trailing = {
                                Switch(
                                    checked = notifyCardClosing,
                                    onCheckedChange = onNotifyCardClosingChange,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("switch_notify_card_closing")
                                )
                            },
                            onClick = { onNotifyCardClosingChange(!notifyCardClosing) }
                        )

                        SettingsRowDivider()

                        // Vencimento de Fatura do Cartão
                        SettingsGroupRow(
                            icon = Icons.Default.CreditCardOff,
                            title = "Vencimento de Faturas",
                            subtitle = "Evite multas e juros por atraso de cartão",
                            trailing = {
                                Switch(
                                    checked = notifyCardDue,
                                    onCheckedChange = onNotifyCardDueChange,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("switch_notify_card_due")
                                )
                            },
                            onClick = { onNotifyCardDueChange(!notifyCardDue) }
                        )

                        SettingsRowDivider()

                        // Alertas de Teto Mensal
                        SettingsGroupRow(
                            icon = Icons.Default.PieChart,
                            title = "Alerta de Teto de Gastos",
                            subtitle = "Aviso ao atingir 80% e 100% da sua meta",
                            trailing = {
                                Switch(
                                    checked = notifyBudgetLimit,
                                    onCheckedChange = onNotifyBudgetLimitChange,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("switch_notify_budget_limit")
                                )
                            },
                            onClick = { onNotifyBudgetLimitChange(!notifyBudgetLimit) }
                        )

                        SettingsRowDivider()

                        // Lembrete Diário
                        SettingsGroupRow(
                            icon = Icons.Default.CalendarToday,
                            title = "Lembrete Diário de Registros",
                            subtitle = "Aviso para não esquecer de anotar os gastos do dia",
                            trailing = {
                                Switch(
                                    checked = notifyDailyReminder,
                                    onCheckedChange = onNotifyDailyReminderChange,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("switch_notify_daily_reminder")
                                )
                            },
                            onClick = { onNotifyDailyReminderChange(!notifyDailyReminder) }
                        )
                    }

                    // Section 2: Agendamento & Horário
                    SettingsGroup(title = "AGENDAMENTO & HORÁRIO") {
                        val advanceDaysLabel = when (notificationAdvanceDays) {
                            0 -> "No próprio dia"
                            1 -> "1 dia antes (Recomendado)"
                            2 -> "2 dias antes"
                            3 -> "3 dias antes"
                            5 -> "5 dias antes"
                            7 -> "1 semana antes"
                            else -> "$notificationAdvanceDays dias antes"
                        }
                        SettingsGroupRow(
                            icon = Icons.Default.AccessTime,
                            title = "Antecedência dos Avisos",
                            subtitle = "Quantos dias antes enviar o primeiro alerta",
                            valueText = advanceDaysLabel,
                            onClick = { showAdvanceDaysDialog = true },
                            testTag = "settings_advance_days_row"
                        )

                        SettingsRowDivider()

                        val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", notificationHour, notificationMinute)
                        SettingsGroupRow(
                            icon = Icons.Default.Schedule,
                            title = "Horário das Notificações",
                            subtitle = "Horário preferencial para receber os alertas",
                            valueText = timeFormatted,
                            onClick = { showTimePickerDialog = true },
                            testTag = "settings_notification_time_row"
                        )
                    }

                    // Section 3: Teste de Disparo
                    SettingsGroup(title = "TESTES & DIAGNÓSTICO") {
                        SettingsGroupRow(
                            icon = Icons.Default.Send,
                            title = "Testar Notificação Agora",
                            subtitle = "Dispara um alerta de teste imediato no seu aparelho",
                            valueText = "Enviar teste",
                            onClick = onSendTestNotification,
                            testTag = "settings_send_test_notif_row"
                        )
                    }
                }
            }
        }
    }

    // ModalBottomSheet: Gerenciar Categorias Personalizadas
    if (showCategoriesSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showCategoriesSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Categorias Personalizadas",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Crie categorias sob medida para classificar suas receitas e despesas:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            showCategoriesSheet = false
                            onAddNewCategory(TransactionType.INCOME)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_income_cat_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = IncomeGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Entrada (+)", color = IncomeGreen, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            showCategoriesSheet = false
                            onAddNewCategory(TransactionType.EXPENSE)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_expense_cat_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Saída (+)", color = ExpenseRed, fontWeight = FontWeight.Bold)
                    }
                }

                if (customCategories.isNotEmpty()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Text(
                        text = "Suas Categorias Criadas (${customCategories.size})",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        customCategories.forEach { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                    .clickable {
                                        showCategoriesSheet = false
                                        onEditCategory(cat)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(cat.color.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = cat.icon, contentDescription = null, tint = cat.color, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = cat.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                        Text(
                                            text = if (cat.type == TransactionType.INCOME) "Entrada" else "Saída",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (cat.type == TransactionType.INCOME) IncomeGreen else ExpenseRed
                                        )
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            showCategoriesSheet = false
                                            onEditCategory(cat)
                                        },
                                        modifier = Modifier.size(32.dp).testTag("edit_category_${cat.dbId}")
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { onDeleteCategory(cat.dbId) },
                                        modifier = Modifier.size(32.dp).testTag("delete_category_${cat.dbId}")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ModalBottomSheet: Backups Salvos
    if (showBackupsSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showBackupsSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Backups Locais Salvos",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (backupFiles.isEmpty()) {
                    Text(
                        text = "Nenhum arquivo de backup gerado ainda neste aparelho.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        backupFiles.forEach { file ->
                            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(file.lastModified()))
                            val sizeKb = file.length() / 1024
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = file.name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                        Text(text = "$dateStr • ${sizeKb} KB", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        TextButton(onClick = {
                                            onRestoreBackup(file)
                                            showBackupsSheet = false
                                        }) {
                                            Text("Restaurar")
                                        }
                                        IconButton(onClick = { onDeleteBackup(file) }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ModalBottomSheet: Atualizações do Aplicativo
    if (showUpdatesSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showUpdatesSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Atualizações do Aplicativo",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "O BUMoney verifica atualizações via GitHub Releases oficial.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = { onCheckForUpdates() },
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isCheckingUpdates && !isDownloading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isCheckingUpdates) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buscando Atualizações...")
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verificar Atualizações Agora")
                    }
                }

                if (isDownloading) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Baixando atualização... ${downloadProgress ?: 0}%", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        LinearProgressIndicator(progress = { (downloadProgress ?: 0).toFloat() / 100f }, modifier = Modifier.fillMaxWidth())
                    }
                }

                updateCheckStatus?.let { status ->
                    when (status) {
                        is com.example.util.UpdateCheckResult.NoUpdate -> {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1B5E20).copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Você já está na versão mais recente!", color = Color(0xFF1B5E20), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                        is com.example.util.UpdateCheckResult.Error -> {
                            Text(text = "Erro: ${status.message}", color = ExpenseRed, style = MaterialTheme.typography.bodySmall)
                        }
                        is com.example.util.UpdateCheckResult.UpdateAvailable -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "Nova Versão: ${status.latestVersionName}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(text = status.releaseNotes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Button(
                                    onClick = { onDownloadAndInstallApk(status.downloadUrl) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Baixar e Instalar APK")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // BottomSheet: Definir Orçamento Mensal
    if (showBudgetDialog) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showBudgetDialog = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Teto de Gastos Mensal",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Informe o limite máximo que você planeja gastar por mês. Digite 0 para remover a meta.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = {
                        val filtered = it.replace(',', '.')
                        if (filtered.count { ch -> ch == '.' } <= 1 && filtered.all { ch -> ch.isDigit() || ch == '.' }) {
                            budgetInput = filtered
                        }
                    },
                    label = { Text("Limite (R$)") },
                    placeholder = { Text("0,00") },
                    leadingIcon = { Text("R$", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("budget_input_field")
                )

                Button(
                    onClick = {
                        val amount = budgetInput.toDoubleOrNull() ?: 0.0
                        onBudgetLimitChange(if (amount >= 0) amount else 0.0)
                        showBudgetDialog = false
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_budget_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Salvar Teto")
                }
            }
        }
    }

    // BottomSheet: Confirmar Restaurar Amostra
    if (showResetDialog) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showResetDialog = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Restaurar Dados de Exemplo?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Isso substituirá seus lançamentos atuais pelo conjunto inicial de teste (com salários, compras, lazer e contas).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showResetDialog = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            onResetToSampleData()
                            showResetDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f).testTag("confirm_reset_sample_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }

    // BottomSheet: Confirmar Apagar Tudo
    if (showClearDialog) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showClearDialog = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Apagar Todos os Lançamentos?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = ExpenseRed
                )
                Text(
                    text = "Tem certeza? Todos os registros de entradas e saídas serão excluídos definitivamente do aparelho.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showClearDialog = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            onClearAllData()
                            showClearDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                        modifier = Modifier.weight(1f).testTag("confirm_clear_all_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Sim, Apagar")
                    }
                }
            }
        }
    }
}

/**
 * Modern grouped settings container matching the reference UI style.
 */
@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                fontSize = 12.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(start = 6.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

/**
 * A single row within a settings group card.
 */
@Composable
private fun SettingsGroupRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    valueText: String? = null,
    leadingBadge: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    testTag: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading Icon Box
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Titles
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = titleColor
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Trailing Content (Value + Chevron or Custom Component)
        if (trailing != null) {
            trailing()
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (leadingBadge != null) {
                    leadingBadge()
                }

                if (valueText != null) {
                    Text(
                        text = valueText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Subtle divider between settings rows, indented past the leading icon.
 */
@Composable
private fun SettingsRowDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
        modifier = Modifier.padding(start = 66.dp, end = 16.dp)
    )
}

private data class ThemeItemInfo(
    val mode: AppThemeMode,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val previewBg: Color,
    val previewSurface: Color,
    val testTag: String
)

/**
 * Interactive Custom Color Palette Studio (HSV Sliders, Hex Code, Presets & Live Preview)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomColorPickerDialog(
    initialColorHex: Long,
    onDismissRequest: () -> Unit,
    onColorSelected: (Long) -> Unit
) {
    val initR = ((initialColorHex shr 16) and 0xFF).toInt()
    val initG = ((initialColorHex shr 8) and 0xFF).toInt()
    val initB = (initialColorHex and 0xFF).toInt()
    val initialHsv = FloatArray(3)
    android.graphics.Color.RGBToHSV(initR, initG, initB, initialHsv)

    var hue by remember { mutableStateOf(initialHsv[0]) }
    var saturation by remember { mutableStateOf(if (initialHsv[1] <= 0.05f) 0.8f else initialHsv[1]) }
    var value by remember { mutableStateOf(if (initialHsv[2] <= 0.05f) 0.9f else initialHsv[2]) }

    val currentRgbInt = android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))
    val currentHexStr = String.format("#%06X", 0xFFFFFF and currentRgbInt)
    var hexInputText by remember(currentHexStr) { mutableStateOf(currentHexStr) }
    var hexInputError by remember { mutableStateOf(false) }

    val currentColor = Color(currentRgbInt)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(currentColor)
                        .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Paleta Personalizada",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ajuste fino de matiz, saturação e luz",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Live Preview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(currentColor)
                                    .border(2.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = currentHexStr,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "RGB: ${((currentRgbInt shr 16) and 0xFF)}, ${((currentRgbInt shr 8) and 0xFF)}, ${(currentRgbInt and 0xFF)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = currentColor,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Text(
                                text = "Ativo",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Elementos de Demonstração
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = currentColor.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, currentColor.copy(alpha = 0.35f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Tag Visual",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = currentColor
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = currentColor,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Botão Principal",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Digitação direta do Código HEX
            OutlinedTextField(
                value = hexInputText,
                onValueChange = { input ->
                    hexInputText = input
                    val clean = input.trim().removePrefix("#")
                    if (clean.length == 6) {
                        try {
                            val parsedLong = clean.toLong(16)
                            val pr = ((parsedLong shr 16) and 0xFF).toInt()
                            val pg = ((parsedLong shr 8) and 0xFF).toInt()
                            val pb = (parsedLong and 0xFF).toInt()
                            val nhsv = FloatArray(3)
                            android.graphics.Color.RGBToHSV(pr, pg, pb, nhsv)
                            hue = nhsv[0]
                            saturation = nhsv[1]
                            value = nhsv[2]
                            hexInputError = false
                        } catch (e: Exception) {
                            hexInputError = true
                        }
                    } else {
                        hexInputError = input.isNotEmpty() && input != "#"
                    }
                },
                label = { Text("Código Hexadecimal") },
                placeholder = { Text("#B76E79") },
                singleLine = true,
                isError = hexInputError,
                supportingText = if (hexInputError) {
                    { Text("Digite um código hexadecimal válido (ex: #E07A5F)") }
                } else null,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = currentColor,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Slider 1: Matiz (Hue Rainbow Track)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Matiz / Tom (Hue)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${hue.toInt()}°",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Red,
                                    Color.Yellow,
                                    Color.Green,
                                    Color.Cyan,
                                    Color.Blue,
                                    Color.Magenta,
                                    Color.Red
                                )
                            )
                        )
                )

                Slider(
                    value = hue,
                    onValueChange = { hue = it },
                    valueRange = 0f..360f,
                    colors = SliderDefaults.colors(
                        thumbColor = currentColor,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    )
                )
            }

            // Slider 2: Saturação
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Saturação / Intensidade",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${(saturation * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Gray,
                                    Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, value)))
                                )
                            )
                        )
                )

                Slider(
                    value = saturation,
                    onValueChange = { saturation = it },
                    valueRange = 0.05f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = currentColor,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    )
                )
            }

            // Slider 3: Luminosidade
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Luminosidade / Brilho",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${(value * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Black,
                                    Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, 1f)))
                                )
                            )
                        )
                )

                Slider(
                    value = value,
                    onValueChange = { value = it },
                    valueRange = 0.15f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = currentColor,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    )
                )
            }

            // Sugestões de Paleta Rápida
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Sugestões de Designers",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                val quickSwatches = listOf(
                    0xFFB76E79L, // Rose Gold Clássico
                    0xFFFF6B6BL, // Coral Red
                    0xFFFF9F43L, // Laranja Sol
                    0xFFFECA57L, // Amarelo Dourado
                    0xFF10AC84L, // Verde Jade
                    0xFF1DD1A1L, // Menta Pastel
                    0xFF48DBFBL, // Azul Celeste
                    0xFF2E86DEL, // Azul Real
                    0xFF5F27CDL, // Roxo Deep
                    0xFFF368E0L, // Rosa Neon
                    0xFF8395A7L, // Titânio Slate
                    0xFF00D2D3L, // Verde Água
                    0xFFE056FDL, // Lavanda
                    0xFFFF7979L, // Pêssego
                    0xFF6C5CE7L, // Iris Elétrica
                    0xFFE17055L  // Terracota
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (swatch in quickSwatches) {
                        val swColor = Color(swatch or 0xFF000000L)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(swColor)
                                .border(
                                    width = if (currentHexStr.equals(String.format("#%06X", 0xFFFFFF and swatch.toInt()), ignoreCase = true)) 3.dp else 1.dp,
                                    color = if (currentHexStr.equals(String.format("#%06X", 0xFFFFFF and swatch.toInt()), ignoreCase = true)) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface,
                                    shape = CircleShape
                                )
                                .clickable {
                                    val sr = ((swatch shr 16) and 0xFF).toInt()
                                    val sg = ((swatch shr 8) and 0xFF).toInt()
                                    val sb = (swatch and 0xFF).toInt()
                                    val nhsv = FloatArray(3)
                                    android.graphics.Color.RGBToHSV(sr, sg, sb, nhsv)
                                    hue = nhsv[0]
                                    saturation = nhsv[1]
                                    value = nhsv[2]
                                }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val finalHex = 0xFF000000L or (0xFFFFFFL and currentRgbInt.toLong())
                    onColorSelected(finalHex)
                },
                colors = ButtonDefaults.buttonColors(containerColor = currentColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Aplicar no App", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
