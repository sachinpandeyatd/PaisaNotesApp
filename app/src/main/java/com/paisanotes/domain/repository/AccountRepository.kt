package com.paisanotes.domain.repository

import com.paisanotes.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAccountsWithBalances(): Flow<List<Account>>
    suspend fun saveAccount(name: String, type: String, initialBalance: Double, statementDay: Int?, dueDay: Int?)
    suspend fun getOrCreateAccountId(name: String): String
    fun getAccountWithBalance(accountId: String): Flow<Account?>
}