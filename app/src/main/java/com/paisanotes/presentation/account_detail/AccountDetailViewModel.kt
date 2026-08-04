package com.paisanotes.presentation.account_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.paisanotes.domain.model.Account
import com.paisanotes.domain.model.Transaction
import com.paisanotes.domain.repository.AccountRepository
import com.paisanotes.domain.repository.TransactionRepository
import com.paisanotes.presentation.navigation.AccountDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AccountDetailState(
    val account: Account? = null,
    val transactions: List<Transaction> = emptyList(),
    val allAccounts: List<Account> = emptyList(), // Needed for the "Pay from" dropdown
    val isLoading: Boolean = true
)

@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val accountId = savedStateHandle.toRoute<AccountDetailRoute>().accountId
    private val _state = MutableStateFlow(AccountDetailState())
    val state: StateFlow<AccountDetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                accountRepository.getAccountWithBalance(accountId),
                transactionRepository.getTransactionsForAccount(accountId),
                accountRepository.getAccountsWithBalances()
            ) { account, txns, allAccs ->
                AccountDetailState(
                    account = account,
                    transactions = txns,
                    allAccounts = allAccs.filter { it.id != accountId }, // Exclude this account from payment sources
                    isLoading = false
                )
            }.collectLatest { _state.value = it }
        }
    }

    // 🚨 1. PAY BUTTON LOGIC (Transfer from Savings to CC)
    fun recordPayment(amount: Double, fromAccountId: String) {
        viewModelScope.launch {
            val txn = Transaction(
                id = UUID.randomUUID().toString(),
                amount = amount,
                transactionType = "TRANSFER",
                merchant = null, category = "Credit Card Payment", categoryId = null,
                accountId = fromAccountId, // Money leaves here
                transferAccountId = accountId, // Money arrives here
                transactionDate = System.currentTimeMillis(),
                paymentMethod = "ONLINE", source = "MANUAL", notes = "Card Repayment"
            )
            transactionRepository.saveTransaction(txn)
        }
    }

    // 🚨 2. RESET BUTTON LOGIC (Forces balance to 0 for a new month)
    fun resetBalance() {
        val currentBalance = _state.value.account?.currentBalance ?: return
        if (currentBalance == 0.0) return

        viewModelScope.launch {
            // If balance is negative (e.g. -5000), we add 5000 INCOME to zero it out.
            // If balance is positive, we add an EXPENSE to zero it out.
            val type = if (currentBalance < 0) "INCOME" else "EXPENSE"
            val amount = kotlin.math.abs(currentBalance)

            val txn = Transaction(
                id = UUID.randomUUID().toString(),
                amount = amount,
                transactionType = type,
                merchant = null, category = "Balance Adjustment", categoryId = null,
                accountId = accountId, transferAccountId = null,
                transactionDate = System.currentTimeMillis(),
                paymentMethod = "SYSTEM", source = "MANUAL", notes = "Manual Reset for New Month"
            )
            transactionRepository.saveTransaction(txn)
        }
    }
}