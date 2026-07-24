package com.paisanotes.data.local.dao

import androidx.room.*
import com.paisanotes.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

data class AccountWithBalanceTuple(
    val id: String,
    val name: String,
    val type: String,
    val initialBalance: Double,
    val currentBalance: Double
)

@Dao
interface AccountDao {
    
    // 🚨 MASTERCLASS SQL: Calculates dynamic real-time balances!
    @Query("""
        SELECT a.id, a.name, a.type, a.initialBalance,
               (a.initialBalance +
                COALESCE((SELECT SUM(amount) FROM transactions WHERE accountId = a.id AND transactionType = 'INCOME' AND isDeleted = 0), 0.0) -
                COALESCE((SELECT SUM(amount) FROM transactions WHERE accountId = a.id AND transactionType = 'EXPENSE' AND isDeleted = 0), 0.0) -
                COALESCE((SELECT SUM(amount) FROM transactions WHERE accountId = a.id AND transactionType = 'TRANSFER' AND isDeleted = 0), 0.0) +
                COALESCE((SELECT SUM(amount) FROM transactions WHERE transferAccountId = a.id AND transactionType = 'TRANSFER' AND isDeleted = 0), 0.0)
               ) AS currentBalance
        FROM accounts a
        WHERE a.isDeleted = 0
        ORDER BY a.name ASC
    """)
    fun getAccountsWithBalances(): Flow<List<AccountWithBalanceTuple>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    // --- SYNC QUERIES ---
    @Query("SELECT * FROM accounts WHERE syncStatus != 'SYNCED'")
    suspend fun getUnsyncedAccounts(): List<AccountEntity>

    @Query("UPDATE accounts SET syncStatus = 'SYNCED' WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)
}