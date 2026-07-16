package com.paisanotes.domain.model

data class Loan(
    val id: String,
    val personId: String,
    val amountLent: Double,
    val dateGiven: Long,
    val expectedReturnDate: Long?,
    val status: String, // ACTIVE, CLOSED
    val notes: String?,
    val amountRepaid: Double = 0.0
)