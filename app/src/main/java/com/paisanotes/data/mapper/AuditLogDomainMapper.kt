package com.paisanotes.data.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.paisanotes.data.local.entity.AuditLogEntity
import com.paisanotes.domain.model.AuditLog

fun AuditLogEntity.toDomainModel(): AuditLog {
    val mapType = object : TypeToken<Map<String, Any>>() {}.type
    val metadataMap: Map<String, Any> = try {
        Gson().fromJson(metadata, mapType) ?: emptyMap()
    } catch (e: Exception) {
        emptyMap()
    }

    return AuditLog(
        id = id,
        entityType = entityType,
        actionType = actionType,
        metadata = metadataMap,
        createdAt = createdAt
    )
}