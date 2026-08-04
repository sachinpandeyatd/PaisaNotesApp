package com.paisanotes.data.mapper

import com.paisanotes.data.local.entity.CreditCardBillEntity
import com.paisanotes.data.local.entity.SyncStatus
import com.paisanotes.data.remote.dto.CreditCardBillDto
import com.paisanotes.domain.model.CreditCardBill
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun CreditCardBillEntity.toDomainModel() = CreditCardBill(id, accountId, billingMonth, totalBilledAmount, minimumDue, amountPaid, dueDate, status)

fun CreditCardBill.toEntity(syncStatus: SyncStatus = SyncStatus.PENDING_INSERT, createdAt: Long = System.currentTimeMillis(), updatedAt: Long = System.currentTimeMillis()) =
    CreditCardBillEntity(id, accountId, billingMonth, totalBilledAmount, minimumDue, amountPaid, dueDate, status, createdAt, updatedAt, syncStatus = syncStatus)

fun CreditCardBillEntity.toDto(): CreditCardBillDto {
    val formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC"))
    return CreditCardBillDto(
        id, accountId, billingMonth, totalBilledAmount, minimumDue, amountPaid,
        dueDate?.let { formatter.format(Instant.ofEpochMilli(it)).substring(0, 10) }, // "YYYY-MM-DD"
        status, formatter.format(Instant.ofEpochMilli(createdAt)), formatter.format(Instant.ofEpochMilli(updatedAt)), isDeleted
    )
}

fun CreditCardBillDto.toEntity(): CreditCardBillEntity {
    val parsedDue = dueDate?.let { try { java.time.LocalDate.parse(it).atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli() } catch (e: Exception) { null } }
    val parsedCreated = try { Instant.parse(createdAt).toEpochMilli() } catch (e: Exception) { System.currentTimeMillis() }
    val parsedUpdated = try { Instant.parse(updatedAt).toEpochMilli() } catch (e: Exception) { System.currentTimeMillis() }

    return CreditCardBillEntity(id, accountId, billingMonth, totalBilledAmount, minimumDue, amountPaid, parsedDue, status, parsedCreated, parsedUpdated, isDeleted, SyncStatus.SYNCED)
}