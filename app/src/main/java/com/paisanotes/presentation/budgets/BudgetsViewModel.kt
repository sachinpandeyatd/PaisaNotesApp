package com.paisanotes.presentation.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paisanotes.domain.model.BudgetProgress
import com.paisanotes.domain.model.Category
import com.paisanotes.domain.repository.BudgetRepository
import com.paisanotes.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class BudgetsState(
    val budgets: List<BudgetProgress> = emptyList(),
    val availableCategories: List<Category> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BudgetsState())
    val state: StateFlow<BudgetsState> = _state.asStateFlow()

    init {
        loadBudgets()
    }

    private fun loadBudgets() {
        val now = LocalDate.now()
        val startOfMonth = now.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        android.util.Log.d("BUDGET_DEBUG", "Querying Room for Dates: $startOfMonth to $endOfMonth")
        viewModelScope.launch {
            combine(
                budgetRepository.getBudgetsWithProgress(startOfMonth, endOfMonth),
                categoryRepository.getAllCategories()
            ) { budgets, categories ->
                budgets.forEach { b ->
                    android.util.Log.d("BUDGET_DEBUG", "Room returned Budget: CatID=${b.categoryId}, Limit=${b.monthlyLimit}, Spent=${b.spentAmount}")
                }
                BudgetsState(
                    budgets = budgets,
                    availableCategories = categories,
                    isLoading = false
                )
            }.collectLatest { combinedState ->
                _state.value = combinedState
            }
        }
    }

    fun saveBudget(categoryId: String, limit: Double) {
        viewModelScope.launch {
            budgetRepository.saveBudget(categoryId, limit)
        }
    }
}