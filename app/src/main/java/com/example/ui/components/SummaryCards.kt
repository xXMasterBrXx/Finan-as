package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.viewmodel.MonthlyAnalytics
import com.example.util.Formatters

@Composable
fun SummaryCards(
    analytics: MonthlyAnalytics,
    hideBalances: Boolean = false,
    monthlyBudgetLimit: Double = 0.0,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("summary_cards_container"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main Hero Balance Card
        val isPositiveBalance = analytics.balance >= 0
        val gradientColors = if (isPositiveBalance) {
            listOf(Color(0xFF0F3E2E), Color(0xFF0B291E))
        } else {
            listOf(Color(0xFF4A1A1E), Color(0xFF2A0F12))
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("net_balance_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(gradientColors))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Balanço do Mês",
                            style = MaterialTheme.typography.labelLarge.copy(
                                letterSpacing = 0.5.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFFCBD5E1)
                        )

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isPositiveBalance) IncomeGreen.copy(alpha = 0.2f) else ExpenseRed.copy(alpha = 0.25f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = if (isPositiveBalance) IncomeGreen else ExpenseRed
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                val pct = (analytics.savingsRate * 100).toInt()
                                Text(
                                    text = if (isPositiveBalance) "+$pct% poupado" else "$pct% saldo",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = if (isPositiveBalance) IncomeGreen else ExpenseRed
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    AnimatedContent(
                        targetState = analytics.balance,
                        label = "balance_amount"
                    ) { balance ->
                        Text(
                            text = if (hideBalances) "R$ ••••••" else Formatters.formatCurrency(balance),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isPositiveBalance) "Finanças no azul! Economia ativa." else "Gastos superando as entradas neste mês.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // Sub-cards for Entradas and Saídas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Entradas Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("income_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(IncomeGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Entradas",
                            tint = IncomeGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Entradas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (hideBalances) "••••••" else Formatters.formatCurrency(analytics.totalIncome),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = IncomeGreen
                        )
                    }
                }
            }

            // Saídas Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("expense_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ExpenseRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Saídas",
                            tint = ExpenseRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Saídas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (hideBalances) "••••••" else Formatters.formatCurrency(analytics.totalExpense),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = ExpenseRed
                        )
                    }
                }
            }
        }

        // Optional: Budget Limit Tracker Card if user configured a monthlyBudgetLimit
        if (monthlyBudgetLimit > 0) {
            val progress = (analytics.totalExpense / monthlyBudgetLimit).coerceIn(0.0, 1.0).toFloat()
            val isOverBudget = analytics.totalExpense > monthlyBudgetLimit
            val remaining = (monthlyBudgetLimit - analytics.totalExpense).coerceAtLeast(0.0)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("budget_tracker_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isOverBudget) Icons.Default.WarningAmber else Icons.Default.Savings,
                                contentDescription = null,
                                tint = if (isOverBudget) ExpenseRed else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Meta de Gastos",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = if (isOverBudget) "Limite estourado!" else if (hideBalances) "Dentro da meta" else "Resta ${Formatters.formatCurrency(remaining)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (isOverBudget) ExpenseRed else MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (isOverBudget) ExpenseRed else if (progress > 0.8f) Color(0xFFF59E0B) else IncomeGreen,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (hideBalances) "Gasto: ••••••" else "Gasto: ${Formatters.formatCurrency(analytics.totalExpense)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Teto: ${Formatters.formatCurrency(monthlyBudgetLimit)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
