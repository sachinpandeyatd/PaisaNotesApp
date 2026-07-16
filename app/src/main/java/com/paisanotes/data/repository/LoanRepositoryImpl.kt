package com.paisanotes.data.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.paisanotes.data.local.dao.AuditLogDao
import com.paisanotes.data.local.dao.LoanDao
import com.paisanotes.data.local.entity.AuditLogEntity
import com.paisanotes.data.local.entity.SyncStatus
import com.paisanotes.data.mapper.toDomainModel
import com.paisanotes.data.mapper.toEntity
import com.paisanotes.domain.model.Loan
import com.paisanotes.domain.repository.LoanRepository
import com.paisanotes.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LoanRepositoryImpl @Inject constructor(
    private val dao: LoanDao,
    private val auditLogDao: AuditLogDao,
    @ApplicationContext private val context: Context
) : LoanRepository {

    private fun triggerBackgroundSync() {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(constraints).build()
        WorkManager.getInstance(context).enqueueUniqueWork("paisa_sync_work", ExistingWorkPolicy.REPLACE, syncWorkRequest)
    }

    override fun getLoansForPerson(personId: String): Flow<List<Loan>> {
        return dao.getLoansByPerson(personId).map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun saveLoan(loan: Loan) {
        // Since we don't have Edit Loan yet, we assume CREATE for now.
        // We will update this when we build the Edit feature.
        val entity = loan.toEntity().copy(syncStatus = SyncStatus.PENDING_INSERT)
        dao.insertLoan(entity)

        // 🚨 CREATE AUDIT LOG
        val metadataJson = """{"amountLent": ${loan.amountLent}, "status": "${loan.status}"}"""
        val auditLog = AuditLogEntity(
            entityType = "LOAN",
            entityId = loan.id,
            actionType = "CREATE",
            metadata = metadataJson
        )
        auditLogDao.insertLog(auditLog)

        triggerBackgroundSync()
    }
}