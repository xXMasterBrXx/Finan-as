package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryIconHelper
import com.example.data.model.CategoryItem
import com.example.data.model.TransactionType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoryDialog(
    initialType: TransactionType = TransactionType.EXPENSE,
    categoryToEdit: CategoryItem? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, type: TransactionType, iconName: String, colorHex: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember(categoryToEdit) { mutableStateOf(categoryToEdit?.name ?: "") }
    var selectedType by remember(categoryToEdit, initialType) {
        mutableStateOf(categoryToEdit?.type ?: initialType)
    }
    var selectedIconName by remember(categoryToEdit, initialType) {
        mutableStateOf(categoryToEdit?.iconName ?: if (selectedType == TransactionType.INCOME) "payments" else "shopping_bag")
    }
    var selectedColorHex by remember(categoryToEdit, initialType) {
        mutableStateOf(categoryToEdit?.colorHex ?: if (selectedType == TransactionType.INCOME) "#43A047" else "#EF5350")
    }
    var nameError by remember { mutableStateOf(false) }

    var iconSearchQuery by remember { mutableStateOf("") }
    var selectedIconGroup by remember { mutableStateOf("Todos") }
    val iconGroups = remember {
        listOf("Todos", "Finanças", "Alimentação", "Transporte", "Compras", "Casa & Família", "Saúde & Lazer", "Trabalho & Outros")
    }

    val filteredIcons = remember(iconSearchQuery, selectedIconGroup) {
        CategoryIconHelper.iconList.filter { item ->
            val matchesGroup = (selectedIconGroup == "Todos" || item.group == selectedIconGroup)
            val matchesSearch = if (iconSearchQuery.isBlank()) true else {
                val q = iconSearchQuery.trim().lowercase()
                item.label.lowercase().contains(q) ||
                item.key.lowercase().contains(q) ||
                item.keywords.lowercase().contains(q) ||
                item.group.lowercase().contains(q)
            }
            matchesGroup && matchesSearch
        }
    }

    val colorList = remember { CategoryIconHelper.availableColors }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("category_modal_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (categoryToEdit == null) "Nova Categoria" else "Editar Categoria",
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
                        if (categoryToEdit == null && selectedColorHex == "#EF5350") {
                            selectedColorHex = "#43A047"
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedType == TransactionType.INCOME) IncomeGreen else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("category_income_toggle")
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
                        if (categoryToEdit == null && selectedColorHex == "#43A047") {
                            selectedColorHex = "#EF5350"
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedType == TransactionType.EXPENSE) ExpenseRed else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("category_expense_toggle")
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

            // Name Input
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = { Text("Nome da Categoria") },
                placeholder = { Text(if (selectedType == TransactionType.INCOME) "Ex: Dividendos, Consultoria" else "Ex: Assinaturas, Mercado Livre") },
                isError = nameError,
                supportingText = if (nameError) { { Text("Informe o nome da categoria") } } else null,
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("category_name_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Icon Selector
            val currentSelectedIconInfo = remember(selectedIconName) {
                CategoryIconHelper.iconList.find { it.key == selectedIconName }
            }
            val themeColor = CategoryIconHelper.parseColor(selectedColorHex)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ícone (${CategoryIconHelper.iconList.size} disponíveis)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (currentSelectedIconInfo != null) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = themeColor.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, themeColor.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = currentSelectedIconInfo.vector,
                                contentDescription = null,
                                tint = themeColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentSelectedIconInfo.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = themeColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Icon Search
            OutlinedTextField(
                value = iconSearchQuery,
                onValueChange = { iconSearchQuery = it },
                placeholder = { Text("Buscar ícone (ex: café, uber, pix, mercado...)", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (iconSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { iconSearchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpar busca",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("category_icon_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Group Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                iconGroups.forEach { group ->
                    val isGroupSelected = (selectedIconGroup == group)
                    Surface(
                        onClick = { selectedIconGroup = group },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isGroupSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = if (isGroupSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Text(
                            text = group,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isGroupSelected) FontWeight.Bold else FontWeight.Normal),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Icons Flow
            if (filteredIcons.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum ícone encontrado para \"$iconSearchQuery\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filteredIcons.forEach { item ->
                        val isSelected = selectedIconName == item.key

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) themeColor.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) themeColor else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedIconName = item.key }
                                .testTag("category_icon_${item.key}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.vector,
                                contentDescription = item.label,
                                tint = if (isSelected) themeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Color Selector
            Text(
                text = "Cor de Destaque",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                colorList.forEach { hex ->
                    val color = CategoryIconHelper.parseColor(hex)
                    val isSelected = selectedColorHex.equals(hex, ignoreCase = true)

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColorHex = hex }
                            .testTag("category_color_$hex"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    if (name.trim().isEmpty()) {
                        nameError = true
                        return@Button
                    }
                    onSave(name.trim(), selectedType, selectedIconName, selectedColorHex)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_category_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == TransactionType.INCOME) IncomeGreen else ExpenseRed
                )
            ) {
                Text(
                    text = if (categoryToEdit == null) "Criar Categoria" else "Salvar Alterações",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
