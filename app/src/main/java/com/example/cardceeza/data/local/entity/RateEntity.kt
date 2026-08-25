package com.example.cardceeza.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gift_card_rates")
data class RateEntity(
    @PrimaryKey val id: String,
    val cardId: String,
    val cardName: String,
    val region: String,
    val currency: String,
    val ratePerUnit: Double, // NGN per $1/£1
    val minimumValue: Double = 10.0,
    val maximumValue: Double = 2000.0,
    val fee: Double = 0.0, // Fixed platform fee in NGN
    val feePercentage: Double = 0.0, // Fee %
    val status: String = "ACTIVE",
    val lastShiftPercentage: Double = 0.0,
    val effectiveFrom: Long = System.currentTimeMillis(),
    val effectiveUntil: Long = System.currentTimeMillis() + 86400000L * 30,
    val updatedAt: Long = System.currentTimeMillis()
)
