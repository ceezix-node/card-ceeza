package com.example.cardceeza.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gift_card_types")
data class GiftCardEntity(
    @PrimaryKey val id: String,
    val name: String,
    val brand: String,
    val slug: String,
    val category: String, // e.g., Gaming, Shopping, Entertainment, Tech
    val country: String, // e.g., "US", "UK", "CA"
    val currency: String, // "USD", "GBP", "CAD"
    val active: Boolean = true,
    val iconName: String,
    val minDenomination: Double = 25.0,
    val maxDenomination: Double = 1000.0,
    val verificationMethod: String = "Code + Receipt Scan",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
