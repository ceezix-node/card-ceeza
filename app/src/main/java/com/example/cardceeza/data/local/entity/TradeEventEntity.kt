package com.example.cardceeza.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.cardceeza.model.TradeStatus

@Entity(tableName = "trade_events")
data class TradeEventEntity(
    @PrimaryKey val id: String,
    val tradeId: String,
    val fromStatus: TradeStatus,
    val toStatus: TradeStatus,
    val actorRole: String,
    val actorName: String,
    val note: String,
    val timestamp: Long = System.currentTimeMillis()
)
