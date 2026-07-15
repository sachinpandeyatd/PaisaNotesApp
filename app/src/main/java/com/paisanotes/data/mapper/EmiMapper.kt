package com.paisanotes.data.mapper

import com.paisanotes.data.local.entity.EmiEntity
import com.paisanotes.domain.model.Emi

fun EmiEntity.toDomainModel() = Emi(
    id = id, personId = personId, refNumber = refNumber, itemName = itemName,
    ownerType = ownerType, principalAmount = principalAmount,
    monthlyEmiAmount = monthlyEmiAmount, totalMonths = totalMonths,
    startDate = startDate, status = status
)

fun Emi.toEntity() = EmiEntity(
    id = id, personId = personId, refNumber = refNumber, itemName = itemName,
    ownerType = ownerType, principalAmount = principalAmount,
    monthlyEmiAmount = monthlyEmiAmount, totalMonths = totalMonths,
    startDate = startDate, status = status,
    createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()
)