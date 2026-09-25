package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CreditCardEntity
import com.example.data.local.ImportedNotificationEntity
import com.example.data.model.Categories
import com.example.data.model.TransactionType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportedNotificationsBottomSheet(
    pendingNotifications: List<ImportedNotificationEntity>,
    allNotifications: List<ImportedNotificationEntity>,
    creditCards: List<CreditCardEntity>,
    isPermissionGranted: Boolean = true,
    isListenerConnected: Boolean = true,
    onRequestPermission: () -> Unit = {},
    onScanActiveNotifications: () -> Unit = {},
    onDismissRequest: () -> Unit,
    onConfirmImport: (id: Long, merchant: String, amount: Double, category: String, cardId: Long?) -> Unit,
    onDiscard: (id: Long) -> Unit,
    onImportAll: () -> Unit,
    onClearHistory: () -> Unit,
    onSimulateNotification: () -> Unit,
    onSimulatePreset: (pkg: String, title: String, text: String) -> Unit = { _, _, _ -> },
    onOpenSettings: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Pendentes, 1 = Histórico
    var showCustomTestDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Importador de Bancos",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (pendingNotifications.isNotEmpty()) "${pendingNotifications.size} pendente(s) para revisar" else "Reconhecimento automático de compras e Pix",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("open_bank_settings_from_sheet")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configurar Bancos",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.testTag("close_imported_sheet_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Permission Status Alert or Live Monitoring Bar
            if (!isPermissionGranted) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Acesso a Notificações Desativado",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "Para que o BUMoney identifique seus gastos e Pix no Nubank, Itaú, Inter e outros, você precisa ativar o acesso a notificações nas configurações do Android.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = onRequestPermission,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sheet_grant_permission_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ativar Acesso no Android")
                        }

                        Text(
                            text = "Dica: No Android 13+, se o botão estiver esmaecido, abra Configurações > Apps > BUMoney > toque nos 3 pontinhos > 'Permitir configurações restritas'.",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = IncomeGreen.copy(alpha = 0.08f)
                    ),
                    border = BorderStroke(1.dp, IncomeGreen.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(IncomeGreen)
                            )
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = "Leitor de Notificações Ativo",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isListenerConnected) "Monitorando compras e Pix em tempo real" else "Serviço registrado no sistema Android",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        FilledTonalButton(
                            onClick = onScanActiveNotifications,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("scan_active_notifications_button")
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Escanear",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // Tab Selector & Quick Simulator Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.weight(1f),
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "Pendentes (${pendingNotifications.size})",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "Histórico (${allNotifications.size})",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }

                // Simulation / Test menu
                FilledTonalButton(
                    onClick = { showCustomTestDialog = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .testTag("simulate_notification_button")
                ) {
                    Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Testar", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == 0) {
                // TAB 0: PENDENTES
                if (pendingNotifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircleOutline,
                                contentDescription = null,
                                tint = IncomeGreen,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Nenhuma transação pendente",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Quando o Nubank, Itaú, Inter, Bradesco ou outros emitirem notificações de compras ou Pix no seu celular, elas aparecerão aqui automaticamente para importação com 1 toque.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            OutlinedButton(
                                onClick = { showCustomTestDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Simular Notificação de Teste")
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Novas transações detectadas:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (pendingNotifications.size > 1) {
                            TextButton(onClick = onImportAll) {
                                Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Importar Todas (${pendingNotifications.size})")
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pendingNotifications, key = { it.id }) { item ->
                            PendingNotificationCard(
                                item = item,
                                creditCards = creditCards,
                                onConfirmImport = { m, a, c, cardId ->
                                    onConfirmImport(item.id, m, a, c, cardId)
                                },
                                onDiscard = { onDiscard(item.id) }
                            )
                        }
                    }
                }
            } else {
                // TAB 1: HISTÓRICO
                if (allNotifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhuma notificação registrada no histórico.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Histórico de ${allNotifications.size} notificações:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        TextButton(onClick = onClearHistory) {
                            Icon(Icons.Default.DeleteSweep, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Limpar Histórico", color = MaterialTheme.colorScheme.error)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allNotifications, key = { it.id }) { item ->
                            HistoryNotificationRow(item = item)
                        }
                    }
                }
            }
        }
    }

    // Custom Test / Preset Dialog
    if (showCustomTestDialog) {
        var customBankPkg by remember { mutableStateOf("com.nu.production") }
        var customBankName by remember { mutableStateOf("Nubank") }
        var customNotificationText by remember {
            mutableStateOf("Compra de R$ 89,90 aprovada no iFood com o cartão final 1234.")
        }

        AlertDialog(
            onDismissRequest = { showCustomTestDialog = false },
            title = {
                Text(
                    text = "Testar Leitor de Notificações",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Escolha um exemplo ou cole o texto exato da notificação que seu banco enviou:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Quick presets chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = customBankName == "Nubank",
                                onClick = {
                                    customBankPkg = "com.nu.production"
                                    customBankName = "Nubank"
                                    customNotificationText = "Compra de R$ 89,90 aprovada no iFood com o cartão final 1234."
                                },
                                label = { Text("Nubank") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = customBankName == "Itaú",
                                onClick = {
                                    customBankPkg = "com.itau"
                                    customBankName = "Itaú"
                                    customNotificationText = "Você recebeu um Pix de R$ 250,00 de Carlos Silva."
                                },
                                label = { Text("Itaú Pix") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = customBankName == "Banco Inter",
                                onClick = {
                                    customBankPkg = "br.com.intermedium"
                                    customBankName = "Banco Inter"
                                    customNotificationText = "Compra de R$ 149,90 aprovada em Amazon.com.br no cartão de crédito."
                                },
                                label = { Text("Inter") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = customBankName == "Bradesco",
                                onClick = {
                                    customBankPkg = "com.bradesco.cartoes"
                                    customBankName = "Bradesco"
                                    customNotificationText = "Compra aprovada no seu cartão final 5678, valor R$ 90,00 em Padaria."
                                },
                                label = { Text("Bradesco") }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = customNotificationText,
                        onValueChange = { customNotificationText = it },
                        label = { Text("Texto da Notificação") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 90.dp),
                        maxLines = 4,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSimulatePreset(customBankPkg, customBankName, customNotificationText)
                        showCustomTestDialog = false
                    }
                ) {
                    Icon(Icons.Default.Send, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Processar Teste")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTestDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun PendingNotificationCard(
    item: ImportedNotificationEntity,
    creditCards: List<CreditCardEntity>,
    onConfirmImport: (merchant: String, amount: Double, category: String, cardId: Long?) -> Unit,
    onDiscard: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var editableMerchant by remember { mutableStateOf(item.merchant) }
    var editableAmountStr by remember { mutableStateOf(String.format(Locale("pt", "BR"), "%.2f", item.amount)) }
    var editableCategory by remember { mutableStateOf(item.category) }
    var selectedCardId by remember { mutableStateOf<Long?>(item.matchedCardId) }

    val formattedDate = remember(item.timestamp) {
        SimpleDateFormat("dd/MM/yy HH:mm", Locale("pt", "BR")).format(Date(item.timestamp))
    }
    val valAmount = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(item.amount)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pending_notification_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Bank Badge & Discard button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = item.bankName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (item.cardLastFourDigits != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "Final ${item.cardLastFourDigits}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.Edit,
                            contentDescription = "Editar detalhes",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDiscard,
                        modifier = Modifier.size(32.dp).testTag("discard_button_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Descartar",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Middle Row: Merchant & Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = editableMerchant,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = editableCategory,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = valAmount,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = if (item.type == "EXPENSE") ExpenseRed else IncomeGreen
                )
            }

            // Expanded Edit Mode
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editableMerchant,
                        onValueChange = { editableMerchant = it },
                        label = { Text("Estabelecimento / Descrição") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = editableAmountStr,
                        onValueChange = { editableAmountStr = it },
                        label = { Text("Valor (R$)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Categories Selector
                    Text(
                        text = "Categoria:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val catList = if (item.type == "EXPENSE") Categories.expenseCategories else Categories.incomeCategories
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(catList) { cat ->
                            val isSel = editableCategory == cat.name
                            FilterChip(
                                selected = isSel,
                                onClick = { editableCategory = cat.name },
                                label = { Text(cat.name, style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    // Card selector
                    if (item.type == "EXPENSE" && creditCards.isNotEmpty()) {
                        Text(
                            text = "Vincular a um Cartão de Crédito:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedCardId == null,
                                    onClick = { selectedCardId = null },
                                    label = { Text("Nenhum (Débito/Dinheiro)") },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                            items(creditCards) { card ->
                                val isSel = selectedCardId == card.id
                                FilterChip(
                                    selected = isSel,
                                    onClick = { selectedCardId = card.id },
                                    label = { Text("${card.name} (•${card.lastFourDigits})") },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Raw notification text preview
            Text(
                text = "Texto original: \"${item.rawText.take(90)}${if (item.rawText.length > 90) "..." else ""}\"",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            // Confirm Import Button
            Button(
                onClick = {
                    val amountParsed = editableAmountStr.replace(',', '.').toDoubleOrNull() ?: item.amount
                    onConfirmImport(
                        editableMerchant.ifBlank { item.merchant },
                        amountParsed,
                        editableCategory,
                        selectedCardId
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("confirm_import_button_${item.id}"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Importar Transação")
            }
        }
    }
}

@Composable
private fun HistoryNotificationRow(item: ImportedNotificationEntity) {
    val formattedDate = remember(item.timestamp) {
        SimpleDateFormat("dd/MM/yy HH:mm", Locale("pt", "BR")).format(Date(item.timestamp))
    }
    val valAmount = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(item.amount)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when (item.status) {
                            "IMPORTED" -> IncomeGreen.copy(alpha = 0.15f)
                            "AUTO_IMPORTED" -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (item.status) {
                        "IMPORTED", "AUTO_IMPORTED" -> Icons.Default.Check
                        else -> Icons.Default.Block
                    },
                    contentDescription = null,
                    tint = when (item.status) {
                        "IMPORTED" -> IncomeGreen
                        "AUTO_IMPORTED" -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(16.dp)
                )
            }

            Column {
                Text(
                    text = item.merchant,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.bankName} • $formattedDate • ${item.status}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = valAmount,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = if (item.type == "EXPENSE") ExpenseRed else IncomeGreen
        )
    }
}
