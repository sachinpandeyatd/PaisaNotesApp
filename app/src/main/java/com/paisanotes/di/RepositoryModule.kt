package com.paisanotes.di

import com.paisanotes.data.repository.AuthRepositoryImpl
import com.paisanotes.data.repository.PersonRepositoryImpl
import com.paisanotes.data.repository.TransactionRepositoryImpl
import com.paisanotes.domain.repository.AuthRepository
import com.paisanotes.domain.repository.PersonRepository
import com.paisanotes.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // Note: Use @Binds for abstract classes/interfaces instead of @Provides.
    // It is significantly more memory-efficient under the hood!
    @Binds
    abstract fun bindTransactionRepository(
        transactionRepositoryImpl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    abstract fun bindPersonRepository(
        personRepositoryImpl: PersonRepositoryImpl
    ): PersonRepository
}