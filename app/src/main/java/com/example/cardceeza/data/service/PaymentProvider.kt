package com.example.cardceeza.data.service

import kotlinx.coroutines.delay
import java.util.UUID

data class BankInfo(
    val code: String,
    val name: String,
    val slug: String
)

data class AccountVerificationResult(
    val isValid: Boolean,
    val accountName: String,
    val bankName: String,
    val errorMessage: String? = null
)

data class PayoutResult(
    val isSuccess: Boolean,
    val reference: String,
    val status: String,
    val settledAt: Long,
    val feeNgn: Double,
    val message: String
)

interface PaymentProvider {
    suspend fun getNigerianBanks(): List<BankInfo>
    suspend fun verifyNigerianBankAccount(bankCode: String, accountNumber: String): AccountVerificationResult
    suspend fun processNgnPayout(
        accountNumber: String,
        bankCode: String,
        amountNgn: Double,
        tradeNumber: String,
        recipientName: String
    ): PayoutResult
}

class MockNigerianPaymentProvider : PaymentProvider {

    private val supportedBanks = listOf(
        BankInfo("058", "Guaranty Trust Bank (GTBank)", "gtbank"),
        BankInfo("044", "Access Bank Plc", "access"),
        BankInfo("057", "Zenith Bank Plc", "zenith"),
        BankInfo("090267", "Kuda Microfinance Bank", "kuda"),
        BankInfo("090405", "OPay Digital Services (Paycom)", "opay"),
        BankInfo("090336", "PalmPay Limited", "palmpay"),
        BankInfo("011", "First Bank of Nigeria", "firstbank"),
        BankInfo("033", "United Bank for Africa (UBA)", "uba"),
        BankInfo("050", "Ecobank Nigeria", "ecobank"),
        BankInfo("214", "First City Monument Bank (FCMB)", "fcmb"),
        BankInfo("035", "Wema Bank Plc (ALAT)", "wema"),
        BankInfo("221", "Stanbic IBTC Bank", "stanbic"),
        BankInfo("070", "Fidelity Bank Plc", "fidelity"),
        BankInfo("090551", "Moniepoint Microfinance Bank", "moniepoint")
    )

    override suspend fun getNigerianBanks(): List<BankInfo> {
        delay(150) // Simulating fast lookup
        return supportedBanks
    }

    override suspend fun verifyNigerianBankAccount(bankCode: String, accountNumber: String): AccountVerificationResult {
        delay(600) // Simulating NIP name inquiry call
        val bank = supportedBanks.find { it.code == bankCode } ?: supportedBanks.first()
        if (accountNumber.length != 10 || !accountNumber.all { it.isDigit() }) {
            return AccountVerificationResult(
                isValid = false,
                accountName = "",
                bankName = bank.name,
                errorMessage = "Nigerian NUBAN account number must be exactly 10 digits."
            )
        }

        val mockNames = listOf(
            "CHUKWUDI EMEKA OKONKWO",
            "OLUWASEUN ADEBAYO JOHNSON",
            "BABATUNDE IBRAHIM YUSUF",
            "NGOZI PRECIOUS EZE",
            "SAMUEL CHUKWUDI OKAFOR",
            "FATIMA HASSAN BELLO"
        )
        val hash = accountNumber.hashCode().let { if (it < 0) -it else it }
        val generatedName = mockNames[hash % mockNames.size]

        return AccountVerificationResult(
            isValid = true,
            accountName = generatedName,
            bankName = bank.name
        )
    }

    override suspend fun processNgnPayout(
        accountNumber: String,
        bankCode: String,
        amountNgn: Double,
        tradeNumber: String,
        recipientName: String
    ): PayoutResult {
        delay(1000) // Simulating payment gateway settlement
        val ref = "PAY-NGN-${System.currentTimeMillis()}-${(1000..9999).random()}"
        return PayoutResult(
            isSuccess = true,
            reference = ref,
            status = "SETTLED_SUCCESS",
            settledAt = System.currentTimeMillis(),
            feeNgn = 0.0,
            message = "₦${"%,.2f".format(amountNgn)} successfully settled to $recipientName ($accountNumber)."
        )
    }
}
