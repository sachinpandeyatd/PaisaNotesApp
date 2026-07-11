package com.paisanotes.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(), // Offline ID generation!
    
    val amount: Double,
    val transactionType: String, // "INCOME" or "EXPENSE"
    val merchant: String?,
    val category: String,
    val transactionDate: Long, // SQLite doesn't support ZonedDateTime natively, we store Unix Epoch Millis
    val paymentMethod: String,
    val source: String,
    val notes: String?,
    
    // Auditing
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    
    // OFFLINE-FIRST MAGIC
    val syncStatus: SyncStatus = SyncStatus.PENDING_INSERT
)