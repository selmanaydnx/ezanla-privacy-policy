package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.DailyPrayerCheck
import com.example.model.KazaBookEntry
import com.example.model.KazaTracker
import com.example.model.PrayerTimeItem
import com.example.model.PrayerType
import com.example.ui.components.KazaProgressSection
import com.example.ui.theme.LocalAppPalette

/**
 * Kullanıcı Talepleri:
 * "namaz ve kaza takibi kısmındaki defterde kişinin yaptığı işlemler kaydedilsin uygulamadan çıktığında sıfırlanmasın"
 * "ve vakit geçtiğinde o vakti kılınmadı olarak işaretleyip otomatik kaza defterine atsın"
 */
@Composable
fun PrayerTrackerScreen(
    dailyCheck: DailyPrayerCheck,
    kazaTracker: KazaTracker,
    kazaBookEntries: List<KazaBookEntry> = emptyList(),
    onToggleDailyPrayer: (String) -> Unit,
    onUpdateKazaTracker: (KazaTracker) -> Unit,
    onLogKazaAction: (title: String, description: String, changeAmount: Int, prayerName: String, newTracker: KazaTracker) -> Unit = { _, _, _, _, _ -> },
    onClearKazaBook: () -> Unit = {},
    prayerTimes: List<PrayerTimeItem> = emptyList(),
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current
    val scrollState = rememberScrollState()

    var showResetDialog by remember { mutableStateOf(false) }
    var showClearBookDialog by remember { mutableStateOf(false) }
    var editPrayerTarget by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var editInputValue by remember { mutableStateOf("") }

    val now = System.currentTimeMillis()
    val gunesItem = prayerTimes.find { it.type == PrayerType.GUNES }
    val ikindiItem = prayerTimes.find { it.type == PrayerType.IKINDI }
    val aksamItem = prayerTimes.find { it.type == PrayerType.AKSAM }
    val yatsiItem = prayerTimes.find { it.type == PrayerType.YATSI }

    val fajrPassed = gunesItem != null && now >= gunesItem.timestampMillis
    val dhuhrPassed = ikindiItem != null && now >= ikindiItem.timestampMillis
    val asrPassed = aksamItem != null && now >= aksamItem.timestampMillis
    val maghribPassed = yatsiItem != null && now >= yatsiItem.timestampMillis
    val ishaPassed = false // Yatsı gün bitimi / sabah imsak ile tamamlanır

    val animatedProgress by animateFloatAsState(
        targetValue = dailyCheck.progress,
        animationSpec = tween(400),
        label = "dailyProgress"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Namaz & Kaza Takibi",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                )
                Text(
                    text = "Günlük vakit namazları ve kalıcı kaza defteri",
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
                        imageVector = Icons.Default.FactCheck,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Automatic Missed Prayer -> Kaza Info Banner
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = palette.surfaceVariant.copy(alpha = 0.7f),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(palette.primary.copy(alpha = 0.5f), Color.Transparent)
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = palette.primary.copy(alpha = 0.2f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoMode,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Otomatik Kaza Aktarımı & Kalıcı Hafıza",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.primary
                        )
                    )
                    Text(
                        text = "Vakti geçen ve kılınmadı olarak kalan vakitler otomatik olarak kaza defterinize eklenir. Defterdeki tüm sayı ve işlemleriniz telefonunuzda kalıcı olarak saklanır.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = palette.textSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Today's 5 Prayers Progress Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = palette.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(palette.primary.copy(alpha = 0.4f), palette.accent.copy(alpha = 0.2f))
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bugünkü Vakit Namazları",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (dailyCheck.completedCount == 5) palette.accent.copy(alpha = 0.2f) else palette.surfaceVariant
                    ) {
                        Text(
                            text = "${dailyCheck.completedCount} / 5 Vakit Kılındı",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (dailyCheck.completedCount == 5) palette.accent else palette.primary
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Progress Bar
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    color = palette.accent,
                    trackColor = palette.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                if (dailyCheck.completedCount == 5) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = palette.accent.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = palette.accent, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Elhamdülillah! Bugünün tüm vakit namazlarını kıldınız.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = palette.accent
                                )
                            )
                        }
                    }
                }

                // 5 Prayer Toggles
                PrayerCheckItem(
                    name = "Sabah Namazı",
                    detail = "2 Sünnet + 2 Farz",
                    isChecked = dailyCheck.fajr,
                    isPassed = fajrPassed,
                    onToggle = { onToggleDailyPrayer("fajr") }
                )
                PrayerCheckItem(
                    name = "Öğle Namazı",
                    detail = "4 İlk Sünnet + 4 Farz + 2 Son Sünnet",
                    isChecked = dailyCheck.dhuhr,
                    isPassed = dhuhrPassed,
                    onToggle = { onToggleDailyPrayer("dhuhr") }
                )
                PrayerCheckItem(
                    name = "İkindi Namazı",
                    detail = "4 Sünnet + 4 Farz",
                    isChecked = dailyCheck.asr,
                    isPassed = asrPassed,
                    onToggle = { onToggleDailyPrayer("asr") }
                )
                PrayerCheckItem(
                    name = "Akşam Namazı",
                    detail = "3 Farz + 2 Sünnet",
                    isChecked = dailyCheck.maghrib,
                    isPassed = maghribPassed,
                    onToggle = { onToggleDailyPrayer("maghrib") }
                )
                PrayerCheckItem(
                    name = "Yatsı & Vitir",
                    detail = "4 İlk Sünnet + 4 Farz + 2 Son Sünnet + 3 Vitir",
                    isChecked = dailyCheck.isha,
                    isPassed = ishaPassed,
                    onToggle = { onToggleDailyPrayer("isha") }
                )
            }
        }

        // Kaza Namazı Borç Erime İlerlemesi & Grafiği (Dairesel Gösterge & Gün Gün Çubuklar)
        KazaProgressSection(
            kazaTracker = kazaTracker,
            kazaBookEntries = kazaBookEntries
        )

        // Kaza Namazları Section (User manages and updates, persists across restarts)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = palette.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(palette.accent.copy(alpha = 0.3f), Color.Transparent)
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Kaza Namazı Borç Defteri",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                        )
                        Text(
                            text = "Toplam ${kazaTracker.total} Vakit (${kazaTracker.totalDays} Günlük Kaza)",
                            style = MaterialTheme.typography.labelSmall.copy(color = palette.secondary)
                        )
                    }

                    // Reset button
                    IconButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Sıfırla",
                            tint = palette.textMuted
                        )
                    }
                }

                // Quick Batch Action Buttons: -1 Gün, +1 Gün, +30 Gün
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // -1 Gün Düş
                    Button(
                        onClick = {
                            val updated = KazaTracker(
                                fajr = (kazaTracker.fajr - 1).coerceAtLeast(0),
                                dhuhr = (kazaTracker.dhuhr - 1).coerceAtLeast(0),
                                asr = (kazaTracker.asr - 1).coerceAtLeast(0),
                                maghrib = (kazaTracker.maghrib - 1).coerceAtLeast(0),
                                isha = (kazaTracker.isha - 1).coerceAtLeast(0),
                                witr = (kazaTracker.witr - 1).coerceAtLeast(0)
                            )
                            onLogKazaAction(
                                "1 Günlük Kaza Kılındı",
                                "Tüm vakitlerden 1'er gün kaza namazı eda edildi (-6 Vakit)",
                                -6,
                                "Tüm Vakitler",
                                updated
                            )
                        },
                        enabled = kazaTracker.total > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Text("-1 Gün Düş", style = MaterialTheme.typography.labelSmall.copy(color = Color.Black, fontWeight = FontWeight.Bold))
                    }

                    // +1 Gün Ekle
                    OutlinedButton(
                        onClick = {
                            val updated = KazaTracker(
                                fajr = kazaTracker.fajr + 1,
                                dhuhr = kazaTracker.dhuhr + 1,
                                asr = kazaTracker.asr + 1,
                                maghrib = kazaTracker.maghrib + 1,
                                isha = kazaTracker.isha + 1,
                                witr = kazaTracker.witr + 1
                            )
                            onLogKazaAction(
                                "1 Günlük Kaza Borcu Eklendi",
                                "Tüm vakitlere 1'er gün kaza borcu eklendi (+6 Vakit)",
                                6,
                                "Tüm Vakitler",
                                updated
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Text("+1 Gün Ekle", style = MaterialTheme.typography.labelSmall.copy(color = palette.textPrimary, fontWeight = FontWeight.SemiBold))
                    }

                    // +30 Gün (1 Ay)
                    OutlinedButton(
                        onClick = {
                            val updated = KazaTracker(
                                fajr = kazaTracker.fajr + 30,
                                dhuhr = kazaTracker.dhuhr + 30,
                                asr = kazaTracker.asr + 30,
                                maghrib = kazaTracker.maghrib + 30,
                                isha = kazaTracker.isha + 30,
                                witr = kazaTracker.witr + 30
                            )
                            onLogKazaAction(
                                "30 Günlük (1 Ay) Kaza Eklendi",
                                "Tüm vakitlere 30 günlük kaza borcu eklendi (+180 Vakit)",
                                180,
                                "Tüm Vakitler",
                                updated
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Text("+1 Ay (30G)", style = MaterialTheme.typography.labelSmall.copy(color = palette.textPrimary, fontWeight = FontWeight.SemiBold))
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                // Individual Kaza Counter Rows
                KazaRow(
                    name = "Sabah Namazı Kazası",
                    count = kazaTracker.fajr,
                    onUpdate = { newCount ->
                        val diff = newCount - kazaTracker.fajr
                        val title = if (diff < 0) "Sabah Kazası Kılındı" else "Sabah Kazası Eklendi"
                        val desc = if (diff < 0) "1 vakit Sabah namazı kazası eda edildi (-1)" else "1 vakit Sabah kazası eklendi (+1)"
                        onLogKazaAction(title, desc, diff, "Sabah Namazı", kazaTracker.copy(fajr = newCount))
                    },
                    onEditDirect = { editPrayerTarget = Pair("Sabah Namazı Kazası", kazaTracker.fajr); editInputValue = kazaTracker.fajr.toString() }
                )
                KazaRow(
                    name = "Öğle Namazı Kazası",
                    count = kazaTracker.dhuhr,
                    onUpdate = { newCount ->
                        val diff = newCount - kazaTracker.dhuhr
                        val title = if (diff < 0) "Öğle Kazası Kılındı" else "Öğle Kazası Eklendi"
                        val desc = if (diff < 0) "1 vakit Öğle namazı kazası eda edildi (-1)" else "1 vakit Öğle kazası eklendi (+1)"
                        onLogKazaAction(title, desc, diff, "Öğle Namazı", kazaTracker.copy(dhuhr = newCount))
                    },
                    onEditDirect = { editPrayerTarget = Pair("Öğle Namazı Kazası", kazaTracker.dhuhr); editInputValue = kazaTracker.dhuhr.toString() }
                )
                KazaRow(
                    name = "İkindi Namazı Kazası",
                    count = kazaTracker.asr,
                    onUpdate = { newCount ->
                        val diff = newCount - kazaTracker.asr
                        val title = if (diff < 0) "İkindi Kazası Kılındı" else "İkindi Kazası Eklendi"
                        val desc = if (diff < 0) "1 vakit İkindi namazı kazası eda edildi (-1)" else "1 vakit İkindi kazası eklendi (+1)"
                        onLogKazaAction(title, desc, diff, "İkindi Namazı", kazaTracker.copy(asr = newCount))
                    },
                    onEditDirect = { editPrayerTarget = Pair("İkindi Namazı Kazası", kazaTracker.asr); editInputValue = kazaTracker.asr.toString() }
                )
                KazaRow(
                    name = "Akşam Namazı Kazası",
                    count = kazaTracker.maghrib,
                    onUpdate = { newCount ->
                        val diff = newCount - kazaTracker.maghrib
                        val title = if (diff < 0) "Akşam Kazası Kılındı" else "Akşam Kazası Eklendi"
                        val desc = if (diff < 0) "1 vakit Akşam namazı kazası eda edildi (-1)" else "1 vakit Akşam kazası eklendi (+1)"
                        onLogKazaAction(title, desc, diff, "Akşam Namazı", kazaTracker.copy(maghrib = newCount))
                    },
                    onEditDirect = { editPrayerTarget = Pair("Akşam Namazı Kazası", kazaTracker.maghrib); editInputValue = kazaTracker.maghrib.toString() }
                )
                KazaRow(
                    name = "Yatsı Namazı Kazası",
                    count = kazaTracker.isha,
                    onUpdate = { newCount ->
                        val diff = newCount - kazaTracker.isha
                        val title = if (diff < 0) "Yatsı Kazası Kılındı" else "Yatsı Kazası Eklendi"
                        val desc = if (diff < 0) "1 vakit Yatsı namazı kazası eda edildi (-1)" else "1 vakit Yatsı kazası eklendi (+1)"
                        onLogKazaAction(title, desc, diff, "Yatsı Namazı", kazaTracker.copy(isha = newCount))
                    },
                    onEditDirect = { editPrayerTarget = Pair("Yatsı Namazı Kazası", kazaTracker.isha); editInputValue = kazaTracker.isha.toString() }
                )
                KazaRow(
                    name = "Vitir Vacip Kazası",
                    count = kazaTracker.witr,
                    onUpdate = { newCount ->
                        val diff = newCount - kazaTracker.witr
                        val title = if (diff < 0) "Vitir Kazası Kılındı" else "Vitir Kazası Eklendi"
                        val desc = if (diff < 0) "1 vakit Vitir vacip kazası eda edildi (-1)" else "1 vakit Vitir vacip kazası eklendi (+1)"
                        onLogKazaAction(title, desc, diff, "Vitir Vacip", kazaTracker.copy(witr = newCount))
                    },
                    onEditDirect = { editPrayerTarget = Pair("Vitir Vacip Kazası", kazaTracker.witr); editInputValue = kazaTracker.witr.toString() }
                )
            }
        }

        // Kaza Defteri & İşlem Geçmişi (Uygulamadan çıkıldığında asla sıfırlanmaz)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = palette.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(palette.primary.copy(alpha = 0.35f), Color.Transparent)
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = palette.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Kaza İşlem & Borç Defteri",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary
                                )
                            )
                        }
                        Text(
                            text = "Yapılan tüm işlemler ve vakti geçen namazlar burada kalıcı saklanır (${kazaBookEntries.size} Kayıt)",
                            style = MaterialTheme.typography.bodySmall.copy(color = palette.textSecondary, fontSize = 11.sp)
                        )
                    }

                    if (kazaBookEntries.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearBookDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Defteri Temizle",
                                tint = palette.textMuted
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                if (kazaBookEntries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HistoryEdu,
                                contentDescription = null,
                                tint = palette.textMuted.copy(alpha = 0.6f),
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Henüz kaza işlem kaydı bulunmuyor",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = palette.textMuted
                                )
                            )
                            Text(
                                text = "Kaza kıldığınızda veya vakti geçen namazlar olduğunda defterinize otomatik kaydedilir.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = palette.textSecondary.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        kazaBookEntries.take(25).forEach { entry ->
                            KazaBookEntryRow(entry = entry)
                        }
                    }
                }
            }
        }
    }

    // Direct Edit Number Dialog
    editPrayerTarget?.let { (title, currentVal) ->
        AlertDialog(
            onDismissRequest = { editPrayerTarget = null },
            title = { Text(title, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Borçlu olduğunuz vakit sayısını doğrudan giriniz:")
                    OutlinedTextField(
                        value = editInputValue,
                        onValueChange = { editInputValue = it.filter { ch -> ch.isDigit() } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = editInputValue.toIntOrNull() ?: 0
                        val (updatedTracker, prayerName, diff) = when (title) {
                            "Sabah Namazı Kazası" -> Triple(kazaTracker.copy(fajr = num), "Sabah Namazı", num - kazaTracker.fajr)
                            "Öğle Namazı Kazası" -> Triple(kazaTracker.copy(dhuhr = num), "Öğle Namazı", num - kazaTracker.dhuhr)
                            "İkindi Namazı Kazası" -> Triple(kazaTracker.copy(asr = num), "İkindi Namazı", num - kazaTracker.asr)
                            "Akşam Namazı Kazası" -> Triple(kazaTracker.copy(maghrib = num), "Akşam Namazı", num - kazaTracker.maghrib)
                            "Yatsı Namazı Kazası" -> Triple(kazaTracker.copy(isha = num), "Yatsı Namazı", num - kazaTracker.isha)
                            "Vitir Vacip Kazası" -> Triple(kazaTracker.copy(witr = num), "Vitir Vacip", num - kazaTracker.witr)
                            else -> Triple(kazaTracker, "", 0)
                        }
                        if (diff != 0) {
                            val actTitle = if (diff < 0) "$prayerName Kazası Kılındı" else "$prayerName Borcu Güncellendi"
                            val desc = "Kaza adedi doğrudan $num olarak güncellendi (${if (diff > 0) "+$diff" else "$diff"} vakit)"
                            onLogKazaAction(actTitle, desc, diff, prayerName, updatedTracker)
                        }
                        editPrayerTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.primary)
                ) {
                    Text("Kaydet", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editPrayerTarget = null }) {
                    Text("İptal", color = palette.textSecondary)
                }
            }
        )
    }

    // Reset Confirm Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Kaza Borçlarını Sıfırla") },
            text = { Text("Tüm kaza namazı borç çetelenizi 0 yapmak istediğinizden emin misiniz?") },
            confirmButton = {
                Button(
                    onClick = {
                        onLogKazaAction(
                            "Kaza Defteri Sıfırlandı",
                            "Tüm kaza borçları sıfırlandı",
                            -kazaTracker.total,
                            "Tüm Vakitler",
                            KazaTracker(0, 0, 0, 0, 0, 0)
                        )
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Evet, Sıfırla", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("İptal", color = palette.textSecondary)
                }
            }
        )
    }

    // Clear Journal Book Dialog
    if (showClearBookDialog) {
        AlertDialog(
            onDismissRequest = { showClearBookDialog = false },
            title = { Text("İşlem Günlüğünü Temizle") },
            text = { Text("Kaza defterindeki işlem kayıt geçmişini temizlemek istediğinizden emin misiniz? (Mevcut kaza borç sayılarınız değişmez)") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearKazaBook()
                        showClearBookDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Temizle", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearBookDialog = false }) {
                    Text("İptal", color = palette.textSecondary)
                }
            }
        )
    }
}

@Composable
private fun PrayerCheckItem(
    name: String,
    detail: String,
    isChecked: Boolean,
    isPassed: Boolean = false,
    onToggle: () -> Unit
) {
    val palette = LocalAppPalette.current

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isChecked) palette.accent.copy(alpha = 0.12f) else palette.surfaceVariant,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (isChecked) listOf(palette.accent.copy(alpha = 0.4f), Color.Transparent)
                else listOf(Color.White.copy(alpha = 0.05f), Color.Transparent)
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (isChecked) palette.accent else palette.textPrimary
                        )
                    )
                    if (!isChecked && isPassed) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = palette.accent.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Vakti Çıktı • Kazaya Eklendi",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = palette.accent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (isChecked) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = palette.primary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Kılındı ✓",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = palette.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = palette.textSecondary,
                        fontSize = 11.sp
                    )
                )
            }

            Checkbox(
                checked = isChecked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = palette.accent,
                    checkmarkColor = Color(0xFF1B1B1B)
                )
            )
        }
    }
}

@Composable
private fun KazaRow(
    name: String,
    count: Int,
    onUpdate: (Int) -> Unit,
    onEditDirect: () -> Unit
) {
    val palette = LocalAppPalette.current

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = palette.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = palette.textPrimary
                    )
                )
                Text(
                    text = "$count Vakit",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (count > 0) palette.secondary else palette.textMuted
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // -1
                FilledIconButton(
                    onClick = { onUpdate((count - 1).coerceAtLeast(0)) },
                    enabled = count > 0,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = palette.surface),
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "-1", tint = palette.textPrimary, modifier = Modifier.size(16.dp))
                }

                // Direct Click to Edit
                Surface(
                    onClick = onEditDirect,
                    shape = RoundedCornerShape(8.dp),
                    color = palette.surface,
                    modifier = Modifier
                        .height(34.dp)
                        .padding(horizontal = 2.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (count > 0) palette.primary else palette.textSecondary
                            )
                        )
                    }
                }

                // +1
                FilledIconButton(
                    onClick = { onUpdate(count + 1) },
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = palette.primary),
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "+1", tint = Color.Black, modifier = Modifier.size(16.dp))
                }

                // +5
                Surface(
                    onClick = { onUpdate(count + 5) },
                    shape = RoundedCornerShape(8.dp),
                    color = palette.primary.copy(alpha = 0.2f),
                    modifier = Modifier.height(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
                        Text("+5", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = palette.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun KazaBookEntryRow(entry: KazaBookEntry) {
    val palette = LocalAppPalette.current
    val isDeduction = entry.changeAmount < 0
    val isAuto = entry.isAutoMissed

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = palette.surfaceVariant,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                when {
                    isAuto -> listOf(palette.accent.copy(alpha = 0.35f), Color.Transparent)
                    isDeduction -> listOf(palette.primary.copy(alpha = 0.35f), Color.Transparent)
                    else -> listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
                }
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isAuto -> palette.accent.copy(alpha = 0.18f)
                            isDeduction -> palette.primary.copy(alpha = 0.18f)
                            else -> palette.surface
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isAuto -> Icons.Default.Schedule
                        isDeduction -> Icons.Default.CheckCircle
                        else -> Icons.Default.PostAdd
                    },
                    contentDescription = null,
                    tint = when {
                        isAuto -> palette.accent
                        isDeduction -> palette.primary
                        else -> palette.textPrimary
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary,
                            fontSize = 13.sp
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when {
                            isAuto -> palette.accent.copy(alpha = 0.2f)
                            isDeduction -> palette.primary.copy(alpha = 0.2f)
                            else -> palette.surface
                        }
                    ) {
                        Text(
                            text = when {
                                isAuto -> "OTOMATİK (+${entry.changeAmount})"
                                isDeduction -> "${entry.changeAmount} VAKİT"
                                else -> "+${entry.changeAmount} VAKİT"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = when {
                                    isAuto -> palette.accent
                                    isDeduction -> palette.primary
                                    else -> palette.textSecondary
                                }
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = entry.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = palette.textSecondary,
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = entry.dateFormatted,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = palette.textMuted,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

