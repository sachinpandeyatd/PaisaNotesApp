package com.paisanotes.presentation.add_emi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paisanotes.domain.model.Emi
import com.paisanotes.domain.repository.EmiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyEmisState(
    val emis: List<Emi> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class MyEmisViewModel @Inject constructor(
    private val emiRepository: EmiRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MyEmisState())
    val state: StateFlow<MyEmisState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            emiRepository.getMyEmis().collect { list ->
                _state.update { it.copy(emis = list, isLoading = false) }
            }
        }
    }

    fun recordEmiPayment(emiId: String) {
        viewModelScope.launch {
            emiRepository.recordEmiPayment(emiId)
        }
    }
}