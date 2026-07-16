package com.paisanotes.domain.repository

interface SyncRepository {
    suspend fun syncWithServer(): Boolean
}