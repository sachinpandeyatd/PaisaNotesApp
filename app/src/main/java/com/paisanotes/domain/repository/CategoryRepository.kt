package com.paisanotes.domain.repository

import com.paisanotes.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    suspend fun saveCategory(category: Category)
}