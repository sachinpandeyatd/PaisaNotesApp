package com.paisanotes.domain.repository

import com.paisanotes.domain.model.Emi
import kotlinx.coroutines.flow.Flow

interface EmiRepository {
    fun getEmisForPerson(personId: String): Flow<List<Emi>>
    suspend fun saveEmi(emi: Emi)
}