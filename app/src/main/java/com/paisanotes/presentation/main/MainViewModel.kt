package com.paisanotes.presentation.main

import androidx.lifecycle.ViewModel
import com.paisanotes.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    fun logout() {
        authRepository.logout() // Clears the token from SharedPreferences
    }
}