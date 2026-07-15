package com.paisanotes.data.repository

import com.paisanotes.data.local.dao.PersonDao
import com.paisanotes.data.mapper.toDomainModel
import com.paisanotes.data.mapper.toEntity
import com.paisanotes.domain.model.Person
import com.paisanotes.domain.repository.PersonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PersonRepositoryImpl @Inject constructor(
    private val dao: PersonDao
) : PersonRepository {

    override fun getAllPeople(): Flow<List<Person>> {
        return dao.getAllActivePeople().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun savePerson(person: Person) {
        // Simple insert for now. It generates a PENDING_INSERT sync status automatically from the mapper.
        dao.insertPerson(person.toEntity())
        
        // TODO: Trigger WorkManager here just like we did for Transactions!
    }
}