package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserPreferences
import com.example.data.repository.ImportedNotificationRepository
import com.example.util.BankNotificationParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class BankNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val TAG = "BankNotifListener"

        @Volatile
        var isServiceConnected: Boolean = false
            private set

        @Volatile
        private var instance: BankNotificationListenerService? = null

        fun getInstance(): BankNotificationListenerService? = instance

        fun rebindService(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    val component = ComponentName(context, BankNotificationListenerService::class.java)
                    requestRebind(component)
                    Log.d(TAG, "Rebind requested for BankNotificationListenerService")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to request rebind", e)
                }
            }
        }

        fun scanActiveNotifications(): Int {
            val service = instance ?: return 0
            return service.processAllActiveNotifications()
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        isServiceConnected = true
        Log.d(TAG, "BankNotificationListenerService connected successfully")

        // Scan any bank notifications currently in the status bar
        serviceScope.launch {
            try {
                processAllActiveNotifications()
            } catch (e: Exception) {
                Log.e(TAG, "Error processing active notifications on connect", e)
            }
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        if (instance == this) {
            instance = null
        }
        isServiceConnected = false
        Log.d(TAG, "BankNotificationListenerService disconnected")

        // Request rebind if disconnected unexpectedly by the OS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val component = ComponentName(this, BankNotificationListenerService::class.java)
                requestRebind(component)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to request rebind on disconnect", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
        isServiceConnected = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        handleStatusBarNotification(sbn)
    }

    fun processAllActiveNotifications(): Int {
        val active = try {
            activeNotifications
        } catch (e: Exception) {
            null
        } ?: return 0

        var count = 0
        for (sbn in active) {
            if (handleStatusBarNotification(sbn)) {
                count++
            }
        }
        return count
    }

    private fun handleStatusBarNotification(sbn: StatusBarNotification): Boolean {
        val packageName = sbn.packageName ?: return false

        // Ignore notifications from our own app
        if (packageName == applicationContext.packageName) return false

        val notif = sbn.notification ?: return false
        val extras = notif.extras ?: return false

        // 1. Extract title (trying standard title, big title, etc.)
        val title = (extras.getCharSequence(Notification.EXTRA_TITLE)
            ?: extras.getCharSequence("android.title.big")
            ?: extras.getCharSequence("android.title")
            ?: "").toString().trim()

        // 2. Extract text from all potential notification fields (text, bigText, textLines, ticker, summary)
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim() ?: ""
        val subtext = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.trim() ?: ""
        val summaryText = extras.getCharSequence("android.summaryText")?.toString()?.trim() ?: ""
        val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)?.joinToString(" ") ?: ""
        val ticker = notif.tickerText?.toString()?.trim() ?: ""

        val combinedContent = buildString {
            if (text.isNotBlank()) append(text)
            if (bigText.isNotBlank() && bigText != text) {
                if (isNotEmpty()) append(" ")
                append(bigText)
            }
            if (lines.isNotBlank() && !contains(lines)) {
                if (isNotEmpty()) append(" ")
                append(lines)
            }
            if (ticker.isNotBlank() && !contains(ticker)) {
                if (isNotEmpty()) append(" ")
                append(ticker)
            }
            if (summaryText.isNotBlank() && !contains(summaryText)) {
                if (isNotEmpty()) append(" ")
                append(summaryText)
            }
        }.trim()

        if (title.isBlank() && combinedContent.isBlank()) return false

        // 3. Verify if it is a genuine bank transaction notification
        if (!BankNotificationParser.isBankNotification(packageName, title, combinedContent)) {
            return false
        }

        // 4. Process and persist into Room database
        val context = applicationContext
        val db = AppDatabase.getInstance(context)
        val userPrefs = UserPreferences.getInstance(context)

        val repository = ImportedNotificationRepository(
            importedNotificationDao = db.importedNotificationDao(),
            transactionDao = db.transactionDao(),
            creditCardDao = db.creditCardDao(),
            userPreferences = userPrefs
        )

        serviceScope.launch {
            try {
                val result = repository.processNotification(
                    packageName = packageName,
                    title = title,
                    text = combinedContent,
                    subtext = subtext.ifBlank { null }
                )
                if (result != null && userPrefs.isNotifyOnImportEnabled()) {
                    showImportNotification(context, result.merchant, result.amount, result.bankName, result.status)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error importing bank notification", e)
            }
        }

        return true
    }

    private fun showImportNotification(
        context: Context,
        merchant: String,
        amount: Double,
        bankName: String,
        status: String
    ) {
        val channelId = "bank_imports_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Importações de Bancos",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificações de transações de bancos detectadas e importadas"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            action = "com.example.ACTION_VIEW_BANK_IMPORTS"
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fmt = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(amount)

        val contentTitle = if (status == "AUTO_IMPORTED") {
            "⚡ Transação de $bankName Importada"
        } else {
            "🔔 Nova Transação de $bankName Detectada"
        }

        val contentText = if (status == "AUTO_IMPORTED") {
            "$fmt em $merchant registrado automaticamente com sucesso!"
        } else {
            "Toque para revisar e confirmar o lançamento de $fmt em $merchant"
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
        } catch (e: SecurityException) {
            // Ignored if POST_NOTIFICATIONS runtime permission revoked
        }
    }
}
