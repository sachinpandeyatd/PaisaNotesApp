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
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class AddEmiViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: EmiRepository
) : ViewModel() {
    private val personId: String = savedStateHandle.toRoute<AddEmiRoute>().personId
    private val _state = MutableStateFlow(AddEmiState())
    val state: StateFlow<AddEmiState> = _state.asStateFlow()

    fun onItemNameChange(v: String) { _state.update { it.copy(itemName = v) } }
    fun onPrincipalChange(v: String) { _state.update { it.copy(principal = v) } }
    fun onMonthlyAmountChange(v: String) { _state.update { it.copy(monthlyAmount = v) } }
    fun onTotalMonthsChange(v: String) { _state.update { it.copy(totalMonths = v) } }

    fun saveEmi() {
        val s = _state.value
        val pAmount = s.principal.toDoubleOrNull()
        val mAmount = s.monthlyAmount.toDoubleOrNull()
        val tMonths = s.totalMonths.toIntOrNull()

        if (pAmount == null || mAmount == null || tMonths == null || s.itemName.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val emi = Emi(
                id = UUID.randomUUID().toString(),
                personId = personId,
                refNumber = null,
                itemName = s.itemName,
                ownerType = "FRIEND", // We are adding this FOR a friend
                principalAmount = pAmount,
                monthlyEmiAmount = mAmount,
                totalMonths = tMonths,
                startDate = System.currentTimeMillis(),
                status = "ACTIVE"
            )
            repository.saveEmi(emi)
            _state.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }
}