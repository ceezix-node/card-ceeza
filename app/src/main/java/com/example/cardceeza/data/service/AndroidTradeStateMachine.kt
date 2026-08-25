package com.example.cardceeza.data.service

import androidx.room.withTransaction
import com.example.cardceeza.data.local.CardCeezaDatabase
import com.example.cardceeza.data.local.entity.AuditLogEntity
import com.example.cardceeza.data.local.entity.NotificationEntity
import com.example.cardceeza.data.local.entity.TradeEntity
import com.example.cardceeza.data.local.entity.TradeEventEntity
import com.example.cardceeza.model.TradeStatus
import com.example.cardceeza.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Result sealed class for trade state transitions.
 */
sealed class TradeTransitionResult {
    data class Success(
        val trade: TradeEntity,
        val event: TradeEventEntity,
        val message: String
    ) : TradeTransitionResult()

    data class IllegalTransition(
        val from: TradeStatus,
        val to: TradeStatus,
        val role: UserRole,
        val message: String
    ) : TradeTransitionResult()

    data class Error(
        val errorCode: String,
        val message: String
    ) : TradeTransitionResult()
}

/**
 * Android Client/Local Trade State Machine
 * Mirrors backend rules for local offline transactions and immediate verification feedback.
 */
class AndroidTradeStateMachine(
    private val db: CardCeezaDatabase
) {

    fun isAllowedTransition(from: TradeStatus, to: TradeStatus, role: UserRole): Boolean {
        return when (to) {
            TradeStatus.SUBMITTED -> from == TradeStatus.DRAFT
            TradeStatus.UNDER_REVIEW -> (from == TradeStatus.SUBMITTED || from == TradeStatus.VERIFICATION_REQUIRED) &&
                    (role == UserRole.VERIFIER || role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN)
            TradeStatus.VERIFICATION_REQUIRED -> from == TradeStatus.UNDER_REVIEW &&
                    (role == UserRole.VERIFIER || role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN)
            TradeStatus.VERIFIED -> from == TradeStatus.UNDER_REVIEW &&
                    (role == UserRole.VERIFIER || role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN)
            TradeStatus.REJECTED -> (from == TradeStatus.UNDER_REVIEW || from == TradeStatus.SUBMITTED || from == TradeStatus.VERIFICATION_REQUIRED) &&
                    (role == UserRole.VERIFIER || role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN)
            TradeStatus.APPROVED -> from == TradeStatus.VERIFIED &&
                    (role == UserRole.VERIFIER || role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN)
            TradeStatus.PAYOUT_PENDING -> (from == TradeStatus.APPROVED || from == TradeStatus.VERIFIED) &&
                    (role == UserRole.FINANCE || role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN)
            TradeStatus.PAID -> (from == TradeStatus.PAYOUT_PENDING || from == TradeStatus.APPROVED) &&
                    (role == UserRole.FINANCE || role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN)
            TradeStatus.CANCELLED -> (from == TradeStatus.DRAFT || from == TradeStatus.SUBMITTED)
            TradeStatus.DISPUTED -> (from == TradeStatus.REJECTED || from == TradeStatus.UNDER_REVIEW)
            TradeStatus.DRAFT -> false
        }
    }

    suspend fun transition(
        tradeId: String,
        targetStatus: TradeStatus,
        actorId: String,
        actorEmail: String,
        actorRole: UserRole,
        notes: String? = null,
        rejectionReason: String? = null
    ): TradeTransitionResult = withContext(Dispatchers.IO) {
        try {
            db.withTransaction {
                val trade = db.tradeDao().getTradeById(tradeId)
                    ?: return@withTransaction TradeTransitionResult.Error("NOT_FOUND", "Trade $tradeId not found")

                val currentStatus = trade.status

                if (!isAllowedTransition(currentStatus, targetStatus, actorRole)) {
                    return@withTransaction TradeTransitionResult.IllegalTransition(
                        from = currentStatus,
                        to = targetStatus,
                        role = actorRole,
                        message = "Transition from $currentStatus to $targetStatus is not permitted for $actorRole"
                    )
                }

                // 1. Update Trade Record
                val updatedTrade = trade.copy(
                    status = targetStatus,
                    rejectionReason = rejectionReason ?: trade.rejectionReason,
                    updatedAt = System.currentTimeMillis()
                )
                db.tradeDao().updateTrade(updatedTrade)

                // 2. Insert Immutable Trade Event
                val event = TradeEventEntity(
                    id = UUID.randomUUID().toString(),
                    tradeId = trade.id,
                    fromStatus = currentStatus,
                    toStatus = targetStatus,
                    actorRole = actorRole.name,
                    actorName = actorEmail,
                    note = notes ?: rejectionReason ?: "Transitioned to $targetStatus",
                    timestamp = System.currentTimeMillis()
                )
                db.tradeEventDao().insertTradeEvent(event)

                // 3. Create Notification
                db.notificationDao().insertNotification(
                    NotificationEntity(
                        id = UUID.randomUUID().toString(),
                        userId = trade.userId,
                        title = "Trade ${trade.tradeNumber} Update",
                        message = "Status changed to ${targetStatus.name.replace('_', ' ')}",
                        type = "TRADE_STATUS"
                    )
                )

                // 4. Audit Log
                db.auditDao().insertAuditLog(
                    AuditLogEntity(
                        id = UUID.randomUUID().toString(),
                        actorEmail = actorEmail,
                        actorRole = actorRole.name,
                        action = "TRADE_STATUS_CHANGE",
                        entity = "Trade",
                        entityId = trade.id,
                        details = "$currentStatus -> $targetStatus | Notes: ${notes ?: "N/A"}",
                        timestamp = System.currentTimeMillis()
                    )
                )

                TradeTransitionResult.Success(
                    trade = updatedTrade,
                    event = event,
                    message = "Trade transitioned to $targetStatus successfully"
                )
            }
        } catch (e: Exception) {
            TradeTransitionResult.Error(
                errorCode = "TRANSACTION_FAILED",
                message = e.message ?: "Failed to execute state transition"
            )
        }
    }
}
