package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NotificationMode
import com.example.model.PrayerTimeItem
import com.example.model.PrayerType
import com.example.ui.theme.LocalAppPalette

@Composable
fun PrayerCardList(
    prayerTimes: List<PrayerTimeItem>,
    notificationSettings: Map<PrayerType, NotificationMode>,
    onToggleNotification: (PrayerType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        prayerTimes.forEach { item ->
            val notifMode = notificationSettings[item.type] ?: NotificationMode.EZAN
            PrayerTimeRowCard(
                item = item,
                notificationMode = notifMode,
                onToggleNotification = { onToggleNotification(item.type) }
            )
        }
    }
}

@Composable
fun PrayerTimeRowCard(
    item: PrayerTimeItem,
    notificationMode: NotificationMode,
    onToggleNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current
    val isHighlighted = item.isNext || item.isCurrent
    var isExpanded by remember { mutableStateOf(false) }

    val containerColor = when {
        item.isCurrent -> palette.surfaceVariant
        else -> palette.surface
    }

    val cardBorder = when {
        item.isNext -> BorderStroke(
            width = 1.5.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(palette.primary, palette.secondary.copy(alpha = 0.8f))
            )
        )
        item.isCurrent -> BorderStroke(
            width = 1.5.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(palette.accent, palette.accent.copy(alpha = 0.7f))
            )
        )
        else -> BorderStroke(
            width = 1.dp,
            color = if (palette.isDark) Color.White.copy(alpha = 0.08f) else palette.textMuted.copy(alpha = 0.22f)
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("prayer_card_${item.type.name.lowercase()}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighlighted) 3.dp else 1.dp),
        border = cardBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = 16.dp, vertical = 13.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Icon + Title & Description
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f, fill = true).padding(end = 8.dp)
                ) {
                    // Icon Avatar
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            item.isCurrent -> palette.accent.copy(alpha = 0.22f)
                            item.isNext -> palette.primary.copy(alpha = 0.18f)
                            item.isPassed -> palette.surfaceVariant.copy(alpha = 0.7f)
                            else -> palette.surfaceVariant
                        },
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = getPrayerIcon(item.type),
                                contentDescription = item.type.titleTr,
                                tint = when {
                                    item.isCurrent -> palette.accent
                                    item.isNext -> palette.primary
                                    item.isPassed -> palette.textMuted
                                    else -> palette.textSecondary
                                },
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Title and status tag
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f, fill = true)
                    ) {
                        val rowTitle = when (item.type) {
                            PrayerType.IMSAK -> "İmsak"
                            PrayerType.GUNES -> "Güneş"
                            else -> item.type.titleTr
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = rowTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (item.isPassed) palette.textSecondary else palette.textPrimary,
                                    fontSize = 15.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (item.isNext) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = palette.primary.copy(alpha = 0.18f),
                                    border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = if (item.type == PrayerType.GUNES) "GÜNDOĞUMU" else "SIRADAKİ",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = palette.primary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else if (item.isCurrent) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = palette.accent.copy(alpha = 0.22f),
                                    border = BorderStroke(1.dp, palette.accent.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = if (item.type == PrayerType.GUNES) "KERÂHAT" else "ŞİMDİKİ",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = palette.accent,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else if (item.isPassed) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = palette.surfaceVariant.copy(alpha = 0.7f)
                                ) {
                                    Text(
                                        text = "GEÇTİ",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = palette.textMuted,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        val isRamadanNow = com.example.calc.PrayerCalculationEngine.isRamadan()
                        val rowSubtitle = when (item.type) {
                            PrayerType.IMSAK -> if (isRamadanNow) "Sabah namazı & sahur sonu" else "Sabah namazı vakti"
                            PrayerType.GUNES -> "Güneş doğuşu & sabah sonu"
                            PrayerType.OGLE -> "Öğle namazı vakti"
                            PrayerType.IKINDI -> "İkindi namazı vakti"
                            PrayerType.AKSAM -> if (isRamadanNow) "Akşam namazı & iftar vakti" else "Akşam namazı vakti"
                            PrayerType.YATSI -> "Yatsı namazı vakti"
                        }

                        Text(
                            text = rowSubtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = palette.textMuted,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Right: Time + Notification bell toggle (Fixed width and consistent alignment)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = item.timeFormatted,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = when {
                                item.isNext -> palette.primary
                                item.isCurrent -> palette.accent
                                item.isPassed -> palette.textSecondary
                                else -> palette.textPrimary
                            },
                            letterSpacing = 0.5.sp,
                            fontSize = 19.sp
                        ),
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(62.dp),
                        maxLines = 1,
                        softWrap = false
                    )

                    // Notification Mode Action Button (Consistent icon style, size, alignment, and active/inactive state)
                    val isNotificationActive = notificationMode != NotificationMode.SILENT
                    val (iconVector, iconTint, bgTint) = when (notificationMode) {
                        NotificationMode.EZAN -> Triple(
                            Icons.Default.NotificationsActive,
                            palette.primary,
                            palette.primary.copy(alpha = 0.16f)
                        )
                        NotificationMode.BEEP -> Triple(
                            Icons.Default.Notifications,
                            palette.secondary,
                            palette.secondary.copy(alpha = 0.16f)
                        )
                        NotificationMode.SILENT -> Triple(
                            Icons.Outlined.NotificationsOff,
                            palette.textMuted,
                            palette.surfaceVariant.copy(alpha = 0.5f)
                        )
                    }

                    IconButton(
                        onClick = onToggleNotification,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(bgTint)
                            .testTag("notif_toggle_${item.type.name.lowercase()}")
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = if (isNotificationActive) "Bildirim Açık" else "Bildirim Kapalı",
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Expandable Detail section with rakat information
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.surfaceVariant.copy(alpha = 0.4f))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val prayerDetails = getPrayerDetailInfo(item.type)
                    Text(
                        text = prayerDetails.first,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.secondary
                        )
                    )
                    Text(
                        text = prayerDetails.second,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = palette.textSecondary,
                            lineHeight = 18.sp,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

private fun getPrayerDetailInfo(type: PrayerType): Pair<String, String> {
    return when (type) {
        PrayerType.IMSAK -> Pair(
            "Sabah Namazı (4 Rekat)",
            "2 rekat sünnet + 2 rekat farz. 'Sabah namazının iki rekat sünneti, dünya ve içindekilerden daha hayırlıdır.'"
        )
        PrayerType.GUNES -> Pair(
            "İşrak Vakti / Kerâhat Sonu",
            "Güneş doğduktan yaklaşık 45 dakika sonra kerahat vakti çıkar ve İşrak (Kuşluk/Duhâ) nafile namazı kılınabilir."
        )
        PrayerType.OGLE -> Pair(
            "Öğle Namazı (10 Rekat)",
            "4 rekat ilk sünnet + 4 rekat farz + 2 rekat son sünnet. Güneşin zeval vaktinden sonra ikindiye kadar kılınır."
        )
        PrayerType.IKINDI -> Pair(
            "İkindi Namazı (8 Rekat)",
            "4 rekat gayr-i müekkede sünnet + 4 rekat farz. Kur'an'da 'orta namaz' olarak zikredilen faziletli namazdır."
        )
        PrayerType.AKSAM -> Pair(
            "Akşam Namazı (5 Rekat)",
            "3 rekat farz + 2 rekat son sünnet. Güneş battıktan hemen sonra kılınması müstehabdır. Ardından Evvâbîn kılınabilir."
        )
        PrayerType.YATSI -> Pair(
            "Yatsı Namazı (10 Rekat + 3 Vitir)",
            "4 rekat ilk sünnet + 4 rekat farz + 2 rekat son sünnet + 3 rekat Vâcip Vitir namazı."
        )
    }
}

private fun getPrayerIcon(type: PrayerType): ImageVector {
    return when (type) {
        PrayerType.IMSAK -> Icons.Default.Bedtime
        PrayerType.GUNES -> Icons.Default.WbTwilight
        PrayerType.OGLE -> Icons.Default.WbSunny
        PrayerType.IKINDI -> Icons.Default.LightMode
        PrayerType.AKSAM -> Icons.Default.Brightness6
        PrayerType.YATSI -> Icons.Default.NightlightRound
    }
}
