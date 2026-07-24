package com.paisanotes.domain.model

data class BudgetProgress(
    val budgetId: String,
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val monthlyLimit: Double,
    val spentAmount: Double
) {
    // Helper property for the UI progress bar (0.0 to 1.0)
    val percentage: Float
        get() = if (monthlyLimit > 0) (spentAmount / monthlyLimit).toFloat().coerceIn(0f, 1f) else 0f
        
    val isExceeded: Boolean
        get() = spentAmount > monthlyLimit
}