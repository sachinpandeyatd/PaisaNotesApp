package com.paisanotes.data.mapper

import com.paisanotes.data.local.entity.BudgetEntity
import com.paisanotes.data.local.entity.BudgetProgressTuple
import com.paisanotes.data.local.entity.SyncStatus
import com.paisanotes.data.remote.dto.BudgetDto
import com.paisanotes.domain.model.Budget
import com.paisanotes.domain.model.BudgetProgress
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun BudgetEntity.toDomainModel() = Budget(id, categoryId, monthlyLimit)

fun Budget.toEntity(
    syncStatus: SyncStatus = SyncStatus.PENDING_INSERT,
    createdAt: Long = System.currentTimeMillis(),
    updatedAt: Long = System.currentTimeMillis()
) = BudgetEntity(
    id = id, categoryId = categoryId, monthlyLimit = monthlyLimit,
    createdAt = createdAt, updatedAt = updatedAt, syncStatus = syncStatus
)

fun BudgetEntity.toDto(): BudgetDto {
    val formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC"))
    return BudgetDto(id, categoryId, monthlyLimit, formatter.format(Instant.ofEpochMilli(createdAt)), formatter.format(Instant.ofEpochMilli(updatedAt)), isDeleted)
}

fun BudgetDto.toEntity(): BudgetEntity {
    val parsedCreatedAt = try { Instant.parse(createdAt).toEpochMilli() } catch (e: Exception) { System.currentTimeMillis() }
    val parsedUpdatedAt = try { Instant.parse(updatedAt).toEpochMilli() } catch (e: Exception) { System.currentTimeMillis() }

    return BudgetEntity(
        id = id, categoryId = categoryId, monthlyLimit = monthlyLimit,
        createdAt = parsedCreatedAt, updatedAt = parsedUpdatedAt,
        isDeleted = isDeleted, syncStatus = SyncStatus.SYNCED
    )
}

fun BudgetProgressTuple.toDomainModel() = BudgetProgress(
    budgetId = budgetId,
    categoryId = categoryId,
    categoryName = categoryName,
    categoryIcon = categoryIcon,
    categoryColor = categoryColor,
    monthlyLimit = monthlyLimit,
    spentAmount = spentAmount
)