package com.paisanotes.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.paisanotes.data.local.entity.EmiEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmiDao {
    @Query("SELECT * FROM emis WHERE isDeleted = 0 ORDER BY startDate DESC")
    fun getAllActiveEmis(): Flow<List<EmiEntity>>
    
    @Query("SELECT * FROM emis WHERE personId = :personId AND isDeleted = 0")
    fun getEmisByPerson(personId: String): Flow<List<EmiEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmi(emi: EmiEntity)

    @Update
    suspend fun updateEmi(emi: EmiEntity)

    @Query("SELECT * FROM emis WHERE syncStatus != 'SYNCED'")
    suspend fun getUnsyncedEmis(): List<EmiEntity>

    @Query("UPDATE emis SET syncStatus = 'SYNCED' WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmis(emis: List<EmiEntity>)
}