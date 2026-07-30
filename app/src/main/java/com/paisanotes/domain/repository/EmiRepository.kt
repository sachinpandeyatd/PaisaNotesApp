package com.paisanotes.domain.repository

import com.paisanotes.domain.model.Emi
import kotlinx.coroutines.flow.Flow

interface EmiRepository {
    fun getEmisForPerson(personId: String): Flow<List<Emi>>
    suspend fun saveEmi(emi: Emi)
    suspend fun recordEmiPayment(emiId: String, amount: Double, monthName: String)

    fun getMyEmis(): Flow<List<Emi>>

    suspend fun getEmiById(id: String): Emi?
    suspend fun deleteEmi(id: String)

}