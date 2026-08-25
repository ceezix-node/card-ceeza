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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cardceeza.data.local.entity.AuditLogEntity
import com.example.cardceeza.data.local.entity.GiftCardEntity
import com.example.cardceeza.data.local.entity.RateEntity
import com.example.cardceeza.data.local.entity.TradeEntity
import com.example.cardceeza.data.local.entity.UserEntity
import com.example.cardceeza.model.RiskLevel
import com.example.cardceeza.model.TradeStatus
import com.example.cardceeza.model.UserRole
import com.example.cardceeza.ui.components.DemoModeBanner
import com.example.cardceeza.ui.components.RiskBadge
import com.example.cardceeza.ui.components.RoleBadge
import com.example.cardceeza.ui.components.StatusBadge
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
fun AdminDashboardScreen(
    currentUser: UserEntity?,
    totalUsersCount: Int,
    totalTradesCount: Int,
    totalPaidVolumeNgn: Double,
    allTrades: List<TradeEntity>,
    verificationQueue: List<TradeEntity>,
    payoutQueue: List<TradeEntity>,
    rates: List<RateEntity>,
    giftCards: List<GiftCardEntity>,
    auditLogs: List<AuditLogEntity>,
    onUpdateTradeStatus: (tradeId: String, newStatus: TradeStatus, note: String, rejectionReason: String) -> Unit,
    onProcessPayout: (tradeId: String) -> Unit,
    onUpdateRate: (rateId: String, newRate: Double, newFee: Double) -> Unit,
    onAddNewRate: (cardId: String, cardName: String, region: String, currency: String, rate: Double, fee: Double) -> Unit,
    onNavigateToTradeDetail: (String) -> Unit,
    isDarkMode: Boolean? = null,
    onToggleDarkMode: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Overview, 1: Verification Queue, 2: Payout Queue, 3: Rate Engine, 4: Audit Logs
    var editingRate by remember { mutableStateOf<RateEntity?>(null) }
    var showAddRateDialog by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    // Edit Rate Modal Dialog
    if (editingRate != null) {
        var newRateText by remember { mutableStateOf(editingRate!!.ratePerUnit.toString()) }
        var newFeeText by remember { mutableStateOf(editingRate!!.fee.toString()) }
        val newRate = newRateText.toDoubleOrNull() ?: editingRate!!.ratePerUnit
        val shift = if (editingRate!!.ratePerUnit > 0) ((newRate - editingRate!!.ratePerUnit) / editingRate!!.ratePerUnit) * 100.0 else 0.0

        Dialog(onDismissRequest = { editingRate = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp).testTag("edit_rate_dialog")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Edit Trading Rate", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("${editingRate!!.cardName} • ${editingRate!!.region}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = newRateText,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) newRateText = it },
                        label = { Text("Rate per 1 ${editingRate!!.currency} (NGN)") },
                        prefix = { Text("₦ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = if (shift >= 0) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Rate Shift: ${if (shift >= 0) "+" else ""}${"%.2f".format(shift)}%",
                            color = if (shift >= 0) Color(0xFF15803D) else Color(0xFFB91C1C),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newFeeText,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) newFeeText = it },
                        label = { Text("Platform Flat Fee (NGN)") },
                        prefix = { Text("₦ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { editingRate = null }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onUpdateRate(editingRate!!.id, newRate, newFeeText.toDoubleOrNull() ?: 0.0)
                                editingRate = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                        ) {
                            Text("Publish Rate Change")
                        }
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("admin_dashboard_screen")
    ) {
        item {
            DemoModeBanner(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }

        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CardCeeza Control Desk",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RoleBadge(role = currentUser?.role ?: UserRole.ADMIN)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RBAC Server-Enforced",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onToggleDarkMode,
                    modifier = Modifier.testTag("admin_theme_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (isDarkMode == true) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme Mode",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Emerald700,
                edgePadding = 16.dp
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Overview") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Verification (${verificationQueue.size})") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Payouts (${payoutQueue.size})") })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Rates (${rates.size})") })
                Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }, text = { Text("Audit Trail") })
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        when (selectedTab) {
            0 -> {
                // TAB 0: Admin Overview KPIs
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AdminKpiCard(
                                title = "Total Users",
                                value = totalUsersCount.toString(),
                                icon = Icons.Default.People,
                                modifier = Modifier.weight(1f)
                            )
                            AdminKpiCard(
                                title = "Total Trades",
                                value = totalTradesCount.toString(),
                                icon = Icons.Default.CurrencyExchange,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AdminKpiCard(
                                title = "Settled Volume",
                                value = "₦${"%,.0f".format(totalPaidVolumeNgn)}",
                                icon = Icons.Default.TrendingUp,
                                modifier = Modifier.weight(1f)
                            )
                            AdminKpiCard(
                                title = "Pending Payouts",
                                value = payoutQueue.size.toString(),
                                icon = Icons.Default.VerifiedUser,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Live Verification Queue (${verificationQueue.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (verificationQueue.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Verification queue is currently clear! 🎉",
                                modifier = Modifier.padding(20.dp),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(verificationQueue) { trade ->
                        AdminTradeItemCard(
                            trade = trade,
                            onClick = { onNavigateToTradeDetail(trade.id) },
                            onApprove = { onUpdateTradeStatus(trade.id, TradeStatus.APPROVED, "Approved by admin", "") },
                            onVerify = { onUpdateTradeStatus(trade.id, TradeStatus.VERIFIED, "Code verified valid", "") },
                            onReject = { onUpdateTradeStatus(trade.id, TradeStatus.REJECTED, "Rejected by admin", "Invalid card code") }
                        )
                    }
                }
            }

            1 -> {
                // TAB 1: Verification Queue Desk
                item {
                    Text(
                        text = "Trades Pending Verifier Confirmation",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                if (verificationQueue.isEmpty()) {
                    item {
                        EmptyWalletSection(text = "No trades awaiting verification.")
                    }
                } else {
                    items(verificationQueue) { trade ->
                        AdminTradeItemCard(
                            trade = trade,
                            onClick = { onNavigateToTradeDetail(trade.id) },
                            onApprove = { onUpdateTradeStatus(trade.id, TradeStatus.APPROVED, "Approved by verifier", "") },
                            onVerify = { onUpdateTradeStatus(trade.id, TradeStatus.VERIFIED, "Code verified valid", "") },
                            onReject = { onUpdateTradeStatus(trade.id, TradeStatus.REJECTED, "Rejected by verifier", "Invalid card code") }
                        )
                    }
                }
            }

            2 -> {
                // TAB 2: Payout Queue Desk (Finance)
                item {
                    Text(
                        text = "Approved Trades Ready for Nigerian Bank Settlement",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                if (payoutQueue.isEmpty()) {
                    item {
                        EmptyWalletSection(text = "No approved trades pending payout disbursement.")
                    }
                } else {
                    items(payoutQueue) { trade ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(trade.tradeNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("${trade.userName} • ${trade.cardName} (${trade.currency} ${trade.cardValue})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Bank: ${trade.payoutBankName} (${trade.payoutAccountNumberMasked})", fontSize = 11.sp, color = Emerald800, fontWeight = FontWeight.SemiBold)
                                    }
                                    Text(
                                        text = "₦${"%,.2f".format(trade.netPayoutNgn)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Emerald800
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = { onProcessPayout(trade.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("admin_disburse_${trade.id}")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Disburse ₦${"%,.2f".format(trade.netPayoutNgn)} (Interbank NIP)")
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // TAB 3: Rate Engine Management
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CardCeeza Rate Catalog (${rates.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                items(rates) { rate ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
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
                                Text(rate.cardName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${rate.region} • 1 ${rate.currency}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("₦${"%,.0f".format(rate.ratePerUnit)}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Emerald800)
                                    Text("Fee: ₦${rate.fee.toInt()}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { editingRate = rate }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Emerald700)
                                }
                            }
                        }
                    }
                }
            }

            4 -> {
                // TAB 4: Audit Trail
                item {
                    Text(
                        text = "System & Administrative Audit Logs",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                items(auditLogs) { log ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Emerald800)
                                Text(dateFormat.format(Date(log.timestamp)), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(log.details, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Actor: ${log.actorEmail} (${log.actorRole}) • IP: ${log.ipAddress}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminKpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, tint = Emerald700, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Emerald900)
            Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AdminTradeItemCard(
    trade: TradeEntity,
    onClick: () -> Unit,
    onApprove: () -> Unit,
    onVerify: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(trade.tradeNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("${trade.userName} • ${trade.cardName} (${trade.currency} ${trade.cardValue})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("₦${"%,.2f".format(trade.netPayoutNgn)}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Emerald800)
                    StatusBadge(status = trade.status)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            RiskBadge(level = trade.riskLevel, score = trade.riskScore)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (trade.status == TradeStatus.SUBMITTED) {
                    Button(
                        onClick = onVerify,
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Verify Code", fontSize = 11.sp)
                    }
                }

                if (trade.status == TradeStatus.VERIFIED || trade.status == TradeStatus.UNDER_REVIEW) {
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald800),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Approve Payout", fontSize = 11.sp)
                    }
                }

                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(0.8f)
                ) {
                    Text("Reject", fontSize = 11.sp)
                }
            }
        }
    }
}
