package com.paisanotes.domain.model

data class Budget(
    val id: String,
    val categoryId: String,
    val monthlyLimit: Double
)