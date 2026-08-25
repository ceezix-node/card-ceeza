package com.example.cardceeza.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.cardceeza.model.KycStatus
import com.example.cardceeza.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val role: UserRole = UserRole.USER,
    val kycStatus: KycStatus = KycStatus.KYC_NOT_STARTED,
    val bvnOrNinMasked: String = "",
    val twoFactorEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
