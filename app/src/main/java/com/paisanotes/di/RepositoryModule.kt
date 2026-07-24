package com.paisanotes.di

import com.paisanotes.data.repository.AuditLogRepositoryImpl
import com.paisanotes.data.repository.AuthRepositoryImpl
import com.paisanotes.data.repository.BudgetRepositoryImpl
import com.paisanotes.data.repository.CategoryRepositoryImpl
import com.paisanotes.data.repository.EmiRepositoryImpl
import com.paisanotes.data.repository.LoanRepositoryImpl
import com.paisanotes.data.repository.PersonRepositoryImpl
import com.paisanotes.data.repository.SyncRepositoryImpl
import com.paisanotes.data.repository.TransactionRepositoryImpl
import com.paisanotes.domain.repository.AuditLogRepository
import com.paisanotes.domain.repository.AuthRepository
import com.paisanotes.domain.repository.BudgetRepository
import com.paisanotes.domain.repository.CategoryRepository
import com.paisanotes.domain.repository.EmiRepository
import com.paisanotes.domain.repository.LoanRepository
import com.paisanotes.domain.repository.PersonRepository
import com.paisanotes.domain.repository.SyncRepository
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

    @Binds
    abstract fun bindLoanRepository(impl: LoanRepositoryImpl): LoanRepository

    @Binds
    abstract fun bindEmiRepository(impl: EmiRepositoryImpl): EmiRepository

    @Binds
    abstract fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository

    @Binds
    abstract fun bindAuditLogRepository(impl: AuditLogRepositoryImpl): AuditLogRepository

    @Binds
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository
}