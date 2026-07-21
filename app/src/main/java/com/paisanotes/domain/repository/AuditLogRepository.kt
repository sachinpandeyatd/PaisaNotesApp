package com.paisanotes.domain.repository

import com.paisanotes.domain.model.AuditLog
import kotlinx.coroutines.flow.Flow

interface AuditLogRepository {
    fun getAllLogs(): Flow<List<AuditLog>>
}