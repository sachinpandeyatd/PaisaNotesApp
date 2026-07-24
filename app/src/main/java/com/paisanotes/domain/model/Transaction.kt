package com.paisanotes.domain.model

data class Transaction(
    val id: String,
    val amount: Double,
    val transactionType: String,
    val merchant: String?,
    val category: String,
    val accountId: String? = null,
    val transferAccountId: String? = null,
    val transactionDate: Long, // Epoch Milliseconds for easy UI formatting
    val paymentMethod: String,
    val source: String,
    val notes: String?,
    val categoryId: String? = null
)