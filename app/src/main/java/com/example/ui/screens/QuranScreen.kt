package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.IslamicDatabase
import com.example.data.QuranAudioPlayer
import com.example.data.QuranAudioState
import com.example.data.QuranAyahProvider
import com.example.model.Ayah
import com.example.model.QuranReciter
import com.example.model.QuranReciters
import com.example.model.Surah
import com.example.model.TajweedRule
import com.example.ui.theme.AppPalette
import com.example.ui.theme.LocalAppPalette
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    audioPlayer: QuranAudioPlayer,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Sureler, 1: Tecvid Rehberi
    var searchQuery by remember { mutableStateOf("") }
    var selectedSurahForReading by remember { mutableStateOf<Surah?>(null) }
    var showReciterSheet by remember { mutableStateOf(false) }

    val audioState by audioPlayer.state.collectAsState()

    BackHandler(enabled = selectedSurahForReading != null) {
        selectedSurahForReading = null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        // Main Tab Content
        if (selectedTab == 0) {
            if (selectedSurahForReading != null) {
                SurahReadingView(
                    surah = selectedSurahForReading!!,
                    audioState = audioState,
                    onBack = { selectedSurahForReading = null },
                    onPlaySurah = { ayahs ->
                        audioPlayer.playSurah(
                            selectedSurahForReading!!.number,
                            selectedSurahForReading!!.nameTr,
                            totalAyahs = ayahs.size,
                            ayahs = ayahs
                        )
                    },
                    onSeekToAyah = { ayahNum, ayahs ->
                        if (audioState.currentSurahNumber != selectedSurahForReading!!.number || !audioState.isPlaying) {
                            audioPlayer.playSurah(
                                selectedSurahForReading!!.number,
                                selectedSurahForReading!!.nameTr,
                                totalAyahs = ayahs.size,
                                ayahs = ayahs
                            )
                        }
                        audioPlayer.seekToAyah(ayahNum)
                    }
                )
            } else {
                SurahListView(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    activeSurahNumber = if (audioState.isPlaying) audioState.currentSurahNumber else -1,
                    onSelectSurah = { surah -> selectedSurahForReading = surah },
                    onPlaySurah = { surah ->
                        val ayahs = QuranAyahProvider.getAllAyahsForSurah(surah)
                        audioPlayer.playSurah(surah.number, surah.nameTr, totalAyahs = ayahs.size, ayahs = ayahs)
                    },
                    headerContent = {
                        QuranHeaderAndControls(
                            palette = palette,
                            audioState = audioState,
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it },
                            onShowReciterSheet = { showReciterSheet = true },
                            audioPlayer = audioPlayer
                        )
                    }
                )
            }
        } else {
            TajweedGuideView(
                rules = IslamicDatabase.tajweedRules,
                headerContent = {
                    QuranHeaderAndControls(
                        palette = palette,
                        audioState = audioState,
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        onShowReciterSheet = { showReciterSheet = true },
                        audioPlayer = audioPlayer
                    )
                }
            )
        }
    }

    // Modal Bottom Sheet for Famous Reciters Selection
    if (showReciterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showReciterSheet = false },
            containerColor = palette.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Kur'an Kârîsi / Hoca Seçimi",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                        )
                        Text(
                            text = "Hoca seçildiğinde tilâvet hemen yeni hocanın sesiyle devam eder",
                            style = MaterialTheme.typography.bodySmall.copy(color = palette.textSecondary)
                        )
                    }
                    IconButton(onClick = { showReciterSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = palette.textSecondary)
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.08f))

                val turkishReciters = remember { QuranReciters.reciters.filter { it.isTurkish } }
                val worldReciters = remember { QuranReciters.reciters.filter { !it.isTurkish } }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        ) {
                            Text(
                                text = "🇹🇷 TÜRK HOCALAR & HÂFIZLAR",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = palette.primary
                                )
                            )
                        }
                    }

                    items(turkishReciters) { reciter ->
                        val isSelected = audioState.currentReciter.id == reciter.id
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) palette.primary.copy(alpha = 0.2f) else palette.surfaceVariant,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(
                                    if (isSelected) listOf(palette.primary, palette.accent)
                                    else listOf(Color.White.copy(alpha = 0.06f), Color.Transparent)
                                )
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    audioPlayer.setReciter(reciter)
                                    showReciterSheet = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) palette.primary else palette.surface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RecordVoiceOver,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.Black else palette.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = reciter.name,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) palette.primary else palette.textPrimary
                                            )
                                        )
                                        Text(
                                            text = reciter.description,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = palette.textSecondary,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Seçili",
                                        tint = palette.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 12.dp, bottom = 2.dp)
                        ) {
                            Text(
                                text = "🌍 DÜNYACA MEŞHUR KARİLER",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = palette.accent
                                )
                            )
                        }
                    }

                    items(worldReciters) { reciter ->
                        val isSelected = audioState.currentReciter.id == reciter.id
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) palette.primary.copy(alpha = 0.2f) else palette.surfaceVariant,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(
                                    if (isSelected) listOf(palette.primary, palette.accent)
                                    else listOf(Color.White.copy(alpha = 0.06f), Color.Transparent)
                                )
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    audioPlayer.setReciter(reciter)
                                    showReciterSheet = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) palette.primary else palette.surface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RecordVoiceOver,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.Black else palette.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = reciter.name,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) palette.primary else palette.textPrimary
                                            )
                                        )
                                        Text(
                                            text = reciter.description,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = palette.textSecondary,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Seçili",
                                        tint = palette.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun QuranPlayerMiniBar(
    audioState: QuranAudioState,
    onPlayPause: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onSeek: (Int) -> Unit,
    onChangeReciter: () -> Unit
) {
    val palette = LocalAppPalette.current

    Surface(
        color = palette.surfaceVariant,
        tonalElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(palette.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Audiotrack,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.clickable { onChangeReciter() }) {
                        Text(
                            text = "${audioState.currentSurahNumber}. ${audioState.currentSurahName} Sûresi",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Kari: ${audioState.currentReciter.name.take(18)}",
                                style = MaterialTheme.typography.labelSmall.copy(color = palette.primary)
                            )
                            if (audioState.isPlaying) {
                                val ayahStatus = when (audioState.currentAyahNumber) {
                                    QuranAudioPlayer.AYAH_ISTIADHA -> "• Eûzü"
                                    QuranAudioPlayer.AYAH_BASMALAH -> "• Besmele"
                                    else -> "• Âyet: ${audioState.currentAyahNumber}/${audioState.totalAyahsInSurah}"
                                }
                                Text(
                                    text = ayahStatus,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = palette.accent,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onRewind, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Replay10, contentDescription = "10sn Geri", tint = palette.textSecondary)
                    }
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(palette.primary)
                    ) {
                        if (audioState.isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (audioState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Oynat / Duraklat",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    IconButton(onClick = onForward, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Forward10, contentDescription = "10sn İleri", tint = palette.textSecondary)
                    }
                }
            }

            // Progress Slider
            if (audioState.durationMs > 0) {
                Slider(
                    value = audioState.progress,
                    onValueChange = { frac ->
                        onSeek((frac * audioState.durationMs).toInt())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = palette.primary,
                        activeTrackColor = palette.primary,
                        inactiveTrackColor = palette.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                )
            }
        }
    }
}

@Composable
private fun QuranHeaderAndControls(
    palette: AppPalette,
    audioState: QuranAudioState,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onShowReciterSheet: () -> Unit,
    audioPlayer: QuranAudioPlayer
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Kur'ân-ı Kerîm",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                )
                Text(
                    text = "114 Sûre • Tüm Âyetler & Mealleri",
                    style = MaterialTheme.typography.bodySmall.copy(color = palette.textSecondary)
                )
            }

            // Reciter Selection Badge
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = palette.surface,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(palette.primary.copy(alpha = 0.5f), palette.accent.copy(alpha = 0.3f))
                    )
                ),
                modifier = Modifier
                    .clickable { onShowReciterSheet() }
                    .testTag("button_select_reciter")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = "Hoca Seçimi",
                        tint = palette.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Column {
                        Text(
                            text = audioState.currentReciter.name.substringBefore(" (").take(16),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.primary,
                                fontSize = 11.sp
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = "Kari Değiştir",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = palette.textMuted,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Selector (Sureler & Tecvid)
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
                onClick = { onTabSelected(0) },
                text = {
                    Text(
                        "Sureler & Dinle",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                text = {
                    Text(
                        "Tecvid Rehberi",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            )
        }
    }

    // Active Player Bar if playing or active surah
    QuranPlayerMiniBar(
        audioState = audioState,
        onPlayPause = {
            if (audioState.isPlaying) audioPlayer.pause() else audioPlayer.resume()
        },
        onRewind = { audioPlayer.skipBackward10Sec() },
        onForward = { audioPlayer.skipForward10Sec() },
        onSeek = { pos -> audioPlayer.seekTo(pos) },
        onChangeReciter = onShowReciterSheet
    )
}

@Composable
private fun SurahListView(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    activeSurahNumber: Int,
    onSelectSurah: (Surah) -> Unit,
    onPlaySurah: (Surah) -> Unit,
    headerContent: @Composable () -> Unit
) {
    val palette = LocalAppPalette.current
    val filteredSurahs = remember(searchQuery) {
        if (searchQuery.isBlank()) IslamicDatabase.surahs
        else IslamicDatabase.surahs.filter {
            it.nameTr.contains(searchQuery, ignoreCase = true) ||
            it.meaningTr.contains(searchQuery, ignoreCase = true) ||
            it.number.toString() == searchQuery.trim()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            headerContent()
        }

        item {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Sûre adı veya numarası ara...", color = palette.textMuted) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = palette.primary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Temizle", tint = palette.textSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = palette.primary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedContainerColor = palette.surface,
                    unfocusedContainerColor = palette.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        items(filteredSurahs, key = { it.number }) { surah ->
            val isPlaying = activeSurahNumber == surah.number
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                SurahItemCard(
                    surah = surah,
                    isPlaying = isPlaying,
                    onClick = { onSelectSurah(surah) },
                    onPlay = { onPlaySurah(surah) }
                )
            }
        }
    }
}

@Composable
private fun SurahItemCard(
    surah: Surah,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onPlay: () -> Unit
) {
    val palette = LocalAppPalette.current

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = palette.surface,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (isPlaying) listOf(palette.primary, palette.accent)
                else listOf(Color.White.copy(alpha = 0.05f), Color.Transparent)
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Surah Number Badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) palette.primary else palette.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = surah.number.toString(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isPlaying) Color.Black else palette.primary
                        )
                    )
                }

                Column {
                    Text(
                        text = "${surah.nameTr} Sûresi",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                    )
                    Text(
                        text = "${surah.meaningTr} • ${surah.ayahCount} Âyet • ${surah.revelationType}",
                        style = MaterialTheme.typography.labelSmall.copy(color = palette.textSecondary)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = surah.nameAr,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.secondary
                    )
                )

                IconButton(
                    onClick = onPlay,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) palette.primary else palette.surfaceVariant)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Sûreyi Dinle",
                        tint = if (isPlaying) Color.Black else palette.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Kullanıcının isteği:
 * 1) "ayet kısmını düzelt bütün ayetler gözüksün"
 * 2) "hoca okurken nerede olduğunu belirt"
 */
@Composable
private fun SurahReadingView(
    surah: Surah,
    audioState: QuranAudioState,
    onBack: () -> Unit,
    onPlaySurah: (List<Ayah>) -> Unit,
    onSeekToAyah: (Int, List<Ayah>) -> Unit
) {
    val context = LocalContext.current
    val palette = LocalAppPalette.current
    val coroutineScope = rememberCoroutineScope()
    val isCurrentSurahPlaying = audioState.isPlaying && audioState.currentSurahNumber == surah.number
    val activeAyahNumber = if (isCurrentSurahPlaying) audioState.currentAyahNumber else -1

    // Kullanıcı Talebi: "Kuranı hocaların okuduğu ayetlere göre yap veriyi istediğin yerden alabilirsin yani hoca kuranda ne okuyorsa gözümle takip edebilmek istiyorum"
    var allAyahs by remember(surah.number) {
        mutableStateOf(QuranAyahProvider.getAllAyahsForSurah(surah))
    }

    LaunchedEffect(surah.number) {
        QuranAyahProvider.loadAyahsForSurah(context, surah) { updatedAyahs ->
            allAyahs = updatedAyahs
        }
    }

    val listState = rememberLazyListState()

    val headerCount = 1 + (if (surah.number != 9) 1 else 0) + (if (isCurrentSurahPlaying) 1 else 0)

    // Hoca okurken otomatik olarak o âyete hassas kaydır ve görünür kıl
    LaunchedEffect(activeAyahNumber, isCurrentSurahPlaying) {
        if (isCurrentSurahPlaying) {
            if (activeAyahNumber in 1..allAyahs.size) {
                val targetIndex = (activeAyahNumber - 1).coerceAtLeast(0) + headerCount
                listState.animateScrollToItem(targetIndex, scrollOffset = -80)
            } else if (activeAyahNumber == QuranAudioPlayer.AYAH_BASMALAH || activeAyahNumber == QuranAudioPlayer.AYAH_ISTIADHA) {
                listState.animateScrollToItem(0, scrollOffset = 0)
            }
        }
    }

    // Okunan ayetin ekranda görünür olup olmadığını kontrol et
    val isCurrentAyahVisible by remember {
        derivedStateOf {
            if (activeAyahNumber <= 0) true
            else {
                val targetIndex = activeAyahNumber - 1 + headerCount
                listState.layoutInfo.visibleItemsInfo.any { it.index == targetIndex }
            }
        }
    }

    // Infinite transition for active reciting ayah glow
    val infiniteTransition = rememberInfiniteTransition(label = "ayahRecitingPulse")
    val recitingGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "recitingGlow"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Back Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = palette.textPrimary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${surah.number}. ${surah.nameTr} Sûresi",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                        )
                        Text(
                            text = "${surah.meaningTr} • ${surah.ayahCount} Âyet (Tamamı) • ${surah.revelationType}",
                            style = MaterialTheme.typography.labelSmall.copy(color = palette.textSecondary)
                        )
                    }

                    IconButton(
                        onClick = { onPlaySurah(allAyahs) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isCurrentSurahPlaying) palette.primary else palette.surface)
                    ) {
                        Icon(
                            imageVector = if (isCurrentSurahPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Dinle",
                            tint = if (isCurrentSurahPlaying) Color.Black else palette.primary
                        )
                    }
                }
            }

            // Besmele Header if not Tevbe
            if (surah.number != 9) {
                item {
                    val isBesmeleActive = isCurrentSurahPlaying && activeAyahNumber == QuranAudioPlayer.AYAH_BASMALAH
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isBesmeleActive) palette.surfaceVariant else palette.surface,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                if (isBesmeleActive) listOf(
                                    palette.primary.copy(alpha = recitingGlow),
                                    palette.accent.copy(alpha = recitingGlow)
                                ) else listOf(palette.primary.copy(alpha = 0.3f), Color.Transparent)
                            ),
                            width = if (isBesmeleActive) 2.dp else 1.dp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBesmeleActive) palette.primary else palette.secondary
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isBesmeleActive) "Bismillâhirrahmânirrahîm okunuyor..." else "Rahmân ve Rahîm olan Allah'ın adıyla",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isBesmeleActive) palette.primary else palette.textSecondary,
                                    fontWeight = if (isBesmeleActive) FontWeight.Bold else FontWeight.Normal
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Live reciter banner if playing
            if (isCurrentSurahPlaying) {
                item {
                    val bannerText = when (activeAyahNumber) {
                        QuranAudioPlayer.AYAH_ISTIADHA -> "Eûzü billâhi mine'ş-şeytâni'r-racîm okunuyor..."
                        QuranAudioPlayer.AYAH_BASMALAH -> "Bismillâhirrahmânirrahîm okunuyor..."
                        else -> "${audioState.currentReciter.name} okuyor • Âyet $activeAyahNumber / ${allAyahs.size}"
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = palette.primary.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                coroutineScope.launch {
                                    if (activeAyahNumber in 1..allAyahs.size) {
                                        listState.animateScrollToItem((activeAyahNumber - 1).coerceAtLeast(0) + headerCount, scrollOffset = -80)
                                    } else {
                                        listState.animateScrollToItem(0, scrollOffset = 0)
                                    }
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = palette.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = bannerText,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = palette.primary
                                    )
                                )
                            }

                            Text(
                                text = "Âyete Git ➜",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = palette.accent,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // All Ayahs List (No ayahs missing!)
            items(allAyahs, key = { it.ayahNumber }) { ayah ->
                    val isBeingRecited = ayah.ayahNumber == activeAyahNumber

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isBeingRecited) palette.surfaceVariant.copy(alpha = 0.95f) else palette.surface
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                if (isBeingRecited) listOf(
                                    palette.primary.copy(alpha = recitingGlow),
                                    palette.accent.copy(alpha = recitingGlow)
                                )
                                else listOf(Color.White.copy(alpha = 0.05f), Color.Transparent)
                            ),
                            width = if (isBeingRecited) 2.5.dp else 1.dp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSeekToAyah(ayah.ayahNumber, allAyahs) }
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
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isBeingRecited) palette.primary else palette.primary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "Âyet ${ayah.ayahNumber}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isBeingRecited) Color.Black else palette.primary
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Active Reciting Badge with Animated Audio waves
                                    if (isBeingRecited) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = palette.accent.copy(alpha = 0.25f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.VolumeUp,
                                                    contentDescription = null,
                                                    tint = palette.accent,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "Şu Anda Okunuyor",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = palette.accent,
                                                        fontSize = 10.sp
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    // Quick listen button
                                    IconButton(
                                        onClick = { onSeekToAyah(ayah.ayahNumber, allAyahs) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isBeingRecited && audioState.isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayCircleOutline,
                                            contentDescription = "Bu âyetten dinle",
                                            tint = if (isBeingRecited) palette.primary else palette.textSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            // Arabic text with enhanced legibility
                            Text(
                                text = ayah.textAr,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBeingRecited) palette.primary else palette.secondary,
                                    lineHeight = 38.sp
                                ),
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Divider(color = Color.White.copy(alpha = 0.06f))

                            // Turkish Meal
                            Text(
                                text = ayah.textTr,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isBeingRecited) Color.White else palette.textPrimary,
                                    lineHeight = 22.sp,
                                    fontWeight = if (isBeingRecited) FontWeight.SemiBold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }

        // Floating Action Button: Scroll back to reciting ayah if user scrolled away
        if (isCurrentSurahPlaying && !isCurrentAyahVisible && activeAyahNumber > 0) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = palette.primary,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .clickable {
                        coroutineScope.launch {
                            listState.animateScrollToItem(
                                (activeAyahNumber - 1).coerceAtLeast(0) + headerCount,
                                scrollOffset = -80
                            )
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Okunan Âyete Odaklan (Âyet $activeAyahNumber)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TajweedGuideView(
    rules: List<TajweedRule>,
    headerContent: @Composable () -> Unit
) {
    val palette = LocalAppPalette.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            headerContent()
        }

        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tecvid İlmi ve Kuralları",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Kur'ân-ı Kerîm'i usûlüne, mahreçlerine ve tecvid kurallarına uygun olarak güzelce tilâvet etme rehberi.",
                            style = MaterialTheme.typography.bodySmall.copy(color = palette.textSecondary)
                        )
                    }
                }
            }
        }

        items(rules) { rule ->
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = palette.surface),
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
                        Text(
                            text = rule.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.primary
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = palette.accent.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Kural",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = palette.accent
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = rule.description,
                        style = MaterialTheme.typography.bodyMedium.copy(color = palette.textSecondary)
                    )

                    val firstExample = rule.examples.firstOrNull()
                    if (firstExample != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = palette.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Örnek:",
                                        style = MaterialTheme.typography.labelSmall.copy(color = palette.textMuted)
                                    )
                                    Text(
                                        text = firstExample.explanation,
                                        style = MaterialTheme.typography.bodySmall.copy(color = palette.textPrimary)
                                    )
                                }
                                Text(
                                    text = firstExample.arabicWord,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = palette.secondary
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
