package com.example.ui.viewmodel

import com.example.data.local.CreditCardEntity
import com.example.data.local.TransactionEntity

data class CardWithExpenses(
    val card: CreditCardEntity,
    val totalExpenseThisMonth: Double,
    val monthlyTransactions: List<TransactionEntity>,
    val limitProgress: Float, // 0.0 to 1.0+
    val remainingLimit: Double,
    val isInvoicePaidThisMonth: Boolean = false,
    val isCurrentOpenInvoicePaid: Boolean = false,
    val currentOpenInvoiceExpense: Double = 0.0,
    val currentOpenInvoiceTransactionsCount: Int = 0,
    val currentInvoiceClosingDateText: String = "",
    val currentInvoiceDueDateText: String = "",
    val currentUsedLimit: Double = 0.0
)
