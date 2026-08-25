package com.example.cardceeza.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.cardceeza.ui.components.BiometricWalletAuthDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardceeza.data.local.entity.BankAccountEntity
import com.example.cardceeza.data.local.entity.LedgerEntryEntity
import com.example.cardceeza.data.local.entity.TradeEntity
import com.example.cardceeza.model.LedgerType
import com.example.cardceeza.model.TradeStatus
import com.example.cardceeza.ui.components.DemoModeBanner
import com.example.cardceeza.ui.components.StatusBadge
import com.example.cardceeza.ui.components.TransactionReceiptDialog
import com.example.cardceeza.ui.theme.Emerald100
import com.example.cardceeza.ui.theme.Emerald50
import com.example.cardceeza.ui.theme.Emerald700
import com.example.cardceeza.ui.theme.Emerald800
import com.example.cardceeza.ui.theme.Emerald900
import com.example.cardceeza.ui.theme.Gold400
import com.example.cardceeza.ui.theme.Gold500
import com.example.cardceeza.ui.theme.StatusError
import com.example.cardceeza.ui.theme.StatusSuccess
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletScreen(
    userBalance: Double,
    bankAccounts: List<BankAccountEntity>,
    ledgerEntries: List<LedgerEntryEntity>,
    trades: List<TradeEntity>,
    onOpenAddBank: () -> Unit,
    onOpenWithdraw: () -> Unit,
    onDeleteBank: (String) -> Unit,
    onNavigateToTradeDetail: (String) -> Unit,
    isBiometricLockEnabled: Boolean = true,
    onToggleBiometricLock: () -> Unit = {},
    onNavigateToTradeHistory: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Ledger History, 1: Trades History, 2: Bank Accounts
    var selectedReceiptTrade by remember { mutableStateOf<TradeEntity?>(null) }
    var showBiometricAuthDialog by remember { mutableStateOf(false) }
    var isWalletUnlocked by remember { mutableStateOf(!isBiometricLockEnabled) }
    var isBalanceHidden by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    if (showBiometricAuthDialog) {
        BiometricWalletAuthDialog(
            onDismiss = { showBiometricAuthDialog = false },
            onAuthenticated = {
                showBiometricAuthDialog = false
                isWalletUnlocked = true
            }
        )
    }

    if (selectedReceiptTrade != null) {
        TransactionReceiptDialog(
            trade = selectedReceiptTrade!!,
            onDismiss = { selectedReceiptTrade = null }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("wallet_screen")
    ) {
        item {
            DemoModeBanner(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }

        // Biometric Security Notice & Status Bar
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isWalletUnlocked) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            if (isWalletUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isWalletUnlocked) Emerald700 else Color(0xFFDC2626),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isWalletUnlocked) "Biometric Session Active" else "Wallet Locked via Biometrics",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isWalletUnlocked) Emerald800 else Color(0xFF991B1B)
                            )
                            Text(
                                text = if (isWalletUnlocked) "Fingerprint verified for NUBAN settlements" else "Authenticate with fingerprint/Face ID to unlock",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!isWalletUnlocked) {
                        Button(
                            onClick = { showBiometricAuthDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("unlock_wallet_biometric_btn")
                        ) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Unlock", fontSize = 11.sp)
                        }
                    } else {
                        IconButton(
                            onClick = { isWalletUnlocked = false }
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock Wallet", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Balance Hero Box
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Emerald900, Emerald800, Color(0xFF04291B))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CardCeeza NGN Wallet",
                                color = Color(0xFFD1FAE5),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { isBalanceHidden = !isBalanceHidden },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Balance",
                                    tint = Color(0xFFD1FAE5),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Surface(
                            color = Color(0x3310B981),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "NIP Instant Settlement",
                                color = Gold400,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isBalanceHidden) "₦ • • • • • •" else if (!isWalletUnlocked && isBiometricLockEnabled) "₦ • • • • • •" else "₦${"%,.2f".format(userBalance)}",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (!isWalletUnlocked && isBiometricLockEnabled) {
                                    showBiometricAuthDialog = true
                                } else {
                                    onOpenWithdraw()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("wallet_withdraw_btn")
                        ) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Withdraw NGN", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                if (!isWalletUnlocked && isBiometricLockEnabled) {
                                    showBiometricAuthDialog = true
                                } else {
                                    onOpenAddBank()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("wallet_add_bank_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Bank", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Tabs Row
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Emerald700,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Ledger (${ledgerEntries.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Trades (${trades.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Banks (${bankAccounts.size})", fontWeight = FontWeight.SemiBold) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        when (selectedTab) {
            0 -> {
                // TAB 0: Double-Entry Immutable Ledger
                if (ledgerEntries.isEmpty()) {
                    item {
                        EmptyWalletSection(text = "No ledger movements yet. Payouts and withdrawals will appear here.")
                    }
                } else {
                    items(ledgerEntries) { entry ->
                        val isCredit = entry.amount >= 0
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isCredit) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                            contentDescription = null,
                                            tint = if (isCredit) StatusSuccess else StatusError,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(entry.description, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            text = "Ref: ${entry.referenceNumber} • ${dateFormat.format(Date(entry.createdAt))}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${if (isCredit) "+" else ""}₦${"%,.2f".format(entry.amount)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = if (isCredit) StatusSuccess else StatusError
                                    )
                                    Text(
                                        text = "Bal: ₦${"%,.2f".format(entry.balanceAfter)}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // TAB 1: User Trade History
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { onNavigateToTradeHistory() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = Emerald700, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Search & Advanced Filters", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Filter by date, card brand, currency & status", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Surface(
                                color = Emerald700,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Filter", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }
                }

                if (trades.isEmpty()) {
                    item {
                        EmptyWalletSection(text = "No gift card trades submitted yet.")
                    }
                } else {
                    items(trades) { trade ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable { onNavigateToTradeDetail(trade.id) }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(trade.cardName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("${trade.tradeNumber} • ${trade.currency} ${"%,.0f".format(trade.cardValue)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("₦${"%,.2f".format(trade.netPayoutNgn)}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Emerald800)
                                        StatusBadge(status = trade.status)
                                    }
                                }

                                if (trade.status == TradeStatus.PAID) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Surface(
                                            color = Emerald50,
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.clickable { selectedReceiptTrade = trade }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Emerald700, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("View Official Receipt", fontSize = 10.sp, color = Emerald700, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // TAB 2: Bank Accounts
                if (bankAccounts.isEmpty()) {
                    item {
                        EmptyWalletSection(text = "No payout bank accounts added yet.")
                    }
                } else {
                    items(bankAccounts) { bank ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(bank.bankName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        if (bank.isDefault) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(4.dp)) {
                                                Text("PRIMARY", color = Color(0xFF15803D), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Account: **** ${bank.accountNumber.takeLast(4)}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text(bank.accountName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                IconButton(onClick = { onDeleteBank(bank.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF94A3B8))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyWalletSection(text: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
