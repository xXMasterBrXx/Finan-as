package com.example.data.p2p

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.CreditCardEntity
import com.example.data.local.CustomCategoryEntity
import com.example.data.local.TransactionEntity
import com.example.data.preferences.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.security.MessageDigest
import java.util.Collections
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class P2PSyncManager(
    private val context: Context,
    private val database: AppDatabase,
    private val userPreferences: UserPreferences
) {
    private val tag = "P2PSyncManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val deviceId: String = UUID.randomUUID().toString().take(8)
    val deviceName: String = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"

    private val _syncStatus = MutableStateFlow(
        P2PSyncStatus(
            role = userPreferences.getP2PRole(),
            isInitialSyncDone = userPreferences.isP2PInitialSyncDone()
        )
    )
    val syncStatus: StateFlow<P2PSyncStatus> = _syncStatus.asStateFlow()

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private var nsdManager: NsdManager? = null
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    private val activeConnections = ConcurrentHashMap<String, PeerConnection>()
    private val discoveredServices = Collections.synchronizedList(mutableListOf<P2PDevice>())

    // Flag to avoid broadcast echoing during incoming sync operations
    @Volatile
    var isApplyingRemoteSync: Boolean = false

    private val serviceType = "_finanflow._tcp."

    init {
        nsdManager = context.getSystemService(Context.NSD_SERVICE) as? NsdManager
    }

    fun hashKey(key: String): String {
        val clean = key.trim().uppercase()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(clean.toByteArray(Charsets.UTF_8))
        return digest.take(8).joinToString("") { "%02x".format(it) }
    }

    fun startSync(syncKey: String, role: P2PRole = userPreferences.getP2PRole()) {
        val cleanKey = syncKey.trim().uppercase()
        if (cleanKey.isBlank()) {
            _syncStatus.value = _syncStatus.value.copy(
                errorMessage = "Chave de sincronização inválida."
            )
            return
        }

        stopSync(preserveState = false)

        val isInitialDone = if (role == P2PRole.HOST) true else userPreferences.isP2PInitialSyncDone()

        _syncStatus.value = P2PSyncStatus(
            isEnabled = true,
            syncKey = cleanKey,
            role = role,
            isInitialSyncDone = isInitialDone,
            state = P2PConnectionState.SEARCHING,
            localIp = getLocalIpAddress(),
            lastEventMessage = if (role == P2PRole.HOST) {
                "Aguardando conexão de outros aparelhos (Aparelho Principal)..."
            } else {
                "Buscando aparelho principal na rede Wi-Fi..."
            }
        )

        scope.launch {
            try {
                startServer(cleanKey)
                startNsdAdvertisement(cleanKey)
                startNsdDiscovery(cleanKey)
                startKeepAliveLoop()
            } catch (e: Exception) {
                Log.e(tag, "Erro ao iniciar P2P Sync", e)
                _syncStatus.value = _syncStatus.value.copy(
                    state = P2PConnectionState.ERROR,
                    errorMessage = "Erro ao iniciar P2P: ${e.localizedMessage}"
                )
            }
        }
    }

    fun stopSync(preserveState: Boolean = false) {
        try {
            stopNsdDiscovery()
            stopNsdAdvertisement()
            closeAllConnections()
            serverJob?.cancel()
            serverJob = null
            serverSocket?.close()
            serverSocket = null
        } catch (e: Exception) {
            Log.e(tag, "Erro ao parar P2P", e)
        }

        if (!preserveState) {
            _syncStatus.value = P2PSyncStatus(
                isEnabled = false,
                syncKey = "",
                role = userPreferences.getP2PRole(),
                isInitialSyncDone = userPreferences.isP2PInitialSyncDone(),
                state = P2PConnectionState.DISABLED
            )
        }
    }

    private fun startServer(syncKey: String) {
        try {
            val server = ServerSocket(0) // dynamic available port
            serverSocket = server
            val port = server.localPort
            val ip = getLocalIpAddress()

            _syncStatus.value = _syncStatus.value.copy(
                localPort = port,
                localIp = ip
            )

            serverJob = scope.launch {
                while (isActive && !server.isClosed) {
                    try {
                        val clientSocket = server.accept()
                        handleIncomingSocket(clientSocket, syncKey)
                    } catch (e: Exception) {
                        if (!server.isClosed) {
                            Log.e(tag, "Erro no accept do servidor", e)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Falha ao abrir ServerSocket", e)
            _syncStatus.value = _syncStatus.value.copy(
                state = P2PConnectionState.ERROR,
                errorMessage = "Falha ao abrir porta de rede local."
            )
        }
    }

    private fun handleIncomingSocket(socket: Socket, syncKey: String) {
        scope.launch {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val writer = PrintWriter(socket.getOutputStream(), true)

            // Read Handshake
            val handshakeLine = withContext(Dispatchers.IO) { reader.readLine() }
            if (handshakeLine != null) {
                try {
                    val json = JSONObject(handshakeLine)
                    if (json.optString("type") == "HANDSHAKE") {
                        val peerKeyHash = json.optString("keyHash")
                        val myKeyHash = hashKey(syncKey)
                        val peerSenderId = json.optString("senderId")
                        val peerSenderName = json.optString("senderName")
                        val peerRoleStr = json.optString("role", P2PRole.CLIENT.name)
                        val peerRole = try { P2PRole.valueOf(peerRoleStr) } catch (e: Exception) { P2PRole.CLIENT }
                        val peerInitialSyncDone = json.optBoolean("isInitialSyncDone", false)

                        if (peerKeyHash == myKeyHash && peerSenderId != deviceId) {
                            val myRole = _syncStatus.value.role
                            val myInitialSyncDone = _syncStatus.value.isInitialSyncDone

                            // Respond Handshake ACK
                            val ack = P2PJsonCodec.createHandshakeAck(
                                senderId = deviceId,
                                senderName = deviceName,
                                status = "OK",
                                role = myRole,
                                isInitialSyncDone = myInitialSyncDone
                            )
                            writer.println(ack.toString())

                            val peerDevice = P2PDevice(
                                id = peerSenderId,
                                name = peerSenderName,
                                ip = socket.inetAddress.hostAddress ?: "unknown",
                                port = socket.port,
                                role = peerRole
                            )

                            activeConnections[peerSenderId]?.close()
                            val connection = PeerConnection(socket, reader, writer, peerDevice)
                            activeConnections[peerSenderId] = connection
                            updateConnectedPeersState()

                            // If this device is HOST and peer is a brand new CLIENT that hasn't done initial sync,
                            // HOST sends the initial authoritative snapshot.
                            // If this device is CLIENT and hasn't done initial sync, request snapshot.
                            // If both devices have completed initial sync (reconnection of established peers),
                            // perform BIDIRECTIONAL SYNC so changes from both sides are preserved and merged!
                            if (myRole == P2PRole.HOST && !peerInitialSyncDone) {
                                sendSnapshotOverwriteToConnection(connection)
                            } else if (!myInitialSyncDone && myRole == P2PRole.CLIENT) {
                                val syncReq = P2PJsonCodec.createSyncRequest(deviceId)
                                writer.println(syncReq.toString())
                            } else {
                                sendSyncDataToConnection(connection, requiresResponse = true)
                            }

                            listenToConnection(connection)
                        } else {
                            // Invalid key hash or self-connection
                            val ack = P2PJsonCodec.createHandshakeAck(
                                senderId = deviceId,
                                senderName = deviceName,
                                status = "REJECTED_KEY_MISMATCH",
                                role = _syncStatus.value.role
                            )
                            writer.println(ack.toString())
                            socket.close()
                        }
                    } else {
                        socket.close()
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Erro ao processar handshake recebido", e)
                    socket.close()
                }
            }
        }
    }

    fun connectToPeerDirect(ip: String, port: Int) {
        val currentKey = _syncStatus.value.syncKey
        if (currentKey.isBlank()) {
            _syncStatus.value = _syncStatus.value.copy(errorMessage = "Defina uma chave antes de conectar.")
            return
        }

        scope.launch {
            try {
                _syncStatus.value = _syncStatus.value.copy(
                    state = P2PConnectionState.CONNECTING,
                    lastEventMessage = "Conectando diretamente a $ip:$port..."
                )
                connectToPeer(ip, port, currentKey)
            } catch (e: Exception) {
                Log.e(tag, "Falha na conexão direta", e)
                _syncStatus.value = _syncStatus.value.copy(
                    errorMessage = "Não foi possível conectar a $ip:$port (${e.localizedMessage})"
                )
            }
        }
    }

    private suspend fun connectToPeer(ip: String, port: Int, syncKey: String) = withContext(Dispatchers.IO) {
        try {
            val socket = Socket(ip, port)
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val writer = PrintWriter(socket.getOutputStream(), true)

            val keyHash = hashKey(syncKey)
            val myRole = _syncStatus.value.role
            val myInitialSyncDone = _syncStatus.value.isInitialSyncDone

            // Send Handshake
            val handshake = P2PJsonCodec.createHandshake(
                senderId = deviceId,
                senderName = deviceName,
                keyHash = keyHash,
                role = myRole,
                isInitialSyncDone = myInitialSyncDone
            )
            writer.println(handshake.toString())

            // Await Handshake ACK
            val ackLine = reader.readLine()
            if (ackLine != null) {
                val ackJson = JSONObject(ackLine)
                if (ackJson.optString("type") == "HANDSHAKE_ACK" && ackJson.optString("status") == "OK") {
                    val peerId = ackJson.optString("senderId")
                    val peerName = ackJson.optString("senderName")
                    val peerRoleStr = ackJson.optString("role", P2PRole.HOST.name)
                    val peerRole = try { P2PRole.valueOf(peerRoleStr) } catch (e: Exception) { P2PRole.HOST }
                    val peerInitialSyncDone = ackJson.optBoolean("isInitialSyncDone", true)

                    val peerDevice = P2PDevice(
                        id = peerId,
                        name = peerName,
                        ip = ip,
                        port = port,
                        role = peerRole
                    )

                    activeConnections[peerId]?.close()
                    val connection = PeerConnection(socket, reader, writer, peerDevice)
                    activeConnections[peerId] = connection
                    updateConnectedPeersState()

                    // If this device is HOST and peer has not completed initial sync, send snapshot.
                    // If this device is CLIENT and has not completed initial sync, request snapshot.
                    // If both devices have completed initial sync (reconnection of established peers),
                    // perform BIDIRECTIONAL SYNC so changes from both sides are preserved and merged!
                    if (myRole == P2PRole.HOST && !peerInitialSyncDone) {
                        sendSnapshotOverwriteToConnection(connection)
                    } else if (myRole == P2PRole.CLIENT && !myInitialSyncDone) {
                        val syncReq = P2PJsonCodec.createSyncRequest(deviceId)
                        writer.println(syncReq.toString())
                    } else {
                        sendSyncDataToConnection(connection, requiresResponse = true)
                    }

                    scope.launch { listenToConnection(connection) }
                } else {
                    socket.close()
                }
            } else {
                socket.close()
            }
        } catch (e: java.net.ConnectException) {
            Log.d(tag, "Conexão recusada para $ip:$port (par offline)")
        } catch (e: Exception) {
            Log.e(tag, "Falha ao conectar com $ip:$port", e)
        }
    }

    private suspend fun listenToConnection(conn: PeerConnection) = withContext(Dispatchers.IO) {
        try {
            while (isActive && !conn.socket.isClosed) {
                val line = conn.reader.readLine() ?: break
                if (line.isBlank()) continue
                handleIncomingMessage(line, conn)
            }
        } catch (e: Exception) {
            Log.d(tag, "Conexão encerrada com ${conn.device.name}", e)
        } finally {
            activeConnections.remove(conn.device.id)
            conn.close()
            updateConnectedPeersState()
        }
    }

    private suspend fun handleIncomingMessage(rawJson: String, fromConn: PeerConnection) {
        try {
            val json = JSONObject(rawJson)
            val type = json.optString("type")

            when (type) {
                "SNAPSHOT_OVERWRITE" -> {
                    // Receiver of the sync key receives authoritative snapshot from the host.
                    // If initial sync was ALREADY completed in the past, this is a reconnecting device.
                    // DO NOT wipe the database! Reconcile incoming data instead so offline changes are preserved!
                    if (_syncStatus.value.isInitialSyncDone) {
                        Log.d(tag, "SNAPSHOT_OVERWRITE recebido após initialSyncDone; reconciliando bidirecionalmente.")
                        handleSyncResponse(json, fromConn)
                        return
                    }

                    _syncStatus.value = _syncStatus.value.copy(
                        state = P2PConnectionState.SYNCING,
                        lastEventMessage = "Recebendo dados originais de ${fromConn.device.name}..."
                    )

                    isApplyingRemoteSync = true
                    var totalCount = 0
                    try {
                        // 1. Parse incoming cards and insert them
                        val cardsArray = json.optJSONArray("cards")
                        val cardUuidToNewIdMap = mutableMapOf<String, Long>()

                        // Clear local data cleanly on first initial pairing only
                        database.transactionDao().deleteAllTransactions()
                        database.creditCardDao().deleteAllCards()
                        database.customCategoryDao().deleteAllCategories()

                        if (cardsArray != null) {
                            for (i in 0 until cardsArray.length()) {
                                val cardJson = cardsArray.getJSONObject(i)
                                val card = P2PJsonCodec.jsonToCard(cardJson)
                                val newId = database.creditCardDao().insertCard(card.copy(id = 0))
                                cardUuidToNewIdMap[card.syncUuid] = newId
                                totalCount++
                            }
                        }

                        // 2. Parse and insert custom categories
                        val catArray = json.optJSONArray("categories")
                        if (catArray != null) {
                            for (i in 0 until catArray.length()) {
                                val catJson = catArray.getJSONObject(i)
                                val cat = P2PJsonCodec.jsonToCategory(catJson)
                                database.customCategoryDao().insertCategory(cat.copy(id = 0))
                                totalCount++
                            }
                        }

                        // 3. Parse and insert transactions with correct mapped card IDs
                        val txArray = json.optJSONArray("transactions")
                        if (txArray != null) {
                            val txList = mutableListOf<TransactionEntity>()
                            for (i in 0 until txArray.length()) {
                                val txJson = txArray.getJSONObject(i)
                                val tx = P2PJsonCodec.jsonToTransaction(txJson)
                                val cardSyncUuid = P2PJsonCodec.extractCardSyncUuid(txJson)

                                val resolvedCardId = if (!cardSyncUuid.isNullOrBlank()) {
                                    cardUuidToNewIdMap[cardSyncUuid] ?: tx.cardId
                                } else {
                                    tx.cardId
                                }

                                txList.add(tx.copy(id = 0, cardId = resolvedCardId))
                                totalCount++
                            }
                            if (txList.isNotEmpty()) {
                                database.transactionDao().insertAll(txList)
                            }
                        }

                        // 4. Reconcile invoices if present
                        reconcileInvoices(
                            paidInvoices = P2PJsonCodec.extractInvoices(json, "paidInvoices"),
                            unpaidInvoices = P2PJsonCodec.extractInvoices(json, "unpaidInvoices")
                        )

                        // Mark initial sync complete
                        userPreferences.setP2PInitialSyncDone(true)
                        userPreferences.setInitialDataSeeded(true)

                        // Respond ACK to Host
                        val ack = P2PJsonCodec.createSnapshotAck(deviceId, totalCount)
                        fromConn.writer.println(ack.toString())

                    } finally {
                        isApplyingRemoteSync = false
                    }

                    _syncStatus.value = _syncStatus.value.copy(
                        state = P2PConnectionState.CONNECTED,
                        isInitialSyncDone = true,
                        lastSyncTimestamp = System.currentTimeMillis(),
                        syncedItemsCount = totalCount,
                        lastEventMessage = "Dados originais sincronizados com sucesso ($totalCount registros)."
                    )
                }

                "SNAPSHOT_ACK" -> {
                    val count = json.optInt("count", 0)
                    _syncStatus.value = _syncStatus.value.copy(
                        state = P2PConnectionState.CONNECTED,
                        isInitialSyncDone = true,
                        lastSyncTimestamp = System.currentTimeMillis(),
                        lastEventMessage = "Dados originais espelhados no outro aparelho com sucesso ($count registros)."
                    )
                }

                "SYNC_REQ" -> {
                    // Send our full dataset to peer without requiring a response
                    sendSyncDataToConnection(fromConn, requiresResponse = false)
                }

                "SYNC_RESP" -> {
                    handleSyncResponse(json, fromConn)
                }

                "TX_UPSERT" -> {
                    val txJson = json.optJSONObject("transaction")
                    if (txJson != null) {
                        val incomingTx = P2PJsonCodec.jsonToTransaction(txJson)
                        val cardSyncUuid = P2PJsonCodec.extractCardSyncUuid(txJson)
                        isApplyingRemoteSync = true
                        try {
                            reconcileTransaction(incomingTx, cardSyncUuid)
                        } finally {
                            isApplyingRemoteSync = false
                        }
                        _syncStatus.value = _syncStatus.value.copy(
                            lastSyncTimestamp = System.currentTimeMillis(),
                            lastEventMessage = "Lançamento recebido: ${incomingTx.title}"
                        )
                    }
                }

                "TX_DELETE" -> {
                    val syncUuid = json.optString("syncUuid")
                    val groupId = if (json.isNull("groupId")) null else json.optString("groupId")
                    isApplyingRemoteSync = true
                    try {
                        if (!groupId.isNullOrBlank()) {
                            database.transactionDao().markInstallmentGroupDeleted(groupId)
                        } else if (syncUuid.isNotBlank()) {
                            val existing = database.transactionDao().getBySyncUuid(syncUuid)
                            if (existing != null) {
                                database.transactionDao().update(
                                    existing.copy(isDeleted = true, updatedAt = System.currentTimeMillis())
                                )
                            }
                        }
                    } finally {
                        isApplyingRemoteSync = false
                    }
                    _syncStatus.value = _syncStatus.value.copy(
                        lastSyncTimestamp = System.currentTimeMillis(),
                        lastEventMessage = "Lançamento excluído remotamente"
                    )
                }

                "CARD_UPSERT" -> {
                    val cardJson = json.optJSONObject("card")
                    if (cardJson != null) {
                        val incomingCard = P2PJsonCodec.jsonToCard(cardJson)
                        isApplyingRemoteSync = true
                        try {
                            reconcileCard(incomingCard)
                        } finally {
                            isApplyingRemoteSync = false
                        }
                        _syncStatus.value = _syncStatus.value.copy(
                            lastSyncTimestamp = System.currentTimeMillis(),
                            lastEventMessage = "Cartão atualizado: ${incomingCard.name}"
                        )
                    }
                }

                "CARD_DELETE" -> {
                    val syncUuid = json.optString("syncUuid")
                    if (syncUuid.isNotBlank()) {
                        isApplyingRemoteSync = true
                        try {
                            val existing = database.creditCardDao().getBySyncUuid(syncUuid)
                            if (existing != null) {
                                database.creditCardDao().updateCard(
                                    existing.copy(isDeleted = true, updatedAt = System.currentTimeMillis())
                                )
                            }
                        } finally {
                            isApplyingRemoteSync = false
                        }
                    }
                }

                "CAT_UPSERT" -> {
                    val catJson = json.optJSONObject("category")
                    if (catJson != null) {
                        val incomingCat = P2PJsonCodec.jsonToCategory(catJson)
                        isApplyingRemoteSync = true
                        try {
                            reconcileCategory(incomingCat)
                        } finally {
                            isApplyingRemoteSync = false
                        }
                    }
                }

                "CAT_DELETE" -> {
                    val syncUuid = json.optString("syncUuid")
                    if (syncUuid.isNotBlank()) {
                        isApplyingRemoteSync = true
                        try {
                            val existing = database.customCategoryDao().getBySyncUuid(syncUuid)
                            if (existing != null) {
                                database.customCategoryDao().updateCategory(
                                    existing.copy(isDeleted = true, updatedAt = System.currentTimeMillis())
                                )
                            }
                        } finally {
                            isApplyingRemoteSync = false
                        }
                    }
                }

                "CLEAR_ALL" -> {
                    isApplyingRemoteSync = true
                    try {
                        database.transactionDao().deleteAllTransactions()
                        database.creditCardDao().deleteAllCards()
                        database.customCategoryDao().deleteAllCategories()
                    } finally {
                        isApplyingRemoteSync = false
                    }
                    _syncStatus.value = _syncStatus.value.copy(
                        lastSyncTimestamp = System.currentTimeMillis(),
                        lastEventMessage = "Dados limpos remotamente por par P2P"
                    )
                }

                "PING" -> {
                    fromConn.writer.println(JSONObject().apply { put("type", "PONG") }.toString())
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Erro ao tratar mensagem P2P", e)
        }
    }

    private suspend fun sendSnapshotOverwriteToConnection(conn: PeerConnection) = withContext(Dispatchers.IO) {
        try {
            _syncStatus.value = _syncStatus.value.copy(
                lastEventMessage = "Enviando dados originais para ${conn.device.name}..."
            )
            val txList = database.transactionDao().getAllRawTransactions()
            val cardList = database.creditCardDao().getAllRawCards()
            val catList = database.customCategoryDao().getAllRawCategories()
            val cardUuidMap = cardList.associate { it.id to it.syncUuid }

            val paidInvoicesMapped = userPreferences.paidInvoices.value.mapNotNull { key ->
                val parts = key.split("_")
                if (parts.size == 3) {
                    val cardId = parts[0].toLongOrNull()
                    val cardSyncUuid = cardId?.let { cardUuidMap[it] }
                    if (cardSyncUuid != null) "${cardSyncUuid}_${parts[1]}_${parts[2]}" else null
                } else null
            }.toSet()

            val unpaidInvoicesMapped = userPreferences.unpaidInvoices.value.mapNotNull { key ->
                val parts = key.split("_")
                if (parts.size == 3) {
                    val cardId = parts[0].toLongOrNull()
                    val cardSyncUuid = cardId?.let { cardUuidMap[it] }
                    if (cardSyncUuid != null) "${cardSyncUuid}_${parts[1]}_${parts[2]}" else null
                } else null
            }.toSet()

            val snapshot = P2PJsonCodec.createSnapshotOverwrite(
                senderId = deviceId,
                transactions = txList,
                cards = cardList,
                categories = catList,
                cardSyncUuidMap = cardUuidMap,
                paidInvoices = paidInvoicesMapped,
                unpaidInvoices = unpaidInvoicesMapped
            )
            conn.writer.println(snapshot.toString())
        } catch (e: Exception) {
            Log.e(tag, "Erro ao enviar snapshot overwrite", e)
        }
    }

    private suspend fun handleSyncResponse(json: JSONObject, fromConn: PeerConnection) {
        _syncStatus.value = _syncStatus.value.copy(
            state = P2PConnectionState.SYNCING,
            lastEventMessage = "Sincronizando atualizações com ${fromConn.device.name}..."
        )

        isApplyingRemoteSync = true
        var syncedCount = 0
        try {
            // 1. Reconcile Credit Cards first
            val cardsArray = json.optJSONArray("cards")
            if (cardsArray != null) {
                for (i in 0 until cardsArray.length()) {
                    val cardJson = cardsArray.getJSONObject(i)
                    val incomingCard = P2PJsonCodec.jsonToCard(cardJson)
                    reconcileCard(incomingCard)
                    syncedCount++
                }
            }

            // 2. Reconcile Custom Categories second
            val catArray = json.optJSONArray("categories")
            if (catArray != null) {
                for (i in 0 until catArray.length()) {
                    val catJson = catArray.getJSONObject(i)
                    val incomingCat = P2PJsonCodec.jsonToCategory(catJson)
                    reconcileCategory(incomingCat)
                    syncedCount++
                }
            }

            // 3. Reconcile Transactions third (with mapped card IDs)
            val txArray = json.optJSONArray("transactions")
            if (txArray != null) {
                for (i in 0 until txArray.length()) {
                    val txJson = txArray.getJSONObject(i)
                    val incomingTx = P2PJsonCodec.jsonToTransaction(txJson)
                    val cardSyncUuid = P2PJsonCodec.extractCardSyncUuid(txJson)
                    reconcileTransaction(incomingTx, cardSyncUuid)
                    syncedCount++
                }
            }

            // 4. Reconcile Invoices
            reconcileInvoices(
                paidInvoices = P2PJsonCodec.extractInvoices(json, "paidInvoices"),
                unpaidInvoices = P2PJsonCodec.extractInvoices(json, "unpaidInvoices")
            )

            userPreferences.setP2PInitialSyncDone(true)
            userPreferences.setInitialDataSeeded(true)
        } finally {
            isApplyingRemoteSync = false
        }

        val requiresResponse = json.optBoolean("requiresResponse", false)
        if (requiresResponse) {
            // Send back our reconciled state so the peer also receives all our offline adjustments!
            sendSyncDataToConnection(fromConn, requiresResponse = false)
        }

        _syncStatus.value = _syncStatus.value.copy(
            state = P2PConnectionState.CONNECTED,
            isInitialSyncDone = true,
            lastSyncTimestamp = System.currentTimeMillis(),
            syncedItemsCount = _syncStatus.value.syncedItemsCount + syncedCount,
            lastEventMessage = "Sincronização bidirecional concluída ($syncedCount registros)."
        )
    }

    private suspend fun sendSyncDataToConnection(conn: PeerConnection, requiresResponse: Boolean) = withContext(Dispatchers.IO) {
        try {
            val txList = database.transactionDao().getAllRawTransactions()
            val cardList = database.creditCardDao().getAllRawCards()
            val catList = database.customCategoryDao().getAllRawCategories()
            val cardUuidMap = cardList.associate { it.id to it.syncUuid }

            val paidInvoicesMapped = userPreferences.paidInvoices.value.mapNotNull { key ->
                val parts = key.split("_")
                if (parts.size == 3) {
                    val cardId = parts[0].toLongOrNull()
                    val cardSyncUuid = cardId?.let { cardUuidMap[it] }
                    if (cardSyncUuid != null) "${cardSyncUuid}_${parts[1]}_${parts[2]}" else null
                } else null
            }.toSet()

            val unpaidInvoicesMapped = userPreferences.unpaidInvoices.value.mapNotNull { key ->
                val parts = key.split("_")
                if (parts.size == 3) {
                    val cardId = parts[0].toLongOrNull()
                    val cardSyncUuid = cardId?.let { cardUuidMap[it] }
                    if (cardSyncUuid != null) "${cardSyncUuid}_${parts[1]}_${parts[2]}" else null
                } else null
            }.toSet()

            val resp = P2PJsonCodec.createSyncResponse(
                senderId = deviceId,
                transactions = txList,
                cards = cardList,
                categories = catList,
                cardSyncUuidMap = cardUuidMap,
                paidInvoices = paidInvoicesMapped,
                unpaidInvoices = unpaidInvoicesMapped,
                requiresResponse = requiresResponse
            )
            conn.writer.println(resp.toString())
        } catch (e: Exception) {
            Log.e(tag, "Erro ao enviar dados de sincronização para ${conn.device.name}", e)
        }
    }

    private suspend fun reconcileInvoices(paidInvoices: Set<String>, unpaidInvoices: Set<String>) {
        if (paidInvoices.isEmpty() && unpaidInvoices.isEmpty()) return
        val allCards = database.creditCardDao().getAllRawCards()
        val cardMap = allCards.associateBy { it.syncUuid }

        paidInvoices.forEach { item ->
            val parts = item.split("_")
            if (parts.size == 3) {
                val cardSyncUuid = parts[0]
                val year = parts[1].toIntOrNull()
                val month = parts[2].toIntOrNull()
                val card = cardMap[cardSyncUuid]
                if (card != null && year != null && month != null) {
                    userPreferences.setInvoicePaid(card.id, year, month, true)
                }
            }
        }

        unpaidInvoices.forEach { item ->
            val parts = item.split("_")
            if (parts.size == 3) {
                val cardSyncUuid = parts[0]
                val year = parts[1].toIntOrNull()
                val month = parts[2].toIntOrNull()
                val card = cardMap[cardSyncUuid]
                if (card != null && year != null && month != null) {
                    userPreferences.setInvoicePaid(card.id, year, month, false)
                }
            }
        }
    }

    private suspend fun reconcileTransaction(incoming: TransactionEntity, cardSyncUuid: String? = null) {
        val resolvedCardId = if (!cardSyncUuid.isNullOrBlank()) {
            val card = database.creditCardDao().getBySyncUuid(cardSyncUuid)
            card?.id
        } else {
            null
        }
        val toSave = incoming.copy(cardId = resolvedCardId)
        val existing = database.transactionDao().getBySyncUuid(toSave.syncUuid)
        if (existing != null) {
            if (toSave.updatedAt >= existing.updatedAt) {
                database.transactionDao().update(
                    toSave.copy(id = existing.id)
                )
            }
        } else {
            database.transactionDao().insert(
                toSave.copy(id = 0)
            )
        }
    }

    private suspend fun reconcileCard(incoming: CreditCardEntity) {
        val existing = database.creditCardDao().getBySyncUuid(incoming.syncUuid)
        if (existing != null) {
            if (incoming.updatedAt >= existing.updatedAt) {
                database.creditCardDao().updateCard(
                    incoming.copy(id = existing.id)
                )
            }
        } else {
            database.creditCardDao().insertCard(
                incoming.copy(id = 0)
            )
        }
    }

    private suspend fun reconcileCategory(incoming: CustomCategoryEntity) {
        val existing = database.customCategoryDao().getBySyncUuid(incoming.syncUuid)
        if (existing != null) {
            if (incoming.updatedAt >= existing.updatedAt) {
                database.customCategoryDao().updateCategory(
                    incoming.copy(id = existing.id)
                )
            }
        } else {
            database.customCategoryDao().insertCategory(
                incoming.copy(id = 0)
            )
        }
    }

    // Live Broadcast Emitter functions
    fun broadcastTransactionUpsert(tx: TransactionEntity) {
        if (!isSyncActive() || isApplyingRemoteSync) return
        scope.launch(Dispatchers.IO) {
            val cardSyncUuid = tx.cardId?.let { database.creditCardDao().getCardById(it)?.syncUuid }
            val json = P2PJsonCodec.createTxUpsert(deviceId, tx, cardSyncUuid)
            broadcast(json.toString())
        }
    }

    fun broadcastTransactionDelete(syncUuid: String, groupId: String? = null) {
        if (!isSyncActive() || isApplyingRemoteSync) return
        val json = P2PJsonCodec.createTxDelete(deviceId, syncUuid, groupId)
        broadcast(json.toString())
    }

    fun broadcastCardUpsert(card: CreditCardEntity) {
        if (!isSyncActive() || isApplyingRemoteSync) return
        val json = P2PJsonCodec.createCardUpsert(deviceId, card)
        broadcast(json.toString())
    }

    fun broadcastCardDelete(syncUuid: String) {
        if (!isSyncActive() || isApplyingRemoteSync) return
        val json = P2PJsonCodec.createCardDelete(deviceId, syncUuid)
        broadcast(json.toString())
    }

    fun broadcastCategoryUpsert(cat: CustomCategoryEntity) {
        if (!isSyncActive() || isApplyingRemoteSync) return
        val json = P2PJsonCodec.createCategoryUpsert(deviceId, cat)
        broadcast(json.toString())
    }

    fun broadcastCategoryDelete(syncUuid: String) {
        if (!isSyncActive() || isApplyingRemoteSync) return
        val json = P2PJsonCodec.createCategoryDelete(deviceId, syncUuid)
        broadcast(json.toString())
    }

    fun broadcastClearAll() {
        if (!isSyncActive() || isApplyingRemoteSync) return
        val json = P2PJsonCodec.createClearAll(deviceId)
        broadcast(json.toString())
    }

    fun triggerFullSyncNow() {
        if (!isSyncActive()) return
        scope.launch {
            _syncStatus.value = _syncStatus.value.copy(
                state = P2PConnectionState.SYNCING,
                lastEventMessage = "Sincronizando com aparelhos conectados..."
            )
            activeConnections.values.forEach { conn ->
                sendSyncDataToConnection(conn, requiresResponse = true)
            }
        }
    }

    private fun broadcast(message: String) {
        scope.launch(Dispatchers.IO) {
            activeConnections.values.forEach { conn ->
                try {
                    conn.writer.println(message)
                } catch (e: Exception) {
                    Log.e(tag, "Falha ao enviar mensagem para ${conn.device.name}", e)
                }
            }
        }
    }

    private fun updateConnectedPeersState() {
        val peers = activeConnections.values.map { it.device }
        val isConn = peers.isNotEmpty()
        val newState = if (isConn) P2PConnectionState.CONNECTED else {
            if (_syncStatus.value.isEnabled) P2PConnectionState.SEARCHING else P2PConnectionState.DISABLED
        }

        _syncStatus.value = _syncStatus.value.copy(
            state = newState,
            connectedPeers = peers,
            lastEventMessage = if (isConn) {
                "${peers.size} dispositivo(s) conectado(s) via P2P"
            } else {
                if (_syncStatus.value.role == P2PRole.HOST) {
                    "Aguardando conexão de outros aparelhos na rede Wi-Fi..."
                } else {
                    "Buscando aparelho principal na rede local..."
                }
            }
        )
    }

    // NSD (Network Service Discovery)
    private fun startNsdAdvertisement(syncKey: String) {
        val keyHash = hashKey(syncKey)
        val port = serverSocket?.localPort ?: return
        val rolePrefix = if (_syncStatus.value.role == P2PRole.HOST) "H" else "C"

        val serviceInfo = NsdServiceInfo().apply {
            serviceName = "FinanFlow_${keyHash.take(8)}_${rolePrefix}_${deviceId.take(4)}"
            serviceType = this@P2PSyncManager.serviceType
            setPort(port)
        }

        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(NsdServiceInfo: NsdServiceInfo) {
                Log.d(tag, "Serviço NSD P2P registrado: ${NsdServiceInfo.serviceName}")
            }

            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e(tag, "Falha no registro NSD: $errorCode")
            }

            override fun onServiceUnregistered(arg0: NsdServiceInfo) {
                Log.d(tag, "Serviço NSD P2P cancelado")
            }

            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e(tag, "Falha ao cancelar registro NSD: $errorCode")
            }
        }

        try {
            nsdManager?.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        } catch (e: Exception) {
            Log.e(tag, "Erro ao registrar serviço NSD", e)
        }
    }

    private fun stopNsdAdvertisement() {
        registrationListener?.let {
            try {
                nsdManager?.unregisterService(it)
            } catch (e: Exception) {
                Log.e(tag, "Erro ao desregistrar NSD", e)
            }
            registrationListener = null
        }
    }

    private fun startNsdDiscovery(syncKey: String) {
        val targetKeyHash = hashKey(syncKey).take(8)

        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.d(tag, "Descoberta de pares P2P iniciada")
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                Log.d(tag, "Serviço encontrado: ${service.serviceName}")
                val name = service.serviceName
                if (name.startsWith("FinanFlow_$targetKeyHash") && !name.endsWith("_${deviceId.take(4)}")) {
                    resolveAndConnect(service, syncKey)
                }
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                Log.d(tag, "Serviço perdido: ${service.serviceName}")
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.d(tag, "Descoberta de pares P2P parada")
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(tag, "Falha ao iniciar descoberta: $errorCode")
                stopNsdDiscovery()
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(tag, "Falha ao parar descoberta: $errorCode")
            }
        }

        try {
            nsdManager?.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            Log.e(tag, "Erro ao iniciar descoberta NSD", e)
        }
    }

    private fun resolveAndConnect(service: NsdServiceInfo, syncKey: String) {
        val resolveListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e(tag, "Falha ao resolver serviço: $errorCode")
            }

            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                val host = serviceInfo.host?.hostAddress
                val port = serviceInfo.port
                if (host != null && port > 0) {
                    scope.launch {
                        val alreadyConnected = activeConnections.values.any { it.device.ip == host }
                        if (!alreadyConnected) {
                            connectToPeer(host, port, syncKey)
                        }
                    }
                }
            }
        }

        try {
            nsdManager?.resolveService(service, resolveListener)
        } catch (e: Exception) {
            Log.e(tag, "Erro ao resolver serviço NSD", e)
        }
    }

    private fun stopNsdDiscovery() {
        discoveryListener?.let {
            try {
                nsdManager?.stopServiceDiscovery(it)
            } catch (e: Exception) {
                Log.e(tag, "Erro ao parar busca NSD", e)
            }
            discoveryListener = null
        }
    }

    private fun startKeepAliveLoop() {
        scope.launch {
            while (isActive && _syncStatus.value.isEnabled) {
                delay(15000)
                if (activeConnections.isNotEmpty()) {
                    val ping = JSONObject().apply { put("type", "PING") }.toString()
                    broadcast(ping)
                }
            }
        }
    }

    private fun closeAllConnections() {
        activeConnections.values.forEach { it.close() }
        activeConnections.clear()
        updateConnectedPeersState()
    }

    fun isSyncActive(): Boolean = _syncStatus.value.isEnabled && activeConnections.isNotEmpty()

    private fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val intf = interfaces.nextElement()
                if (intf.isLoopback || !intf.isUp) continue
                val addrs = intf.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Erro ao obter IP local", e)
        }
        return "127.0.0.1"
    }

    private class PeerConnection(
        val socket: Socket,
        val reader: BufferedReader,
        val writer: PrintWriter,
        val device: P2PDevice
    ) {
        fun close() {
            try {
                socket.close()
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}
