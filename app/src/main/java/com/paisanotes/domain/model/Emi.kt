package com.paisanotes.domain.model

data class Emi(
    val id: String,
    val personId: String?,
    val refNumber: String?,
    val itemName: String,
    val ownerType: String, // ME, FRIEND
    val principalAmount: Double,
    val monthlyEmiAmount: Double,
    val totalMonths: Int,
    val completedMonths: Int = 0,
    val startDate: Long,
    val status: String // ACTIVE, CLOSED
)