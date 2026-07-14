package com.paisanotes.presentation.transactions

import com.paisanotes.domain.model.Transaction

// 1. The State: Everything the screen needs to display
data class TransactionUiState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// 2. The Events: Everything the user can click or trigger
sealed interface TransactionUiEvent {
    data class DeleteTransaction(val id: String) : TransactionUiEvent
    data object ForceSync : TransactionUiEvent // User pulls to refresh
}