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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var showIntervalDialog by remember { mutableStateOf(false) }
    var showCategoriesSheet by remember { mutableStateOf(false) }
    var showBackupsSheet by remember { mutableStateOf(false) }
    var showUpdatesSheet by remember { mutableStateOf(false) }

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
                    subtitle = "Cor principal aplicada a botões e gráficos",
                    valueText = themeColor.displayName,
                    leadingBadge = {
                        val colorHex = if (isSystemInDarkTheme()) themeColor.primaryDarkHex else themeColor.primaryLightHex
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(colorHex))
                        )
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
                    title = "Versão do FinanFlow",
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Cor de Destaque",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Escolha a cor principal para realçar botões e elementos ativos:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    for (col in AppThemeColor.values()) {
                        val isColorSelected = (themeColor == col)
                        val colorHex = if (isSystemInDarkTheme()) col.primaryDarkHex else col.primaryLightHex

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    onThemeColorChange(col)
                                    showColorSheet = false
                                }
                                .padding(4.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(colorHex),
                                modifier = Modifier
                                    .size(50.dp)
                                    .testTag("theme_color_${col.name.lowercase()}"),
                                border = if (isColorSelected) BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface) else null
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isColorSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = col.displayName,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = col.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isColorSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog: Frequência de Backup
    if (showIntervalDialog) {
        AlertDialog(
            onDismissRequest = { showIntervalDialog = false },
            title = { Text("Backup Automático") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Defina com que frequência o FinanFlow deve gerar cópias de segurança criptografadas:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
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
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
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
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showIntervalDialog = false }) {
                    Text("Fechar")
                }
            }
        )
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
                    text = "O FinanFlow verifica atualizações via GitHub Releases oficial.",
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

    // Dialog: Definir Orçamento Mensal
    if (showBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text("Teto de Gastos Mensal") },
            text = {
                Column {
                    Text(
                        text = "Informe o limite máximo que você planeja gastar por mês. Digite 0 para remover a meta.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = budgetInput.toDoubleOrNull() ?: 0.0
                        onBudgetLimitChange(if (amount >= 0) amount else 0.0)
                        showBudgetDialog = false
                    },
                    modifier = Modifier.testTag("save_budget_button")
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog: Confirmar Restaurar Amostra
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Restaurar dados de exemplo?") },
            text = {
                Text("Isso substituirá seus lançamentos atuais pelo conjunto inicial de teste (com salários, compras, lazer e contas).")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetToSampleData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("confirm_reset_sample_button")
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog: Confirmar Apagar Tudo
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Apagar todos os lançamentos?") },
            text = {
                Text("Tem certeza? Todos os registros de entradas e saídas serão excluídos definitivamente.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    modifier = Modifier.testTag("confirm_clear_all_button")
                ) {
                    Text("Sim, Apagar Tudo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
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
