package com.example.cardceeza.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val actorEmail: String,
    val actorRole: String,
    val action: String, // USER_CREATED, RATE_UPDATED, TRADE_APPROVED, etc.
    val entity: String, // Trade, Rate, User, Payout
    val entityId: String,
    val details: String,
    val ipAddress: String = "197.210.55.18 (Lagos, NG)",
    val timestamp: Long = System.currentTimeMillis()
)
