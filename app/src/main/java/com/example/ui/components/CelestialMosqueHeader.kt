package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PrayerType
import com.example.ui.theme.LocalAppPalette
import kotlin.math.cos
import kotlin.math.sin

/**
 * Modern İslâmî Mimari ve Selçuklu Geometrik Motifleri ile Yenilenen Başlık Bileşeni.
 * Görsel fotoğraf yerine zarif İslâmî mimari hatlar, 8 köşeli Selçuklu yıldızı ve
 * mihrap kemerleri dinamik olarak Canvas üzerinde çizilir ve seçilen temaya göre renk değiştirir.
 */
@Composable
fun CelestialMosqueHeader(
    cityName: String,
    hijriDateStr: String,
    gregorianDateStr: String,
    currentPrayerType: PrayerType?,
    onCityClick: () -> Unit,
    onOpenSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current

    // Infinite breathing glow for Islamic geometric lattice
    val infiniteTransition = rememberInfiniteTransition(label = "islamicMotifPulse")
    val motifGlow by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "motifGlow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        palette.surfaceVariant,
                        palette.surface,
                        palette.background
                    )
                )
            )
            .padding(bottom = 8.dp)
    ) {
        // Modern Islamic Geometric Architecture Canvas Drawing
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val width = size.width
            val height = size.height

            // 1. Subtle Radial Aura from Top Center (Mihrab illumination)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.primary.copy(alpha = motifGlow * 0.35f),
                        palette.secondary.copy(alpha = motifGlow * 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(width / 2f, 30f),
                    radius = width * 0.55f
                )
            )

            // 2. Draw Mihrab Vaulting Curves (İslâmî Kemer Hatları)
            val mihrabPath = Path().apply {
                moveTo(0f, height * 0.95f)
                cubicTo(
                    width * 0.25f, height * 0.85f,
                    width * 0.35f, height * 0.25f,
                    width * 0.5f, height * 0.12f
                )
                cubicTo(
                    width * 0.65f, height * 0.25f,
                    width * 0.75f, height * 0.85f,
                    width, height * 0.95f
                )
            }

            drawPath(
                path = mihrabPath,
                color = palette.primary.copy(alpha = 0.25f),
                style = Stroke(
                    width = 2.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f)
                )
            )

            // Inner Mihrab Arch
            val innerArch = Path().apply {
                moveTo(width * 0.1f, height)
                cubicTo(
                    width * 0.28f, height * 0.85f,
                    width * 0.38f, height * 0.38f,
                    width * 0.5f, height * 0.25f
                )
                cubicTo(
                    width * 0.62f, height * 0.38f,
                    width * 0.72f, height * 0.85f,
                    width * 0.9f, height
                )
            }

            drawPath(
                path = innerArch,
                color = palette.secondary.copy(alpha = 0.18f),
                style = Stroke(width = 1.8f)
            )

            // 3. Draw Authentic 8-Pointed Seljuk Stars (Selçuklu Yıldız Motifleri)
            drawSeljukStar(
                center = Offset(width * 0.15f, height * 0.35f),
                radius = 22f,
                color = palette.primary.copy(alpha = motifGlow * 0.45f)
            )
            drawSeljukStar(
                center = Offset(width * 0.85f, height * 0.35f),
                radius = 22f,
                color = palette.primary.copy(alpha = motifGlow * 0.45f)
            )
            drawSeljukStar(
                center = Offset(width * 0.5f, height * 0.18f),
                radius = 18f,
                color = palette.accent.copy(alpha = motifGlow * 0.55f)
            )

            // 4. Islamic Geometric Frieze Pattern along the bottom
            val numDiamonds = 12
            val diamondWidth = width / numDiamonds
            for (i in 0 until numDiamonds) {
                val cx = (i + 0.5f) * diamondWidth
                val cy = height * 0.92f
                val dSize = 6f
                val diamondPath = Path().apply {
                    moveTo(cx, cy - dSize)
                    lineTo(cx + dSize, cy)
                    lineTo(cx, cy + dSize)
                    lineTo(cx - dSize, cy)
                    close()
                }
                drawPath(diamondPath, color = palette.primary.copy(alpha = 0.15f))
            }
        }

        // Header Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: City Selection & Settings Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive City Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = palette.surface.copy(alpha = 0.85f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(palette.primary.copy(alpha = 0.5f), palette.secondary.copy(alpha = 0.3f))
                        )
                    ),
                    modifier = Modifier
                        .clickable(onClick = onCityClick)
                        .testTag("button_select_city")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Şehir",
                            tint = palette.primary,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = cityName,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                        )
                        Text(
                            text = "Değiştir",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = palette.primary,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Right controls: Current Prayer Badge & Settings Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val currentTitle = currentPrayerType?.titleTr ?: "Vakit"
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = palette.primary.copy(alpha = 0.15f),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(
                                listOf(palette.primary.copy(alpha = 0.4f), Color.Transparent)
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(palette.accent)
                            )
                            Text(
                                text = "$currentTitle Vakti",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = palette.primary
                                )
                            )
                        }
                    }

                    ThemeMotifBadge(
                        motif = palette.motif,
                        primaryColor = palette.primary,
                        accentColor = palette.accent,
                        size = 30.dp,
                        backgroundColor = palette.surface.copy(alpha = 0.85f),
                        modifier = Modifier.testTag("header_theme_motif_badge")
                    )
                }
            }

            // Dual Calendar Display: Gregorian Calendar Date alongside Hijri Date
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = palette.surface.copy(alpha = 0.85f),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(palette.primary.copy(alpha = 0.4f), palette.secondary.copy(alpha = 0.25f))
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dual_calendar_date_header")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gregorian Calendar Date
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("gregorian_date_display")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Miladî Takvim",
                            tint = palette.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Column {
                            Text(
                                text = "MİLADÎ",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textMuted,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                text = gregorianDateStr,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = palette.textPrimary,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Elegant Divider
                    Box(
                        modifier = Modifier
                            .height(26.dp)
                            .width(1.dp)
                            .background(palette.primary.copy(alpha = 0.3f))
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Hijri Date alongside
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hijri_date_display")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mosque,
                            contentDescription = "Hicrî Takvim",
                            tint = palette.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Column {
                            Text(
                                text = "HİCRÎ",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.primary,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                text = hijriDateStr,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = palette.primary,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Canvas Helper: Çift kare kesişimi ile 8 Köşeli Selçuklu Yıldızı çizer
 */
private fun DrawScope.drawSeljukStar(center: Offset, radius: Float, color: Color) {
    // Square 1
    val path1 = Path().apply {
        for (i in 0 until 4) {
            val angle = Math.toRadians((i * 90.0) + 45.0)
            val x = center.x + (radius * cos(angle)).toFloat()
            val y = center.y + (radius * sin(angle)).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    // Square 2 (Rotated 45 degrees)
    val path2 = Path().apply {
        for (i in 0 until 4) {
            val angle = Math.toRadians((i * 90.0))
            val x = center.x + (radius * cos(angle)).toFloat()
            val y = center.y + (radius * sin(angle)).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }

    drawPath(path1, color = color, style = Stroke(width = 1.6f))
    drawPath(path2, color = color, style = Stroke(width = 1.6f))
    drawCircle(color = color, radius = 2f, center = center)
}
