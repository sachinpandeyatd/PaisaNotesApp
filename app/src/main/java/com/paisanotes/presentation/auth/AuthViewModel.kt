package com.paisanotes.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paisanotes.data.remote.dto.LoginRequest
import com.paisanotes.data.remote.dto.RegisterRequest
import com.paisanotes.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun onNameChange(name: String) { _state.update { it.copy(name = name) } }
    fun onEmailChange(email: String) { _state.update { it.copy(email = email) } }
    fun onPasswordChange(password: String) { _state.update { it.copy(password = password) } }

    fun login() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = repository.login(LoginRequest(_state.value.email, _state.value.password))
            handleAuthResult(result)
        }
    }

    fun register() {
        viewModelScope.launch {
            val s = _state.value
            if (s.name.isBlank() || s.email.isBlank() || s.password.length < 6) {
                _state.update { it.copy(error = "Please fill all fields correctly.") }
                return@launch
            }

            _state.update { it.copy(isLoading = true, error = null) }
            val result = repository.register(RegisterRequest(s.name, s.email, s.password))
            handleAuthResult(result)
        }
    }

    fun googleLogin(idToken: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = repository.googleLogin(idToken)
            handleAuthResult(result)
        }
    }

    private fun handleAuthResult(result: Result<Unit>) {
        result.onSuccess {
            _state.update { it.copy(isLoading = false, loginSuccess = true) }
        }.onFailure { exception ->
            _state.update { it.copy(isLoading = false, error = exception.message) }
        }
    }
}