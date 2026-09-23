package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
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

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val packageName = sbn.packageName ?: return

        // Ignore notifications from our own app
        if (packageName == applicationContext.packageName) return

        val extras = sbn.notification?.extras ?: return
        val title = extras.getCharSequence("android.title")?.toString() ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        val subtext = extras.getCharSequence("android.subText")?.toString()

        if (title.isBlank() && text.isBlank()) return

        if (!BankNotificationParser.isBankNotification(packageName, title, text)) return

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
                val result = repository.processNotification(packageName, title, text, subtext)
                if (result != null && userPrefs.isNotifyOnImportEnabled()) {
                    showImportNotification(context, result.merchant, result.amount, result.bankName, result.status)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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

        notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
    }
}
