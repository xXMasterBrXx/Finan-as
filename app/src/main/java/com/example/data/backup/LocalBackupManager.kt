package com.example.data.backup

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.example.data.local.AppDatabase
import com.example.data.local.CreditCardEntity
import com.example.data.local.CustomCategoryEntity
import com.example.data.local.TransactionEntity
import com.example.data.p2p.P2PJsonCodec
import com.example.data.preferences.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocalBackupManager(
    private val context: Context,
    private val database: AppDatabase,
    private val userPreferences: UserPreferences
) {
    private val backupDir: File
        get() = File(context.filesDir, "backups").apply { if (!exists()) mkdirs() }

    // Universal deterministic master key so backup files survive fresh app reinstalls across devices
    private fun getUniversalSecretKey(): SecretKey {
        val masterSecret = "FinanFlow_Secure_Backup_Master_Key_v2026_Unified"
        val sha = MessageDigest.getInstance("SHA-256")
        val keyBytes = sha.digest(masterSecret.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    private fun getLegacySecretKey(): SecretKey? {
        val prefKey = "local_backup_aes_key"
        val existing = userPreferences.getString(prefKey, "")
        return if (!existing.isNullOrBlank()) {
            try {
                val decoded = Base64.decode(existing, Base64.NO_WRAP)
                SecretKeySpec(decoded, 0, decoded.size, "AES")
            } catch (e: Exception) {
                null
            }
        } else null
    }

    suspend fun generateBackupJson(): String = withContext(Dispatchers.IO) {
        val txs = database.transactionDao().getAllRawTransactions()
        val cards = database.creditCardDao().getAllRawCards()
        val cats = database.customCategoryDao().getAllRawCategories()

        val root = JSONObject().apply {
            put("version", 2)
            put("timestamp", System.currentTimeMillis())
            put("budgetLimit", userPreferences.monthlyBudgetLimit.value)
            put("currency", userPreferences.defaultCurrency.value)
            put("themeMode", userPreferences.themeMode.value.name)

            val txArray = JSONArray()
            txs.forEach { txArray.put(P2PJsonCodec.transactionToJson(it)) }
            put("transactions", txArray)

            val cardArray = JSONArray()
            cards.forEach { cardArray.put(P2PJsonCodec.cardToJson(it)) }
            put("cards", cardArray)

            val catArray = JSONArray()
            cats.forEach { catArray.put(P2PJsonCodec.categoryToJson(it)) }
            put("categories", catArray)
        }
        root.toString()
    }

    suspend fun createBackupBytes(): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val jsonString = generateBackupJson()
            val secretKey = getUniversalSecretKey()
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val encrypted = cipher.doFinal(jsonString.toByteArray(Charsets.UTF_8))
            Result.success(encrypted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createBackup(): Result<File> = withContext(Dispatchers.IO) {
        try {
            val bytesResult = createBackupBytes()
            if (bytesResult.isFailure) {
                return@withContext Result.failure(bytesResult.exceptionOrNull() ?: Exception("Erro ao gerar backup"))
            }
            val encrypted = bytesResult.getOrThrow()
            val timeStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(backupDir, "finanflow_backup_$timeStr.finbackup")
            file.writeBytes(encrypted)

            userPreferences.setLastBackupTimestamp(System.currentTimeMillis())

            // Prune backups to keep only the 5 most recent ones
            val currentBackups = listBackups()
            if (currentBackups.size > 5) {
                for (i in 5 until currentBackups.size) {
                    currentBackups[i].delete()
                }
            }

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun writeBackupToUri(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val bytesResult = createBackupBytes()
            if (bytesResult.isFailure) {
                return@withContext Result.failure(bytesResult.exceptionOrNull() ?: Exception("Erro ao gerar backup"))
            }
            val encrypted = bytesResult.getOrThrow()
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(encrypted)
                outputStream.flush()
            } ?: return@withContext Result.failure(Exception("Não foi possível abrir o destino para salvar o arquivo"))

            userPreferences.setLastBackupTimestamp(System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun decryptPayload(encryptedBytes: ByteArray): String {
        // Try with universal master key first
        try {
            val secretKey = getUniversalSecretKey()
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            // Try with legacy key if available
            val legacyKey = getLegacySecretKey()
            if (legacyKey != null) {
                val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
                cipher.init(Cipher.DECRYPT_MODE, legacyKey)
                val decryptedBytes = cipher.doFinal(encryptedBytes)
                return String(decryptedBytes, Charsets.UTF_8)
            }
            throw e
        }
    }

    suspend fun restoreFromBytes(encryptedBytes: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (encryptedBytes.isEmpty()) {
                return@withContext Result.failure(Exception("O arquivo de backup está vazio"))
            }
            val jsonString = try {
                decryptPayload(encryptedBytes)
            } catch (e: Exception) {
                return@withContext Result.failure(Exception("Arquivo de backup inválido ou não compatível com o BUMoney"))
            }

            val root = JSONObject(jsonString)

            if (root.has("budgetLimit")) {
                userPreferences.setMonthlyBudgetLimit(root.optDouble("budgetLimit", 3000.0))
            }
            if (root.has("currency")) {
                userPreferences.setDefaultCurrency(root.optString("currency", "BRL"))
            }

            val txArray = root.optJSONArray("transactions")
            val cardArray = root.optJSONArray("cards")
            val catArray = root.optJSONArray("categories")

            val txs = mutableListOf<TransactionEntity>()
            if (txArray != null) {
                for (i in 0 until txArray.length()) {
                    txs.add(P2PJsonCodec.jsonToTransaction(txArray.getJSONObject(i)))
                }
            }

            val cards = mutableListOf<CreditCardEntity>()
            if (cardArray != null) {
                for (i in 0 until cardArray.length()) {
                    cards.add(P2PJsonCodec.jsonToCard(cardArray.getJSONObject(i)))
                }
            }

            val cats = mutableListOf<CustomCategoryEntity>()
            if (catArray != null) {
                for (i in 0 until catArray.length()) {
                    cats.add(P2PJsonCodec.jsonToCategory(catArray.getJSONObject(i)))
                }
            }

            database.transactionDao().deleteAllTransactions()
            database.creditCardDao().deleteAllCards()
            database.customCategoryDao().deleteAllCategories()

            if (txs.isNotEmpty()) database.transactionDao().insertAll(txs)
            if (cards.isNotEmpty()) database.creditCardDao().insertCards(cards)
            if (cats.isNotEmpty()) database.customCategoryDao().insertCategories(cats)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreBackup(file: File): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val encryptedBytes = file.readBytes()
            restoreFromBytes(encryptedBytes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreFromUri(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Não foi possível acessar o arquivo selecionado"))
            val bytes = inputStream.use { it.readBytes() }
            restoreFromBytes(bytes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun listBackups(): List<File> {
        return backupDir.listFiles { file -> file.extension == "finbackup" }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    suspend fun createPreUpdateSnapshot(): Result<File> = withContext(Dispatchers.IO) {
        try {
            val bytesResult = createBackupBytes()
            if (bytesResult.isFailure) {
                return@withContext Result.failure(bytesResult.exceptionOrNull() ?: Exception("Erro ao gerar backup de segurança"))
            }
            val encrypted = bytesResult.getOrThrow()
            
            // 1. Save dedicated pre-update safety file in filesDir
            val safetyFile = File(context.filesDir, "pre_update_safety_backup.finbackup")
            safetyFile.writeBytes(encrypted)

            // 2. Also save into standard backups folder
            val timeStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val archiveFile = File(backupDir, "finanflow_backup_pre_update_$timeStr.finbackup")
            archiveFile.writeBytes(encrypted)

            userPreferences.setLastBackupTimestamp(System.currentTimeMillis())
            Result.success(safetyFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMostRecentSafetyBackup(): File? {
        val safetyFile = File(context.filesDir, "pre_update_safety_backup.finbackup")
        if (safetyFile.exists() && safetyFile.length() > 0L) {
            return safetyFile
        }
        val backups = listBackups()
        return backups.firstOrNull { it.length() > 0L }
    }

    fun deleteBackup(file: File): Boolean {
        return file.delete()
    }
}
