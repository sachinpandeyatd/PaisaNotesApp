package com.paisanotes.domain.repository

import com.paisanotes.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    // UI just observes this flow
    fun getAllTransactions(): Flow<List<Transaction>>

    // UI calls these methods to save data locally
    suspend fun saveTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transactionId: String)

    // WorkManager calls this to perform background syncing
    suspend fun syncWithServer(): Boolean
}