package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.ThemeMotif
import kotlin.math.cos
import kotlin.math.sin

/**
 * Temalara tema ile alakalı motifler (Selçuklu yıldızı, Kabe örtüsü bordürü,
 * Ravza kubbesi, Osmanlı lalesi, Endülüs kemeri vb.)
 * Mobilde taşmadan ve kusursuz ölçeklenecek Canvas tabanlı motif çizici.
 */
@Composable
fun ThemeMotifBadge(
    motif: ThemeMotif,
    primaryColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    backgroundColor: Color = Color.Transparent
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            drawThemeMotif(
                motif = motif,
                primaryColor = primaryColor,
                accentColor = accentColor,
                strokeWidth = 1.6f.dp.toPx()
            )
        }
    }
}

/**
 * Arka plan veya kart içi zarif motif filigranı (Mobilde tam oturur, taşmaz)
 */
@Composable
fun ThemeMotifWatermark(
    motif: ThemeMotif,
    primaryColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
    alpha: Float = 0.10f
) {
    Box(
        modifier = modifier.clipToBounds()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawThemeMotif(
                motif = motif,
                primaryColor = primaryColor.copy(alpha = alpha),
                accentColor = accentColor.copy(alpha = alpha * 0.8f),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

fun DrawScope.drawThemeMotif(
    motif: ThemeMotif,
    primaryColor: Color,
    accentColor: Color,
    strokeWidth: Float
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f
    val radius = minOf(w, h) / 2f * 0.88f

    when (motif) {
        ThemeMotif.SELJUK_STAR -> {
            // Meşhur Selçuklu 8 Köşeli Yıldızı: İki karenin 45 derece açıyla üst üste geçmesi
            val s = radius * 1.35f
            val half = s / 2f
            val path1 = Path().apply {
                addRect(androidx.compose.ui.geometry.Rect(cx - half, cy - half, cx + half, cy + half))
            }
            drawPath(path1, primaryColor, style = Stroke(width = strokeWidth))

            rotate(45f, pivot = Offset(cx, cy)) {
                drawPath(path1, accentColor, style = Stroke(width = strokeWidth))
            }

            // Merkez sekizgen iç halka
            drawCircle(color = primaryColor, radius = radius * 0.35f, center = Offset(cx, cy), style = Stroke(width = strokeWidth * 0.8f))
            drawCircle(color = accentColor, radius = radius * 0.12f, center = Offset(cx, cy))
        }

        ThemeMotif.KISVE_GOLD -> {
            // Kâbe örtüsü altın sırmalı geometrik bordür & baklava motifi
            val diamondPath = Path().apply {
                moveTo(cx, cy - radius)
                lineTo(cx + radius, cy)
                lineTo(cx, cy + radius)
                lineTo(cx - radius, cy)
                close()
            }
            drawPath(diamondPath, primaryColor, style = Stroke(width = strokeWidth))

            val innerDiamond = Path().apply {
                moveTo(cx, cy - radius * 0.6f)
                lineTo(cx + radius * 0.6f, cy)
                lineTo(cx, cy + radius * 0.6f)
                lineTo(cx - radius * 0.6f, cy)
                close()
            }
            drawPath(innerDiamond, accentColor, style = Stroke(width = strokeWidth * 0.8f))

            // Yatay ve dikey sırma çizgileri
            drawLine(primaryColor, Offset(cx - radius, cy), Offset(cx + radius, cy), strokeWidth = strokeWidth * 0.7f)
            drawLine(primaryColor, Offset(cx, cy - radius), Offset(cx, cy + radius), strokeWidth = strokeWidth * 0.7f)
        }

        ThemeMotif.RAVZA_DOME -> {
            // Ravza Kubbesi & Selvi Ağacı silueti
            val domePath = Path().apply {
                // Kubbe kavisi
                moveTo(cx - radius * 0.75f, cy + radius * 0.6f)
                cubicTo(
                    cx - radius * 0.75f, cy - radius * 0.4f,
                    cx - radius * 0.2f, cy - radius * 0.95f,
                    cx, cy - radius * 0.95f
                )
                cubicTo(
                    cx + radius * 0.2f, cy - radius * 0.95f,
                    cx + radius * 0.75f, cy - radius * 0.4f,
                    cx + radius * 0.75f, cy + radius * 0.6f
                )
                close()
            }
            drawPath(domePath, primaryColor, style = Stroke(width = strokeWidth))

            // Kubbe alem / hilal ucu
            drawLine(accentColor, Offset(cx, cy - radius * 0.95f), Offset(cx, cy - radius * 1.15f), strokeWidth = strokeWidth)
            drawCircle(accentColor, radius = radius * 0.12f, center = Offset(cx, cy - radius * 1.18f))

            // Taban kemeri
            drawLine(primaryColor, Offset(cx - radius * 0.85f, cy + radius * 0.6f), Offset(cx + radius * 0.85f, cy + radius * 0.6f), strokeWidth = strokeWidth)
        }

        ThemeMotif.OTTOMAN_TULIP -> {
            // Klasik Osmanlı Saray Lâlesi
            val tulipPath = Path().apply {
                moveTo(cx, cy + radius * 0.85f)
                // Sol taç yaprak
                cubicTo(
                    cx - radius * 0.9f, cy + radius * 0.2f,
                    cx - radius * 0.8f, cy - radius * 0.7f,
                    cx - radius * 0.5f, cy - radius * 0.9f
                )
                cubicTo(
                    cx - radius * 0.3f, cy - radius * 0.4f,
                    cx - radius * 0.1f, cy - radius * 0.2f,
                    cx, cy - radius * 0.5f
                )
                // Sağ taç yaprak
                cubicTo(
                    cx + radius * 0.1f, cy - radius * 0.2f,
                    cx + radius * 0.3f, cy - radius * 0.4f,
                    cx + radius * 0.5f, cy - radius * 0.9f
                )
                cubicTo(
                    cx + radius * 0.8f, cy - radius * 0.7f,
                    cx + radius * 0.9f, cy + radius * 0.2f,
                    cx, cy + radius * 0.85f
                )
                close()
            }
            drawPath(tulipPath, primaryColor, style = Stroke(width = strokeWidth))

            // Orta tohum yaprağı
            val centerPetal = Path().apply {
                moveTo(cx, cy + radius * 0.4f)
                cubicTo(cx - radius * 0.25f, cy, cx - radius * 0.2f, cy - radius * 0.8f, cx, cy - radius * 1.05f)
                cubicTo(cx + radius * 0.2f, cy - radius * 0.8f, cx + radius * 0.25f, cy, cx, cy + radius * 0.4f)
                close()
            }
            drawPath(centerPetal, accentColor, style = Stroke(width = strokeWidth * 0.8f))
        }

        ThemeMotif.ALHAMBRA_ARCH -> {
            // Endülüs Elhamra At Nalı Kemer & Mukarnas
            val archPath = Path().apply {
                moveTo(cx - radius * 0.8f, cy + radius * 0.8f)
                lineTo(cx - radius * 0.8f, cy + radius * 0.1f)
                cubicTo(
                    cx - radius * 0.95f, cy - radius * 0.7f,
                    cx + radius * 0.95f, cy - radius * 0.7f,
                    cx + radius * 0.8f, cy + radius * 0.1f
                )
                lineTo(cx + radius * 0.8f, cy + radius * 0.8f)
            }
            drawPath(archPath, primaryColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

            // İç narin kemer
            val innerArch = Path().apply {
                moveTo(cx - radius * 0.5f, cy + radius * 0.8f)
                lineTo(cx - radius * 0.5f, cy + radius * 0.15f)
                cubicTo(
                    cx - radius * 0.6f, cy - radius * 0.4f,
                    cx + radius * 0.6f, cy - radius * 0.4f,
                    cx + radius * 0.5f, cy + radius * 0.15f
                )
                lineTo(cx + radius * 0.5f, cy + radius * 0.8f)
            }
            drawPath(innerArch, accentColor, style = Stroke(width = strokeWidth * 0.8f))

            // Kemer üstü yıldız çini
            drawCircle(accentColor, radius = radius * 0.15f, center = Offset(cx, cy - radius * 0.2f))
        }

        ThemeMotif.ARABESQUE_SUN -> {
            // 12 Kollu İslami Geometrik Güneş Rozeti
            val rays = 12
            for (i in 0 until rays) {
                val angle = (i * 360f / rays) * (Math.PI / 180f)
                val innerX = cx + (radius * 0.35f) * cos(angle).toFloat()
                val innerY = cy + (radius * 0.35f) * sin(angle).toFloat()
                val outerX = cx + radius * cos(angle).toFloat()
                val outerY = cy + radius * sin(angle).toFloat()
                drawLine(
                    if (i % 2 == 0) primaryColor else accentColor,
                    Offset(innerX, innerY),
                    Offset(outerX, outerY),
                    strokeWidth = strokeWidth
                )
            }
            drawCircle(primaryColor, radius = radius * 0.35f, center = Offset(cx, cy), style = Stroke(width = strokeWidth))
            drawCircle(accentColor, radius = radius * 0.14f, center = Offset(cx, cy))
        }

        ThemeMotif.MADINAH_ROSE -> {
            // 8 Taç Yapraklı Gül-i Muhammedî
            val petals = 8
            for (i in 0 until petals) {
                val angle = (i * 360f / petals)
                rotate(angle, pivot = Offset(cx, cy)) {
                    val petalPath = Path().apply {
                        moveTo(cx, cy)
                        cubicTo(cx - radius * 0.3f, cy - radius * 0.4f, cx - radius * 0.2f, cy - radius * 0.9f, cx, cy - radius * 0.95f)
                        cubicTo(cx + radius * 0.2f, cy - radius * 0.9f, cx + radius * 0.3f, cy - radius * 0.4f, cx, cy)
                    }
                    drawPath(petalPath, if (i % 2 == 0) primaryColor else accentColor, style = Stroke(width = strokeWidth * 0.8f))
                }
            }
            drawCircle(accentColor, radius = radius * 0.22f, center = Offset(cx, cy), style = Stroke(width = strokeWidth))
            drawCircle(primaryColor, radius = radius * 0.08f, center = Offset(cx, cy))
        }

        ThemeMotif.QUDS_OCTAGON -> {
            // Kubbet-üs Sahrâ Sekizgen Geometrisi
            val sides = 8
            val octPath = Path()
            for (i in 0 until sides) {
                val angle = (i * 360f / sides - 22.5f) * (Math.PI / 180f)
                val x = cx + radius * cos(angle).toFloat()
                val y = cy + radius * sin(angle).toFloat()
                if (i == 0) octPath.moveTo(x, y) else octPath.lineTo(x, y)
            }
            octPath.close()
            drawPath(octPath, primaryColor, style = Stroke(width = strokeWidth))

            // İç sekizgen kubbe çemberi
            drawCircle(accentColor, radius = radius * 0.52f, center = Offset(cx, cy), style = Stroke(width = strokeWidth * 0.9f))
            drawCircle(primaryColor, radius = radius * 0.22f, center = Offset(cx, cy), style = Stroke(width = strokeWidth * 0.8f))
            drawCircle(accentColor, radius = radius * 0.08f, center = Offset(cx, cy))
        }

        ThemeMotif.TEZHIP_MARBLE -> {
            // 4 Yapraklı Asr-ı Saadet Tezhip Madalyonu
            for (i in 0 until 4) {
                rotate(i * 90f, pivot = Offset(cx, cy)) {
                    val leaf = Path().apply {
                        moveTo(cx, cy)
                        cubicTo(cx - radius * 0.4f, cy - radius * 0.3f, cx - radius * 0.3f, cy - radius * 0.85f, cx, cy - radius)
                        cubicTo(cx + radius * 0.3f, cy - radius * 0.85f, cx + radius * 0.4f, cy - radius * 0.3f, cx, cy)
                    }
                    drawPath(leaf, primaryColor, style = Stroke(width = strokeWidth * 0.9f))
                }
            }
            drawCircle(accentColor, radius = radius * 0.25f, center = Offset(cx, cy), style = Stroke(width = strokeWidth))
        }

        ThemeMotif.TOPKAPI_RUMI -> {
            // Üç Benek Çintemani & Rûmî Motifi
            val dotRadius = radius * 0.22f
            // Üst nokta
            drawCircle(accentColor, radius = dotRadius, center = Offset(cx, cy - radius * 0.45f), style = Stroke(width = strokeWidth))
            drawCircle(primaryColor, radius = dotRadius * 0.4f, center = Offset(cx, cy - radius * 0.45f))
            // Sol alt nokta
            drawCircle(primaryColor, radius = dotRadius, center = Offset(cx - radius * 0.42f, cy + radius * 0.35f), style = Stroke(width = strokeWidth))
            drawCircle(accentColor, radius = dotRadius * 0.4f, center = Offset(cx - radius * 0.42f, cy + radius * 0.35f))
            // Sağ alt nokta
            drawCircle(primaryColor, radius = dotRadius, center = Offset(cx + radius * 0.42f, cy + radius * 0.35f), style = Stroke(width = strokeWidth))
            drawCircle(accentColor, radius = dotRadius * 0.4f, center = Offset(cx + radius * 0.42f, cy + radius * 0.35f))

            // Dış çevreleyen rumi dalga çemberi
            drawCircle(accentColor.copy(alpha = 0.6f), radius = radius * 0.95f, center = Offset(cx, cy), style = Stroke(width = strokeWidth * 0.7f))
        }
    }
}
