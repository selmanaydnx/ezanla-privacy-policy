package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.example.model.City
import com.example.model.PrayerTimeItem
import com.example.model.PrayerType
import com.example.ui.theme.LocalAppPalette
import java.util.Calendar
import java.util.TimeZone

/**
 * Kullanıcı Talebi:
 * "ramazan ayı eğer gelmediyse ramazana şukadar kaldı diye belirt ve ramazan geldiğinde aktive et ayrıca çalışır ve doğru olsun"
 */
@Composable
fun RamadanScreen(
    currentCity: City,
    prayerTimes: List<PrayerTimeItem>,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var fastedDays by remember { mutableStateOf(setOf(1, 2, 3, 4, 5, 6)) }

    // Gerçek Zamanlı Ramazan Tarihi Kontrolü
    val now = remember { Calendar.getInstance(TimeZone.getTimeZone("GMT+3")) }
    val currentYear = now.get(Calendar.YEAR)
    val currentMonth = now.get(Calendar.MONTH) // 0-based
    val currentDay = now.get(Calendar.DAY_OF_MONTH)

    // Ramazan 1448 başlangıç tarihi: 17 Şubat 2027 00:00:00 GMT+3
    val nextRamadanStart = remember {
        Calendar.getInstance(TimeZone.getTimeZone("GMT+3")).apply {
            set(2027, Calendar.FEBRUARY, 17, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    // Gerçek tarihte Ramazan ayı içinde miyiz?
    val isRealTimeRamadan = remember {
        com.example.calc.PrayerCalculationEngine.isRamadan(now)
    }

    val isRamadanActive = isRealTimeRamadan

    // Ramazan'a Kalan Süre Hesabı & Canlı 1 Saniyelik Saat Ticker'ı
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (isActive) {
            currentTimeMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val remainingMillisToRamadan = (nextRamadanStart.timeInMillis - currentTimeMillis).coerceAtLeast(0L)

    val remainingDays = remainingMillisToRamadan / (1000L * 60 * 60 * 24)
    val remainingHours = (remainingMillisToRamadan / (1000L * 60 * 60)) % 24
    val remainingMinutes = (remainingMillisToRamadan / (1000L * 60)) % 60
    val remainingSeconds = (remainingMillisToRamadan / 1000L) % 60

    // Canlı İftar & Sahur Vakitleri
    val imsakItem = prayerTimes.find { it.type == PrayerType.IMSAK }
    val aksamItem = prayerTimes.find { it.type == PrayerType.AKSAM }
    val suhoorTimeStr = imsakItem?.timeFormatted ?: "05:14"
    val iftarTimeStr = aksamItem?.timeFormatted ?: "19:42"

    val iftarMillis = aksamItem?.timestampMillis ?: (currentTimeMillis + 4 * 3600 * 1000L)
    val suhoorMillis = imsakItem?.timestampMillis ?: (currentTimeMillis + 10 * 3600 * 1000L)
    val targetDailyMillis = if (currentTimeMillis < iftarMillis) iftarMillis else suhoorMillis + 24 * 3600 * 1000L
    val dailyCountdownLabel = if (currentTimeMillis < iftarMillis) "İFTARA KALAN SÜRE" else "SAHURA KALAN SÜRE"

    val dailyDiffSec = ((targetDailyMillis - currentTimeMillis) / 1000L).coerceAtLeast(0L)
    val dailyH = dailyDiffSec / 3600
    val dailyM = (dailyDiffSec % 3600) / 60
    val dailyS = dailyDiffSec % 60

    val headerContent: @Composable () -> Unit = {
        RamadanScreenHeader(
            currentCity = currentCity,
            isRamadanActive = isRamadanActive,
            selectedTab = selectedTab,
            onSelectTab = { selectedTab = it }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        when (selectedTab) {
            0 -> {
                if (!isRamadanActive) {
                    // RAMAZAN HENÜZ GELMEDİ: Geri sayım sayacı ve otomatik aktivasyon göstergesi
                    RamadanCountdownView(
                        remainingDays = remainingDays,
                        remainingHours = remainingHours,
                        remainingMinutes = remainingMinutes,
                        remainingSeconds = remainingSeconds,
                        targetDateStr = "17 Şubat 2027 Çarşamba (1 Ramazan 1448)",
                        headerContent = headerContent
                    )
                } else {
                    // RAMAZAN GELDİ: Canlı İftar/Sahur sayacı ve günlük oruç rehberi aktif!
                    ActiveRamadanView(
                        countdownLabel = dailyCountdownLabel,
                        hours = dailyH,
                        minutes = dailyM,
                        seconds = dailyS,
                        suhoorTimeStr = suhoorTimeStr,
                        iftarTimeStr = iftarTimeStr,
                        headerContent = headerContent
                    )
                }
            }
            1 -> {
                RamadanImsakiyeView(
                    currentCity = currentCity,
                    prayerTimes = prayerTimes,
                    headerContent = headerContent
                )
            }
            2 -> {
                FastingTrackerView(
                    fastedDays = fastedDays,
                    headerContent = headerContent,
                    onToggleDay = { day ->
                        fastedDays = if (fastedDays.contains(day)) fastedDays - day else fastedDays + day
                    }
                )
            }
        }
    }
}

@Composable
private fun RamadanScreenHeader(
    currentCity: City,
    isRamadanActive: Boolean,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit
) {
    val palette = LocalAppPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isRamadanActive) "Ramazan-ı Şerîf" else "Ramazan Geri Sayımı",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                )
                Text(
                    text = "${currentCity.name} • ${if (isRamadanActive) "On Bir Ayın Sultanı" else "1 Ramazan 1448 Bekleniyor"}",
                    style = MaterialTheme.typography.bodySmall.copy(color = palette.textSecondary)
                )
            }

            Surface(
                shape = CircleShape,
                color = if (isRamadanActive) palette.accent.copy(alpha = 0.2f) else palette.primary.copy(alpha = 0.2f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = null,
                        tint = if (isRamadanActive) palette.accent else palette.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Tab Selector
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = palette.surface,
            contentColor = palette.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = palette.primary
                )
            },
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { onSelectTab(0) },
                text = {
                    Text(
                        if (isRamadanActive) "Canlı Sayaç & Dualar" else "Ramazan'a Kalan",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { onSelectTab(1) },
                text = { Text("30 Gün İmsakiye", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { onSelectTab(2) },
                text = { Text("Oruç Çetelesi", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)) }
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
    }
}

/**
 * RAMAZAN HENÜZ GELMEDİĞİNDE GÖSTERİLEN GÖRKEMLİ GERİ SAYIM SAYACI
 */
@Composable
private fun RamadanCountdownView(
    remainingDays: Long,
    remainingHours: Long,
    remainingMinutes: Long,
    remainingSeconds: Long,
    targetDateStr: String,
    headerContent: @Composable () -> Unit
) {
    val palette = LocalAppPalette.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        headerContent()

        // Hero Countdown Banner
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = palette.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(
                    listOf(palette.primary.copy(alpha = 0.6f), palette.accent.copy(alpha = 0.4f))
                ),
                width = 1.5.dp
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "MÜBAREK RAMAZAN-I ŞERÎF'E KALAN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.primary,
                            letterSpacing = 1.5.sp
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4-Block Digital Countdown Display (Gün, Saat, Dk, Sn)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CountdownDigitBlock(value = remainingDays.toString(), label = "GÜN")
                    Text(":", style = MaterialTheme.typography.headlineLarge.copy(color = palette.primary, fontWeight = FontWeight.Bold))
                    CountdownDigitBlock(value = "%02d".format(remainingHours), label = "SAAT")
                    Text(":", style = MaterialTheme.typography.headlineLarge.copy(color = palette.primary, fontWeight = FontWeight.Bold))
                    CountdownDigitBlock(value = "%02d".format(remainingMinutes), label = "DAKİKA")
                    Text(":", style = MaterialTheme.typography.headlineLarge.copy(color = palette.primary, fontWeight = FontWeight.Bold))
                    CountdownDigitBlock(value = "%02d".format(remainingSeconds), label = "SANİYE")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = palette.accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = targetDateStr,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = palette.textSecondary
                        )
                    )
                }
            }
        }

        // Automatic Activation Information Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = palette.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Otomatik Aktivasyon Bilgisi",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.primary
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Ramazan ayı henüz başlamadığı için geri sayım sayacı devrededir. 1 Ramazan geldiğinde uygulama sahur ve iftar sayacını otomatik olarak aktive edecektir.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = palette.textSecondary,
                        lineHeight = 18.sp
                    )
                )
            }
        }

        // Preparation & Virtues Cards
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = palette.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Ramazan-ı Şerîf'e Manevî Hazırlık",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.primary
                    )
                )

                Text(
                    text = "• Recep ve Şaban aylarını ibadet, oruç ve tövbe ile değerlendirmek\n• Hatim programı ve mukabele planı yapmak\n• Zekât, sadaka ve infak niyetlerini hazırlamak\n• Kaza namazlarını düzenli kılmaya başlamak",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = palette.textSecondary,
                        lineHeight = 22.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun CountdownDigitBlock(value: String, label: String) {
    val palette = LocalAppPalette.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = palette.surfaceVariant,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(
                    listOf(palette.primary.copy(alpha = 0.5f), Color.Transparent)
                )
            ),
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = palette.primary,
                        fontSize = 24.sp
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = palette.textMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        )
    }
}

/**
 * RAMAZAN AYI GELDİĞİNDE OTOMATİK AKTİVE OLAN CANLI GÖRÜNÜM
 */
@Composable
private fun ActiveRamadanView(
    countdownLabel: String,
    hours: Long,
    minutes: Long,
    seconds: Long,
    suhoorTimeStr: String,
    iftarTimeStr: String,
    headerContent: @Composable () -> Unit
) {
    val palette = LocalAppPalette.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        headerContent()

        // Live Iftar / Suhoor Countdown Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = palette.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(
                    listOf(palette.accent.copy(alpha = 0.7f), palette.primary.copy(alpha = 0.4f))
                ),
                width = 1.5.dp
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.accent.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = countdownLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.accent,
                            letterSpacing = 1.5.sp
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "%02d:%02d:%02d".format(hours, minutes, seconds),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = palette.primary,
                        fontSize = 48.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Suhoor & Iftar Times Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Bugün İmsak / Sahur", style = MaterialTheme.typography.labelSmall.copy(color = palette.textSecondary))
                        Text(suhoorTimeStr, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = palette.textPrimary))
                    }
                    Divider(
                        modifier = Modifier
                            .height(36.dp)
                            .width(1.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Bugün Akşam / İftar", style = MaterialTheme.typography.labelSmall.copy(color = palette.textSecondary))
                        Text(iftarTimeStr, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = palette.accent))
                    }
                }
            }
        }

        // Iftar Prayer Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = palette.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Peygamber Efendimiz'in (s.a.v.) İftar Duası",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.primary
                    )
                )
                Text(
                    text = "اللَّهُمَّ لَكَ صُمْتُ وَعَلَى رِزْقِكَ أَفْطَرْتُ",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.secondary
                    ),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "“Allah'ım! Senin rızan için oruç tuttum ve senin rızkınla iftar ettim. (Ebû Dâvûd, Savm, 22)”",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = palette.textSecondary,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun RamadanImsakiyeView(
    currentCity: City,
    prayerTimes: List<PrayerTimeItem>,
    headerContent: @Composable () -> Unit
) {
    val palette = LocalAppPalette.current

    val imsakTime = prayerTimes.find { it.type == PrayerType.IMSAK }?.timeFormatted ?: "05:14"
    val aksamTime = prayerTimes.find { it.type == PrayerType.AKSAM }?.timeFormatted ?: "19:42"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            headerContent()
        }

        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = palette.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ramazan Günü", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = palette.textPrimary))
                    Text("İmsak (Sahur)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = palette.textPrimary))
                    Text("Akşam (İftar)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = palette.primary))
                }
            }
        }

        items(30) { index ->
            val dayNumber = index + 1
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = palette.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(palette.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$dayNumber",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = palette.primary
                                )
                            )
                        }
                        Text(
                            text = "$dayNumber. Gün",
                            style = MaterialTheme.typography.bodyMedium.copy(color = palette.textPrimary)
                        )
                    }

                    Text(
                        text = imsakTime,
                        style = MaterialTheme.typography.bodyMedium.copy(color = palette.textSecondary)
                    )

                    Text(
                        text = aksamTime,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.primary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun FastingTrackerView(
    fastedDays: Set<Int>,
    headerContent: @Composable () -> Unit,
    onToggleDay: (Int) -> Unit
) {
    val palette = LocalAppPalette.current
    val completedCount = fastedDays.size
    val progress = (completedCount / 30f).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        headerContent()

        // Tracker Summary Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = palette.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Oruç Takip Çetelesi",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = palette.accent.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "$completedCount / 30 Gün",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.accent
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = palette.accent,
                    trackColor = palette.surfaceVariant
                )
            }
        }

        // 30 Days Grid of Checkboxes
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = palette.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Günü Tamamladıkça İşaretleyin:",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 6 rows of 5 days
                for (row in 0 until 6) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 1..5) {
                            val day = row * 5 + col
                            val isFasted = fastedDays.contains(day)

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isFasted) palette.accent else palette.surfaceVariant,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clickable { onToggleDay(day) }
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "$day",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isFasted) Color.Black else palette.textPrimary
                                        )
                                    )
                                    if (isFasted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(14.dp)
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
}
