package com.paisanotes.data.repository

import android.content.Context
import androidx.work.*
import com.paisanotes.data.local.dao.AccountDao
import com.paisanotes.data.local.entity.SyncStatus
import com.paisanotes.data.mapper.toDomainModel
import com.paisanotes.data.mapper.toEntity
import com.paisanotes.domain.model.Account
import com.paisanotes.domain.repository.AccountRepository
import com.paisanotes.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val dao: AccountDao,
    @ApplicationContext private val context: Context
) : AccountRepository {

    private fun triggerBackgroundSync() {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val request = OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(constraints).build()
        WorkManager.getInstance(context).enqueueUniqueWork("paisa_sync_work", ExistingWorkPolicy.REPLACE, request)
    }

    override fun getAccountsWithBalances(): Flow<List<Account>> {
        return dao.getAccountsWithBalances().map { tuples -> tuples.map { it.toDomainModel() } }
    }

    override suspend fun saveAccount(name: String, type: String, initialBalance: Double) {
        val account = Account(
            id = UUID.randomUUID().toString(),
            name = name,
            type = type,
            initialBalance = initialBalance
        ).toEntity(syncStatus = SyncStatus.PENDING_INSERT)
        
        dao.insertAccount(account)
        triggerBackgroundSync()
    }
}