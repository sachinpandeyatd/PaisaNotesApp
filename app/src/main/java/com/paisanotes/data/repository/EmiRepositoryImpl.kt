package com.paisanotes.data.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.paisanotes.data.local.dao.AuditLogDao
import com.paisanotes.data.local.dao.EmiDao
import com.paisanotes.data.local.entity.AuditLogEntity
import com.paisanotes.data.local.entity.SyncStatus
import com.paisanotes.data.mapper.toDomainModel
import com.paisanotes.data.mapper.toEntity
import com.paisanotes.domain.model.Emi
import com.paisanotes.domain.repository.EmiRepository
import com.paisanotes.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EmiRepositoryImpl @Inject constructor(
    private val dao: EmiDao,
    private val auditLogDao: AuditLogDao,
    @ApplicationContext private val context: Context
) : EmiRepository {

    private fun triggerBackgroundSync() {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(constraints).build()
        WorkManager.getInstance(context).enqueueUniqueWork("paisa_sync_work", ExistingWorkPolicy.REPLACE, syncWorkRequest)
    }

    override fun getEmisForPerson(personId: String): Flow<List<Emi>> {
        return dao.getEmisByPerson(personId).map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun saveEmi(emi: Emi) {
        val entity = emi.toEntity().copy(syncStatus = SyncStatus.PENDING_INSERT)
        dao.insertEmi(entity)

        // 🚨 CREATE AUDIT LOG
        val metadataJson = """{"itemName": "${emi.itemName}", "principal": ${emi.principalAmount}, "monthly": ${emi.monthlyEmiAmount}}"""
        val auditLog = AuditLogEntity(
            entityType = "EMI",
            entityId = emi.id,
            actionType = "CREATE",
            metadata = metadataJson
        )
        auditLogDao.insertLog(auditLog)

        triggerBackgroundSync()
    }
}