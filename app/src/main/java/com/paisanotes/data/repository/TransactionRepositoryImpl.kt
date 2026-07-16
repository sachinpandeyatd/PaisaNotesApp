package com.paisanotes.data.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.paisanotes.data.local.dao.TransactionDao
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

        val entity = if (existingEntity == null) {
            transaction.toEntity(syncStatus = SyncStatus.PENDING_INSERT)
        } else {
            transaction.toEntity(
                syncStatus = SyncStatus.PENDING_UPDATE,
                createdAt = existingEntity.createdAt,
                updatedAt = System.currentTimeMillis() // Update the timestamp!
            )
        }

        dao.insertTransaction(entity) // Instantly saves locally. UI updates immediately.

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
}