package com.paisanotes.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.paisanotes.data.local.entity.PersonEntity
import com.paisanotes.data.local.entity.PersonWithExposureTuple
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Query("SELECT * FROM people WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllActivePeople(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people WHERE id = :id")
    suspend fun getPersonById(id: String): PersonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: PersonEntity)

    @Update
    suspend fun updatePerson(person: PersonEntity)
    
    @Query("SELECT * FROM people WHERE syncStatus != 'SYNCED'")
    suspend fun getUnsyncedPeople(): List<PersonEntity>

    @Query("UPDATE people SET syncStatus = 'SYNCED' WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)

    // This query sums up all active loans and proxy EMIs for each person dynamically!
    @Query("""
        SELECT p.*, 
               (
                 COALESCE((SELECT SUM(
                     CASE 
                        WHEN type = 'LENT' THEN (amountLent - amountRepaid) 
                        ELSE -(amountLent - amountRepaid) 
                     END
                 ) FROM loans WHERE personId = p.id AND status = 'ACTIVE' AND isDeleted = 0), 0.0) 
                 +
                 COALESCE((SELECT SUM(principalAmount) FROM emis WHERE personId = p.id AND status = 'ACTIVE' AND isDeleted = 0), 0.0)
               ) AS totalExposure
        FROM people p
        WHERE p.isDeleted = 0
        ORDER BY p.name ASC
    """)
    fun getAllActivePeopleWithExposure(): Flow<List<PersonWithExposureTuple>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeople(people: List<PersonEntity>)
}