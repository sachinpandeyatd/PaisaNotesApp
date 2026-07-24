package com.paisanotes.data.remote.dto

import com.paisanotes.data.local.entity.SyncStatus

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
    val categoryId: String?,
    val accountId: String?,
    val transferAccountId: String?,
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
    val auditLogs: List<AuditLogDto>? = emptyList(),
    val categories: List<CategoryDto>? = emptyList(),
    val budgets: List<BudgetDto>? = emptyList(),
    val accounts: List<AccountDto>? = emptyList()
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
    val type: String,
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
    val auditLogs: List<AuditLogDto>? = emptyList(),
    val categories: List<CategoryDto>?,
    val budgets: List<BudgetDto>? = emptyList(),
    val accounts: List<AccountDto>? = emptyList()
)

data class SyncPushResponse(
    val processedTransactionIds: List<String>,
    val processedPersonIds: List<String> = emptyList(),
    val processedLoanIds: List<String> = emptyList(),
    val processedEmiIds: List<String> = emptyList(),
    val processedAuditLogIds: List<String> = emptyList(),
    val processedCategoryIds: List<String>?,
    val processedBudgetIds: List<String>? = emptyList(),
    val processedAccountIds: List<String>? = emptyList()
)

data class CategoryDto(
    val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val isDefault: Boolean,

    val createdAt: String,
    val updatedAt: String,
    val isDeleted: Boolean,
    val syncStatus: SyncStatus,
)

data class BudgetDto(
    val id: String,
    val categoryId: String,
    val monthlyLimit: Double,
    val createdAt: String,
    val updatedAt: String,
    val isDeleted: Boolean
)

data class AccountDto(
    val id: String,
    val name: String,
    val type: String, // CASH, SAVINGS, CREDIT_CARD, WALLET
    val initialBalance: Double,
    val createdAt: String,
    val updatedAt: String,
    val isDeleted: Boolean
)