package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.IslamicDatabase
import com.example.model.ReligiousDay
import com.example.ui.theme.LocalAppPalette

@Composable
fun ReligiousDaysScreen(
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current
    val days = IslamicDatabase.religiousDays

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Dini Günler & Kandiller",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                    )
                    Text(
                        text = "Hicrî ve Miladî Önemli Günler Takvimi",
                        style = MaterialTheme.typography.bodySmall.copy(color = palette.textSecondary)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = palette.primary.copy(alpha = 0.2f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Closest Upcoming Day Hero Card
        val nearestDay = days.firstOrNull()
        if (nearestDay != null) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(palette.primary, palette.secondary.copy(alpha = 0.4f))
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Stars, contentDescription = null, tint = palette.primary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "EN YAKIN MÜBAREK VAKİT",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = palette.primary
                                    )
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = palette.accent.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "${nearestDay.daysRemaining} Gün Kaldı",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = palette.accent
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = nearestDay.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = nearestDay.gregorianDate,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = palette.secondary
                                )
                            )
                            Text(
                                text = "• ${nearestDay.hijriDate}",
                                style = MaterialTheme.typography.bodySmall.copy(color = palette.textMuted)
                            )
                        }

                        Text(
                            text = nearestDay.significance,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = palette.textSecondary,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }
        }

        // All Days List
        items(days) { day ->
            ReligiousDayItemCard(day = day)
        }
    }
}

@Composable
private fun ReligiousDayItemCard(day: ReligiousDay) {
    val palette = LocalAppPalette.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("religious_day_${day.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = day.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    ),
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = palette.surfaceVariant
                ) {
                    Text(
                        text = "${day.daysRemaining} gün",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.primary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        maxLines = 1
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = day.gregorianDate,
                    style = MaterialTheme.typography.labelSmall.copy(color = palette.secondary, fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "(${day.hijriDate})",
                    style = MaterialTheme.typography.labelSmall.copy(color = palette.textMuted)
                )
            }

            Text(
                text = day.significance,
                style = MaterialTheme.typography.bodySmall.copy(color = palette.textSecondary, lineHeight = 17.sp)
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = palette.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = palette.accent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Tavsiye: ${day.worshipRecommendation}",
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
