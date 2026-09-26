package com.example.data.webserver

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.CreditCardEntity
import com.example.data.local.TransactionEntity
import com.example.data.model.Categories
import com.example.data.model.TransactionType
import com.example.data.preferences.UserPreferences
import com.example.data.repository.CategoryRepository
import com.example.data.repository.CreditCardRepository
import com.example.data.repository.TransactionRepository
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
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

class WebDesktopServer(
    private val context: Context,
    private val database: AppDatabase,
    private val repository: TransactionRepository,
    private val cardRepository: CreditCardRepository,
    private val categoryRepository: CategoryRepository,
    private val userPreferences: UserPreferences
) {
    private val tag = "WebDesktopServer"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val deviceName: String = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _serverPort = MutableStateFlow(8080)
    val serverPort: StateFlow<Int> = _serverPort.asStateFlow()

    private val _serverUrl = MutableStateFlow("")
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _connectedClientsCount = MutableStateFlow(0)
    val connectedClientsCount: StateFlow<Int> = _connectedClientsCount.asStateFlow()

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private var ssePingJob: Job? = null

    private val validTokens = Collections.synchronizedSet(mutableSetOf<String>())
    private val activeSseWriters = CopyOnWriteArrayList<PrintWriter>()

    fun startServer(desiredPort: Int = userPreferences.webServerPort.value) {
        if (_isRunning.value) return

        scope.launch {
            try {
                // Find available port starting from desiredPort
                var port = desiredPort
                var createdSocket: ServerSocket? = null
                for (p in port..(port + 20)) {
                    try {
                        createdSocket = ServerSocket(p)
                        port = p
                        break
                    } catch (e: Exception) {
                        // try next port
                    }
                }

                if (createdSocket == null) {
                    createdSocket = ServerSocket(0)
                    port = createdSocket.localPort
                }

                serverSocket = createdSocket
                _serverPort.value = port
                val localIp = getLocalIpAddress()
                _serverUrl.value = "http://$localIp:$port"
                _isRunning.value = true

                Log.d(tag, "Servidor Web PC iniciado em ${_serverUrl.value}")

                startSsePingLoop()

                serverJob = scope.launch {
                    while (isActive && !createdSocket.isClosed) {
                        try {
                            val clientSocket = createdSocket.accept()
                            scope.launch {
                                handleHttpClient(clientSocket)
                            }
                        } catch (e: Exception) {
                            if (!createdSocket.isClosed) {
                                Log.e(tag, "Erro no accept do servidor web", e)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Falha ao iniciar Servidor Web Desktop", e)
                _isRunning.value = false
            }
        }
    }

    fun stopServer() {
        try {
            _isRunning.value = false
            serverJob?.cancel()
            serverJob = null
            ssePingJob?.cancel()
            ssePingJob = null

            activeSseWriters.forEach {
                try { it.close() } catch (ignored: Exception) {}
            }
            activeSseWriters.clear()
            _connectedClientsCount.value = 0

            serverSocket?.close()
            serverSocket = null
            Log.d(tag, "Servidor Web PC finalizado")
        } catch (e: Exception) {
            Log.e(tag, "Erro ao parar servidor web", e)
        }
    }

    fun broadcastUpdate(eventType: String = "SYNC") {
        if (!_isRunning.value || activeSseWriters.isEmpty()) return

        scope.launch {
            val deadWriters = mutableListOf<PrintWriter>()
            val payload = JSONObject().apply {
                put("type", eventType)
                put("timestamp", System.currentTimeMillis())
            }
            val sseData = "event: update\ndata: $payload\n\n"

            for (writer in activeSseWriters) {
                try {
                    writer.print(sseData)
                    writer.flush()
                    if (writer.checkError()) {
                        deadWriters.add(writer)
                    }
                } catch (e: Exception) {
                    deadWriters.add(writer)
                }
            }

            if (deadWriters.isNotEmpty()) {
                activeSseWriters.removeAll(deadWriters)
                _connectedClientsCount.value = activeSseWriters.size
            }
        }
    }

    private fun startSsePingLoop() {
        ssePingJob?.cancel()
        ssePingJob = scope.launch {
            while (isActive && _isRunning.value) {
                delay(15000)
                if (activeSseWriters.isNotEmpty()) {
                    val dead = mutableListOf<PrintWriter>()
                    for (writer in activeSseWriters) {
                        try {
                            writer.print(": ping\n\n")
                            writer.flush()
                            if (writer.checkError()) dead.add(writer)
                        } catch (e: Exception) {
                            dead.add(writer)
                        }
                    }
                    if (dead.isNotEmpty()) {
                        activeSseWriters.removeAll(dead)
                        _connectedClientsCount.value = activeSseWriters.size
                    }
                }
            }
        }
    }

    private suspend fun handleHttpClient(socket: Socket) = withContext(Dispatchers.IO) {
        try {
            val inputStream = socket.getInputStream()
            val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
            val outputStream = socket.getOutputStream()
            val writer = PrintWriter(outputStream, false, Charsets.UTF_8)

            val requestLine = reader.readLine() ?: return@withContext
            val parts = requestLine.split(" ")
            if (parts.size < 2) return@withContext

            val method = parts[0].uppercase()
            val rawPath = parts[1]
            val path = rawPath.substringBefore("?")
            val queryString = if (rawPath.contains("?")) rawPath.substringAfter("?") else ""
            val queryParams = parseQueryParams(queryString)

            var contentLength = 0
            var authHeader = ""

            // Read Headers
            var headerLine: String? = reader.readLine()
            while (!headerLine.isNullOrBlank()) {
                val colonIdx = headerLine.indexOf(':')
                if (colonIdx > 0) {
                    val key = headerLine.substring(0, colonIdx).trim().lowercase()
                    val value = headerLine.substring(colonIdx + 1).trim()
                    if (key == "content-length") {
                        contentLength = value.toIntOrNull() ?: 0
                    } else if (key == "authorization") {
                        authHeader = value
                    }
                }
                headerLine = reader.readLine()
            }

            // Read Body if present
            val bodyBuilder = StringBuilder()
            if (contentLength > 0) {
                val buffer = CharArray(1024)
                var bytesReadTotal = 0
                while (bytesReadTotal < contentLength) {
                    val toRead = minOf(buffer.size, contentLength - bytesReadTotal)
                    val read = reader.read(buffer, 0, toRead)
                    if (read == -1) break
                    bodyBuilder.append(buffer, 0, read)
                    bytesReadTotal += read
                }
            }
            val body = bodyBuilder.toString()

            // CORS Preflight
            if (method == "OPTIONS") {
                sendCorsPreflight(writer)
                socket.close()
                return@withContext
            }

            // Route matching
            when {
                // Main HTML page
                (method == "GET" && (path == "/" || path == "/index.html")) -> {
                    val html = WebDesktopAppHtml.getHtml()
                    sendResponse(writer, 200, "OK", "text/html; charset=utf-8", html)
                    socket.close()
                }

                // Public Status API
                (method == "GET" && path == "/api/status") -> {
                    val res = JSONObject().apply {
                        put("status", "ONLINE")
                        put("deviceName", deviceName)
                        put("ip", getLocalIpAddress())
                        put("port", _serverPort.value)
                        put("clients", _connectedClientsCount.value)
                    }
                    sendResponse(writer, 200, "OK", "application/json", res.toString())
                    socket.close()
                }

                // PIN Authentication
                (method == "POST" && path == "/api/auth") -> {
                    val json = try { JSONObject(body) } catch (e: Exception) { JSONObject() }
                    val pin = json.optString("pin", "").trim()
                    val expectedPin = userPreferences.webServerPin.value.trim()

                    if (pin == expectedPin) {
                        val token = UUID.randomUUID().toString()
                        validTokens.add(token)
                        val res = JSONObject().apply {
                            put("success", true)
                            put("token", token)
                            put("deviceName", deviceName)
                        }
                        sendResponse(writer, 200, "OK", "application/json", res.toString())
                    } else {
                        val res = JSONObject().apply {
                            put("success", false)
                            put("error", "Código PIN incorreto.")
                        }
                        sendResponse(writer, 401, "Unauthorized", "application/json", res.toString())
                    }
                    socket.close()
                }

                // Server-Sent Events (SSE) Stream
                (method == "GET" && path == "/api/events") -> {
                    val token = queryParams["token"] ?: authHeader.removePrefix("Bearer ").trim()
                    if (!isAuthorized(token, queryParams["pin"])) {
                        sendResponse(writer, 401, "Unauthorized", "application/json", "{\"error\":\"Unauthorized\"}")
                        socket.close()
                        return@withContext
                    }

                    // Keep socket open for SSE
                    writer.print("HTTP/1.1 200 OK\r\n")
                    writer.print("Content-Type: text/event-stream; charset=utf-8\r\n")
                    writer.print("Cache-Control: no-cache\r\n")
                    writer.print("Connection: keep-alive\r\n")
                    writer.print("Access-Control-Allow-Origin: *\r\n\r\n")
                    writer.print("event: connected\ndata: {\"status\":\"connected\"}\n\n")
                    writer.flush()

                    activeSseWriters.add(writer)
                    _connectedClientsCount.value = activeSseWriters.size
                    // Do not close socket here, keeps streaming!
                }

                // Authenticated GET Data API
                (method == "GET" && path == "/api/data") -> {
                    val token = queryParams["token"] ?: authHeader.removePrefix("Bearer ").trim()
                    if (!isAuthorized(token, queryParams["pin"])) {
                        sendResponse(writer, 401, "Unauthorized", "application/json", "{\"error\":\"Unauthorized\"}")
                        socket.close()
                        return@withContext
                    }

                    val data = fetchFullFinancialDataJson()
                    sendResponse(writer, 200, "OK", "application/json", data.toString())
                    socket.close()
                }

                // Add Transaction
                (method == "POST" && path == "/api/transactions") -> {
                    val token = queryParams["token"] ?: authHeader.removePrefix("Bearer ").trim()
                    if (!isAuthorized(token, queryParams["pin"])) {
                        sendResponse(writer, 401, "Unauthorized", "application/json", "{\"error\":\"Unauthorized\"}")
                        socket.close()
                        return@withContext
                    }

                    val json = JSONObject(body)
                    val result = handleCreateTransaction(json)
                    sendResponse(writer, 200, "OK", "application/json", result.toString())
                    broadcastUpdate("TRANSACTION_CREATED")
                    socket.close()
                }

                // Update Transaction
                (method == "PUT" && path.startsWith("/api/transactions/")) -> {
                    val token = queryParams["token"] ?: authHeader.removePrefix("Bearer ").trim()
                    if (!isAuthorized(token, queryParams["pin"])) {
                        sendResponse(writer, 401, "Unauthorized", "application/json", "{\"error\":\"Unauthorized\"}")
                        socket.close()
                        return@withContext
                    }

                    val id = path.removePrefix("/api/transactions/").toLongOrNull()
                    if (id != null) {
                        val json = JSONObject(body)
                        val result = handleUpdateTransaction(id, json)
                        sendResponse(writer, 200, "OK", "application/json", result.toString())
                        broadcastUpdate("TRANSACTION_UPDATED")
                    } else {
                        sendResponse(writer, 400, "Bad Request", "application/json", "{\"error\":\"ID inválido\"}")
                    }
                    socket.close()
                }

                // Delete Transaction
                (method == "DELETE" && path.startsWith("/api/transactions/")) -> {
                    val token = queryParams["token"] ?: authHeader.removePrefix("Bearer ").trim()
                    if (!isAuthorized(token, queryParams["pin"])) {
                        sendResponse(writer, 401, "Unauthorized", "application/json", "{\"error\":\"Unauthorized\"}")
                        socket.close()
                        return@withContext
                    }

                    val id = path.removePrefix("/api/transactions/").toLongOrNull()
                    if (id != null) {
                        repository.deleteById(id)
                        sendResponse(writer, 200, "OK", "application/json", "{\"success\":true}")
                        broadcastUpdate("TRANSACTION_DELETED")
                    } else {
                        sendResponse(writer, 400, "Bad Request", "application/json", "{\"error\":\"ID inválido\"}")
                    }
                    socket.close()
                }

                // Add Card
                (method == "POST" && path == "/api/cards") -> {
                    val token = queryParams["token"] ?: authHeader.removePrefix("Bearer ").trim()
                    if (!isAuthorized(token, queryParams["pin"])) {
                        sendResponse(writer, 401, "Unauthorized", "application/json", "{\"error\":\"Unauthorized\"}")
                        socket.close()
                        return@withContext
                    }

                    val json = JSONObject(body)
                    val card = CreditCardEntity(
                        name = json.optString("name", "Novo Cartão").trim(),
                        lastFourDigits = json.optString("lastFourDigits", "").trim(),
                        colorHex = json.optString("colorHex", "#8A05BE"),
                        limitAmount = json.optDouble("limitAmount", 0.0),
                        closingDay = json.optInt("closingDay", 5),
                        dueDay = json.optInt("dueDay", 12)
                    )
                    val newId = cardRepository.insert(card)
                    sendResponse(writer, 200, "OK", "application/json", "{\"success\":true,\"id\":$newId}")
                    broadcastUpdate("CARD_CREATED")
                    socket.close()
                }

                // Export CSV
                (method == "GET" && path == "/api/export/csv") -> {
                    val token = queryParams["token"] ?: authHeader.removePrefix("Bearer ").trim()
                    if (!isAuthorized(token, queryParams["pin"])) {
                        sendResponse(writer, 401, "Unauthorized", "application/json", "{\"error\":\"Unauthorized\"}")
                        socket.close()
                        return@withContext
                    }

                    val csv = generateCsvExport()
                    writer.print("HTTP/1.1 200 OK\r\n")
                    writer.print("Content-Type: text/csv; charset=utf-8\r\n")
                    writer.print("Content-Disposition: attachment; filename=\"bumoney_transacoes.csv\"\r\n")
                    writer.print("Access-Control-Allow-Origin: *\r\n")
                    val bytes = csv.toByteArray(Charsets.UTF_8)
                    writer.print("Content-Length: ${bytes.size}\r\n\r\n")
                    writer.print(csv)
                    writer.flush()
                    socket.close()
                }

                // Export JSON
                (method == "GET" && path == "/api/export/json") -> {
                    val token = queryParams["token"] ?: authHeader.removePrefix("Bearer ").trim()
                    if (!isAuthorized(token, queryParams["pin"])) {
                        sendResponse(writer, 401, "Unauthorized", "application/json", "{\"error\":\"Unauthorized\"}")
                        socket.close()
                        return@withContext
                    }

                    val jsonDump = fetchFullFinancialDataJson().toString(2)
                    writer.print("HTTP/1.1 200 OK\r\n")
                    writer.print("Content-Type: application/json; charset=utf-8\r\n")
                    writer.print("Content-Disposition: attachment; filename=\"bumoney_backup.json\"\r\n")
                    writer.print("Access-Control-Allow-Origin: *\r\n")
                    val bytes = jsonDump.toByteArray(Charsets.UTF_8)
                    writer.print("Content-Length: ${bytes.size}\r\n\r\n")
                    writer.print(jsonDump)
                    writer.flush()
                    socket.close()
                }

                else -> {
                    sendResponse(writer, 404, "Not Found", "application/json", "{\"error\":\"Endpoint não encontrado\"}")
                    socket.close()
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Erro ao processar requisição HTTP", e)
            try { socket.close() } catch (ignored: Exception) {}
        }
    }

    private fun isAuthorized(token: String?, queryPin: String?): Boolean {
        if (!queryPin.isNullOrBlank() && queryPin.trim() == userPreferences.webServerPin.value.trim()) {
            return true
        }
        if (!token.isNullOrBlank() && validTokens.contains(token)) {
            return true
        }
        return false
    }

    private suspend fun fetchFullFinancialDataJson(): JSONObject = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("deviceName", deviceName)
        root.put("timestamp", System.currentTimeMillis())

        // Preferences
        val prefs = JSONObject().apply {
            put("monthlyBudget", userPreferences.monthlyBudgetLimit.value)
            put("hideBalances", userPreferences.hideBalances.value)
            put("currency", userPreferences.defaultCurrency.value)
            put("themeColor", userPreferences.themeColor.value.name)
        }
        root.put("preferences", prefs)

        // Transactions
        val rawTxs = database.transactionDao().getAllRawTransactions().filter { !it.isDeleted }
        val txsArray = JSONArray()
        for (t in rawTxs) {
            val obj = JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                put("amount", t.amount)
                put("type", t.type)
                put("category", t.category)
                put("timestamp", t.timestamp)
                put("note", t.note)
                put("cardId", t.cardId ?: JSONObject.NULL)
                put("isInstallment", t.isInstallment)
                put("installmentNumber", t.installmentNumber)
                put("totalInstallments", t.totalInstallments)
                put("isRecurring", t.isRecurring)
                put("syncUuid", t.syncUuid)
            }
            txsArray.put(obj)
        }
        root.put("transactions", txsArray)

        // Cards
        val rawCards = database.creditCardDao().getAllCardsSync()
        val cardsArray = JSONArray()
        for (c in rawCards) {
            val obj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("lastFourDigits", c.lastFourDigits)
                put("colorHex", c.colorHex)
                put("limitAmount", c.limitAmount)
                put("closingDay", c.closingDay)
                put("dueDay", c.dueDay)
            }
            cardsArray.put(obj)
        }
        root.put("cards", cardsArray)

        // Categories
        val catsArray = JSONArray()
        // Default categories
        for (cat in Categories.expenseCategories + Categories.incomeCategories) {
            val obj = JSONObject().apply {
                put("id", -1)
                put("name", cat.name)
                put("type", cat.type.name)
                put("colorHex", "#10B981")
                put("isDefault", true)
            }
            catsArray.put(obj)
        }
        // Custom categories
        val customCats = database.customCategoryDao().getAllRawCategories().filter { !it.isDeleted }
        for (cc in customCats) {
            val obj = JSONObject().apply {
                put("id", cc.id)
                put("name", cc.name)
                put("type", cc.type)
                put("colorHex", cc.colorHex)
                put("isDefault", false)
            }
            catsArray.put(obj)
        }
        root.put("categories", catsArray)

        root
    }

    private suspend fun handleCreateTransaction(json: JSONObject): JSONObject = withContext(Dispatchers.IO) {
        val title = json.optString("title", "Lançamento Web").trim()
        val amount = json.optDouble("amount", 0.0)
        val typeStr = json.optString("type", "EXPENSE")
        val type = if (typeStr == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE
        val category = json.optString("category", "Outros")
        val timestamp = json.optLong("timestamp", System.currentTimeMillis())
        val note = json.optString("note", "")
        val cardId = if (json.has("cardId") && !json.isNull("cardId")) json.optLong("cardId") else null
        val isInstallment = json.optBoolean("isInstallment", false)
        val totalInstallments = json.optInt("totalInstallments", 1)
        val isRecurring = json.optBoolean("isRecurring", false)
        val recurringMonths = json.optInt("recurringMonths", 1)

        val res = JSONObject()

        if (isInstallment && totalInstallments > 1 && type == TransactionType.EXPENSE) {
            repository.createInstallments(
                title = title,
                totalAmount = amount,
                category = category,
                startTimestamp = timestamp,
                note = note,
                cardId = cardId,
                totalInstallments = totalInstallments
            )
            res.put("success", true)
            res.put("message", "Lançamento parcelado em ${totalInstallments}x criado com sucesso.")
        } else if (isRecurring && recurringMonths > 1) {
            repository.createRecurring(
                title = title,
                amount = amount,
                type = type,
                category = category,
                startTimestamp = timestamp,
                note = note,
                cardId = cardId,
                monthsCount = recurringMonths,
                intervalMonths = 1,
                isIndefinite = false
            )
            res.put("success", true)
            res.put("message", "Lançamento recorrente criado com sucesso.")
        } else {
            val entity = TransactionEntity(
                title = title,
                amount = amount,
                type = type.name,
                category = category,
                timestamp = timestamp,
                note = note,
                cardId = cardId
            )
            val newId = repository.insert(entity)
            res.put("success", true)
            res.put("id", newId)
        }

        res
    }

    private suspend fun handleUpdateTransaction(id: Long, json: JSONObject): JSONObject = withContext(Dispatchers.IO) {
        val existing = database.transactionDao().getById(id)
        val res = JSONObject()
        if (existing == null) {
            res.put("success", false)
            res.put("error", "Lançamento não encontrado")
            return@withContext res
        }

        val title = json.optString("title", existing.title).trim()
        val amount = json.optDouble("amount", existing.amount)
        val typeStr = json.optString("type", existing.type)
        val type = if (typeStr == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE
        val category = json.optString("category", existing.category)
        val timestamp = json.optLong("timestamp", existing.timestamp)
        val note = json.optString("note", existing.note)
        val cardId = if (json.has("cardId") && !json.isNull("cardId")) json.optLong("cardId") else null
        val isInstallment = json.optBoolean("isInstallment", existing.isInstallment)
        val totalInstallments = json.optInt("totalInstallments", existing.totalInstallments)
        val isRecurring = json.optBoolean("isRecurring", existing.isRecurring)
        val recurringMonths = json.optInt("recurringMonths", 6)

        repository.updateDetailedTransaction(
            existing = existing,
            title = title,
            amount = amount,
            type = type,
            category = category,
            timestamp = timestamp,
            note = note,
            cardId = cardId,
            isInstallment = isInstallment,
            totalInstallments = totalInstallments,
            isRecurring = isRecurring,
            recurringMonths = recurringMonths,
            recurringIntervalMonths = 1,
            isIndefinite = false
        )

        res.put("success", true)
        res
    }

    private suspend fun generateCsvExport(): String = withContext(Dispatchers.IO) {
        val txs = database.transactionDao().getAllRawTransactions().filter { !it.isDeleted }
            .sortedByDescending { it.timestamp }
        val cardsMap = database.creditCardDao().getAllCardsSync().associateBy { it.id }

        val sb = StringBuilder()
        // UTF-8 BOM for Microsoft Excel
        sb.append("\uFEFF")
        sb.append("Data,Tipo,Título,Categoria,Cartão,Valor,Anotações\n")

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        for (t in txs) {
            val dateStr = sdf.format(Date(t.timestamp))
            val cardName = cardsMap[t.cardId]?.name ?: (if (t.type == "EXPENSE") "Conta Corrente" else "-")
            val cleanTitle = t.title.replace("\"", "\"\"")
            val cleanNote = t.note.replace("\"", "\"\"")

            sb.append("\"$dateStr\",")
            sb.append("\"${if (t.type == "INCOME") "Receita" else "Despesa"}\",")
            sb.append("\"$cleanTitle\",")
            sb.append("\"${t.category}\",")
            sb.append("\"$cardName\",")
            sb.append(String.format(Locale.US, "%.2f", t.amount))
            sb.append(",\"$cleanNote\"\n")
        }
        sb.toString()
    }

    private fun sendCorsPreflight(writer: PrintWriter) {
        writer.print("HTTP/1.1 204 No Content\r\n")
        writer.print("Access-Control-Allow-Origin: *\r\n")
        writer.print("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n")
        writer.print("Access-Control-Allow-Headers: Content-Type, Authorization\r\n")
        writer.print("Access-Control-Max-Age: 86400\r\n")
        writer.print("Content-Length: 0\r\n\r\n")
        writer.flush()
    }

    private fun sendResponse(
        writer: PrintWriter,
        statusCode: Int,
        statusText: String,
        contentType: String,
        body: String
    ) {
        val bytes = body.toByteArray(Charsets.UTF_8)
        writer.print("HTTP/1.1 $statusCode $statusText\r\n")
        writer.print("Content-Type: $contentType\r\n")
        writer.print("Access-Control-Allow-Origin: *\r\n")
        writer.print("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n")
        writer.print("Access-Control-Allow-Headers: Content-Type, Authorization\r\n")
        writer.print("Cache-Control: no-cache, no-store, must-revalidate\r\n")
        writer.print("Content-Length: ${bytes.size}\r\n\r\n")
        writer.print(body)
        writer.flush()
    }

    private fun parseQueryParams(queryString: String): Map<String, String> {
        if (queryString.isBlank()) return emptyMap()
        val result = mutableMapOf<String, String>()
        val pairs = queryString.split("&")
        for (pair in pairs) {
            val idx = pair.indexOf("=")
            if (idx > 0) {
                val key = URLDecoder.decode(pair.substring(0, idx), "UTF-8")
                val value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
                result[key] = value
            }
        }
        return result
    }

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
}
