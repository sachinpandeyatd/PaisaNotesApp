package com.paisanotes.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "emis")
data class EmiEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val personId: String?, // Null if it's your own EMI. Contains PersonEntity UUID if it's a proxy EMI for a friend.
    val refNumber: String?,
    val itemName: String,
    val ownerType: String, // "ME" or "FRIEND"
    val principalAmount: Double,
    val monthlyEmiAmount: Double,
    val totalMonths: Int,
    val completedMonths: Int = 0,
    val startDate: Long,
    val status: String = "ACTIVE", // "ACTIVE" or "CLOSED"

    // Auditing & Sync
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.PENDING_INSERT
)