package com.paisanotes.data.mapper

import com.paisanotes.data.local.entity.SyncStatus
import com.paisanotes.data.local.entity.TransactionEntity
import com.paisanotes.data.remote.dto.TransactionDto
import com.paisanotes.domain.model.Transaction
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

// 1. Map Entity (DB) -> Domain (UI)
fun TransactionEntity.toDomainModel(): Transaction {
    return Transaction(
        id = id,
        amount = amount,
        transactionType = transactionType,
        merchant = merchant,
        category = category,
        categoryId = categoryId,
        accountId = accountId,
        transferAccountId = transferAccountId,
        transactionDate = transactionDate,
        paymentMethod = paymentMethod,
        source = source,
        notes = notes
    )
}

// 2. Map Domain (UI) -> Entity (DB)
fun Transaction.toEntity(
    syncStatus: SyncStatus = SyncStatus.PENDING_INSERT,
    createdAt: Long = System.currentTimeMillis(),
    updatedAt: Long = System.currentTimeMillis(),
    isDeleted: Boolean = false
): TransactionEntity {
    return TransactionEntity(
        id = id,
        amount = amount,
        transactionType = transactionType,
        merchant = merchant,
        category = category,
        categoryId = categoryId,
        accountId = accountId,
        transferAccountId = transferAccountId,
        transactionDate = transactionDate,
        paymentMethod = paymentMethod,
        source = source,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        syncStatus = syncStatus
    )
}

// 3. Map DTO (Network) -> Entity (DB)
fun TransactionDto.toEntity(): TransactionEntity {
    // Spring sends ISO-8601 Strings. We need to convert them to Long (Epoch Millis) for Room DB.
    val parsedDate = ZonedDateTime.parse(transactionDate, DateTimeFormatter.ISO_ZONED_DATE_TIME).toInstant().toEpochMilli()
    val parsedCreated = ZonedDateTime.parse(createdAt, DateTimeFormatter.ISO_ZONED_DATE_TIME).toInstant().toEpochMilli()
    val parsedUpdated = ZonedDateTime.parse(updatedAt, DateTimeFormatter.ISO_ZONED_DATE_TIME).toInstant().toEpochMilli()

    return TransactionEntity(
        id = id,
        amount = amount,
        transactionType = transactionType,
        merchant = merchant,
        category = category,
        transactionDate = parsedDate,
        paymentMethod = paymentMethod,
        source = source,
        notes = notes,
        categoryId = categoryId,
        accountId = accountId,
        transferAccountId = transferAccountId,
        createdAt = parsedCreated,
        updatedAt = parsedUpdated,
        isDeleted = isDeleted,
        syncStatus = SyncStatus.SYNCED // Coming straight from server, so it's perfectly synced!
    )
}

// 4. Map Entity (DB) -> DTO (Network)
fun TransactionEntity.toDto(): TransactionDto {
    val formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC"))

    return TransactionDto(
        id = id,
        amount = amount,
        transactionType = transactionType,
        merchant = merchant,
        category = category,
        transactionDate = formatter.format(Instant.ofEpochMilli(transactionDate)),
        paymentMethod = paymentMethod,
        source = source,
        notes = notes,
        categoryId = categoryId,
        accountId = accountId,
        transferAccountId = transferAccountId,
        createdAt = formatter.format(Instant.ofEpochMilli(createdAt)),
        updatedAt = formatter.format(Instant.ofEpochMilli(updatedAt)),
        isDeleted = isDeleted
    )
}