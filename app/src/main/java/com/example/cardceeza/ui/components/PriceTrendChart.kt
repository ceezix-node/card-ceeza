package com.example.cardceeza.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cardceeza.ui.theme.Emerald100
import com.example.cardceeza.ui.theme.Emerald700
import com.example.cardceeza.ui.theme.Emerald800
import com.example.cardceeza.ui.theme.Emerald900
import com.example.cardceeza.ui.theme.Gold400
import com.example.cardceeza.ui.theme.Gold500
import com.example.cardceeza.ui.theme.StatusSuccess

data class PricePoint(
    val timeLabel: String,
    val rateNgn: Double
)

@Composable
fun PriceTrendChart(
    modifier: Modifier = Modifier,
    selectedCardName: String = "Apple & iTunes",
    currentRateNgn: Double = 1430.0
) {
    var selectedTimeframe by remember { mutableStateOf("7D") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedPointIndex by remember { mutableIntStateOf(-1) }

    // Mock realistic price fluctuation points based on category & timeframe
    val points = remember(selectedCardName, selectedTimeframe) {
        val base = currentRateNgn
        when (selectedTimeframe) {
            "24H" -> listOf(
                PricePoint("00:00", base - 18.0),
                PricePoint("04:00", base - 12.0),
                PricePoint("08:00", base - 5.0),
                PricePoint("12:00", base + 8.0),
                PricePoint("16:00", base + 4.0),
                PricePoint("20:00", base + 15.0),
                PricePoint("Now", base)
            )
            "7D" -> listOf(
                PricePoint("Mon", base - 45.0),
                PricePoint("Tue", base - 28.0),
                PricePoint("Wed", base - 35.0),
                PricePoint("Thu", base - 10.0),
                PricePoint("Fri", base + 12.0),
                PricePoint("Sat", base + 22.0),
                PricePoint("Sun", base)
            )
            "30D" -> listOf(
                PricePoint("W1", base - 110.0),
                PricePoint("W2", base - 75.0),
                PricePoint("W3", base - 30.0),
                PricePoint("W4", base + 15.0),
                PricePoint("Today", base)
            )
            else -> listOf(
                PricePoint("Q1", base - 240.0),
                PricePoint("Q2", base - 160.0),
                PricePoint("Q3", base - 80.0),
                PricePoint("Q4", base)
            )
        }
    }

    val minRate = (points.minOfOrNull { it.rateNgn } ?: currentRateNgn) - 10.0
    val maxRate = (points.maxOfOrNull { it.rateNgn } ?: currentRateNgn) + 10.0
    val rateRange = (maxRate - minRate).coerceAtLeast(1.0)
    val percentageChange = remember(points) {
        val first = points.firstOrNull()?.rateNgn ?: currentRateNgn
        val last = points.lastOrNull()?.rateNgn ?: currentRateNgn
        ((last - first) / first) * 100
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("price_trend_chart_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with rate & percentage gain
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Emerald700,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Rate Volatility & Trend",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$selectedCardName (USD/NGN)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = if (percentageChange >= 0) StatusSuccess.copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (percentageChange >= 0) "+%.1f%%".format(percentageChange) else "%.1f%%".format(percentageChange),
                        color = if (percentageChange >= 0) StatusSuccess else Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Timeframe Selector Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("24H", "7D", "30D", "1Y").forEach { tf ->
                    val isSelected = selectedTimeframe == tf
                    Surface(
                        color = if (isSelected) Emerald700 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable {
                                selectedTimeframe = tf
                                selectedPointIndex = -1
                            }
                            .testTag("timeframe_${tf}")
                    ) {
                        Text(
                            text = tf,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Canvas Curve Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .pointerInput(points) {
                            detectTapGestures { offset ->
                                val step = size.width / (points.size - 1)
                                val index = (offset.x / step).toInt().coerceIn(0, points.size - 1)
                                selectedPointIndex = index
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val stepX = width / (points.size - 1)

                    val path = Path()
                    val fillPath = Path()

                    points.forEachIndexed { i, pt ->
                        val x = i * stepX
                        val normalizedY = ((pt.rateNgn - minRate) / rateRange).toFloat()
                        val y = height - (normalizedY * (height - 30.dp.toPx())) - 15.dp.toPx()

                        if (i == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, height)
                            fillPath.lineTo(x, y)
                        } else {
                            val prevX = (i - 1) * stepX
                            val prevNormY = ((points[i - 1].rateNgn - minRate) / rateRange).toFloat()
                            val prevY = height - (prevNormY * (height - 30.dp.toPx())) - 15.dp.toPx()

                            val cX1 = prevX + (x - prevX) / 2
                            val cY1 = prevY
                            val cX2 = prevX + (x - prevX) / 2
                            val cY2 = y

                            path.cubicTo(cX1, cY1, cX2, cY2, x, y)
                            fillPath.cubicTo(cX1, cY1, cX2, cY2, x, y)
                        }

                        if (i == points.size - 1) {
                            fillPath.lineTo(x, height)
                            fillPath.close()
                        }
                    }

                    // Draw subtle area fill gradient
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Emerald700.copy(alpha = 0.28f), Color.Transparent),
                            startY = 0f,
                            endY = height
                        )
                    )

                    // Draw main trend curve line
                    drawPath(
                        path = path,
                        color = Emerald700,
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw active points circles
                    points.forEachIndexed { i, pt ->
                        val x = i * stepX
                        val normalizedY = ((pt.rateNgn - minRate) / rateRange).toFloat()
                        val y = height - (normalizedY * (height - 30.dp.toPx())) - 15.dp.toPx()

                        val isHighlighted = i == selectedPointIndex || (selectedPointIndex == -1 && i == points.size - 1)

                        if (isHighlighted) {
                            drawCircle(
                                color = Gold500,
                                radius = 6.dp.toPx(),
                                center = Offset(x, y)
                            )
                            drawCircle(
                                color = Emerald900,
                                radius = 3.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // X-Axis Labels & Hover Value Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                points.forEachIndexed { idx, pt ->
                    val isSelected = idx == selectedPointIndex || (selectedPointIndex == -1 && idx == points.size - 1)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = pt.timeLabel,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Emerald700 else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isSelected) {
                            Text(
                                text = "₦${pt.rateNgn.toInt()}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald900
                            )
                        }
                    }
                }
            }
        }
    }
}
