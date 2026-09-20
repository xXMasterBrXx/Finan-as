package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.p2p.P2PConnectionState
import com.example.data.p2p.P2PSyncStatus
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun P2PSyncDialog(
    status: P2PSyncStatus,
    onEnableSync: (String) -> Unit,
    onDisableSync: () -> Unit,
    onTriggerFullSync: () -> Unit,
    onConnectDirect: (String, Int) -> Unit,
    onGenerateKey: () -> String,
    onDismiss: () -> Unit
) {
    val EmeraldPrimary = MaterialTheme.colorScheme.primary
    val context = LocalContext.current
    var inputKey by remember { mutableStateOf(status.syncKey.ifBlank { onGenerateKey() }) }
    var isManualConnectExpanded by remember { mutableStateOf(false) }
    var directIp by remember { mutableStateOf("") }
    var directPort by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "Sincronização P2P",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Conexão Direta (Sem Servidor)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Switch Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (status.isEnabled) {
                            EmeraldPrimary.copy(alpha = 0.08f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ativar Conexão Peer-to-Peer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (status.isEnabled) "Sincronizando em tempo real com dispositivos da mesma chave" else "Desconectado da rede local",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = status.isEnabled,
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    val keyToUse = if (inputKey.isNotBlank()) inputKey else onGenerateKey()
                                    inputKey = keyToUse
                                    onEnableSync(keyToUse)
                                } else {
                                    onDisableSync()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EmeraldPrimary
                            ),
                            modifier = Modifier.testTag("p2p_toggle_switch")
                        )
                    }
                }

                // Sync Key Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Chave de Sincronização",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Display Key Box
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = 1.dp,
                                    color = EmeraldPrimary.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (status.isEnabled && status.syncKey.isNotBlank()) status.syncKey else inputKey,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 2.sp
                                    ),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldPrimary
                                )

                                Row {
                                    IconButton(
                                        onClick = {
                                            val keyToCopy = if (status.isEnabled && status.syncKey.isNotBlank()) status.syncKey else inputKey
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Chave P2P FinanFlow", keyToCopy)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Chave copiada!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Copiar chave",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val newKey = onGenerateKey()
                                            inputKey = newKey
                                            if (status.isEnabled) {
                                                onEnableSync(newKey)
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Gerar nova chave",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Use exatamente esta mesma chave nos outros celulares/dispositivos para que eles se encontrem na rede.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Enter Custom / Peer Key TextField
                        OutlinedTextField(
                            value = inputKey,
                            onValueChange = { inputKey = it.uppercase().trim() },
                            label = { Text("Digitar / Colar outra chave") },
                            placeholder = { Text("Ex: FIN-XYZ789") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("p2p_key_input"),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                keyboardType = KeyboardType.Ascii,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (inputKey.isNotBlank()) {
                                        onEnableSync(inputKey)
                                    }
                                }
                            ),
                            trailingIcon = {
                                if (inputKey != status.syncKey && inputKey.isNotBlank()) {
                                    TextButton(
                                        onClick = { onEnableSync(inputKey) }
                                    ) {
                                        Text("Conectar", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                    }
                                }
                            }
                        )
                    }
                }

                // Connection & Sync Status Card
                if (status.isEnabled) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Status Header Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val (statusColor, statusText, statusIcon) = when (status.state) {
                                        P2PConnectionState.CONNECTED -> Triple(
                                            IncomeGreen,
                                            "${status.connectedPeers.size} dispositivo(s) conectado(s)",
                                            Icons.Default.CheckCircle
                                        )
                                        P2PConnectionState.SEARCHING -> Triple(
                                            Color(0xFFFFB300),
                                            "Buscando aparelhos na rede...",
                                            Icons.Default.Refresh
                                        )
                                        P2PConnectionState.SYNCING -> Triple(
                                            EmeraldPrimary,
                                            "Sincronizando dados...",
                                            Icons.Default.Sync
                                        )
                                        P2PConnectionState.ERROR -> Triple(
                                            ExpenseRed,
                                            "Erro de conexão",
                                            Icons.Default.Warning
                                        )
                                        else -> Triple(
                                            Color.Gray,
                                            "Desconectado",
                                            Icons.Default.Close
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(statusColor)
                                            .then(
                                                if (status.state == P2PConnectionState.SEARCHING || status.state == P2PConnectionState.SYNCING) {
                                                    Modifier.alpha(pulseAlpha)
                                                } else Modifier
                                            )
                                    )

                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                }

                                if (status.state == P2PConnectionState.SYNCING) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = EmeraldPrimary
                                    )
                                }
                            }

                            // Connected Devices List
                            if (status.connectedPeers.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Dispositivos pareados:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    status.connectedPeers.forEach { peer ->
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surface
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Phone,
                                                    contentDescription = null,
                                                    tint = IncomeGreen,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = peer.name,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Text(
                                                        text = "${peer.ip}:${peer.port}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(IncomeGreen.copy(alpha = 0.15f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "ATIVO",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = IncomeGreen
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = "Certifique-se de que os aparelhos estejam conectados à mesma rede Wi-Fi e com a mesma chave ativa.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Last Event Message
                            status.lastEventMessage?.let { msg ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                ) {
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            // Last sync details
                            if (status.lastSyncTimestamp > 0) {
                                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                Text(
                                    text = "Última sincronização: ${timeFormat.format(Date(status.lastSyncTimestamp))} (${status.syncedItemsCount} registros)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Sync All Button
                            Button(
                                onClick = onTriggerFullSync,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("p2p_sync_now_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sincronizar Tudo Agora", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Advanced / Direct IP Connection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isManualConnectExpanded = !isManualConnectExpanded },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Conexão Manual por IP (Avançado)",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Icon(
                                imageVector = if (isManualConnectExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        AnimatedVisibility(visible = isManualConnectExpanded) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (status.localIp.isNotBlank()) {
                                    Text(
                                        text = "Seu endereço local: ${status.localIp}:${status.localPort}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = EmeraldPrimary
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = directIp,
                                        onValueChange = { directIp = it.trim() },
                                        label = { Text("IP do Par") },
                                        placeholder = { Text("192.168.1.50") },
                                        singleLine = true,
                                        modifier = Modifier.weight(2f),
                                        shape = RoundedCornerShape(10.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )

                                    OutlinedTextField(
                                        value = directPort,
                                        onValueChange = { directPort = it.trim() },
                                        label = { Text("Porta") },
                                        placeholder = { Text("8989") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        val portInt = directPort.toIntOrNull() ?: 0
                                        if (directIp.isNotBlank() && portInt > 0) {
                                            onConnectDirect(directIp, portInt)
                                        } else {
                                            Toast.makeText(context, "Preencha IP e Porta válidos", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Conectar por IP Direto")
                                }
                            }
                        }
                    }
                }

                // Security & Architecture Info Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Privacidade total: Conexão direta entre dispositivos na mesma rede local. Seus lançamentos, cartões e categorias são sincronizados ponto a ponto sem intermediação de servidores ou nuvem.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Fechar")
            }
        }
    )
}
