package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // "RECURRING", "CARD_CLOSING", "CARD_DUE", "BUDGET_ALERT", "SYSTEM_REMINDER"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val referenceId: Long? = null,
    val actionRoute: String? = null,
    val severity: String = "INFO", // "INFO", "WARNING", "URGENT", "SUCCESS"
    val createdAt: Long = System.currentTimeMillis()
)
