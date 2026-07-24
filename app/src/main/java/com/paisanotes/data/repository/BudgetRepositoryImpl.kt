package com.paisanotes.data.repository

import android.content.Context
import androidx.work.*
import com.paisanotes.data.local.dao.BudgetDao
import com.paisanotes.data.local.entity.SyncStatus
import com.paisanotes.data.mapper.toDomainModel
import com.paisanotes.data.mapper.toEntity
import com.paisanotes.domain.model.Budget
import com.paisanotes.domain.model.BudgetProgress
import com.paisanotes.domain.repository.BudgetRepository
import com.paisanotes.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val dao: BudgetDao,
    @ApplicationContext private val context: Context
) : BudgetRepository {

    private fun triggerBackgroundSync() {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(constraints).build()
        WorkManager.getInstance(context).enqueueUniqueWork("paisa_sync_work", ExistingWorkPolicy.REPLACE, syncWorkRequest)
    }

    override fun getBudgetsWithProgress(startOfMonth: Long, endOfMonth: Long): Flow<List<BudgetProgress>> {
        return dao.getBudgetsWithProgress(startOfMonth, endOfMonth).map { tuples ->
            tuples.map { it.toDomainModel() }
        }
    }

    override suspend fun saveBudget(categoryId: String, monthlyLimit: Double) {
        val existingEntity = dao.getBudgetByCategory(categoryId)

        val entity = if (existingEntity != null) {
            existingEntity.copy(
                monthlyLimit = monthlyLimit,
                updatedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING_UPDATE
            )
        } else {
            Budget(
                id = UUID.randomUUID().toString(),
                categoryId = categoryId,
                monthlyLimit = monthlyLimit
            ).toEntity(syncStatus = SyncStatus.PENDING_INSERT)
        }

        dao.insertBudget(entity)
        triggerBackgroundSync()
    }
}