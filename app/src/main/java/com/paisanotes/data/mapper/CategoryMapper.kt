package com.paisanotes.data.mapper

import com.paisanotes.data.local.entity.CategoryEntity
import com.paisanotes.data.local.entity.SyncStatus
import com.paisanotes.data.remote.dto.CategoryDto
import com.paisanotes.domain.model.Category
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun CategoryEntity.toDomainModel() = Category(id, name, icon, color, isDefault)

fun Category.toEntity() = CategoryEntity(
    id = id, name = name, icon = icon, color = color, isDefault = isDefault
)

fun CategoryEntity.toDto(): CategoryDto {
    val formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC"))
    return CategoryDto(id, name, icon, color, isDefault, formatter.format(Instant.ofEpochMilli(createdAt)), formatter.format(Instant.ofEpochMilli(updatedAt)), isDeleted, syncStatus)
}

fun CategoryDto.toEntity(): CategoryEntity {
    // 🚨 Safe fallback parsing using Instant
    val parsedCreatedAt = try {
        java.time.Instant.parse(createdAt).toEpochMilli()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }

    val parsedUpdatedAt = try {
        java.time.Instant.parse(updatedAt).toEpochMilli()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }

    return CategoryEntity(
        id = id,
        name = name,
        icon = icon,
        color = color,
        isDefault = isDefault,
        createdAt = parsedCreatedAt,
        updatedAt = parsedUpdatedAt,
        isDeleted = isDeleted,
        syncStatus = SyncStatus.SYNCED
    )
}