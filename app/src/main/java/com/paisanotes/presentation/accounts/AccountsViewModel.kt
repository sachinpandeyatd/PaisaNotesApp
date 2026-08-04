package com.paisanotes.presentation.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paisanotes.domain.model.Account
import com.paisanotes.domain.model.CreditCardBill
import com.paisanotes.domain.repository.AccountRepository
import com.paisanotes.domain.repository.CreditCardBillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountsState(
    val accounts: List<Account> = emptyList(),
    val isLoading: Boolean = true,
    val activeBills: List<CreditCardBill> = emptyList(),
)

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val ccBillRepository: CreditCardBillRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AccountsState())
    val state: StateFlow<AccountsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // Use combine to listen to both Flows at the same time!
            kotlinx.coroutines.flow.combine(
                accountRepository.getAccountsWithBalances(),
                ccBillRepository.getActiveBills()
            ) { accounts, bills ->
                AccountsState(accounts = accounts, activeBills = bills, isLoading = false)
            }.collectLatest { combinedState ->
                _state.value = combinedState
            }
        }
    }

    fun saveAccount(name: String, type: String, initialBalance: Double, statementDay: Int?, dueDay: Int?) {
        viewModelScope.launch {
            // NOTE: You'll need to update AccountRepository.saveAccount to accept statementDay/dueDay!
            accountRepository.saveAccount(name, type, initialBalance, statementDay, dueDay)
        }
    }

    // Payment Function
    fun payCreditCardBill(billId: String, amount: Double, fromAccountId: String) {
        viewModelScope.launch {
            ccBillRepository.recordBillPayment(billId, amount, fromAccountId)
        }
    }
}