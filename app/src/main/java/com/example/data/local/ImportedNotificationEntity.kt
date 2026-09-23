package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "imported_bank_notifications")
data class ImportedNotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val bankName: String,
    val rawTitle: String,
    val rawText: String,
    val amount: Double,
    val type: String, // "EXPENSE" or "INCOME"
    val merchant: String,
    val category: String,
    val cardLastFourDigits: String? = null,
    val matchedCardId: Long? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "PENDING", // "PENDING", "IMPORTED", "DISCARDED", "AUTO_IMPORTED"
    val importedTransactionId: Long? = null
)
