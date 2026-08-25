package com.example.cardceeza.ui.components

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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardceeza.model.RiskLevel
import com.example.cardceeza.model.TradeStatus
import com.example.cardceeza.model.UserRole
import com.example.cardceeza.ui.theme.Emerald100
import com.example.cardceeza.ui.theme.Emerald700
import com.example.cardceeza.ui.theme.Emerald800
import com.example.cardceeza.ui.theme.Gold100
import com.example.cardceeza.ui.theme.Gold500
import com.example.cardceeza.ui.theme.StatusError
import com.example.cardceeza.ui.theme.StatusInfo
import com.example.cardceeza.ui.theme.StatusPurple
import com.example.cardceeza.ui.theme.StatusSuccess
import com.example.cardceeza.ui.theme.StatusWarning

@Composable
fun DemoModeBanner(
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFFFFFBEB),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("demo_mode_banner")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Gold500)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "DEMO / SANDBOX MODE • No real funds transferred. Mock Nigerian NIP settlements enabled.",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF92400E)
            )
        }
    }
}

@Composable
fun StatusBadge(
    status: TradeStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        TradeStatus.DRAFT -> Pair(Color(0xFFF1F5F9), Color(0xFF475569))
        TradeStatus.SUBMITTED -> Pair(Color(0xFFE0F2FE), Color(0xFF0369A1))
        TradeStatus.UNDER_REVIEW -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309))
        TradeStatus.VERIFICATION_REQUIRED -> Pair(Color(0xFFEDE9FE), Color(0xFF6D28D9))
        TradeStatus.VERIFIED -> Pair(Color(0xFFD1FAE5), Color(0xFF065F46))
        TradeStatus.APPROVED -> Pair(Color(0xFFCCFBF1), Color(0xFF0F766E))
        TradeStatus.PAYOUT_PENDING -> Pair(Color(0xFFFEF9C3), Color(0xFF854D0E))
        TradeStatus.PAID -> Pair(Color(0xFFDCFCE7), Color(0xFF15803D))
        TradeStatus.REJECTED -> Pair(Color(0xFFFEE2E2), Color(0xFFB91C1C))
        TradeStatus.CANCELLED -> Pair(Color(0xFFF3F4F6), Color(0xFF374151))
        TradeStatus.DISPUTED -> Pair(Color(0xFFFFEDD5), Color(0xFFC2410C))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Text(
            text = status.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun RoleBadge(
    role: UserRole,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (role) {
        UserRole.USER -> Pair(Emerald100, Emerald800)
        UserRole.VERIFIER -> Pair(Color(0xFFE0E7FF), Color(0xFF3730A3))
        UserRole.FINANCE -> Pair(Gold100, Color(0xFF854D0E))
        UserRole.ADMIN, UserRole.SUPER_ADMIN -> Pair(Color(0xFFEDE9FE), Color(0xFF5B21B6))
        UserRole.SUPPORT -> Pair(Color(0xFFE0F2FE), Color(0xFF075985))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = role.name,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun RiskBadge(
    level: RiskLevel,
    score: Int,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (level) {
        RiskLevel.LOW -> Pair(Color(0xFFDCFCE7), Color(0xFF15803D))
        RiskLevel.MEDIUM -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309))
        RiskLevel.HIGH -> Pair(Color(0xFFFFEDD5), Color(0xFFC2410C))
        RiskLevel.CRITICAL -> Pair(Color(0xFFFEE2E2), Color(0xFFB91C1C))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = "${level.name} (Risk: $score/100)",
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

fun getGiftCardIcon(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "apple" -> Icons.Default.ShoppingBag
        "shopping_cart" -> Icons.Default.ShoppingCart
        "sports_esports" -> Icons.Default.SportsEsports
        "play_arrow" -> Icons.Default.PlayArrow
        "videogame_asset" -> Icons.Default.VideogameAsset
        "gamepad" -> Icons.Default.Gamepad
        "checkroom" -> Icons.Default.Checkroom
        "brush" -> Icons.Default.Brush
        "storefront" -> Icons.Default.Storefront
        "bolt" -> Icons.Default.Bolt
        else -> Icons.Default.ShoppingBag
    }
}
