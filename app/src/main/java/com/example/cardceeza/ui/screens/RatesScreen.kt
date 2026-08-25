package com.example.cardceeza.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import com.example.cardceeza.ui.components.PriceTrendChart
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardceeza.data.local.entity.RateEntity
import com.example.cardceeza.ui.components.DemoModeBanner
import com.example.cardceeza.ui.theme.Emerald100
import com.example.cardceeza.ui.theme.Emerald50
import com.example.cardceeza.ui.theme.Emerald700
import com.example.cardceeza.ui.theme.Emerald800
import com.example.cardceeza.ui.theme.Emerald900
import com.example.cardceeza.ui.theme.Gold500
import com.example.cardceeza.ui.theme.StatusError
import com.example.cardceeza.ui.theme.StatusSuccess

@Composable
fun RatesScreen(
    rates: List<RateEntity>,
    onTradeCard: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    // Rate Calculator State
    var calcCardName by remember { mutableStateOf(rates.firstOrNull()?.cardName ?: "Apple & iTunes") }
    var calcRate by remember { mutableStateOf(rates.firstOrNull() ?: RateEntity("d", "gc_apple", "Apple & iTunes", "United States (US)", "USD", 1430.0)) }
    var calcAmountText by remember { mutableStateOf("100") }

    val calcAmount = calcAmountText.toDoubleOrNull() ?: 0.0
    val grossNgn = calcAmount * calcRate.ratePerUnit
    val feeNgn = calcRate.fee
    val estimatedPayout = (grossNgn - feeNgn).coerceAtLeast(0.0)

    val filteredRates = rates.filter {
        (searchQuery.isBlank() || it.cardName.contains(searchQuery, ignoreCase = true) || it.region.contains(searchQuery, ignoreCase = true))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("rates_screen")
    ) {
        item {
            DemoModeBanner(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }

        // Header
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Live Exchange Rates",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Real-time CardCeeza rates in Nigerian Naira (NGN)",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Live Rate Price Trend Chart
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                PriceTrendChart(
                    selectedCardName = calcRate.cardName,
                    currentRateNgn = calcRate.ratePerUnit
                )
            }
        }

        // Interactive Rate Calculator Widget
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("rate_calculator_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Calculate, contentDescription = null, tint = Emerald700)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Instant Payout Calculator", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Selected Card & Region:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(rates.take(8)) { r ->
                            val isSel = calcRate.id == r.id
                            Surface(
                                color = if (isSel) Emerald700 else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable { calcRate = r }
                            ) {
                                Text(
                                    text = "${r.cardName} (${r.currency})",
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = calcAmountText,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) calcAmountText = it },
                        label = { Text("Face Value (${calcRate.currency})") },
                        prefix = { Text("${calcRate.currency} ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calc_amount_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Calculation Results
                    Surface(
                        color = Emerald50,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Applied Rate:", fontSize = 12.sp, color = Emerald800)
                                Text("₦${"%,.2f".format(calcRate.ratePerUnit)} / 1 ${calcRate.currency}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Emerald800)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Estimated NGN Payout:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Emerald900)
                                Text("₦${"%,.2f".format(estimatedPayout)}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Emerald900)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onTradeCard,
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text("Trade This Card Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Search Bar
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search card name or country...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_rates_input")
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "All Supported Gift Card Rates (${filteredRates.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Rates List
        items(filteredRates) { rate ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onTradeCard() }
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
                        Text(
                            text = "${rate.region} • Min ${rate.currency} ${rate.minimumValue.toInt()} - Max ${rate.currency} ${rate.maximumValue.toInt()}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₦${"%,.0f".format(rate.ratePerUnit)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Emerald800
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (rate.lastShiftPercentage >= 0) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(11.dp))
                                Text("+${"%.1f".format(rate.lastShiftPercentage)}%", fontSize = 10.sp, color = StatusSuccess, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = StatusError, modifier = Modifier.size(11.dp))
                                Text("${"%.1f".format(rate.lastShiftPercentage)}%", fontSize = 10.sp, color = StatusError, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Rates may change before verification. Final settlement is determined after successful verifier confirmation.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}
