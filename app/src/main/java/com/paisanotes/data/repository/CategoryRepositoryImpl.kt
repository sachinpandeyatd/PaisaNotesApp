package com.paisanotes.data.repository

import com.paisanotes.data.local.dao.CategoryDao
import com.paisanotes.data.mapper.toDomainModel
import com.paisanotes.data.mapper.toEntity
import com.paisanotes.domain.model.Category
import com.paisanotes.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> {
        return dao.getAllActiveCategories().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun saveCategory(category: Category) {
        dao.insertCategory(category.toEntity())
    }
}