package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyPrayersDatabase
import com.example.model.CategorizedDua
import com.example.model.DailyWisdom
import com.example.model.DuaCategory
import com.example.ui.theme.LocalAppPalette

@Composable
fun WisdomAndPrayersScreen(
    wisdom: DailyWisdom,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = LocalAppPalette.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val headerContent: @Composable () -> Unit = {
        WisdomAndPrayersHeader(
            selectedTabIndex = selectedTabIndex,
            onTabSelected = { selectedTabIndex = it }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        when (selectedTabIndex) {
            0 -> WisdomTabContent(wisdom = wisdom, headerContent = headerContent)
            1 -> CategorizedDuasTabContent(headerContent = headerContent)
        }
    }
}

@Composable
private fun WisdomAndPrayersHeader(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val palette = LocalAppPalette.current
    Column {
        // Top Header
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = "Dualar & Vakitler Rehberi",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
            )
            Text(
                text = "Günün âyet ve hadîsleri ile kategorize edilmiş meâsir dualar",
                style = MaterialTheme.typography.bodySmall.copy(color = palette.textMuted)
            )
        }

        // Tabs Header
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = palette.surface,
            contentColor = palette.primary,
            divider = {
                HorizontalDivider(color = if (palette.isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f))
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { onTabSelected(0) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Günün Hikmeti", fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { onTabSelected(1) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Günlük Dualar", fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun WisdomTabContent(
    wisdom: DailyWisdom,
    headerContent: @Composable () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val palette = LocalAppPalette.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        headerContent()

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Günün Ayeti Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = palette.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(palette.primary.copy(alpha = 0.5f), Color.Transparent))
            ),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = palette.primary, modifier = Modifier.size(18.dp))
                        Text("GÜNÜN ÂYET-İ KERÎMESİ", style = MaterialTheme.typography.labelMedium.copy(color = palette.secondary, fontWeight = FontWeight.Bold))
                    }
                    IconButton(
                        onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("Ayet", "${wisdom.ayahArabic}\n\n${wisdom.ayahTurkish} (${wisdom.ayahSurah})"))
                            Toast.makeText(context, "Âyet kopyalandı", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp).testTag("copy_ayah_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Kopyala", tint = palette.textSecondary, modifier = Modifier.size(16.dp))
                    }
                }

                Text(
                    text = wisdom.ayahArabic,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = palette.secondary,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 32.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "“${wisdom.ayahTurkish}”",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = palette.textPrimary,
                        lineHeight = 22.sp
                    )
                )

                Text(
                    text = wisdom.ayahSurah,
                    style = MaterialTheme.typography.labelSmall.copy(color = palette.accent, fontWeight = FontWeight.SemiBold)
                )
            }
        }

        // Günün Hadis-i Şerifi Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = palette.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(palette.accent.copy(alpha = 0.4f), Color.Transparent))
            ),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Mosque, contentDescription = null, tint = palette.accent, modifier = Modifier.size(18.dp))
                        Text("GÜNÜN HADÎS-İ ŞERÎFİ", style = MaterialTheme.typography.labelMedium.copy(color = palette.accent, fontWeight = FontWeight.Bold))
                    }
                }

                Text(
                    text = "“${wisdom.hadithTurkish}”",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = palette.textPrimary,
                        lineHeight = 22.sp
                    )
                )

                Text(
                    text = "Kaynak: ${wisdom.hadithSource}",
                    style = MaterialTheme.typography.labelSmall.copy(color = palette.textSecondary)
                )
            }
        }

        // Ezan Duası Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = palette.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(palette.primary.copy(alpha = 0.4f), Color.Transparent))
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "EZANDAN SONRA OKUNACAK DUA",
                    style = MaterialTheme.typography.labelMedium.copy(color = palette.primary, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = wisdom.duaArabic,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = palette.secondary,
                        lineHeight = 26.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = wisdom.duaTurkish,
                    style = MaterialTheme.typography.bodySmall.copy(color = palette.textSecondary, lineHeight = 18.sp)
                )
            }
        }

        // Namaz Rekatları Özeti
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = palette.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = palette.primary, modifier = Modifier.size(18.dp))
                    Text("5 VAKİT NAMAZ REKATLARI", style = MaterialTheme.typography.labelMedium.copy(color = palette.textPrimary, fontWeight = FontWeight.Bold))
                }

                RakatRow("Sabah Namazı", "2 Sünnet + 2 Farz", "4 Rekat")
                RakatRow("Öğle Namazı", "4 İlk Sünnet + 4 Farz + 2 Son Sünnet", "10 Rekat")
                RakatRow("İkindi Namazı", "4 Sünnet + 4 Farz", "8 Rekat")
                RakatRow("Akşam Namazı", "3 Farz + 2 Sünnet", "5 Rekat")
                RakatRow("Yatsı Namazı", "4 İlk Sünnet + 4 Farz + 2 Son Sünnet + 3 Vitir", "13 Rekat")
            }
        }

        // Kerahet Vakitleri Bilgisi
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = palette.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "KERAHET VAKİTLERİ HAKKINDA",
                    style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "• Güneş doğarken (Güneşin doğuşundan yaklaşık 45 dakika sonrasına kadar)\n• Güneş tam tepedeyken (Öğle namazından yaklaşık 45 dakika önce)\n• Güneş batarken (Akşam namazından önceki yaklaşık 45 dakika)\nBu vakitlerde kaza veya nafile namazı kılınması mekruhtur.",
                    style = MaterialTheme.typography.bodySmall.copy(color = palette.textSecondary, lineHeight = 18.sp)
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun CategorizedDuasTabContent(
    headerContent: @Composable () -> Unit
) {
    val context = LocalContext.current
    val palette = LocalAppPalette.current
    var selectedCategory by remember { mutableStateOf<DuaCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val allPrayers = remember { DailyPrayersDatabase.allPrayers }

    val filteredPrayers = remember(selectedCategory, searchQuery) {
        allPrayers.filter { dua ->
            val matchesCat = selectedCategory == null || dua.category == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                dua.title.contains(searchQuery, ignoreCase = true) ||
                dua.turkishMeaning.contains(searchQuery, ignoreCase = true) ||
                dua.arabicText.contains(searchQuery, ignoreCase = true) ||
                dua.virtueNotes.contains(searchQuery, ignoreCase = true)
            matchesCat && matchesQuery
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        headerContent()

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Dualarda ara...", color = palette.textMuted) },
            leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = "Ara", tint = palette.primary)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Temizle", tint = palette.textMuted)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = palette.surface,
                unfocusedContainerColor = palette.surface,
                focusedBorderColor = palette.primary,
                unfocusedBorderColor = if (palette.isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Categories horizontal chips
        val categoryScroll = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(categoryScroll),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                label = { Text("Tümü (${allPrayers.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = palette.primary,
                    selectedLabelColor = Color.White
                )
            )

            DuaCategory.entries.forEach { cat ->
                val isSelected = selectedCategory == cat
                val countInCat = allPrayers.count { it.category == cat }
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = if (isSelected) null else cat },
                    label = { Text("${cat.titleTr} ($countInCat)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = palette.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // List of Prayers
        if (filteredPrayers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aramanıza uygun dua bulunamadı.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = palette.textMuted)
                )
            }
        } else {
            Text(
                text = "${filteredPrayers.size} dua listelendi",
                style = MaterialTheme.typography.labelSmall.copy(color = palette.textMuted)
            )

            filteredPrayers.forEach { dua ->
                DuaItemCard(dua = dua)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun DuaItemCard(dua: CategorizedDua) {
    val context = LocalContext.current
    val palette = LocalAppPalette.current

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        border = BorderStroke(
            1.dp,
            if (palette.isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Category Badge + Title + Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = palette.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = dua.category.titleTr,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = palette.primary,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (dua.recommendedCount.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = palette.accent.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = dua.recommendedCount,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = palette.accent,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    // Copy button
                    IconButton(
                        onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val formattedText = "${dua.title}\n\n${dua.arabicText}\n\nOkunuşu:\n${dua.transliteration}\n\nAnlamı:\n${dua.turkishMeaning}\n\nFazîleti:\n${dua.virtueNotes}"
                            cm.setPrimaryClip(ClipData.newPlainText(dua.title, formattedText))
                            Toast.makeText(context, "${dua.title} kopyalandı", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Kopyala",
                            tint = palette.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Share button
                    IconButton(
                        onClick = {
                            val formattedText = "🤲 *${dua.title}*\n\n${dua.arabicText}\n\n*Okunuşu:*\n_${dua.transliteration}_\n\n*Anlamı:*\n${dua.turkishMeaning}\n\n*Fazîlet & Kaynak:*\n${dua.virtueNotes}\n\n_Ezanla uygulamasından paylaşıldı._"
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, formattedText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "${dua.title} Paylaş"))
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Share,
                            contentDescription = "Paylaş",
                            tint = palette.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Title
            Text(
                text = dua.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
            )

            // Arabic text in calligraphy card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = palette.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = dua.arabicText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = palette.secondary,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 32.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                )
            }

            // Transliteration (Latin okunuşu)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Türkçe Okunuşu:",
                    style = MaterialTheme.typography.labelSmall.copy(color = palette.textMuted, fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = dua.transliteration,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = palette.textPrimary.copy(alpha = 0.9f),
                        fontStyle = FontStyle.Italic,
                        lineHeight = 20.sp
                    )
                )
            }

            // Turkish Meaning
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Meâli / Anlamı:",
                    style = MaterialTheme.typography.labelSmall.copy(color = palette.textMuted, fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "“${dua.turkishMeaning}”",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = palette.textPrimary,
                        lineHeight = 20.sp
                    )
                )
            }

            // Virtue / Hadith Notes
            if (dua.virtueNotes.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = palette.primary.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(16.dp).padding(top = 2.dp)
                        )
                        Text(
                            text = dua.virtueNotes,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = palette.textPrimary,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RakatRow(name: String, details: String, total: String) {
    val palette = LocalAppPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, style = MaterialTheme.typography.bodyMedium.copy(color = palette.textPrimary, fontWeight = FontWeight.SemiBold))
            Text(text = details, style = MaterialTheme.typography.bodySmall.copy(color = palette.textMuted, fontSize = 11.sp))
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = palette.surfaceVariant
        ) {
            Text(
                text = total,
                style = MaterialTheme.typography.labelSmall.copy(color = palette.secondary, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
