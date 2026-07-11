package com.paisanotes.data.local.entity

enum class SyncStatus {
    SYNCED,           // Data perfectly matches the Spring Boot server
    PENDING_INSERT,   // Created offline, needs to be POSTed
    PENDING_UPDATE,   // Edited offline, needs to be POSTed
    PENDING_DELETE    // Soft-deleted offline, needs to be POSTed
}