package com.paisanotes.data.local.dao

import androidx.room.*
import com.paisanotes.data.local.entity.BudgetEntity
import com.paisanotes.data.local.entity.BudgetProgressTuple
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("""
        SELECT 
            b.id AS budgetId,
            c.id AS categoryId,
            c.name AS categoryName,
            c.icon AS categoryIcon,
            c.color AS categoryColor,
            b.monthlyLimit AS monthlyLimit,
            (
                SELECT COALESCE(SUM(amount), 0.0) 
                FROM transactions 
                WHERE (categoryId = c.id OR category = c.name)
                  AND isDeleted = 0 
                  AND transactionType = 'EXPENSE'
                  AND transactionDate >= :startOfMonth 
                  AND transactionDate <= :endOfMonth
            ) AS spentAmount
        FROM budgets b
        INNER JOIN categories c ON b.categoryId = c.id
        WHERE b.isDeleted = 0 AND c.isDeleted = 0
        ORDER BY spentAmount DESC
    """)
    fun getBudgetsWithProgress(startOfMonth: Long, endOfMonth: Long): Flow<List<BudgetProgressTuple>>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND isDeleted = 0 LIMIT 1")
    suspend fun getBudgetByCategory(categoryId: String): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetEntity>)

    // --- SYNC QUERIES ---
    @Query("SELECT * FROM budgets WHERE syncStatus != 'SYNCED'")
    suspend fun getUnsyncedBudgets(): List<BudgetEntity>

    @Query("UPDATE budgets SET syncStatus = 'SYNCED' WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)
}