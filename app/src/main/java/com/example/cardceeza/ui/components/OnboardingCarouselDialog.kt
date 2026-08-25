package com.example.cardceeza.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.cardceeza.ui.theme.Emerald100
import com.example.cardceeza.ui.theme.Emerald700
import com.example.cardceeza.ui.theme.Emerald800
import com.example.cardceeza.ui.theme.Emerald900
import com.example.cardceeza.ui.theme.Gold400
import com.example.cardceeza.ui.theme.Gold500

data class OnboardingStep(
    val stepNumber: Int,
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val badgeText: String,
    val highlights: List<String>
)

val CardCeezaOnboardingSteps = listOf(
    OnboardingStep(
        stepNumber = 1,
        title = "Select Gift Card & Region",
        subtitle = "Wide Multi-Region Brand Support",
        description = "Choose from Steam, Apple, Amazon, Google Play, Razer Gold, Xbox, PlayStation, and more across US, UK, Canada, and Europe.",
        icon = Icons.Default.CurrencyExchange,
        badgeText = "STEP 1: BRAND SELECTION",
        highlights = listOf("Top market rate transparency", "Multi-currency input ($ / £ / €)", "Zero hidden verification fees")
    ),
    OnboardingStep(
        stepNumber = 2,
        title = "Live Automated Rate Calculator",
        subtitle = "Transparent NGN Payout Preview",
        description = "Enter your gift card face value and immediately preview your gross NGN settlement, applicable fee, and net bank payout.",
        icon = Icons.Default.Calculate,
        badgeText = "STEP 2: VALUE ESTIMATION",
        highlights = listOf("Up-to-the-minute rate updates", "Instant Nigerian Naira conversion", "Audited server-side price lock")
    ),
    OnboardingStep(
        stepNumber = 3,
        title = "Upload Card & Receipt Evidence",
        subtitle = "Military-Grade Secure Vault",
        description = "Snap clean photos of your physical card code or digital e-code along with the purchase receipt for rapid verification by our certified desk.",
        icon = Icons.Default.PhotoCamera,
        badgeText = "STEP 3: EVIDENCE SUBMISSION",
        highlights = listOf("Private end-to-end encrypted storage", "Smart fraud & integrity protection", "Real-time state machine tracking")
    ),
    OnboardingStep(
        stepNumber = 4,
        title = "Instant NGN Bank Payout",
        subtitle = "Direct Settlement to Any Nigerian Bank",
        description = "Once verified and approved, your NGN funds are instantly credited to your CardCeeza wallet or paid directly into your verified Nigerian bank account.",
        icon = Icons.Default.AccountBalance,
        badgeText = "STEP 4: GET PAID IN NAIRA",
        highlights = listOf("Support for 25+ Nigerian banks", "Automated NIP payout rails", "Downloadable audit & receipt records")
    )
)

@Composable
fun OnboardingCarouselDialog(
    onDismiss: () -> Unit,
    onStartTrading: () -> Unit
) {
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val step = CardCeezaOnboardingSteps[currentStepIndex]
    val totalSteps = CardCeezaOnboardingSteps.size

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("onboarding_carousel_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Skip button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Emerald100,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = step.badgeText,
                            color = Emerald900,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("onboarding_close_btn")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close Tutorial",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Carousel Content Area with Animated Transition
                AnimatedContent(
                    targetState = step,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "onboarding_carousel_anim"
                ) { currentSlide ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Hero Icon Box
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Emerald700, Emerald900)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = currentSlide.icon,
                                contentDescription = null,
                                tint = Gold400,
                                modifier = Modifier.size(46.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = currentSlide.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = currentSlide.subtitle,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Emerald700,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = currentSlide.description,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Key Highlights Box
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                currentSlide.highlights.forEach { highlight ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 3.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Emerald700,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = highlight,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Dots Indicator
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(totalSteps) { index ->
                        val isActive = index == currentStepIndex
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(if (isActive) 24.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (isActive) Emerald700 else MaterialTheme.colorScheme.outlineVariant)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Navigation Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStepIndex > 0) {
                        OutlinedButton(
                            onClick = { currentStepIndex-- },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("onboarding_prev_btn")
                        ) {
                            Text("Back", fontSize = 13.sp)
                        }
                    } else {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("onboarding_skip_btn")
                        ) {
                            Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        }
                    }

                    if (currentStepIndex < totalSteps - 1) {
                        Button(
                            onClick = { currentStepIndex++ },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("onboarding_next_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Next", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                onDismiss()
                                onStartTrading()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald800),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("onboarding_start_trading_btn")
                        ) {
                            Text("Start Trading Now", fontWeight = FontWeight.Bold, color = Gold400, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
