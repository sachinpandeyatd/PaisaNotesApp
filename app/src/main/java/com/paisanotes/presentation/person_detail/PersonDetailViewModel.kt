package com.paisanotes.presentation.person_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.paisanotes.domain.model.Emi
import com.paisanotes.domain.model.Loan
import com.paisanotes.domain.model.Person
import com.paisanotes.domain.repository.EmiRepository
import com.paisanotes.domain.repository.LoanRepository
import com.paisanotes.domain.repository.PersonRepository
import com.paisanotes.presentation.navigation.PersonDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PersonDetailState(
    val person: Person? = null,
    val loans: List<Loan> = emptyList(),
    val proxyEmis: List<Emi> = emptyList(),
    val totalExposure: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class PersonDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle, // 🚨 INJECTED BY HILT
    private val personRepository: PersonRepository,
    private val loanRepository: LoanRepository,
    private val emiRepository: EmiRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PersonDetailState())
    val state: StateFlow<PersonDetailState> = _state.asStateFlow()

    init {
        // 🚨 MAGIC: Extract the safe argument directly from the Nav Route!
        val route = savedStateHandle.toRoute<PersonDetailRoute>()
        val personId = route.personId

        loadPersonData(personId)
    }

    private fun loadPersonData(personId: String) {
        viewModelScope.launch {
            // We use flow 'combine' to listen to Loans and EMIs simultaneously
            combine(
                personRepository.getAllPeople(),
                loanRepository.getLoansForPerson(personId),
                emiRepository.getEmisForPerson(personId)
            ) { people, loans, emis ->

                // 1. Find our person from the list
                val person = people.find { it.id == personId }

                // The SQLite query we wrote in PersonDao already calculated this perfectly.
                val totalExposure = person?.totalExposure ?: 0.0

                PersonDetailState(
                    person = person,
                    loans = loans,
                    proxyEmis = emis,
                    totalExposure = totalExposure, // Using the DB's math!
                    isLoading = false
                )
            }.collectLatest { combinedState ->
                _state.value = combinedState
            }
        }
    }

    fun recordLoanRepayment(loanId: String, amount: Double) {
        viewModelScope.launch { loanRepository.recordRepayment(loanId, amount) }
    }
    fun recordEmiPayment(emiId: String) {
        viewModelScope.launch { emiRepository.recordEmiPayment(emiId) }
    }
}