package com.paisanotes.data.mapper

import com.paisanotes.data.local.entity.PersonEntity
import com.paisanotes.data.local.entity.SyncStatus
import com.paisanotes.domain.model.Person

fun PersonEntity.toDomainModel(): Person {
    return Person(
        id = id,
        name = name,
        phoneNumber = phoneNumber
    )
}

fun Person.toEntity(
    syncStatus: SyncStatus = SyncStatus.PENDING_INSERT,
    createdAt: Long = System.currentTimeMillis(),
    updatedAt: Long = System.currentTimeMillis(),
    isDeleted: Boolean = false
): PersonEntity {
    return PersonEntity(
        id = id,
        name = name,
        phoneNumber = phoneNumber,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        syncStatus = syncStatus
    )
}