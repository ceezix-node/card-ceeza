package com.example.cardceeza.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bank_accounts")
data class BankAccountEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val bankName: String,
    val bankCode: String,
    val accountNumber: String, // Stored encrypted/masked in UI
    val accountName: String,
    val isDefault: Boolean = false,
    val isVerified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
