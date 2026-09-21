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
    val containerDarkHex: Long
) {
    EMERALD("Verde Esmeralda", 0xFF00A86B, 0xFF34D399, 0xFFE6F7F0, 0xFF064E3B),
    BLUE("Azul Safira", 0xFF1D4ED8, 0xFF60A5FA, 0xFFEFF6FF, 0xFF1E3A8A),
    PURPLE("Roxo Ametista", 0xFF7C3AED, 0xFFA78BFA, 0xFFF5F3FF, 0xFF4C1D95),
    ORANGE("Laranja Âmbar", 0xFFEA580C, 0xFFFB923C, 0xFFFFF7ED, 0xFF7C2D12),
    RED("Vermelho Rubi", 0xFFDC2626, 0xFFF87171, 0xFFFEF2F2, 0xFF7F1D1D),
    PINK("Rosa Choque", 0xFFDB2777, 0xFFF472B6, 0xFFFDF2F8, 0xFF5B0E2D),
    GOLD("Ouro Nobre", 0xFFD97706, 0xFFFBBF24, 0xFFFEF3C7, 0xFF451A03)
}

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("finanflow_user_prefs", Context.MODE_PRIVATE)

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

    private val _installedVersionTag = MutableStateFlow(
        prefs.getString(KEY_INSTALLED_VERSION_TAG, "") ?: ""
    )
    val installedVersionTag: StateFlow<String> = _installedVersionTag.asStateFlow()

    fun setInstalledVersionTag(tag: String) {
        val clean = tag.trim()
        prefs.edit().putString(KEY_INSTALLED_VERSION_TAG, clean).apply()
        _installedVersionTag.value = clean
    }

    fun isInitialDataSeeded(): Boolean {
        return prefs.getBoolean(KEY_HAS_SEEDED_INITIAL_DATA, false)
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
        private const val KEY_MONTHLY_BUDGET = "key_monthly_budget"
        private const val KEY_HIDE_BALANCES = "key_hide_balances"
        private const val KEY_DEFAULT_CURRENCY = "key_default_currency"
        private const val KEY_HAS_SEEDED_INITIAL_DATA = "key_has_seeded_initial_data"
        private const val KEY_P2P_SYNC_ENABLED = "key_p2p_sync_enabled"
        private const val KEY_P2P_SYNC_KEY = "key_p2p_sync_key"
        private const val KEY_BACKUP_INTERVAL = "key_backup_interval"
        private const val KEY_LAST_BACKUP_TIMESTAMP = "key_last_backup_timestamp"
        private const val KEY_GITHUB_REPO = "key_github_repo"
        private const val KEY_INSTALLED_VERSION_TAG = "key_installed_version_tag"

        @Volatile
        private var instance: UserPreferences? = null

        fun getInstance(context: Context): UserPreferences {
            return instance ?: synchronized(this) {
                instance ?: UserPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
