package com.paisanotes.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paisanotes.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    // Backing property (Mutable, private)
    private val _state = MutableStateFlow(TransactionUiState())
    // Public property (Immutable, UI only reads this)
    val state: StateFlow<TransactionUiState> = _state.asStateFlow()

    init {
        // Start listening to the Room database the moment the ViewModel is created
        observeTransactions()
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            repository.getAllTransactions()
                .onStart { _state.update { it.copy(isLoading = true) } }
                .catch { error -> _state.update { it.copy(isLoading = false, error = error.message) } }
                .collect { transactionsList ->
                    // Every time the DB changes, this block automatically executes!
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            transactions = transactionsList,
                            error = null
                        )
                    }
                }
        }
    }

    // Handles user actions
    fun onEvent(event: TransactionUiEvent) {
        when (event) {
            is TransactionUiEvent.DeleteTransaction -> {
                viewModelScope.launch {
                    repository.deleteTransaction(event.id)
                }
            }
            TransactionUiEvent.ForceSync -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    repository.syncWithServer()
                    _state.update { it.copy(isLoading = false) }
                }
            }
        }
    }
}