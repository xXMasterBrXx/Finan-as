package com.example.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
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
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.AppThemeMode

data class NavigationTabItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
    val badgeCount: Int? = null
)

/**
 * Apple-style Liquid Glass floating pill bottom navigation bar.
 * - Compact & ergonomic 56dp pill height with balanced proportions.
 * - Authentic Apple-style frosted glass with specular border sheen and blur.
 * - Smooth sliding indicator capsule that glides across tabs with fluid physics.
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

    // Apple-style Frosted Glass translucent background layer
    val frostedLayer = when {
        isOled -> Brush.verticalGradient(
            listOf(
                Color(0xFF141416).copy(alpha = 0.76f),
                Color(0xFF09090B).copy(alpha = 0.82f)
            )
        )
        isDark -> Brush.verticalGradient(
            listOf(
                Color(0xFF23272F).copy(alpha = 0.74f),
                Color(0xFF171A20).copy(alpha = 0.80f)
            )
        )
        isWarm -> Brush.verticalGradient(
            listOf(
                Color(0xFFFAF7F2).copy(alpha = 0.78f),
                Color(0xFFEFE8DC).copy(alpha = 0.68f)
            )
        )
        else -> Brush.verticalGradient(
            listOf(
                Color(0xFFFFFFFF).copy(alpha = 0.72f),
                Color(0xFFF1F5F9).copy(alpha = 0.65f)
            )
        )
    }

    // Apple-style Specular Border Highlight (hairline gradient catching top light)
    val glassBorderBrush = when {
        isOled -> Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.32f),
                Color.White.copy(alpha = 0.06f)
            )
        )
        isDark -> Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.38f),
                Color.White.copy(alpha = 0.08f)
            )
        )
        isWarm -> Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.88f),
                Color(0xFFDCD4C7).copy(alpha = 0.35f)
            )
        )
        else -> Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.90f),
                Color(0xFFCBD5E1).copy(alpha = 0.35f)
            )
        )
    }

    // Sliding active pill background & border
    val slidingPillBackground = when {
        isOled -> Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.16f),
                Color.White.copy(alpha = 0.06f)
            )
        )
        isDark -> Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.18f),
                Color.White.copy(alpha = 0.08f)
            )
        )
        isWarm -> Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.96f),
                Color(0xFFF7F3EB).copy(alpha = 0.88f)
            )
        )
        else -> Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.95f),
                Color(0xFFF8FAFC).copy(alpha = 0.88f)
            )
        )
    }

    val slidingPillBorder = when {
        isOled -> Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.28f),
                Color.White.copy(alpha = 0.05f)
            )
        )
        isDark -> Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.32f),
                Color.White.copy(alpha = 0.06f)
            )
        )
        isWarm -> Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.95f),
                Color(0xFFE2D9CB).copy(alpha = 0.50f)
            )
        )
        else -> Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.95f),
                Color(0xFFE2E8F0).copy(alpha = 0.60f)
            )
        )
    }

    val shadowSpotColor = when {
        isOled -> Color.Black.copy(alpha = 0.70f)
        isDark -> Color.Black.copy(alpha = 0.45f)
        isWarm -> Color(0xFF5A4D3B).copy(alpha = 0.12f)
        else -> Color(0xFF0F172A).copy(alpha = 0.12f)
    }

    val shadowAmbientColor = when {
        isOled -> Color.Black.copy(alpha = 0.35f)
        isDark -> Color.Black.copy(alpha = 0.25f)
        isWarm -> Color.Black.copy(alpha = 0.04f)
        else -> Color.Black.copy(alpha = 0.04f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Frosted Glass Pill (Horizontally compact 300dp for a sleek floating dock aesthetic)
        Surface(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .fillMaxWidth()
                .height(56.dp)
                .shadow(
                    elevation = if (isDark || isOled) 14.dp else 10.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = shadowSpotColor,
                    ambientColor = shadowAmbientColor
                )
                .clip(RoundedCornerShape(28.dp))
                .border(
                    width = 0.8.dp,
                    brush = glassBorderBrush,
                    shape = RoundedCornerShape(28.dp)
                )
                .testTag("liquid_glass_bottom_bar"),
            color = Color.Transparent
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val tabCount = tabs.size
                val tabWidth = maxWidth / tabCount

                // Translucent Apple Frosted Glass Material Tint
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(frostedLayer)
                )

                // Smooth Sliding Indicator Capsule (Glides across tabs with spring physics)
                val targetOffset = tabWidth * selectedTab
                val animatedOffset by animateDpAsState(
                    targetValue = targetOffset,
                    animationSpec = spring(
                        dampingRatio = 0.85f, // Smooth Apple-style glide without harsh bounce
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "sliding_pill_glide"
                )

                Box(
                    modifier = Modifier
                        .offset(x = animatedOffset)
                        .width(tabWidth)
                        .fillMaxHeight()
                        .padding(horizontal = 2.5.dp, vertical = 3.dp)
                        .shadow(
                            elevation = if (isDark || isOled) 2.dp else 3.dp,
                            shape = RoundedCornerShape(25.dp),
                            spotColor = if (isDark || isOled) Color.Black.copy(alpha = 0.45f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                            ambientColor = Color.Transparent
                        )
                        .clip(RoundedCornerShape(25.dp))
                        .background(slidingPillBackground)
                        .border(
                            width = 0.8.dp,
                            brush = slidingPillBorder,
                            shape = RoundedCornerShape(25.dp)
                        )
                )

                // Tab items laid out over the sliding capsule
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val isSelected = (selectedTab == index)

                        // Smooth scale & vibrant color animation for icon & label
                        val iconScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.08f else 1.0f,
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
                                if (isDark || isOled) Color(0xFF94A3B8).copy(alpha = 0.70f)
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
                            },
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "tab_color_$index"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(25.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
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

                                Spacer(modifier = Modifier.height(1.dp))

                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.5.sp,
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
