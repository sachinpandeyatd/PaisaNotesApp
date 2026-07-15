package com.paisanotes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.paisanotes.data.local.dao.EmiDao
import com.paisanotes.data.local.dao.LoanDao
import com.paisanotes.data.local.dao.PersonDao
import com.paisanotes.data.local.dao.TransactionDao
import com.paisanotes.data.local.entity.EmiEntity
import com.paisanotes.data.local.entity.LoanEntity
import com.paisanotes.data.local.entity.PersonEntity
import com.paisanotes.data.local.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        PersonEntity::class,
        EmiEntity::class,
        LoanEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class PaisaDatabase : RoomDatabase() {
    abstract val transactionDao: TransactionDao
    abstract val personDao: PersonDao
    abstract val emiDao: EmiDao
    abstract val loanDao: LoanDao
}