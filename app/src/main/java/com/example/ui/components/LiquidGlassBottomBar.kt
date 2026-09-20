package com.example.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.data.preferences.AppThemeMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class NavigationTabItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
    val badgeCount: Int? = null
)

/**
 * Liquid Glass floating pill bottom navigation bar.
 * Adapts dynamically to any theme mode (Light, Warm Light, Dark, OLED).
 */
@Composable
fun LiquidGlassBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    transactionCount: Int,
    cardCount: Int,
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val systemDark = isSystemInDarkTheme()
    val isOled = themeMode == AppThemeMode.DARK_OLED || (themeMode == AppThemeMode.SYSTEM && systemDark && surfaceColor.luminance() < 0.05f)
    val isDark = themeMode == AppThemeMode.DARK || (themeMode == AppThemeMode.SYSTEM && systemDark && !isOled)
    val isWarm = themeMode == AppThemeMode.LIGHT_WARM

    val tabs = remember(transactionCount, cardCount) {
        listOf(
            NavigationTabItem(
                title = "Resumo",
                selectedIcon = Icons.Filled.AutoGraph,
                unselectedIcon = Icons.Outlined.AutoGraph,
                testTag = "nav_tab_overview"
            ),
            NavigationTabItem(
                title = "Extrato",
                selectedIcon = Icons.Filled.ListAlt,
                unselectedIcon = Icons.Outlined.ListAlt,
                testTag = "nav_tab_transactions",
                badgeCount = if (transactionCount > 0) transactionCount else null
            ),
            NavigationTabItem(
                title = "Cartões",
                selectedIcon = Icons.Filled.CreditCard,
                unselectedIcon = Icons.Outlined.CreditCard,
                testTag = "nav_tab_cards",
                badgeCount = if (cardCount > 0) cardCount else null
            ),
            NavigationTabItem(
                title = "Ajustes",
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings,
                testTag = "nav_tab_settings"
            )
        )
    }

    // Frosted Glass Layer with true transparency and distinct dedicated colors for OLED, Dark (Noturno), Warm Light, and Standard Light
    val frostedLayer = when {
        isOled -> Brush.verticalGradient(
            listOf(
                Color(0xFF141414).copy(alpha = 0.82f),
                Color(0xFF000000).copy(alpha = 0.88f)
            )
        )
        isDark -> Brush.verticalGradient(
            listOf(
                Color(0xFF374151).copy(alpha = 0.78f),
                Color(0xFF1F2937).copy(alpha = 0.85f)
            )
        )
        isWarm -> Brush.verticalGradient(
            listOf(
                Color(0xFFF7F4EE).copy(alpha = 0.82f),
                Color(0xFFEBE4D8).copy(alpha = 0.88f)
            )
        )
        else -> Brush.verticalGradient(
            listOf(
                surfaceColor.copy(alpha = 0.82f),
                surfaceVariantColor.copy(alpha = 0.88f)
            )
        )
    }

    // Specular border reflection for frosted glass
    val glassBorderBrush = when {
        isOled -> Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.35f),
                Color.White.copy(alpha = 0.08f)
            )
        )
        isDark -> Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.40f),
                Color.White.copy(alpha = 0.10f)
            )
        )
        isWarm -> Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.95f),
                Color(0xFFD4CDC0).copy(alpha = 0.45f)
            )
        )
        else -> Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.98f),
                MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
            )
        )
    }

    val shadowSpotColor = when {
        isOled -> Color.Black
        isDark -> Color.Black.copy(alpha = 0.40f)
        isWarm -> Color.Black.copy(alpha = 0.08f)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
    }

    val shadowAmbientColor = when {
        isOled -> Color.Black
        isDark -> Color.Black.copy(alpha = 0.20f)
        isWarm -> Color.Black.copy(alpha = 0.03f)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Frosted Glass Pill
        Surface(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .height(68.dp)
                .shadow(
                    elevation = if (isDark) 16.dp else 12.dp,
                    shape = RoundedCornerShape(34.dp),
                    spotColor = shadowSpotColor,
                    ambientColor = shadowAmbientColor
                )
                .clip(RoundedCornerShape(34.dp))
                .border(
                    width = 1.2.dp,
                    brush = glassBorderBrush,
                    shape = RoundedCornerShape(34.dp)
                )
                .testTag("liquid_glass_bottom_bar"),
            color = Color.Transparent
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Frosted blur background layer providing true intensive gaussian blur
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(frostedLayer)
                        .blur(radius = 55.dp)
                )

                // Sharp Icon & Text Content Layer on top
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, tab ->
                    val isSelected = (selectedTab == index)

                    // Spring animation for active pill scale and colors
                    val activeScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.05f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "tab_scale_$index"
                    )

                    val pillBgColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = if (isDark || isOled) 0.32f else 0.18f)
                        } else {
                            Color.Transparent
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "pill_bg_$index"
                    )

                    val pillBorderColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = if (isDark || isOled) 0.55f else 0.40f)
                        } else {
                            Color.Transparent
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "pill_border_$index"
                    )

                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            if (isDark || isOled) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "tab_color_$index"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(26.dp))
                            .background(pillBgColor)
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = pillBorderColor,
                                shape = RoundedCornerShape(26.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onTabSelected(index)
                            }
                            .scale(activeScale)
                            .testTag(tab.testTag),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            if (tab.badgeCount != null) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(16.dp)
                                        ) {
                                            Text(
                                                text = if (tab.badgeCount > 99) "99+" else tab.badgeCount.toString(),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title,
                                        tint = contentColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title,
                                    tint = contentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    letterSpacing = 0.2.sp
                                ),
                                color = contentColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            }
        }
    }
}
