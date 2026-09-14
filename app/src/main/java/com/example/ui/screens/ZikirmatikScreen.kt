package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WisdomDatabase
import com.example.model.DhikrBookEntry
import com.example.model.DhikrPreset
import com.example.ui.theme.AppPalette
import com.example.ui.theme.LocalAppPalette

enum class DhikrScreenTab {
    COUNTER,   // Zikirmatik Sayacı
    NOTEBOOK   // Zikir Defteri & Çetele
}

@Composable
fun ZikirmatikScreen(
    currentDhikrIndex: Int,
    dhikrCount: Int,
    completedLaps: Int,
    isVibrationEnabled: Boolean,
    dhikrBookEntries: List<DhikrBookEntry>,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onSelectPreset: (Int) -> Unit,
    onSelectPresetById: (String) -> Unit,
    onResetBookEntry: (String) -> Unit,
    onToggleVibration: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current
    var activeTab by remember { mutableStateOf(DhikrScreenTab.COUNTER) }
    var showDhikrPickerBox by remember { mutableStateOf(false) }

    val preset = WisdomDatabase.allDhikrPresets.getOrNull(currentDhikrIndex) ?: WisdomDatabase.allDhikrPresets[0]

    // Hedef Belirleme Durumu
    var customTarget by remember(currentDhikrIndex) { mutableIntStateOf(preset.target) }
    var showTargetDialog by remember { mutableStateOf(false) }
    var targetInputText by remember { mutableStateOf(customTarget.toString()) }

    val activeTarget = if (customTarget > 0) customTarget else preset.target
    val isTargetReached = dhikrCount >= activeTarget && activeTarget > 0
    val progress = if (activeTarget > 0) (dhikrCount.toFloat() / activeTarget.toFloat()).coerceIn(0f, 1f) else 0.5f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "dhikrProgress"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "buttonScale"
    )

    val topHeaderContent: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (activeTab == DhikrScreenTab.COUNTER) "Akıllı Zikirmatik" else "Zikir Defterim",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
            )

            // Sekme Değiştirici (Pill Tab - Her Cihaza Uygun)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = palette.surfaceVariant.copy(alpha = 0.6f),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(palette.primary.copy(alpha = 0.3f), palette.accent.copy(alpha = 0.2f)))
                )
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    Surface(
                        onClick = { activeTab = DhikrScreenTab.COUNTER },
                        shape = RoundedCornerShape(14.dp),
                        color = if (activeTab == DhikrScreenTab.COUNTER) palette.primary else Color.Transparent,
                        modifier = Modifier.testTag("tab_dhikr_counter")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = "Sayaç",
                                tint = if (activeTab == DhikrScreenTab.COUNTER) Color.Black else palette.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Sayaç",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeTab == DhikrScreenTab.COUNTER) Color.Black else palette.textSecondary
                                )
                            )
                        }
                    }

                    Surface(
                        onClick = { activeTab = DhikrScreenTab.NOTEBOOK },
                        shape = RoundedCornerShape(14.dp),
                        color = if (activeTab == DhikrScreenTab.NOTEBOOK) palette.primary else Color.Transparent,
                        modifier = Modifier.testTag("tab_dhikr_notebook")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Zikir Defteri",
                                tint = if (activeTab == DhikrScreenTab.NOTEBOOK) Color.Black else palette.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Defter",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeTab == DhikrScreenTab.NOTEBOOK) Color.Black else palette.textSecondary
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        if (activeTab == DhikrScreenTab.COUNTER) {
            // ==========================================
            // 1. SEKME: ZİKİRMATİK SAYACI
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Üst Başlık & Sekme Geçişi (Sayfayla birlikte kayar)
                topHeaderContent()
                // ZİKİR SEÇME BÖLÜMÜ (Kullanıcı Talebi: "zikir kısmına zikir seçme bölümü yap ve ordan kutucuk çıkıp insanlar zikirini seçsin")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDhikrPickerBox = true }
                        .testTag("dhikr_picker_trigger"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.surfaceVariant.copy(alpha = 0.7f)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(palette.primary.copy(alpha = 0.5f), palette.accent.copy(alpha = 0.3f))
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(palette.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatListBulleted,
                                    contentDescription = "Zikir Seç",
                                    tint = palette.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = palette.textPrimary
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Dokunun ve Zikrinizi Seçin ▾",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = palette.accent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        // Zikri Değiştir Buton Etiketi
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = palette.primary.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "Zikir Seç ▾",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = palette.primary
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Üst Araç Çubuğu: Hedef Belirle, Titreşim, Sıfırla (Erişilebilir & Her Cihaza Uygun)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hedef Belirle Butonu (Geniş basma alanı)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isTargetReached) palette.accent.copy(alpha = 0.25f) else palette.surface,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                listOf(palette.primary.copy(alpha = 0.5f), palette.accent.copy(alpha = 0.3f))
                            )
                        ),
                        modifier = Modifier
                            .clickable {
                                targetInputText = activeTarget.toString()
                                showTargetDialog = true
                            }
                            .testTag("set_target_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrackChanges,
                                contentDescription = "Zikir Hedefi",
                                tint = if (isTargetReached) palette.accent else palette.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Hedef: $activeTarget",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTargetReached) palette.accent else palette.primary
                                )
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Titreşim (En az 44dp dokunma hedefi)
                        IconButton(
                            onClick = onToggleVibration,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(palette.surface)
                                .testTag("toggle_vibration_button")
                        ) {
                            Icon(
                                imageVector = if (isVibrationEnabled) Icons.Default.Vibration else Icons.Outlined.Vibration,
                                contentDescription = if (isVibrationEnabled) "Titreşimi Kapat" else "Titreşimi Aç",
                                tint = if (isVibrationEnabled) palette.primary else palette.textMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Sıfırla (En az 44dp dokunma hedefi)
                        IconButton(
                            onClick = onReset,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(palette.surface)
                                .testTag("reset_dhikr_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sayacı Sıfırla",
                                tint = palette.textSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Ortadaki Büyük Zikirmatik Sayacı & Kadran (Her Ekran Boyutuna Uyarlanır)
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val dialSize = (maxWidth * 0.72f).coerceIn(240.dp, 300.dp)
                    Box(
                        modifier = Modifier
                            .size(dialSize)
                            .testTag("dhikr_counter_dial"),
                        contentAlignment = Alignment.Center
                    ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 11.dp.toPx()
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.width / 2 - stroke

                        // Arka Plan Çemberi
                        drawCircle(
                            color = palette.surfaceVariant,
                            radius = radius,
                            center = center,
                            style = Stroke(width = stroke)
                        )

                        // İlerleme Yayı
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(palette.primary, palette.accent, palette.primary),
                                center = center
                            ),
                            startAngle = -90f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }

                    // Kadran İçi Bilgileri
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = preset.arabic,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.secondary
                            ),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Sayı Göstergesi
                        Text(
                            text = "$dhikrCount",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = if (isTargetReached) palette.accent else palette.textPrimary,
                                fontSize = 56.sp
                            )
                        )

                        // Tur ve Hedef Rozeti
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isTargetReached) palette.accent.copy(alpha = 0.25f) else palette.surfaceVariant
                            ) {
                                Text(
                                    text = if (isTargetReached) "Hedef Tamamlandı! ✨" else "Hedef: $activeTarget",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isTargetReached) palette.accent else palette.textSecondary
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            if (completedLaps > 0) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = palette.primary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "$completedLaps Tur",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = palette.primary
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                }

                // Alt Kısım: Dev Zikir Dokunma Butonu (Her Cihaza Uygun & Ergonomik)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = palette.primary,
                        shadowElevation = 10.dp,
                        modifier = Modifier
                            .size(100.dp)
                            .scale(buttonScale)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = onIncrement
                            )
                            .testTag("dhikr_increment_button")
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = "Zikir Say",
                                tint = Color.Black,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // ==========================================
            // 2. SEKME: ZİKİR DEFTERİ & ÇETELE SAYFASI
            // (Kullanıcı Talebi: "ne kadar hangi zikirleri çektiğide bir defter gibi sayfaya konsun")
            // ==========================================
            DhikrNotebookView(
                entries = dhikrBookEntries,
                palette = palette,
                headerContent = topHeaderContent,
                onSelectToCount = { entry ->
                    onSelectPresetById(entry.dhikrId)
                    activeTab = DhikrScreenTab.COUNTER
                },
                onResetEntry = onResetBookEntry
            )
        }
    }

    // ==========================================
    // ZİKİR SEÇME KUTUCUĞU (DIALOG / POPUP)
    // (Kullanıcı Talebi: "ordan kutucuk çıkıp insanlar zikirini seçsin")
    // ==========================================
    if (showDhikrPickerBox) {
        DhikrPickerBoxDialog(
            currentPresetId = preset.id,
            onSelect = { selectedIndex ->
                onSelectPreset(selectedIndex)
                showDhikrPickerBox = false
            },
            onDismiss = { showDhikrPickerBox = false },
            palette = palette
        )
    }

    // Hedef Belirleme Dialogu
    if (showTargetDialog) {
        AlertDialog(
            onDismissRequest = { showTargetDialog = false },
            title = {
                Text(
                    text = "Zikir Hedefi Belirle",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Hedeflenen zikir sayısını seçin veya özel bir sayı girin:",
                        style = MaterialTheme.typography.bodyMedium.copy(color = palette.textSecondary)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(33, 99, 100, 500, 1000).forEach { num ->
                            FilterChip(
                                selected = targetInputText == num.toString(),
                                onClick = { targetInputText = num.toString() },
                                label = { Text("$num") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = targetInputText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 6) {
                                targetInputText = input
                            }
                        },
                        label = { Text("Özel Hedef Sayısı") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = targetInputText.toIntOrNull()
                        if (parsed != null && parsed > 0) {
                            customTarget = parsed
                        }
                        showTargetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.primary)
                ) {
                    Text("Hedefi Kaydet", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTargetDialog = false }) {
                    Text("İptal", color = palette.textSecondary)
                }
            },
            containerColor = palette.surface
        )
    }
}

/**
 * Kullanıcı Talebi:
 * "zikir kısmına zikir seçme bölümü yap ve ordan kutucuk çıkıp insanlar zikirini seçsin"
 * Açılan zengin zikir seçim kutucuğu (Arama, Arapça metin, Fazilet ve Tavsiye Hedef)
 */
@Composable
private fun DhikrPickerBoxDialog(
    currentPresetId: String,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
    palette: AppPalette
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            WisdomDatabase.allDhikrPresets.mapIndexed { index, item -> Pair(index, item) }
        } else {
            WisdomDatabase.allDhikrPresets.mapIndexed { index, item -> Pair(index, item) }
                .filter { (_, item) ->
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.meaning.contains(searchQuery, ignoreCase = true) ||
                    item.virtues.contains(searchQuery, ignoreCase = true)
                }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .testTag("dhikr_picker_dialog"),
        containerColor = palette.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Zikir Seçiniz",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Kapat", tint = palette.textSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Arama Kutucuğu
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Zikir veya fazilet ara...", color = palette.textMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = palette.textMuted)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Temizle", tint = palette.textMuted)
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedContainerColor = palette.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = palette.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "${filteredList.size} Mübarek Zikir Mevcut",
                    style = MaterialTheme.typography.labelSmall.copy(color = palette.textSecondary)
                )

                // Zikir Kartları Listesi (Kutucuk içi)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList) { (originalIndex, item) ->
                        val isSelected = item.id == currentPresetId
                        Surface(
                            onClick = { onSelect(originalIndex) },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) palette.primary.copy(alpha = 0.15f) else palette.surfaceVariant.copy(alpha = 0.4f),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = if (isSelected) Brush.horizontalGradient(listOf(palette.primary, palette.accent))
                                else Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.06f), Color.Transparent))
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("select_dhikr_${item.id}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) palette.primary else palette.textPrimary
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = palette.surfaceVariant
                                    ) {
                                        Text(
                                            text = "Hedef: ${item.target}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = palette.accent,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                // Arapça Hat
                                Text(
                                    text = item.arabic,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = palette.secondary
                                    ),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )

                                // Anlam
                                Text(
                                    text = item.meaning,
                                    style = MaterialTheme.typography.bodySmall.copy(color = palette.textSecondary),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (item.virtues.isNotEmpty()) {
                                    Text(
                                        text = "✦ ${item.virtues}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = palette.primary.copy(alpha = 0.85f),
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}

/**
 * Kullanıcı Talebi:
 * "ne kadar hangi zikirleri çektiğide bir defter gibi sayfaya konsun"
 * Zikir Defteri & Çetele Görünümü:
 * - Toplam çekilen zikirler, bugün çekilenler, hedefler
 * - Kalıcı defter çetelesi
 */
@Composable
private fun DhikrNotebookView(
    entries: List<DhikrBookEntry>,
    palette: AppPalette,
    headerContent: @Composable () -> Unit,
    onSelectToCount: (DhikrBookEntry) -> Unit,
    onResetEntry: (String) -> Unit
) {
    val totalDhikrAllTime = remember(entries) { entries.sumOf { it.totalCount } }
    val totalDhikrToday = remember(entries) { entries.sumOf { it.todayCount } }
    val totalLaps = remember(entries) { entries.sumOf { it.completedLaps } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dhikr_notebook_page"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Üst Başlık & Sekme Geçişi (Sayfayla birlikte kayar)
        item {
            headerContent()
        }

        // Defter Başlık & Özet Kartı
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = palette.surfaceVariant.copy(alpha = 0.6f)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(palette.primary.copy(alpha = 0.6f), palette.accent.copy(alpha = 0.4f)))
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoStories,
                                contentDescription = null,
                                tint = palette.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Zikir & Tesbihât Defterim",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = palette.accent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Kalıcı Kayıt",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = palette.accent,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // 3'lü İstatistik Çetelesi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Toplam Çekilen
                        NotebookStatBox(
                            title = "Toplam Zikir",
                            value = "$totalDhikrAllTime",
                            color = palette.primary,
                            palette = palette,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Bugün Çekilen
                        NotebookStatBox(
                            title = "Bugün",
                            value = "$totalDhikrToday",
                            color = palette.accent,
                            palette = palette,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Tamamlanan Turlar
                        NotebookStatBox(
                            title = "Hatim/Tur",
                            value = "$totalLaps",
                            color = palette.secondary,
                            palette = palette,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Defter Açıklaması
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Zikir Çetele Kayıtları (${entries.size})",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.textSecondary
                    )
                )
                Text(
                    text = "Otomatik Kaydedilir",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = palette.textMuted
                    )
                )
            }
        }

        if (entries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.surface.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = palette.textMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "Defteriniz Henüz Boş",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                        )
                        Text(
                            text = "Sayaç sekmesine geçip zikir çektikçe, çektiğiniz tüm zikirler ve adetleri bu deftere otomatik ve kalıcı olarak kaydedilecektir.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = palette.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        } else {
            items(entries) { entry ->
                DhikrBookRowCard(
                    entry = entry,
                    palette = palette,
                    onCountClick = { onSelectToCount(entry) },
                    onResetClick = { onResetEntry(entry.dhikrId) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NotebookStatBox(
    title: String,
    value: String,
    color: Color,
    palette: AppPalette,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = palette.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = color
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = palette.textSecondary,
                    fontSize = 10.sp
                )
            )
        }
    }
}

/**
 * Zikir Defteri Satır Kartı (Tıpkı hat sanatı çetele defteri sayfası gibi)
 */
@Composable
private fun DhikrBookRowCard(
    entry: DhikrBookEntry,
    palette: AppPalette,
    onCountClick: () -> Unit,
    onResetClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(palette.primary.copy(alpha = 0.25f), Color.Transparent)
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dhikr_book_card_${entry.dhikrId}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                    )
                    Text(
                        text = entry.arabic,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.secondary
                        )
                    )
                }

                // Sayı ve Çetele Rozeti
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${entry.totalCount}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = palette.primary
                        )
                    )
                    Text(
                        text = "Toplam Adet",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = palette.textSecondary,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            // Çetele İlerleme Çubuğu ve Detaylar
            val target = if (entry.customTarget > 0) entry.customTarget else 33
            val lapProgress = ((entry.totalCount % target).toFloat() / target.toFloat()).coerceIn(0f, 1f)

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                LinearProgressIndicator(
                    progress = { lapProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = palette.accent,
                    trackColor = palette.surfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Bugün: ${entry.todayCount} adet • ${entry.completedLaps} Tamamlanan Tur",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = palette.textSecondary,
                            fontSize = 11.sp
                        )
                    )
                    if (entry.lastUpdatedDate.isNotEmpty()) {
                        Text(
                            text = "Son: ${entry.lastUpdatedDate}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = palette.textMuted,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            // Aksiyon Butonları
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onResetClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = palette.textMuted)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Zikri Sıfırla",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sıfırla", fontSize = 12.sp)
                }

                Button(
                    onClick = onCountClick,
                    colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = "Zikre Başla",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Bu Zikri Çek",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                }
            }
        }
    }
}
