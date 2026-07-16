package com.paisanotes.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.paisanotes.data.local.TokenManager
import com.paisanotes.data.local.dao.AuditLogDao
import com.paisanotes.data.local.dao.EmiDao
import com.paisanotes.data.local.dao.LoanDao
import com.paisanotes.data.local.dao.PersonDao
import com.paisanotes.data.local.dao.TransactionDao
import com.paisanotes.data.local.entity.AuditLogEntity
import com.paisanotes.data.local.entity.SyncStatus
import com.paisanotes.data.mapper.toDto
import com.paisanotes.data.mapper.toEntity
import com.paisanotes.data.remote.api.PaisaApiService
import com.paisanotes.data.remote.dto.AuditLogDto
import com.paisanotes.data.remote.dto.SyncPushRequest
import com.paisanotes.domain.repository.SyncRepository
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class SyncRepositoryImpl @Inject constructor(
    private val api: PaisaApiService,
    private val transactionDao: TransactionDao,
    private val personDao: PersonDao,
    private val loanDao: LoanDao,
    private val emiDao: EmiDao,
    private val tokenManager: TokenManager,
    private val auditLogDao: AuditLogDao
) : SyncRepository {

    override suspend fun syncWithServer(): Boolean {
        return try {
            // 1. Gather all unsynced data from all tables
            val unsyncedTransactions = transactionDao.getUnsyncedTransactions()
            val unsyncedPeople = personDao.getUnsyncedPeople()
            val unsyncedLoans = loanDao.getUnsyncedLoans()
            val unsyncedEmis = emiDao.getUnsyncedEmis()
            val unsyncedLogs = auditLogDao.getUnsyncedLogs()

            // 2. Only push if there is actually data to send
            if (unsyncedTransactions.isNotEmpty() || unsyncedPeople.isNotEmpty()
                || unsyncedLoans.isNotEmpty() || unsyncedEmis.isNotEmpty()) {
                val pushRequest = SyncPushRequest(
                    transactions = unsyncedTransactions.map { it.toDto() },
                    people = unsyncedPeople.map { it.toDto() },
                    loans = unsyncedLoans.map { it.toDto() },
                    emis = unsyncedEmis.map { it.toDto() },
                    auditLogs = unsyncedLogs.map { it.toDto() }
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
                    if (body.processedAuditLogIds.isNotEmpty()) auditLogDao.markAsSynced(body.processedAuditLogIds)
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
                if (!pullBody.auditLogs.isNullOrEmpty()) auditLogDao.insertLogs(pullBody.auditLogs.map { it.toEntity() })
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun AuditLogEntity.toDto(): AuditLogDto {
        val formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC"))

        // Convert String JSON back to Map<String, Any> for Retrofit
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        val metadataMap: Map<String, Any> = Gson().fromJson(metadata, mapType)

        return AuditLogDto(
            id = id,
            entityType = entityType,
            entityId = entityId,
            actionType = actionType,
            metadata = metadataMap,
            createdAt = formatter.format(Instant.ofEpochMilli(createdAt))
        )
    }

    fun AuditLogDto.toEntity(): AuditLogEntity {
        return AuditLogEntity(
            id = id,
            entityType = entityType,
            entityId = entityId,
            actionType = actionType,
            metadata = Gson().toJson(metadata),
            createdAt = java.time.ZonedDateTime.parse(createdAt).toInstant().toEpochMilli(),
            syncStatus = SyncStatus.SYNCED
        )
    }
}