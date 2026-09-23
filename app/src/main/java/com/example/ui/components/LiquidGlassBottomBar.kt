package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.AppThemeMode
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.LightCard
import com.example.ui.theme.OledCard
import com.example.ui.theme.WarmLightCard

data class NavigationTabItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
    val badgeCount: Int? = null
)

/**
 * Solid Card-Styled Bottom Navigation Bar.
 * Removed transparency to match the application's card aesthetic across all themes:
 * - OLED Pure Black: Solid OledCard background with subtle border
 * - Dark Slate: Solid DarkCard/DarkSurface background
 * - Warm Light: Solid WarmLightCard background
 * - Standard Light: Solid LightCard/Surface background with crisp border and elevation
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
    val systemDark = isSystemInDarkTheme()
    val isOled = themeMode == AppThemeMode.DARK_OLED
    val isDark = themeMode == AppThemeMode.DARK || (themeMode == AppThemeMode.SYSTEM && systemDark)
    val isWarm = themeMode == AppThemeMode.LIGHT_WARM || (themeMode == AppThemeMode.SYSTEM && !systemDark)

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
                testTag = "nav_tab_transactions"
            ),
            NavigationTabItem(
                title = "Gráficos",
                selectedIcon = Icons.Filled.PieChart,
                unselectedIcon = Icons.Outlined.PieChart,
                testTag = "nav_tab_charts"
            ),
            NavigationTabItem(
                title = "Cartões",
                selectedIcon = Icons.Filled.CreditCard,
                unselectedIcon = Icons.Outlined.CreditCard,
                testTag = "nav_tab_cards"
            )
        )
    }

    // Solid theme-matched container color matching app cards (100% opaque, zero transparency)
    val cardContainerColor = when {
        isOled -> OledCard
        isDark -> DarkCard
        isWarm -> WarmLightCard
        else -> LightCard
    }

    val cardBorderColor = when {
        isOled -> Color(0xFF222222)
        isDark -> Color(0xFF243447)
        isWarm -> Color(0xFFE6DECE)
        else -> Color(0xFFE2E8F0)
    }

    // Active pill background color
    val activePillColor = when {
        isOled -> MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
        isDark -> MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
        isWarm -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    }

    val activePillBorderColor = when {
        isOled -> MaterialTheme.colorScheme.primary.copy(alpha = 0.40f)
        isDark -> MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
        isWarm -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Solid Card Dock Navigation Bar with reduced width and fully rounded corners
        Card(
            modifier = Modifier
                .widthIn(max = 330.dp)
                .fillMaxWidth(0.88f)
                .height(56.dp)
                .testTag("liquid_glass_bottom_bar"),
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = cardContainerColor),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isDark || isOled) 4.dp else 6.dp
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.SolidColor(cardBorderColor)
            )
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val tabCount = tabs.size
                val tabWidth = maxWidth / tabCount

                // Smooth Sliding Active Pill Indicator
                val safeSelectedTab = selectedTab.coerceIn(0, (tabCount - 1).coerceAtLeast(0))
                val targetIndicatorOffset = tabWidth * safeSelectedTab
                val animatedIndicatorOffset by animateDpAsState(
                    targetValue = targetIndicatorOffset,
                    animationSpec = spring(
                        dampingRatio = 0.82f,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "bottom_bar_indicator_offset"
                )

                // Render Active Sliding Capsule (when a tab inside the bar is selected)
                if (selectedTab in 0 until tabCount) {
                    Box(
                        modifier = Modifier
                            .offset(x = animatedIndicatorOffset)
                            .width(tabWidth)
                            .fillMaxHeight()
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .clip(CircleShape)
                            .background(activePillColor)
                            .border(
                                width = 1.dp,
                                color = activePillBorderColor,
                                shape = CircleShape
                            )
                    )
                }

                // Tabs Row
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val isSelected = selectedTab == index
                        val interactionSource = remember { MutableInteractionSource() }

                        val iconScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.08f else 0.94f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "tab_scale_$index"
                        )

                        val contentColor by animateColorAsState(
                            targetValue = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                            },
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "tab_color_$index"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    onTabSelected(index)
                                }
                                .testTag(tab.testTag),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .padding(vertical = 2.dp)
                                    .scale(iconScale)
                            ) {
                                if (tab.badgeCount != null) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(15.dp)
                                            ) {
                                                Text(
                                                    text = if (tab.badgeCount > 99) "99+" else tab.badgeCount.toString(),
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title,
                                            tint = contentColor,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title,
                                        tint = contentColor,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        letterSpacing = 0.1.sp
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
