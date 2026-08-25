package com.example.cardceeza.data.local

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
import com.example.cardceeza.model.KycStatus
import com.example.cardceeza.model.LedgerType
import com.example.cardceeza.model.RiskLevel
import com.example.cardceeza.model.TicketCategory
import com.example.cardceeza.model.TicketPriority
import com.example.cardceeza.model.TicketStatus
import com.example.cardceeza.model.TradeStatus
import com.example.cardceeza.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseInitializer {

    suspend fun populateInitialDataIfEmpty(db: CardCeezaDatabase) = withContext(Dispatchers.IO) {
        val existingUser = db.userDao().getUserByEmail("samuel.okafor@example.ng")
        if (existingUser != null) return@withContext

        // 1. Seed Users
        val users = listOf(
            UserEntity(
                id = "user_samuel_001",
                firstName = "Samuel",
                lastName = "Okafor",
                email = "samuel.okafor@example.ng",
                phone = "+234 803 456 7890",
                passwordHash = "hash_demo_123",
                role = UserRole.USER,
                kycStatus = KycStatus.KYC_VERIFIED,
                bvnOrNinMasked = "2234****890",
                twoFactorEnabled = true
            ),
            UserEntity(
                id = "user_verifier_001",
                firstName = "Chinaza",
                lastName = "Verifier",
                email = "verifier@cardceeza.com",
                phone = "+234 802 111 2233",
                passwordHash = "hash_demo_123",
                role = UserRole.VERIFIER,
                kycStatus = KycStatus.KYC_VERIFIED
            ),
            UserEntity(
                id = "user_finance_001",
                firstName = "Tunde",
                lastName = "Finance",
                email = "finance@cardceeza.com",
                phone = "+234 805 333 4455",
                passwordHash = "hash_demo_123",
                role = UserRole.FINANCE,
                kycStatus = KycStatus.KYC_VERIFIED
            ),
            UserEntity(
                id = "user_admin_001",
                firstName = "Emeka",
                lastName = "Administrator",
                email = "admin@cardceeza.com",
                phone = "+234 809 777 8899",
                passwordHash = "hash_demo_123",
                role = UserRole.ADMIN,
                kycStatus = KycStatus.KYC_VERIFIED
            ),
            UserEntity(
                id = "user_superadmin_001",
                firstName = "CardCeeza",
                lastName = "Root",
                email = "superadmin@cardceeza.com",
                phone = "+234 800 000 0001",
                passwordHash = "hash_demo_123",
                role = UserRole.SUPER_ADMIN,
                kycStatus = KycStatus.KYC_VERIFIED
            )
        )
        for (u in users) db.userDao().insertUser(u)

        // 2. Bank Accounts
        db.bankAccountDao().insertBankAccount(
            BankAccountEntity(
                id = "bank_acc_001",
                userId = "user_samuel_001",
                bankName = "Guaranty Trust Bank (GTBank)",
                bankCode = "058",
                accountNumber = "0123456789",
                accountName = "SAMUEL CHUKWUDI OKAFOR",
                isDefault = true,
                isVerified = true
            )
        )
        db.bankAccountDao().insertBankAccount(
            BankAccountEntity(
                id = "bank_acc_002",
                userId = "user_samuel_001",
                bankName = "Kuda Bank",
                bankCode = "090267",
                accountNumber = "2001928374",
                accountName = "SAMUEL OKAFOR",
                isDefault = false,
                isVerified = true
            )
        )

        // 3. Gift Card Types
        val giftCards = listOf(
            GiftCardEntity(
                id = "gc_apple",
                name = "Apple & iTunes",
                brand = "Apple Inc.",
                slug = "apple-itunes",
                category = "Tech & Media",
                country = "US, UK, CA",
                currency = "USD, GBP, CAD",
                active = true,
                iconName = "apple",
                minDenomination = 25.0,
                maxDenomination = 1000.0,
                verificationMethod = "E-Code / Physical Card with Cash Receipt",
                description = "High liquidity card for App Store, iTunes, Apple Store hardware & digital purchases."
            ),
            GiftCardEntity(
                id = "gc_amazon",
                name = "Amazon Gift Card",
                brand = "Amazon.com",
                slug = "amazon",
                category = "E-Commerce",
                country = "US, UK, CA, DE",
                currency = "USD, GBP, CAD, EUR",
                active = true,
                iconName = "shopping_cart",
                minDenomination = 25.0,
                maxDenomination = 2000.0,
                verificationMethod = "Claim Code + Cash / Debit Receipt",
                description = "Leading retail marketplace card. Accepts physical with receipt or Amazon digital claim codes."
            ),
            GiftCardEntity(
                id = "gc_steam",
                name = "Steam Wallet Card",
                brand = "Valve Corporation",
                slug = "steam",
                category = "Gaming",
                country = "US, UK, EU, CA",
                currency = "USD, GBP, EUR, CAD",
                active = true,
                iconName = "sports_esports",
                minDenomination = 20.0,
                maxDenomination = 500.0,
                verificationMethod = "Wallet Code / Physical Card Photo",
                description = "Instant gaming credit for Steam PC games, in-game items, and community marketplace."
            ),
            GiftCardEntity(
                id = "gc_google_play",
                name = "Google Play",
                brand = "Google LLC",
                slug = "google-play",
                category = "Apps & Digital",
                country = "US, UK, CA",
                currency = "USD, GBP, CAD",
                active = true,
                iconName = "play_arrow",
                minDenomination = 10.0,
                maxDenomination = 500.0,
                verificationMethod = "16-digit Code + Receipt",
                description = "Redeemable for Android apps, subscriptions, games, and Play Store books/movies."
            ),
            GiftCardEntity(
                id = "gc_xbox",
                name = "Xbox & Microsoft",
                brand = "Microsoft Corp",
                slug = "xbox",
                category = "Gaming",
                country = "US, UK",
                currency = "USD, GBP",
                active = true,
                iconName = "videogame_asset",
                minDenomination = 25.0,
                maxDenomination = 500.0,
                verificationMethod = "25-character Code",
                description = "Digital code for Xbox Game Pass, console titles, and Microsoft digital store."
            ),
            GiftCardEntity(
                id = "gc_playstation",
                name = "PlayStation Network",
                brand = "Sony Interactive Entertainment",
                slug = "playstation",
                category = "Gaming",
                country = "US, UK",
                currency = "USD, GBP",
                active = true,
                iconName = "gamepad",
                minDenomination = 25.0,
                maxDenomination = 250.0,
                verificationMethod = "12-digit Voucher Code",
                description = "PlayStation Store credits for PS5 & PS4 downloads, PS Plus memberships, and add-ons."
            ),
            GiftCardEntity(
                id = "gc_nike",
                name = "Nike Gift Card",
                brand = "Nike",
                slug = "nike",
                category = "Fashion & Apparel",
                country = "US",
                currency = "USD",
                active = true,
                iconName = "checkroom",
                minDenomination = 50.0,
                maxDenomination = 1000.0,
                verificationMethod = "Card Number + PIN",
                description = "Valid for official Nike.com purchases, SNKRS app, and Nike retail stores."
            ),
            GiftCardEntity(
                id = "gc_sephora",
                name = "Sephora Gift Card",
                brand = "Sephora",
                slug = "sephora",
                category = "Beauty & Cosmetics",
                country = "US, CA",
                currency = "USD, CAD",
                active = true,
                iconName = "brush",
                minDenomination = 25.0,
                maxDenomination = 500.0,
                verificationMethod = "16-digit Card + 8-digit PIN",
                description = "Premium cosmetics, fragrance, skincare, and beauty products."
            ),
            GiftCardEntity(
                id = "gc_walmart",
                name = "Walmart Gift Card",
                brand = "Walmart Inc.",
                slug = "walmart",
                category = "Retail & Groceries",
                country = "US, CA",
                currency = "USD, CAD",
                active = true,
                iconName = "storefront",
                minDenomination = 50.0,
                maxDenomination = 1000.0,
                verificationMethod = "Card Number + 4-digit PIN",
                description = "General retail merchandise, electronics, and groceries at Walmart."
            ),
            GiftCardEntity(
                id = "gc_razer_gold",
                name = "Razer Gold",
                brand = "Razer Inc.",
                slug = "razer-gold",
                category = "Gaming & Virtual Credits",
                country = "Global / US",
                currency = "USD",
                active = true,
                iconName = "bolt",
                minDenomination = 20.0,
                maxDenomination = 1000.0,
                verificationMethod = "PIN + Serial Number",
                description = "Unified virtual credits for gamers worldwide to recharge games and in-game content."
            )
        )
        db.giftCardDao().insertGiftCards(giftCards)

        // 4. Rates
        val rates = listOf(
            RateEntity(
                id = "rate_apple_us",
                cardId = "gc_apple",
                cardName = "Apple & iTunes",
                region = "United States (US)",
                currency = "USD",
                ratePerUnit = 1430.0,
                minimumValue = 25.0,
                maximumValue = 1000.0,
                fee = 0.0,
                lastShiftPercentage = 2.1
            ),
            RateEntity(
                id = "rate_apple_uk",
                cardId = "gc_apple",
                cardName = "Apple & iTunes",
                region = "United Kingdom (UK)",
                currency = "GBP",
                ratePerUnit = 1860.0,
                minimumValue = 25.0,
                maximumValue = 1000.0,
                fee = 0.0,
                lastShiftPercentage = 1.4
            ),
            RateEntity(
                id = "rate_apple_ca",
                cardId = "gc_apple",
                cardName = "Apple & iTunes",
                region = "Canada (CA)",
                currency = "CAD",
                ratePerUnit = 1060.0,
                minimumValue = 25.0,
                maximumValue = 1000.0,
                fee = 0.0,
                lastShiftPercentage = 0.8
            ),
            RateEntity(
                id = "rate_amazon_us",
                cardId = "gc_amazon",
                cardName = "Amazon Gift Card",
                region = "United States (US)",
                currency = "USD",
                ratePerUnit = 1390.0,
                minimumValue = 25.0,
                maximumValue = 2000.0,
                fee = 0.0,
                lastShiftPercentage = -0.5
            ),
            RateEntity(
                id = "rate_amazon_uk",
                cardId = "gc_amazon",
                cardName = "Amazon Gift Card",
                region = "United Kingdom (UK)",
                currency = "GBP",
                ratePerUnit = 1810.0,
                minimumValue = 25.0,
                maximumValue = 2000.0,
                fee = 0.0,
                lastShiftPercentage = 1.1
            ),
            RateEntity(
                id = "rate_steam_us",
                cardId = "gc_steam",
                cardName = "Steam Wallet Card",
                region = "United States (US)",
                currency = "USD",
                ratePerUnit = 1460.0,
                minimumValue = 20.0,
                maximumValue = 500.0,
                fee = 0.0,
                lastShiftPercentage = 3.2
            ),
            RateEntity(
                id = "rate_steam_uk",
                cardId = "gc_steam",
                cardName = "Steam Wallet Card",
                region = "United Kingdom (UK)",
                currency = "GBP",
                ratePerUnit = 1890.0,
                minimumValue = 20.0,
                maximumValue = 500.0,
                fee = 0.0,
                lastShiftPercentage = 1.9
            ),
            RateEntity(
                id = "rate_google_play_us",
                cardId = "gc_google_play",
                cardName = "Google Play",
                region = "United States (US)",
                currency = "USD",
                ratePerUnit = 1370.0,
                minimumValue = 10.0,
                maximumValue = 500.0,
                fee = 0.0,
                lastShiftPercentage = 0.0
            ),
            RateEntity(
                id = "rate_xbox_us",
                cardId = "gc_xbox",
                cardName = "Xbox & Microsoft",
                region = "United States (US)",
                currency = "USD",
                ratePerUnit = 1330.0,
                minimumValue = 25.0,
                maximumValue = 500.0,
                fee = 0.0,
                lastShiftPercentage = -1.2
            ),
            RateEntity(
                id = "rate_playstation_us",
                cardId = "gc_playstation",
                cardName = "PlayStation Network",
                region = "United States (US)",
                currency = "USD",
                ratePerUnit = 1350.0,
                minimumValue = 25.0,
                maximumValue = 250.0,
                fee = 0.0,
                lastShiftPercentage = 0.5
            ),
            RateEntity(
                id = "rate_nike_us",
                cardId = "gc_nike",
                cardName = "Nike Gift Card",
                region = "United States (US)",
                currency = "USD",
                ratePerUnit = 1310.0,
                minimumValue = 50.0,
                maximumValue = 1000.0,
                fee = 0.0,
                lastShiftPercentage = 0.0
            ),
            RateEntity(
                id = "rate_sephora_us",
                cardId = "gc_sephora",
                cardName = "Sephora Gift Card",
                region = "United States (US)",
                currency = "USD",
                ratePerUnit = 1290.0,
                minimumValue = 25.0,
                maximumValue = 500.0,
                fee = 0.0,
                lastShiftPercentage = 0.3
            ),
            RateEntity(
                id = "rate_walmart_us",
                cardId = "gc_walmart",
                cardName = "Walmart Gift Card",
                region = "United States (US)",
                currency = "USD",
                ratePerUnit = 1300.0,
                minimumValue = 50.0,
                maximumValue = 1000.0,
                fee = 0.0,
                lastShiftPercentage = 0.0
            ),
            RateEntity(
                id = "rate_razer_gold_us",
                cardId = "gc_razer_gold",
                cardName = "Razer Gold",
                region = "United States (US)",
                currency = "USD",
                ratePerUnit = 1470.0,
                minimumValue = 20.0,
                maximumValue = 1000.0,
                fee = 0.0,
                lastShiftPercentage = 2.8
            )
        )
        db.rateDao().insertRates(rates)

        // 5. Seed Historical Trade 1: Fully Paid
        val trade1 = TradeEntity(
            id = "trade_ccz_001201",
            tradeNumber = "CCZ-2026-000124",
            userId = "user_samuel_001",
            userEmail = "samuel.okafor@example.ng",
            userName = "Samuel Okafor",
            cardId = "gc_apple",
            cardName = "Apple & iTunes",
            region = "United States (US)",
            currency = "USD",
            cardValue = 100.0,
            appliedRate = 1430.0,
            grossNgn = 143000.0,
            feeNgn = 0.0,
            netPayoutNgn = 143000.0,
            status = TradeStatus.PAID,
            riskScore = 10,
            riskLevel = RiskLevel.LOW,
            riskFlags = "Standard trade size, verified KYC account",
            eCodeOrPin = "X7M8-9K2L-44NJ-P8QW",
            evidenceUri = "demo_evidence_apple_receipt.jpg",
            payoutBankName = "Guaranty Trust Bank (GTBank)",
            payoutAccountNumberMasked = "0123****89",
            payoutAccountName = "SAMUEL CHUKWUDI OKAFOR",
            payoutReference = "PAY-NGN-9823419082",
            verifierId = "user_verifier_001",
            verifierNotes = "Apple claim code validated with original cash purchase receipt. Legit trade.",
            createdAt = System.currentTimeMillis() - 86400000L * 2,
            updatedAt = System.currentTimeMillis() - 86400000L * 2 + 1800000L
        )
        db.tradeDao().insertTrade(trade1)

        db.tradeEventDao().insertTradeEvent(
            TradeEventEntity(
                id = "evt_001_1",
                tradeId = trade1.id,
                fromStatus = TradeStatus.DRAFT,
                toStatus = TradeStatus.SUBMITTED,
                actorRole = "USER",
                actorName = "Samuel Okafor",
                note = "Trade submitted with e-code and cash receipt upload.",
                timestamp = trade1.createdAt
            )
        )
        db.tradeEventDao().insertTradeEvent(
            TradeEventEntity(
                id = "evt_001_2",
                tradeId = trade1.id,
                fromStatus = TradeStatus.SUBMITTED,
                toStatus = TradeStatus.VERIFIED,
                actorRole = "VERIFIER",
                actorName = "Chinaza Verifier",
                note = "Card verified valid against provider system.",
                timestamp = trade1.createdAt + 600000L
            )
        )
        db.tradeEventDao().insertTradeEvent(
            TradeEventEntity(
                id = "evt_001_3",
                tradeId = trade1.id,
                fromStatus = TradeStatus.VERIFIED,
                toStatus = TradeStatus.APPROVED,
                actorRole = "VERIFIER",
                actorName = "Chinaza Verifier",
                note = "Trade approved for payout.",
                timestamp = trade1.createdAt + 900000L
            )
        )
        db.tradeEventDao().insertTradeEvent(
            TradeEventEntity(
                id = "evt_001_4",
                tradeId = trade1.id,
                fromStatus = TradeStatus.APPROVED,
                toStatus = TradeStatus.PAID,
                actorRole = "PAYMENT_PROVIDER",
                actorName = "Nigerian Interbank Settlement (Mock)",
                note = "₦143,000.00 settled successfully to GTBank 0123****89.",
                timestamp = trade1.updatedAt
            )
        )

        // Seed Ledger Entry for Trade 1
        db.ledgerDao().insertLedgerEntry(
            LedgerEntryEntity(
                id = "led_001",
                userId = "user_samuel_001",
                tradeId = trade1.id,
                referenceNumber = "TXN-2026-88910",
                type = LedgerType.TRADE_CREDIT,
                amount = 143000.0,
                balanceAfter = 143000.0,
                description = "Trade Credit for Apple $100 (CCZ-2026-000124)",
                idempotencyKey = "idemp_trade_ccz_001201_credit",
                createdAt = trade1.updatedAt
            )
        )

        // 6. Seed Active Trade 2: Under Review
        val trade2 = TradeEntity(
            id = "trade_ccz_001202",
            tradeNumber = "CCZ-2026-000125",
            userId = "user_samuel_001",
            userEmail = "samuel.okafor@example.ng",
            userName = "Samuel Okafor",
            cardId = "gc_steam",
            cardName = "Steam Wallet Card",
            region = "United States (US)",
            currency = "USD",
            cardValue = 50.0,
            appliedRate = 1460.0,
            grossNgn = 73000.0,
            feeNgn = 0.0,
            netPayoutNgn = 73000.0,
            status = TradeStatus.UNDER_REVIEW,
            riskScore = 12,
            riskLevel = RiskLevel.LOW,
            riskFlags = "Standard gaming denomination",
            eCodeOrPin = "8H9K-4NNM-22LK",
            evidenceUri = "demo_evidence_steam.jpg",
            payoutBankName = "Guaranty Trust Bank (GTBank)",
            payoutAccountNumberMasked = "0123****89",
            payoutAccountName = "SAMUEL CHUKWUDI OKAFOR",
            createdAt = System.currentTimeMillis() - 3600000L,
            updatedAt = System.currentTimeMillis() - 1800000L
        )
        db.tradeDao().insertTrade(trade2)

        db.tradeEventDao().insertTradeEvent(
            TradeEventEntity(
                id = "evt_002_1",
                tradeId = trade2.id,
                fromStatus = TradeStatus.DRAFT,
                toStatus = TradeStatus.SUBMITTED,
                actorRole = "USER",
                actorName = "Samuel Okafor",
                note = "Submitted $50 Steam Wallet Card.",
                timestamp = trade2.createdAt
            )
        )
        db.tradeEventDao().insertTradeEvent(
            TradeEventEntity(
                id = "evt_002_2",
                tradeId = trade2.id,
                fromStatus = TradeStatus.SUBMITTED,
                toStatus = TradeStatus.UNDER_REVIEW,
                actorRole = "SYSTEM",
                actorName = "Verification Engine",
                note = "Trade assigned to Verifier chinaza@cardceeza.com queue.",
                timestamp = trade2.updatedAt
            )
        )

        // 7. Seed Notifications
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_001",
                userId = "user_samuel_001",
                title = "Trade Approved & Paid! 🎉",
                message = "Trade CCZ-2026-000124 (Apple \$100) was approved and ₦143,000 was transferred to your GTBank account.",
                type = "PAYOUT_SUCCESS",
                tradeId = trade1.id,
                isRead = false,
                createdAt = trade1.updatedAt
            )
        )
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_002",
                userId = "user_samuel_001",
                title = "Trade Under Review",
                message = "Trade CCZ-2026-000125 (Steam \$50) is now undergoing security and balance verification.",
                type = "TRADE_UPDATE",
                tradeId = trade2.id,
                isRead = false,
                createdAt = trade2.updatedAt
            )
        )
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_003",
                userId = "user_samuel_001",
                title = "Welcome to CardCeeza",
                message = "Your identity profile and Nigerian payout account are ready for high-speed trading.",
                type = "SYSTEM",
                isRead = true,
                createdAt = System.currentTimeMillis() - 86400000L * 3
            )
        )

        // 8. Seed Support Ticket
        db.supportDao().insertTicket(
            SupportTicketEntity(
                id = "ticket_001",
                ticketNumber = "TCK-88219",
                userId = "user_samuel_001",
                userEmail = "samuel.okafor@example.ng",
                userName = "Samuel Okafor",
                subject = "Question regarding UK Steam Card acceptance",
                category = TicketCategory.TRADE_ISSUE,
                priority = TicketPriority.LOW,
                status = TicketStatus.RESOLVED,
                messagesJson = """[{"sender":"user","senderName":"Samuel Okafor","text":"Hello CardCeeza support, do you accept physical Steam £50 cards with activation barcode slip?","time":${System.currentTimeMillis() - 86400000L}},{"sender":"support","senderName":"Tola (CardCeeza Support)","text":"Hi Samuel! Yes, we gladly accept UK physical Steam cards. Make sure to upload both the clear card scratch panel and the retail activation receipt.","time":${System.currentTimeMillis() - 80000000L}}]""",
                createdAt = System.currentTimeMillis() - 86400000L,
                updatedAt = System.currentTimeMillis() - 80000000L
            )
        )

        // 9. Seed Audit Logs
        db.auditDao().insertAuditLog(
            AuditLogEntity(
                id = "audit_001",
                actorEmail = "system@cardceeza.com",
                actorRole = "SYSTEM",
                action = "SYSTEM_INITIALIZED",
                entity = "Platform",
                entityId = "cardceeza_v1",
                details = "CardCeeza trading rates and core gift card catalog provisioned."
            )
        )
        db.auditDao().insertAuditLog(
            AuditLogEntity(
                id = "audit_002",
                actorEmail = "verifier@cardceeza.com",
                actorRole = "VERIFIER",
                action = "TRADE_APPROVED",
                entity = "Trade",
                entityId = trade1.id,
                details = "Trade CCZ-2026-000124 approved for gross payout ₦143,000."
            )
        )
    }
}
