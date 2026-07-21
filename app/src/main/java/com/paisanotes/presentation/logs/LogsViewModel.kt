package com.paisanotes.presentation.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paisanotes.domain.model.AuditLog
import com.paisanotes.domain.repository.AuditLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LogsState(
    val logs: List<AuditLog> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class LogsViewModel @Inject constructor(
    private val repository: AuditLogRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LogsState())
    val state: StateFlow<LogsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllLogs().collect { logsList ->
                _state.update { it.copy(logs = logsList, isLoading = false) }
            }
        }
    }
}