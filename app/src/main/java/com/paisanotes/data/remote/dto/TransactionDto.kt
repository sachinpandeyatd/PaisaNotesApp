package com.paisanotes.data.remote.dto

data class TransactionDto(
    val id: String,
    val amount: Double,
    val transactionType: String,
    val merchant: String?,
    val category: String,
    val transactionDate: String, // Spring sends ISO-8601 String for ZonedDateTime
    val paymentMethod: String,
    val source: String,
    val notes: String?,
    val createdAt: String,
    val updatedAt: String,
    val isDeleted: Boolean
)

data class SyncPullResponse(
    val serverTimestamp: String,
    val transactions: List<TransactionDto>
)

data class SyncPushRequest(
    val transactions: List<TransactionDto>
)

data class SyncPushResponse(
    val processedTransactionIds: List<String>,
    val processedAuditLogIds: List<String>? // Nullable if missing in response
)