package com.paisanotes.data.repository

import com.paisanotes.data.local.TokenManager
import com.paisanotes.data.local.dao.EmiDao
import com.paisanotes.data.local.dao.LoanDao
import com.paisanotes.data.local.dao.PersonDao
import com.paisanotes.data.local.dao.TransactionDao
import com.paisanotes.data.mapper.toDto
import com.paisanotes.data.mapper.toEntity
import com.paisanotes.data.remote.api.PaisaApiService
import com.paisanotes.data.remote.dto.SyncPushRequest
import com.paisanotes.domain.repository.SyncRepository
import javax.inject.Inject

class SyncRepositoryImpl @Inject constructor(
    private val api: PaisaApiService,
    private val transactionDao: TransactionDao,
    private val personDao: PersonDao,
    private val loanDao: LoanDao,
    private val emiDao: EmiDao,
    private val tokenManager: TokenManager
) : SyncRepository {

    override suspend fun syncWithServer(): Boolean {
        return try {
            // 1. Gather all unsynced data from all tables
            val unsyncedTransactions = transactionDao.getUnsyncedTransactions()
            val unsyncedPeople = personDao.getUnsyncedPeople()
             val unsyncedLoans = loanDao.getUnsyncedLoans()
             val unsyncedEmis = emiDao.getUnsyncedEmis()

            // 2. Only push if there is actually data to send
            if (unsyncedTransactions.isNotEmpty() || unsyncedPeople.isNotEmpty()
                || unsyncedLoans.isNotEmpty() || unsyncedEmis.isNotEmpty()) {
                val pushRequest = SyncPushRequest(
                    transactions = unsyncedTransactions.map { it.toDto() },
                    people = unsyncedPeople.map { it.toDto() },
                    loans = unsyncedLoans.map { it.toDto() },
                    emis = unsyncedEmis.map { it.toDto() }
                )

                // 3. Send massive payload to Spring Boot
                val pushResponse = api.pushData(pushRequest)

                // 4. If successful, mark everything as synced locally
                if (pushResponse.isSuccessful && pushResponse.body() != null) {
                    val body = pushResponse.body()!!

                    if (body.processedTransactionIds.isNotEmpty()) transactionDao.markAsSynced(body.processedTransactionIds)
                    if (body.processedPersonIds.isNotEmpty()) personDao.markAsSynced(body.processedPersonIds)
                    if (body.processedLoanIds.isNotEmpty()) loanDao.markAsSynced(body.processedLoanIds)
                    if (body.processedEmiIds.isNotEmpty()) emiDao.markAsSynced(body.processedEmiIds)
                } else {
                    return false // Network call failed (e.g. 500 error)
                }
            }
            val lastSyncTime = tokenManager.getLastSyncTime()
            val pullResponse = api.pullData(lastSyncTime)

            if (pullResponse.isSuccessful && pullResponse.body() != null) {
                val pullBody = pullResponse.body()!!

                // Update the app's watermark clock to the Server's clock!
                tokenManager.saveLastSyncTime(pullBody.serverTimestamp)

                // Convert Network JSON to SQLite Entities and Batch Insert!
                if (pullBody.transactions.isNotEmpty()) transactionDao.insertTransactions(pullBody.transactions.map { it.toEntity() })
                if (!pullBody.people.isNullOrEmpty()) personDao.insertPeople(pullBody.people.map { it.toEntity() })
                if (!pullBody.loans.isNullOrEmpty()) loanDao.insertLoans(pullBody.loans.map { it.toEntity() })
                if (!pullBody.emis.isNullOrEmpty()) emiDao.insertEmis(pullBody.emis.map { it.toEntity() })
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}