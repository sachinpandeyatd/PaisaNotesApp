package com.paisanotes.presentation.add_loan

import androidx.compose.ui.text.font.Typeface
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.paisanotes.domain.model.Loan
import com.paisanotes.domain.repository.LoanRepository
import com.paisanotes.presentation.navigation.AddLoanRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddLoanState(
    val type: String = "LENT",
    val amount: String = "",
    val notes: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class AddLoanViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle, // Automatically grabs personId from NavHost!
    private val repository: LoanRepository
) : ViewModel() {

    private val personId: String = savedStateHandle.toRoute<AddLoanRoute>().personId

    private val _state = MutableStateFlow(AddLoanState())
    val state: StateFlow<AddLoanState> = _state.asStateFlow()

    fun onAmountChange(value: String) { _state.update { it.copy(amount = value) } }
    fun onNotesChange(value: String) { _state.update { it.copy(notes = value) } }
    fun onTypeChange(type: String) { _state.update { it.copy(type = type) } }

    fun saveLoan() {
        val parsedAmount = _state.value.amount.toDoubleOrNull()
        if (parsedAmount == null || parsedAmount <= 0) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val loan = Loan(
                id = UUID.randomUUID().toString(),
                personId = personId,
                type = _state.value.type,
                amountLent = parsedAmount,
                dateGiven = System.currentTimeMillis(),
                expectedReturnDate = null,
                status = "ACTIVE",
                notes = _state.value.notes
            )
            repository.saveLoan(loan) // Saves to Room!
            _state.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }
}