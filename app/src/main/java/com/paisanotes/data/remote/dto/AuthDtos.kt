package com.paisanotes.data.remote.dto

// Sent to server
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val name: String, val email: String, val password: String)

// Received from server
data class AuthResponse(
    val token: String,
    val userId: String,
    val name: String,
    val email: String
)

data class GoogleLoginRequest(
    val idToken: String
)