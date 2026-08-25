package com.example.cardceeza.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String, // TRADE_UPDATE, PAYOUT_SUCCESS, SECURITY, SYSTEM
    val tradeId: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
