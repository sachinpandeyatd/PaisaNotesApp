package com.paisanotes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.paisanotes.data.local.dao.TransactionDao
import com.paisanotes.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class], // We will add PersonEntity, EmiEntity later
    version = 1,
    exportSchema = false
)
abstract class PaisaDatabase : RoomDatabase() {
    abstract val transactionDao: TransactionDao
}