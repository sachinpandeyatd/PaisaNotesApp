package com.paisanotes.data.repository

import com.paisanotes.data.local.dao.TransactionDao
import com.paisanotes.data.local.entity.SyncStatus
import com.paisanotes.data.mapper.toDomainModel
import com.paisanotes.data.mapper.toDto
import com.paisanotes.data.mapper.toEntity
import com.paisanotes.data.remote.api.PaisaApiService
import com.paisanotes.data.remote.dto.SyncPushRequest
import com.paisanotes.domain.model.Transaction
import com.paisanotes.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao,
    private val api: PaisaApiService
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
    }

    // 4. THE SYNC ENGINE! (Pull -> Push)
    override suspend fun syncWithServer(): Boolean {
        return try {
            // STEP A: PULL (Download from Server)
            // TODO: In a real app, fetch lastSyncTime from SharedPreferences instead of null
            val pullResponse = api.pullData(lastSync = null)
            if (pullResponse.isSuccessful && pullResponse.body() != null) {
                val serverTransactions = pullResponse.body()!!.transactions

                // Convert incoming DTOs to Entities and save to Room
                val entitiesToInsert = serverTransactions.map { it.toEntity() }
                dao.insertTransactions(entitiesToInsert)
            }

            // STEP B: PUSH (Upload offline changes to Server)
            val unsyncedEntities = dao.getUnsyncedTransactions()
            if (unsyncedEntities.isNotEmpty()) {
                val pushRequest = SyncPushRequest(
                    transactions = unsyncedEntities.map { it.toDto() }
                )
                val pushResponse = api.pushData(pushRequest)

                // If the server processed them successfully, mark them as SYNCED locally!
                if (pushResponse.isSuccessful && pushResponse.body() != null) {
                    val processedIds = pushResponse.body()!!.processedTransactionIds
                    dao.markAsSynced(processedIds)
                }
            }
            true // Sync successful
        } catch (e: Exception) {
            e.printStackTrace()
            false // Sync failed (e.g., no internet)
        }
    }
}