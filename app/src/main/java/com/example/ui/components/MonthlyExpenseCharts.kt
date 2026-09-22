package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
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
import com.example.ui.viewmodel.CategorySpend
import com.example.ui.viewmodel.DailySpend
import com.example.ui.viewmodel.MonthlyAnalytics
import com.example.util.Formatters
import kotlin.math.atan2

@Composable
fun MonthlyExpenseCharts(
    analytics: MonthlyAnalytics,
    hideBalances: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedChartMode by remember { mutableStateOf(0) } // 0: Categorias, 1: Evolução Diária
    var selectedCategory by remember { mutableStateOf<CategorySpend?>(null) }

    // Reset selected category if not present in new list
    LaunchedEffect(analytics.categoryExpenses) {
        if (selectedCategory != null &&
            analytics.categoryExpenses.none { it.category.name == selectedCategory?.category?.name }
        ) {
            selectedCategory = null
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_charts_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with Chart Mode Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gráficos de Gastos",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Compact Pill Selector
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        PillButton(
                            selected = selectedChartMode == 0,
                            icon = Icons.Default.PieChart,
                            label = "Categorias",
                            onClick = { selectedChartMode = 0 },
                            tag = "chart_mode_pie"
                        )
                        PillButton(
                            selected = selectedChartMode == 1,
                            icon = Icons.Default.BarChart,
                            label = "Dias",
                            onClick = { selectedChartMode = 1 },
                            tag = "chart_mode_bar"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (analytics.totalExpense <= 0.0 || analytics.categoryExpenses.isEmpty()) {
                // Empty state for the month
                EmptyChartState()
            } else {
                when (selectedChartMode) {
                    0 -> {
                        // Category Donut Chart + Category Breakdown
                        CategoryChartContent(
                            categoryExpenses = analytics.categoryExpenses,
                            totalExpense = analytics.totalExpense,
                            selectedCategory = selectedCategory,
                            hideBalances = hideBalances,
                            onSelectCategory = { cat ->
                                selectedCategory = if (selectedCategory == cat) null else cat
                            }
                        )
                    }
                    1 -> {
                        // Daily spending bar chart
                        DailyTimelineChart(
                            dailyExpenses = analytics.dailyExpenses,
                            hideBalances = hideBalances
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PillButton(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        modifier = Modifier.testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CategoryChartContent(
    categoryExpenses: List<CategorySpend>,
    totalExpense: Double,
    selectedCategory: CategorySpend?,
    hideBalances: Boolean,
    onSelectCategory: (CategorySpend) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Donut Canvas
        Box(
            modifier = Modifier
                .size(220.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            DonutChartCanvas(
                categoryExpenses = categoryExpenses,
                selectedCategory = selectedCategory
            )

            // Center Info Overlay
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                if (selectedCategory != null) {
                    Text(
                        text = selectedCategory.category.name,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = selectedCategory.category.color,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (hideBalances) "••••••" else Formatters.formatCurrency(selectedCategory.total),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${String.format("%.1f", selectedCategory.percentage * 100)}% dos gastos",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Total Saídas",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (hideBalances) "R$ ••••••" else Formatters.formatCurrency(totalExpense),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${categoryExpenses.size} categorias",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal Category Chips for quick interactive filtering
        Text(
            text = "Distribuição por Categoria",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // List of categories with progress bar & values
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            categoryExpenses.forEach { item ->
                val isSelected = selectedCategory == item
                CategorySpendItem(
                    item = item,
                    hideBalances = hideBalances,
                    isSelected = isSelected,
                    onClick = { onSelectCategory(item) }
                )
            }
        }
    }
}

@Composable
private fun DonutChartCanvas(
    categoryExpenses: List<CategorySpend>,
    selectedCategory: CategorySpend?
) {
    val transitionProgress = remember { Animatable(0f) }

    LaunchedEffect(categoryExpenses) {
        transitionProgress.snapTo(0f)
        transitionProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
        )
    }

    Canvas(
        modifier = Modifier
            .size(200.dp)
            .testTag("donut_chart_canvas")
    ) {
        val strokeWidth = 26.dp.toPx()
        val selectedStrokeWidth = 34.dp.toPx()
        val radius = (size.minDimension - selectedStrokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)

        var currentAngle = -90f

        categoryExpenses.forEach { item ->
            val sweep = (item.percentage * 360f) * transitionProgress.value
            val isSelected = selectedCategory == item
            val effectiveStroke = if (isSelected) selectedStrokeWidth else strokeWidth

            // Draw slice arc
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

@Composable
private fun CategorySpendItem(
    item: CategorySpend,
    hideBalances: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) item.category.color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cat_item_${item.category.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
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
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(item.category.color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.category.icon,
                            contentDescription = item.category.name,
                            modifier = Modifier.size(17.dp),
                            tint = item.category.color
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = item.category.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${item.count} ${if (item.count == 1) "saída" else "saídas"}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = if (hideBalances) "••••••" else Formatters.formatCurrency(item.total),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${String.format("%.1f", item.percentage * 100)}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = item.category.color
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Percentage Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(item.percentage.coerceIn(0.01f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(item.category.color)
                )
            }
        }
    }
}

@Composable
private fun DailyTimelineChart(
    dailyExpenses: List<DailySpend>,
    hideBalances: Boolean
) {
    val maxExpense = dailyExpenses.maxOfOrNull { it.amount } ?: 0.0
    val maxIncome = dailyExpenses.maxOfOrNull { it.incomeAmount } ?: 0.0
    val highestPeak = maxOf(maxExpense, maxIncome)
    val effectiveMax = if (highestPeak <= 0.0) 1.0 else highestPeak

    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }
    val selectedDay = selectedDayIndex?.let { dailyExpenses.getOrNull(it) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_timeline_chart")
    ) {
        // Legend & Peak Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Income Legend (Green)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(IncomeGreen)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Ganhos",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = IncomeGreen
                    )
                }

                // Expense Legend (Red)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ExpenseRed)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Gastos",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ExpenseRed
                    )
                }
            }

            Text(
                text = "Pico: ${if (hideBalances) "••••••" else Formatters.formatCurrency(effectiveMax)}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
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

        // Custom Multi-Line Chart (Green: Income, Red: Expense)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
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

                val availableWidth = size.width
                val chartHeight = size.height - 24f
                val baselineY = size.height - 12f
                val slotWidth = availableWidth / (pointCount - 1).coerceAtLeast(1)

                // Draw subtle gridlines
                val gridSteps = 3
                for (i in 0..gridSteps) {
                    val y = 12f + (chartHeight / gridSteps) * i
                    drawLine(
                        color = Color.White.copy(alpha = 0.07f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                }

                // Compute points
                val expensePoints = dailyExpenses.mapIndexed { index, daily ->
                    val x = index * slotWidth
                    val fraction = (daily.amount / effectiveMax).toFloat().coerceIn(0f, 1f)
                    val y = baselineY - (fraction * (chartHeight - 12f))
                    Offset(x, y)
                }

                val incomePoints = dailyExpenses.mapIndexed { index, daily ->
                    val x = index * slotWidth
                    val fraction = (daily.incomeAmount / effectiveMax).toFloat().coerceIn(0f, 1f)
                    val y = baselineY - (fraction * (chartHeight - 12f))
                    Offset(x, y)
                }

                // Helper to create smooth path
                fun buildPath(points: List<Offset>): Path {
                    val path = Path()
                    if (points.isEmpty()) return path
                    path.moveTo(points.first().x, points.first().y)
                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val cx = (p0.x + p1.x) / 2f
                        path.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    }
                    return path
                }

                val expensePath = buildPath(expensePoints)
                val incomePath = buildPath(incomePoints)

                // Fill area under Income Line (Green Gradient)
                val incomeFillPath = Path().apply {
                    addPath(incomePath)
                    lineTo(size.width, baselineY)
                    lineTo(0f, baselineY)
                    close()
                }
                drawPath(
                    path = incomeFillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(IncomeGreen.copy(alpha = 0.18f), IncomeGreen.copy(alpha = 0.0f)),
                        startY = 0f,
                        endY = baselineY
                    ),
                    style = Fill
                )

                // Fill area under Expense Line (Red Gradient)
                val expenseFillPath = Path().apply {
                    addPath(expensePath)
                    lineTo(size.width, baselineY)
                    lineTo(0f, baselineY)
                    close()
                }
                drawPath(
                    path = expenseFillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(ExpenseRed.copy(alpha = 0.15f), ExpenseRed.copy(alpha = 0.0f)),
                        startY = 0f,
                        endY = baselineY
                    ),
                    style = Fill
                )

                // Draw Income Line (Green)
                drawPath(
                    path = incomePath,
                    color = IncomeGreen,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Draw Expense Line (Red)
                drawPath(
                    path = expensePath,
                    color = ExpenseRed,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Draw Data Dots for Income
                dailyExpenses.forEachIndexed { index, daily ->
                    if (daily.incomeAmount > 0) {
                        val pt = incomePoints[index]
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = IncomeGreen,
                            radius = 2.5.dp.toPx(),
                            center = pt
                        )
                    }
                }

                // Draw Data Dots for Expense
                dailyExpenses.forEachIndexed { index, daily ->
                    if (daily.amount > 0) {
                        val pt = expensePoints[index]
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = ExpenseRed,
                            radius = 2.5.dp.toPx(),
                            center = pt
                        )
                    }
                }

                // Highlight selected day indicator
                selectedDayIndex?.let { idx ->
                    if (idx in dailyExpenses.indices) {
                        val x = idx * slotWidth
                        drawLine(
                            color = Color.White.copy(alpha = 0.4f),
                            start = Offset(x, 0f),
                            end = Offset(x, baselineY),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Indicator Labels: Dia 1, Dia 10, Dia 20, Fim do Mês
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

@Composable
private fun EmptyChartState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Sem gastos registrados neste mês",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Toque no botão '+' abaixo para cadastrar saídas e visualizar gráficos automáticos.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}
