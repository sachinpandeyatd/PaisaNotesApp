package com.paisanotes.data.mapper

import com.paisanotes.data.local.entity.LoanEntity
import com.paisanotes.domain.model.Loan

fun LoanEntity.toDomainModel() = Loan(
    id = id, personId = personId, amountLent = amountLent,
    dateGiven = dateGiven, expectedReturnDate = expectedReturnDate,
    status = status, notes = notes
)

fun Loan.toEntity() = LoanEntity(
    id = id, personId = personId, amountLent = amountLent,
    dateGiven = dateGiven, expectedReturnDate = expectedReturnDate,
    status = status, notes = notes,
    createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()
)