package com.paisanotes.di

import android.content.Context
import androidx.room.Room
import com.paisanotes.data.local.PaisaDatabase
import com.paisanotes.data.local.dao.AuditLogDao
import com.paisanotes.data.local.dao.EmiDao
import com.paisanotes.data.local.dao.LoanDao
import com.paisanotes.data.local.dao.PersonDao
import com.paisanotes.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // This module lives as long as the Application lives
object DatabaseModule {

    @Provides
    @Singleton
    fun providePaisaDatabase(@ApplicationContext context: Context): PaisaDatabase {
        return Room.databaseBuilder(
            context,
            PaisaDatabase::class.java,
            "paisanotes_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: PaisaDatabase): TransactionDao {
        return database.transactionDao
    }

    @Provides
    @Singleton
    fun providePersonDao(database: PaisaDatabase): PersonDao = database.personDao

    @Provides
    @Singleton
    fun provideEmiDao(database: PaisaDatabase): EmiDao = database.emiDao

    @Provides
    @Singleton
    fun provideLoanDao(database: PaisaDatabase): LoanDao = database.loanDao

    @Provides
    @Singleton
    fun provideAuditLogDao(database: PaisaDatabase): AuditLogDao = database.auditLogDao
}