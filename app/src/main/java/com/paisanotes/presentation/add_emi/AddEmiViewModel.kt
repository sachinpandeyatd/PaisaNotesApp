package com.paisanotes.presentation.add_emi

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.paisanotes.domain.model.Emi
import com.paisanotes.domain.repository.EmiRepository
import com.paisanotes.presentation.navigation.AddEmiRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddEmiState(
    val itemName: String = "",
    val principal: String = "",
    val monthlyAmount: String = "",
    val totalMonths: String = "",
    val refNumber: String = "",
    val totalAmountWithInterest: String = "",
    val interestRate: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class AddEmiViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: EmiRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AddEmiState())
    val state: StateFlow<AddEmiState> = _state.asStateFlow()

    private val route = savedStateHandle.toRoute<AddEmiRoute>()
    private val personId: String? = route.personId
    private val emiId: String? = route.emiId

    init {
        if (emiId != null) {
            viewModelScope.launch {
                val existingEmi = repository.getEmiById(emiId)
                if (existingEmi != null) {
                    _state.update {
                        it.copy(
                            isEditing = true,
                            itemName = existingEmi.itemName,
                            principal = existingEmi.principalAmount.toString(),
                            monthlyAmount = existingEmi.monthlyEmiAmount.toString(),
                            totalMonths = existingEmi.totalMonths.toString(),
                            refNumber = existingEmi.refNumber ?: "",
                            totalAmountWithInterest = existingEmi.totalAmountWithInterest.toString(),
                            interestRate = existingEmi.interestRate?.toString() ?: ""
                        )
                    }
                }
            }
        }
    }

    fun onItemNameChange(v: String) { _state.update { it.copy(itemName = v) } }
    fun onPrincipalChange(v: String) { _state.update { it.copy(principal = v) } }
    fun onMonthlyAmountChange(v: String) { _state.update { it.copy(monthlyAmount = v) } }
    fun onTotalMonthsChange(v: String) { _state.update { it.copy(totalMonths = v) } }
    fun onRefNumberChange(v: String) { _state.update { it.copy(refNumber = v) } }
    fun onTotalAmountWithInterestChange(v: String) { _state.update { it.copy(totalAmountWithInterest = v) } }
    fun onInterestRateChange(v: String) { _state.update { it.copy(interestRate = v) } }

    fun saveEmi() {
        val s = _state.value
        val pAmount = s.principal.toDoubleOrNull()
        val mAmount = s.monthlyAmount.toDoubleOrNull()
        val tMonths = s.totalMonths.toIntOrNull()

        if (pAmount == null || mAmount == null || tMonths == null || s.itemName.isBlank()) {
            _state.update { it.copy(isSaving = false) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            val totalAmt = s.totalAmountWithInterest.toDoubleOrNull() ?: pAmount
            val intRate = s.interestRate.toDoubleOrNull()

            // Fetch the actual existing data from Room so we don't erase history!
            val existingEmi = if (emiId != null) repository.getEmiById(emiId) else null

            // Safely copy existing data, or create new if it's null
            val emi = if (existingEmi != null) {
                existingEmi.copy(
                    itemName = s.itemName,
                    principalAmount = pAmount,
                    monthlyEmiAmount = mAmount,
                    totalMonths = tMonths,
                    totalAmountWithInterest = totalAmt,
                    interestRate = intRate,
                    refNumber = s.refNumber.takeIf { it.isNotBlank() },
                    // Check if the new edits mean the EMI is now fully paid!
                    status = if (existingEmi.amountPaid >= totalAmt || existingEmi.completedMonths >= tMonths) "CLOSED" else "ACTIVE"
                )
            } else {
                com.paisanotes.domain.model.Emi(
                    id = java.util.UUID.randomUUID().toString(),
                    personId = personId,
                    refNumber = s.refNumber.takeIf { it.isNotBlank() },
                    itemName = s.itemName,
                    ownerType = if (personId == null) "ME" else "FRIEND",
                    principalAmount = pAmount,
                    monthlyEmiAmount = mAmount,
                    totalMonths = tMonths,
                    completedMonths = 0,
                    totalAmountWithInterest = totalAmt,
                    interestRate = intRate,
                    amountPaid = 0.0,
                    startDate = System.currentTimeMillis(),
                    status = "ACTIVE"
                )
            }

            repository.saveEmi(emi)
            _state.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }

    fun deleteEmi() {
        if (emiId == null) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            repository.deleteEmi(emiId)
            _state.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }
}