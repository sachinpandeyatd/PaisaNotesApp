package com.paisanotes.domain.model

data class Account(
    val id: String,
    val name: String,
    val type: String, // CASH, SAVINGS, CREDIT_CARD, WALLET
    val initialBalance: Double,
    val currentBalance: Double = 0.0 // We will calculate this dynamically!
)