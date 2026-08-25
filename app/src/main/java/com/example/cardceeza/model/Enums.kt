package com.example.cardceeza.model

enum class UserRole {
    USER,
    VERIFIER,
    SUPPORT,
    FINANCE,
    ADMIN,
    SUPER_ADMIN
}

enum class TradeStatus(val label: String) {
    DRAFT("Draft"),
    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under Review"),
    VERIFICATION_REQUIRED("Verification Required"),
    VERIFIED("Verified"),
    REJECTED("Rejected"),
    APPROVED("Approved"),
    PAYOUT_PENDING("Payout Pending"),
    PAID("Paid"),
    CANCELLED("Cancelled"),
    DISPUTED("Disputed")
}

enum class KycStatus(val label: String) {
    KYC_NOT_STARTED("Not Started"),
    KYC_PENDING("Pending Verification"),
    KYC_VERIFIED("Verified (Tier 2)"),
    KYC_REJECTED("Rejected")
}

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class LedgerType(val label: String) {
    TRADE_CREDIT("Trade Credit"),
    WITHDRAWAL_PAYOUT("Bank Withdrawal"),
    BONUS_REWARD("Referral Bonus"),
    FEE_DEDUCTION("Platform Fee"),
    SYSTEM_ADJUSTMENT("System Adjustment")
}

enum class TicketCategory(val label: String) {
    TRADE_ISSUE("Trade Issue"),
    PAYOUT_ISSUE("Payout Issue"),
    ACCOUNT_ISSUE("Account Issue"),
    VERIFICATION_ISSUE("Verification Issue"),
    OTHER("Other")
}

enum class TicketPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

enum class TicketStatus(val label: String) {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    WAITING_FOR_USER("Waiting for User"),
    RESOLVED("Resolved"),
    CLOSED("Closed")
}
