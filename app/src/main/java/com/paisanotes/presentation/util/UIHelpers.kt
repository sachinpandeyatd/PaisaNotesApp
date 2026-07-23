package com.paisanotes.presentation.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

fun parseHexColor(hexString: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hexString))
    } catch (e: Exception) {
        Color.Gray // Fallback
    }
}

fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName) {
        "Fastfood" -> Icons.Default.Fastfood
        "ShoppingCart" -> Icons.Default.ShoppingCart
        "Commute" -> Icons.Default.Commute
        "Receipt" -> Icons.Default.Receipt
        "MedicalServices" -> Icons.Default.MedicalServices
        "Payments" -> Icons.Default.Payments
        "TrendingUp" -> Icons.Default.TrendingUp
        else -> Icons.Default.Category
    }
}