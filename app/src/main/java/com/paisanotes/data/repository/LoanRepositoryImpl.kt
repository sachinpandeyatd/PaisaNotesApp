package com.paisanotes.data.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.paisanotes.data.local.dao.AuditLogDao
import com.paisanotes.data.local.dao.LoanDao
import com.paisanotes.data.local.dao.TransactionDao
import com.paisanotes.data.local.entity.AuditLogEntity
import com.paisanotes.data.local.entity.SyncStatus
import com.paisanotes.data.local.entity.TransactionEntity
import com.paisanotes.data.mapper.toDomainModel
import com.paisanotes.data.mapper.toEntity
import com.paisanotes.domain.model.Loan
import com.paisanotes.domain.repository.LoanRepository
import com.paisanotes.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class LoanRepositoryImpl @Inject constructor(
    private val dao: LoanDao,
    private val auditLogDao: AuditLogDao,
    private val transactionDao: TransactionDao,
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
        val entity = loan.toEntity().copy(syncStatus = SyncStatus.PENDING_INSERT)
        dao.insertLoan(entity)

        val txnType = if (loan.type == "LENT") "EXPENSE" else "INCOME"
        val categoryText = if (loan.type == "LENT") "Given to Friend" else "Received from Friend"
        val txnId = java.util.UUID.randomUUID().toString()

        transactionDao.insertTransaction(
            com.paisanotes.data.local.entity.TransactionEntity(
                id = txnId, amount = loan.amountLent, transactionType = txnType, merchant = null,
                category = categoryText, transactionDate = loan.dateGiven,
                paymentMethod = "CASH", source = "FRIEND_LEDGER", notes = loan.notes,
                createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING_INSERT
            )
        )

        val metadataJson = """{"amount": ${loan.amountLent}, "type": "${loan.type}"}"""

        auditLogDao.insertLog(AuditLogEntity(
            entityType = "LOAN",
            entityId = loan.id,
            actionType = "CREATE",
            metadata = metadataJson)
        )
        auditLogDao.insertLog(AuditLogEntity(
            entityType = "TRANSACTION",
            entityId = txnId,
            actionType = "CREATE",
            metadata = """{"amount": ${loan.amountLent}}""")
        )

        triggerBackgroundSync()
    }

    override suspend fun recordRepayment(loanId: String, amount: Double) {
        val entity = dao.getLoanById(loanId) ?: return

        val newRepaid = entity.amountRepaid + amount
        val status = if (newRepaid >= entity.amountLent) "CLOSED" else "ACTIVE"

        // 1. Update Loan
        dao.updateLoan(entity.copy(
            amountRepaid = newRepaid,
            status = status,
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING_UPDATE
        ))

        // 2. Add an INCOME transaction automatically
        val txnType = if (entity.type == "LENT") "INCOME" else "EXPENSE"
        val categoryText = if (entity.type == "LENT") "Loan Repayment Received" else "Loan Repayment Sent"

        val txnId = UUID.randomUUID().toString()
        transactionDao.insertTransaction(
            TransactionEntity(
                id = txnId, amount = amount, transactionType = txnType, merchant = null,
                category = categoryText, transactionDate = System.currentTimeMillis(),
                paymentMethod = "CASH", source = "LOAN_REPAYMENT", notes = "Repayment for Loan",
                createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING_INSERT
            )
        )

        // 3. Create Audit Logs
        val logMetadata = """{"amountPaid": $amount, "newTotalRepaid": $newRepaid, "status": "$status"}"""
        auditLogDao.insertLog(AuditLogEntity(entityType = "LOAN", entityId = loanId, actionType = "UPDATE", metadata = logMetadata))
        auditLogDao.insertLog(AuditLogEntity(entityType = "TRANSACTION", entityId = txnId, actionType = "CREATE", metadata = """{"amount": $amount}"""))

        triggerBackgroundSync()
    }
}