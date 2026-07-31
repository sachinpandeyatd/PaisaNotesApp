package com.paisanotes.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paisanotes.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun deleteAccount() {
        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true, error = null) }
            val result = authRepository.deleteAccount()

            result.onSuccess {
                _state.update { it.copy(isDeleting = false, deleteSuccess = true) }
            }.onFailure { e ->
                _state.update { it.copy(isDeleting = false, error = e.message) }
            }
        }
    }
}