package com.paisanotes.presentation.add_emi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paisanotes.domain.model.AuditLog
import com.paisanotes.domain.model.Emi
import com.paisanotes.domain.repository.AuditLogRepository
import com.paisanotes.domain.repository.EmiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyEmisState(
    val emis: List<Emi> = emptyList(),
    val recentAutoCaptures: List<com.paisanotes.domain.model.Transaction> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class MyEmisViewModel @Inject constructor(
    private val emiRepository: EmiRepository,
    private val auditLogRepository: AuditLogRepository,
    private val transactionRepository: com.paisanotes.domain.repository.TransactionRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MyEmisState())
    val state: StateFlow<MyEmisState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                emiRepository.getMyEmis(),
                transactionRepository.getRecentAutoCapturedTransactions()
            ) { emisList, autoCaptures ->
                MyEmisState(
                    emis = emisList,
                    recentAutoCaptures = autoCaptures,
                    isLoading = false
                )
            }.collectLatest { combinedState ->
                _state.value = combinedState
            }
        }
    }

    fun recordEmiPayment(emiId: String, amount: Double, monthName: String, linkedTxnId:String?) {
        viewModelScope.launch {
            emiRepository.recordEmiPayment(emiId, amount, monthName, linkedTxnId)
        }
    }

    fun getEmiHistory(emiId: String): Flow<List<AuditLog>> {
        return auditLogRepository.getLogsForEntity(emiId)
    }

    fun editEmiPayment(logId: String, emiId: String, transactionId: String?, oldAmount: Double, newAmount: Double, newMonth: String) {
        viewModelScope.launch {
            emiRepository.editEmiPayment(logId, emiId, transactionId, oldAmount, newAmount, newMonth)
        }
    }
}