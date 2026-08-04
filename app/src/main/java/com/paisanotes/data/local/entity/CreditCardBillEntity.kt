package com.paisanotes.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "credit_card_bills")
data class CreditCardBillEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val accountId: String,
    val billingMonth: String,
    val totalBilledAmount: Double,
    val minimumDue: Double,
    val amountPaid: Double,
    val dueDate: Long?,
    val status: String,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.PENDING_INSERT
)