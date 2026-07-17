package com.paisanotes.data.mapper

import com.paisanotes.data.local.entity.LoanEntity
import com.paisanotes.data.local.entity.SyncStatus
import com.paisanotes.domain.model.Loan
import com.paisanotes.data.remote.dto.LoanDto
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun LoanEntity.toDomainModel() = Loan(
    id = id,
    personId = personId,
    type = type,
    amountLent = amountLent,
    amountRepaid = amountRepaid,
    dateGiven = dateGiven,
    expectedReturnDate = expectedReturnDate,
    status = status,
    notes = notes
)

fun Loan.toEntity(
    syncStatus: SyncStatus = SyncStatus.PENDING_INSERT,
    createdAt: Long = System.currentTimeMillis(),
    updatedAt: Long = System.currentTimeMillis()
) = LoanEntity(
    id = id,
    personId = personId,
    type = type,
    amountLent = amountLent,
    amountRepaid = amountRepaid,
    dateGiven = dateGiven,
    expectedReturnDate = expectedReturnDate,
    status = status,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus
)

fun LoanEntity.toDto(): LoanDto {
    val formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC"))
    return LoanDto(
        id = id,
        personId = personId,
        type = type,
        amountLent = amountLent,
        amountRepaid = amountRepaid,
        dateGiven = formatter.format(Instant.ofEpochMilli(dateGiven)),
        expectedReturnDate = expectedReturnDate?.let { formatter.format(Instant.ofEpochMilli(it)) },
        status = status,
        notes = notes,
        createdAt = formatter.format(Instant.ofEpochMilli(createdAt)),
        updatedAt = formatter.format(Instant.ofEpochMilli(updatedAt)),
        isDeleted = isDeleted
    )
}

fun com.paisanotes.data.remote.dto.LoanDto.toEntity(): LoanEntity {
    return LoanEntity(
        id = id,
        personId = personId,
        type = type,
        amountLent = amountLent,
        amountRepaid = amountRepaid,
        // Handle LocalDate parsing safely
        dateGiven = java.time.ZonedDateTime.parse(dateGiven + "T00:00:00Z").toInstant().toEpochMilli(),
        expectedReturnDate = expectedReturnDate?.let { java.time.ZonedDateTime.parse(it + "T00:00:00Z").toInstant().toEpochMilli() },
        status = status,
        notes = notes,
        createdAt = java.time.ZonedDateTime.parse(createdAt).toInstant().toEpochMilli(),
        updatedAt = java.time.ZonedDateTime.parse(updatedAt).toInstant().toEpochMilli(),
        isDeleted = isDeleted,
        syncStatus = SyncStatus.SYNCED
    )
}