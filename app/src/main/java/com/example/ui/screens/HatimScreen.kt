package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HatimJuzContentProvider
import com.example.data.IslamicDatabase
import com.example.data.JuzPage
import com.example.data.JuzReadingContent
import com.example.data.QuranAudioPlayer
import com.example.data.QuranAudioState
import com.example.model.Ayah
import com.example.model.HatimJuz
import com.example.model.JuzStatus
import com.example.model.QuranReciter
import com.example.model.QuranReciters
import com.example.ui.theme.LocalAppPalette
import kotlinx.coroutines.launch

/**
 * Kullanıcı Talebi:
 * "Hatim kısmında cüzlerin içi tam dolu değil Doldur ve kuran gibi okuma dinleme takip etme formatı yap."
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HatimScreen(
    audioPlayer: QuranAudioPlayer? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = LocalAppPalette.current
    val coroutineScope = rememberCoroutineScope()

    // Persistent Hatim Cüz Durumu (SharedPreferences ile kalıcı saklama)
    val prefs = remember { context.getSharedPreferences("hatim_tracker_prefs", Context.MODE_PRIVATE) }
    
    var juzList by remember {
        val initial = IslamicDatabase.getInitialHatimJuzs()
        val restored = initial.map { juz ->
            val isDone = prefs.getBoolean("juz_completed_${juz.number}", false)
            if (isDone) juz.copy(status = JuzStatus.COMPLETED, readerName = "Okundu")
            else juz
        }
        mutableStateOf(restored)
    }

    var readingJuz by remember { mutableStateOf<HatimJuz?>(null) }
    var selectedJuzForEdit by remember { mutableStateOf<HatimJuz?>(null) }
    var showHatimDuaSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Audio player desteği
    val safeAudioPlayer = audioPlayer ?: remember { QuranAudioPlayer(context) }
    val audioState by safeAudioPlayer.state.collectAsState()

    BackHandler(enabled = readingJuz != null) {
        readingJuz = null
    }

    val completedCount = juzList.count { it.status == JuzStatus.COMPLETED }
    val progressFrac = completedCount / 30f

    val animatedProgress by animateFloatAsState(
        targetValue = progressFrac,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "hatimProgress"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = palette.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (readingJuz != null) {
                // TAM DOLU VE KURAN GİBİ OKUMA, DİNLEME, TAKİP FORMATI
                JuzReadingAndListeningView(
                    juz = readingJuz!!,
                    audioPlayer = safeAudioPlayer,
                    audioState = audioState,
                    onBack = { readingJuz = null },
                    onMarkAsCompleted = { completedJuz ->
                        prefs.edit().putBoolean("juz_completed_${completedJuz.number}", true).apply()
                        juzList = juzList.map {
                            if (it.number == completedJuz.number) it.copy(status = JuzStatus.COMPLETED, readerName = "Okundu")
                            else it
                        }
                        readingJuz = null
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "${completedJuz.number}. Cüz tilâvetiniz hatiminize başarıyla kaydedildi.",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                )
            } else {
                // CÜZ LİSTESİ VE HATİM ÖZETİ
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Top Header
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Hatim & Mukabele",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = palette.textPrimary
                                    )
                                )
                                Text(
                                    text = "Cüzü seçin • Kuran gibi okuyun, dinleyin ve takip edin",
                                    style = MaterialTheme.typography.bodySmall.copy(color = palette.textSecondary)
                                )
                            }

                            IconButton(
                                onClick = { showHatimDuaSheet = true },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(palette.primary.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = "Hatim Duası",
                                    tint = palette.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // Progress Overview Card
                    item(span = { GridItemSpan(maxLineSpan) }) {
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
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Hatim İlerlemesi",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = palette.secondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$completedCount / 30 Cüz Tamamlandı",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = palette.textPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (completedCount == 30) "Tebrikler! Hatm-i Şerîf tamamlandı, duasını yapabilirsiniz."
                                        else "${30 - completedCount} cüz kaldı. Her cüz 20 sayfadır.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = palette.textSecondary)
                                    )
                                }

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    val trackColor = palette.surfaceVariant
                                    val primaryColor = palette.primary
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawCircle(
                                            color = trackColor,
                                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                        drawArc(
                                            color = primaryColor,
                                            startAngle = -90f,
                                            sweepAngle = animatedProgress * 360f,
                                            useCenter = false,
                                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    }
                                    Text(
                                        text = "%${(progressFrac * 100).toInt()}",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = palette.primary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // 30 Cüz Grid Kartları
                    items(juzList, key = { it.number }) { juz ->
                        HatimJuzGridCard(
                            juz = juz,
                            onClick = { readingJuz = juz },
                            onLongClick = { selectedJuzForEdit = juz }
                        )
                    }
                }
            }
        }
    }

    // Hatim Duası Modal Sheet
    if (showHatimDuaSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHatimDuaSheet = false },
            containerColor = palette.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.primary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Hatm-i Şerîf Duası",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.primary
                        ),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = "صَدَقَ اللّٰهُ الْعَظِيمُ وَبَلَّغَ رَسُولُهُ الْكَرِيمُ",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.secondary
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Ey Yüce Rabbimiz! Okuduğumuz Kur'ân-ı Kerîm'i dergâh-ı izzetinde kabul eyle. Hâsıl olan sevâbı başta Sevgili Peygamberimiz Hazret-i Muhammed Mustafâ (s.a.v.) Efendimizin mübârek ruhuna, ehl-i beytine, ashâbına ve cümle geçmişlerimizin ruhlarına hediye eyledik, vâsıl eyle. Kalplerimizi Kur'ân nûru ile nurlandır, ahlâkımızı Kur'ân ahlâkı eyle. Âmin.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = palette.textPrimary,
                        lineHeight = 24.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = { showHatimDuaSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Kapat", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * KURAN GİBİ OKUMA, DİNLEME VE TAKİP ETME FORMATI (TAM DOLU CÜZ)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JuzReadingAndListeningView(
    juz: HatimJuz,
    audioPlayer: QuranAudioPlayer,
    audioState: QuranAudioState,
    onBack: () -> Unit,
    onMarkAsCompleted: (HatimJuz) -> Unit
) {
    val palette = LocalAppPalette.current
    val coroutineScope = rememberCoroutineScope()
    val juzContent = remember(juz.number) { HatimJuzContentProvider.getJuzContent(juz.number) }

    val allJuzAyahs = remember(juzContent) {
        juzContent.pages.flatMap { it.ayahs }
    }

    // Sayfa seçimi (1..20. cüz sayfaları)
    var selectedPageIndex by remember { mutableIntStateOf(0) }
    var arabicFontSize by remember { mutableFloatStateOf(24f) }
    var showTurkishMeal by remember { mutableStateOf(true) }
    var showReciterSheet by remember { mutableStateOf(false) }

    val activePage = juzContent.pages.getOrElse(selectedPageIndex) { juzContent.pages[0] }
    val listState = rememberLazyListState()

    val isAudioPlaying = audioState.isPlaying && (
        (audioState.isHatimMode && audioState.currentJuzNumber == juz.number) ||
        (!audioState.isHatimMode && audioState.currentSurahNumber == juzContent.primarySurahNumber)
    )
    val activeAyahNumber = if (isAudioPlaying) audioState.currentAyahNumber else -1

    val playCurrentJuz: () -> Unit = {
        val startAyah = activePage.ayahs.firstOrNull()?.ayahNumber ?: 1
        audioPlayer.playHatim(
            juzNumber = juz.number,
            juzName = "${juz.number}. Cüz (${juzContent.primarySurahName})",
            ayahs = allJuzAyahs,
            startAyahNumber = startAyah
        )
    }

    // Dinleme esnasında gerçek zamanlı ayet takibi: Sayfayı ve konumu anında ve gecikmesiz kaydır
    LaunchedEffect(activeAyahNumber, audioState.currentSurahNumber, isAudioPlaying) {
        if (isAudioPlaying) {
            if (activeAyahNumber > 0) {
                val targetPageIdx = juzContent.pages.indexOfFirst { page ->
                    page.ayahs.any { it.surahNumber == audioState.currentSurahNumber && it.ayahNumber == activeAyahNumber }
                }
                if (targetPageIdx >= 0) {
                    if (targetPageIdx != selectedPageIndex) {
                        selectedPageIndex = targetPageIdx
                    }
                    val page = juzContent.pages[targetPageIdx]
                    val targetAyahIdx = page.ayahs.indexOfFirst { it.surahNumber == audioState.currentSurahNumber && it.ayahNumber == activeAyahNumber }
                    if (targetAyahIdx >= 0) {
                        listState.animateScrollToItem(targetAyahIdx + 1, scrollOffset = -80)
                    }
                }
            } else if (activeAyahNumber == QuranAudioPlayer.AYAH_BASMALAH || activeAyahNumber == QuranAudioPlayer.AYAH_ISTIADHA) {
                listState.animateScrollToItem(0, scrollOffset = 0)
            }
        }
    }

    // Dinleme esnasında ayet takip animasyonu
    val infiniteTransition = rememberInfiniteTransition(label = "ayahFollowPulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        // Üst Navigasyon ve Kontrol Çubuğu
        Surface(
            color = palette.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = palette.textPrimary)
                        }
                        Column {
                            Text(
                                text = "${juz.number}. Cüz-i Şerîf (Sayfa ${activePage.pageNumber})",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary
                                )
                            )
                            Text(
                                text = "${juz.startSurah} ➔ ${juz.endSurah} • Mukabele",
                                style = MaterialTheme.typography.labelSmall.copy(color = palette.textSecondary)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Yazı boyutu büyüt / küçült
                        IconButton(onClick = {
                            arabicFontSize = if (arabicFontSize >= 32f) 22f else arabicFontSize + 3f
                        }) {
                            Icon(Icons.Default.FormatSize, contentDescription = "Yazı Boyutu", tint = palette.primary)
                        }

                        // Meâl Aç / Kapat (Sırf Mushaf Hattı Modu)
                        IconButton(onClick = { showTurkishMeal = !showTurkishMeal }) {
                            Icon(
                                imageVector = if (showTurkishMeal) Icons.Default.Translate else Icons.Outlined.MenuBook,
                                contentDescription = "Meâl Göster",
                                tint = if (showTurkishMeal) palette.primary else palette.textSecondary
                            )
                        }
                    }
                }

                // SESLİ DİNLEME VE KURAN TAKİP ÇUBUĞU (Kâri Tilâveti)
                Surface(
                    color = palette.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // 10sn Geri
                            IconButton(
                                onClick = { audioPlayer.skipBackward10Sec() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay10,
                                    contentDescription = "10sn Geri",
                                    tint = palette.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Oynat / Duraklat
                            IconButton(
                                onClick = {
                                    if (isAudioPlaying) {
                                        audioPlayer.pause()
                                    } else {
                                        playCurrentJuz()
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(palette.primary)
                            ) {
                                Icon(
                                    imageVector = if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isAudioPlaying) "Duraklat" else "Dinle",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // 10sn İleri
                            IconButton(
                                onClick = { audioPlayer.skipForward10Sec() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Forward10,
                                    contentDescription = "10sn İleri",
                                    tint = palette.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(
                                modifier = Modifier.clickable { showReciterSheet = true }
                            ) {
                                Text(
                                    text = if (isAudioPlaying) "Okunuyor: ${juzContent.primarySurahName}" else "Sesli Tilâveti Başlat",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAudioPlaying) palette.primary else palette.textPrimary
                                    )
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.RecordVoiceOver,
                                        contentDescription = "Kâri Değiştir",
                                        tint = palette.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Kâri: ${audioState.currentReciter.name} (Değiştir)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            color = palette.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }

                        if (isAudioPlaying) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = palette.primary.copy(alpha = 0.2f),
                                modifier = Modifier.clickable {
                                    val targetAyahIdx = activePage.ayahs.indexOfFirst { it.ayahNumber == activeAyahNumber }
                                    if (targetAyahIdx >= 0) {
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(targetAyahIdx + 1, scrollOffset = -80)
                                        }
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = palette.primary, modifier = Modifier.size(14.dp))
                                    val hatimAyahText = when (activeAyahNumber) {
                                        QuranAudioPlayer.AYAH_ISTIADHA -> "Eûzü..."
                                        QuranAudioPlayer.AYAH_BASMALAH -> "Besmele..."
                                        else -> "Âyet $activeAyahNumber / ${allJuzAyahs.size}"
                                    }
                                    Text(
                                        text = hatimAyahText,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = palette.primary)
                                    )
                                }
                            }
                        }
                    }
                }

                // CÜZÜN 20 SAYFASI YATAY SEÇİCİ (Mukabele Sayfaları)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    juzContent.pages.forEachIndexed { index, page ->
                        val isSelected = selectedPageIndex == index
                        Surface(
                            onClick = { selectedPageIndex = index },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) palette.primary else palette.surface,
                            border = if (isSelected) null else CardDefaults.outlinedCardBorder()
                        ) {
                            Text(
                                text = "Sayfa ${index + 1} (s.${page.pageNumber})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.Black else palette.textSecondary,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // AYET LİSTESİ (Sayfa içeriği tam ve zengin Kur'an formatı)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Besmele-i Şerîf
            item {
                val isBesmeleActive = isAudioPlaying && activeAyahNumber == QuranAudioPlayer.AYAH_BASMALAH
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isBesmeleActive) palette.surfaceVariant else palette.surface
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            if (isBesmeleActive) listOf(
                                palette.primary.copy(alpha = pulseGlow),
                                palette.accent.copy(alpha = pulseGlow)
                            ) else listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
                        ),
                        width = if (isBesmeleActive) 2.dp else 1.dp
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isBesmeleActive) palette.primary else palette.secondary
                            )
                        )
                        Text(
                            text = if (isBesmeleActive) "Bismillâhirrahmânirrahîm okunuyor..."
                                   else "${juz.number}. Cüz • ${activePage.surahName} • Sayfa ${activePage.pageNumber}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isBesmeleActive) palette.primary else palette.textSecondary,
                                fontWeight = if (isBesmeleActive) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }

            // Sayfadaki Âyetler
            itemsIndexed(activePage.ayahs, key = { index, _ -> "${activePage.pageNumber}_$index" }) { index, ayah ->
                val isBeingRecited = isAudioPlaying && activeAyahNumber == ayah.ayahNumber && audioState.currentSurahNumber == ayah.surahNumber

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isBeingRecited) palette.surfaceVariant else palette.surface
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            if (isBeingRecited) listOf(palette.primary, palette.accent)
                            else listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
                        ),
                        width = if (isBeingRecited) 2.dp else 1.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            audioPlayer.playAyahInHatim(
                                juzNumber = juz.number,
                                targetAyah = ayah,
                                allAyahs = allJuzAyahs
                            )
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isBeingRecited) palette.primary else palette.surfaceVariant,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${ayah.ayahNumber}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isBeingRecited) Color.Black else palette.primary,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }

                                if (isBeingRecited) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = palette.accent.copy(alpha = pulseGlow * 0.35f)
                                    ) {
                                        Text(
                                            text = "Tilâvet Ediliyor",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = palette.accent,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            // Doğrudan Âyeti Dinle Butonu
                            IconButton(
                                onClick = {
                                    audioPlayer.playAyahInHatim(
                                        juzNumber = juz.number,
                                        targetAyah = ayah,
                                        allAyahs = allJuzAyahs
                                    )
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isBeingRecited) Icons.Default.GraphicEq else Icons.Default.PlayCircle,
                                    contentDescription = "Bu Ayeti Dinle",
                                    tint = if (isBeingRecited) palette.primary else palette.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Arapça Orijinal Metin (Büyük, net hat)
                        Text(
                            text = ayah.textAr,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = arabicFontSize.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBeingRecited) palette.primary else palette.secondary,
                                lineHeight = (arabicFontSize * 1.6f).sp
                            ),
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Türkçe Meâl (İsteğe bağlı)
                        if (showTurkishMeal) {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                            Text(
                                text = ayah.textTr,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = palette.textPrimary,
                                    lineHeight = 21.sp
                                )
                            )
                        }
                    }
                }
            }

            // Sayfa Altı Gezinme Butonları (Önceki Sayfa / Sonraki Sayfa)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (selectedPageIndex > 0) selectedPageIndex--
                        },
                        enabled = selectedPageIndex > 0,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Önceki Sayfa")
                    }

                    Text(
                        text = "${selectedPageIndex + 1} / ${juzContent.pages.size}",
                        style = MaterialTheme.typography.labelMedium.copy(color = palette.textSecondary)
                    )

                    OutlinedButton(
                        onClick = {
                            if (selectedPageIndex < juzContent.pages.size - 1) selectedPageIndex++
                        },
                        enabled = selectedPageIndex < juzContent.pages.size - 1,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Sonraki Sayfa")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // "BU CÜZÜ OKUDUM" BÜYÜK TAMAMLAMA BUTONU (Kullanıcı Talebi)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.surfaceVariant),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(palette.primary, palette.accent))
                    ),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "${juz.number}. Cüz-i Şerîf'i Bitirdiniz mi?",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                        )
                        Text(
                            text = "Bu cüzü okuduysanız aşağıdaki butona tıklayarak Hatim Takibinize 'Okundu' olarak kaydedebilirsiniz.",
                            style = MaterialTheme.typography.bodySmall.copy(color = palette.textSecondary),
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = { onMarkAsCompleted(juz) },
                            colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("mark_juz_completed_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Bu Cüzü Okudum (Hatimime Kaydet)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            )
                        }
                    }
                }
            }
        }

        // Kâri (Hoca) Seçim Menüsü (ModalBottomSheet)
        if (showReciterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showReciterSheet = false },
                containerColor = palette.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Kâri (Hoca) Seçimi",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary
                                )
                            )
                            Text(
                                text = "Okunan âyeti hoca okurken anlık ve gecikmesiz takip edin",
                                style = MaterialTheme.typography.bodySmall.copy(color = palette.textSecondary)
                            )
                        }
                        IconButton(onClick = { showReciterSheet = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Kapat", tint = palette.textSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val turkishReciters = QuranReciters.reciters.filter { it.isTurkish }
                        val worldReciters = QuranReciters.reciters.filter { !it.isTurkish }

                        item {
                            Text(
                                text = "🇹🇷 TÜRKİYE HATİM & MUKABELE ÜSTADLARI",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = palette.primary
                                ),
                                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                            )
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
                                                .size(38.dp)
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
                            Text(
                                text = "🌍 DÜNYACA MEŞHUR KARİLER",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = palette.accent
                                ),
                                modifier = Modifier.padding(top = 12.dp, bottom = 2.dp)
                            )
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
                                                .size(38.dp)
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

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

/**
 * Cüz Grid Kartı
 */
@Composable
private fun HatimJuzGridCard(
    juz: HatimJuz,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val palette = LocalAppPalette.current
    val statusColor = when (juz.status) {
        JuzStatus.COMPLETED -> palette.primary
        JuzStatus.IN_PROGRESS -> palette.accent
        JuzStatus.AVAILABLE -> palette.surfaceVariant
    }

    val statusLabel = when (juz.status) {
        JuzStatus.COMPLETED -> "Okundu"
        JuzStatus.IN_PROGRESS -> "Okunuyor"
        JuzStatus.AVAILABLE -> "Okunmadı"
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = palette.surface,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                if (juz.status == JuzStatus.COMPLETED) listOf(palette.primary.copy(alpha = 0.6f), palette.primary.copy(alpha = 0.2f))
                else listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .clickable(onClick = onClick)
            .testTag("juz_card_${juz.number}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(if (juz.status == JuzStatus.COMPLETED) palette.primary else palette.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${juz.number}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (juz.status == JuzStatus.COMPLETED) Color.Black else palette.primary,
                            fontSize = 11.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (juz.status == JuzStatus.COMPLETED) palette.primary else palette.surfaceVariant
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (juz.status == JuzStatus.COMPLETED) Color.Black else palette.textSecondary,
                            fontSize = 9.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${juz.number}. Cüz",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                )
                Text(
                    text = "S.${juz.pageStart}-${juz.pageEnd}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = palette.textMuted,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}
