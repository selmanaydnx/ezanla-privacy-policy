package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calc.PrayerCalculationEngine
import com.example.data.LiveGpsState
import com.example.model.City
import com.example.model.CompassDialStyle
import com.example.ui.theme.LocalAppPalette
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaCompassScreen(
    city: City,
    compassAzimuth: Float,
    isSensorAvailable: Boolean,
    onManualAzimuthChange: (Float) -> Unit,
    liveGpsState: LiveGpsState = LiveGpsState(),
    onRefreshGps: () -> Unit = {},
    compassDialStyle: CompassDialStyle = CompassDialStyle.CLASSIC_KAABA,
    onSelectDialStyle: (CompassDialStyle) -> Unit = {},
    isHapticEnabled: Boolean = true,
    onToggleHaptic: () -> Unit = {},
    isSoundEnabled: Boolean = false,
    onToggleSound: () -> Unit = {},
    isLevelEnabled: Boolean = true,
    onToggleLevel: () -> Unit = {},
    isTrueNorthEnabled: Boolean = true,
    onToggleTrueNorth: () -> Unit = {},
    pitch: Float = 0f,
    roll: Float = 0f,
    isDeviceFlat: Boolean = true,
    sunAzimuth: Double = 0.0,
    sunAltitude: Double = 0.0,
    magneticDeclination: Float = 5.5f,
    onQiblaAligned: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current
    var isOptionsSheetOpen by remember { mutableStateOf(false) }

    // Live GPS or City Coordinates
    val hasGpsLock = liveGpsState.latitude != null && liveGpsState.longitude != null
    val effectiveLat = if (hasGpsLock) liveGpsState.latitude!! else city.latitude
    val effectiveLng = if (hasGpsLock) liveGpsState.longitude!! else city.longitude

    val qiblaBearing = remember(effectiveLat, effectiveLng) {
        PrayerCalculationEngine.calculateQiblaBearing(effectiveLat, effectiveLng).toFloat()
    }
    val kaabaDistanceKm = remember(effectiveLat, effectiveLng) {
        PrayerCalculationEngine.calculateDistanceToKaabaKm(effectiveLat, effectiveLng)
    }

    // Declination adjustment: true north vs magnetic north
    val effectiveAzimuth = if (isTrueNorthEnabled) {
        (compassAzimuth + magneticDeclination) % 360f
    } else {
        compassAzimuth
    }

    // Angle difference between current heading and Qibla bearing
    val angleDifference = (effectiveAzimuth - qiblaBearing).normalizeAngle()
    val isAligned = abs(angleDifference) < 3.5f

    // Trigger haptic / sound feedback when entering aligned state
    var previousAligned by remember { mutableStateOf(false) }
    LaunchedEffect(isAligned) {
        if (isAligned && !previousAligned) {
            onQiblaAligned()
        }
        previousAligned = isAligned
    }

    // Smooth unwrapped continuous angle to eliminate 360-degree wrapping jitter
    var continuousAzimuth by remember { mutableFloatStateOf(effectiveAzimuth) }
    LaunchedEffect(effectiveAzimuth) {
        val diff = (effectiveAzimuth - continuousAzimuth) % 360f
        val shortestDiff = when {
            diff > 180f -> diff - 360f
            diff < -180f -> diff + 360f
            else -> diff
        }
        continuousAzimuth += shortestDiff
    }

    // High-performance, natural physical spring damping for fluid, lifelike magnetic needle rotation
    val animatedAzimuth by animateFloatAsState(
        targetValue = continuousAzimuth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, // Doğal mekanik ibre esnekliği (0.75f)
            stiffness = Spring.StiffnessLow                 // Akıcı ve yumuşak ivmelenme/yavaşlama
        ),
        label = "compassAzimuthSpringAnim"
    )

    // Current normalized compass bearing for UI display (0-360 deg)
    val displayAzimuth = ((animatedAzimuth % 360f) + 360f) % 360f
    val animatedAngleDifference = (displayAzimuth - qiblaBearing).normalizeAngle()

    // Alignment pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "qiblaPulse")
    val pulseWave by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseWave"
    )

    val radarSweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarSweep"
    )

    val alignmentColor by animateColorAsState(
        targetValue = if (isAligned) palette.accent else palette.primary,
        animationSpec = tween(350),
        label = "alignmentColor"
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ==========================================
        // 1. ÜST BAŞLIK VE KONTROLLER
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Kıble Pusulası",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                )
                Text(
                    text = "Kâbe İstikameti ve Pusula Rehberi",
                    style = MaterialTheme.typography.labelSmall.copy(color = palette.textSecondary)
                )
            }

            // Seçenekler & Kalibrasyon Butonu
            IconButton(
                onClick = { isOptionsSheetOpen = true },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(palette.surfaceVariant)
                    .testTag("open_compass_options_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Pusula Seçenekleri ve Kalibrasyon",
                    tint = palette.primary
                )
            }
        }

        // GPS ve Sapma Bilgi Rozetleri
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Canlı GPS Rozeti
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (hasGpsLock) palette.accent.copy(alpha = 0.15f) else palette.surfaceVariant,
                border = BorderStroke(1.dp, if (hasGpsLock) palette.accent.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = null,
                        tint = if (hasGpsLock) palette.accent else palette.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (hasGpsLock) {
                            val acc = liveGpsState.accuracyMeters?.let { " ±${it.toInt()}m" } ?: ""
                            "Canlı GPS$acc"
                        } else {
                            city.name
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (hasGpsLock) palette.accent else palette.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = onRefreshGps,
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "GPS Yenile",
                            tint = palette.textSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            // Kâbe Mesafesi & Açı Rozeti
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = palette.surfaceVariant,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NearMe,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${qiblaBearing.toInt()}° • $kaabaDistanceKm km",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = palette.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        }

        // ==========================================
        // 2. KADRAN STİLİ SEÇİCİ SEKMELER
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CompassDialStyle.entries.forEach { style ->
                val isSelected = style == compassDialStyle
                val chipIcon = when (style) {
                    CompassDialStyle.CLASSIC_KAABA -> Icons.Filled.Explore
                    CompassDialStyle.MODERN_MINIMAL -> Icons.Filled.Navigation
                    CompassDialStyle.RADAR_QIBLA -> Icons.Filled.Radar
                    CompassDialStyle.SOLAR_CELESTIAL -> Icons.Filled.WbSunny
                    CompassDialStyle.OTTOMAN_ASTROLABE -> Icons.Filled.Stars
                    CompassDialStyle.EMERALD_RAWDA -> Icons.Filled.Mosque
                    CompassDialStyle.SELJUK_GEOMETRIC -> Icons.Filled.AutoAwesome
                    CompassDialStyle.NIGHT_NAVIGATOR -> Icons.Filled.Nightlight
                }

                Surface(
                    onClick = { onSelectDialStyle(style) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) palette.primary.copy(alpha = 0.18f) else palette.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) palette.primary else palette.surfaceVariant
                    ),
                    modifier = Modifier.testTag("compass_style_${style.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = chipIcon,
                            contentDescription = null,
                            tint = if (isSelected) palette.primary else palette.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = style.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) palette.primary else palette.textSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // ==========================================
        // 3. MERKEZİ PUSULA KADRANI (CANVAS)
        // ==========================================
        Box(
            modifier = Modifier
                .size(310.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newAngle = (compassAzimuth + dragAmount.x * 0.4f) % 360f
                        onManualAzimuthChange(newAngle)
                    }
                }
                .testTag("qibla_compass_dial"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.width / 2 - 14.dp.toPx()

                // Arka plan dairesi
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            palette.surfaceVariant,
                            palette.surface,
                            palette.background
                        ),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )

                // Dış çerçeve ve hizalanma halkası
                drawCircle(
                    color = alignmentColor.copy(alpha = if (isAligned) 0.85f * pulseWave else 0.35f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = if (isAligned) 4.5.dp.toPx() else 2.dp.toPx())
                )

                when (compassDialStyle) {
                    CompassDialStyle.CLASSIC_KAABA -> {
                        // Dönen Kadran
                        rotate(degrees = -animatedAzimuth, pivot = center) {
                            drawClassicCompassRose(
                                center = center,
                                radius = radius,
                                primaryColor = palette.primary,
                                accentColor = palette.accent,
                                textSecondary = palette.textSecondary,
                                textMuted = palette.textMuted
                            )

                            // Kadran üzerindeki Kâbe görseli
                            val kaabaAngleRad = Math.toRadians((qiblaBearing - 90.0))
                            val kaabaDist = radius - 26.dp.toPx()
                            val kaabaX = center.x + (kaabaDist * cos(kaabaAngleRad)).toFloat()
                            val kaabaY = center.y + (kaabaDist * sin(kaabaAngleRad)).toFloat()

                            drawKaabaVisual(
                                center = Offset(kaabaX, kaabaY),
                                sizePx = 28.dp.toPx(),
                                isAligned = isAligned,
                                pulse = pulseWave,
                                goldColor = palette.primary,
                                accentColor = palette.accent
                            )
                        }

                        // Kâbe'yi Gösteren Sabit/Göreceli İbre
                        val relativeQiblaAngle = (qiblaBearing - animatedAzimuth).toFloat()
                        rotate(degrees = relativeQiblaAngle, pivot = center) {
                            drawClassicQiblaNeedle(
                                center = center,
                                radius = radius,
                                isAligned = isAligned,
                                alignmentColor = alignmentColor,
                                primaryColor = palette.primary,
                                accentColor = palette.accent,
                                secondaryColor = palette.secondary,
                                textMuted = palette.textMuted,
                                pulse = pulseWave
                            )
                        }
                    }

                    CompassDialStyle.MODERN_MINIMAL -> {
                        // Modern Minimal Kadran
                        rotate(degrees = -animatedAzimuth, pivot = center) {
                            drawModernMinimalDial(
                                center = center,
                                radius = radius,
                                primaryColor = palette.primary,
                                accentColor = palette.accent,
                                textSecondary = palette.textSecondary
                            )
                        }

                        // Modern Dijital İbre & Hedef Konisi
                        val relativeQiblaAngle = (qiblaBearing - animatedAzimuth).toFloat()
                        rotate(degrees = relativeQiblaAngle, pivot = center) {
                            drawModernMinimalNeedle(
                                center = center,
                                radius = radius,
                                isAligned = isAligned,
                                alignmentColor = alignmentColor,
                                accentColor = palette.accent
                            )
                        }
                    }

                    CompassDialStyle.RADAR_QIBLA -> {
                        // Radar Halkaları ve Izgara
                        drawRadarRings(
                            center = center,
                            radius = radius,
                            radarSweepAngle = radarSweepAngle,
                            radarColor = if (isAligned) palette.accent else palette.primary
                        )

                        // Dönen Kâbe Hedef Konumu (Radar HUD)
                        val relativeQiblaAngle = (qiblaBearing - animatedAzimuth).toFloat()
                        rotate(degrees = relativeQiblaAngle, pivot = center) {
                            drawRadarTargetLock(
                                center = center,
                                radius = radius,
                                isAligned = isAligned,
                                pulse = pulseWave,
                                targetColor = if (isAligned) palette.accent else palette.primary
                            )
                        }
                    }

                    CompassDialStyle.SOLAR_CELESTIAL -> {
                        // Göksel Kadran: Güneş Konumu & Kâbe Açısı
                        rotate(degrees = -animatedAzimuth, pivot = center) {
                            drawClassicCompassRose(
                                center = center,
                                radius = radius,
                                primaryColor = palette.primary,
                                accentColor = palette.accent,
                                textSecondary = palette.textSecondary,
                                textMuted = palette.textMuted
                            )

                            // Canlı Güneş Konumu Çizimi
                            val sunAngleRad = Math.toRadians((sunAzimuth - 90.0))
                            val sunDist = radius - 30.dp.toPx()
                            val sunX = center.x + (sunDist * cos(sunAngleRad)).toFloat()
                            val sunY = center.y + (sunDist * sin(sunAngleRad)).toFloat()

                            drawSunVisual(
                                center = Offset(sunX, sunY),
                                sizePx = 22.dp.toPx(),
                                isDay = sunAltitude > 0
                            )

                            // Kâbe simgesi
                            val kaabaAngleRad = Math.toRadians((qiblaBearing - 90.0))
                            val kaabaDist = radius - 26.dp.toPx()
                            val kaabaX = center.x + (kaabaDist * cos(kaabaAngleRad)).toFloat()
                            val kaabaY = center.y + (kaabaDist * sin(kaabaAngleRad)).toFloat()

                            drawKaabaVisual(
                                center = Offset(kaabaX, kaabaY),
                                sizePx = 26.dp.toPx(),
                                isAligned = isAligned,
                                pulse = pulseWave,
                                goldColor = palette.primary,
                                accentColor = palette.accent
                            )
                        }

                        // Kâbe İbresi
                        val relativeQiblaAngle = (qiblaBearing - animatedAzimuth).toFloat()
                        rotate(degrees = relativeQiblaAngle, pivot = center) {
                            drawClassicQiblaNeedle(
                                center = center,
                                radius = radius,
                                isAligned = isAligned,
                                alignmentColor = alignmentColor,
                                primaryColor = palette.primary,
                                accentColor = palette.accent,
                                secondaryColor = palette.secondary,
                                textMuted = palette.textMuted,
                                pulse = pulseWave
                            )
                        }
                    }

                    CompassDialStyle.OTTOMAN_ASTROLABE -> {
                        rotate(degrees = -animatedAzimuth, pivot = center) {
                            drawOttomanAstrolabeDial(
                                center = center,
                                radius = radius,
                                primaryColor = palette.primary,
                                accentColor = palette.accent,
                                textSecondary = palette.textSecondary
                            )

                            val kaabaAngleRad = Math.toRadians((qiblaBearing - 90.0))
                            val kaabaDist = radius - 26.dp.toPx()
                            val kaabaX = center.x + (kaabaDist * cos(kaabaAngleRad)).toFloat()
                            val kaabaY = center.y + (kaabaDist * sin(kaabaAngleRad)).toFloat()
                            drawKaabaVisual(
                                center = Offset(kaabaX, kaabaY),
                                sizePx = 26.dp.toPx(),
                                isAligned = isAligned,
                                pulse = pulseWave,
                                goldColor = palette.primary,
                                accentColor = palette.accent
                            )
                        }

                        val relativeQiblaAngle = (qiblaBearing - animatedAzimuth).toFloat()
                        rotate(degrees = relativeQiblaAngle, pivot = center) {
                            drawAstrolabeReteNeedle(
                                center = center,
                                radius = radius,
                                isAligned = isAligned,
                                alignmentColor = alignmentColor,
                                primaryColor = palette.primary,
                                accentColor = palette.accent,
                                pulse = pulseWave
                            )
                        }
                    }

                    CompassDialStyle.EMERALD_RAWDA -> {
                        rotate(degrees = -animatedAzimuth, pivot = center) {
                            drawEmeraldRawdaDial(
                                center = center,
                                radius = radius,
                                primaryColor = palette.primary,
                                accentColor = palette.accent,
                                textSecondary = palette.textSecondary
                            )

                            val kaabaAngleRad = Math.toRadians((qiblaBearing - 90.0))
                            val kaabaDist = radius - 26.dp.toPx()
                            val kaabaX = center.x + (kaabaDist * cos(kaabaAngleRad)).toFloat()
                            val kaabaY = center.y + (kaabaDist * sin(kaabaAngleRad)).toFloat()
                            drawKaabaVisual(
                                center = Offset(kaabaX, kaabaY),
                                sizePx = 26.dp.toPx(),
                                isAligned = isAligned,
                                pulse = pulseWave,
                                goldColor = palette.primary,
                                accentColor = palette.accent
                            )
                        }

                        val relativeQiblaAngle = (qiblaBearing - animatedAzimuth).toFloat()
                        rotate(degrees = relativeQiblaAngle, pivot = center) {
                            drawRawdaDomeNeedle(
                                center = center,
                                radius = radius,
                                isAligned = isAligned,
                                alignmentColor = alignmentColor,
                                primaryColor = palette.primary,
                                accentColor = palette.accent,
                                pulse = pulseWave
                            )
                        }
                    }

                    CompassDialStyle.SELJUK_GEOMETRIC -> {
                        rotate(degrees = -animatedAzimuth, pivot = center) {
                            drawSeljukGeometricDial(
                                center = center,
                                radius = radius,
                                primaryColor = palette.primary,
                                accentColor = palette.accent,
                                textSecondary = palette.textSecondary
                            )

                            val kaabaAngleRad = Math.toRadians((qiblaBearing - 90.0))
                            val kaabaDist = radius - 26.dp.toPx()
                            val kaabaX = center.x + (kaabaDist * cos(kaabaAngleRad)).toFloat()
                            val kaabaY = center.y + (kaabaDist * sin(kaabaAngleRad)).toFloat()
                            drawKaabaVisual(
                                center = Offset(kaabaX, kaabaY),
                                sizePx = 26.dp.toPx(),
                                isAligned = isAligned,
                                pulse = pulseWave,
                                goldColor = palette.primary,
                                accentColor = palette.accent
                            )
                        }

                        val relativeQiblaAngle = (qiblaBearing - animatedAzimuth).toFloat()
                        rotate(degrees = relativeQiblaAngle, pivot = center) {
                            drawSeljukStarNeedle(
                                center = center,
                                radius = radius,
                                isAligned = isAligned,
                                alignmentColor = alignmentColor,
                                primaryColor = palette.primary,
                                accentColor = palette.accent,
                                pulse = pulseWave
                            )
                        }
                    }

                    CompassDialStyle.NIGHT_NAVIGATOR -> {
                        rotate(degrees = -animatedAzimuth, pivot = center) {
                            drawNightNavigatorDial(
                                center = center,
                                radius = radius,
                                primaryColor = palette.primary,
                                accentColor = palette.accent,
                                textSecondary = palette.textSecondary
                            )

                            val kaabaAngleRad = Math.toRadians((qiblaBearing - 90.0))
                            val kaabaDist = radius - 26.dp.toPx()
                            val kaabaX = center.x + (kaabaDist * cos(kaabaAngleRad)).toFloat()
                            val kaabaY = center.y + (kaabaDist * sin(kaabaAngleRad)).toFloat()
                            drawKaabaVisual(
                                center = Offset(kaabaX, kaabaY),
                                sizePx = 26.dp.toPx(),
                                isAligned = isAligned,
                                pulse = pulseWave,
                                goldColor = palette.primary,
                                accentColor = palette.accent
                            )
                        }

                        val relativeQiblaAngle = (qiblaBearing - animatedAzimuth).toFloat()
                        rotate(degrees = relativeQiblaAngle, pivot = center) {
                            drawNightNavigatorNeedle(
                                center = center,
                                radius = radius,
                                isAligned = isAligned,
                                alignmentColor = alignmentColor,
                                primaryColor = palette.primary,
                                accentColor = palette.accent,
                                pulse = pulseWave
                            )
                        }
                    }
                }

                // Göbek Pini (Merkez Dairesi)
                drawCircle(
                    color = palette.surface,
                    radius = 16.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = alignmentColor,
                    radius = 8.dp.toPx(),
                    center = center
                )
            }

            // Modern Minimal modunda kadranın ortasında büyük dijital açı
            if (compassDialStyle == CompassDialStyle.MODERN_MINIMAL) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 80.dp)
                ) {
                    Text(
                        text = "${displayAzimuth.toInt()}°",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = alignmentColor
                        )
                    )
                    Text(
                        text = "Kâbe: ${qiblaBearing.toInt()}°",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = palette.textSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        // ==========================================
        // 4. SU TERAZİSİ (DÜZLÜK GÖSTERGESİ)
        // ==========================================
        if (isLevelEnabled) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDeviceFlat) palette.accent.copy(alpha = 0.12f) else Color(0xFFF59E0B).copy(alpha = 0.15f),
                border = BorderStroke(
                    1.dp,
                    if (isDeviceFlat) palette.accent.copy(alpha = 0.35f) else Color(0xFFF59E0B).copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Mini Su Terazisi Kabarcığı
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(palette.surface)
                            .border(1.dp, palette.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val maxOffsetPx = 8f
                        val bubbleOffsetX = (roll / 30f * maxOffsetPx).coerceIn(-maxOffsetPx, maxOffsetPx)
                        val bubbleOffsetY = (pitch / 30f * maxOffsetPx).coerceIn(-maxOffsetPx, maxOffsetPx)

                        Box(
                            modifier = Modifier
                                .offset(x = bubbleOffsetX.dp, y = bubbleOffsetY.dp)
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isDeviceFlat) palette.accent else Color(0xFFF59E0B))
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isDeviceFlat) "Cihaz Yere Paralel (Yüksek Hassasiyet)" else "Cihazı Düz Tutunuz",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isDeviceFlat) palette.accent else Color(0xFFF59E0B)
                            )
                        )
                        Text(
                            text = if (isDeviceFlat) {
                                "Pusula sensörü azami doğrulukta çalışıyor"
                            } else {
                                "Eğim: Eğim açısı pusula ibresinde sapmaya yol açabilir"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = palette.textSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }

                    if (isDeviceFlat) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = palette.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // ==========================================
        // 5. DURUM VE YÖNLENDİRME KARTI
        // ==========================================
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = palette.surface,
            border = BorderStroke(
                width = if (isAligned) 1.5.dp else 1.dp,
                color = if (isAligned) palette.accent.copy(alpha = 0.6f) else palette.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isAligned) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = palette.accent,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "KÂBE İSTİKAMETİNDESİNİZ",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.accent,
                                letterSpacing = 0.8.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                    Text(
                        text = "Telefonunuz tam olarak Kâbe-i Muazzama doğrultusunda. Namaza durabilirsiniz.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = palette.textPrimary,
                            textAlign = TextAlign.Center
                        )
                    )
                } else {
                    val turnDir = if (angleDifference > 0) "Sola" else "Sağa"
                    val degreesToTurn = abs(angleDifference).toInt()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (angleDifference > 0) Icons.Default.ArrowBack else Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "$degreesToTurn° $turnDir Dönünüz",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.primary
                            )
                        )
                    }
                    Text(
                        text = "Altın Kâbe simgesi pusulanın tepesiyle birleşene kadar çeviriniz.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = palette.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    )
                }

                if (!isSensorAvailable) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompassCalibration,
                            contentDescription = null,
                            tint = palette.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Manyetik sensör tespit edilemedi. Kadranı parmağınızla döndürebilirsiniz.",
                            style = MaterialTheme.typography.labelSmall.copy(color = palette.textMuted)
                        )
                    }
                }
            }
        }
    }

    // ==========================================
    // 6. PUSULA SEÇENEKLERİ & KALİBRASYON BOTTOM SHEET
    // ==========================================
    if (isOptionsSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isOptionsSheetOpen = false },
            containerColor = palette.surface,
            scrimColor = Color.Black.copy(alpha = 0.65f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Modal Başlığı
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = palette.primary
                        )
                        Text(
                            text = "Pusula Seçenekleri & Kalibrasyon",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                        )
                    }

                    IconButton(onClick = { isOptionsSheetOpen = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = palette.textSecondary
                        )
                    }
                }

                HorizontalDivider(color = palette.surfaceVariant)

                // Kadran Modu Seçimi
                Text(
                    text = "KADRAN GÖRÜNÜMÜ",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.primary,
                        letterSpacing = 1.sp
                    )
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompassDialStyle.entries.forEach { style ->
                        val isSelected = style == compassDialStyle
                        Surface(
                            onClick = { onSelectDialStyle(style) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) palette.primary.copy(alpha = 0.15f) else palette.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) palette.primary else palette.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelectDialStyle(style) },
                                    colors = RadioButtonDefaults.colors(selectedColor = palette.primary)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = style.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = palette.textPrimary
                                        )
                                    )
                                    Text(
                                        text = style.subtitle,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = palette.textSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = palette.surfaceVariant)

                // Fonksiyonel Ayarlar
                Text(
                    text = "GERİ BİLDİRİM & DOĞRULUK AYARLARI",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.primary,
                        letterSpacing = 1.sp
                    )
                )

                // 1. Titreşimli Geri Bildirim
                CompassOptionRow(
                    icon = Icons.Outlined.Vibration,
                    title = "Kıbleye Kilitlenme Titreşimi",
                    subtitle = "Tam Kâbe yönüne denk gelindiğinde hafif haptik titreşim verir",
                    checked = isHapticEnabled,
                    onCheckedChange = { onToggleHaptic() },
                    palette = palette
                )

                // 2. Sesli Bip Sinyali
                CompassOptionRow(
                    icon = Icons.Outlined.VolumeUp,
                    title = "Sesli Kilitlenme Sinyali (Bip)",
                    subtitle = "Kıble istikameti bulunduğunda sesli bildirim tonu çalar",
                    checked = isSoundEnabled,
                    onCheckedChange = { onToggleSound() },
                    palette = palette
                )

                // 3. Su Terazisi Göstergesi
                CompassOptionRow(
                    icon = Icons.Outlined.PanoramaHorizontal,
                    title = "Su Terazisi (Düzlük Sensörü)",
                    subtitle = "Cihazın eğimini ölçerek manyetik ibrenin doğru çalışmasını sağlar",
                    checked = isLevelEnabled,
                    onCheckedChange = { onToggleLevel() },
                    palette = palette
                )

                // 4. Coğrafi / Gerçek Kuzey Düzeltmesi
                CompassOptionRow(
                    icon = Icons.Outlined.Public,
                    title = "Coğrafi Gerçek Kuzey Düzeltmesi",
                    subtitle = "Manyetik kutup sapmasını (+${magneticDeclination}° Doğu) hesaplayarak harita kuzeyine göre düzeltir",
                    checked = isTrueNorthEnabled,
                    onCheckedChange = { onToggleTrueNorth() },
                    palette = palette
                )

                HorizontalDivider(color = palette.surfaceVariant)

                // Manyetik Kalibrasyon Rehberi Kartı
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = palette.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AllInclusive,
                                contentDescription = null,
                                tint = palette.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Pusula Kalibrasyonu (8 Çizme Hareketi)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = palette.primary
                                )
                            )
                        }
                        Text(
                            text = "Eğer pusula ibresi sapıyor veya titriyorsa, telefonunuzu elinizde havada yatay bir sekiz (∞) çizecek şekilde 3-4 kez çeviriniz. Bu işlem cihazın manyetometre sensörünü anında sıfırlar.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = palette.textSecondary,
                                lineHeight = 16.sp
                            )
                        )
                        Text(
                            text = "İpucu: Mıknatıslı telefon kılıfları ve metal yüzeyler pusulayı saptırabilir. Cihazı bu tür metallerden uzak tutunuz.",
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

@Composable
private fun CompassOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: () -> Unit,
    palette: com.example.ui.theme.AppPalette
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (checked) palette.primary.copy(alpha = 0.2f) else palette.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (checked) palette.primary else palette.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = palette.textPrimary
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = palette.textSecondary,
                        fontSize = 11.sp
                    )
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = palette.primary,
                checkedTrackColor = palette.primary.copy(alpha = 0.35f)
            )
        )
    }
}

/**
 * KLASİK PUSULA KADRANI
 */
private fun DrawScope.drawClassicCompassRose(
    center: Offset,
    radius: Float,
    primaryColor: Color,
    accentColor: Color,
    textSecondary: Color,
    textMuted: Color
) {
    for (i in 0 until 360 step 5) {
        val angleDeg = i.toFloat()
        val angleRad = Math.toRadians((angleDeg - 90.0))
        val isCardinal = i % 90 == 0
        val isSemiCardinal = i % 45 == 0 && !isCardinal

        val tickLength = when {
            isCardinal -> 18.dp.toPx()
            isSemiCardinal -> 12.dp.toPx()
            i % 15 == 0 -> 8.dp.toPx()
            else -> 4.dp.toPx()
        }
        val strokeWidth = when {
            isCardinal -> 2.5f
            isSemiCardinal -> 1.8f
            else -> 1.0f
        }
        val tickColor = when {
            isCardinal && angleDeg == 0f -> Color(0xFFEF4444) // North in Red
            isCardinal -> primaryColor
            isSemiCardinal -> accentColor
            else -> textMuted.copy(alpha = 0.35f)
        }

        val startX = center.x + ((radius - tickLength) * cos(angleRad)).toFloat()
        val startY = center.y + ((radius - tickLength) * sin(angleRad)).toFloat()
        val endX = center.x + (radius * cos(angleRad)).toFloat()
        val endY = center.y + (radius * sin(angleRad)).toFloat()

        drawLine(
            color = tickColor,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = strokeWidth
        )
    }
}

/**
 * KLASİK KÂBE İBRESİ
 */
private fun DrawScope.drawClassicQiblaNeedle(
    center: Offset,
    radius: Float,
    isAligned: Boolean,
    alignmentColor: Color,
    primaryColor: Color,
    accentColor: Color,
    secondaryColor: Color,
    textMuted: Color,
    pulse: Float
) {
    val needlePath = Path().apply {
        moveTo(center.x, center.y - radius + 36.dp.toPx())
        lineTo(center.x - 10.dp.toPx(), center.y - 12.dp.toPx())
        lineTo(center.x + 10.dp.toPx(), center.y - 12.dp.toPx())
        close()
    }
    drawPath(
        path = needlePath,
        brush = Brush.verticalGradient(
            listOf(if (isAligned) accentColor else secondaryColor, alignmentColor)
        )
    )

    val tailPath = Path().apply {
        moveTo(center.x, center.y + radius - 45.dp.toPx())
        lineTo(center.x - 7.dp.toPx(), center.y + 12.dp.toPx())
        lineTo(center.x + 7.dp.toPx(), center.y + 12.dp.toPx())
        close()
    }
    drawPath(
        path = tailPath,
        color = textMuted.copy(alpha = 0.5f)
    )

    // İbre ucunda zarif yön belirteci (Tek Kâbe kadranda yer aldığı için üst üste binme önlendi)
    drawCircle(
        color = if (isAligned) accentColor else primaryColor,
        radius = 5.dp.toPx(),
        center = Offset(center.x, center.y - radius + 32.dp.toPx())
    )
    drawCircle(
        color = Color.White,
        radius = 2.dp.toPx(),
        center = Offset(center.x, center.y - radius + 32.dp.toPx())
    )
}

/**
 * MODERN MİNİMAL KADRAN
 */
private fun DrawScope.drawModernMinimalDial(
    center: Offset,
    radius: Float,
    primaryColor: Color,
    accentColor: Color,
    textSecondary: Color
) {
    // İç ince halka
    drawCircle(
        color = textSecondary.copy(alpha = 0.15f),
        radius = radius * 0.75f,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )

    // Ana yön çizgileri
    for (i in 0 until 360 step 30) {
        val angleRad = Math.toRadians((i - 90.0))
        val isNorth = i == 0
        val tickLen = if (i % 90 == 0) 14.dp.toPx() else 8.dp.toPx()
        val tickColor = if (isNorth) Color(0xFFEF4444) else primaryColor.copy(alpha = 0.6f)

        val startX = center.x + ((radius - tickLen) * cos(angleRad)).toFloat()
        val startY = center.y + ((radius - tickLen) * sin(angleRad)).toFloat()
        val endX = center.x + (radius * cos(angleRad)).toFloat()
        val endY = center.y + (radius * sin(angleRad)).toFloat()

        drawLine(
            color = tickColor,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = if (isNorth) 3f else 1.5f
        )
    }
}

/**
 * MODERN MİNİMAL İBRE
 */
private fun DrawScope.drawModernMinimalNeedle(
    center: Offset,
    radius: Float,
    isAligned: Boolean,
    alignmentColor: Color,
    accentColor: Color
) {
    // Neon çizgi
    drawLine(
        color = alignmentColor,
        start = Offset(center.x, center.y - 18.dp.toPx()),
        end = Offset(center.x, center.y - radius + 20.dp.toPx()),
        strokeWidth = if (isAligned) 4.dp.toPx() else 2.5.dp.toPx(),
        cap = StrokeCap.Round
    )

    // İbre başındaki parlayan ok başı
    val arrowPath = Path().apply {
        moveTo(center.x, center.y - radius + 14.dp.toPx())
        lineTo(center.x - 8.dp.toPx(), center.y - radius + 30.dp.toPx())
        lineTo(center.x + 8.dp.toPx(), center.y - radius + 30.dp.toPx())
        close()
    }
    drawPath(path = arrowPath, color = alignmentColor)
}

/**
 * RADAR HALKALARI & SÜPÜRME
 */
private fun DrawScope.drawRadarRings(
    center: Offset,
    radius: Float,
    radarSweepAngle: Float,
    radarColor: Color
) {
    val ringSteps = listOf(0.35f, 0.65f, 0.95f)
    ringSteps.forEach { step ->
        drawCircle(
            color = radarColor.copy(alpha = 0.22f),
            radius = radius * step,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )
    }

    // Çapraz radar çizgileri (Grid)
    drawLine(
        color = radarColor.copy(alpha = 0.2f),
        start = Offset(center.x, center.y - radius),
        end = Offset(center.x, center.y + radius),
        strokeWidth = 1f
    )
    drawLine(
        color = radarColor.copy(alpha = 0.2f),
        start = Offset(center.x - radius, center.y),
        end = Offset(center.x + radius, center.y),
        strokeWidth = 1f
    )

    // Dönen radar tarama süpürmesi (Sweep)
    rotate(degrees = radarSweepAngle, pivot = center) {
        val sweepRad = Math.toRadians((radarSweepAngle - 90.0))
        val targetX = center.x + (radius * cos(sweepRad)).toFloat()
        val targetY = center.y + (radius * sin(sweepRad)).toFloat()
        drawLine(
            brush = Brush.radialGradient(
                colors = listOf(radarColor.copy(alpha = 0.7f), Color.Transparent),
                center = center,
                radius = radius
            ),
            start = center,
            end = Offset(targetX, targetY),
            strokeWidth = 2.dp.toPx()
        )
    }
}

/**
 * RADAR HEDEF KİLİDİ (HUD CROSSHAIR)
 */
private fun DrawScope.drawRadarTargetLock(
    center: Offset,
    radius: Float,
    isAligned: Boolean,
    pulse: Float,
    targetColor: Color
) {
    val targetDist = radius - 30.dp.toPx()
    val targetPos = Offset(center.x, center.y - targetDist)

    // Hedef kutusu / Crosshair
    val boxSize = (if (isAligned) 28.dp.toPx() else 22.dp.toPx()) * (if (isAligned) pulse else 1f)
    val half = boxSize / 2

    // Crosshair köşeleri
    drawCircle(
        color = targetColor.copy(alpha = if (isAligned) 0.8f else 0.4f),
        radius = half,
        center = targetPos,
        style = Stroke(width = 2.dp.toPx())
    )

    // Hedef kilidi çizgileri
    val lineLen = 6.dp.toPx()
    drawLine(
        color = targetColor,
        start = Offset(targetPos.x, targetPos.y - half - lineLen),
        end = Offset(targetPos.x, targetPos.y - half),
        strokeWidth = 2f
    )
    drawLine(
        color = targetColor,
        start = Offset(targetPos.x, targetPos.y + half),
        end = Offset(targetPos.x, targetPos.y + half + lineLen),
        strokeWidth = 2f
    )
    drawLine(
        color = targetColor,
        start = Offset(targetPos.x - half - lineLen, targetPos.y),
        end = Offset(targetPos.x - half, targetPos.y),
        strokeWidth = 2f
    )
    drawLine(
        color = targetColor,
        start = Offset(targetPos.x + half, targetPos.y),
        end = Offset(targetPos.x + half + lineLen, targetPos.y),
        strokeWidth = 2f
    )
}

/**
 * GÜNEŞ GÖRSELİ (KIBLE DOĞRULAMA)
 */
private fun DrawScope.drawSunVisual(
    center: Offset,
    sizePx: Float,
    isDay: Boolean
) {
    val sunColor = if (isDay) Color(0xFFFBBF24) else Color(0xFF94A3B8)
    drawCircle(
        color = sunColor.copy(alpha = 0.3f),
        radius = sizePx * 0.8f,
        center = center
    )
    drawCircle(
        color = sunColor,
        radius = sizePx * 0.45f,
        center = center
    )
    // Güneş ışınları
    for (i in 0 until 8) {
        val angleRad = Math.toRadians((i * 45.0))
        val r1 = sizePx * 0.55f
        val r2 = sizePx * 0.75f
        drawLine(
            color = sunColor,
            start = Offset(center.x + (r1 * cos(angleRad)).toFloat(), center.y + (r1 * sin(angleRad)).toFloat()),
            end = Offset(center.x + (r2 * cos(angleRad)).toFloat(), center.y + (r2 * sin(angleRad)).toFloat()),
            strokeWidth = 1.5.dp.toPx()
        )
    }
}

/**
 * KÂBE-İ MUAZZAMA GÖRSEL ÇİZİMİ
 * Siyah Kisve, altın Hizam kuşağı ve altın kapı
 */
private fun DrawScope.drawKaabaVisual(
    center: Offset,
    sizePx: Float,
    isAligned: Boolean,
    pulse: Float,
    goldColor: Color,
    accentColor: Color
) {
    val half = sizePx / 2
    val topLeft = Offset(center.x - half, center.y - half)

    // Hizalandığında parlayan nurani hale
    if (isAligned) {
        drawCircle(
            color = accentColor.copy(alpha = 0.35f * pulse),
            radius = sizePx * 0.95f * pulse,
            center = center
        )
    }

    // Siyah kübik Kâbe gövdesi
    drawRoundRect(
        color = Color(0xFF111111),
        topLeft = topLeft,
        size = Size(sizePx, sizePx),
        cornerRadius = CornerRadius(4.dp.toPx())
    )

    // Altın sırma Kisve kuşağı (Hizam)
    val bandHeight = sizePx * 0.22f
    val bandTop = center.y - half + sizePx * 0.18f
    drawRect(
        brush = Brush.horizontalGradient(
            listOf(goldColor, Color(0xFFFFE082), goldColor)
        ),
        topLeft = Offset(center.x - half, bandTop),
        size = Size(sizePx, bandHeight)
    )

    // Altın Kâbe kapısı (Bâbü'l-Kâbe)
    val doorWidth = sizePx * 0.25f
    val doorHeight = sizePx * 0.45f
    val doorLeft = center.x + half * 0.15f
    val doorTop = center.y + half * 0.05f
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFFFFD54F), goldColor)
        ),
        topLeft = Offset(doorLeft, doorTop),
        size = Size(doorWidth, doorHeight),
        cornerRadius = CornerRadius(2.dp.toPx())
    )

    // Altınoluk & çatı çizgisi
    drawLine(
        color = goldColor.copy(alpha = 0.85f),
        start = Offset(center.x - half, center.y - half),
        end = Offset(center.x + half, center.y - half),
        strokeWidth = 2.dp.toPx()
    )
}

/**
 * 1. OSMANLI USTURLAP KADRANI (Ottoman Astrolabe Dial)
 * Pirinç / bronz renkli gök küre izdüşüm çizgileri ve lale motifli dereceler.
 */
private fun DrawScope.drawOttomanAstrolabeDial(
    center: Offset,
    radius: Float,
    primaryColor: Color,
    accentColor: Color,
    textSecondary: Color
) {
    val bronzeOuter = Color(0xFFD4AF37)
    val bronzeInner = Color(0xFF8C6D23)

    // Dış pirinç çember
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(bronzeInner.copy(alpha = 0.2f), bronzeOuter.copy(alpha = 0.4f)),
            center = center,
            radius = radius
        ),
        radius = radius - 4.dp.toPx(),
        center = center,
        style = Stroke(width = 3.dp.toPx())
    )

    // Usturlap Almukantarat (yükseklik eğrileri)
    for (step in 1..4) {
        val r = radius * (step * 0.22f)
        val cyOffset = center.y + (radius * 0.08f * (4 - step))
        drawCircle(
            color = bronzeOuter.copy(alpha = 0.22f),
            radius = r,
            center = Offset(center.x, cyOffset),
            style = Stroke(width = 1.dp.toPx())
        )
    }

    // 360 Derecelik Usturlap Taksimatı (Her 5 ve 15 derecede bir antik işaret)
    for (deg in 0 until 360 step 5) {
        val rad = Math.toRadians((deg - 90.0))
        val isMajor = deg % 30 == 0
        val isMedium = deg % 15 == 0 && !isMajor
        val tickLen = if (isMajor) 16.dp.toPx() else if (isMedium) 10.dp.toPx() else 5.dp.toPx()
        val startR = radius - 6.dp.toPx()
        val endR = startR - tickLen

        val x1 = center.x + (startR * cos(rad)).toFloat()
        val y1 = center.y + (startR * sin(rad)).toFloat()
        val x2 = center.x + (endR * cos(rad)).toFloat()
        val y2 = center.y + (endR * sin(rad)).toFloat()

        drawLine(
            color = if (isMajor) accentColor else bronzeOuter.copy(alpha = 0.6f),
            start = Offset(x1, y1),
            end = Offset(x2, y2),
            strokeWidth = if (isMajor) 2.dp.toPx() else 1.2.dp.toPx()
        )
    }
}

private fun DrawScope.drawAstrolabeReteNeedle(
    center: Offset,
    radius: Float,
    isAligned: Boolean,
    alignmentColor: Color,
    primaryColor: Color,
    accentColor: Color,
    pulse: Float
) {
    val needleLen = (radius - 20.dp.toPx()) * if (isAligned) pulse else 1f
    val needleWidth = 14.dp.toPx()

    val path = Path().apply {
        moveTo(center.x, center.y - needleLen)
        lineTo(center.x + needleWidth * 0.6f, center.y - needleLen * 0.4f)
        lineTo(center.x + needleWidth * 0.3f, center.y + needleLen * 0.25f)
        lineTo(center.x, center.y + needleLen * 0.35f)
        lineTo(center.x - needleWidth * 0.3f, center.y + needleLen * 0.25f)
        lineTo(center.x - needleWidth * 0.6f, center.y - needleLen * 0.4f)
        close()
    }

    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(alignmentColor, accentColor, Color(0xFF8C6D23)),
            startY = center.y - needleLen,
            endY = center.y + needleLen * 0.35f
        )
    )

    // Hilal ucu (Kıble yönü)
    drawCircle(
        color = alignmentColor,
        radius = 5.dp.toPx(),
        center = Offset(center.x, center.y - needleLen)
    )
}

/**
 * 2. ZÜMRÜT RAVZA KADRANI (Emerald Rawda Dial)
 * Mescid-i Nebevi zümrüt yeşili, altın hat ve dilimli kubbe motifleri.
 */
private fun DrawScope.drawEmeraldRawdaDial(
    center: Offset,
    radius: Float,
    primaryColor: Color,
    accentColor: Color,
    textSecondary: Color
) {
    val emerald = Color(0xFF10B981)
    val gold = Color(0xFFF59E0B)

    // Dilimli kubbe dış halkası
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(emerald.copy(alpha = 0.15f), emerald.copy(alpha = 0.45f)),
            center = center,
            radius = radius
        ),
        radius = radius - 6.dp.toPx(),
        center = center,
        style = Stroke(width = 2.5.dp.toPx())
    )

    // 16 Yapraklı Ravza Rozeti
    val petals = 16
    for (i in 0 until petals) {
        val angleRad = Math.toRadians((i * (360.0 / petals)))
        val px = center.x + ((radius * 0.65f) * cos(angleRad)).toFloat()
        val py = center.y + ((radius * 0.65f) * sin(angleRad)).toFloat()
        drawCircle(
            color = gold.copy(alpha = 0.28f),
            radius = radius * 0.18f,
            center = Offset(px, py),
            style = Stroke(width = 1.2.dp.toPx())
        )
    }

    // Derece Çizgileri
    for (deg in 0 until 360 step 10) {
        val rad = Math.toRadians((deg - 90.0))
        val isMajor = deg % 90 == 0
        val tickLen = if (isMajor) 14.dp.toPx() else 7.dp.toPx()
        val startR = radius - 8.dp.toPx()
        val endR = startR - tickLen

        val x1 = center.x + (startR * cos(rad)).toFloat()
        val y1 = center.y + (startR * sin(rad)).toFloat()
        val x2 = center.x + (endR * cos(rad)).toFloat()
        val y2 = center.y + (endR * sin(rad)).toFloat()

        drawLine(
            color = if (isMajor) gold else emerald.copy(alpha = 0.7f),
            start = Offset(x1, y1),
            end = Offset(x2, y2),
            strokeWidth = if (isMajor) 2.dp.toPx() else 1.2.dp.toPx()
        )
    }
}

private fun DrawScope.drawRawdaDomeNeedle(
    center: Offset,
    radius: Float,
    isAligned: Boolean,
    alignmentColor: Color,
    primaryColor: Color,
    accentColor: Color,
    pulse: Float
) {
    val needleLen = (radius - 22.dp.toPx()) * if (isAligned) pulse else 1f
    val w = 12.dp.toPx()

    // Kubbe silueti şeklinde ibre
    val path = Path().apply {
        moveTo(center.x, center.y - needleLen)
        cubicTo(
            center.x + w * 0.8f, center.y - needleLen * 0.7f,
            center.x + w * 0.6f, center.y - needleLen * 0.3f,
            center.x + w * 0.2f, center.y + needleLen * 0.25f
        )
        lineTo(center.x, center.y + needleLen * 0.35f)
        lineTo(center.x - w * 0.2f, center.y + needleLen * 0.25f)
        cubicTo(
            center.x - w * 0.6f, center.y - needleLen * 0.3f,
            center.x - w * 0.8f, center.y - needleLen * 0.7f,
            center.x, center.y - needleLen
        )
        close()
    }

    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF34D399), alignmentColor, Color(0xFF064E3B)),
            startY = center.y - needleLen,
            endY = center.y + needleLen * 0.35f
        )
    )

    // Alem ucu
    drawCircle(
        color = Color(0xFFFBBF24),
        radius = 4.5.dp.toPx(),
        center = Offset(center.x, center.y - needleLen)
    )
}

/**
 * 3. SELÇUKLU HENDEST KADRANI (Seljuk Geometric Dial)
 * 8 Köşeli Selçuklu Yıldızı ve iç içe geçen geometrik kurgu.
 */
private fun DrawScope.drawSeljukGeometricDial(
    center: Offset,
    radius: Float,
    primaryColor: Color,
    accentColor: Color,
    textSecondary: Color
) {
    // 8 Köşeli Selçuklu Yıldızı dış kuşağı
    val starRadius = radius * 0.78f
    val path1 = Path().apply {
        for (i in 0 until 4) {
            val angle = Math.toRadians((i * 90.0) + 45.0)
            val x = center.x + (starRadius * cos(angle)).toFloat()
            val y = center.y + (starRadius * sin(angle)).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    val path2 = Path().apply {
        for (i in 0 until 4) {
            val angle = Math.toRadians((i * 90.0))
            val x = center.x + (starRadius * cos(angle)).toFloat()
            val y = center.y + (starRadius * sin(angle)).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }

    drawPath(path1, color = primaryColor.copy(alpha = 0.35f), style = Stroke(width = 2.dp.toPx()))
    drawPath(path2, color = accentColor.copy(alpha = 0.35f), style = Stroke(width = 2.dp.toPx()))

    // Dış Geometrik Çember
    drawCircle(
        color = primaryColor.copy(alpha = 0.5f),
        radius = radius - 6.dp.toPx(),
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )

    // 8 Yön Selçuklu Romboidleri
    for (i in 0 until 8) {
        val angleRad = Math.toRadians((i * 45.0) - 90.0)
        val dist = radius - 14.dp.toPx()
        val px = center.x + (dist * cos(angleRad)).toFloat()
        val py = center.y + (dist * sin(angleRad)).toFloat()
        drawCircle(
            color = if (i % 2 == 0) accentColor else primaryColor,
            radius = 3.5.dp.toPx(),
            center = Offset(px, py)
        )
    }
}

private fun DrawScope.drawSeljukStarNeedle(
    center: Offset,
    radius: Float,
    isAligned: Boolean,
    alignmentColor: Color,
    primaryColor: Color,
    accentColor: Color,
    pulse: Float
) {
    val len = (radius - 22.dp.toPx()) * if (isAligned) pulse else 1f
    val w = 11.dp.toPx()

    val path = Path().apply {
        moveTo(center.x, center.y - len)
        lineTo(center.x + w, center.y - len * 0.45f)
        lineTo(center.x + w * 0.4f, center.y)
        lineTo(center.x + w * 0.5f, center.y + len * 0.28f)
        lineTo(center.x, center.y + len * 0.38f)
        lineTo(center.x - w * 0.5f, center.y + len * 0.28f)
        lineTo(center.x - w * 0.4f, center.y)
        lineTo(center.x - w, center.y - len * 0.45f)
        close()
    }

    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(alignmentColor, primaryColor, accentColor.copy(alpha = 0.8f)),
            startY = center.y - len,
            endY = center.y + len * 0.38f
        )
    )
}

/**
 * 4. GECE SEYRÜSEFERİ KADRANI (Night Navigator Dial)
 * Gece göğü koordinat ağı, Kutup Yıldızı ve yüksek kontrastlı HUD.
 */
private fun DrawScope.drawNightNavigatorDial(
    center: Offset,
    radius: Float,
    primaryColor: Color,
    accentColor: Color,
    textSecondary: Color
) {
    val cyanGlow = Color(0xFF06B6D4)
    val darkBlueGrid = Color(0xFF1E3A8A)

    // Konsantrik HUD Halkaları
    for (rFactor in listOf(0.35f, 0.65f, 0.92f)) {
        drawCircle(
            color = cyanGlow.copy(alpha = 0.25f),
            radius = radius * rFactor,
            center = center,
            style = Stroke(width = 1.2.dp.toPx())
        )
    }

    // Çapraz Nişangah Çizgileri
    drawLine(
        color = cyanGlow.copy(alpha = 0.2f),
        start = Offset(center.x - radius * 0.92f, center.y),
        end = Offset(center.x + radius * 0.92f, center.y),
        strokeWidth = 1.dp.toPx()
    )
    drawLine(
        color = cyanGlow.copy(alpha = 0.2f),
        start = Offset(center.x, center.y - radius * 0.92f),
        end = Offset(center.x, center.y + radius * 0.92f),
        strokeWidth = 1.dp.toPx()
    )

    // Kutup Yıldızı (Kuzey İndikatörü)
    drawCircle(
        color = Color(0xFF38BDF8),
        radius = 5.dp.toPx(),
        center = Offset(center.x, center.y - radius * 0.85f)
    )

    // Fosforlu Derece Tıklamaları
    for (deg in 0 until 360 step 15) {
        val rad = Math.toRadians((deg - 90.0))
        val isMajor = deg % 45 == 0
        val tickLen = if (isMajor) 12.dp.toPx() else 6.dp.toPx()
        val startR = radius - 8.dp.toPx()
        val endR = startR - tickLen

        val x1 = center.x + (startR * cos(rad)).toFloat()
        val y1 = center.y + (startR * sin(rad)).toFloat()
        val x2 = center.x + (endR * cos(rad)).toFloat()
        val y2 = center.y + (endR * sin(rad)).toFloat()

        drawLine(
            color = if (isMajor) Color(0xFF38BDF8) else cyanGlow.copy(alpha = 0.5f),
            start = Offset(x1, y1),
            end = Offset(x2, y2),
            strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
        )
    }
}

private fun DrawScope.drawNightNavigatorNeedle(
    center: Offset,
    radius: Float,
    isAligned: Boolean,
    alignmentColor: Color,
    primaryColor: Color,
    accentColor: Color,
    pulse: Float
) {
    val len = (radius - 20.dp.toPx()) * if (isAligned) pulse else 1f
    val w = 8.dp.toPx()

    // Lazer HUD Göstergesi
    val path = Path().apply {
        moveTo(center.x, center.y - len)
        lineTo(center.x + w, center.y - len * 0.2f)
        lineTo(center.x + w * 0.3f, center.y + len * 0.25f)
        lineTo(center.x - w * 0.3f, center.y + len * 0.25f)
        lineTo(center.x - w, center.y - len * 0.2f)
        close()
    }

    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(if (isAligned) Color(0xFF10B981) else Color(0xFF06B6D4), Color(0xFF0284C7)),
            startY = center.y - len,
            endY = center.y + len * 0.25f
        )
    )

    // Holografik Hedef Halka
    drawCircle(
        color = if (isAligned) Color(0xFF10B981) else Color(0xFF38BDF8),
        radius = 7.dp.toPx(),
        center = Offset(center.x, center.y - len),
        style = Stroke(width = 2.dp.toPx())
    )
}

private fun Float.normalizeAngle(): Float {
    var a = this % 360f
    if (a > 180f) a -= 360f
    if (a < -180f) a += 360f
    return a
}
