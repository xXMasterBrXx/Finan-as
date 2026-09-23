package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.util.NotificationManagerHelper

class NotificationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // 1. Process pending notifications (bills, cards, budgets)
        NotificationManagerHelper.checkAndNotifyNow(context)

        // 2. Schedule next day's alarm
        NotificationManagerHelper.scheduleDailyAlarm(context)
    }
}
