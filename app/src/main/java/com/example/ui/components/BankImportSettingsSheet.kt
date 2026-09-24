package com.example.ui.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import com.example.data.preferences.UserPreferences
import com.example.ui.theme.IncomeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankImportSettingsSheet(
    userPreferences: UserPreferences,
    onDismissRequest: () -> Unit,
    onSimulateNotification: (bankPkg: String, title: String, text: String) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val autoImportEnabled by userPreferences.autoImportNotificationsEnabled.collectAsState()
    val importMode by userPreferences.autoImportMode.collectAsState()
    val autoCatEnabled by userPreferences.autoImportCategorizationEnabled.collectAsState()
    val ignoreDuplicates by userPreferences.autoImportIgnoreDuplicates.collectAsState()
    val notifyOnImport by userPreferences.autoImportNotifyOnImport.collectAsState()

    var isPermissionGranted by remember { mutableStateOf(checkNotificationListenerPermission(context)) }

    // Re-check permission when composable gains focus
    DisposableEffect(Unit) {
        isPermissionGranted = checkNotificationListenerPermission(context)
        onDispose {}
    }

    val supportedBanks = remember {
        listOf(
            BankInfo("com.nu.production", "Nubank", "Cartão de Crédito, Pix e Nuconta"),
            BankInfo("com.itau", "Itaú", "Itaúcard, Personnalité e Pix"),
            BankInfo("br.com.intermedium", "Banco Inter", "Super App Inter e Cartões"),
            BankInfo("com.bradesco", "Bradesco", "Bradesco Cartões e Pix"),
            BankInfo("br.com.bb.android", "Banco do Brasil", "BB Ourocard e Conta"),
            BankInfo("com.santander.app", "Santander", "Santander Way e Conta"),
            BankInfo("com.c6bank.app", "C6 Bank", "C6 Átomos e Cartões"),
            BankInfo("br.gov.caixa.tem", "Caixa Econômica", "Caixa Tem e Cartões"),
            BankInfo("com.picpay", "PicPay", "Carteira e Cartão PicPay"),
            BankInfo("com.mercadopago.wallet", "Mercado Pago", "Mercado Crédito e Conta"),
            BankInfo("br.com.uol.ps.myaccount", "PagBank", "PagSeguro e Cartão"),
            BankInfo("co.stone.app", "Stone", "Stone Pagamentos"),
            BankInfo("br.com.bradesco.next", "Next", "Conta Digital Next"),
            BankInfo("com.btg.pactual.banking", "BTG Pactual", "BTG Banking"),
            BankInfo("br.com.sicoob.mobile", "Sicoob", "Sicoob FaçaParte"),
            BankInfo("br.com.sicredi.mobile", "Sicredi", "Sicredi Mobile"),
            BankInfo("br.com.xp.wallet", "XP Investimentos", "Cartão XP e Conta"),
            BankInfo("com.nomad.app", "Nomad", "Conta Internacional USD"),
            BankInfo("com.transferwise.android", "Wise", "Cartão Multimoedas"),
            BankInfo("com.willbank.app", "Will Bank", "Cartão Will"),
            BankInfo("br.com.neon", "Neon", "Conta e Cartão Neon")
        )
    }

    var disabledBanks by remember { mutableStateOf(userPreferences.getDisabledBankPackages()) }

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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            text = "Importador por Notificação",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Leitura automática de compras e Pix nos bancos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismissRequest) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                }
            }

            // Android Permission Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPermissionGranted) IncomeGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                ),
                border = BorderStroke(
                    1.dp,
                    if (isPermissionGranted) IncomeGreen.copy(alpha = 0.4f) else MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isPermissionGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isPermissionGranted) IncomeGreen else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = if (isPermissionGranted) "Permissão no Android Concedida" else "Acesso às Notificações Necessário",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = if (isPermissionGranted)
                            "O BUMoney possui permissão do sistema Android para ouvir as notificações emitidas pelos aplicativos dos seus bancos."
                        else
                            "Para importar transações automaticamente do Nubank, Itaú, Bradesco, Inter e outros, ative a opção 'BUMoney - Leitor' nas configurações do Android.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!isPermissionGranted) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val intent = Intent(Settings.ACTION_SETTINGS)
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("grant_notification_listener_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ativar Acesso no Android")
                        }
                    }
                }
            }

            // Main Master Switch
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Leitor de Notificações",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Reconhecer automaticamente novos gastos e entradas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = autoImportEnabled,
                        onCheckedChange = { userPreferences.setAutoImportNotificationsEnabled(it) },
                        modifier = Modifier.testTag("toggle_auto_import_master_switch")
                    )
                }
            }

            if (autoImportEnabled) {
                // Import Mode Selection (CONFIRM vs AUTO)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Modo de Importação",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            onClick = { userPreferences.setAutoImportMode("CONFIRM") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("select_mode_confirm_card"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (importMode == "CONFIRM") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            border = if (importMode == "CONFIRM") BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(
                                    imageVector = Icons.Default.FactCheck,
                                    contentDescription = null,
                                    tint = if (importMode == "CONFIRM") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Revisar Antes",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Confirme com 1 toque antes de salvar no extrato.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Card(
                            onClick = { userPreferences.setAutoImportMode("AUTO") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("select_mode_auto_card"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (importMode == "AUTO") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            border = if (importMode == "AUTO") BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = if (importMode == "AUTO") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Automático",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Insere direto no extrato assim que a notificação chega.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Options Switches
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Categorização Inteligente",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = autoCatEnabled,
                                onCheckedChange = { userPreferences.setAutoImportCategorizationEnabled(it) }
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ignorar Notificações Duplicadas",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = ignoreDuplicates,
                                onCheckedChange = { userPreferences.setIgnoreNotificationDuplicatesEnabled(it) }
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Alertar ao Importar no Sistema",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = notifyOnImport,
                                onCheckedChange = { userPreferences.setNotifyOnImportEnabled(it) }
                            )
                        }
                    }
                }

                // Supported Banks Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Bancos Suportados (${supportedBanks.size})",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    supportedBanks.forEach { bank ->
                        val isDisabled = disabledBanks.contains(bank.pkg)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bank.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = bank.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = !isDisabled,
                                onCheckedChange = {
                                    userPreferences.toggleDisabledBankPackage(bank.pkg)
                                    disabledBanks = userPreferences.getDisabledBankPackages()
                                }
                            )
                        }
                    }
                }

                // Test Notification Simulator
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "Testar Reconhecimento com Exemplos",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedButton(
                        onClick = {
                            onSimulateNotification(
                                "com.nu.production",
                                "Nubank",
                                "Compra de R$ 89,90 aprovada no iFood com o cartão final 1234."
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Fastfood, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Simular Nubank (iFood - R$ 89,90)")
                    }

                    OutlinedButton(
                        onClick = {
                            onSimulateNotification(
                                "com.itau",
                                "Itaú",
                                "Você recebeu um Pix de R$ 250,00 de Carlos Silva."
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Payments, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Simular Itaú (Pix Recebido - R$ 250,00)")
                    }

                    OutlinedButton(
                        onClick = {
                            onSimulateNotification(
                                "br.com.intermedium",
                                "Banco Inter",
                                "Compra de R$ 149,90 aprovada em Amazon.com.br no cartão de crédito."
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Simular Banco Inter (Amazon - R$ 149,90)")
                    }
                }
            }
        }
    }
}

private data class BankInfo(
    val pkg: String,
    val name: String,
    val description: String
)

private fun checkNotificationListenerPermission(context: android.content.Context): Boolean {
    return try {
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        flat != null && flat.contains(context.packageName)
    } catch (e: Exception) {
        false
    }
}
