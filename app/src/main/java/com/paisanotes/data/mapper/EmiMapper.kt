package com.paisanotes.data.mapper

import com.paisanotes.data.local.entity.EmiEntity
import com.paisanotes.data.local.entity.SyncStatus
import com.paisanotes.domain.model.Emi
import com.paisanotes.data.remote.dto.EmiDto
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun EmiEntity.toDomainModel() = Emi(
    id = id, personId = personId, refNumber = refNumber, itemName = itemName,
    ownerType = ownerType, principalAmount = principalAmount,
    monthlyEmiAmount = monthlyEmiAmount, totalMonths = totalMonths,
    completedMonths = completedMonths,
    totalAmountWithInterest = totalAmountWithInterest,
    interestRate = interestRate,
    amountPaid = amountPaid,
    startDate = startDate, status = status
)

fun Emi.toEntity(
    syncStatus: SyncStatus = SyncStatus.PENDING_INSERT,
    createdAt: Long = System.currentTimeMillis(),
    updatedAt: Long = System.currentTimeMillis()
) = EmiEntity(
    id = id,
    personId = personId,
    refNumber = refNumber,
    itemName = itemName,
    ownerType = ownerType,
    principalAmount = principalAmount,
    monthlyEmiAmount = monthlyEmiAmount,
    totalMonths = totalMonths,
    completedMonths = completedMonths,
    totalAmountWithInterest = totalAmountWithInterest,
    interestRate = interestRate,
    amountPaid = amountPaid,
    startDate = startDate,
    status = status,

    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus
)

fun EmiEntity.toDto(): EmiDto {
    val formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC"))
    return EmiDto(
        id = id,
        personId = personId,
        refNumber = refNumber,
        itemName = itemName,
        ownerType = ownerType,
        principalAmount = principalAmount,
        monthlyEmiAmount = monthlyEmiAmount,
        totalMonths = totalMonths,
        completedMonths = completedMonths,
        totalAmountWithInterest = totalAmountWithInterest,
        interestRate = interestRate,
        amountPaid = amountPaid,
        startDate = formatter.format(Instant.ofEpochMilli(startDate)),
        status = status,
        createdAt = formatter.format(Instant.ofEpochMilli(createdAt)),
        updatedAt = formatter.format(Instant.ofEpochMilli(updatedAt)),
        isDeleted = isDeleted
    )
}

fun com.paisanotes.data.remote.dto.EmiDto.toEntity(): EmiEntity {
    return EmiEntity(
        id = id,
        personId = personId,
        refNumber = refNumber,
        itemName = itemName,
        ownerType = ownerType,
        principalAmount = principalAmount,
        monthlyEmiAmount = monthlyEmiAmount,
        totalMonths = totalMonths,
        completedMonths = completedMonths,
        totalAmountWithInterest = totalAmountWithInterest,
        interestRate = interestRate,
        amountPaid = amountPaid,
        startDate = java.time.ZonedDateTime.parse(startDate + "T00:00:00Z").toInstant().toEpochMilli(),
        status = status,
        createdAt = java.time.ZonedDateTime.parse(createdAt).toInstant().toEpochMilli(),
        updatedAt = java.time.ZonedDateTime.parse(updatedAt).toInstant().toEpochMilli(),
        isDeleted = isDeleted,
        syncStatus = SyncStatus.SYNCED
    )
}