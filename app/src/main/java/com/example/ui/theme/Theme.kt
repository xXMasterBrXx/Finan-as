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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.preferences.AppThemeMode
import com.example.data.preferences.AppThemeColor

val LocalAppThemeColor = staticCompositionLocalOf { AppThemeColor.EMERALD }
val LocalThemeGradient = staticCompositionLocalOf {
    Brush.linearGradient(listOf(Color(0xFF00A86B), Color(0xFF34D399)))
}

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
    customColorHex: Long = 0xFFB76E79L,
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

    val primaryVal: Color
    val containerVal: Color
    val onContainerVal: Color

    if (themeColor == AppThemeColor.CUSTOM) {
        val baseLong = customColorHex or 0xFF000000L
        val r = ((baseLong shr 16) and 0xFF).toInt()
        val g = ((baseLong shr 8) and 0xFF).toInt()
        val b = (baseLong and 0xFF).toInt()
        val hsv = FloatArray(3)
        android.graphics.Color.RGBToHSV(r, g, b, hsv)

        if (isDark) {
            val darkHsv = floatArrayOf(
                hsv[0],
                (hsv[1] * 0.85f).coerceIn(0.25f, 0.85f),
                hsv[2].coerceAtLeast(0.75f)
            )
            primaryVal = Color(android.graphics.Color.HSVToColor(darkHsv))
            val contHsv = floatArrayOf(hsv[0], 0.85f, 0.22f)
            containerVal = Color(android.graphics.Color.HSVToColor(contHsv))
            onContainerVal = Color.White
        } else {
            val lightHsv = floatArrayOf(
                hsv[0],
                hsv[1].coerceAtLeast(0.65f),
                (hsv[2] * 0.9f).coerceIn(0.35f, 0.75f)
            )
            primaryVal = Color(android.graphics.Color.HSVToColor(lightHsv))
            val contHsv = floatArrayOf(hsv[0], 0.16f, 0.96f)
            containerVal = Color(android.graphics.Color.HSVToColor(contHsv))
            onContainerVal = primaryVal
        }
    } else {
        primaryVal = Color(if (isDark) themeColor.primaryDarkHex else themeColor.primaryLightHex)
        containerVal = Color(if (isDark) themeColor.containerDarkHex else themeColor.containerLightHex)
        onContainerVal = if (isDark) Color.White else Color(themeColor.primaryLightHex)
    }

    val baseColorScheme: ColorScheme = when (themeMode) {
        AppThemeMode.SYSTEM -> if (systemDark) DarkColorScheme else WarmLightColorScheme
        AppThemeMode.LIGHT -> LightColorScheme
        AppThemeMode.LIGHT_WARM -> WarmLightColorScheme
        AppThemeMode.DARK -> DarkColorScheme
        AppThemeMode.DARK_OLED -> OledDarkColorScheme
    }

    val colorScheme = baseColorScheme.copy(
        primary = primaryVal,
        onPrimary = Color.White,
        primaryContainer = containerVal,
        onPrimaryContainer = onContainerVal,
        secondary = primaryVal,
        onSecondary = Color.White,
        secondaryContainer = containerVal,
        onSecondaryContainer = onContainerVal,
        tertiary = primaryVal,
        tertiaryContainer = containerVal,
        onTertiaryContainer = onContainerVal,
        surfaceTint = primaryVal
    )

    val themeBrush = if (themeColor.isGradient && themeColor.gradientColors.size >= 2) {
        Brush.linearGradient(themeColor.gradientColors.map { Color(it) })
    } else {
        Brush.linearGradient(listOf(primaryVal, primaryVal))
    }

    CompositionLocalProvider(
        LocalAppThemeColor provides themeColor,
        LocalThemeGradient provides themeBrush
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

