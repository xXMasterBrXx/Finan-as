package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.NotificationItemEntity
import com.example.data.preferences.UserPreferences
import com.example.data.repository.NotificationRepository
import com.example.receiver.NotificationAlarmReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

object NotificationManagerHelper {

    const val CHANNEL_ID = "finanflow_reminders_channel"
    private const val CHANNEL_NAME = "Lembretes e Vencimentos"
    private const val CHANNEL_DESC = "Notificações de contas a pagar, vencimento de cartões e lembretes financeiros"

    private const val ALARM_REQ_CODE = 90210
    private const val TEST_NOTIF_ID = 88888

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Checks whether the special Notification Listener permission (to read bank notifications) is granted in Android.
     */
    fun isNotificationListenerPermissionGranted(context: Context): Boolean {
        return try {
            val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
            if (enabledPackages.contains(context.packageName)) {
                return true
            }
            val flat = android.provider.Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: ""
            flat.contains(context.packageName)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Opens Android System Settings to allow the user to enable Notification Listener access for BUMoney.
     */
    fun openNotificationListenerSettings(context: Context) {
        try {
            val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (err: Exception) {
                err.printStackTrace()
            }
        }
    }

    /**
     * Attempts to reconnect/rebind the BankNotificationListenerService if unhooked by the Android OS.
     */
    fun rebindNotificationListener(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val componentName = android.content.ComponentName(
                    context,
                    com.example.service.BankNotificationListenerService::class.java
                )
                android.service.notification.NotificationListenerService.requestRebind(componentName)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Dispatches an Android system notification.
     */
    fun showSystemNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        actionRoute: String? = null
    ) {
        createNotificationChannel(context)

        if (!hasNotificationPermission(context)) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_ROUTE", actionRoute ?: "notifications")
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            pendingIntentFlags
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Ignored if permission was revoked in runtime
        }
    }

    /**
     * Schedules the daily alarm at the user's preferred time.
     */
    fun scheduleDailyAlarm(context: Context) {
        val userPrefs = UserPreferences.getInstance(context)
        if (!userPrefs.notificationsEnabled.value) {
            cancelDailyAlarm(context)
            return
        }

        val hour = userPrefs.notificationHour.value
        val minute = userPrefs.notificationMinute.value

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, NotificationAlarmReceiver::class.java)

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQ_CODE,
            intent,
            flags
        )

        val targetCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetCal.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    targetCal.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                targetCal.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelDailyAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, NotificationAlarmReceiver::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_NO_CREATE
        }
        val pendingIntent = PendingIntent.getBroadcast(context, ALARM_REQ_CODE, intent, flags)
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    /**
     * Checks database for pending events, creates database notification records, and fires system notifications.
     */
    fun checkAndNotifyNow(context: Context, onComplete: ((List<NotificationItemEntity>) -> Unit)? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val userPrefs = UserPreferences.getInstance(context)
                val repository = NotificationRepository(
                    notificationDao = db.notificationDao(),
                    transactionDao = db.transactionDao(),
                    creditCardDao = db.creditCardDao(),
                    userPreferences = userPrefs
                )

                val newNotifications = repository.checkAndGeneratePendingNotifications()
                for (item in newNotifications) {
                    showSystemNotification(
                        context = context,
                        notificationId = (item.id.toInt().takeIf { it != 0 } ?: (item.title.hashCode() and 0x7FFFFFFF)),
                        title = item.title,
                        message = item.message,
                        actionRoute = item.actionRoute
                    )
                }

                onComplete?.invoke(newNotifications)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Sends an instant test notification.
     */
    fun sendTestNotification(context: Context) {
        val title = "🔔 BUMoney: Notificação de Teste"
        val message = "Tudo pronto! Seu sistema de alertas para contas, faturas de cartão e orçamentos está funcionando perfeitamente."

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(context)
            val notif = NotificationItemEntity(
                title = title,
                message = message,
                type = "SYSTEM_REMINDER",
                timestamp = System.currentTimeMillis(),
                isRead = false,
                referenceId = null,
                actionRoute = "notifications",
                severity = "SUCCESS"
            )
            val id = db.notificationDao().insert(notif)
            showSystemNotification(
                context = context,
                notificationId = TEST_NOTIF_ID,
                title = title,
                message = message,
                actionRoute = "notifications"
            )
        }
    }
}
