package com.paisanotes.presentation.person_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.paisanotes.domain.model.AuditLog
import com.paisanotes.domain.model.Emi
import com.paisanotes.domain.model.Loan
import com.paisanotes.domain.model.Person
import com.paisanotes.domain.repository.AuditLogRepository
import com.paisanotes.domain.repository.EmiRepository
import com.paisanotes.domain.repository.LoanRepository
import com.paisanotes.domain.repository.PersonRepository
import com.paisanotes.presentation.navigation.PersonDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
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
    val isLoading: Boolean = true,

    val showEditDialog: Boolean = false,
    val editName: String = "",
    val editPhone: String = "",
    val deleteSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class PersonDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val personRepository: PersonRepository,
    private val loanRepository: LoanRepository,
    private val emiRepository: EmiRepository,
    private val auditLogRepository: AuditLogRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PersonDetailState())
    val state: StateFlow<PersonDetailState> = _state.asStateFlow()

    init {
        val route = savedStateHandle.toRoute<PersonDetailRoute>()
        val personId = route.personId

        loadPersonData(personId)
    }

    fun getEmiHistory(emiId: String): Flow<List<AuditLog>> {
        return auditLogRepository.getLogsForEntity(emiId)
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
    fun recordEmiPayment(emiId: String, amount: Double, monthName: String) {
        viewModelScope.launch {
            emiRepository.recordEmiPayment(emiId, amount, monthName)
        }
    }

    // --- EDIT PERSON LOGIC ---

    fun openEditDialog() {
        val p = _state.value.person ?: return
        _state.update { it.copy(showEditDialog = true, editName = p.name, editPhone = p.phoneNumber ?: "") }
    }

    fun closeEditDialog() {
        _state.update { it.copy(showEditDialog = false, errorMessage = null) }
    }

    fun onEditNameChange(name: String) { _state.update { it.copy(editName = name) } }
    fun onEditPhoneChange(phone: String) { _state.update { it.copy(editPhone = phone) } }

    fun savePersonEdits() {
        val s = _state.value
        val personId = s.person?.id ?: return
        if (s.editName.isBlank()) return

        viewModelScope.launch {
            personRepository.updatePerson(personId, s.editName, s.editPhone)
            closeEditDialog()
        }
    }

    fun deletePerson() {
        val s = _state.value

        if (s.totalExposure != 0.0) {
            _state.update {
                it.copy(errorMessage = "Cannot delete a friend with an active balance. Total Exposure must be exactly ₹0.00.")
            }
            return // Stop execution!
        }

        val personId = s.person?.id ?: return
        viewModelScope.launch {
            personRepository.deletePerson(personId)
            _state.update { it.copy(showEditDialog = false, deleteSuccess = true) }
        }
    }

    fun editEmiPayment(logId: String, emiId: String, transactionId: String?, oldAmount: Double, newAmount: Double, newMonth: String) {
        viewModelScope.launch {
            emiRepository.editEmiPayment(logId, emiId, transactionId, oldAmount, newAmount, newMonth)
        }
    }
}