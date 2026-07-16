package com.paisanotes.domain.repository

import com.paisanotes.domain.model.Loan
import kotlinx.coroutines.flow.Flow

interface LoanRepository {
    fun getLoansForPerson(personId: String): Flow<List<Loan>>
    suspend fun saveLoan(loan: Loan)

    suspend fun recordRepayment(loanId: String, amount: Double)
}