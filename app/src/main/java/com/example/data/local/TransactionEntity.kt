package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "INCOME" or "EXPENSE"
    val category: String,
    val timestamp: Long,
    val note: String = "",
    val cardId: Long? = null, // Associated card if expense is paid with a registered card
    val isInstallment: Boolean = false,
    val installmentNumber: Int = 1,
    val totalInstallments: Int = 1,
    val installmentGroupId: String? = null,
    val isAnticipated: Boolean = false,
    val syncUuid: String = UUID.randomUUID().toString(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

