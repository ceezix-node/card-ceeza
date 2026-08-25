package com.example.cardceeza.data.service

import com.example.cardceeza.model.RiskLevel

data class RiskAnalysisResult(
    val score: Int, // 0 to 100
    val level: RiskLevel,
    val flags: List<String>,
    val autoApprovable: Boolean
)

object RiskEngine {

    fun analyzeTrade(
        cardValue: Double,
        userCompletedTradesCount: Int,
        eCode: String,
        hasEvidence: Boolean,
        isKycVerified: Boolean
    ): RiskAnalysisResult {
        var score = 10
        val flags = mutableListOf<String>()

        if (!hasEvidence) {
            score += 30
            flags.add("No purchase receipt or physical card image attached")
        }

        if (!isKycVerified) {
            score += 20
            flags.add("User KYC Tier 1 / unverified identity")
        }

        if (cardValue >= 500.0) {
            score += 25
            flags.add("High denomination trade ($$cardValue)")
        } else if (cardValue >= 200.0) {
            score += 10
            flags.add("Medium denomination trade ($$cardValue)")
        }

        if (userCompletedTradesCount == 0) {
            score += 15
            flags.add("First-time trader account")
        }

        if (eCode.trim().length < 8) {
            score += 35
            flags.add("Suspicious short code format")
        }

        val level = when {
            score >= 60 -> RiskLevel.HIGH
            score >= 35 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

        return RiskAnalysisResult(
            score = score.coerceIn(0, 100),
            level = level,
            flags = flags,
            autoApprovable = level == RiskLevel.LOW && isKycVerified && userCompletedTradesCount > 2
        )
    }
}
