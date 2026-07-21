package com.paisanotes.data.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.paisanotes.data.local.dao.AuditLogDao
import com.paisanotes.data.local.dao.TransactionDao
import com.paisanotes.data.local.entity.AuditLogEntity
import com.paisanotes.data.local.entity.SyncStatus
import com.paisanotes.data.mapper.toDomainModel
import com.paisanotes.data.mapper.toEntity
import com.paisanotes.domain.model.Transaction
import com.paisanotes.domain.repository.TransactionRepository
import com.paisanotes.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao,
    private val auditLogDao: AuditLogDao,
    @ApplicationContext private val context: Context
) : TransactionRepository {

    // 1. UI observes this. We use Kotlin's .map operator on the Flow to convert Entities to Domain Models!
    override fun getAllTransactions(): Flow<List<Transaction>> {
        return dao.getAllActiveTransactions().map { entityList ->
            entityList.map { it.toDomainModel() }
        }
    }

    // 2. Offline-First Save
    override suspend fun saveTransaction(transaction: Transaction) {
        val existingEntity = dao.getTransactionById(transaction.id)

        val actionType = if (existingEntity == null) "CREATE" else "UPDATE"

        val metadataJson = """{"amount": ${transaction.amount}, "category": "${transaction.category}"}"""

        val entity = if (existingEntity == null) {
            transaction.toEntity(syncStatus = SyncStatus.PENDING_INSERT)
        } else {
            transaction.toEntity(
                syncStatus = SyncStatus.PENDING_UPDATE,
                createdAt = existingEntity.createdAt,
                updatedAt = System.currentTimeMillis()
            )
        }

        dao.insertTransaction(entity)

        val auditLog = AuditLogEntity(
            entityType = "TRANSACTION",
            entityId = transaction.id,
            actionType = actionType,
            metadata = metadataJson
        )
        auditLogDao.insertLog(auditLog)

        triggerBackgroundSync()
    }

    // 3. Offline-First Soft Delete
    override suspend fun deleteTransaction(transactionId: String) {
        val entity = dao.getTransactionById(transactionId) ?: return
        val deletedEntity = entity.copy(
            isDeleted = true,
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING_DELETE
        )
        dao.updateTransaction(deletedEntity)

        val metadataJson = """{"amount": ${entity.amount}, "category": "${entity.category}"}"""

        val auditLog = AuditLogEntity(
            entityType = "TRANSACTION",
            entityId = transactionId,
            actionType = "DELETE",
            metadata = metadataJson
        )
        auditLogDao.insertLog(auditLog)

        triggerBackgroundSync()
    }

    // Helper method to enqueue work
    private fun triggerBackgroundSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Only run if internet is available
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        // REPLACE means if a sync is already running, cancel it and start a fresh one
        WorkManager.getInstance(context).enqueueUniqueWork(
            "paisa_sync_work",
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )
    }

    override suspend fun getTransactionById(id: String): Transaction? {
        // Fetch from DAO and convert Entity to Domain Model
        return dao.getTransactionById(id)?.toDomainModel()
    }

    override fun getIncomeBetween(startDate: Long, endDate: Long): Flow<Double> {
        return dao.getIncomeBetween(startDate, endDate).map { it ?: 0.0 }
    }

    override fun getExpenseBetween(startDate: Long, endDate: Long): Flow<Double> {
        return dao.getExpenseBetween(startDate, endDate).map { it ?: 0.0 }
    }

    override fun getRecentTransactions(limit: Int): Flow<List<Transaction>> {
        return dao.getRecentTransactions(limit).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
}