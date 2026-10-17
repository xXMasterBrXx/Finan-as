package com.example.util

import kotlin.math.roundToLong

/**
 * Data class representing the transparent preview of an installment schedule.
 */
data class InstallmentPreview(
    val baseAmount: Double,
    val lastAmount: Double,
    val totalInstallments: Int,
    val totalAmount: Double,
    val hasDifference: Boolean,
    val previewText: String,
    val detailText: String
)

/**
 * Utility to calculate exact installment amounts in cents to eliminate IEEE 754 floating-point
 * rounding errors, guaranteeing that the mathematical sum of all installments equals the total amount.
 * The difference of cents is allocated to the last installment.
 */
object InstallmentCalculator {

    /**
     * Calculates the exact schedule of installment amounts.
     * Guaranteed: schedule.sumOf { Math.round(it * 100) } == Math.round(totalAmount * 100)
     */
    fun calculateSchedule(totalAmount: Double, totalInstallments: Int): List<Double> {
        val safeCount = totalInstallments.coerceAtLeast(1)
        val totalCents = (totalAmount * 100.0).roundToLong()

        if (safeCount == 1) {
            return listOf(totalCents / 100.0)
        }

        // Standard financial rounding for base installments
        val baseCents = (totalCents.toDouble() / safeCount).roundToLong()
        // The last installment absorbs any remainder (positive or negative)
        val lastCents = totalCents - (baseCents * (safeCount - 1))

        return List(safeCount) { index ->
            val cents = if (index == safeCount - 1) lastCents else baseCents
            cents / 100.0
        }
    }

    /**
     * Generates an exact, human-readable preview for the UI.
     */
    fun getPreview(totalAmount: Double, totalInstallments: Int): InstallmentPreview {
        val safeCount = totalInstallments.coerceAtLeast(1)
        val schedule = calculateSchedule(totalAmount, safeCount)
        val baseAmount = schedule.firstOrNull() ?: 0.0
        val lastAmount = schedule.lastOrNull() ?: baseAmount
        val hasDifference = safeCount > 1 && Math.abs(lastAmount - baseAmount) >= 0.009

        val previewText: String
        val detailText: String

        if (!hasDifference || safeCount <= 1) {
            previewText = "${safeCount}x de ${Formatters.formatCurrency(baseAmount)}"
            detailText = "Total exato: ${Formatters.formatCurrency(totalAmount)}"
        } else {
            val regularCount = safeCount - 1
            previewText = "${regularCount}x de ${Formatters.formatCurrency(baseAmount)} + 1x de ${Formatters.formatCurrency(lastAmount)}"
            detailText = "Última parcela ajustada em ${Formatters.formatCurrency(lastAmount)} para fechar o total exato de ${Formatters.formatCurrency(totalAmount)}"
        }

        return InstallmentPreview(
            baseAmount = baseAmount,
            lastAmount = lastAmount,
            totalInstallments = safeCount,
            totalAmount = totalAmount,
            hasDifference = hasDifference,
            previewText = previewText,
            detailText = detailText
        )
    }
}
