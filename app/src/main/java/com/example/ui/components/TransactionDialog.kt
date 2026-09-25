package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CreditCardEntity
import com.example.data.local.TransactionEntity
import com.example.data.model.Categories
import com.example.data.model.CategoryItem
import com.example.data.model.TransactionType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.Formatters
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionDialog(
    transactionToEdit: TransactionEntity? = null,
    availableCards: List<CreditCardEntity> = emptyList(),
    customCategories: List<CategoryItem> = emptyList(),
    initialCardId: Long? = null,
    onDismiss: () -> Unit,
    onAddNewCategory: ((TransactionType) -> Unit)? = null,
    onEditCategory: ((CategoryItem) -> Unit)? = null,
    onSave: (
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        timestamp: Long,
        note: String,
        cardId: Long?,
        isInstallment: Boolean,
        totalInstallments: Int,
        isRecurring: Boolean,
        recurringMonths: Int,
        recurringIntervalMonths: Int,
        isIndefinite: Boolean
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val initialType = if (transactionToEdit != null) {
        if (transactionToEdit.type == TransactionType.INCOME.name) TransactionType.INCOME else TransactionType.EXPENSE
    } else {
        TransactionType.EXPENSE
    }

    var selectedType by remember { mutableStateOf(initialType) }
    var title by remember { mutableStateOf(transactionToEdit?.title ?: "") }
    var amountText by remember {
        mutableStateOf(
            if (transactionToEdit != null) {
                if (transactionToEdit.isInstallment && transactionToEdit.totalInstallments > 1) {
                    val total = transactionToEdit.amount * transactionToEdit.totalInstallments
                    String.format(java.util.Locale.US, "%.2f", total)
                } else {
                    String.format(java.util.Locale.US, "%.2f", transactionToEdit.amount)
                }
            } else ""
        )
    }
    var note by remember { mutableStateOf(transactionToEdit?.note ?: "") }
    var selectedTimestamp by remember { mutableLongStateOf(transactionToEdit?.timestamp ?: System.currentTimeMillis()) }
    var selectedCardId by remember {
        mutableStateOf<Long?>(transactionToEdit?.cardId ?: initialCardId)
    }

    var isInstallment by remember { mutableStateOf(transactionToEdit?.isInstallment ?: false) }
    var totalInstallments by remember { mutableIntStateOf(if ((transactionToEdit?.totalInstallments ?: 1) > 1) transactionToEdit!!.totalInstallments else 2) }
    var isRecurring by remember { mutableStateOf(transactionToEdit?.isRecurring ?: false) }
    var isIndefiniteRecurring by remember { mutableStateOf(true) }
    var recurringIntervalMonths by remember { mutableIntStateOf(1) }
    var recurringMonths by remember { mutableIntStateOf(12) }

    val typeCustom = customCategories.filter { it.type == selectedType }
    val categories = if (selectedType == TransactionType.INCOME) {
        Categories.incomeCategories + typeCustom
    } else {
        Categories.expenseCategories + typeCustom
    }

    var selectedCategory by remember(selectedType, categories) {
        mutableStateOf(
            if (transactionToEdit != null && transactionToEdit.type == selectedType.name) {
                Categories.getCategoryByName(transactionToEdit.category, selectedType, customCategories)
            } else {
                categories.firstOrNull() ?: if (selectedType == TransactionType.INCOME) Categories.Salary else Categories.Food
            }
        )
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("transaction_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (transactionToEdit == null) "Novo Lançamento" else "Editar Lançamento",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Type Switcher: Entrada vs Saída
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Entrada Tab
                Surface(
                    onClick = {
                        selectedType = TransactionType.INCOME
                        selectedCategory = Categories.incomeCategories.first()
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedType == TransactionType.INCOME) IncomeGreen else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("type_income_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (selectedType == TransactionType.INCOME) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Entrada",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedType == TransactionType.INCOME) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Saída Tab
                Surface(
                    onClick = {
                        selectedType = TransactionType.EXPENSE
                        selectedCategory = Categories.expenseCategories.first()
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedType == TransactionType.EXPENSE) ExpenseRed else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("type_expense_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (selectedType == TransactionType.EXPENSE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Saída",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedType == TransactionType.EXPENSE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Amount Input
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    val filtered = it.replace(',', '.')
                    if (filtered.count { ch -> ch == '.' } <= 1 && filtered.all { ch -> ch.isDigit() || ch == '.' }) {
                        amountText = filtered
                        amountError = false
                    }
                },
                label = { Text("Valor (R$)") },
                placeholder = { Text("0,00") },
                leadingIcon = {
                    Text(
                        text = "R$",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedType == TransactionType.INCOME) IncomeGreen else ExpenseRed,
                        modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                    )
                },
                isError = amountError,
                supportingText = if (amountError) { { Text("Informe um valor válido maior que zero") } } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tx_amount_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Title Input (Opcional)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Descrição (opcional)") },
                placeholder = { Text(if (selectedType == TransactionType.INCOME) "Ex: Salário, Projeto X (padrão: ${selectedCategory.name})" else "Ex: Almoço, Uber (padrão: ${selectedCategory.name})") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tx_title_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Picker
            Text(
                text = "Categoria",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory.id == cat.id
                    Surface(
                        onClick = { selectedCategory = cat },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) cat.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, cat.color) else null,
                        modifier = Modifier.testTag("cat_chip_${cat.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = cat.icon,
                                contentDescription = null,
                                tint = if (isSelected) cat.color else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) cat.color else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (cat.dbId > 0 && onEditCategory != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .clickable { onEditCategory(cat) }
                                        .padding(2.dp)
                                        .testTag("edit_custom_cat_chip_${cat.dbId}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar ${cat.name}",
                                        tint = if (isSelected) cat.color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (onAddNewCategory != null) {
                    Surface(
                        onClick = { onAddNewCategory(selectedType) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("add_new_cat_chip_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Add,
                                contentDescription = "Nova Categoria",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Nova Categoria",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Credit Card Selection for Expenses
            if (selectedType == TransactionType.EXPENSE && availableCards.isNotEmpty()) {
                Text(
                    text = "Vincular a um Cartão de Crédito:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Option for "No card" (Cash / Debit / Pix)
                    val isNoneSelected = selectedCardId == null
                    Surface(
                        onClick = { selectedCardId = null },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isNoneSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (isNoneSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier.testTag("card_chip_none")
                    ) {
                        Text(
                            text = "Nenhum (Débito/Pix/Dinheiro)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isNoneSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isNoneSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }

                    availableCards.forEach { card ->
                        val isCardSelected = selectedCardId == card.id
                        val cardColor = try {
                            Color(android.graphics.Color.parseColor(card.colorHex))
                        } catch (e: Exception) {
                            Color(0xFF8A05BE)
                        }

                        Surface(
                            onClick = { selectedCardId = card.id },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCardSelected) cardColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (isCardSelected) androidx.compose.foundation.BorderStroke(1.5.dp, cardColor) else null,
                            modifier = Modifier.testTag("card_chip_${card.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = if (isCardSelected) cardColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = card.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isCardSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isCardSelected) cardColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Date Picker Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Data da Transação",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = Formatters.formatFullDate(selectedTimestamp),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TextButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.testTag("open_date_picker_button")
                ) {
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Alterar data")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Parcelamento Section for Expenses
            if (selectedType == TransactionType.EXPENSE && !isRecurring) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isInstallment) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = if (isInstallment) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Parcelar Gasto / Compra",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Dividir valor em parcelas nos meses posteriores",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = isInstallment,
                                onCheckedChange = { 
                                    isInstallment = it 
                                    if (it) isRecurring = false
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.testTag("installment_switch")
                            )
                        }

                        if (isInstallment) {
                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Número de Parcelas:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Quick chips
                            val quickInstallments = listOf(2, 3, 4, 5, 6, 10, 12, 18, 24)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                quickInstallments.forEach { n ->
                                    val isSelected = totalInstallments == n
                                    Surface(
                                        onClick = { totalInstallments = n },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.testTag("installment_chip_${n}x")
                                    ) {
                                        Text(
                                            text = "${n}x",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                            ),
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Stepper for custom installments
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Ajustar parcelas:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    FilledTonalIconButton(
                                        onClick = { if (totalInstallments > 2) totalInstallments-- },
                                        enabled = totalInstallments > 2,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Menos", modifier = Modifier.size(16.dp))
                                    }

                                    Text(
                                        text = "${totalInstallments}x",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )

                                    FilledTonalIconButton(
                                        onClick = { if (totalInstallments < 48) totalInstallments++ },
                                        enabled = totalInstallments < 48,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "Mais", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            val parsed = amountText.toDoubleOrNull() ?: 0.0
                            if (parsed > 0.0) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Valor por parcela:",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${totalInstallments}x de " + Formatters.formatCurrency(parsed / totalInstallments),
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold, color = ExpenseRed)
                                            )
                                        }
                                        Text(
                                            text = "Total: " + Formatters.formatCurrency(parsed),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Gastos / Receitas Recorrentes (Fixo mensal)
            if (!isInstallment) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isRecurring) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = if (isRecurring) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (selectedType == TransactionType.EXPENSE) "Gasto Fixo / Recorrente" else "Entrada Recorrente",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Repetir todo mês (ex: Aluguel, Assinaturas, Salário)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = isRecurring,
                                onCheckedChange = { 
                                    isRecurring = it 
                                    if (it) isInstallment = false
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.secondary,
                                    checkedTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.testTag("recurring_switch")
                            )
                        }

                        if (isRecurring) {
                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Frequência da cobrança (Regularidade)
                            Text(
                                text = "Frequência / Regularidade:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val intervals = listOf(
                                1 to "Mensal (Todo mês)",
                                2 to "A cada 2 meses (Bimestral)",
                                3 to "A cada 3 meses (Trimestral)",
                                6 to "A cada 6 meses (Semestral)",
                                12 to "A cada 12 meses (Anual)"
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                intervals.forEach { (interval, label) ->
                                    val isSelected = recurringIntervalMonths == interval
                                    Surface(
                                        onClick = { recurringIntervalMonths = interval },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.testTag("recurring_interval_${interval}m")
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                            ),
                                            color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Duração da Recorrência
                            Text(
                                text = "Duração da Recorrência:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Opção Indefinido
                                val isIndefiniteSelected = isIndefiniteRecurring
                                Surface(
                                    onClick = { isIndefiniteRecurring = true },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isIndefiniteSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.testTag("recurring_chip_indefinite")
                                ) {
                                    Text(
                                        text = "Indefinido (Sempre)",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isIndefiniteSelected) FontWeight.ExtraBold else FontWeight.Medium
                                        ),
                                        color = if (isIndefiniteSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }

                                listOf(3, 6, 12, 24, 36).forEach { count ->
                                    val isSelected = !isIndefiniteRecurring && recurringMonths == count
                                    Surface(
                                        onClick = {
                                            isIndefiniteRecurring = false
                                            recurringMonths = count
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.testTag("recurring_chip_${count}x")
                                    ) {
                                        Text(
                                            text = "$count ${if (recurringIntervalMonths == 1) "meses" else "vezes"}",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                            ),
                                            color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isIndefiniteRecurring) {
                                    "Cobrado continuamente ${if (recurringIntervalMonths == 1) "todo mês" else "a cada $recurringIntervalMonths meses"} indefinidamente."
                                } else {
                                    "Serão gerados $recurringMonths lançamentos ${if (recurringIntervalMonths == 1) "mensais" else "a cada $recurringIntervalMonths meses"}."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Note Input (Optional)
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Observação (opcional)") },
                placeholder = { Text("Ex: Pago no débito, parcelado em 2x, etc.") },
                singleLine = false,
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tx_note_input")
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    val parsedAmount = amountText.toDoubleOrNull()
                    if (parsedAmount == null || parsedAmount <= 0.0) {
                        amountError = true
                    } else {
                        val finalTitle = if (title.isNotBlank()) title.trim() else selectedCategory.name
                        onSave(
                            finalTitle,
                            parsedAmount,
                            selectedType,
                            selectedCategory.name,
                            selectedTimestamp,
                            note.trim(),
                            if (selectedType == TransactionType.EXPENSE) selectedCardId else null,
                            isInstallment && selectedType == TransactionType.EXPENSE,
                            if (isInstallment && selectedType == TransactionType.EXPENSE) totalInstallments else 1,
                            isRecurring,
                            recurringMonths,
                            recurringIntervalMonths,
                            isIndefiniteRecurring
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_transaction_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == TransactionType.INCOME) IncomeGreen else ExpenseRed
                )
            ) {
                Text(
                    text = if (transactionToEdit == null) {
                        if (isRecurring) {
                            if (isIndefiniteRecurring) {
                                "Salvar ${if (selectedType == TransactionType.EXPENSE) "Gasto" else "Receita"} Recorrente (Indefinido)"
                            } else {
                                "Salvar ${if (selectedType == TransactionType.EXPENSE) "Gasto" else "Receita"} Recorrente (${recurringMonths}x)"
                            }
                        } else if (isInstallment && selectedType == TransactionType.EXPENSE) {
                            "Criar Compra Parcelada (${totalInstallments}x)"
                        } else {
                            "Salvar Lançamento"
                        }
                    } else {
                        if (isRecurring) {
                            "Atualizar Lançamento Recorrente"
                        } else if (isInstallment && selectedType == TransactionType.EXPENSE) {
                            "Atualizar Parcelamento (${totalInstallments}x)"
                        } else {
                            "Atualizar Lançamento"
                        }
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Material 3 Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedTimestamp
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { utcMillis ->
                            // Convert UTC midnight millis to local midnight/noon millis to prevent timezone offset shifts
                            val utcCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = utcMillis
                            }
                            val localCal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
                                set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
                                set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
                                set(Calendar.HOUR_OF_DAY, 12)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            selectedTimestamp = localCal.timeInMillis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
