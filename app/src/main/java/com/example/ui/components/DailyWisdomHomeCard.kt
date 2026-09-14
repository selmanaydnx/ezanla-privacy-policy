package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailyWisdom
import com.example.ui.theme.LocalAppPalette

enum class WisdomMode {
    AYET,
    HADIS
}

@Composable
fun DailyWisdomHomeCard(
    wisdom: DailyWisdom,
    onNextWisdom: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current
    val context = LocalContext.current
    var selectedMode by remember { mutableStateOf(WisdomMode.AYET) }
    var refreshAngle by remember { mutableFloatStateOf(0f) }

    val animatedRotation by animateFloatAsState(
        targetValue = refreshAngle,
        animationSpec = tween(durationMillis = 400),
        label = "refreshRotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_wisdom_home_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    palette.primary.copy(alpha = 0.4f),
                    palette.accent.copy(alpha = 0.2f),
                    Color.Transparent
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Row: Title badge on left, Action buttons (Refresh, Copy, Share) on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.primary.copy(alpha = 0.15f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(palette.primary.copy(alpha = 0.4f), Color.Transparent)
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "GÜNÜN HİKMETİ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.primary,
                                letterSpacing = 0.8.sp
                            )
                        )
                    }
                }

                // 3 Action Buttons: Refresh, Copy, Share
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Refresh
                    Surface(
                        onClick = {
                            refreshAngle += 360f
                            onNextWisdom()
                        },
                        shape = CircleShape,
                        color = palette.surfaceVariant,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("refresh_wisdom_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Farklı Âyet/Hadis",
                                tint = palette.textSecondary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .rotate(animatedRotation)
                            )
                        }
                    }

                    // Copy
                    Surface(
                        onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val textToCopy = if (selectedMode == WisdomMode.AYET) {
                                "${wisdom.ayahArabic}\n\n“${wisdom.ayahTurkish}”\n(${wisdom.ayahSurah})"
                            } else {
                                "Hz. Muhammed (s.a.v.) şöyle buyurdu:\n\n“${wisdom.hadithTurkish}”\n(Kaynak: ${wisdom.hadithSource})"
                            }
                            cm.setPrimaryClip(ClipData.newPlainText("Günün Hikmeti", textToCopy))
                            Toast.makeText(context, "Metin panoya kopyalandı", Toast.LENGTH_SHORT).show()
                        },
                        shape = CircleShape,
                        color = palette.surfaceVariant,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("copy_wisdom_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Kopyala",
                                tint = palette.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Share
                    Surface(
                        onClick = {
                            val shareBody = if (selectedMode == WisdomMode.AYET) {
                                "${wisdom.ayahArabic}\n\n“${wisdom.ayahTurkish}”\n(${wisdom.ayahSurah})\n\n— Ezanla"
                            } else {
                                "Hz. Peygamber (s.a.v.) şöyle buyurdu:\n\n“${wisdom.hadithTurkish}”\n(${wisdom.hadithSource})\n\n— Ezanla"
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Günün Hikmeti")
                                putExtra(Intent.EXTRA_TEXT, shareBody)
                            }
                            context.startActivity(Intent.createChooser(intent, "Paylaş"))
                        },
                        shape = CircleShape,
                        color = palette.surfaceVariant,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("share_wisdom_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Paylaş",
                                tint = palette.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Balanced Full-Width Segmented Tab Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.surfaceVariant)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                WisdomSegmentedTab(
                    text = "Âyet-i Kerîme",
                    icon = Icons.Default.MenuBook,
                    isSelected = selectedMode == WisdomMode.AYET,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedMode = WisdomMode.AYET }
                )
                WisdomSegmentedTab(
                    text = "Hadîs-i Şerîf",
                    icon = Icons.Default.FormatQuote,
                    isSelected = selectedMode == WisdomMode.HADIS,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedMode = WisdomMode.HADIS }
                )
            }

            // Content Transition
            AnimatedContent(
                targetState = selectedMode,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                },
                label = "wisdomContentAnim"
            ) { mode ->
                when (mode) {
                    WisdomMode.AYET -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Arabic Calligraphy
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = palette.surfaceVariant.copy(alpha = 0.5f),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(
                                        listOf(palette.primary.copy(alpha = 0.3f), Color.Transparent)
                                    )
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = wisdom.ayahArabic,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = palette.secondary,
                                        fontSize = 19.sp,
                                        lineHeight = 30.sp
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp)
                                )
                            }

                            // Turkish Meaning
                            Text(
                                text = "“${wisdom.ayahTurkish}”",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = palette.textPrimary,
                                    lineHeight = 22.sp,
                                    fontSize = 14.sp
                                ),
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )

                            // Surah Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = palette.accent.copy(alpha = 0.15f),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(
                                        listOf(palette.accent.copy(alpha = 0.4f), Color.Transparent)
                                    )
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = palette.accent,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = wisdom.ayahSurah,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = palette.accent,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    WisdomMode.HADIS -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Prophet quote intro tag
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = palette.primary.copy(alpha = 0.2f),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Mosque,
                                            contentDescription = null,
                                            tint = palette.primary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Resûlullah (sallallâhu aleyhi ve sellem) buyurdu:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = palette.primary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                )
                            }

                            // Hadith text
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = palette.surfaceVariant.copy(alpha = 0.5f),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(
                                        listOf(palette.accent.copy(alpha = 0.25f), Color.Transparent)
                                    )
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "“${wisdom.hadithTurkish}”",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = palette.textPrimary,
                                        lineHeight = 22.sp,
                                        fontSize = 14.sp
                                    ),
                                    modifier = Modifier.padding(14.dp)
                                )
                            }

                            // Hadith Source Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = palette.primary.copy(alpha = 0.15f),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(
                                        listOf(palette.primary.copy(alpha = 0.4f), Color.Transparent)
                                    )
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BookmarkBorder,
                                        contentDescription = null,
                                        tint = palette.secondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Kaynak: ${wisdom.hadithSource}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = palette.secondary,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WisdomSegmentedTab(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val palette = LocalAppPalette.current

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(11.dp),
        color = if (isSelected) palette.primary else Color.Transparent,
        modifier = modifier.height(36.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF1E1400) else palette.textSecondary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color(0xFF1E1400) else palette.textSecondary,
                    fontSize = 12.sp
                )
            )
        }
    }
}
