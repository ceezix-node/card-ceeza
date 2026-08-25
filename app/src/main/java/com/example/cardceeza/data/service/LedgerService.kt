package com.example.cardceeza.data.service

import androidx.room.withTransaction
import com.example.cardceeza.data.local.CardCeezaDatabase
import com.example.cardceeza.data.local.entity.AuditLogEntity
import com.example.cardceeza.data.local.entity.LedgerEntryEntity
import com.example.cardceeza.model.LedgerType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Result sealed class for all financial ledger operations.
 */
sealed class LedgerOperationResult {
    data class Success(
        val entry: LedgerEntryEntity,
        val newBalance: Double,
        val message: String
    ) : LedgerOperationResult()

    data class DuplicateOperation(
        val existingEntry: LedgerEntryEntity,
        val currentBalance: Double,
        val message: String = "Transaction already processed with this idempotency key"
    ) : LedgerOperationResult()

    data class InsufficientFunds(
        val requestedAmount: Double,
        val availableBalance: Double,
        val message: String = "Insufficient wallet balance to perform this operation"
    ) : LedgerOperationResult()

    data class Error(
        val errorCode: String,
        val message: String
    ) : LedgerOperationResult()
}

/**
 * Production Ledger Service
 *
 * Enforces financial invariants:
 * 1. Double-entry immutable ledger accounting.
 * 2. Strict atomicity through database transactions.
 * 3. Idempotency guarantees to prevent double credits or duplicate withdrawals.
 * 4. Real-time balance derivation (balance cannot be directly written/mutated).
 * 5. Comprehensive audit trail logging for all financial events.
 */
class LedgerService(
    private val database: CardCeezaDatabase
) {

    /**
     * Atomically credits a user's wallet upon verified & approved gift card trade settlement.
     *
     * @param userId The recipient user ID.
     * @param tradeId The trade identifier being settled.
     * @param amount The net NGN amount to credit (must be > 0).
     * @param referenceNumber The settlement reference (e.g. payout ref or trade number).
     * @param idempotencyKey Unique key to prevent double credit on retry or concurrent submission.
     * @param description Human-readable description of the credit transaction.
     * @param actorEmail Email of the administrator/system actor executing the credit.
     */
    suspend fun recordTradeCredit(
        userId: String,
        tradeId: String,
        amount: Double,
        referenceNumber: String,
        idempotencyKey: String,
        description: String,
        actorEmail: String = "finance@cardceeza.com"
    ): LedgerOperationResult = withContext(Dispatchers.IO) {
        if (amount <= 0) {
            return@withContext LedgerOperationResult.Error(
                errorCode = "INVALID_AMOUNT",
                message = "Credit amount must be greater than zero (received ₦$amount)"
            )
        }

        try {
            database.withTransaction {
                // 1. Idempotency Check: Check if an entry with this key already exists
                val existingEntry = database.ledgerDao().getEntryByIdempotencyKey(idempotencyKey)
                if (existingEntry != null) {
                    val currentBal = database.ledgerDao().getSyncUserBalance(userId)
                    return@withTransaction LedgerOperationResult.DuplicateOperation(
                        existingEntry = existingEntry,
                        currentBalance = currentBal
                    )
                }

                // 2. Fetch current balance inside transaction
                val currentBalance = database.ledgerDao().getSyncUserBalance(userId)
                val newBalance = currentBalance + amount

                // 3. Create immutable Ledger entry
                val newEntry = LedgerEntryEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    tradeId = tradeId,
                    referenceNumber = referenceNumber,
                    type = LedgerType.TRADE_CREDIT,
                    amount = amount, // Positive amount
                    balanceAfter = newBalance,
                    description = description,
                    idempotencyKey = idempotencyKey,
                    createdAt = System.currentTimeMillis()
                )
                database.ledgerDao().insertLedgerEntry(newEntry)

                // 4. Record audit log
                database.auditDao().insertAuditLog(
                    AuditLogEntity(
                        id = UUID.randomUUID().toString(),
                        actorEmail = actorEmail,
                        actorRole = "FINANCE",
                        action = "LEDGER_TRADE_CREDIT",
                        entity = "LedgerEntry",
                        entityId = newEntry.id,
                        details = "Credited ₦${"%,.2f".format(amount)} to user $userId for Trade $tradeId. New Balance: ₦${"%,.2f".format(newBalance)}",
                        timestamp = System.currentTimeMillis()
                    )
                )

                LedgerOperationResult.Success(
                    entry = newEntry,
                    newBalance = newBalance,
                    message = "Successfully credited ₦${"%,.2f".format(amount)}"
                )
            }
        } catch (e: Exception) {
            LedgerOperationResult.Error(
                errorCode = "TRANSACTION_FAILED",
                message = e.message ?: "Failed to process ledger credit"
            )
        }
    }

    /**
     * Atomically debits a user's wallet for a payout / withdrawal to a verified Nigerian bank account.
     *
     * @param userId The user requesting withdrawal.
     * @param amount The NGN amount to withdraw (must be > 0).
     * @param referenceNumber The payout reference (e.g. PAY-2026-000412).
     * @param idempotencyKey Unique key to ensure single debit.
     * @param description Description of the bank destination.
     * @param actorEmail The user/actor triggering withdrawal.
     */
    suspend fun recordWithdrawalDebit(
        userId: String,
        amount: Double,
        referenceNumber: String,
        idempotencyKey: String,
        description: String,
        actorEmail: String
    ): LedgerOperationResult = withContext(Dispatchers.IO) {
        if (amount <= 0) {
            return@withContext LedgerOperationResult.Error(
                errorCode = "INVALID_AMOUNT",
                message = "Withdrawal amount must be greater than zero"
            )
        }

        try {
            database.withTransaction {
                // 1. Idempotency Check
                val existingEntry = database.ledgerDao().getEntryByIdempotencyKey(idempotencyKey)
                if (existingEntry != null) {
                    val currentBal = database.ledgerDao().getSyncUserBalance(userId)
                    return@withTransaction LedgerOperationResult.DuplicateOperation(
                        existingEntry = existingEntry,
                        currentBalance = currentBal
                    )
                }

                // 2. Atomic Balance Check & Overdraft Protection
                val currentBalance = database.ledgerDao().getSyncUserBalance(userId)
                if (currentBalance < amount) {
                    return@withTransaction LedgerOperationResult.InsufficientFunds(
                        requestedAmount = amount,
                        availableBalance = currentBalance
                    )
                }

                val newBalance = currentBalance - amount

                // 3. Create negative amount debit entry
                val debitEntry = LedgerEntryEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    tradeId = "",
                    referenceNumber = referenceNumber,
                    type = LedgerType.WITHDRAWAL_PAYOUT,
                    amount = -amount, // Negative for debit
                    balanceAfter = newBalance,
                    description = description,
                    idempotencyKey = idempotencyKey,
                    createdAt = System.currentTimeMillis()
                )
                database.ledgerDao().insertLedgerEntry(debitEntry)

                // 4. Audit Log
                database.auditDao().insertAuditLog(
                    AuditLogEntity(
                        id = UUID.randomUUID().toString(),
                        actorEmail = actorEmail,
                        actorRole = "USER",
                        action = "LEDGER_WITHDRAWAL_DEBIT",
                        entity = "LedgerEntry",
                        entityId = debitEntry.id,
                        details = "Debited ₦${"%,.2f".format(amount)} for withdrawal $referenceNumber. Remaining: ₦${"%,.2f".format(newBalance)}",
                        timestamp = System.currentTimeMillis()
                    )
                )

                LedgerOperationResult.Success(
                    entry = debitEntry,
                    newBalance = newBalance,
                    message = "Successfully debited ₦${"%,.2f".format(amount)}"
                )
            }
        } catch (e: Exception) {
            LedgerOperationResult.Error(
                errorCode = "TRANSACTION_FAILED",
                message = e.message ?: "Failed to process withdrawal debit"
            )
        }
    }

    /**
     * Atomically processes reversals or fee adjustments with full ledger tracking.
     */
    suspend fun recordAdjustment(
        userId: String,
        amount: Double,
        type: LedgerType,
        referenceNumber: String,
        idempotencyKey: String,
        reason: String,
        adminEmail: String
    ): LedgerOperationResult = withContext(Dispatchers.IO) {
        try {
            database.withTransaction {
                val existingEntry = database.ledgerDao().getEntryByIdempotencyKey(idempotencyKey)
                if (existingEntry != null) {
                    val currentBal = database.ledgerDao().getSyncUserBalance(userId)
                    return@withTransaction LedgerOperationResult.DuplicateOperation(
                        existingEntry = existingEntry,
                        currentBalance = currentBal
                    )
                }

                val currentBalance = database.ledgerDao().getSyncUserBalance(userId)
                val newBalance = currentBalance + amount

                if (newBalance < 0) {
                    return@withTransaction LedgerOperationResult.InsufficientFunds(
                        requestedAmount = -amount,
                        availableBalance = currentBalance
                    )
                }

                val entry = LedgerEntryEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    tradeId = "",
                    referenceNumber = referenceNumber,
                    type = type,
                    amount = amount,
                    balanceAfter = newBalance,
                    description = "Adjustment: $reason",
                    idempotencyKey = idempotencyKey,
                    createdAt = System.currentTimeMillis()
                )
                database.ledgerDao().insertLedgerEntry(entry)

                database.auditDao().insertAuditLog(
                    AuditLogEntity(
                        id = UUID.randomUUID().toString(),
                        actorEmail = adminEmail,
                        actorRole = "ADMIN",
                        action = "LEDGER_ADJUSTMENT",
                        entity = "LedgerEntry",
                        entityId = entry.id,
                        details = "Adjustment of ₦${"%,.2f".format(amount)} ($type). Reason: $reason",
                        timestamp = System.currentTimeMillis()
                    )
                )

                LedgerOperationResult.Success(
                    entry = entry,
                    newBalance = newBalance,
                    message = "Adjustment applied successfully"
                )
            }
        } catch (e: Exception) {
            LedgerOperationResult.Error(
                errorCode = "TRANSACTION_FAILED",
                message = e.message ?: "Failed to process ledger adjustment"
            )
        }
    }
}
