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
    val transactions: List<TransactionDto>,
    val people: List<PersonDto>? = emptyList(),
    val loans: List<LoanDto>? = emptyList(),
    val emis: List<EmiDto>? = emptyList(),
    val auditLogs: List<AuditLogDto>? = emptyList()
)

data class PersonDto(
    val id: String,
    val name: String,
    val phoneNumber: String?,
    val createdAt: String,
    val updatedAt: String,
    val isDeleted: Boolean
)

data class LoanDto(
    val id: String,
    val personId: String,
    val amountLent: Double,
    val dateGiven: String,
    val expectedReturnDate: String?,
    val status: String,
    val notes: String?,
    val amountRepaid: Double,
    val createdAt: String,
    val updatedAt: String,
    val isDeleted: Boolean
)

data class EmiDto(
    val id: String,
    val personId: String?,
    val refNumber: String?,
    val itemName: String,
    val ownerType: String,
    val principalAmount: Double,
    val monthlyEmiAmount: Double,
    val totalMonths: Int,
    val completedMonths: Int,
    val startDate: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val isDeleted: Boolean
)

data class AuditLogDto(
    val id: String,
    val entityType: String,
    val entityId: String,
    val actionType: String,
    val metadata: Map<String, Any>,
    val createdAt: String
)

data class SyncPushRequest(
    val transactions: List<TransactionDto>,
    val people: List<PersonDto>? = emptyList(),
    val loans: List<LoanDto>? = emptyList(),
    val emis: List<EmiDto>? = emptyList(),
    val auditLogs: List<AuditLogDto>? = emptyList()
)

data class SyncPushResponse(
    val processedTransactionIds: List<String>,
    val processedPersonIds: List<String> = emptyList(),
    val processedLoanIds: List<String> = emptyList(),
    val processedEmiIds: List<String> = emptyList(),
    val processedAuditLogIds: List<String> = emptyList()
)