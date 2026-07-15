package com.paisanotes.domain.repository

import com.paisanotes.domain.model.Person
import kotlinx.coroutines.flow.Flow

interface PersonRepository {
    fun getAllPeople(): Flow<List<Person>>
    suspend fun savePerson(person: Person)
}