package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.preferences.AppThemeMode
import com.example.data.preferences.AppThemeColor

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldLight,
    onPrimary = Color(0xFF003822),
    primaryContainer = EmeraldContainerDark,
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = TealSecondary,
    onSecondary = Color.White,
    tertiary = IndigoAccent,
    background = DarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E2D40)
)

private val OledDarkColorScheme = darkColorScheme(
    primary = EmeraldLight,
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF033324),
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = TealSecondary,
    onSecondary = Color.Black,
    tertiary = IndigoAccent,
    background = OledBackground,
    onBackground = Color(0xFFFFFFFF),
    surface = OledSurface,
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = OledSurfaceVariant,
    onSurfaceVariant = Color(0xFFA3A3A3),
    outline = Color(0xFF262626),
    outlineVariant = Color(0xFF171717)
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = EmeraldDark,
    secondary = TealSecondary,
    onSecondary = Color.White,
    tertiary = IndigoAccent,
    background = LightBackground,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

private val WarmLightColorScheme = lightColorScheme(
    primary = EmeraldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCF3E7),
    onPrimaryContainer = Color(0xFF004D30),
    secondary = TealSecondary,
    onSecondary = Color.White,
    tertiary = IndigoAccent,
    background = WarmLightBackground,
    onBackground = WarmLightText,
    surface = WarmLightSurface,
    onSurface = WarmLightText,
    surfaceVariant = WarmLightSurfaceVariant,
    onSurfaceVariant = WarmLightTextSecondary,
    outline = Color(0xFFD4CDC0),
    outlineVariant = Color(0xFFE2DCCF)
)

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    themeColor: AppThemeColor = AppThemeColor.EMERALD,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemDark
        AppThemeMode.DARK, AppThemeMode.DARK_OLED -> true
        else -> false
    }

    val primaryVal = Color(if (isDark) themeColor.primaryDarkHex else themeColor.primaryLightHex)
    val containerVal = Color(if (isDark) themeColor.containerDarkHex else themeColor.containerLightHex)
    val onContainerVal = Color(if (isDark) 0xFFFFFFFF else themeColor.primaryDarkHex)

    val baseColorScheme: ColorScheme = when (themeMode) {
        AppThemeMode.SYSTEM -> if (systemDark) DarkColorScheme else WarmLightColorScheme
        AppThemeMode.LIGHT -> LightColorScheme
        AppThemeMode.LIGHT_WARM -> WarmLightColorScheme
        AppThemeMode.DARK -> DarkColorScheme
        AppThemeMode.DARK_OLED -> OledDarkColorScheme
    }

    val colorScheme = baseColorScheme.copy(
        primary = primaryVal,
        primaryContainer = containerVal,
        onPrimaryContainer = onContainerVal,
        secondary = primaryVal,
        onSecondary = Color.White,
        tertiary = primaryVal
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
