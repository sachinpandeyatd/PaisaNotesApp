package com.paisanotes.domain.model

data class CreditCardBill(
    val id: String,
    val accountId: String,
    val billingMonth: String,
    val totalBilledAmount: Double,
    val minimumDue: Double,
    val amountPaid: Double,
    val dueDate: Long?,
    val status: String // UNPAID, PARTIALLY_PAID, CLEARED
)