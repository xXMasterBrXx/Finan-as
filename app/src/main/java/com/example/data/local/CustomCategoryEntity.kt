package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "custom_categories")
data class CustomCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // "INCOME" or "EXPENSE"
    val iconName: String = "category",
    val colorHex: String = "#42A5F5",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val syncUuid: String = UUID.randomUUID().toString(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

