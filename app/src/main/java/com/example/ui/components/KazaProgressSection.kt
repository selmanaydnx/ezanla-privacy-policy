package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KazaBookEntry
import com.example.model.KazaTracker
import com.example.ui.theme.LocalAppPalette
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data model for day-by-day kaza clearance progress.
 */
data class DayKazaProgress(
    val dayLabel: String,
    val dateStr: String,
    val clearedCount: Int,
    val isToday: Boolean
)

/**
 * Modern Kaza Progress Section featuring:
 * 1. Circular Chart (Dairesel Grafik): Showing today's clearance & overall cleared percentage.
 * 2. Linear Progress Bar (İlerleme Çubuğu): Daily target progress (e.g. 6 prayers / 1 day).
 * 3. Day-by-Day Clearance Bar Chart (Gün Gün Erime Grafiği): Visualizing last 7 days clearance trend.
 */
@Composable
fun KazaProgressSection(
    kazaTracker: KazaTracker,
    kazaBookEntries: List<KazaBookEntry>,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current
    val now = remember { System.currentTimeMillis() }

    // Calculate day-by-day clearance for the last 7 days
    val dailyProgressList = remember(kazaBookEntries) {
        val result = mutableListOf<DayKazaProgress>()
        val cal = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEE", Locale("tr"))
        val dateFormat = SimpleDateFormat("d MMM", Locale("tr"))

        for (offset in 6 downTo 0) {
            cal.timeInMillis = now
            cal.add(Calendar.DAY_OF_YEAR, -offset)

            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startOfDay = cal.timeInMillis

            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val endOfDay = cal.timeInMillis

            val isToday = (offset == 0)
            val dayName = if (isToday) "Bugün" else dayFormat.format(Date(startOfDay)).replace(".", "")
            val dateFormatted = dateFormat.format(Date(startOfDay))

            val clearedCount = kazaBookEntries
                .filter { it.changeAmount < 0 && it.timestampMillis in startOfDay..endOfDay }
                .sumOf { -it.changeAmount }

            result.add(
                DayKazaProgress(
                    dayLabel = dayName,
                    dateStr = dateFormatted,
                    clearedCount = clearedCount,
                    isToday = isToday
                )
            )
        }
        result
    }

    val todayCleared = dailyProgressList.lastOrNull()?.clearedCount ?: 0
    val totalClearedEver = remember(kazaBookEntries) {
        kazaBookEntries.filter { it.changeAmount < 0 }.sumOf { -it.changeAmount }
    }

    val totalManaged = totalClearedEver + kazaTracker.total
    val overallClearedRatio = if (totalManaged > 0) {
        (totalClearedEver.toFloat() / totalManaged).coerceIn(0f, 1f)
    } else 0f

    // Daily target (1 full day of kaza = 6 prayers)
    val dailyTargetPrayers = 6
    val dailyTargetRatio = (todayCleared.toFloat() / dailyTargetPrayers).coerceIn(0f, 1f)

    // Animated ratios
    val animatedOverallRatio by animateFloatAsState(
        targetValue = overallClearedRatio,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "overallRatio"
    )

    val animatedDailyRatio by animateFloatAsState(
        targetValue = dailyTargetRatio,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "dailyRatio"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(palette.primary.copy(alpha = 0.45f), palette.accent.copy(alpha = 0.25f))
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("kaza_progress_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(palette.primary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Kaza Borcu Erime İlerlemesi",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                        )
                        Text(
                            text = "Gün gün kılınan ve eritilen kaza borçları",
                            style = MaterialTheme.typography.labelSmall.copy(color = palette.textMuted)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.primary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "Bugün: $todayCleared Vakit",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.primary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // ==========================================
            // 1. DAİRESEL GRAFİK & DETAYLI SAYILAR
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.surfaceVariant.copy(alpha = 0.6f))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Circular Gauge
                Box(
                    modifier = Modifier.size(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 8.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                        val arcSize = Size(diameter, diameter)

                        // Background track ring
                        drawArc(
                            color = Color.White.copy(alpha = 0.08f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Progress ring (showing overall or today's ratio)
                        val sweep = (if (totalClearedEver > 0) animatedOverallRatio else animatedDailyRatio) * 360f
                        if (sweep > 0f) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(palette.primary, palette.accent, palette.primary)
                                ),
                                startAngle = -90f,
                                sweepAngle = sweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }

                    // Inside text
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (totalClearedEver > 0) "%${(overallClearedRatio * 100).toInt()}" else "$todayCleared/6",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = palette.textPrimary
                            )
                        )
                        Text(
                            text = if (totalClearedEver > 0) "Eritildi" else "Bugün",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = palette.textSecondary
                            )
                        )
                    }
                }

                // Summary Statistics Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatRowItem(
                        label = "Bugün Eritilen:",
                        value = "$todayCleared Vakit (${todayCleared / 6} Gün)",
                        valueColor = if (todayCleared > 0) Color(0xFF4CAF50) else palette.textPrimary,
                        palette = palette
                    )
                    StatRowItem(
                        label = "Toplam Eritilen:",
                        value = "$totalClearedEver Vakit (${totalClearedEver / 6} Gün)",
                        valueColor = palette.primary,
                        palette = palette
                    )
                    StatRowItem(
                        label = "Kalan Kaza Borcu:",
                        value = "${kazaTracker.total} Vakit (${kazaTracker.totalDays} Gün)",
                        valueColor = palette.secondary,
                        palette = palette
                    )
                }
            }

            // ==========================================
            // 2. GÜNLÜK HEDEF İLERLEME ÇUBUĞU (LINEAR BAR)
            // ==========================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bugünkü Hedef (1 Günlük Kaza / 6 Vakit)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = palette.textPrimary
                        )
                    )
                    Text(
                        text = "$todayCleared / 6 Vakit (%${(dailyTargetRatio * 100).toInt()})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (todayCleared >= 6) Color(0xFF4CAF50) else palette.primary
                        )
                    )
                }

                // Smooth Linear Progress Bar
                LinearProgressIndicator(
                    progress = { animatedDailyRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .testTag("kaza_daily_linear_progress"),
                    color = if (todayCleared >= 6) Color(0xFF4CAF50) else palette.primary,
                    trackColor = Color.White.copy(alpha = 0.08f)
                )

                // Motivational Tip
                Text(
                    text = when {
                        todayCleared >= 6 -> "✨ Maşallah! Bugün tam 1 günlük kaza borcunuzu (${todayCleared} vakit) erittiniz."
                        todayCleared in 1..5 -> "👏 Tebrikler! Bugün ${todayCleared} vakit kaza kılındı. 1 tam güne ulaşmak için ${6 - todayCleared} vakit kaldı."
                        else -> "💡 Her vakit namazından sonra 1 kaza kılarak günde 1 günlük borç eritebilirsiniz."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (todayCleared >= 6) Color(0xFF81C784) else palette.textSecondary,
                        fontSize = 11.5.sp
                    )
                )
            }

            // ==========================================
            // 3. GÜN GÜN ERİME GRAFİĞİ (SON 7 GÜNLÜK ÇUBUKLAR)
            // ==========================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Son 7 Günün Erime Çizelgesi",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = palette.textPrimary
                        )
                    )
                    Text(
                        text = "Günlük Dağılım",
                        style = MaterialTheme.typography.labelSmall.copy(color = palette.textMuted)
                    )
                }

                // 7 Days Chart Row
                val maxClearedInWeek = (dailyProgressList.maxOfOrNull { it.clearedCount } ?: 6).coerceAtLeast(6)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(palette.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    dailyProgressList.forEach { dayProgress ->
                        DayBarColumn(
                            dayProgress = dayProgress,
                            maxCleared = maxClearedInWeek,
                            palette = palette,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRowItem(
    label: String,
    value: String,
    valueColor: Color,
    palette: com.example.ui.theme.AppPalette
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = palette.textSecondary,
                fontSize = 12.sp
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = valueColor,
                fontSize = 12.sp
            )
        )
    }
}

@Composable
private fun DayBarColumn(
    dayProgress: DayKazaProgress,
    maxCleared: Int,
    palette: com.example.ui.theme.AppPalette,
    modifier: Modifier = Modifier
) {
    val fillFraction = (dayProgress.clearedCount.toFloat() / maxCleared).coerceIn(0f, 1f)
    val animatedHeightFraction by animateFloatAsState(
        targetValue = fillFraction,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "barHeight"
    )

    Column(
        modifier = modifier.padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Count Pill
        Text(
            text = if (dayProgress.clearedCount > 0) "${dayProgress.clearedCount}" else "-",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (dayProgress.isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (dayProgress.clearedCount > 0) {
                    if (dayProgress.isToday) palette.primary else palette.textPrimary
                } else palette.textMuted,
                fontSize = 10.sp
            )
        )

        // Vertical Bar Container (Total height: 50.dp)
        Box(
            modifier = Modifier
                .width(14.dp)
                .height(50.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color.White.copy(alpha = 0.06f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            val barHeight = (50.dp * animatedHeightFraction).coerceAtLeast(if (dayProgress.clearedCount > 0) 6.dp else 2.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        if (dayProgress.clearedCount > 0) {
                            if (dayProgress.isToday) {
                                Brush.verticalGradient(
                                    listOf(palette.primary, palette.accent)
                                )
                            } else {
                                Brush.verticalGradient(
                                    listOf(palette.primary.copy(alpha = 0.8f), palette.primary.copy(alpha = 0.4f))
                                )
                            }
                        } else {
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.05f))
                            )
                        }
                    )
            )
        }

        // Day Label
        Text(
            text = dayProgress.dayLabel,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (dayProgress.isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (dayProgress.isToday) palette.primary else palette.textSecondary,
                fontSize = 10.sp
            ),
            maxLines = 1
        )
    }
}
