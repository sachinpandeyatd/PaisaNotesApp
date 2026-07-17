package com.paisanotes.data.remote.api

import com.paisanotes.data.remote.dto.AuthResponse
import com.paisanotes.data.remote.dto.GoogleLoginRequest
import com.paisanotes.data.remote.dto.LoginRequest
import com.paisanotes.data.remote.dto.RegisterRequest
import com.paisanotes.data.remote.dto.SyncPullResponse
import com.paisanotes.data.remote.dto.SyncPushRequest
import com.paisanotes.data.remote.dto.SyncPushResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PaisaApiService {

    // --- AUTH ENDPOINTS ---
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // --- SYNC ENDPOINTS ---
    @GET("api/v1/sync/pull")
    suspend fun pullData(
        @Query("lastSync") lastSync: String? // Nullable for first-time sync
    ): Response<SyncPullResponse>

    @POST("api/v1/sync/push")
    suspend fun pushData(
        @Body request: SyncPushRequest
    ): Response<SyncPushResponse>

    @POST("api/v1/auth/google")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<AuthResponse>
}