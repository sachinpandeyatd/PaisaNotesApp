package com.paisanotes.data.repository

import com.paisanotes.data.local.dao.AuditLogDao
import com.paisanotes.data.mapper.toDomainModel
import com.paisanotes.domain.model.AuditLog
import com.paisanotes.domain.repository.AuditLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuditLogRepositoryImpl @Inject constructor(
    private val dao: AuditLogDao
) : AuditLogRepository {

    override fun getAllLogs(): Flow<List<AuditLog>> {
        return dao.getAllLogs().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
}