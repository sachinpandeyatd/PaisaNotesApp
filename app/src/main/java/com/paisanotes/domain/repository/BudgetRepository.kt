package com.paisanotes.domain.repository

import com.paisanotes.domain.model.Budget
import com.paisanotes.domain.model.BudgetProgress
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getBudgetsWithProgress(startOfMonth: Long, endOfMonth: Long): Flow<List<BudgetProgress>>
    suspend fun saveBudget(categoryId: String, monthlyLimit: Double)
}