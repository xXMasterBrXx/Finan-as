package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val type: TransactionType,
    val iconName: String = "category",
    val colorHex: String = "#42A5F5",
    val isCustom: Boolean = false,
    val dbId: Long = 0
)

object CategoryIconHelper {
    val availableIcons: Map<String, ImageVector> = mapOf(
        "restaurant" to Icons.Default.Restaurant,
        "fastfood" to Icons.Default.Fastfood,
        "home" to Icons.Default.Home,
        "directions_car" to Icons.Default.DirectionsCar,
        "local_gas_station" to Icons.Default.LocalGasStation,
        "sports_esports" to Icons.Default.SportsEsports,
        "movie" to Icons.Default.Movie,
        "medical_services" to Icons.Default.MedicalServices,
        "fitness_center" to Icons.Default.FitnessCenter,
        "school" to Icons.Default.School,
        "receipt_long" to Icons.Default.ReceiptLong,
        "shopping_bag" to Icons.Default.ShoppingBag,
        "pets" to Icons.Default.Pets,
        "flight" to Icons.Default.Flight,
        "payments" to Icons.Default.Payments,
        "laptop_mac" to Icons.Default.LaptopMac,
        "work" to Icons.Default.Work,
        "trending_up" to Icons.Default.TrendingUp,
        "savings" to Icons.Default.Savings,
        "card_giftcard" to Icons.Default.CardGiftcard,
        "account_balance_wallet" to Icons.Default.AccountBalanceWallet,
        "star" to Icons.Default.Star,
        "category" to Icons.Default.Category,
        "more_horiz" to Icons.Default.MoreHoriz
    )

    val availableColors = listOf(
        "#EF5350", "#EC407A", "#AB47BC", "#7E57C2",
        "#5C6BC0", "#42A5F5", "#29B6F6", "#26C6DA",
        "#26A69A", "#43A047", "#66BB6A", "#9CCC65",
        "#D4E157", "#FFEE58", "#FFCA28", "#FFA726",
        "#FF7043", "#8D6E63", "#78909C", "#0284C7"
    )

    fun getIcon(iconName: String): ImageVector {
        return availableIcons[iconName] ?: Icons.Default.Category
    }

    fun parseColor(hex: String, fallback: Color = Color(0xFF42A5F5)): Color {
        return try {
            val cleanHex = hex.removePrefix("#")
            val colorInt = if (cleanHex.length == 6) {
                ("FF$cleanHex").toLong(16).toInt()
            } else if (cleanHex.length == 8) {
                cleanHex.toLong(16).toInt()
            } else {
                return fallback
            }
            Color(colorInt)
        } catch (e: Exception) {
            fallback
        }
    }
}

object Categories {
    // Expense Categories
    val Food = CategoryItem(
        id = "food",
        name = "Alimentação",
        icon = Icons.Default.Restaurant,
        color = Color(0xFFFF7043),
        type = TransactionType.EXPENSE
    )
    val Housing = CategoryItem(
        id = "housing",
        name = "Moradia",
        icon = Icons.Default.Home,
        color = Color(0xFF42A5F5),
        type = TransactionType.EXPENSE
    )
    val Transport = CategoryItem(
        id = "transport",
        name = "Transporte",
        icon = Icons.Default.DirectionsCar,
        color = Color(0xFFAB47BC),
        type = TransactionType.EXPENSE
    )
    val Leisure = CategoryItem(
        id = "leisure",
        name = "Lazer",
        icon = Icons.Default.SportsEsports,
        color = Color(0xFFEC407A),
        type = TransactionType.EXPENSE
    )
    val Health = CategoryItem(
        id = "health",
        name = "Saúde",
        icon = Icons.Default.MedicalServices,
        color = Color(0xFFEF5350),
        type = TransactionType.EXPENSE
    )
    val Education = CategoryItem(
        id = "education",
        name = "Educação",
        icon = Icons.Default.School,
        color = Color(0xFF26A69A),
        type = TransactionType.EXPENSE
    )
    val Bills = CategoryItem(
        id = "bills",
        name = "Contas & Fixas",
        icon = Icons.Default.ReceiptLong,
        color = Color(0xFFFFB74D),
        type = TransactionType.EXPENSE
    )
    val Shopping = CategoryItem(
        id = "shopping",
        name = "Compras",
        icon = Icons.Default.ShoppingBag,
        color = Color(0xFF7E57C2),
        type = TransactionType.EXPENSE
    )
    val OtherExpense = CategoryItem(
        id = "other_expense",
        name = "Outras Saídas",
        icon = Icons.Default.MoreHoriz,
        color = Color(0xFF78909C),
        type = TransactionType.EXPENSE
    )

    // Income Categories
    val Salary = CategoryItem(
        id = "salary",
        name = "Salário",
        icon = Icons.Default.Payments,
        color = Color(0xFF2E7D32),
        type = TransactionType.INCOME
    )
    val Freelance = CategoryItem(
        id = "freelance",
        name = "Freelance / Extra",
        icon = Icons.Default.LaptopMac,
        color = Color(0xFF00897B),
        type = TransactionType.INCOME
    )
    val Investments = CategoryItem(
        id = "investments",
        name = "Investimentos",
        icon = Icons.Default.TrendingUp,
        color = Color(0xFF1E88E5),
        type = TransactionType.INCOME
    )
    val Gift = CategoryItem(
        id = "gift",
        name = "Presente / Bônus",
        icon = Icons.Default.CardGiftcard,
        color = Color(0xFF8E24AA),
        type = TransactionType.INCOME
    )
    val OtherIncome = CategoryItem(
        id = "other_income",
        name = "Outras Entradas",
        icon = Icons.Default.AccountBalanceWallet,
        color = Color(0xFF43A047),
        type = TransactionType.INCOME
    )

    val expenseCategories = listOf(
        Food,
        Housing,
        Transport,
        Bills,
        Shopping,
        Leisure,
        Health,
        Education,
        OtherExpense
    )

    val incomeCategories = listOf(
        Salary,
        Freelance,
        Investments,
        Gift,
        OtherIncome
    )

    fun getCategoryByName(
        name: String,
        type: TransactionType,
        customCategories: List<CategoryItem> = emptyList()
    ): CategoryItem {
        val customMatches = customCategories.filter { it.type == type }
        val list = if (type == TransactionType.INCOME) (incomeCategories + customMatches) else (expenseCategories + customMatches)
        return list.firstOrNull { it.name.equals(name, ignoreCase = true) }
            ?: (if (type == TransactionType.INCOME) OtherIncome else OtherExpense)
    }

    fun findCategoryByName(
        name: String,
        customCategories: List<CategoryItem> = emptyList()
    ): CategoryItem {
        val all = expenseCategories + incomeCategories + customCategories
        return all.firstOrNull { it.name.equals(name, ignoreCase = true) }
            ?: OtherExpense
    }
}
