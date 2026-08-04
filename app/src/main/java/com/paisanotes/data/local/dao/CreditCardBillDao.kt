package com.paisanotes.data.local.dao

import androidx.room.*
import com.paisanotes.data.local.entity.CreditCardBillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardBillDao {
    @Query("SELECT * FROM credit_card_bills WHERE isDeleted = 0 ORDER BY dueDate ASC")
    fun getAllActiveBills(): Flow<List<CreditCardBillEntity>>

    @Query("SELECT * FROM credit_card_bills WHERE id = :id")
    suspend fun getBillById(id: String): CreditCardBillEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: CreditCardBillEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBills(bills: List<CreditCardBillEntity>)

    @Update
    suspend fun updateBill(bill: CreditCardBillEntity)

    @Query("SELECT * FROM credit_card_bills WHERE syncStatus != 'SYNCED'")
    suspend fun getUnsyncedBills(): List<CreditCardBillEntity>

    @Query("UPDATE credit_card_bills SET syncStatus = 'SYNCED' WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)
}