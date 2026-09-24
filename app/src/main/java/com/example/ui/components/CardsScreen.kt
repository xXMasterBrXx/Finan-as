package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.local.CreditCardEntity
import com.example.data.local.TransactionEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.viewmodel.CardWithExpenses
import com.example.ui.viewmodel.MonthPeriod
import com.example.util.Formatters
import java.util.Calendar

@Composable
fun CardsScreen(
    cardsWithExpenses: List<CardWithExpenses>,
    hideBalances: Boolean,
    currentPeriod: MonthPeriod? = null,
    onAddNewCard: () -> Unit,
    onEditCard: (CreditCardEntity) -> Unit,
    onDeleteCard: (CreditCardEntity) -> Unit,
    onAddExpenseForCard: (CreditCardEntity) -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onToggleInvoicePaid: (cardId: Long, year: Int, month: Int) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var selectedCardId by remember(cardsWithExpenses) {
        mutableStateOf<Long?>(cardsWithExpenses.firstOrNull()?.card?.id)
    }

    var cardToDelete by remember { mutableStateOf<CreditCardEntity?>(null) }

    val activeCardItem = cardsWithExpenses.find { it.card.id == selectedCardId }
        ?: cardsWithExpenses.firstOrNull()

    val periodCal = remember(currentPeriod) {
        Calendar.getInstance().apply {
            if (currentPeriod != null) {
                set(Calendar.YEAR, currentPeriod.year)
                set(Calendar.MONTH, currentPeriod.month)
            }
        }
    }
    val periodMonthLabel = remember(periodCal) {
        Formatters.formatMonthYear(periodCal)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("cards_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header with Title and "Add Card" button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Meus Cartões",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Gerencie seus cartões e acompanhe as faturas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onAddNewCard,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("add_card_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Novo", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        // Empty state if no cards registered
        if (cardsWithExpenses.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Nenhum cartão cadastrado",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Cadastre seus cartões de crédito para registrar e acompanhar despesas por fatura.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onAddNewCard,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cadastrar Primeiro Cartão")
                        }
                    }
                }
            }
        } else {
            // Horizontal Carousel of Credit Cards
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(cardsWithExpenses, key = { it.card.id }) { item ->
                        val isSelected = (item.card.id == activeCardItem?.card?.id)
                        CreditCardVisualItem(
                            cardWithExpenses = item,
                            isSelected = isSelected,
                            hideBalances = hideBalances,
                            onClick = { selectedCardId = item.card.id }
                        )
                    }
                }
            }

            // Detail Panel for Selected Card
            if (activeCardItem != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("selected_card_details"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Header Row with Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(parseHexColor(activeCardItem.card.colorHex))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = activeCardItem.card.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = { onEditCard(activeCardItem.card) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar Cartão",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { cardToDelete = activeCardItem.card },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Excluir Cartão",
                                            tint = ExpenseRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 2. Visão da Fatura Atual (Aberta / Paga)
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = if (activeCardItem.isCurrentOpenInvoicePaid) {
                                    IncomeGreen.copy(alpha = 0.08f)
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                },
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (activeCardItem.isCurrentOpenInvoicePaid) {
                                        IncomeGreen.copy(alpha = 0.35f)
                                    } else {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                    }
                                )
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
                                                imageVector = if (activeCardItem.isCurrentOpenInvoicePaid) Icons.Default.CheckCircle else Icons.Default.CreditCard,
                                                contentDescription = null,
                                                tint = if (activeCardItem.isCurrentOpenInvoicePaid) IncomeGreen else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (activeCardItem.isCurrentOpenInvoicePaid) "FATURA ATUAL (PAGA)" else "FATURA ATUAL (ABERTA)",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 0.8.sp
                                                ),
                                                color = if (activeCardItem.isCurrentOpenInvoicePaid) IncomeGreen else MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (activeCardItem.isCurrentOpenInvoicePaid) IncomeGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = if (activeCardItem.isCurrentOpenInvoicePaid) "Paga" else "Em Aberto",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                ),
                                                color = if (activeCardItem.isCurrentOpenInvoicePaid) IncomeGreen else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = if (hideBalances) "R$ ••••••" else Formatters.formatCurrency(activeCardItem.currentOpenInvoiceExpense),
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (activeCardItem.isCurrentOpenInvoicePaid) IncomeGreen else MaterialTheme.colorScheme.onSurface
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Fecha em: ${activeCardItem.currentInvoiceClosingDateText.ifBlank { "Dia ${activeCardItem.card.closingDay}" }}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Vence em: ${activeCardItem.currentInvoiceDueDateText.ifBlank { "Dia ${activeCardItem.card.dueDay}" }}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // 3. Limite Utilizado Atual (Considera data de hoje e todas as compras abertas)
                            if (activeCardItem.card.limitAmount > 0) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Limite utilizado atual",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (hideBalances) "Disponível: ••••••" else "Disponível: ${Formatters.formatCurrency(activeCardItem.remainingLimit)}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (activeCardItem.remainingLimit > 0) IncomeGreen else ExpenseRed
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    LinearProgressIndicator(
                                        progress = { activeCardItem.limitProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = if (activeCardItem.limitProgress > 0.9f) ExpenseRed else if (activeCardItem.limitProgress > 0.7f) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        strokeCap = StrokeCap.Round
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${(activeCardItem.limitProgress * 100).toInt()}% utilizado (${if (hideBalances) "R$ •••" else Formatters.formatCurrency(activeCardItem.currentUsedLimit)})",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Limite: ${Formatters.formatCurrency(activeCardItem.card.limitAmount)}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // 4. Fatura do Período Filtrado com Opção de Marcar como Paga (Libera limite)
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = if (activeCardItem.isInvoicePaidThisMonth) {
                                    IncomeGreen.copy(alpha = 0.08f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                },
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (activeCardItem.isInvoicePaidThisMonth) {
                                        IncomeGreen.copy(alpha = 0.35f)
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    }
                                )
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
                                        Column {
                                            Text(
                                                text = "Fatura de $periodMonthLabel",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (hideBalances) "R$ ••••••" else Formatters.formatCurrency(activeCardItem.totalExpenseThisMonth),
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (activeCardItem.isInvoicePaidThisMonth) IncomeGreen else ExpenseRed
                                                )
                                            )
                                        }

                                        // Badge de Status
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (activeCardItem.isInvoicePaidThisMonth) IncomeGreen.copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (activeCardItem.isInvoicePaidThisMonth) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                                    contentDescription = null,
                                                    tint = if (activeCardItem.isInvoicePaidThisMonth) IncomeGreen else Color(0xFFD97706),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (activeCardItem.isInvoicePaidThisMonth) "Fatura Paga" else "Fatura Aberta",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp
                                                    ),
                                                    color = if (activeCardItem.isInvoicePaidThisMonth) IncomeGreen else Color(0xFFD97706)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Botão de Alternar Fatura Paga / Liberar Limite
                                    if (activeCardItem.isInvoicePaidThisMonth) {
                                        OutlinedButton(
                                            onClick = {
                                                if (currentPeriod != null) {
                                                    onToggleInvoicePaid(activeCardItem.card.id, currentPeriod.year, currentPeriod.month)
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("toggle_invoice_paid_btn"),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = IncomeGreen
                                            ),
                                            border = BorderStroke(1.dp, IncomeGreen.copy(alpha = 0.6f))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Fatura Paga (Limite liberado) • Reabrir?",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                            )
                                        }
                                    } else {
                                        FilledTonalButton(
                                            onClick = {
                                                if (currentPeriod != null) {
                                                    onToggleInvoicePaid(activeCardItem.card.id, currentPeriod.year, currentPeriod.month)
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("toggle_invoice_paid_btn"),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Marcar Fatura como Paga (Liberar Limite)",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action Button: + Lançar gasto neste cartão
                            Button(
                                onClick = { onAddExpenseForCard(activeCardItem.card) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("add_expense_to_card_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payment,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Adicionar Gasto neste Cartão",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Section: Monthly Expenses for this card
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gastos da Fatura (${activeCardItem.monthlyTransactions.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (activeCardItem.monthlyTransactions.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ) {
                            Text(
                                text = "Nenhum gasto registrado neste cartão para o mês selecionado.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(activeCardItem.monthlyTransactions, key = { it.id }) { tx ->
                        TransactionItem(
                            transaction = tx,
                            onEdit = onEditTransaction,
                            onDelete = onDeleteTransaction,
                            hideBalances = hideBalances,
                            cardName = activeCardItem.card.name,
                            card = activeCardItem.card
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(140.dp))
        }
    }

    // Delete Card Confirmation Dialog
    cardToDelete?.let { card ->
        AlertDialog(
            onDismissRequest = { cardToDelete = null },
            title = { Text("Excluir Cartão") },
            text = {
                Text("Deseja realmente remover o cartão \"${card.name}\"? Os lançamentos associados a ele não serão apagados, apenas desvinculados do cartão.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCard(card)
                        cardToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Excluir", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { cardToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CreditCardVisualItem(
    cardWithExpenses: CardWithExpenses,
    isSelected: Boolean,
    hideBalances: Boolean,
    onClick: () -> Unit
) {
    val cardColor = parseHexColor(cardWithExpenses.card.colorHex)
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            cardColor,
            cardColor.copy(alpha = 0.8f),
            cardColor.copy(alpha = 0.95f)
        )
    )

    Card(
        modifier = Modifier
            .width(260.dp)
            .height(150.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("credit_card_chip_${cardWithExpenses.card.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp),
        border = if (isSelected) BorderStroke(2.dp, Color.White.copy(alpha = 0.8f)) else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Card Top: Name and Chip Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = cardWithExpenses.card.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 1
                    )

                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Digits
                Text(
                    text = if (cardWithExpenses.card.lastFourDigits.isNotBlank()) {
                        "•••• •••• •••• ${cardWithExpenses.card.lastFourDigits}"
                    } else {
                        "•••• •••• •••• ••••"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.9f),
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                // Bottom: Current Month Invoice
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Fatura atual",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 10.sp
                            )
                        )
                        Text(
                            text = if (hideBalances) "R$ ••••••" else Formatters.formatCurrency(cardWithExpenses.currentOpenInvoiceExpense),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Text(
                        text = if (cardWithExpenses.currentInvoiceDueDateText.isNotBlank()) {
                            "Venc: ${cardWithExpenses.currentInvoiceDueDateText}"
                        } else {
                            "Venc: Dia ${cardWithExpenses.card.dueDay}"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

private fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color(0xFF8A05BE)
    }
}
