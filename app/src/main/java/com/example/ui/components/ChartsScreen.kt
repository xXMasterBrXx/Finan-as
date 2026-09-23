package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.NeutralGray
import com.example.ui.viewmodel.AdvancedAnalytics
import com.example.ui.viewmodel.CategoryComparisonItem
import com.example.ui.viewmodel.CategoryMonthlyAverage
import com.example.ui.viewmodel.CategorySpend
import com.example.ui.viewmodel.DailySpend
import com.example.ui.viewmodel.MonthPeriod
import com.example.ui.viewmodel.MonthlyAnalytics
import com.example.ui.viewmodel.MonthlyTrendPoint
import com.example.ui.viewmodel.PeriodComparisonData
import com.example.util.Formatters
import kotlin.math.abs

/**
 * Dedicated Charts Screen:
 * Houses rich financial visualizations:
 * 1. Categorias (Donut Chart & Distribution)
 * 2. Comparativo entre Períodos (Mês atual vs anterior, variações por categoria)
 * 3. Média de Gastos Mensais por Categoria (Com base nos últimos 6 meses)
 * 4. Tendência Semestral (Comparativo de Entradas vs Gastos mês a mês)
 * 5. Evolução Diária (Gastos e ganhos ao longo dos dias do mês)
 */
@Composable
fun ChartsScreen(
    currentPeriod: MonthPeriod,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onGoToCurrentMonth: () -> Unit,
    monthlyAnalytics: MonthlyAnalytics,
    advancedAnalytics: AdvancedAnalytics,
    hideBalances: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedChartTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Categorias", "Comparativo", "Média Mensal", "Tendência 6M", "Evolução Diária")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("charts_screen_content"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            MonthSelector(
                period = currentPeriod,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onGoToCurrentMonth = onGoToCurrentMonth
            )
        }

        // Sub-tabs navigation for different chart perspectives
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                ScrollableTabRow(
                    selectedTabIndex = selectedChartTab,
                    edgePadding = 12.dp,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        if (selectedChartTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedChartTab]),
                                color = MaterialTheme.colorScheme.primary,
                                height = 3.dp
                            )
                        }
                    },
                    divider = {}
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        val isSelected = selectedChartTab == index
                        Tab(
                            selected = isSelected,
                            onClick = { selectedChartTab = index },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            },
                            modifier = Modifier.testTag("chart_tab_$index")
                        )
                    }
                }
            }
        }

        when (selectedChartTab) {
            0 -> {
                // Category Donut Chart
                item {
                    CategoryDonutCard(
                        categoryExpenses = monthlyAnalytics.categoryExpenses,
                        totalExpense = monthlyAnalytics.totalExpense,
                        hideBalances = hideBalances
                    )
                }
            }
            1 -> {
                // Period Comparison View (Mês Atual vs Mês Anterior)
                item {
                    val comp = advancedAnalytics.comparison
                    if (comp != null) {
                        PeriodComparisonCard(
                            comparison = comp,
                            hideBalances = hideBalances
                        )
                    } else {
                        EmptyChartCard(message = "Dados insuficientes para comparativo de períodos.")
                    }
                }
            }
            2 -> {
                // Category Monthly Averages View
                item {
                    CategoryAveragesCard(
                        categoryAverages = advancedAnalytics.categoryAverages,
                        monthsCount = advancedAnalytics.monthsAnalyzedCount,
                        hideBalances = hideBalances
                    )
                }
            }
            3 -> {
                // 6-Month Income vs Expense Trend Bar & Line
                item {
                    SixMonthTrendCard(
                        trendMonths = advancedAnalytics.trendMonths,
                        hideBalances = hideBalances
                    )
                }
            }
            4 -> {
                // Daily spending line / timeline
                item {
                    DailyEvolutionCard(
                        dailyExpenses = monthlyAnalytics.dailyExpenses,
                        hideBalances = hideBalances
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(130.dp)) // Clearance for bottom navigation bar
        }
    }
}

// -------------------------------------------------------------------------------------
// 1. Category Donut Card
// -------------------------------------------------------------------------------------
@Composable
private fun CategoryDonutCard(
    categoryExpenses: List<CategorySpend>,
    totalExpense: Double,
    hideBalances: Boolean
) {
    var selectedCategory by remember { mutableStateOf<CategorySpend?>(null) }

    LaunchedEffect(categoryExpenses) {
        if (selectedCategory != null && categoryExpenses.none { it.category.name == selectedCategory?.category?.name }) {
            selectedCategory = null
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("category_donut_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Distribuição por Categoria",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${categoryExpenses.size} categorias ativas neste mês",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (totalExpense <= 0.0 || categoryExpenses.isEmpty()) {
                EmptyChartCard(message = "Nenhum gasto registrado neste mês.")
            } else {
                // Donut Chart Canvas & Center Info
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DonutChartDrawing(
                        categoryExpenses = categoryExpenses,
                        selectedCategory = selectedCategory
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        if (selectedCategory != null) {
                            Text(
                                text = selectedCategory!!.category.name,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = selectedCategory!!.category.color,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (hideBalances) "••••••" else Formatters.formatCurrency(selectedCategory!!.total),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${String.format("%.1f", selectedCategory!!.percentage * 100)}% do total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "Total Gastos",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (hideBalances) "••••••" else Formatters.formatCurrency(totalExpense),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Toque numa fatia",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = NeutralGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Breakdown list
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    categoryExpenses.forEach { item ->
                        val isSelected = selectedCategory == item
                        Surface(
                            onClick = {
                                selectedCategory = if (selectedCategory == item) null else item
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) item.category.color.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(item.category.color.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = item.category.icon,
                                            contentDescription = null,
                                            tint = item.category.color,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = item.category.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${item.count} ${if (item.count == 1) "saída" else "saídas"}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (hideBalances) "••••••" else Formatters.formatCurrency(item.total),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${String.format("%.1f", item.percentage * 100)}%",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = item.category.color
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChartDrawing(
    categoryExpenses: List<CategorySpend>,
    selectedCategory: CategorySpend?
) {
    val transitionProgress = remember { Animatable(0f) }

    LaunchedEffect(categoryExpenses) {
        transitionProgress.snapTo(0f)
        transitionProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    Canvas(modifier = Modifier.size(200.dp)) {
        val strokeWidth = 24.dp.toPx()
        val selectedStrokeWidth = 32.dp.toPx()
        val radius = (size.minDimension - selectedStrokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)

        var currentAngle = -90f

        categoryExpenses.forEach { item ->
            val sweep = (item.percentage * 360f) * transitionProgress.value
            val isSelected = selectedCategory == item
            val effectiveStroke = if (isSelected) selectedStrokeWidth else strokeWidth

            drawArc(
                color = if (selectedCategory == null || isSelected) item.category.color else item.category.color.copy(alpha = 0.35f),
                startAngle = currentAngle,
                sweepAngle = (sweep - 2f).coerceAtLeast(1f),
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = effectiveStroke, cap = StrokeCap.Round)
            )

            currentAngle += sweep
        }
    }
}

// -------------------------------------------------------------------------------------
// 2. Period Comparison Card
// -------------------------------------------------------------------------------------
@Composable
private fun PeriodComparisonCard(
    comparison: PeriodComparisonData,
    hideBalances: Boolean
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("period_comparison_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Comparativo entre Períodos",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${comparison.currentPeriodLabel} vs ${comparison.previousPeriodLabel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Comparative Metric Tiles (Gastos e Entradas)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Gastos comparison box
                val isExpHigher = comparison.currentExpense > comparison.previousExpense
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Total de Gastos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (hideBalances) "••••••" else Formatters.formatCurrency(comparison.currentExpense),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = ExpenseRed
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isExpHigher) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = if (isExpHigher) ExpenseRed else IncomeGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val sign = if (comparison.expenseDiffPercent >= 0) "+" else ""
                            Text(
                                text = "$sign${String.format("%.1f", comparison.expenseDiffPercent)}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isExpHigher) ExpenseRed else IncomeGreen
                            )
                        }
                        Text(
                            text = "Ant: ${if (hideBalances) "••••" else Formatters.formatCurrency(comparison.previousExpense)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = NeutralGray
                        )
                    }
                }

                // Entradas comparison box
                val isIncHigher = comparison.currentIncome >= comparison.previousIncome
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Total de Entradas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (hideBalances) "••••••" else Formatters.formatCurrency(comparison.currentIncome),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = IncomeGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isIncHigher) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = if (isIncHigher) IncomeGreen else ExpenseRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val sign = if (comparison.incomeDiffPercent >= 0) "+" else ""
                            Text(
                                text = "$sign${String.format("%.1f", comparison.incomeDiffPercent)}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isIncHigher) IncomeGreen else ExpenseRed
                            )
                        }
                        Text(
                            text = "Ant: ${if (hideBalances) "••••" else Formatters.formatCurrency(comparison.previousIncome)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = NeutralGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Variação de Gastos por Categoria",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (comparison.categoryDiffs.isEmpty()) {
                Text(
                    text = "Nenhuma movimentação registrada nos dois períodos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    comparison.categoryDiffs.forEach { catDiff ->
                        CategoryDiffRow(
                            item = catDiff,
                            hideBalances = hideBalances
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryDiffRow(
    item: CategoryComparisonItem,
    hideBalances: Boolean
) {
    val isIncreased = item.diffAmount > 0
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(item.category.color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.category.icon,
                        contentDescription = null,
                        tint = item.category.color,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = item.category.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ant: ${if (hideBalances) "••••" else Formatters.formatCurrency(item.previousAmount)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (hideBalances) "••••••" else Formatters.formatCurrency(item.currentAmount),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isIncreased) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (isIncreased) ExpenseRed else IncomeGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    val sign = if (item.diffAmount >= 0) "+" else ""
                    Text(
                        text = "$sign${String.format("%.0f", item.diffPercent)}% (${if (hideBalances) "••" else Formatters.formatCurrency(item.diffAmount)})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        ),
                        color = if (isIncreased) ExpenseRed else IncomeGreen
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// 3. Category Monthly Averages Card
// -------------------------------------------------------------------------------------
@Composable
private fun CategoryAveragesCard(
    categoryAverages: List<CategoryMonthlyAverage>,
    monthsCount: Int,
    hideBalances: Boolean
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("category_averages_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Média de Gastos Mensais",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Base calculada nos últimos $monthsCount meses",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (categoryAverages.isEmpty() || categoryAverages.all { it.totalAmount <= 0 }) {
                EmptyChartCard(message = "Sem histórico suficiente de gastos para calcular médias.")
            } else {
                val maxAvg = categoryAverages.maxOfOrNull { it.averageMonthlyAmount } ?: 1.0
                val effectiveMax = if (maxAvg <= 0.0) 1.0 else maxAvg

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    categoryAverages.filter { it.totalAmount > 0 }.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(item.category.color.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = item.category.icon,
                                                contentDescription = null,
                                                tint = item.category.color,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = item.category.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Total em $monthsCount meses: ${if (hideBalances) "••••" else Formatters.formatCurrency(item.totalAmount)}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${if (hideBalances) "••••" else Formatters.formatCurrency(item.averageMonthlyAmount)}/mês",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        val isAboveAvg = item.currentMonthAmount > item.averageMonthlyAmount
                                        val diffSign = if (item.diffFromAveragePercent >= 0) "+" else ""
                                        Text(
                                            text = "Este mês: $diffSign${String.format("%.0f", item.diffFromAveragePercent)}%",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            color = if (isAboveAvg) ExpenseRed else IncomeGreen
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Relative average bar
                                val fraction = (item.averageMonthlyAmount / effectiveMax).toFloat().coerceIn(0.02f, 1f)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fraction)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(item.category.color)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// 4. Six Months Trend Card
// -------------------------------------------------------------------------------------
@Composable
private fun SixMonthTrendCard(
    trendMonths: List<MonthlyTrendPoint>,
    hideBalances: Boolean
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("six_month_trend_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Tendência Semestral",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Entradas vs Gastos nos últimos 6 meses",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Legends
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(IncomeGreen)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Entradas", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = IncomeGreen)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(ExpenseRed)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Gastos", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = ExpenseRed)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Grouped Bar Chart Canvas
            val maxVal = trendMonths.maxOfOrNull { maxOf(it.totalExpense, it.totalIncome) } ?: 1.0
            val effectiveMax = if (maxVal <= 0.0) 1.0 else maxVal

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height - 24.dp.toPx() // Room for labels
                    val slotCount = trendMonths.size
                    if (slotCount == 0) return@Canvas

                    val slotWidth = width / slotCount
                    val barWidth = 10.dp.toPx()
                    val barSpacing = 3.dp.toPx()

                    trendMonths.forEachIndexed { i, pt ->
                        val slotCenter = i * slotWidth + (slotWidth / 2)

                        val incBarHeight = ((pt.totalIncome / effectiveMax) * height).toFloat()
                        val expBarHeight = ((pt.totalExpense / effectiveMax) * height).toFloat()

                        // Income Bar (Left of pair)
                        val incX = slotCenter - barWidth - (barSpacing / 2)
                        val incY = height - incBarHeight
                        if (incBarHeight > 0) {
                            drawRoundRect(
                                color = IncomeGreen,
                                topLeft = Offset(incX, incY),
                                size = Size(barWidth, incBarHeight),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                        }

                        // Expense Bar (Right of pair)
                        val expX = slotCenter + (barSpacing / 2)
                        val expY = height - expBarHeight
                        if (expBarHeight > 0) {
                            drawRoundRect(
                                color = ExpenseRed,
                                topLeft = Offset(expX, expY),
                                size = Size(barWidth, expBarHeight),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                        }

                        // Baseline
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.2f),
                            start = Offset(0f, height),
                            end = Offset(width, height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }

                // Month labels below baseline
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    trendMonths.forEach { pt ->
                        Text(
                            text = pt.label,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Month by Month Net Results table
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                trendMonths.reversed().forEach { pt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = pt.label,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "+${if (hideBalances) "••••" else Formatters.formatCurrency(pt.totalIncome)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = IncomeGreen
                            )
                            Text(
                                text = "-${if (hideBalances) "••••" else Formatters.formatCurrency(pt.totalExpense)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ExpenseRed
                            )
                            val isPos = pt.netBalance >= 0
                            Text(
                                text = "= ${if (hideBalances) "••••" else Formatters.formatCurrency(pt.netBalance)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isPos) IncomeGreen else ExpenseRed
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// 5. Daily Evolution Card
// -------------------------------------------------------------------------------------
@Composable
private fun DailyEvolutionCard(
    dailyExpenses: List<DailySpend>,
    hideBalances: Boolean
) {
    val maxExpense = dailyExpenses.maxOfOrNull { it.amount } ?: 0.0
    val maxIncome = dailyExpenses.maxOfOrNull { it.incomeAmount } ?: 0.0
    val highestPeak = maxOf(maxExpense, maxIncome)
    val effectiveMax = if (highestPeak <= 0.0) 1.0 else highestPeak

    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }
    val selectedDay = selectedDayIndex?.let { dailyExpenses.getOrNull(it) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_evolution_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoGraph,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Evolução Diária do Mês",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Flutuação diária de entradas e despesas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Peak indicator & legends
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(IncomeGreen))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Ganhos", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = IncomeGreen)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ExpenseRed))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Gastos", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = ExpenseRed)
                    }
                }

                Text(
                    text = "Maior pico: ${if (hideBalances) "••••••" else Formatters.formatCurrency(effectiveMax)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (selectedDay != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dia ${selectedDay.dayOfMonth}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (selectedDay.incomeAmount > 0) {
                                Text(
                                    text = "+${if (hideBalances) "••••" else Formatters.formatCurrency(selectedDay.incomeAmount)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = IncomeGreen
                                )
                            }
                            if (selectedDay.amount > 0) {
                                Text(
                                    text = "-${if (hideBalances) "••••" else Formatters.formatCurrency(selectedDay.amount)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ExpenseRed
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Timeline multi-line chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .pointerInput(dailyExpenses) {
                            detectTapGestures { offset ->
                                val count = dailyExpenses.size
                                if (count > 0) {
                                    val slotW = size.width / count
                                    val clickedIdx = (offset.x / slotW).toInt().coerceIn(0, count - 1)
                                    selectedDayIndex = if (selectedDayIndex == clickedIdx) null else clickedIdx
                                }
                            }
                        }
                ) {
                    val pointCount = dailyExpenses.size
                    if (pointCount < 2) return@Canvas

                    val width = size.width
                    val baselineY = size.height - 18.dp.toPx()
                    val chartHeight = baselineY - 10.dp.toPx()
                    val slotWidth = width / (pointCount - 1)

                    val incomePoints = dailyExpenses.mapIndexed { index, item ->
                        val x = index * slotWidth
                        val ratio = (item.incomeAmount / effectiveMax).toFloat().coerceIn(0f, 1f)
                        val y = baselineY - (ratio * chartHeight)
                        Offset(x, y)
                    }

                    val expensePoints = dailyExpenses.mapIndexed { index, item ->
                        val x = index * slotWidth
                        val ratio = (item.amount / effectiveMax).toFloat().coerceIn(0f, 1f)
                        val y = baselineY - (ratio * chartHeight)
                        Offset(x, y)
                    }

                    // Baseline
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.25f),
                        start = Offset(0f, baselineY),
                        end = Offset(width, baselineY),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Draw Income Path
                    val incomePath = Path().apply {
                        moveTo(incomePoints.first().x, incomePoints.first().y)
                        incomePoints.forEach { pt -> lineTo(pt.x, pt.y) }
                    }
                    drawPath(path = incomePath, color = IncomeGreen, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

                    // Draw Expense Path
                    val expensePath = Path().apply {
                        moveTo(expensePoints.first().x, expensePoints.first().y)
                        expensePoints.forEach { pt -> lineTo(pt.x, pt.y) }
                    }
                    drawPath(path = expensePath, color = ExpenseRed, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

                    // Draw dots on peak points
                    dailyExpenses.forEachIndexed { index, daily ->
                        if (daily.amount > 0) {
                            val pt = expensePoints[index]
                            drawCircle(color = Color.White, radius = 3.5.dp.toPx(), center = pt)
                            drawCircle(color = ExpenseRed, radius = 2.dp.toPx(), center = pt)
                        }
                        if (daily.incomeAmount > 0) {
                            val pt = incomePoints[index]
                            drawCircle(color = Color.White, radius = 3.5.dp.toPx(), center = pt)
                            drawCircle(color = IncomeGreen, radius = 2.dp.toPx(), center = pt)
                        }
                    }

                    // Highlight selected day
                    selectedDayIndex?.let { idx ->
                        if (idx in dailyExpenses.indices) {
                            val x = idx * slotWidth
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.6f),
                                start = Offset(x, 0f),
                                end = Offset(x, baselineY),
                                strokeWidth = 1.5.dp.toPx()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Dia 1", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = NeutralGray)
                Text(text = "Dia 10", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = NeutralGray)
                Text(text = "Dia 20", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = NeutralGray)
                Text(text = "Fim do Mês", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = NeutralGray)
            }
        }
    }
}

@Composable
private fun EmptyChartCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
