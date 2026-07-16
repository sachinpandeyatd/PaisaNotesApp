package com.paisanotes.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val personId: String, // Always tied to a friend
    val amountLent: Double,
    val dateGiven: Long,
    val expectedReturnDate: Long?,
    val status: String = "ACTIVE", // "ACTIVE" or "CLOSED"
    val notes: String?,
    val amountRepaid: Double = 0.0,
    
    // Auditing & Sync
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.PENDING_INSERT
)