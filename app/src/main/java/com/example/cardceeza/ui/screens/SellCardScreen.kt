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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.cardceeza.data.local.entity.BankAccountEntity
import com.example.cardceeza.data.local.entity.GiftCardEntity
import com.example.cardceeza.data.local.entity.RateEntity
import com.example.cardceeza.data.local.entity.TradeEntity
import com.example.cardceeza.ui.components.DemoModeBanner
import com.example.cardceeza.ui.components.getGiftCardIcon
import com.example.cardceeza.ui.theme.Emerald100
import com.example.cardceeza.ui.theme.Emerald50
import com.example.cardceeza.ui.theme.Emerald700
import com.example.cardceeza.ui.theme.Emerald800
import com.example.cardceeza.ui.theme.Emerald900
import com.example.cardceeza.ui.theme.Gold400
import com.example.cardceeza.ui.theme.Gold500
import com.example.cardceeza.ui.theme.StatusSuccess

@Composable
fun SellCardScreen(
    giftCards: List<GiftCardEntity>,
    rates: List<RateEntity>,
    bankAccounts: List<BankAccountEntity>,
    onBack: () -> Unit,
    onSubmitTrade: (cardId: String, region: String, cardValue: Double, eCode: String, evidenceUri: String, (TradeEntity) -> Unit) -> Unit,
    onTradeCreated: (String) -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: Card & Region, 2: Value & Code, 3: Evidence, 4: Review

    var selectedCard by remember { mutableStateOf(giftCards.firstOrNull() ?: GiftCardEntity("gc_apple", "Apple & iTunes", "Apple", "apple", "Tech", "US, UK, CA", "USD", true, "apple")) }
    var selectedRegion by remember { mutableStateOf("United States (US)") }
    var cardValueText by remember { mutableStateOf("100") }
    var eCodeText by remember { mutableStateOf("") }
    var hasReceiptProof by remember { mutableStateOf(true) }
    var attachedEvidenceName by remember { mutableStateOf("card_front_and_receipt.jpg") }

    val regionsForCard = remember(selectedCard) {
        val list = mutableListOf<String>()
        if (selectedCard.country.contains("US", ignoreCase = true)) list.add("United States (US)")
        if (selectedCard.country.contains("UK", ignoreCase = true)) list.add("United Kingdom (UK)")
        if (selectedCard.country.contains("CA", ignoreCase = true)) list.add("Canada (CA)")
        if (selectedCard.country.contains("EU", ignoreCase = true) || selectedCard.country.contains("DE", ignoreCase = true)) list.add("Europe (EU)")
        if (list.isEmpty()) list.add("United States (US)")
        list
    }

    val currentRate = rates.find { it.cardId == selectedCard.id && it.region == selectedRegion }
        ?: rates.find { it.cardId == selectedCard.id }
        ?: RateEntity("default", selectedCard.id, selectedCard.name, selectedRegion, "USD", 1430.0)

    val cardValue = cardValueText.toDoubleOrNull() ?: 0.0
    val grossNgn = cardValue * currentRate.ratePerUnit
    val feeNgn = currentRate.fee
    val netPayoutNgn = (grossNgn - feeNgn).coerceAtLeast(0.0)
    val defaultBank = bankAccounts.find { it.isDefault } ?: bankAccounts.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("sell_card_screen")
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { if (step > 1) step-- else onBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Sell Gift Card",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                color = Emerald50,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Step $step of 4",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Emerald700,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        DemoModeBanner(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))

        // Progress line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (i in 1..4) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (i <= step) Emerald700 else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            when (step) {
                1 -> {
                    // STEP 1: Select Card & Region
                    item {
                        Text(
                            text = "Select Gift Card Brand",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Choose the brand you want to exchange for NGN",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        giftCards.forEach { card ->
                            val isSelected = selectedCard.id == card.id
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Emerald50 else MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) Emerald700 else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        selectedCard = card
                                        selectedRegion = regionsForCard.firstOrNull() ?: "United States (US)"
                                    }
                                    .testTag("select_card_${card.slug}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Emerald700 else Color(0xFFE2E8F0)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            getGiftCardIcon(card.iconName),
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else Color(0xFF334155),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(card.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(
                                            text = "${card.category} • Min ${card.currency} ${card.minDenomination.toInt()}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald700)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Select Card Country / Region",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(regionsForCard) { reg ->
                                val isRegSelected = selectedRegion == reg
                                Surface(
                                    color = if (isRegSelected) Emerald700 else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .border(
                                            1.dp,
                                            if (isRegSelected) Emerald700 else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedRegion = reg }
                                ) {
                                    Text(
                                        text = reg,
                                        color = if (isRegSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // STEP 2: Value & E-Code
                    item {
                        Text(
                            text = "Card Face Value & Claim Code",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Enter exact face value on physical card or receipt",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Denomination Chips
                        val quickValues = listOf("25", "50", "100", "200", "500")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(quickValues) { v ->
                                val isSel = cardValueText == v
                                Surface(
                                    color = if (isSel) Emerald700 else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .border(1.dp, if (isSel) Emerald700 else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                        .clickable { cardValueText = v }
                                ) {
                                    Text(
                                        text = "${currentRate.currency} $v",
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = cardValueText,
                            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) cardValueText = it },
                            label = { Text("Face Value (${currentRate.currency})") },
                            prefix = { Text("${currentRate.currency} ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_value_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = eCodeText,
                            onValueChange = { eCodeText = it },
                            label = { Text("Card E-Code / PIN / Serial (Optional)") },
                            placeholder = { Text("e.g. X7M8-9K2L-44NJ-P8QW") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_ecode_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Live Rate Preview Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Emerald50),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Current Rate:", fontSize = 12.sp, color = Emerald800)
                                    Text("₦${"%,.2f".format(currentRate.ratePerUnit)} / 1 ${currentRate.currency}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Emerald800)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Estimated Payout:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Emerald900)
                                    Text("₦${"%,.2f".format(netPayoutNgn)}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Emerald900)
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // STEP 3: Evidence Upload
                    item {
                        Text(
                            text = "Upload Card & Receipt Proof",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Clear images ensure fast verifier approval within 5-10 minutes",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Emerald700, RoundedCornerShape(16.dp))
                                .padding(4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(Emerald100),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Emerald700, modifier = Modifier.size(28.dp))
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Attached File:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = attachedEvidenceName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Emerald700
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Physical Card Scratch Panel + Retail Cash Receipt", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = Emerald700, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "All images are end-to-end encrypted and only accessible by authorized KYC verifiers during the trade verification lifecycle.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                4 -> {
                    // STEP 4: Final Review & Confirmation
                    item {
                        Text(
                            text = "Review & Submit Trade",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Please verify your settlement bank and trade details",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Gift Card Brand:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(selectedCard.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Region / Currency:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${selectedRegion} (${currentRate.currency})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Card Value:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${currentRate.currency} ${"%,.2f".format(cardValue)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Applied Rate:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("₦${"%,.2f".format(currentRate.ratePerUnit)}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Platform Fee:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("₦0.00 (Zero Fee)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = StatusSuccess)
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Expected NGN Payout:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        "₦${"%,.2f".format(netPayoutNgn)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        color = Emerald800
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Surface(
                                    color = Emerald50,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Settlement Account:", fontSize = 11.sp, color = Emerald800, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = if (defaultBank != null) "${defaultBank.bankName} (**** ${defaultBank.accountNumber.takeLast(4)})" else "Primary GTBank (0123****89)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Rates may change before verification. Final settlement is determined after successful verifier confirmation.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Bottom Action Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick = { step-- },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Previous")
                    }
                }

                Button(
                    onClick = {
                        if (step < 4) {
                            step++
                        } else {
                            onSubmitTrade(
                                selectedCard.id,
                                selectedRegion,
                                cardValue,
                                eCodeText,
                                attachedEvidenceName
                            ) { createdTrade ->
                                onTradeCreated(createdTrade.id)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp)
                        .testTag(if (step == 4) "submit_trade_final_btn" else "sell_next_step_btn")
                ) {
                    Text(
                        text = if (step == 4) "Confirm & Submit Trade" else "Continue to Step ${step + 1}",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
