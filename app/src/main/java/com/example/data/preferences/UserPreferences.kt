package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    LIGHT_WARM,
    DARK,
    DARK_OLED
}

enum class AppThemeColor(
    val displayName: String,
    val primaryLightHex: Long,
    val primaryDarkHex: Long,
    val containerLightHex: Long,
    val containerDarkHex: Long,
    val isGradient: Boolean = false,
    val gradientColors: List<Long> = emptyList()
) {
    // Cores em Degradê & Especiais
    ROSE_GOLD(
        displayName = "Rose Gold Degradê",
        primaryLightHex = 0xFFB76E79,
        primaryDarkHex = 0xFFE8A598,
        containerLightHex = 0xFFFDF2F4,
        containerDarkHex = 0xFF4A1F26,
        isGradient = true,
        gradientColors = listOf(0xFFE0A96D, 0xFFD4818F, 0xFFB76E79, 0xFFE8A598)
    ),
    SUNSET(
        displayName = "Pôr do Sol Degradê",
        primaryLightHex = 0xFFE11D48,
        primaryDarkHex = 0xFFFB7185,
        containerLightHex = 0xFFFFF1F2,
        containerDarkHex = 0xFF4C0519,
        isGradient = true,
        gradientColors = listOf(0xFFFF512F, 0xFFF09819, 0xFFDD2476)
    ),
    AURORA(
        displayName = "Aurora Boreal Degradê",
        primaryLightHex = 0xFF059669,
        primaryDarkHex = 0xFF34D399,
        containerLightHex = 0xFFECFDF5,
        containerDarkHex = 0xFF064E3B,
        isGradient = true,
        gradientColors = listOf(0xFF00C9FF, 0xFF92FE9D, 0xFF00B4D8)
    ),
    CYBER_NEON(
        displayName = "Cyberpunk Degradê",
        primaryLightHex = 0xFF9333EA,
        primaryDarkHex = 0xFFC084FC,
        containerLightHex = 0xFFFAF5FF,
        containerDarkHex = 0xFF3B0764,
        isGradient = true,
        gradientColors = listOf(0xFF8A2387, 0xFFE94057, 0xFFF27121)
    ),
    OCEAN_DEEP(
        displayName = "Oceano Degradê",
        primaryLightHex = 0xFF0284C7,
        primaryDarkHex = 0xFF38BDF8,
        containerLightHex = 0xFFF0F9FF,
        containerDarkHex = 0xFF0C4A6E,
        isGradient = true,
        gradientColors = listOf(0xFF2E3192, 0xFF1BFFFF, 0xFF0072FF)
    ),
    TITANIUM(
        displayName = "Titânio Grafite Degradê",
        primaryLightHex = 0xFF475569,
        primaryDarkHex = 0xFF94A3B8,
        containerLightHex = 0xFFF8FAFC,
        containerDarkHex = 0xFF1E293B,
        isGradient = true,
        gradientColors = listOf(0xFF2C3E50, 0xFF4CA1AF, 0xFFBDC3C7)
    ),
    GOLD(
        displayName = "Ouro Imperial Degradê",
        primaryLightHex = 0xFFD97706,
        primaryDarkHex = 0xFFFBBF24,
        containerLightHex = 0xFFFEF3C7,
        containerDarkHex = 0xFF451A03,
        isGradient = true,
        gradientColors = listOf(0xFFF7971E, 0xFFFFD200, 0xFFE65C00)
    ),

    // Cores Clássicas Sólidas
    EMERALD(
        displayName = "Verde Esmeralda",
        primaryLightHex = 0xFF00A86B,
        primaryDarkHex = 0xFF34D399,
        containerLightHex = 0xFFE6F7F0,
        containerDarkHex = 0xFF064E3B,
        isGradient = false,
        gradientColors = listOf(0xFF00A86B, 0xFF10B981, 0xFF34D399)
    ),
    BLUE(
        displayName = "Azul Safira",
        primaryLightHex = 0xFF1D4ED8,
        primaryDarkHex = 0xFF60A5FA,
        containerLightHex = 0xFFEFF6FF,
        containerDarkHex = 0xFF1E3A8A,
        isGradient = false,
        gradientColors = listOf(0xFF1D4ED8, 0xFF3B82F6, 0xFF60A5FA)
    ),
    PURPLE(
        displayName = "Roxo Ametista",
        primaryLightHex = 0xFF7C3AED,
        primaryDarkHex = 0xFFA78BFA,
        containerLightHex = 0xFFF5F3FF,
        containerDarkHex = 0xFF4C1D95,
        isGradient = false,
        gradientColors = listOf(0xFF7C3AED, 0xFF8B5CF6, 0xFFA78BFA)
    ),
    MINT(
        displayName = "Menta Fresca",
        primaryLightHex = 0xFF0D9488,
        primaryDarkHex = 0xFF2DD4BF,
        containerLightHex = 0xFFF0FDFA,
        containerDarkHex = 0xFF134E4A,
        isGradient = false,
        gradientColors = listOf(0xFF0D9488, 0xFF14B8A6, 0xFF2DD4BF)
    ),
    ORANGE(
        displayName = "Laranja Âmbar",
        primaryLightHex = 0xFFEA580C,
        primaryDarkHex = 0xFFFB923C,
        containerLightHex = 0xFFFFF7ED,
        containerDarkHex = 0xFF7C2D12,
        isGradient = false,
        gradientColors = listOf(0xFFEA580C, 0xFFFB923C)
    ),
    RED(
        displayName = "Vermelho Rubi",
        primaryLightHex = 0xFFDC2626,
        primaryDarkHex = 0xFFF87171,
        containerLightHex = 0xFFFEF2F2,
        containerDarkHex = 0xFF7F1D1D,
        isGradient = false,
        gradientColors = listOf(0xFFDC2626, 0xFFEF4444)
    ),
    PINK(
        displayName = "Rosa Magenta",
        primaryLightHex = 0xFFDB2777,
        primaryDarkHex = 0xFFF472B6,
        containerLightHex = 0xFFFDF2F8,
        containerDarkHex = 0xFF5B0E2D,
        isGradient = false,
        gradientColors = listOf(0xFFDB2777, 0xFFEC4899)
    ),

    // Cor Personalizada do Usuário
    CUSTOM(
        displayName = "Personalizada",
        primaryLightHex = 0xFFB76E79,
        primaryDarkHex = 0xFFE8A598,
        containerLightHex = 0xFFFDF2F4,
        containerDarkHex = 0xFF4A1F26,
        isGradient = false,
        gradientColors = emptyList()
    ),

    // Compatibilidade Legada
    TEAL(
        displayName = "Teal",
        primaryLightHex = 0xFF0D9488,
        primaryDarkHex = 0xFF2DD4BF,
        containerLightHex = 0xFFF0FDFA,
        containerDarkHex = 0xFF134E4A,
        isGradient = false,
        gradientColors = listOf(0xFF0D9488, 0xFF14B8A6, 0xFF2DD4BF)
    ),
    CYBER_VIOLET(
        displayName = "Cyber Violet",
        primaryLightHex = 0xFF9333EA,
        primaryDarkHex = 0xFFC084FC,
        containerLightHex = 0xFFFAF5FF,
        containerDarkHex = 0xFF3B0764,
        isGradient = true,
        gradientColors = listOf(0xFF8A2387, 0xFFE94057, 0xFFF27121)
    ),
    MIDNIGHT_GOLD(
        displayName = "Midnight Gold",
        primaryLightHex = 0xFFD97706,
        primaryDarkHex = 0xFFFBBF24,
        containerLightHex = 0xFFFEF3C7,
        containerDarkHex = 0xFF451A03,
        isGradient = true,
        gradientColors = listOf(0xFFF7971E, 0xFFFFD200, 0xFFE65C00)
    );

    fun getBrush(): androidx.compose.ui.graphics.Brush {
        return if (isGradient && gradientColors.size >= 2) {
            androidx.compose.ui.graphics.Brush.linearGradient(
                gradientColors.map { androidx.compose.ui.graphics.Color(it) }
            )
        } else {
            androidx.compose.ui.graphics.Brush.linearGradient(
                listOf(
                    androidx.compose.ui.graphics.Color(primaryLightHex),
                    androidx.compose.ui.graphics.Color(primaryDarkHex)
                )
            )
        }
    }
}

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("finanflow_user_prefs", Context.MODE_PRIVATE)

    init {
        // Clear any legacy premature version tag preference
        if (prefs.contains("key_installed_version_tag")) {
            prefs.edit().remove("key_installed_version_tag").apply()
        }
    }

    private val _themeMode = MutableStateFlow(
        try {
            AppThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _themeColor = MutableStateFlow(
        try {
            AppThemeColor.valueOf(prefs.getString(KEY_THEME_COLOR, AppThemeColor.EMERALD.name) ?: AppThemeColor.EMERALD.name)
        } catch (e: Exception) {
            AppThemeColor.EMERALD
        }
    )
    val themeColor: StateFlow<AppThemeColor> = _themeColor.asStateFlow()

    private val _customThemeColorHex = MutableStateFlow(
        prefs.getLong(KEY_CUSTOM_THEME_COLOR_HEX, 0xFFB76E79L)
    )
    val customThemeColorHex: StateFlow<Long> = _customThemeColorHex.asStateFlow()

    private val _backupInterval = MutableStateFlow(
        prefs.getString(KEY_BACKUP_INTERVAL, "MANUAL") ?: "MANUAL"
    )
    val backupInterval: StateFlow<String> = _backupInterval.asStateFlow()

    private val _lastBackupTimestamp = MutableStateFlow(
        prefs.getLong(KEY_LAST_BACKUP_TIMESTAMP, 0L)
    )
    val lastBackupTimestamp: StateFlow<Long> = _lastBackupTimestamp.asStateFlow()

    private val _monthlyBudgetLimit = MutableStateFlow(
        prefs.getFloat(KEY_MONTHLY_BUDGET, 0f).toDouble()
    )
    val monthlyBudgetLimit: StateFlow<Double> = _monthlyBudgetLimit.asStateFlow()

    private val _githubRepo = MutableStateFlow(
        prefs.getString(KEY_GITHUB_REPO, com.example.util.GitHubUpdateManager.DEFAULT_REPO) ?: com.example.util.GitHubUpdateManager.DEFAULT_REPO
    )
    val githubRepo: StateFlow<String> = _githubRepo.asStateFlow()

    private val _hideBalances = MutableStateFlow(
        prefs.getBoolean(KEY_HIDE_BALANCES, false)
    )
    val hideBalances: StateFlow<Boolean> = _hideBalances.asStateFlow()

    private val _defaultCurrency = MutableStateFlow(
        prefs.getString(KEY_DEFAULT_CURRENCY, "BRL") ?: "BRL"
    )
    val defaultCurrency: StateFlow<String> = _defaultCurrency.asStateFlow()

    private val _p2pSyncEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_P2P_SYNC_ENABLED, false)
    )
    val p2pSyncEnabled: StateFlow<Boolean> = _p2pSyncEnabled.asStateFlow()

    private val _p2pSyncKey = MutableStateFlow(
        prefs.getString(KEY_P2P_SYNC_KEY, "") ?: ""
    )
    val p2pSyncKey: StateFlow<String> = _p2pSyncKey.asStateFlow()

    // Notification Preferences
    private val _notificationsEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    )
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _notifyRecurring = MutableStateFlow(
        prefs.getBoolean(KEY_NOTIFY_RECURRING, true)
    )
    val notifyRecurring: StateFlow<Boolean> = _notifyRecurring.asStateFlow()

    private val _notifyCardClosing = MutableStateFlow(
        prefs.getBoolean(KEY_NOTIFY_CARD_CLOSING, true)
    )
    val notifyCardClosing: StateFlow<Boolean> = _notifyCardClosing.asStateFlow()

    private val _notifyCardDue = MutableStateFlow(
        prefs.getBoolean(KEY_NOTIFY_CARD_DUE, true)
    )
    val notifyCardDue: StateFlow<Boolean> = _notifyCardDue.asStateFlow()

    private val _notifyBudgetLimit = MutableStateFlow(
        prefs.getBoolean(KEY_NOTIFY_BUDGET_LIMIT, true)
    )
    val notifyBudgetLimit: StateFlow<Boolean> = _notifyBudgetLimit.asStateFlow()

    private val _notifyDailyReminder = MutableStateFlow(
        prefs.getBoolean(KEY_NOTIFY_DAILY_REMINDER, false)
    )
    val notifyDailyReminder: StateFlow<Boolean> = _notifyDailyReminder.asStateFlow()

    private val _notificationAdvanceDays = MutableStateFlow(
        prefs.getInt(KEY_NOTIFICATION_ADVANCE_DAYS, 1) // 0 = no dia, 1 = 1 dia antes, 2 = 2 dias antes
    )
    val notificationAdvanceDays: StateFlow<Int> = _notificationAdvanceDays.asStateFlow()

    private val _notificationHour = MutableStateFlow(
        prefs.getInt(KEY_NOTIFICATION_HOUR, 9) // 09:00 AM padrão
    )
    val notificationHour: StateFlow<Int> = _notificationHour.asStateFlow()

    private val _notificationMinute = MutableStateFlow(
        prefs.getInt(KEY_NOTIFICATION_MINUTE, 0)
    )
    val notificationMinute: StateFlow<Int> = _notificationMinute.asStateFlow()

    fun isInitialDataSeeded(): Boolean {
        return prefs.getBoolean(KEY_HAS_SEEDED_INITIAL_DATA, false)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        _notificationsEnabled.value = enabled
    }

    fun setNotifyRecurring(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFY_RECURRING, enabled).apply()
        _notifyRecurring.value = enabled
    }

    fun setNotifyCardClosing(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFY_CARD_CLOSING, enabled).apply()
        _notifyCardClosing.value = enabled
    }

    fun setNotifyCardDue(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFY_CARD_DUE, enabled).apply()
        _notifyCardDue.value = enabled
    }

    fun setNotifyBudgetLimit(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFY_BUDGET_LIMIT, enabled).apply()
        _notifyBudgetLimit.value = enabled
    }

    fun setNotifyDailyReminder(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFY_DAILY_REMINDER, enabled).apply()
        _notifyDailyReminder.value = enabled
    }

    fun setNotificationAdvanceDays(days: Int) {
        prefs.edit().putInt(KEY_NOTIFICATION_ADVANCE_DAYS, days).apply()
        _notificationAdvanceDays.value = days
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_NOTIFICATION_HOUR, hour)
            .putInt(KEY_NOTIFICATION_MINUTE, minute)
            .apply()
        _notificationHour.value = hour
        _notificationMinute.value = minute
    }

    fun setInitialDataSeeded(seeded: Boolean = true) {
        prefs.edit().putBoolean(KEY_HAS_SEEDED_INITIAL_DATA, seeded).apply()
    }

    fun setP2PSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_P2P_SYNC_ENABLED, enabled).apply()
        _p2pSyncEnabled.value = enabled
    }

    fun setP2PSyncKey(key: String) {
        val clean = key.trim().uppercase()
        prefs.edit().putString(KEY_P2P_SYNC_KEY, clean).apply()
        _p2pSyncKey.value = clean
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun setThemeColor(color: AppThemeColor) {
        prefs.edit().putString(KEY_THEME_COLOR, color.name).apply()
        _themeColor.value = color
    }

    fun setCustomThemeColor(hex: Long) {
        prefs.edit()
            .putLong(KEY_CUSTOM_THEME_COLOR_HEX, hex)
            .putString(KEY_THEME_COLOR, AppThemeColor.CUSTOM.name)
            .apply()
        _customThemeColorHex.value = hex
        _themeColor.value = AppThemeColor.CUSTOM
    }

    fun setMonthlyBudgetLimit(amount: Double) {
        prefs.edit().putFloat(KEY_MONTHLY_BUDGET, amount.toFloat()).apply()
        _monthlyBudgetLimit.value = amount
    }

    fun setHideBalances(hide: Boolean) {
        prefs.edit().putBoolean(KEY_HIDE_BALANCES, hide).apply()
        _hideBalances.value = hide
    }

    fun setDefaultCurrency(curr: String) {
        prefs.edit().putString(KEY_DEFAULT_CURRENCY, curr).apply()
        _defaultCurrency.value = curr
    }

    fun getBackupInterval(): String {
        return prefs.getString(KEY_BACKUP_INTERVAL, "MANUAL") ?: "MANUAL"
    }

    fun setBackupInterval(interval: String) {
        prefs.edit().putString(KEY_BACKUP_INTERVAL, interval).apply()
        _backupInterval.value = interval
    }

    fun getLastBackupTimestamp(): Long {
        return prefs.getLong(KEY_LAST_BACKUP_TIMESTAMP, 0L)
    }

    fun setLastBackupTimestamp(ts: Long) {
        prefs.edit().putLong(KEY_LAST_BACKUP_TIMESTAMP, ts).apply()
        _lastBackupTimestamp.value = ts
    }

    fun setGithubRepo(repo: String) {
        prefs.edit().putString(KEY_GITHUB_REPO, repo).apply()
        _githubRepo.value = repo
    }

    fun getString(key: String, defaultValue: String): String? {
        return prefs.getString(key, defaultValue)
    }

    fun setString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    companion object {
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_THEME_COLOR = "key_theme_color"
        private const val KEY_CUSTOM_THEME_COLOR_HEX = "key_custom_theme_color_hex"
        private const val KEY_MONTHLY_BUDGET = "key_monthly_budget"
        private const val KEY_HIDE_BALANCES = "key_hide_balances"
        private const val KEY_DEFAULT_CURRENCY = "key_default_currency"
        private const val KEY_HAS_SEEDED_INITIAL_DATA = "key_has_seeded_initial_data"
        private const val KEY_P2P_SYNC_ENABLED = "key_p2p_sync_enabled"
        private const val KEY_P2P_SYNC_KEY = "key_p2p_sync_key"
        private const val KEY_BACKUP_INTERVAL = "key_backup_interval"
        private const val KEY_LAST_BACKUP_TIMESTAMP = "key_last_backup_timestamp"
        private const val KEY_GITHUB_REPO = "key_github_repo"
        private const val KEY_NOTIFICATIONS_ENABLED = "key_notifications_enabled"
        private const val KEY_NOTIFY_RECURRING = "key_notify_recurring"
        private const val KEY_NOTIFY_CARD_CLOSING = "key_notify_card_closing"
        private const val KEY_NOTIFY_CARD_DUE = "key_notify_card_due"
        private const val KEY_NOTIFY_BUDGET_LIMIT = "key_notify_budget_limit"
        private const val KEY_NOTIFY_DAILY_REMINDER = "key_notify_daily_reminder"
        private const val KEY_NOTIFICATION_ADVANCE_DAYS = "key_notification_advance_days"
        private const val KEY_NOTIFICATION_HOUR = "key_notification_hour"
        private const val KEY_NOTIFICATION_MINUTE = "key_notification_minute"

        @Volatile
        private var instance: UserPreferences? = null

        fun getInstance(context: Context): UserPreferences {
            return instance ?: synchronized(this) {
                instance ?: UserPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
