package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CreditCardEntity
import com.example.data.local.TransactionEntity
import com.example.data.model.Categories
import com.example.data.model.CategoryItem
import com.example.data.model.TransactionType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

@Composable
fun TransactionList(
    transactions: List<TransactionEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedType: TransactionType?,
    onSelectType: (TransactionType?) -> Unit,
    selectedCategory: String?,
    onSelectCategory: (String?) -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onAnticipateTransaction: ((TransactionEntity) -> Unit)? = null,
    onDeleteInstallmentGroup: ((String) -> Unit)? = null,
    hideBalances: Boolean = false,
    availableCards: List<CreditCardEntity> = emptyList(),
    customCategories: List<CategoryItem> = emptyList(),
    modifier: Modifier = Modifier
) {
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("transaction_list_container")
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("search_transactions_input"),
            placeholder = { Text("Buscar por descrição ou categoria...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Limpar busca",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Type Filter Chips: [Todas], [Entradas], [Saídas]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedType == null,
                onClick = { onSelectType(null) },
                label = { Text("Todas") },
                modifier = Modifier.testTag("filter_all"),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )

            FilterChip(
                selected = selectedType == TransactionType.INCOME,
                onClick = { onSelectType(if (selectedType == TransactionType.INCOME) null else TransactionType.INCOME) },
                label = { Text("Entradas") },
                modifier = Modifier.testTag("filter_income"),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = IncomeGreen.copy(alpha = 0.2f),
                    selectedLabelColor = IncomeGreen
                )
            )

            FilterChip(
                selected = selectedType == TransactionType.EXPENSE,
                onClick = { onSelectType(if (selectedType == TransactionType.EXPENSE) null else TransactionType.EXPENSE) },
                label = { Text("Saídas") },
                modifier = Modifier.testTag("filter_expense"),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ExpenseRed.copy(alpha = 0.2f),
                    selectedLabelColor = ExpenseRed
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (transactions.isEmpty()) {
            EmptyTransactionState(
                hasFilters = searchQuery.isNotBlank() || selectedType != null || selectedCategory != null
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transactions_lazy_column"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lançamentos (${transactions.size})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(
                    items = transactions,
                    key = { it.id }
                ) { transaction ->
                    val matchedCard = availableCards.find { it.id == transaction.cardId }
                    TransactionItem(
                        transaction = transaction,
                        onEdit = onEditTransaction,
                        onDelete = { transactionToDelete = it },
                        onAnticipate = onAnticipateTransaction,
                        hideBalances = hideBalances,
                        cardName = matchedCard?.name,
                        customCategories = customCategories
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(140.dp)) // Padding for floating pill & FAB
                }
            }
        }
    }

    // Delete Confirmation Dialog
    transactionToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = {
                Text(if (tx.isInstallment) "Excluir lançamento parcelado" else "Excluir lançamento")
            },
            text = {
                val displayTitle = tx.title.ifBlank { tx.category }
                Column {
                    Text("Deseja realmente remover '$displayTitle'?")
                    if (tx.isInstallment && !tx.installmentGroupId.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Este gasto faz parte de uma compra parcelada em ${tx.totalInstallments}x. Você pode excluir apenas esta parcela ou todas as parcelas deste grupo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (tx.isInstallment && !tx.installmentGroupId.isNullOrBlank() && onDeleteInstallmentGroup != null) {
                        Button(
                            onClick = {
                                onDeleteInstallmentGroup(tx.installmentGroupId)
                                transactionToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                            modifier = Modifier.testTag("confirm_delete_all_installments_button")
                        ) {
                            Text("Excluir Todas (${tx.totalInstallments}x)", color = Color.White)
                        }
                    }

                    Button(
                        onClick = {
                            onDeleteTransaction(tx)
                            transactionToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (tx.isInstallment && !tx.installmentGroupId.isNullOrBlank()) MaterialTheme.colorScheme.errorContainer else ExpenseRed
                        ),
                        modifier = Modifier.testTag("confirm_delete_button")
                    ) {
                        Text(
                            text = if (tx.isInstallment) "Apenas Esta" else "Excluir",
                            color = if (tx.isInstallment && !tx.installmentGroupId.isNullOrBlank()) MaterialTheme.colorScheme.onErrorContainer else Color.White
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun EmptyTransactionState(hasFilters: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 48.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (hasFilters) "Nenhum lançamento encontrado" else "Nenhuma transação neste mês",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (hasFilters) "Tente ajustar a busca ou os filtros aplicados." else "Clique no botão '+' para registrar suas entradas e saídas.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
