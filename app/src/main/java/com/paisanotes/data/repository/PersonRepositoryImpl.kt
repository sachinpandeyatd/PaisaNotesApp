package com.paisanotes.data.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.paisanotes.data.local.dao.AuditLogDao
import com.paisanotes.data.local.dao.PersonDao
import com.paisanotes.data.local.entity.AuditLogEntity
import com.paisanotes.data.local.entity.SyncStatus
import com.paisanotes.data.mapper.toDomainModel
import com.paisanotes.data.mapper.toEntity
import com.paisanotes.domain.model.Person
import com.paisanotes.domain.repository.PersonRepository
import com.paisanotes.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PersonRepositoryImpl @Inject constructor(
    private val dao: PersonDao,
    private val auditLogDao: AuditLogDao,
    @ApplicationContext private val context: Context
) : PersonRepository {

    private fun triggerBackgroundSync() {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(constraints).build()
        WorkManager.getInstance(context).enqueueUniqueWork("paisa_sync_work", ExistingWorkPolicy.REPLACE, syncWorkRequest)
    }

    override fun getAllPeople(): Flow<List<Person>> {
        return dao.getAllActivePeopleWithExposure().map { tuples ->
            tuples.map { it.toDomainModel() }
        }
    }

    override suspend fun savePerson(person: Person) {
        val existingEntity = dao.getPersonById(person.id)
        val actionType = if (existingEntity == null) "CREATE" else "UPDATE"
        val metadataJson = """{"name": "${person.name}", "phone": "${person.phoneNumber ?: ""}"}"""

        val entity = if (existingEntity == null) {
            person.toEntity(syncStatus = SyncStatus.PENDING_INSERT)
        } else {
            person.toEntity(
                syncStatus = SyncStatus.PENDING_UPDATE,
                createdAt = existingEntity.createdAt,
                updatedAt = System.currentTimeMillis()
            )
        }

        dao.insertPerson(entity)

        // 🚨 CREATE AUDIT LOG
        val auditLog = AuditLogEntity(
            entityType = "PERSON",
            entityId = person.id,
            actionType = actionType,
            metadata = metadataJson
        )
        auditLogDao.insertLog(auditLog)

        triggerBackgroundSync()
    }
}