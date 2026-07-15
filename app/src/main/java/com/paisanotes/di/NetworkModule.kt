package com.paisanotes.di

import com.paisanotes.data.local.TokenManager
import com.paisanotes.data.remote.api.AuthInterceptor
import com.paisanotes.data.remote.api.PaisaApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // 1. Provide the Base URL of your Spring Boot server
    // For Android Emulator to access your local Spring Boot backend, use 10.0.2.2!
    private const val BASE_URL = "http://192.168.18.2:8080/"

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        // This logs all HTTP requests/responses to the Logcat (like Spring's show-sql)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // Attach the JWT token
            .addInterceptor(loggingInterceptor) // Log the traffic
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()) // Converts JSON to Kotlin Data Classes
            .build()
    }

    @Provides
    @Singleton
    fun providePaisaApiService(retrofit: Retrofit): PaisaApiService {
        // Retrofit dynamically generates the implementation of our interface here!
        return retrofit.create(PaisaApiService::class.java)
    }
}