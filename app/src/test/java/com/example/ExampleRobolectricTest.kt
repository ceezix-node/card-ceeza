package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.cardceeza.data.service.RiskEngine
import com.example.cardceeza.model.RiskLevel
import com.example.cardceeza.model.TradeStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches CardCeeza brand`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CardCeeza", appName)
    }

    @Test
    fun `rate engine gross and net payout calculation test`() {
        val faceValue = 100.0
        val ratePerUnit = 1430.0
        val fee = 0.0

        val grossNgn = faceValue * ratePerUnit
        val netPayout = grossNgn - fee

        assertEquals(143000.0, grossNgn, 0.001)
        assertEquals(143000.0, netPayout, 0.001)
    }

    @Test
    fun `risk engine assessment for regular trade`() {
        val result = RiskEngine.analyzeTrade(
            cardValue = 100.0,
            userCompletedTradesCount = 5,
            eCode = "X7KG-92MN-44LA-99BQ",
            hasEvidence = true,
            isKycVerified = true
        )

        assertEquals(RiskLevel.LOW, result.level)
        assertTrue(result.score < 35)
    }

    @Test
    fun `risk engine flags high risk for unverified user with large amount and no proof`() {
        val result = RiskEngine.analyzeTrade(
            cardValue = 1500.0,
            userCompletedTradesCount = 0,
            eCode = "123",
            hasEvidence = false,
            isKycVerified = false
        )

        assertTrue(result.level == RiskLevel.HIGH)
        assertTrue(result.score >= 60)
        assertTrue(result.flags.any { it.contains("High denomination") })
    }

    @Test
    fun `ledger service handles atomic credits and duplicate idempotency keys`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inMemoryDb = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            com.example.cardceeza.data.local.CardCeezaDatabase::class.java
        ).allowMainThreadQueries().build()

        val ledgerService = com.example.cardceeza.data.service.LedgerService(inMemoryDb)

        val userId = "test_user_001"
        val idempKey = "test_idemp_key_101"

        // 1. Initial credit
        val res1 = ledgerService.recordTradeCredit(
            userId = userId,
            tradeId = "trade_001",
            amount = 50000.0,
            referenceNumber = "CCZ-2026-TEST",
            idempotencyKey = idempKey,
            description = "Test trade settlement"
        )
        assertTrue(res1 is com.example.cardceeza.data.service.LedgerOperationResult.Success)
        assertEquals(50000.0, (res1 as com.example.cardceeza.data.service.LedgerOperationResult.Success).newBalance, 0.001)

        // 2. Duplicate credit attempt with same idempotency key
        val res2 = ledgerService.recordTradeCredit(
            userId = userId,
            tradeId = "trade_001",
            amount = 50000.0,
            referenceNumber = "CCZ-2026-TEST",
            idempotencyKey = idempKey,
            description = "Duplicate attempt"
        )
        assertTrue(res2 is com.example.cardceeza.data.service.LedgerOperationResult.DuplicateOperation)
        // Balance must remain 50000.0, not 100000.0
        assertEquals(50000.0, (res2 as com.example.cardceeza.data.service.LedgerOperationResult.DuplicateOperation).currentBalance, 0.001)

        // 3. Withdrawal debit
        val res3 = ledgerService.recordWithdrawalDebit(
            userId = userId,
            amount = 20000.0,
            referenceNumber = "WTH-TEST-1",
            idempotencyKey = "withdraw_idemp_1",
            description = "Test withdrawal",
            actorEmail = "user@test.ng"
        )
        assertTrue(res3 is com.example.cardceeza.data.service.LedgerOperationResult.Success)
        assertEquals(30000.0, (res3 as com.example.cardceeza.data.service.LedgerOperationResult.Success).newBalance, 0.001)

        // 4. Overdraft protection
        val res4 = ledgerService.recordWithdrawalDebit(
            userId = userId,
            amount = 50000.0, // Exceeds 30,000 balance
            referenceNumber = "WTH-TEST-2",
            idempotencyKey = "withdraw_idemp_2",
            description = "Overdraft attempt",
            actorEmail = "user@test.ng"
        )
        assertTrue(res4 is com.example.cardceeza.data.service.LedgerOperationResult.InsufficientFunds)

        inMemoryDb.close()
    }
}
