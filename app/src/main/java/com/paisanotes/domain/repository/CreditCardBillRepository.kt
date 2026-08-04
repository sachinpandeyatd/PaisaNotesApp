package com.paisanotes.domain.repository

import com.paisanotes.domain.model.CreditCardBill
import kotlinx.coroutines.flow.Flow

interface CreditCardBillRepository {
    fun getActiveBills(): Flow<List<CreditCardBill>>
    suspend fun recordBillPayment(billId: String, amountPaid: Double, fromAccountId: String)
}