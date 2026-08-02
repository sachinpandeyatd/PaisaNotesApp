package com.paisanotes.domain.model

data class AuditLog(
    val id: String,
    val entityType: String, // TRANSACTION, PERSON, LOAN, EMI
    val entityId: String,
    val actionType: String, // CREATE, UPDATE, DELETE
    val metadata: Map<String, Any>, // Parsed JSON!
    val createdAt: Long
)