package com.example.cardceeza.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.cardceeza.data.local.entity.TradeEntity
import com.example.cardceeza.model.TradeStatus
import com.example.cardceeza.ui.components.DemoModeBanner
import com.example.cardceeza.ui.components.StatusBadge
import com.example.cardceeza.ui.components.TransactionReceiptDialog
import com.example.cardceeza.ui.theme.Emerald100
import com.example.cardceeza.ui.theme.Emerald700
import com.example.cardceeza.ui.theme.Emerald800
import com.example.cardceeza.ui.theme.Emerald900
import com.example.cardceeza.ui.theme.Gold400
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TradeHistoryScreen(
    trades: List<TradeEntity>,
    onBack: () -> Unit,
    onNavigateToTradeDetail: (String) -> Unit,
    onStartNewTrade: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var selectedDateRange by remember { mutableStateOf("ALL") } // ALL, TODAY, WEEK, MONTH
    var selectedCardType by remember { mutableStateOf("ALL") }
    var selectedReceiptTrade by remember { mutableStateOf<TradeEntity?>(null) }

    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    // Unique Card Types extracted from trades
    val availableCardTypes = remember(trades) {
        listOf("ALL") + trades.map { it.cardName }.distinct()
    }

    // Filter Logic
    val filteredTrades = remember(trades, searchQuery, selectedStatusFilter, selectedDateRange, selectedCardType) {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        trades.filter { trade ->
            // Search text match
            val matchesSearch = searchQuery.isBlank() ||
                    trade.tradeNumber.contains(searchQuery, ignoreCase = true) ||
                    trade.cardName.contains(searchQuery, ignoreCase = true) ||
                    trade.cardValue.toString().contains(searchQuery)

            // Status filter match
            val matchesStatus = selectedStatusFilter == "ALL" || trade.status.name == selectedStatusFilter

            // Card type filter match
            val matchesCardType = selectedCardType == "ALL" || trade.cardName.equals(selectedCardType, ignoreCase = true)

            // Date range filter match
            val matchesDateRange = when (selectedDateRange) {
                "TODAY" -> {
                    calendar.timeInMillis = now
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    trade.createdAt >= calendar.timeInMillis
                }
                "WEEK" -> {
                    val sevenDaysAgo = now - (7L * 24 * 60 * 60 * 1000)
                    trade.createdAt >= sevenDaysAgo
                }
                "MONTH" -> {
                    val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)
                    trade.createdAt >= thirtyDaysAgo
                }
                else -> true
            }

            matchesSearch && matchesStatus && matchesCardType && matchesDateRange
        }
    }

    val totalVolumeNgn: Double = filteredTrades.sumOf { it.netPayoutNgn }

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
            .testTag("trade_history_screen")
    ) {
        item {
            DemoModeBanner(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }

        // App Bar Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("trade_history_back_btn")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Trade History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Search & filter all submitted transactions",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Search Input Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by Trade ID, card name, or amount...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Emerald700) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald700,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("trade_search_input")
            )
        }

        // Status Filter Chips Row
        item {
            val statusChips = listOf(
                "ALL" to "All Statuses",
                TradeStatus.SUBMITTED.name to "Submitted",
                TradeStatus.UNDER_REVIEW.name to "Under Review",
                TradeStatus.VERIFIED.name to "Verified",
                TradeStatus.APPROVED.name to "Approved",
                TradeStatus.PAYOUT_PENDING.name to "Payout Pending",
                TradeStatus.PAID.name to "Paid",
                TradeStatus.REJECTED.name to "Rejected",
                TradeStatus.DISPUTED.name to "Disputed"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statusChips.forEach { (statusCode, label) ->
                    val isSelected = selectedStatusFilter == statusCode
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStatusFilter = statusCode },
                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Emerald700,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("status_filter_${statusCode}")
                    )
                }
            }
        }

        // Date Range & Card Filter Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Date Range Chips
                listOf("ALL" to "All Time", "TODAY" to "Today", "WEEK" to "This Week", "MONTH" to "This Month").forEach { (code, label) ->
                    val isSelected = selectedDateRange == code
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDateRange = code },
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Emerald800,
                            selectedLabelColor = Gold400
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        // Results Summary Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Emerald100.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Found ${filteredTrades.size} transactions",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Emerald900
                    )
                    Text(
                        text = "Volume: ₦%,d".format(totalVolumeNgn.toLong()),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Emerald900
                    )
                }
            }
        }

        // Trades List or Empty State
        if (filteredTrades.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No transactions match your filter criteria",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Try searching with a different status or card keyword.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(filteredTrades, key = { it.id }) { trade ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                        .clickable { onNavigateToTradeDetail(trade.id) }
                        .testTag("trade_history_item_${trade.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Emerald100),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = Emerald800,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = trade.cardName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "#${trade.tradeNumber} • ${trade.region}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            StatusBadge(status = trade.status)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Submitted Face Value",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${trade.currency} ${trade.cardValue.toInt()}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Estimated / Paid Payout",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₦%,d".format(trade.netPayoutNgn.toLong()),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald700
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateFormat.format(Date(trade.createdAt)),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (trade.status == TradeStatus.PAID) {
                                    IconButton(
                                        onClick = { selectedReceiptTrade = trade },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ReceiptLong,
                                            contentDescription = "Receipt",
                                            tint = Emerald700,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
