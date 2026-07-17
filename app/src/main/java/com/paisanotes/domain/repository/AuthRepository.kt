package com.paisanotes.domain.repository

import com.paisanotes.data.remote.dto.LoginRequest
import com.paisanotes.data.remote.dto.RegisterRequest

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<Unit>
    suspend fun register(request: RegisterRequest): Result<Unit>
    fun isLoggedIn(): Boolean
    fun logout()
    suspend fun googleLogin(idToken: String): Result<Unit>
}