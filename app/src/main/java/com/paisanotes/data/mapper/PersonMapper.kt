package com.paisanotes.data.mapper

import com.paisanotes.data.local.entity.PersonEntity
import com.paisanotes.data.local.entity.PersonWithExposureTuple
import com.paisanotes.data.local.entity.SyncStatus
import com.paisanotes.data.remote.dto.PersonDto
import com.paisanotes.domain.model.Person
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

fun PersonWithExposureTuple.toDomainModel(): Person {
    return Person(
        id = person.id,
        name = person.name,
        phoneNumber = person.phoneNumber,
        totalExposure = totalExposure // Map the calculated SQL value!
    )
}

fun PersonEntity.toDto(): PersonDto {
    val formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC"))
    return PersonDto(
        id = id,
        name = name,
        phoneNumber = phoneNumber,
        createdAt = formatter.format(Instant.ofEpochMilli(createdAt)),
        updatedAt = formatter.format(Instant.ofEpochMilli(updatedAt)),
        isDeleted = isDeleted
    )
}

fun com.paisanotes.data.remote.dto.PersonDto.toEntity(): PersonEntity {
    return PersonEntity(
        id = id,
        name = name,
        phoneNumber = phoneNumber,
        createdAt = java.time.ZonedDateTime.parse(createdAt).toInstant().toEpochMilli(),
        updatedAt = java.time.ZonedDateTime.parse(updatedAt).toInstant().toEpochMilli(),
        isDeleted = isDeleted,
        syncStatus = SyncStatus.SYNCED // Downloaded from server, so it's perfectly synced!
    )
}