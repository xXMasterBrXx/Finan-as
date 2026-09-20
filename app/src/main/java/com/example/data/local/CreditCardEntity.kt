package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "credit_cards")
data class CreditCardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,             // e.g. "Nubank Ultravioleta", "Inter Gold"
    val lastFourDigits: String = "", // e.g. "1234"
    val colorHex: String = "#8A05BE", // Hex color representation for the card banner
    val limitAmount: Double = 0.0,   // Optional total credit limit
    val closingDay: Int = 10,        // Dia do fechamento da fatura
    val dueDay: Int = 17,            // Dia do vencimento da fatura
    val syncUuid: String = UUID.randomUUID().toString(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

