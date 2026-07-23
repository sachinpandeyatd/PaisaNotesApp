package com.paisanotes.data.local.dao

import androidx.room.*
import com.paisanotes.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllActiveCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("SELECT * FROM categories WHERE syncStatus != 'SYNCED'")
    suspend fun getUnsyncedCategories(): List<CategoryEntity>

    @Query("UPDATE categories SET syncStatus = 'SYNCED' WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)
}