package com.example.cardceeza.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cardceeza.data.local.entity.TradeEntity
import com.example.cardceeza.data.local.entity.TradeEventEntity
import com.example.cardceeza.data.local.entity.UserEntity
import com.example.cardceeza.model.RiskLevel
import com.example.cardceeza.model.TradeStatus
import com.example.cardceeza.model.UserRole
import com.example.cardceeza.ui.components.DemoModeBanner
import com.example.cardceeza.ui.components.ReceiptRow
import com.example.cardceeza.ui.components.RiskBadge
import com.example.cardceeza.ui.components.RoleBadge
import com.example.cardceeza.ui.components.StatusBadge
import com.example.cardceeza.ui.components.TransactionReceiptDialog
import com.example.cardceeza.ui.theme.Emerald100
import com.example.cardceeza.ui.theme.Emerald50
import com.example.cardceeza.ui.theme.Emerald700
import com.example.cardceeza.ui.theme.Emerald800
import com.example.cardceeza.ui.theme.Gold500
import com.example.cardceeza.ui.theme.StatusError
import com.example.cardceeza.ui.theme.StatusSuccess
import com.example.cardceeza.ui.theme.StatusWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TradeDetailScreen(
    trade: TradeEntity?,
    events: List<TradeEventEntity>,
    currentUser: UserEntity?,
    onBack: () -> Unit,
    onUpdateTradeStatus: (tradeId: String, newStatus: TradeStatus, note: String, rejectionReason: String) -> Unit,
    onProcessPayout: (tradeId: String) -> Unit
) {
    if (trade == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Trade not found.")
        }
        return
    }

    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    var showReceiptDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectionReasonText by remember { mutableStateOf("") }

    if (showReceiptDialog) {
        TransactionReceiptDialog(trade = trade, onDismiss = { showReceiptDialog = false })
    }

    if (showRejectDialog) {
        Dialog(onDismissRequest = { showRejectDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Reject Trade", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = rejectionReasonText,
                        onValueChange = { rejectionReasonText = it },
                        label = { Text("Reason for rejection") },
                        placeholder = { Text("e.g. Card already redeemed / Invalid PIN") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { showRejectDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onUpdateTradeStatus(
                                    trade.id,
                                    TradeStatus.REJECTED,
                                    "Trade rejected by verifier",
                                    rejectionReasonText.ifBlank { "Card invalid/already redeemed" }
                                )
                                showRejectDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                        ) {
                            Text("Confirm Rejection")
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("trade_detail_screen")
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Column {
                Text(
                    text = trade.tradeNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateFormat.format(Date(trade.createdAt)),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            StatusBadge(status = trade.status)
        }

        DemoModeBanner(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            // Main Trade Summary Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = trade.cardName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "${trade.region} • ${trade.currency} ${"%,.2f".format(trade.cardValue)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₦${"%,.2f".format(trade.netPayoutNgn)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = Emerald800
                                )
                                Text(
                                    text = "Rate: ₦${"%,.0f".format(trade.appliedRate)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        ReceiptRow(label = "User / Trader", value = "${trade.userName} (${trade.userEmail})")
                        ReceiptRow(label = "Settlement Bank", value = trade.payoutBankName)
                        ReceiptRow(label = "Bank Account", value = "${trade.payoutAccountNumberMasked} (${trade.payoutAccountName})")
                        if (trade.eCodeOrPin.isNotBlank()) {
                            ReceiptRow(label = "E-Code / PIN", value = trade.eCodeOrPin)
                        }
                        if (trade.payoutReference.isNotBlank()) {
                            ReceiptRow(label = "Payout Ref", value = trade.payoutReference)
                        }

                        if (trade.status == TradeStatus.PAID) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showReceiptDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("view_receipt_btn")
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("View Official Settlement Receipt")
                            }
                        }
                    }
                }
            }

            // Risk Assessment Info (Visible to Verifiers & Admins)
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = Emerald700, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Risk Analysis Engine", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            RiskBadge(level = trade.riskLevel, score = trade.riskScore)
                        }
                        if (trade.riskFlags.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Flags: ${trade.riskFlags}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Action Desk (Role-based actions for Verifier, Finance, Admin)
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Operational Controls & Actions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                val role = currentUser?.role ?: UserRole.USER

                if (role == UserRole.VERIFIER || role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("🛡️ Verifier Workspace", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E40AF))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (trade.status == TradeStatus.SUBMITTED) {
                                    Button(
                                        onClick = {
                                            onUpdateTradeStatus(trade.id, TradeStatus.UNDER_REVIEW, "Assigned to verifier queue", "")
                                        },
                                        modifier = Modifier.weight(1f).testTag("action_under_review")
                                    ) {
                                        Text("Mark Reviewing", fontSize = 11.sp)
                                    }
                                }

                                if (trade.status == TradeStatus.UNDER_REVIEW || trade.status == TradeStatus.SUBMITTED) {
                                    Button(
                                        onClick = {
                                            onUpdateTradeStatus(trade.id, TradeStatus.VERIFIED, "Code verified valid against giftcard database", "")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                                        modifier = Modifier.weight(1f).testTag("action_verify")
                                    ) {
                                        Text("Mark Verified", fontSize = 11.sp)
                                    }
                                }

                                if (trade.status == TradeStatus.VERIFIED || trade.status == TradeStatus.UNDER_REVIEW) {
                                    Button(
                                        onClick = {
                                            onUpdateTradeStatus(trade.id, TradeStatus.APPROVED, "Approved by verifier for finance payout", "")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Emerald800),
                                        modifier = Modifier.weight(1f).testTag("action_approve")
                                    ) {
                                        Text("Approve Trade", fontSize = 11.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (trade.status != TradeStatus.PAID && trade.status != TradeStatus.REJECTED) {
                                OutlinedButton(
                                    onClick = { showRejectDialog = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError),
                                    modifier = Modifier.fillMaxWidth().testTag("action_reject")
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reject Card Submission", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                if (role == UserRole.FINANCE || role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("💳 Finance & Payout Desk", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF92400E))
                            Spacer(modifier = Modifier.height(8.dp))

                            if (trade.status == TradeStatus.APPROVED) {
                                Button(
                                    onClick = { onProcessPayout(trade.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("action_process_payout")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Disburse ₦${"%,.2f".format(trade.netPayoutNgn)} via Interbank NIP", fontWeight = FontWeight.Bold)
                                }
                            } else if (trade.status == TradeStatus.PAID) {
                                Text("✅ Payout settled. Ledger updated.", fontSize = 12.sp, color = Color(0xFF15803D), fontWeight = FontWeight.SemiBold)
                            } else {
                                Text("Payout can be processed after verifier marks trade APPROVED.", fontSize = 12.sp, color = Color(0xFF78350F))
                            }
                        }
                    }
                }
            }

            // Audit & Event Timeline
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Trade Lifecycle Audit Trail",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            items(events) { evt ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Emerald100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Emerald700, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${evt.fromStatus.name} → ${evt.toStatus.name}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = evt.actorRole,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(evt.note, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(dateFormat.format(Date(evt.timestamp)), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}
