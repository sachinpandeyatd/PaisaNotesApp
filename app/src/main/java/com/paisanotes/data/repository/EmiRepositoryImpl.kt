package com.paisanotes.data.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.paisanotes.data.local.dao.AuditLogDao
import com.paisanotes.data.local.dao.EmiDao
import com.paisanotes.data.local.dao.TransactionDao
import com.paisanotes.data.local.entity.AuditLogEntity
import com.paisanotes.data.local.entity.SyncStatus
import com.paisanotes.data.local.entity.TransactionEntity
import com.paisanotes.data.mapper.toDomainModel
import com.paisanotes.data.mapper.toEntity
import com.paisanotes.domain.model.Emi
import com.paisanotes.domain.repository.EmiRepository
import com.paisanotes.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class EmiRepositoryImpl @Inject constructor(
    private val dao: EmiDao,
    private val auditLogDao: AuditLogDao,
    private val transactionDao: TransactionDao,
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
        val existingEntity = dao.getEmiById(emi.id)
        val actionType = if (existingEntity == null) "CREATE" else "UPDATE"

        val entity = if (existingEntity == null) {
            emi.toEntity(syncStatus = SyncStatus.PENDING_INSERT)
        } else {
            emi.toEntity(
                syncStatus = SyncStatus.PENDING_UPDATE,
                createdAt = existingEntity.createdAt,
                updatedAt = System.currentTimeMillis()
            )
        }
        dao.insertEmi(entity)

        val metadataJson = """{"itemName": "${emi.itemName}", "principal": ${emi.principalAmount}, "monthly": ${emi.monthlyEmiAmount}}"""
        auditLogDao.insertLog(com.paisanotes.data.local.entity.AuditLogEntity(entityType = "EMI", entityId = emi.id, actionType = actionType, metadata = metadataJson))

        triggerBackgroundSync()
    }

    override suspend fun recordEmiPayment(emiId: String, amount: Double, monthName: String) {
        val entity = dao.getEmiById(emiId) ?: return

        val newCompleted = entity.completedMonths + 1
        val newAmountPaid = entity.amountPaid + amount

        // Close it if the amount is fully paid OR if the months are done
        val status = if (newAmountPaid >= entity.totalAmountWithInterest || newCompleted >= entity.totalMonths) "CLOSED" else "ACTIVE"

        dao.updateEmi(entity.copy(
            completedMonths = newCompleted,
            amountPaid = newAmountPaid,
            status = status,
            updatedAt = System.currentTimeMillis(),
            syncStatus = com.paisanotes.data.local.entity.SyncStatus.PENDING_UPDATE
        ))

        // DYNAMIC LEDGER ENTRY
        val txnType = if (entity.ownerType == "ME") "EXPENSE" else "INCOME"
        val refText = if (!entity.refNumber.isNullOrBlank()) " (Ref: ${entity.refNumber})" else ""
        val txnId = java.util.UUID.randomUUID().toString()

        transactionDao.insertTransaction(
            com.paisanotes.data.local.entity.TransactionEntity(
                id = txnId,
                amount = amount,
                transactionType = txnType,
                merchant = null,
                category = "EMI Repayment",
                categoryId = null,
                accountId = null,
                transferAccountId = null,
                transactionDate = System.currentTimeMillis(),
                paymentMethod = "UPI",
                source = "EMI_AUTO",
                notes = "EMI: $monthName$refText",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                syncStatus = com.paisanotes.data.local.entity.SyncStatus.PENDING_INSERT
            )
        )

        // AUDIT LOGS
        val logMetadata = """{"amountPaid": $amount, "totalPaid": $newAmountPaid, "month": "$monthName"}"""
        auditLogDao.insertLog(com.paisanotes.data.local.entity.AuditLogEntity(
            entityType = "EMI", entityId = emiId, actionType = "UPDATE", metadata = logMetadata
        ))

        triggerBackgroundSync()
    }

    override fun getMyEmis(): Flow<List<Emi>> {
        return dao.getMyEmis().map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun getEmiById(id: String): Emi? {
        return dao.getEmiById(id)?.toDomainModel()
    }

    override suspend fun deleteEmi(id: String) {
        val entity = dao.getEmiById(id) ?: return

        dao.updateEmi(entity.copy(
            isDeleted = true,
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING_DELETE
        ))

        val metadataJson = """{"itemName": "${entity.itemName}", "principal": ${entity.principalAmount}}"""
        auditLogDao.insertLog(com.paisanotes.data.local.entity.AuditLogEntity(entityType = "EMI", entityId = id, actionType = "DELETE", metadata = metadataJson))
        triggerBackgroundSync()
    }
}