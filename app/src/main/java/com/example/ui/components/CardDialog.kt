package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CreditCardEntity

val PresetCardColors = listOf(
    "#8A05BE" to "Nubank",
    "#FF7A00" to "Inter",
    "#EAB308" to "Amarelo / Ouro",
    "#1E293B" to "Black / Escuro",
    "#2563EB" to "Azul Itaú/BB",
    "#DC2626" to "Santander/Bradesco",
    "#059669" to "Sicredi / Esmeralda"
)

@Composable
fun CardDialog(
    cardToEdit: CreditCardEntity? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, lastFourDigits: String, colorHex: String, limit: Double, closingDay: Int, dueDay: Int) -> Unit
) {
    var name by remember { mutableStateOf(cardToEdit?.name ?: "") }
    var lastFourDigits by remember { mutableStateOf(cardToEdit?.lastFourDigits ?: "") }
    var selectedColor by remember { mutableStateOf(cardToEdit?.colorHex ?: PresetCardColors.first().first) }
    var limitInput by remember {
        mutableStateOf(if (cardToEdit != null && cardToEdit.limitAmount > 0) cardToEdit.limitAmount.toString() else "")
    }
    var closingDayInput by remember { mutableStateOf(cardToEdit?.closingDay?.toString() ?: "10") }
    var dueDayInput by remember { mutableStateOf(cardToEdit?.dueDay?.toString() ?: "17") }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (cardToEdit == null) "Novo Cartão" else "Editar Cartão",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_dialog_content"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = { Text("Nome do Cartão (ex: Nubank Roxo)") },
                    placeholder = { Text("Ex: Nubank, Inter, XP") },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Por favor insira um nome para o cartão") }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_name_input")
                )

                // Last 4 digits & Limit row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = lastFourDigits,
                        onValueChange = {
                            if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                lastFourDigits = it
                            }
                        },
                        label = { Text("Finais (4 dígitos)") },
                        placeholder = { Text("1234") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_digits_input")
                    )

                    OutlinedTextField(
                        value = limitInput,
                        onValueChange = { limitInput = it },
                        label = { Text("Limite (R$)") },
                        placeholder = { Text("5000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_limit_input")
                    )
                }

                // Due Day and Closing Day
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = closingDayInput,
                        onValueChange = {
                            if (it.length <= 2 && it.all { c -> c.isDigit() }) {
                                closingDayInput = it
                            }
                        },
                        label = { Text("Dia Fechamento") },
                        placeholder = { Text("10") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_closing_day_input")
                    )

                    OutlinedTextField(
                        value = dueDayInput,
                        onValueChange = {
                            if (it.length <= 2 && it.all { c -> c.isDigit() }) {
                                dueDayInput = it
                            }
                        },
                        label = { Text("Dia Vencimento") },
                        placeholder = { Text("17") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_due_day_input")
                    )
                }

                // Color Selection
                Text(
                    text = "Cor do Cartão:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(PresetCardColors) { (hex, _) ->
                        val color = try {
                            Color(android.graphics.Color.parseColor(hex))
                        } catch (e: Exception) {
                            Color(0xFF8A05BE)
                        }
                        val isSelected = selectedColor.equals(hex, ignoreCase = true)

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex }
                                .testTag("color_preset_$hex")
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    val limit = limitInput.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val closingDay = closingDayInput.toIntOrNull() ?: 10
                    val dueDay = dueDayInput.toIntOrNull() ?: 17

                    onSave(name.trim(), lastFourDigits.trim(), selectedColor, limit, closingDay, dueDay)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_card_button")
            ) {
                Text("Salvar Cartão")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_card_button")
            ) {
                Text("Cancelar")
            }
        }
    )
}
