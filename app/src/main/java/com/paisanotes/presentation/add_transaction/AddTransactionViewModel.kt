package com.paisanotes.presentation.add_transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.paisanotes.domain.model.Category
import com.paisanotes.domain.model.Transaction
import com.paisanotes.domain.repository.AccountRepository
import com.paisanotes.domain.repository.CategoryRepository
import com.paisanotes.domain.repository.TransactionRepository
import com.paisanotes.presentation.navigation.AddTransactionRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddTransactionState(
    val amount: String = "",
    val category: String = "",
    val transactionType: String = "EXPENSE", // Default to Expense
    val notes: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val categoryId: String? = null,
    val categories: List<Category> = emptyList(),
    val accounts: List<com.paisanotes.domain.model.Account> = emptyList(),
    val accountId: String? = null,
    val accountName: String = "",
    val transferAccountId: String? = null,
    val transferAccountName: String = "",
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddTransactionState())
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()
    private val transactionId: String? = savedStateHandle.toRoute<AddTransactionRoute>().transactionId

    // Update individual fields as the user types
    fun onAmountChange(value: String) { _state.update { it.copy(amount = value) } }
    fun onCategoryChange(value: String) { _state.update { it.copy(category = value) } }
    fun onNotesChange(value: String) { _state.update { it.copy(notes = value) } }
    fun onTypeChange(type: String) { _state.update { it.copy(transactionType = type) } }

    fun onCategorySelect(category: Category) {
        _state.update { it.copy(categoryId = category.id, category = category.name) }
    }

    fun onAccountSelect(id: String, name: String) { _state.update { it.copy(accountId = id, accountName = name) } }
    fun onTransferAccountSelect(id: String, name: String) { _state.update { it.copy(transferAccountId = id, transferAccountName = name) } }

    init {
        // 1. Fetch Categories for the dropdown (Always run this, not just when editing)
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { list ->
                _state.update { it.copy(categories = list) }
            }
        }

        // 2. Fetch Accounts (Moved to its own launch block!)
        viewModelScope.launch {
            accountRepository.getAccountsWithBalances().collect { list ->
                _state.update {
                    it.copy(
                        accounts = list,
                        accountId = it.accountId ?: list.firstOrNull()?.id,
                        accountName = it.accountName.ifBlank { list.firstOrNull()?.name ?: "" }
                    )
                }
            }
        }

        // 3. If an ID was passed, we are EDITING! Load the transaction data.
        if (transactionId != null) {
            viewModelScope.launch {
                val existingTxn = repository.getTransactionById(transactionId)
                if (existingTxn != null) {
                    _state.update {
                        it.copy(
                            amount = existingTxn.amount.toString(),
                            category = existingTxn.category,
                            transactionType = existingTxn.transactionType,
                            notes = existingTxn.notes ?: "",
                            categoryId = existingTxn.categoryId,
                            accountId = existingTxn.accountId,
                        )
                    }

                    val account = accountRepository.getAccountsWithBalances().firstOrNull()?.find { it.id == existingTxn.accountId }
                    if (account != null) {
                        _state.update { it.copy(accountName = account.name) }
                    }
                }
            }
        }
    }

    fun saveTransaction() {
        val currentState = _state.value
        val parsedAmount = currentState.amount.toDoubleOrNull()
        
        if (parsedAmount == null || currentState.category.isBlank()) {
            return // Basic validation: In a real app, show an error message
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            val transaction = Transaction(
                id = transactionId ?: UUID.randomUUID().toString(), // Generate Offline ID
                amount = parsedAmount,
                transactionType = currentState.transactionType,
                merchant = null,
                category = currentState.category,
                transactionDate = System.currentTimeMillis(),
                paymentMethod = "CASH", // Defaulting for now
                source = "MANUAL",
                notes = currentState.notes,
                categoryId = currentState.categoryId,
                accountId = currentState.accountId,
                transferAccountId = if (currentState.transactionType == "TRANSFER") currentState.transferAccountId else null,
            )

            // Save to Room DB!
            repository.saveTransaction(transaction)

            // Trigger navigation back to the list
            _state.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }
}