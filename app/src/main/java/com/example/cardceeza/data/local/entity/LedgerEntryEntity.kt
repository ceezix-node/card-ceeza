package com.example.cardceeza.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.cardceeza.model.LedgerType

@Entity(tableName = "ledger_entries")
data class LedgerEntryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val tradeId: String = "",
    val referenceNumber: String,
    val type: LedgerType,
    val amount: Double, // Positive for credit, negative for debit
    val balanceAfter: Double,
    val description: String,
    val idempotencyKey: String,
    val createdAt: Long = System.currentTimeMillis()
)
