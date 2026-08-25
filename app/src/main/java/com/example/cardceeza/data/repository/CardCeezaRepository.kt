package com.example.cardceeza.data.repository

import com.example.cardceeza.data.local.CardCeezaDatabase
import com.example.cardceeza.data.local.entity.AuditLogEntity
import com.example.cardceeza.data.local.entity.BankAccountEntity
import com.example.cardceeza.data.local.entity.GiftCardEntity
import com.example.cardceeza.data.local.entity.LedgerEntryEntity
import com.example.cardceeza.data.local.entity.NotificationEntity
import com.example.cardceeza.data.local.entity.RateEntity
import com.example.cardceeza.data.local.entity.SupportTicketEntity
import com.example.cardceeza.data.local.entity.TradeEntity
import com.example.cardceeza.data.local.entity.TradeEventEntity
import com.example.cardceeza.data.local.entity.UserEntity
import com.example.cardceeza.data.service.AccountVerificationResult
import com.example.cardceeza.data.service.BankInfo
import com.example.cardceeza.data.service.LedgerOperationResult
import com.example.cardceeza.data.service.LedgerService
import com.example.cardceeza.data.service.MockNigerianPaymentProvider
import com.example.cardceeza.data.service.PaymentProvider
import com.example.cardceeza.data.service.RiskEngine
import com.example.cardceeza.model.KycStatus
import com.example.cardceeza.model.LedgerType
import com.example.cardceeza.model.TicketCategory
import com.example.cardceeza.model.TicketPriority
import com.example.cardceeza.model.TicketStatus
import com.example.cardceeza.model.TradeStatus
import com.example.cardceeza.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class CardCeezaRepository(
    private val db: CardCeezaDatabase,
    private val paymentProvider: PaymentProvider = MockNigerianPaymentProvider()
) {
    val ledgerService: LedgerService = LedgerService(db)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    init {
        // Automatically login default user for seamless interactive testing
        scope.launch {
            val defaultUser = db.userDao().getUserByEmail("samuel.okafor@example.ng")
            _currentUser.value = defaultUser
        }
    }

    // AUTH & USERS
    suspend fun login(email: String, role: UserRole? = null): Boolean = withContext(Dispatchers.IO) {
        val user = db.userDao().getUserByEmail(email.trim().lowercase())
        if (user != null) {
            _currentUser.value = user
            logAudit(user.email, user.role.name, "USER_LOGIN", "User", user.id, "Logged into CardCeeza")
            return@withContext true
        }
        false
    }

    suspend fun switchDemoRole(role: UserRole) = withContext(Dispatchers.IO) {
        val targetEmail = when (role) {
            UserRole.USER -> "samuel.okafor@example.ng"
            UserRole.VERIFIER -> "verifier@cardceeza.com"
            UserRole.FINANCE -> "finance@cardceeza.com"
            UserRole.ADMIN -> "admin@cardceeza.com"
            UserRole.SUPER_ADMIN -> "superadmin@cardceeza.com"
            UserRole.SUPPORT -> "admin@cardceeza.com"
        }
        val user = db.userDao().getUserByEmail(targetEmail)
        if (user != null) {
            _currentUser.value = user
            logAudit(user.email, user.role.name, "DEMO_ROLE_SWITCHED", "User", user.id, "Switched role to ${role.name}")
        }
    }

    suspend fun registerUser(
        firstName: String,
        lastName: String,
        email: String,
        phone: String
    ): Boolean = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val existing = db.userDao().getUserByEmail(cleanEmail)
        if (existing != null) return@withContext false

        val newUser = UserEntity(
            id = "user_${UUID.randomUUID().toString().take(8)}",
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            email = cleanEmail,
            phone = phone.trim(),
            passwordHash = "demo_hash_${System.currentTimeMillis()}",
            role = UserRole.USER,
            kycStatus = KycStatus.KYC_VERIFIED,
            bvnOrNinMasked = "22${(1000..9999).random()}****${(100..999).random()}"
        )
        db.userDao().insertUser(newUser)
        _currentUser.value = newUser

        // Send welcome notification
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = newUser.id,
                title = "Welcome to CardCeeza! 🇳🇬",
                message = "Your CardCeeza account is ready. Add your Nigerian bank account to start trading gift cards for NGN.",
                type = "SYSTEM"
            )
        )
        logAudit(newUser.email, "USER", "USER_REGISTERED", "User", newUser.id, "Created new CardCeeza user account")
        true
    }

    fun logout() {
        _currentUser.value = null
    }

    // GIFT CARDS & RATES
    fun getActiveGiftCards(): Flow<List<GiftCardEntity>> = db.giftCardDao().getActiveGiftCards()
    fun getAllGiftCards(): Flow<List<GiftCardEntity>> = db.giftCardDao().getAllGiftCards()
    fun getActiveRates(): Flow<List<RateEntity>> = db.rateDao().getActiveRates()
    fun getAllRates(): Flow<List<RateEntity>> = db.rateDao().getAllRates()

    suspend fun calculateEstimatedPayout(cardId: String, region: String, cardValue: Double): Pair<RateEntity?, Double> {
        val rate = db.rateDao().getRateByCardAndRegion(cardId, region) ?: db.rateDao().getActiveRates().firstOrNull()?.firstOrNull { it.cardId == cardId }
        if (rate == null) return Pair(null, 0.0)
        val gross = cardValue * rate.ratePerUnit
        val fee = rate.fee + (gross * (rate.feePercentage / 100.0))
        val net = (gross - fee).coerceAtLeast(0.0)
        return Pair(rate, net)
    }

    // TRADES
    fun getUserTrades(userId: String): Flow<List<TradeEntity>> = db.tradeDao().getTradesForUser(userId)
    fun getAllTrades(): Flow<List<TradeEntity>> = db.tradeDao().getAllTrades()
    suspend fun getTradeById(tradeId: String): TradeEntity? = db.tradeDao().getTradeById(tradeId)
    fun getTradeFlow(tradeId: String): Flow<TradeEntity?> = db.tradeDao().getTradeFlowById(tradeId)
    fun getTradeEvents(tradeId: String): Flow<List<TradeEventEntity>> = db.tradeEventDao().getEventsForTrade(tradeId)
    fun getVerificationQueue(): Flow<List<TradeEntity>> = db.tradeDao().getPendingVerificationQueue()
    fun getPayoutQueue(): Flow<List<TradeEntity>> = db.tradeDao().getPendingPayoutsQueue()

    suspend fun submitTrade(
        userId: String,
        cardId: String,
        region: String,
        cardValue: Double,
        eCodeOrPin: String,
        evidenceUri: String
    ): TradeEntity = withContext(Dispatchers.IO) {
        val user = db.userDao().getUserById(userId) ?: _currentUser.value!!
        val card = db.giftCardDao().getGiftCardById(cardId)
        val cardName = card?.name ?: "Gift Card"
        val currency = when {
            region.contains("UK", ignoreCase = true) || region.contains("United Kingdom", ignoreCase = true) -> "GBP"
            region.contains("CA", ignoreCase = true) || region.contains("Canada", ignoreCase = true) -> "CAD"
            region.contains("EU", ignoreCase = true) || region.contains("Europe", ignoreCase = true) -> "EUR"
            else -> "USD"
        }

        val rate = db.rateDao().getRateByCardAndRegion(cardId, region)
            ?: db.rateDao().getActiveRates().firstOrNull()?.firstOrNull { it.cardId == cardId }
        val appliedRate = rate?.ratePerUnit ?: 1400.0
        val grossNgn = cardValue * appliedRate
        val feeNgn = rate?.fee ?: 0.0
        val netPayoutNgn = (grossNgn - feeNgn).coerceAtLeast(0.0)

        // Get user's default bank account
        val defaultBank = db.bankAccountDao().getDefaultBankAccount(userId)
        val bankName = defaultBank?.bankName ?: "Guaranty Trust Bank (GTBank)"
        val bankNumMasked = defaultBank?.accountNumber?.let { "**** " + it.takeLast(4) } ?: "**** 4567"
        val bankAccountName = defaultBank?.accountName ?: "${user.firstName} ${user.lastName}".uppercase()

        // Risk analysis
        val userTradesCount = db.tradeDao().getTradesForUser(userId).firstOrNull()?.size ?: 0
        val risk = RiskEngine.analyzeTrade(
            cardValue = cardValue,
            userCompletedTradesCount = userTradesCount,
            eCode = eCodeOrPin,
            hasEvidence = evidenceUri.isNotBlank() || eCodeOrPin.isNotBlank(),
            isKycVerified = user.kycStatus == KycStatus.KYC_VERIFIED
        )

        val randomTradeNum = "CCZ-2026-${(100000..999999).random()}"
        val tradeId = "trade_${UUID.randomUUID().toString().take(8)}"

        val trade = TradeEntity(
            id = tradeId,
            tradeNumber = randomTradeNum,
            userId = userId,
            userEmail = user.email,
            userName = "${user.firstName} ${user.lastName}",
            cardId = cardId,
            cardName = cardName,
            region = region,
            currency = currency,
            cardValue = cardValue,
            appliedRate = appliedRate,
            grossNgn = grossNgn,
            feeNgn = feeNgn,
            netPayoutNgn = netPayoutNgn,
            status = TradeStatus.SUBMITTED,
            riskScore = risk.score,
            riskLevel = risk.level,
            riskFlags = risk.flags.joinToString("; "),
            eCodeOrPin = eCodeOrPin,
            evidenceUri = evidenceUri,
            payoutBankName = bankName,
            payoutAccountNumberMasked = bankNumMasked,
            payoutAccountName = bankAccountName,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        db.tradeDao().insertTrade(trade)

        // Event log
        db.tradeEventDao().insertTradeEvent(
            TradeEventEntity(
                id = UUID.randomUUID().toString(),
                tradeId = trade.id,
                fromStatus = TradeStatus.DRAFT,
                toStatus = TradeStatus.SUBMITTED,
                actorRole = "USER",
                actorName = "${user.firstName} ${user.lastName}",
                note = "Trade submitted for verification. Denomination: $currency $cardValue."
            )
        )

        // Notification
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Trade ${trade.tradeNumber} Submitted",
                message = "Your $cardName ($currency $cardValue) trade was received and queued for verifier review.",
                type = "TRADE_UPDATE",
                tradeId = trade.id
            )
        )

        logAudit(user.email, "USER", "TRADE_SUBMITTED", "Trade", trade.id, "Trade ${trade.tradeNumber} created: $currency $cardValue -> ₦${"%,.2f".format(netPayoutNgn)}")
        trade
    }

    suspend fun updateTradeStatusByVerifier(
        tradeId: String,
        newStatus: TradeStatus,
        verifierNote: String,
        rejectionReason: String = ""
    ) = withContext(Dispatchers.IO) {
        val trade = db.tradeDao().getTradeById(tradeId) ?: return@withContext
        val oldStatus = trade.status
        val actor = _currentUser.value
        val actorName = if (actor != null) "${actor.firstName} ${actor.lastName}" else "Verifier"

        val updatedTrade = trade.copy(
            status = newStatus,
            verifierNotes = verifierNote,
            rejectionReason = rejectionReason,
            verifierId = actor?.id ?: "verifier_001",
            updatedAt = System.currentTimeMillis()
        )
        db.tradeDao().updateTrade(updatedTrade)

        db.tradeEventDao().insertTradeEvent(
            TradeEventEntity(
                id = UUID.randomUUID().toString(),
                tradeId = tradeId,
                fromStatus = oldStatus,
                toStatus = newStatus,
                actorRole = actor?.role?.name ?: "VERIFIER",
                actorName = actorName,
                note = verifierNote.ifBlank { "Status moved to ${newStatus.label}" }
            )
        )

        val notifTitle = when (newStatus) {
            TradeStatus.UNDER_REVIEW -> "Trade Under Review"
            TradeStatus.VERIFIED -> "Trade Verified ✅"
            TradeStatus.APPROVED -> "Trade Approved! ₦ Payout Processing"
            TradeStatus.REJECTED -> "Trade Rejected"
            TradeStatus.VERIFICATION_REQUIRED -> "Additional Verification Required"
            else -> "Trade Status Update"
        }
        val notifMsg = when (newStatus) {
            TradeStatus.APPROVED -> "Trade ${trade.tradeNumber} was approved for ₦${"%,.2f".format(trade.netPayoutNgn)}. Payout is scheduled."
            TradeStatus.REJECTED -> "Trade ${trade.tradeNumber} was rejected. Reason: ${rejectionReason.ifBlank { "Card invalid/already redeemed" }}"
            else -> "Trade ${trade.tradeNumber} status is now ${newStatus.label}."
        }

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = trade.userId,
                title = notifTitle,
                message = notifMsg,
                type = "TRADE_UPDATE",
                tradeId = trade.id
            )
        )

        logAudit(actor?.email ?: "verifier@cardceeza.com", actor?.role?.name ?: "VERIFIER", "TRADE_STATUS_UPDATED", "Trade", trade.id, "Trade ${trade.tradeNumber} moved from ${oldStatus.name} to ${newStatus.name}")
    }

    suspend fun processPayoutForTrade(tradeId: String) = withContext(Dispatchers.IO) {
        val trade = db.tradeDao().getTradeById(tradeId) ?: return@withContext
        val actor = _currentUser.value

        // Execute payment via Nigerian provider abstraction
        val payoutResult = paymentProvider.processNgnPayout(
            accountNumber = trade.payoutAccountNumberMasked,
            bankCode = "058",
            amountNgn = trade.netPayoutNgn,
            tradeNumber = trade.tradeNumber,
            recipientName = trade.payoutAccountName
        )

        val updatedTrade = trade.copy(
            status = TradeStatus.PAID,
            payoutReference = payoutResult.reference,
            updatedAt = System.currentTimeMillis()
        )
        db.tradeDao().updateTrade(updatedTrade)

        // Add event
        db.tradeEventDao().insertTradeEvent(
            TradeEventEntity(
                id = UUID.randomUUID().toString(),
                tradeId = tradeId,
                fromStatus = TradeStatus.APPROVED,
                toStatus = TradeStatus.PAID,
                actorRole = actor?.role?.name ?: "FINANCE",
                actorName = if (actor != null) "${actor.firstName} ${actor.lastName}" else "Finance Officer",
                note = "Payout ₦${"%,.2f".format(trade.netPayoutNgn)} successfully settled. Ref: ${payoutResult.reference}"
            )
        )

        // Write immutable ledger entry using atomic LedgerService with idempotency
        val creditResult = ledgerService.recordTradeCredit(
            userId = trade.userId,
            tradeId = trade.id,
            amount = trade.netPayoutNgn,
            referenceNumber = payoutResult.reference,
            idempotencyKey = "idemp_${trade.id}_settlement",
            description = "Settlement for Trade ${trade.tradeNumber} (${trade.cardName} ${trade.currency} ${trade.cardValue})",
            actorEmail = actor?.email ?: "finance@cardceeza.com"
        )

        // Notify user
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = trade.userId,
                title = "Payout Settled! ₦${"%,.2f".format(trade.netPayoutNgn)} 🎉",
                message = "Trade ${trade.tradeNumber} payment has been credited to your ${trade.payoutBankName} account.",
                type = "PAYOUT_SUCCESS",
                tradeId = trade.id
            )
        )

        logAudit(actor?.email ?: "finance@cardceeza.com", actor?.role?.name ?: "FINANCE", "PAYOUT_COMPLETED", "Trade", trade.id, "Settled ₦${"%,.2f".format(trade.netPayoutNgn)} for ${trade.tradeNumber}. Ref: ${payoutResult.reference}")
    }

    // LEDGER & WALLET
    fun getUserLedger(userId: String): Flow<List<LedgerEntryEntity>> = db.ledgerDao().getLedgerEntriesForUser(userId)
    fun getUserBalance(userId: String): Flow<Double> = db.ledgerDao().getUserBalance(userId)

    suspend fun requestWithdrawal(userId: String, amountNgn: Double, bankAccountId: String): Boolean = withContext(Dispatchers.IO) {
        val user = _currentUser.value
        val bankAccount = db.bankAccountDao().getDefaultBankAccount(userId)
        val bankName = bankAccount?.bankName ?: "Nigerian Bank"
        val ref = "WTH-NGN-${System.currentTimeMillis()}"
        val idempKey = "idemp_withdraw_${userId}_${System.currentTimeMillis()}"

        val debitResult = ledgerService.recordWithdrawalDebit(
            userId = userId,
            amount = amountNgn,
            referenceNumber = ref,
            idempotencyKey = idempKey,
            description = "Withdrawal to $bankName (${bankAccount?.accountNumber?.takeLast(4) ?: "XXXX"})",
            actorEmail = user?.email ?: "user@cardceeza.ng"
        )

        if (debitResult !is com.example.cardceeza.data.service.LedgerOperationResult.Success) {
            return@withContext false
        }

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Withdrawal Initiated ₦${"%,.2f".format(amountNgn)}",
                message = "Your transfer to $bankName has been processed. Reference: $ref",
                type = "PAYOUT_SUCCESS"
            )
        )
        true
    }

    // BANK ACCOUNTS
    fun getUserBankAccounts(userId: String): Flow<List<BankAccountEntity>> = db.bankAccountDao().getBankAccountsForUser(userId)
    suspend fun getNigerianBanks(): List<BankInfo> = paymentProvider.getNigerianBanks()
    suspend fun verifyNigerianAccount(bankCode: String, accountNumber: String): AccountVerificationResult =
        paymentProvider.verifyNigerianBankAccount(bankCode, accountNumber)

    suspend fun addBankAccount(
        userId: String,
        bankName: String,
        bankCode: String,
        accountNumber: String,
        accountName: String,
        isDefault: Boolean
    ) = withContext(Dispatchers.IO) {
        if (isDefault) {
            db.bankAccountDao().clearDefaultFlags(userId)
        }
        val account = BankAccountEntity(
            id = "bank_${UUID.randomUUID().toString().take(8)}",
            userId = userId,
            bankName = bankName,
            bankCode = bankCode,
            accountNumber = accountNumber,
            accountName = accountName,
            isDefault = isDefault,
            isVerified = true
        )
        db.bankAccountDao().insertBankAccount(account)
        val user = _currentUser.value
        logAudit(user?.email ?: "user@cardceeza.ng", "USER", "BANK_ACCOUNT_ADDED", "BankAccount", account.id, "Added $bankName account ($accountNumber)")
    }

    suspend fun deleteBankAccount(id: String) = withContext(Dispatchers.IO) {
        db.bankAccountDao().deleteBankAccount(id)
    }

    // NOTIFICATIONS
    fun getUserNotifications(userId: String): Flow<List<NotificationEntity>> = db.notificationDao().getNotificationsForUser(userId)
    fun getUnreadNotificationCount(userId: String): Flow<Int> = db.notificationDao().getUnreadCount(userId)
    suspend fun markNotificationRead(id: String) = withContext(Dispatchers.IO) { db.notificationDao().markAsRead(id) }
    suspend fun markAllNotificationsRead(userId: String) = withContext(Dispatchers.IO) { db.notificationDao().markAllAsRead(userId) }

    // SUPPORT
    fun getUserTickets(userId: String): Flow<List<SupportTicketEntity>> = db.supportDao().getTicketsForUser(userId)
    fun getAllTickets(): Flow<List<SupportTicketEntity>> = db.supportDao().getAllTickets()
    fun getTicketFlow(ticketId: String): Flow<SupportTicketEntity?> = db.supportDao().getTicketFlowById(ticketId)

    suspend fun createSupportTicket(
        userId: String,
        subject: String,
        category: TicketCategory,
        initialMessage: String,
        relatedTradeId: String = ""
    ): SupportTicketEntity = withContext(Dispatchers.IO) {
        val user = db.userDao().getUserById(userId) ?: _currentUser.value!!
        val ticketId = "ticket_${UUID.randomUUID().toString().take(8)}"
        val ticketNumber = "TCK-${(10000..99999).random()}"

        val messageJson = """[{"sender":"user","senderName":"${user.firstName} ${user.lastName}","text":"${initialMessage.replace("\"", "\\\"")}","time":${System.currentTimeMillis()}}]"""

        val ticket = SupportTicketEntity(
            id = ticketId,
            ticketNumber = ticketNumber,
            userId = userId,
            userEmail = user.email,
            userName = "${user.firstName} ${user.lastName}",
            subject = subject,
            category = category,
            priority = TicketPriority.MEDIUM,
            status = TicketStatus.OPEN,
            messagesJson = messageJson,
            relatedTradeId = relatedTradeId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        db.supportDao().insertTicket(ticket)
        logAudit(user.email, "USER", "SUPPORT_TICKET_CREATED", "SupportTicket", ticket.id, "Ticket $ticketNumber created: $subject")
        ticket
    }

    suspend fun sendSupportMessage(ticketId: String, messageText: String, isSupportStaff: Boolean) = withContext(Dispatchers.IO) {
        val ticket = db.supportDao().getTicketById(ticketId) ?: return@withContext
        val actor = _currentUser.value
        val sender = if (isSupportStaff) "support" else "user"
        val senderName = if (isSupportStaff) "CardCeeza Support (${actor?.firstName ?: "Agent"})" else (actor?.firstName ?: "User")

        val currentJson = ticket.messagesJson
        val newMsg = """{"sender":"$sender","senderName":"$senderName","text":"${messageText.replace("\"", "\\\"")}","time":${System.currentTimeMillis()}}"""
        val updatedJson = if (currentJson.endsWith("]")) {
            currentJson.dropLast(1) + (if (currentJson.length > 2) "," else "") + newMsg + "]"
        } else {
            "[$newMsg]"
        }

        val updatedTicket = ticket.copy(
            messagesJson = updatedJson,
            status = if (isSupportStaff) TicketStatus.IN_PROGRESS else TicketStatus.WAITING_FOR_USER,
            updatedAt = System.currentTimeMillis()
        )
        db.supportDao().insertTicket(updatedTicket)
    }

    // ADMIN RATE MANAGEMENT
    suspend fun updateRate(rateId: String, newRatePerUnit: Double, newFee: Double) = withContext(Dispatchers.IO) {
        val existing = db.rateDao().getRateById(rateId) ?: return@withContext
        val previousRate = existing.ratePerUnit
        val shiftPct = if (previousRate > 0) ((newRatePerUnit - previousRate) / previousRate) * 100.0 else 0.0

        val updated = existing.copy(
            ratePerUnit = newRatePerUnit,
            fee = newFee,
            lastShiftPercentage = shiftPct,
            updatedAt = System.currentTimeMillis()
        )
        db.rateDao().updateRate(updated)

        val actor = _currentUser.value
        logAudit(actor?.email ?: "admin@cardceeza.com", "ADMIN", "RATE_UPDATED", "Rate", rateId, "${existing.cardName} (${existing.region}) rate changed from ₦$previousRate to ₦$newRatePerUnit (${"%.1f".format(shiftPct)}%)")
    }

    suspend fun addNewRate(
        cardId: String,
        cardName: String,
        region: String,
        currency: String,
        ratePerUnit: Double,
        fee: Double
    ) = withContext(Dispatchers.IO) {
        val newRate = RateEntity(
            id = "rate_${UUID.randomUUID().toString().take(8)}",
            cardId = cardId,
            cardName = cardName,
            region = region,
            currency = currency,
            ratePerUnit = ratePerUnit,
            fee = fee
        )
        db.rateDao().insertRate(newRate)
        val actor = _currentUser.value
        logAudit(actor?.email ?: "admin@cardceeza.com", "ADMIN", "RATE_CREATED", "Rate", newRate.id, "Added new rate for $cardName ($region): ₦$ratePerUnit")
    }

    // AUDIT LOGS
    fun getRecentAuditLogs(): Flow<List<AuditLogEntity>> = db.auditDao().getRecentAuditLogs()

    private suspend fun logAudit(
        actorEmail: String,
        actorRole: String,
        action: String,
        entity: String,
        entityId: String,
        details: String
    ) {
        db.auditDao().insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                actorEmail = actorEmail,
                actorRole = actorRole,
                action = action,
                entity = entity,
                entityId = entityId,
                details = details,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // KPI METRICS
    fun getTotalUsersCount(): Flow<Int> = db.userDao().getUserCount()
    fun getTotalTradesCount(): Flow<Int> = db.tradeDao().getTotalTradesCount()
    fun getTotalPaidVolumeNgn(): Flow<Double?> = db.tradeDao().getTotalPaidVolumeNgn()
}
