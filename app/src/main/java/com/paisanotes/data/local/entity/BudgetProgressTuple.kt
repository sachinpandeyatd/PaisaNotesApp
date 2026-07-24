package com.paisanotes.data.local.entity

data class BudgetProgressTuple(
    val budgetId: String,
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val monthlyLimit: Double,
    val spentAmount: Double // We will calculate this dynamically in SQL!
)