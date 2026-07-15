package com.paisanotes.data.repository

import com.paisanotes.data.local.dao.EmiDao
import com.paisanotes.data.mapper.toDomainModel
import com.paisanotes.data.mapper.toEntity
import com.paisanotes.domain.model.Emi
import com.paisanotes.domain.repository.EmiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EmiRepositoryImpl @Inject constructor(private val dao: EmiDao) : EmiRepository {
    override fun getEmisForPerson(personId: String): Flow<List<Emi>> {
        return dao.getEmisByPerson(personId).map { list -> list.map { it.toDomainModel() } }
    }
    override suspend fun saveEmi(emi: Emi) { dao.insertEmi(emi.toEntity()) }
}