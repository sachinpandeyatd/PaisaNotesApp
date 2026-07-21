package com.paisanotes.data.repository

import com.paisanotes.data.local.TokenManager
import com.paisanotes.data.remote.api.PaisaApiService
import com.paisanotes.data.remote.dto.ForgotPasswordRequest
import com.paisanotes.data.remote.dto.GoogleLoginRequest
import com.paisanotes.data.remote.dto.LoginRequest
import com.paisanotes.data.remote.dto.RegisterRequest
import com.paisanotes.data.remote.dto.ResetPasswordRequest
import com.paisanotes.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: PaisaApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(request: LoginRequest): Result<Unit> {
        return withContext(Dispatchers.IO) { // Run on background thread
            try {
                val response = api.login(request)
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    tokenManager.saveToken(authResponse.token) // SAVE JWT!
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Invalid email or password"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Network error. Is Spring Boot running?"))
            }
        }
    }

    override suspend fun register(request: RegisterRequest): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.register(request)
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    tokenManager.saveToken(authResponse.token) // SAVE JWT!
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Registration failed. Email might exist."))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Network error."))
            }
        }
    }

    override fun isLoggedIn(): Boolean {
        return tokenManager.getToken() != null
    }

    override fun logout() {
        tokenManager.clearToken()
    }

    override suspend fun googleLogin(idToken: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val request = GoogleLoginRequest(idToken)
                val response = api.googleLogin(request)
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    tokenManager.saveToken(authResponse.token) // SAVE JWT!
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Google Sign-In rejected by server"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Network error. Is Spring Boot running?"))
            }
        }
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.forgotPassword(ForgotPasswordRequest(email))
                if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Failed"))
            } catch (e: Exception) { Result.failure(Exception("Network error")) }
        }
    }

    override suspend fun resetPassword(email: String, otp: String, newPassword: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.resetPassword(ResetPasswordRequest(email, otp, newPassword))
                if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Invalid or Expired OTP"))
            } catch (e: Exception) { Result.failure(Exception("Network error")) }
        }
    }
}