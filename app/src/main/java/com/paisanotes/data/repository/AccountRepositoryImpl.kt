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

    override suspend fun getOrCreateAccountId(name: String): String {
        // 1. Try to find existing account
        val existingAccount = dao.findAccountByName(name)
        if (existingAccount != null) {
            return existingAccount.id
        }

        // 2. If it doesn't exist, create it dynamically!
        val newId = UUID.randomUUID().toString()
        val newAccount = com.paisanotes.data.local.entity.AccountEntity(
            id = newId,
            name = name,
            type = "SAVINGS", // Defaulting to SAVINGS for auto-captured bank accounts
            initialBalance = 0.0,
            syncStatus = com.paisanotes.data.local.entity.SyncStatus.PENDING_INSERT
        )

        dao.insertAccount(newAccount)
        triggerBackgroundSync()

        return newId
    }
}