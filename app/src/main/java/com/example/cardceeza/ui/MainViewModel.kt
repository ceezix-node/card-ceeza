package com.example.cardceeza.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cardceeza.data.local.CardCeezaDatabase
import com.example.cardceeza.data.local.DatabaseInitializer
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
import com.example.cardceeza.data.repository.CardCeezaRepository
import com.example.cardceeza.data.service.AccountVerificationResult
import com.example.cardceeza.data.service.BankInfo
import com.example.cardceeza.data.service.PushNotificationTtsService
import com.example.cardceeza.model.TicketCategory
import com.example.cardceeza.model.TradeStatus
import com.example.cardceeza.model.UserRole
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = CardCeezaDatabase.getInstance(application)
    val repository = CardCeezaRepository(db)
    val ttsService = PushNotificationTtsService(application)

    val currentUser: StateFlow<UserEntity?> = repository.currentUser

    val activeGiftCards: StateFlow<List<GiftCardEntity>> = repository.getActiveGiftCards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGiftCards: StateFlow<List<GiftCardEntity>> = repository.getAllGiftCards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeRates: StateFlow<List<RateEntity>> = repository.getActiveRates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRates: StateFlow<List<RateEntity>> = repository.getAllRates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userTrades: StateFlow<List<TradeEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getUserTrades(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTrades: StateFlow<List<TradeEntity>> = repository.getAllTrades()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val verificationQueue: StateFlow<List<TradeEntity>> = repository.getVerificationQueue()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payoutQueue: StateFlow<List<TradeEntity>> = repository.getPayoutQueue()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userLedger: StateFlow<List<LedgerEntryEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getUserLedger(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userBalance: StateFlow<Double> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getUserBalance(user.id) else flowOf(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val userBankAccounts: StateFlow<List<BankAccountEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getUserBankAccounts(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userNotifications: StateFlow<List<NotificationEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getUserNotifications(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotifCount: StateFlow<Int> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getUnreadNotificationCount(user.id) else flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val userTickets: StateFlow<List<SupportTicketEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getUserTickets(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTickets: StateFlow<List<SupportTicketEntity>> = repository.getAllTickets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.getRecentAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val kpiUsersCount: StateFlow<Int> = repository.getTotalUsersCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val kpiTradesCount: StateFlow<Int> = repository.getTotalTradesCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val kpiPaidVolumeNgn: StateFlow<Double?> = repository.getTotalPaidVolumeNgn()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _nigerianBanks = MutableStateFlow<List<BankInfo>>(emptyList())
    val nigerianBanks: StateFlow<List<BankInfo>> = _nigerianBanks.asStateFlow()

    private val _snackMessage = MutableStateFlow<String?>(null)
    val snackMessage: StateFlow<String?> = _snackMessage.asStateFlow()

    private val _isDarkMode = MutableStateFlow<Boolean?>(null) // null = system default, true = dark, false = light
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    private val _isBiometricLockEnabled = MutableStateFlow(true)
    val isBiometricLockEnabled: StateFlow<Boolean> = _isBiometricLockEnabled.asStateFlow()

    private val _isTtsVoiceEnabled = MutableStateFlow(true)
    val isTtsVoiceEnabled: StateFlow<Boolean> = _isTtsVoiceEnabled.asStateFlow()

    init {
        viewModelScope.launch {
            DatabaseInitializer.populateInitialDataIfEmpty(db)
            _nigerianBanks.value = repository.getNigerianBanks()
        }
    }

    fun toggleDarkMode() {
        val current = _isDarkMode.value
        _isDarkMode.value = if (current == null || !current) true else false
    }

    fun setDarkMode(dark: Boolean?) {
        _isDarkMode.value = dark
    }

    fun toggleBiometricLock() {
        _isBiometricLockEnabled.value = !_isBiometricLockEnabled.value
        showSnack(if (_isBiometricLockEnabled.value) "Biometric wallet lock enabled" else "Biometric lock disabled")
    }

    fun toggleTtsVoice() {
        _isTtsVoiceEnabled.value = !_isTtsVoiceEnabled.value
        ttsService.isTtsVoiceEnabled = _isTtsVoiceEnabled.value
        showSnack(if (_isTtsVoiceEnabled.value) "Voice readout enabled for PAID & UNDER_REVIEW alerts" else "Voice readout muted")
    }

    fun showSnack(msg: String) {
        _snackMessage.value = msg
    }

    fun clearSnack() {
        _snackMessage.value = null
    }

    fun switchDemoRole(role: UserRole) {
        viewModelScope.launch {
            repository.switchDemoRole(role)
            showSnack("Switched session to ${role.name}")
        }
    }

    fun login(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.login(email)
            if (success) {
                showSnack("Logged in successfully")
            } else {
                showSnack("Account not found. Try demo accounts or register.")
            }
            onResult(success)
        }
    }

    fun register(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val success = repository.registerUser(firstName, lastName, email, phone)
            if (success) {
                showSnack("Account created! Welcome to CardCeeza.")
            } else {
                showSnack("Email already registered.")
            }
            onResult(success)
        }
    }

    fun logout() {
        repository.logout()
        showSnack("Signed out")
    }

    fun submitTrade(
        cardId: String,
        region: String,
        cardValue: Double,
        eCode: String,
        evidenceUri: String,
        onSuccess: (TradeEntity) -> Unit
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val trade = repository.submitTrade(
                userId = user.id,
                cardId = cardId,
                region = region,
                cardValue = cardValue,
                eCodeOrPin = eCode,
                evidenceUri = evidenceUri
            )
            showSnack("Trade ${trade.tradeNumber} submitted for verification!")
            onSuccess(trade)
        }
    }

    fun getTradeFlow(tradeId: String): Flow<TradeEntity?> = repository.getTradeFlow(tradeId)
    fun getTradeEvents(tradeId: String): Flow<List<TradeEventEntity>> = repository.getTradeEvents(tradeId)
    fun getTicketFlow(ticketId: String): Flow<SupportTicketEntity?> = repository.getTicketFlow(ticketId)

    fun updateTradeStatus(
        tradeId: String,
        newStatus: TradeStatus,
        note: String,
        rejectionReason: String = ""
    ) {
        viewModelScope.launch {
            val trade = repository.getTradeById(tradeId)
            repository.updateTradeStatusByVerifier(tradeId, newStatus, note, rejectionReason)
            showSnack("Trade status updated to ${newStatus.label}")
            ttsService.speakTradeStatusUpdate(
                tradeRef = trade?.tradeNumber ?: tradeId,
                newStatus = newStatus,
                payoutNgn = trade?.netPayoutNgn ?: 0.0
            )
        }
    }

    fun processPayout(tradeId: String) {
        viewModelScope.launch {
            val trade = repository.getTradeById(tradeId)
            repository.processPayoutForTrade(tradeId)
            showSnack("Payout processed and settled successfully!")
            ttsService.speakTradeStatusUpdate(
                tradeRef = trade?.tradeNumber ?: tradeId,
                newStatus = TradeStatus.PAID,
                payoutNgn = trade?.netPayoutNgn ?: 0.0
            )
        }
    }

    fun verifyBankAccount(bankCode: String, accountNumber: String, onResult: (AccountVerificationResult) -> Unit) {
        viewModelScope.launch {
            val res = repository.verifyNigerianAccount(bankCode, accountNumber)
            onResult(res)
        }
    }

    fun addBankAccount(
        bankName: String,
        bankCode: String,
        accountNumber: String,
        accountName: String,
        isDefault: Boolean
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.addBankAccount(user.id, bankName, bankCode, accountNumber, accountName, isDefault)
            showSnack("Bank account added: $bankName ($accountNumber)")
        }
    }

    fun deleteBankAccount(id: String) {
        viewModelScope.launch {
            repository.deleteBankAccount(id)
            showSnack("Bank account removed")
        }
    }

    fun requestWithdrawal(amountNgn: Double, bankAccountId: String, onSuccess: () -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val ok = repository.requestWithdrawal(user.id, amountNgn, bankAccountId)
            if (ok) {
                showSnack("Withdrawal of ₦${"%,.2f".format(amountNgn)} completed!")
                onSuccess()
            } else {
                showSnack("Withdrawal failed. Check balance.")
            }
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun markAllNotificationsRead() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.markAllNotificationsRead(user.id)
            showSnack("All notifications marked as read")
        }
    }

    fun createSupportTicket(
        subject: String,
        category: TicketCategory,
        message: String,
        relatedTradeId: String = "",
        onSuccess: (SupportTicketEntity) -> Unit
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val ticket = repository.createSupportTicket(user.id, subject, category, message, relatedTradeId)
            showSnack("Ticket ${ticket.ticketNumber} created")
            onSuccess(ticket)
        }
    }

    fun sendSupportMessage(ticketId: String, text: String, isSupportStaff: Boolean) {
        viewModelScope.launch {
            repository.sendSupportMessage(ticketId, text, isSupportStaff)
        }
    }

    fun updateRate(rateId: String, newRate: Double, newFee: Double) {
        viewModelScope.launch {
            repository.updateRate(rateId, newRate, newFee)
            showSnack("Rate updated successfully")
        }
    }

    fun addNewRate(
        cardId: String,
        cardName: String,
        region: String,
        currency: String,
        ratePerUnit: Double,
        fee: Double
    ) {
        viewModelScope.launch {
            repository.addNewRate(cardId, cardName, region, currency, ratePerUnit, fee)
            showSnack("New rate published for $cardName ($region)")
        }
    }
}
