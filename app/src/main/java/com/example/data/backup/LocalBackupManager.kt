package com.example.data.backup

import android.content.Context
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
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
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

    private fun getOrCreateSecretKey(): SecretKey {
        val prefKey = "local_backup_aes_key"
        val existing = userPreferences.getString(prefKey, "")
        if (!existing.isNullOrBlank()) {
            val decoded = Base64.decode(existing, Base64.NO_WRAP)
            return SecretKeySpec(decoded, 0, decoded.size, "AES")
        } else {
            val keyGen = KeyGenerator.getInstance("AES")
            keyGen.init(128)
            val key = keyGen.generateKey()
            val encoded = Base64.encodeToString(key.encoded, Base64.NO_WRAP)
            userPreferences.setString(prefKey, encoded)
            return key
        }
    }

    suspend fun createBackup(): Result<File> = withContext(Dispatchers.IO) {
        try {
            val txs = database.transactionDao().getAllRawTransactions()
            val cards = database.creditCardDao().getAllRawCards()
            val cats = database.customCategoryDao().getAllRawCategories()

            val root = JSONObject().apply {
                put("version", 1)
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

            val jsonString = root.toString()
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val encrypted = cipher.doFinal(jsonString.toByteArray(Charsets.UTF_8))

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

    suspend fun restoreBackup(file: File): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val encryptedBytes = file.readBytes()
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            val jsonString = String(decryptedBytes, Charsets.UTF_8)

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

    fun listBackups(): List<File> {
        return backupDir.listFiles { file -> file.extension == "finbackup" }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    fun deleteBackup(file: File): Boolean {
        return file.delete()
    }
}
