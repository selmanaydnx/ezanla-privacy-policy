package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccessTime
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PrayerTimeItem
import com.example.ui.theme.LocalAppPalette
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PrayerCountdownGauge(
    nextPrayer: PrayerTimeItem?,
    currentPrayer: PrayerTimeItem?,
    countdownText: String,
    progress: Float,
    dateOffsetDays: Int,
    onDateOffsetChange: (Int) -> Unit,
    gregorianDateStr: String = "",
    hijriDateStr: String = "",
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current

    // Smooth animated progress
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "gaugeProgress"
    )

    // Pulsing glow transition
    val infiniteTransition = rememberInfiniteTransition(label = "pulseGlow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    // Breathing ring scale
    val subtleScale by infiniteTransition.animateFloat(
        initialValue = 0.99f,
        targetValue = 1.01f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "subtleScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                colors = listOf(palette.primary.copy(alpha = 0.45f), palette.accent.copy(alpha = 0.25f))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, bottom = 14.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Day selector pill (Dün | Bugün | Yarın)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(palette.surfaceVariant)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onDateOffsetChange(dateOffsetDays - 1) },
                    modifier = Modifier.size(32.dp).testTag("prev_day_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Önceki Gün",
                        tint = palette.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                val dayLabel = when (dateOffsetDays) {
                    -1 -> "Dün"
                    0 -> "Bugün"
                    1 -> "Yarın"
                    else -> if (dateOffsetDays < 0) "${-dateOffsetDays} gün önce" else "$dateOffsetDays gün sonra"
                }

                Text(
                    text = dayLabel,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (dateOffsetDays == 0) palette.primary else palette.textPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                IconButton(
                    onClick = { onDateOffsetChange(dateOffsetDays + 1) },
                    modifier = Modifier.size(32.dp).testTag("next_day_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Sonraki Gün",
                        tint = palette.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (gregorianDateStr.isNotBlank() && hijriDateStr.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                        .testTag("gauge_dual_calendar_display"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = gregorianDateStr,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = palette.textSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = palette.primary.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = hijriDateStr,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = palette.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Circular Arch Gauge
            Box(
                modifier = Modifier
                    .size(204.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val arcPadding = strokeWidth / 2f
                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                    val arcTopLeft = Offset(arcPadding, arcPadding)
                    val startAngle = 135f
                    val sweepTotal = 270f

                    // Background Track
                    drawArc(
                        color = palette.surfaceVariant,
                        startAngle = startAngle,
                        sweepAngle = sweepTotal,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active Progress Arc with luminous gradient
                    val sweepProgress = sweepTotal * animatedProgress
                    if (sweepProgress > 0f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                0.0f to palette.primary,
                                0.5f to palette.accent,
                                1.0f to palette.secondary,
                                center = Offset(size.width / 2, size.height / 2)
                            ),
                            startAngle = startAngle,
                            sweepAngle = sweepProgress,
                            useCenter = false,
                            topLeft = arcTopLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Glowing marker indicator at the tip
                        val currentAngleDeg = startAngle + sweepProgress
                        val currentAngleRad = Math.toRadians(currentAngleDeg.toDouble())
                        val radius = (size.width - strokeWidth) / 2f
                        val indicatorX = size.width / 2f + (radius * cos(currentAngleRad)).toFloat()
                        val indicatorY = size.height / 2f + (radius * sin(currentAngleRad)).toFloat()

                        // Outer halo
                        drawCircle(
                            color = palette.secondary.copy(alpha = 0.45f * pulseGlow),
                            radius = strokeWidth * 0.95f,
                            center = Offset(indicatorX, indicatorY)
                        )
                        // Inner bright dot
                        drawCircle(
                            color = Color.White,
                            radius = strokeWidth * 0.40f,
                            center = Offset(indicatorX, indicatorY)
                        )
                    }
                }

                // Center Text Display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val isRamadanNow = remember { com.example.calc.PrayerCalculationEngine.isRamadan() }
                    val nextLabel = when (nextPrayer?.type) {
                        com.example.model.PrayerType.IMSAK -> "İMSAK VAKTİNE"
                        com.example.model.PrayerType.GUNES -> "GÜNEŞ DOĞUŞUNA"
                        com.example.model.PrayerType.OGLE -> "ÖĞLE VAKTİNE"
                        com.example.model.PrayerType.IKINDI -> "İKİNDİ VAKTİNE"
                        com.example.model.PrayerType.AKSAM -> if (isRamadanNow) "AKŞAM (İFTAR) VAKTİNE" else "AKŞAM VAKTİNE"
                        com.example.model.PrayerType.YATSI -> "YATSI VAKTİNE"
                        null -> "VAKİT"
                    }
                    Text(
                        text = if (dateOffsetDays == 0) nextLabel else (nextPrayer?.type?.titleTr?.uppercase() ?: "VAKİT"),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = palette.secondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (dateOffsetDays == 0) countdownText else (nextPrayer?.timeFormatted ?: "--:--"),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = palette.textPrimary,
                            letterSpacing = 1.5.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = CircleShape,
                        color = if (dateOffsetDays == 0) palette.accent.copy(alpha = 0.2f) else palette.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = palette.accent,
                                modifier = Modifier.size(12.dp)
                            )
                            val currentDisplayName = when (currentPrayer?.type) {
                                com.example.model.PrayerType.IMSAK -> "Sabah Namazı Vakti"
                                com.example.model.PrayerType.GUNES -> "İşrak / Kuşluk Vakti"
                                com.example.model.PrayerType.OGLE -> "Öğle Vakti"
                                com.example.model.PrayerType.IKINDI -> "İkindi Vakti"
                                com.example.model.PrayerType.AKSAM -> "Akşam Vakti"
                                com.example.model.PrayerType.YATSI -> "Yatsı Vakti"
                                null -> "Gündoğumu"
                            }
                            val subtitle = if (dateOffsetDays == 0) {
                                "Vakit: $currentDisplayName"
                            } else {
                                "İlk Vakit: ${nextPrayer?.timeFormatted}"
                            }
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = palette.accent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
