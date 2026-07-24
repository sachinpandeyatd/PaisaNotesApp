package com.paisanotes.presentation.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paisanotes.domain.model.Account
import com.paisanotes.domain.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountsState(
    val accounts: List<Account> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val repository: AccountRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AccountsState())
    val state: StateFlow<AccountsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAccountsWithBalances().collect { accounts ->
                _state.update { it.copy(accounts = accounts, isLoading = false) }
            }
        }
    }

    fun saveAccount(name: String, type: String, initialBalance: Double) {
        viewModelScope.launch { repository.saveAccount(name, type, initialBalance) }
    }
}