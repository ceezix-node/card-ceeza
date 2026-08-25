package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardceeza.model.UserRole
import com.example.cardceeza.ui.MainViewModel
import com.example.cardceeza.ui.components.AddBankAccountDialog
import com.example.cardceeza.ui.components.OnboardingCarouselDialog
import com.example.cardceeza.ui.components.WithdrawalDialog
import com.example.cardceeza.ui.screens.AdminDashboardScreen
import com.example.cardceeza.ui.screens.AuthScreen
import com.example.cardceeza.ui.screens.LandingScreen
import com.example.cardceeza.ui.screens.NotificationsScreen
import com.example.cardceeza.ui.screens.ProfileAndSecurityScreen
import com.example.cardceeza.ui.screens.RatesScreen
import com.example.cardceeza.ui.screens.SellCardScreen
import com.example.cardceeza.ui.screens.SupportScreen
import com.example.cardceeza.ui.screens.TradeDetailScreen
import com.example.cardceeza.ui.screens.TradeHistoryScreen
import com.example.cardceeza.ui.screens.UserDashboardScreen
import com.example.cardceeza.ui.screens.WalletScreen
import com.example.cardceeza.ui.theme.CardCeezaTheme
import com.example.cardceeza.ui.theme.Emerald700
import com.example.cardceeza.ui.theme.Emerald800
import com.example.cardceeza.ui.theme.Emerald900
import com.example.cardceeza.ui.theme.Gold400

enum class CardCeezaScreen {
    LANDING,
    AUTH,
    DASHBOARD,
    SELL_CARD,
    RATES,
    WALLET,
    TRADE_HISTORY,
    TRADE_DETAIL,
    ADMIN,
    NOTIFICATIONS,
    SUPPORT,
    PROFILE
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val systemInDark = androidx.compose.foundation.isSystemInDarkTheme()
            val effectiveDark = isDarkMode ?: systemInDark

            CardCeezaTheme(darkTheme = effectiveDark) {
                CardCeezaApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CardCeezaApp(viewModel: MainViewModel) {
    var currentScreen by remember { mutableStateOf(CardCeezaScreen.DASHBOARD) }
    var selectedTradeId by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val snackMessage by viewModel.snackMessage.collectAsState()

    LaunchedEffect(snackMessage) {
        snackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnack()
        }
    }

    // Dialogs state
    var showAddBankDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    // Collect Reactive StateFlows from ViewModel
    val currentUser by viewModel.currentUser.collectAsState()
    val userBalance by viewModel.userBalance.collectAsState()
    val activeGiftCards by viewModel.activeGiftCards.collectAsState()
    val allGiftCards by viewModel.allGiftCards.collectAsState()
    val activeRates by viewModel.activeRates.collectAsState()
    val allRates by viewModel.allRates.collectAsState()
    val userBankAccounts by viewModel.userBankAccounts.collectAsState()
    val userTrades by viewModel.userTrades.collectAsState()
    val allTrades by viewModel.allTrades.collectAsState()
    val verificationQueue by viewModel.verificationQueue.collectAsState()
    val payoutQueue by viewModel.payoutQueue.collectAsState()
    val userLedger by viewModel.userLedger.collectAsState()
    val userNotifications by viewModel.userNotifications.collectAsState()
    val unreadNotifCount by viewModel.unreadNotifCount.collectAsState()
    val userTickets by viewModel.userTickets.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val kpiUsersCount by viewModel.kpiUsersCount.collectAsState()
    val kpiTradesCount by viewModel.kpiTradesCount.collectAsState()
    val kpiPaidVolumeNgn by viewModel.kpiPaidVolumeNgn.collectAsState()
    val nigerianBanks by viewModel.nigerianBanks.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isBiometricLockEnabled by viewModel.isBiometricLockEnabled.collectAsState()
    val isTtsVoiceEnabled by viewModel.isTtsVoiceEnabled.collectAsState()

    var showOnboardingTutorialDialog by remember { mutableStateOf(false) }

    if (showOnboardingTutorialDialog) {
        OnboardingCarouselDialog(
            onDismiss = { showOnboardingTutorialDialog = false },
            onStartTrading = {
                showOnboardingTutorialDialog = false
                currentScreen = CardCeezaScreen.SELL_CARD
            }
        )
    }

    // Dialog: Add Nigerian Bank Account
    if (showAddBankDialog) {
        AddBankAccountDialog(
            banks = nigerianBanks,
            onVerifyAccount = { code, num, cb ->
                viewModel.verifyBankAccount(code, num, cb)
            },
            onSaveAccount = { bName, bCode, acctNum, acctName, isDef ->
                viewModel.addBankAccount(bName, bCode, acctNum, acctName, isDef)
                showAddBankDialog = false
            },
            onDismiss = { showAddBankDialog = false }
        )
    }

    // Dialog: Withdraw to Nigerian Bank
    if (showWithdrawDialog) {
        WithdrawalDialog(
            availableBalanceNgn = userBalance,
            bankAccounts = userBankAccounts,
            onWithdraw = { amount, bankId ->
                viewModel.requestWithdrawal(amount, bankId) {
                    showWithdrawDialog = false
                }
            },
            onDismiss = { showWithdrawDialog = false }
        )
    }

    val showBottomBar = currentScreen in listOf(
        CardCeezaScreen.DASHBOARD,
        CardCeezaScreen.RATES,
        CardCeezaScreen.WALLET,
        CardCeezaScreen.ADMIN,
        CardCeezaScreen.PROFILE
    )

    // Handle hardware & gesture back button navigation
    BackHandler(enabled = currentScreen != CardCeezaScreen.DASHBOARD && currentScreen != CardCeezaScreen.LANDING && currentScreen != CardCeezaScreen.AUTH) {
        currentScreen = CardCeezaScreen.DASHBOARD
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Emerald700,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    NavigationBarItem(
                        selected = currentScreen == CardCeezaScreen.DASHBOARD,
                        onClick = { currentScreen = CardCeezaScreen.DASHBOARD },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Emerald800,
                            selectedTextColor = Emerald800,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == CardCeezaScreen.RATES,
                        onClick = { currentScreen = CardCeezaScreen.RATES },
                        icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Rates") },
                        label = { Text("Rates", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Emerald800,
                            selectedTextColor = Emerald800,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == CardCeezaScreen.SELL_CARD,
                        onClick = { currentScreen = CardCeezaScreen.SELL_CARD },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Emerald700),
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                Icon(Icons.Default.CurrencyExchange, contentDescription = "Sell", tint = Gold400, modifier = Modifier.size(20.dp))
                            }
                        },
                        label = { Text("Trade", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Emerald800) }
                    )

                    NavigationBarItem(
                        selected = currentScreen == CardCeezaScreen.WALLET,
                        onClick = { currentScreen = CardCeezaScreen.WALLET },
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallet") },
                        label = { Text("Wallet", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Emerald800,
                            selectedTextColor = Emerald800,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    val isAdminRole = currentUser?.role in listOf(UserRole.ADMIN, UserRole.VERIFIER, UserRole.FINANCE, UserRole.SUPER_ADMIN)

                    if (isAdminRole) {
                        NavigationBarItem(
                            selected = currentScreen == CardCeezaScreen.ADMIN,
                            onClick = { currentScreen = CardCeezaScreen.ADMIN },
                            icon = {
                                BadgedBox(badge = {
                                    if (verificationQueue.isNotEmpty()) {
                                        Badge { Text(verificationQueue.size.toString()) }
                                    }
                                }) {
                                    Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin")
                                }
                            },
                            label = { Text("Admin", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Emerald800,
                                selectedTextColor = Emerald800,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    } else {
                        NavigationBarItem(
                            selected = currentScreen == CardCeezaScreen.PROFILE,
                            onClick = { currentScreen = CardCeezaScreen.PROFILE },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profile", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Emerald800,
                                selectedTextColor = Emerald800,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { targetScreen ->
                when (targetScreen) {
                    CardCeezaScreen.LANDING -> {
                        LandingScreen(
                            giftCards = activeGiftCards,
                            rates = activeRates,
                            onStartTrading = { currentScreen = CardCeezaScreen.DASHBOARD },
                            onViewRates = { currentScreen = CardCeezaScreen.RATES },
                            onLoginClick = { currentScreen = CardCeezaScreen.AUTH }
                        )
                    }

                    CardCeezaScreen.AUTH -> {
                        AuthScreen(
                            onLogin = { email, callback ->
                                viewModel.login(email, callback)
                            },
                            onRegister = { first, last, email, phone, callback ->
                                viewModel.register(first, last, email, phone, callback)
                            },
                            onSwitchDemoRole = { role ->
                                viewModel.switchDemoRole(role)
                            },
                            onAuthSuccess = {
                                currentScreen = CardCeezaScreen.DASHBOARD
                            }
                        )
                    }

                    CardCeezaScreen.DASHBOARD -> {
                        UserDashboardScreen(
                            user = currentUser,
                            userBalance = userBalance,
                            trades = userTrades,
                            rates = activeRates,
                            bankAccounts = userBankAccounts,
                            unreadNotifCount = unreadNotifCount,
                            onNavigateToTrade = { currentScreen = CardCeezaScreen.SELL_CARD },
                            onNavigateToRates = { currentScreen = CardCeezaScreen.RATES },
                            onNavigateToWallet = { currentScreen = CardCeezaScreen.WALLET },
                            onNavigateToTradeHistory = { currentScreen = CardCeezaScreen.TRADE_HISTORY },
                            onNavigateToNotifications = { currentScreen = CardCeezaScreen.NOTIFICATIONS },
                            onNavigateToTradeDetail = { tradeId ->
                                selectedTradeId = tradeId
                                currentScreen = CardCeezaScreen.TRADE_DETAIL
                            },
                            onOpenAddBank = { showAddBankDialog = true },
                            onOpenWithdraw = { showWithdrawDialog = true },
                            onSwitchDemoRole = { role ->
                                viewModel.switchDemoRole(role)
                            },
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = { viewModel.toggleDarkMode() }
                        )
                    }

                    CardCeezaScreen.SELL_CARD -> {
                        SellCardScreen(
                            giftCards = activeGiftCards,
                            rates = activeRates,
                            bankAccounts = userBankAccounts,
                            onBack = { currentScreen = CardCeezaScreen.DASHBOARD },
                            onSubmitTrade = { cardId, region, value, eCode, evidence, onDone ->
                                viewModel.submitTrade(cardId, region, value, eCode, evidence, onDone)
                            },
                            onTradeCreated = { tradeId ->
                                selectedTradeId = tradeId
                                currentScreen = CardCeezaScreen.TRADE_DETAIL
                            }
                        )
                    }

                    CardCeezaScreen.RATES -> {
                        RatesScreen(
                            rates = activeRates,
                            onTradeCard = { currentScreen = CardCeezaScreen.SELL_CARD }
                        )
                    }

                    CardCeezaScreen.WALLET -> {
                        WalletScreen(
                            userBalance = userBalance,
                            bankAccounts = userBankAccounts,
                            ledgerEntries = userLedger,
                            trades = userTrades,
                            onOpenAddBank = { showAddBankDialog = true },
                            onOpenWithdraw = { showWithdrawDialog = true },
                            onDeleteBank = { bankId -> viewModel.deleteBankAccount(bankId) },
                            onNavigateToTradeDetail = { tradeId ->
                                selectedTradeId = tradeId
                                currentScreen = CardCeezaScreen.TRADE_DETAIL
                            },
                            isBiometricLockEnabled = isBiometricLockEnabled,
                            onToggleBiometricLock = { viewModel.toggleBiometricLock() },
                            onNavigateToTradeHistory = { currentScreen = CardCeezaScreen.TRADE_HISTORY }
                        )
                    }

                    CardCeezaScreen.TRADE_HISTORY -> {
                        TradeHistoryScreen(
                            trades = userTrades,
                            onBack = { currentScreen = CardCeezaScreen.DASHBOARD },
                            onNavigateToTradeDetail = { tradeId ->
                                selectedTradeId = tradeId
                                currentScreen = CardCeezaScreen.TRADE_DETAIL
                            },
                            onStartNewTrade = { currentScreen = CardCeezaScreen.SELL_CARD }
                        )
                    }

                    CardCeezaScreen.TRADE_DETAIL -> {
                        val activeTrade = (allTrades + userTrades).find { it.id == selectedTradeId }
                        val events by viewModel.getTradeEvents(selectedTradeId ?: "").collectAsState(initial = emptyList())

                        TradeDetailScreen(
                            trade = activeTrade,
                            events = events,
                            currentUser = currentUser,
                            onBack = { currentScreen = CardCeezaScreen.DASHBOARD },
                            onUpdateTradeStatus = { tradeId, status, note, rejection ->
                                viewModel.updateTradeStatus(tradeId, status, note, rejection)
                            },
                            onProcessPayout = { tradeId ->
                                viewModel.processPayout(tradeId)
                            }
                        )
                    }

                    CardCeezaScreen.ADMIN -> {
                        AdminDashboardScreen(
                            currentUser = currentUser,
                            totalUsersCount = kpiUsersCount,
                            totalTradesCount = kpiTradesCount,
                            totalPaidVolumeNgn = kpiPaidVolumeNgn ?: 0.0,
                            allTrades = allTrades,
                            verificationQueue = verificationQueue,
                            payoutQueue = payoutQueue,
                            rates = allRates,
                            giftCards = allGiftCards,
                            auditLogs = auditLogs,
                            onUpdateTradeStatus = { tradeId, status, note, rejection ->
                                viewModel.updateTradeStatus(tradeId, status, note, rejection)
                            },
                            onProcessPayout = { tradeId ->
                                viewModel.processPayout(tradeId)
                            },
                            onUpdateRate = { rateId, newRate, newFee ->
                                viewModel.updateRate(rateId, newRate, newFee)
                            },
                            onAddNewRate = { cardId, cardName, region, curr, rate, fee ->
                                viewModel.addNewRate(cardId, cardName, region, curr, rate, fee)
                            },
                            onNavigateToTradeDetail = { tradeId ->
                                selectedTradeId = tradeId
                                currentScreen = CardCeezaScreen.TRADE_DETAIL
                            },
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = { viewModel.toggleDarkMode() }
                        )
                    }

                    CardCeezaScreen.NOTIFICATIONS -> {
                        NotificationsScreen(
                            notifications = userNotifications,
                            onBack = { currentScreen = CardCeezaScreen.DASHBOARD },
                            onMarkRead = { id -> viewModel.markNotificationRead(id) },
                            onMarkAllRead = { viewModel.markAllNotificationsRead() },
                            onNavigateToTradeDetail = { tradeId ->
                                selectedTradeId = tradeId
                                currentScreen = CardCeezaScreen.TRADE_DETAIL
                            }
                        )
                    }

                    CardCeezaScreen.SUPPORT -> {
                        SupportScreen(
                            currentUser = currentUser,
                            tickets = userTickets,
                            onBack = { currentScreen = CardCeezaScreen.DASHBOARD },
                            onCreateTicket = { subj, cat, msg, callback ->
                                viewModel.createSupportTicket(subj, cat, msg, onSuccess = callback)
                            },
                            onSendMessage = { ticketId, text, isStaff ->
                                viewModel.sendSupportMessage(ticketId, text, isStaff)
                            }
                        )
                    }

                    CardCeezaScreen.PROFILE -> {
                        ProfileAndSecurityScreen(
                            user = currentUser,
                            onBack = { currentScreen = CardCeezaScreen.DASHBOARD },
                            onLogout = {
                                viewModel.logout()
                                currentScreen = CardCeezaScreen.AUTH
                            },
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = { viewModel.toggleDarkMode() },
                            isBiometricLockEnabled = isBiometricLockEnabled,
                            onToggleBiometricLock = { viewModel.toggleBiometricLock() },
                            isTtsVoiceEnabled = isTtsVoiceEnabled,
                            onToggleTtsVoice = { viewModel.toggleTtsVoice() },
                            onOpenTutorial = { showOnboardingTutorialDialog = true },
                            onOpenTradeHistory = { currentScreen = CardCeezaScreen.TRADE_HISTORY }
                        )
                    }
                }
            }
        }
    }
}
