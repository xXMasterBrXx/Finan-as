package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Formatters {
    private val ptBrLocale = Locale("pt", "BR")
    private val currencyFormat = NumberFormat.getCurrencyInstance(ptBrLocale)
    private val dateFormat = SimpleDateFormat("dd 'de' MMM", ptBrLocale)
    private val fullDateFormat = SimpleDateFormat("dd/MM/yyyy", ptBrLocale)
    private val monthYearFormat = SimpleDateFormat("MMMM 'de' yyyy", ptBrLocale)
    private val shortMonthYearFormat = SimpleDateFormat("MMM yyyy", ptBrLocale)

    fun formatCurrency(value: Double): String {
        return currencyFormat.format(value)
    }

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun formatFullDate(timestamp: Long): String {
        return fullDateFormat.format(Date(timestamp))
    }

    fun formatMonthYear(calendar: Calendar): String {
        val str = monthYearFormat.format(calendar.time)
        return str.replaceFirstChar { if (it.isLowerCase()) it.titlecase(ptBrLocale) else it.toString() }
    }

    fun formatMonthYear(timestamp: Long): String {
        val str = monthYearFormat.format(Date(timestamp))
        return str.replaceFirstChar { if (it.isLowerCase()) it.titlecase(ptBrLocale) else it.toString() }
    }

    fun formatShortMonthYear(calendar: Calendar): String {
        val str = shortMonthYearFormat.format(calendar.time)
        return str.replaceFirstChar { if (it.isLowerCase()) it.titlecase(ptBrLocale) else it.toString() }
    }

    fun formatShortMonthYear(timestamp: Long): String {
        val str = shortMonthYearFormat.format(Date(timestamp))
        return str.replaceFirstChar { if (it.isLowerCase()) it.titlecase(ptBrLocale) else it.toString() }
    }
}
