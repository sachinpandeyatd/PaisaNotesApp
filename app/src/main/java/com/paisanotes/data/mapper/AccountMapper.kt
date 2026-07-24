package com.paisanotes.data.mapper

import com.paisanotes.data.local.dao.AccountWithBalanceTuple
import com.paisanotes.data.local.entity.AccountEntity
import com.paisanotes.data.local.entity.SyncStatus
import com.paisanotes.data.remote.dto.AccountDto
import com.paisanotes.domain.model.Account
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun AccountWithBalanceTuple.toDomainModel() = Account(id, name, type, initialBalance, currentBalance)
fun AccountEntity.toDomainModel() = Account(id, name, type, initialBalance, initialBalance) // Fallback

fun Account.toEntity(syncStatus: SyncStatus = SyncStatus.PENDING_INSERT) = AccountEntity(
    id = id, name = name, type = type, initialBalance = initialBalance, syncStatus = syncStatus
)

fun AccountEntity.toDto(): AccountDto {
    val formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC"))
    return AccountDto(id, name, type, initialBalance, formatter.format(Instant.ofEpochMilli(createdAt)), formatter.format(Instant.ofEpochMilli(updatedAt)), isDeleted)
}

fun AccountDto.toEntity(): AccountEntity {
    val parsedCreated = try { Instant.parse(createdAt).toEpochMilli() } catch (e: Exception) { System.currentTimeMillis() }
    val parsedUpdated = try { Instant.parse(updatedAt).toEpochMilli() } catch (e: Exception) { System.currentTimeMillis() }
    return AccountEntity(id, name, type, initialBalance, parsedCreated, parsedUpdated, isDeleted, SyncStatus.SYNCED)
}