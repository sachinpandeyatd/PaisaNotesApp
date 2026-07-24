package com.paisanotes.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val categoryId: String,
    val monthlyLimit: Double,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.PENDING_INSERT
)