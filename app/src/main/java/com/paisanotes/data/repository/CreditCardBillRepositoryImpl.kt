package com.paisanotes.data.repository

import android.content.Context
import androidx.work.*
import com.paisanotes.data.local.dao.AuditLogDao
import com.paisanotes.data.local.dao.CreditCardBillDao
import com.paisanotes.data.local.dao.TransactionDao
import com.paisanotes.data.local.entity.AuditLogEntity
import com.paisanotes.data.local.entity.SyncStatus
import com.paisanotes.data.local.entity.TransactionEntity
import com.paisanotes.data.mapper.toDomainModel
import com.paisanotes.domain.model.CreditCardBill
import com.paisanotes.domain.repository.CreditCardBillRepository
import com.paisanotes.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class CreditCardBillRepositoryImpl @Inject constructor(
    private val dao: CreditCardBillDao,
    private val transactionDao: TransactionDao,
    private val auditLogDao: AuditLogDao,
    @ApplicationContext private val context: Context
) : CreditCardBillRepository {

    private fun triggerBackgroundSync() {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val request = OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(constraints).build()
        WorkManager.getInstance(context).enqueueUniqueWork("paisa_sync_work", ExistingWorkPolicy.REPLACE, request)
    }

    override fun getActiveBills(): Flow<List<CreditCardBill>> {
        return dao.getAllActiveBills().map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun recordBillPayment(billId: String, amountPaid: Double, fromAccountId: String) {
        val entity = dao.getBillById(billId) ?: return
        
        val newAmountPaid = entity.amountPaid + amountPaid
        val status = if (newAmountPaid >= entity.totalBilledAmount) "CLEARED" else "PARTIALLY_PAID"

        // 1. Update the Bill
        dao.updateBill(entity.copy(
            amountPaid = newAmountPaid,
            status = status,
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING_UPDATE
        ))

        // 2. Create the TRANSFER transaction automatically!
        val txnId = UUID.randomUUID().toString()
        transactionDao.insertTransaction(
            TransactionEntity(
                id = txnId,
                amount = amountPaid,
                transactionType = "TRANSFER", // Money leaving savings, going to CC
                merchant = null,
                category = "Credit Card Bill Payment",
                categoryId = null,
                accountId = fromAccountId,
                transferAccountId = entity.accountId,
                transactionDate = System.currentTimeMillis(),
                paymentMethod = "ONLINE",
                source = "CC_BILL_PAYMENT",
                notes = "Paid bill for ${entity.billingMonth}",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isDeleted = false,
                syncStatus = SyncStatus.PENDING_INSERT
            )
        )

        // 3. Audit Log
        val metadataJson = """{"amountPaid": $amountPaid, "status": "$status"}"""
        auditLogDao.insertLog(AuditLogEntity(entityType = "CREDIT_CARD_BILL", entityId = billId, actionType = "UPDATE", metadata = metadataJson))
        
        triggerBackgroundSync()
    }
}