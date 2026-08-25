package com.example.cardceeza.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.cardceeza.model.RiskLevel
import com.example.cardceeza.model.TradeStatus

@Entity(tableName = "trades")
data class TradeEntity(
    @PrimaryKey val id: String,
    val tradeNumber: String, // e.g. CCZ-2026-000124
    val userId: String,
    val userEmail: String,
    val userName: String,
    val cardId: String,
    val cardName: String,
    val region: String,
    val currency: String,
    val cardValue: Double,
    val appliedRate: Double,
    val grossNgn: Double,
    val feeNgn: Double,
    val netPayoutNgn: Double,
    val status: TradeStatus = TradeStatus.SUBMITTED,
    val riskScore: Int = 15,
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val riskFlags: String = "",
    val eCodeOrPin: String = "", // E-code or Pin (masked where appropriate)
    val evidenceUri: String = "",
    val receiptUri: String = "",
    val payoutBankName: String = "",
    val payoutAccountNumberMasked: String = "",
    val payoutAccountName: String = "",
    val payoutReference: String = "",
    val rejectionReason: String = "",
    val verifierNotes: String = "",
    val verifierId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
