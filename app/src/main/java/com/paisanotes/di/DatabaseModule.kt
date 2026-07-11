package com.paisanotes.di

import android.content.Context
import androidx.room.Room
import com.paisanotes.data.local.PaisaDatabase
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
        ).build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: PaisaDatabase): TransactionDao {
        return database.transactionDao
    }
}