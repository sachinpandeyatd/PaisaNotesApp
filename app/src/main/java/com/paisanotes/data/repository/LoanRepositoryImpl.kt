package com.paisanotes.data.repository

import com.paisanotes.data.local.dao.LoanDao
import com.paisanotes.data.mapper.toDomainModel
import com.paisanotes.data.mapper.toEntity
import com.paisanotes.domain.model.Loan
import com.paisanotes.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LoanRepositoryImpl @Inject constructor(private val dao: LoanDao) : LoanRepository {
    override fun getLoansForPerson(personId: String): Flow<List<Loan>> {
        return dao.getLoansByPerson(personId).map { list -> list.map { it.toDomainModel() } }
    }
    override suspend fun saveLoan(loan: Loan) { dao.insertLoan(loan.toEntity()) } // TODO: Add Worker trigger later
}