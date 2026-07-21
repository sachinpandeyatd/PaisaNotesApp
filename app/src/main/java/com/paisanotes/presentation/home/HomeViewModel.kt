package com.paisanotes.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paisanotes.domain.model.Transaction
import com.paisanotes.domain.repository.PersonRepository
import com.paisanotes.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class HomeState(
    val thisMonthIncome: Double = 0.0,
    val thisMonthExpense: Double = 0.0,
    val totalExposure: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val personRepository: PersonRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            // Calculate timestamps for the 1st and last day of the current month
            val now = LocalDate.now()
            val startOfMonth = now.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            // Combine 4 different database flows!
            combine(
                transactionRepository.getIncomeBetween(startOfMonth, endOfMonth),
                transactionRepository.getExpenseBetween(startOfMonth, endOfMonth),
                personRepository.getAllPeople(), // We sum up the exposure from all people
                transactionRepository.getRecentTransactions(limit = 5)
            ) { income, expense, peopleList, recentTxns ->
                
                // Calculate how much money all friends combined owe you
                val exposure = peopleList.sumOf { it.totalExposure }

                HomeState(
                    thisMonthIncome = income,
                    thisMonthExpense = expense,
                    totalExposure = exposure,
                    recentTransactions = recentTxns,
                    isLoading = false
                )
            }.collectLatest { combinedState ->
                _state.value = combinedState
            }
        }
    }
}