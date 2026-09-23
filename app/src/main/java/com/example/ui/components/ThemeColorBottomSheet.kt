package com.example.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.AppThemeColor
import com.example.ui.theme.getBrush

// 18 Cores de inspiração para a paleta interativa
private val INSPIRATION_PALETTE_HEX = listOf(
    0xFFB76E79L, // Rose Gold Clássico
    0xFFF43F5E, // Coral Rosa
    0xFFFB7185, // Rosa Flamingo
    0xFFE11D48, // Carmim Rubelita
    0xFFF97316, // Laranja Pôr do Sol
    0xFFFBBF24, // Âmbar Dourado
    0xFFEAB308, // Ouro Real
    0xFF10B981, // Esmeralda Fresco
    0xFF059669, // Jade Botânico
    0xFF14B8A6, // Menta Turquesa
    0xFF06B6D4, // Ciano Oceano
    0xFF0EA5E9, // Azul Safira
    0xFF3B82F6, // Azul Cobalto
    0xFF6366F1, // Índigo Elétrico
    0xFF8B5CF6, // Violeta Intenso
    0xFFA855F7, // Púrpura Imperial
    0xFFEC4899, // Pink Neon
    0xFF64748B  // Grafite Titânio
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeColorBottomSheet(
    themeColor: AppThemeColor,
    customColorHex: Long,
    onThemeColorChange: (AppThemeColor) -> Unit,
    onCustomColorChange: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isDark = isSystemInDarkTheme()
    var selectedTab by remember {
        mutableIntStateOf(
            when {
                themeColor == AppThemeColor.CUSTOM -> 2
                themeColor.isGradient -> 0
                else -> 1
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("theme_color_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Cores",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Cores e Degradês de Destaque",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Escolha um degradê, cor clássica ou crie a sua",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Tabs: Degradês / Clássicas / Paleta
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Degradês", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Palette,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clássicas", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Paleta", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> GradientThemesSection(
                    currentTheme = themeColor,
                    isDark = isDark,
                    onSelect = {
                        onThemeColorChange(it)
                        onDismiss()
                    }
                )
                1 -> ClassicColorsSection(
                    currentTheme = themeColor,
                    isDark = isDark,
                    onSelect = {
                        onThemeColorChange(it)
                        onDismiss()
                    }
                )
                2 -> CustomPaletteSection(
                    customColorHex = customColorHex,
                    isDark = isDark,
                    onApplyCustomColor = { hex ->
                        onCustomColorChange(hex)
                        onThemeColorChange(AppThemeColor.CUSTOM)
                        onDismiss()
                    }
                )
            }
        }
    }
}

/**
 * Seção de temas em degradê (Rose Gold, Pôr do Sol, Aurora Boreal, etc.)
 */
@Composable
private fun GradientThemesSection(
    currentTheme: AppThemeColor,
    isDark: Boolean,
    onSelect: (AppThemeColor) -> Unit
) {
    val gradientColors = remember {
        listOf(
            AppThemeColor.ROSE_GOLD,
            AppThemeColor.SUNSET,
            AppThemeColor.AURORA,
            AppThemeColor.CYBER_VIOLET,
            AppThemeColor.OCEAN_DEEP,
            AppThemeColor.MIDNIGHT_GOLD
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Efeitos em degradê sofisticados aplicados a cabeçalhos e botões:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        for (col in gradientColors) {
            val isSelected = (currentTheme == col)
            val brush = col.getBrush(isDark)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(col) }
                    .testTag("gradient_color_${col.name.lowercase()}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    // Preview do degradê em formato de cápsula
                    Box(
                        modifier = Modifier
                            .size(width = 56.dp, height = 40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(brush),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selecionado",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = col.displayName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (col == AppThemeColor.ROSE_GOLD) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFF4A896).copy(alpha = 0.25f),
                                    modifier = Modifier.padding(1.dp)
                                ) {
                                    Text(
                                        text = "Destaque",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = if (isDark) Color(0xFFFFCAD4) else Color(0xFFB76E79),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = when (col) {
                                AppThemeColor.ROSE_GOLD -> "Rosa champanhe luxuoso e metálico"
                                AppThemeColor.SUNSET -> "Coral carmesim vibrante ao pôr do sol"
                                AppThemeColor.AURORA -> "Verde esmeralda brilhante ao ciano ártico"
                                AppThemeColor.CYBER_VIOLET -> "Roxo cibernético ao neon magenta"
                                AppThemeColor.OCEAN_DEEP -> "Azul marinho profundo à turquesa"
                                AppThemeColor.MIDNIGHT_GOLD -> "Dourado nobre ao âmbar bronze"
                                else -> "Degradê harmonioso de duas tonalidades"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isSelected) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
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

/**
 * Seção de cores clássicas sólidas
 */
@Composable
private fun ClassicColorsSection(
    currentTheme: AppThemeColor,
    isDark: Boolean,
    onSelect: (AppThemeColor) -> Unit
) {
    val classicColors = remember {
        listOf(
            AppThemeColor.EMERALD,
            AppThemeColor.BLUE,
            AppThemeColor.PURPLE,
            AppThemeColor.ORANGE,
            AppThemeColor.RED,
            AppThemeColor.PINK,
            AppThemeColor.GOLD,
            AppThemeColor.TEAL
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Cores sólidas vibrantes com contraste refinado no Material 3:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(classicColors) { col ->
                val isSelected = (currentTheme == col)
                val colorHex = if (isDark) col.primaryDarkHex else col.primaryLightHex
                val color = Color(colorHex)

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(col) }
                        .testTag("classic_color_${col.name.lowercase()}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = color,
                            modifier = Modifier.size(34.dp),
                            border = BorderStroke(2.dp, Color.White.copy(alpha = 0.6f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = col.displayName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

/**
 * Seção com Paleta Interativa e Sliders de Cor Personalizada
 */
@Composable
private fun CustomPaletteSection(
    customColorHex: Long,
    isDark: Boolean,
    onApplyCustomColor: (Long) -> Unit
) {
    val focusManager = LocalFocusManager.current

    // Convert initial hex to HSV
    val initialArgb = (0xFF000000L or (customColorHex and 0xFFFFFFL)).toInt()
    val initialHsv = remember(customColorHex) {
        val hsv = FloatArray(3)
        AndroidColor.colorToHSV(initialArgb, hsv)
        hsv
    }

    var hue by remember { mutableFloatStateOf(initialHsv[0]) } // 0..360
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) } // 0..1
    var value by remember { mutableFloatStateOf(initialHsv[2]) } // 0..1

    // Computed Color
    val currentArgb = remember(hue, saturation, value) {
        AndroidColor.HSVToColor(floatArrayOf(hue, saturation, value))
    }
    val currentColor = remember(currentArgb) { Color(currentArgb) }
    val currentHexStr = remember(currentArgb) {
        String.format("%06X", 0xFFFFFF and currentArgb)
    }

    var hexInputText by remember { mutableStateOf(currentHexStr) }
    var hexInputError by remember { mutableStateOf(false) }

    // Synchronize input text when sliders change
    LaunchedEffect(currentHexStr) {
        if (!hexInputError) {
            hexInputText = currentHexStr
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Crie sua própria cor através da paleta interativa ou digite o código Hex:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Card de Pré-visualização da Cor
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Círculo grande de prévia
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(currentColor)
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Colorize,
                            contentDescription = null,
                            tint = if (value > 0.6f && saturation < 0.4f) Color.Black else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Prévia da Cor",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "#$currentHexStr",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Botão de exemplo usando a cor
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = currentColor,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            text = "Exemplo",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (value > 0.65f && saturation < 0.35f) Color.Black else Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 18 Cores Pré-selecionadas de Inspiração
        Column {
            Text(
                text = "Paleta de Inspiração Rápida",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Primeira linha de 9 swatches
                val row1 = INSPIRATION_PALETTE_HEX.take(9)
                for (hex in row1) {
                    val c = Color(hex)
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(c)
                            .clickable {
                                val argb = (0xFF000000L or (hex and 0xFFFFFFL)).toInt()
                                val hsv = FloatArray(3)
                                AndroidColor.colorToHSV(argb, hsv)
                                hue = hsv[0]
                                saturation = hsv[1]
                                value = hsv[2]
                                hexInputText = String.format("%06X", 0xFFFFFF and argb)
                                hexInputError = false
                            }
                            .border(
                                width = if (currentHexStr.equals(String.format("%06X", 0xFFFFFF and hex.toInt()), ignoreCase = true)) 2.dp else 1.dp,
                                color = if (currentHexStr.equals(String.format("%06X", 0xFFFFFF and hex.toInt()), ignoreCase = true)) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                shape = CircleShape
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Segunda linha de 9 swatches
                val row2 = INSPIRATION_PALETTE_HEX.drop(9).take(9)
                for (hex in row2) {
                    val c = Color(hex)
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(c)
                            .clickable {
                                val argb = (0xFF000000L or (hex and 0xFFFFFFL)).toInt()
                                val hsv = FloatArray(3)
                                AndroidColor.colorToHSV(argb, hsv)
                                hue = hsv[0]
                                saturation = hsv[1]
                                value = hsv[2]
                                hexInputText = String.format("%06X", 0xFFFFFF and argb)
                                hexInputError = false
                            }
                            .border(
                                width = if (currentHexStr.equals(String.format("%06X", 0xFFFFFF and hex.toInt()), ignoreCase = true)) 2.dp else 1.dp,
                                color = if (currentHexStr.equals(String.format("%06X", 0xFFFFFF and hex.toInt()), ignoreCase = true)) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                shape = CircleShape
                            )
                    )
                }
            }
        }

        // Sliders HSV (Matiz, Saturação, Brilho)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Slider 1: Matiz (Hue 0..360)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Matiz (Tom)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${hue.toInt()}°",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Rainbow track background behind slider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
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
                    ),
                    modifier = Modifier.testTag("slider_hue")
                )
            }

            // Slider 2: Saturação (0..1)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Saturação (Intensidade)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
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
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.White,
                                    Color(AndroidColor.HSVToColor(floatArrayOf(hue, 1f, value)))
                                )
                            )
                        )
                )
                Slider(
                    value = saturation,
                    onValueChange = { saturation = it },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = currentColor,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    ),
                    modifier = Modifier.testTag("slider_saturation")
                )
            }

            // Slider 3: Brilho (0.2..1)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Brilho / Luminosidade",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
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
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Black,
                                    Color(AndroidColor.HSVToColor(floatArrayOf(hue, saturation, 1f)))
                                )
                            )
                        )
                )
                Slider(
                    value = value,
                    onValueChange = { value = it },
                    valueRange = 0.2f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = currentColor,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    ),
                    modifier = Modifier.testTag("slider_brightness")
                )
            }
        }

        // Campo para inserir Hex diretamente
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = hexInputText,
                onValueChange = { input ->
                    val clean = input.trim().replace("#", "").uppercase().take(6)
                    hexInputText = clean
                    if (clean.length == 6) {
                        try {
                            val parsedArgb = AndroidColor.parseColor("#$clean")
                            val hsv = FloatArray(3)
                            AndroidColor.colorToHSV(parsedArgb, hsv)
                            hue = hsv[0]
                            saturation = hsv[1]
                            value = hsv[2]
                            hexInputError = false
                        } catch (e: Exception) {
                            hexInputError = true
                        }
                    } else {
                        hexInputError = (clean.length > 0 && clean.length < 6)
                    }
                },
                prefix = {
                    Text(
                        text = "#",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                label = { Text("Código Hexadecimal") },
                isError = hexInputError,
                supportingText = if (hexInputError) {
                    { Text("Código inválido (ex: B76E79)", color = MaterialTheme.colorScheme.error) }
                } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = currentColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_hex_color")
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Indicador de cor do Hex
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(currentColor)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            )
        }

        // Botão Salvar e Aplicar
        Button(
            onClick = {
                val finalHex = 0xFF000000L or (currentArgb.toLong() and 0xFFFFFFL)
                onApplyCustomColor(finalHex)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = currentColor,
                contentColor = if (value > 0.65f && saturation < 0.35f) Color.Black else Color.White
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_apply_custom_color")
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Aplicar Cor Personalizada (#$currentHexStr)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
