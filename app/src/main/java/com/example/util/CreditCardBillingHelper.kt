package com.example.util

import com.example.data.local.CreditCardEntity
import java.util.Calendar

object CreditCardBillingHelper {

    /**
     * Data class holding the billing period (fechamento) and due date (vencimento)
     * in terms of month and year.
     */
    data class CardInvoiceDate(
        val invoiceYear: Int,
        val invoiceMonth: Int,   // 0-based Calendar.MONTH (month when invoice is paid)
        val closingDateEpoch: Long,
        val dueDateEpoch: Long
    )

    /**
     * Determines when a credit card transaction actually leaves the account (due date / vencimento).
     *
     * Rules:
     * - Let `txDay` be the day of month when the purchase occurred, in month `txMonth` and `txYear`.
     * - If `closingDay <= dueDay`:
     *     - If `txDay <= closingDay`: Purchase falls in current month's invoice. Due date is in current month (`txMonth`, `txYear`).
     *     - If `txDay > closingDay`: Purchase falls in next month's invoice. Due date is in next month (`txMonth + 1`).
     * - If `closingDay > dueDay` (e.g. closes on the 25th, pays on the 5th of next month):
     *     - If `txDay <= closingDay`: Closes on the 25th of `txMonth`, due on 5th of `txMonth + 1`.
     *     - If `txDay > closingDay`: Closes on the 25th of `txMonth + 1`, due on 5th of `txMonth + 2`.
     *
     * Returns a [CardInvoiceDate] with the year and 0-based month in which the invoice is PAID (effective cash outflow).
     */
    fun calculatePaymentMonthAndYear(
        purchaseTimestamp: Long,
        card: CreditCardEntity
    ): Pair<Int, Int> { // Pair(year, 0-based month)
        val cal = Calendar.getInstance().apply { timeInMillis = purchaseTimestamp }
        val txYear = cal.get(Calendar.YEAR)
        val txMonth = cal.get(Calendar.MONTH)
        val txDay = cal.get(Calendar.DAY_OF_MONTH)

        val closingDay = card.closingDay.coerceIn(1, 31)
        val dueDay = card.dueDay.coerceIn(1, 31)

        val dueCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, txYear)
            set(Calendar.MONTH, txMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        if (closingDay <= dueDay) {
            // E.g. Closes day 5, due day 15 of same month
            if (txDay <= closingDay) {
                // Paid this same month
                return Pair(txYear, txMonth)
            } else {
                // Falls into next month's invoice
                dueCal.add(Calendar.MONTH, 1)
                return Pair(dueCal.get(Calendar.YEAR), dueCal.get(Calendar.MONTH))
            }
        } else {
            // E.g. Closes day 25, due day 5 of NEXT month (most common in Brazil, e.g. closes 25th, due 5th)
            if (txDay <= closingDay) {
                // Due next month
                dueCal.add(Calendar.MONTH, 1)
                return Pair(dueCal.get(Calendar.YEAR), dueCal.get(Calendar.MONTH))
            } else {
                // Purchase after closing on the 25th, enters next month's invoice which is due in 2 months!
                dueCal.add(Calendar.MONTH, 2)
                return Pair(dueCal.get(Calendar.YEAR), dueCal.get(Calendar.MONTH))
            }
        }
    }

    /**
     * Checks whether a purchase made on [purchaseTimestamp] with [card]
     * has its cash outflow (payment) in [targetYear] and [targetMonth] (0-based).
     */
    fun isPaymentDueInPeriod(
        purchaseTimestamp: Long,
        card: CreditCardEntity,
        targetYear: Int,
        targetMonth: Int
    ): Boolean {
        val (dueYear, dueMonth) = calculatePaymentMonthAndYear(purchaseTimestamp, card)
        return dueYear == targetYear && dueMonth == targetMonth
    }
}
