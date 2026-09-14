package com.example.ui.screens

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
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
import com.example.model.AsmaUlHusna
import com.example.ui.theme.LocalAppPalette
import java.util.Locale

/**
 * Kullanıcı Talebi:
 * "esmaül hüsnada tıkladığımızda okusun sesli bir şekilde"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsmaUlHusnaScreen(
    onSelectForDhikr: (AsmaUlHusna) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = LocalAppPalette.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedNameForDetail by remember { mutableStateOf<AsmaUlHusna?>(null) }
    var speakingAsmaNumber by remember { mutableIntStateOf(-1) }

    // Text To Speech Initialization for authentic voice pronunciation
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(context) {
        var speech: TextToSpeech? = null
        speech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                speech?.language = Locale("tr", "TR")
                speech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        speakingAsmaNumber = -1
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        speakingAsmaNumber = -1
                    }
                })
                isTtsReady = true
            }
        }
        tts = speech

        onDispose {
            speech?.stop()
            speech?.shutdown()
        }
    }

    fun speakName(item: AsmaUlHusna) {
        speakingAsmaNumber = item.number
        val pronounceText = if (item.number == 1) {
            "Allah Celle Celâlühü. Kendisinden başka ilâh olmayan tek yaratıcı."
        } else {
            "Yâ ${item.transliteration}. ${item.turkishMeaning}"
        }
        try {
            tts?.speak(pronounceText, TextToSpeech.QUEUE_FLUSH, null, "asma_${item.number}")
        } catch (_: Exception) {}
    }

    val filteredNames = remember(searchQuery) {
        if (searchQuery.isBlank()) IslamicDatabase.asmaUlHusna
        else IslamicDatabase.asmaUlHusna.filter {
            it.transliteration.contains(searchQuery, ignoreCase = true) ||
            it.turkishMeaning.contains(searchQuery, ignoreCase = true) ||
            it.number.toString() == searchQuery.trim()
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
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
                        text = "Esmâü'l-Hüsnâ",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                    )
                    Text(
                        text = "Sesli Dinleme • 99 Güzel İsim ve Faziletleri",
                        style = MaterialTheme.typography.bodySmall.copy(color = palette.textSecondary)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.primary.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = palette.primary, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Sesli Okuma Aktif",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.primary
                            )
                        )
                    }
                }
            }
        }

        // Search Bar
        item(span = { GridItemSpan(maxLineSpan) }) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("İsim veya anlam ara...", color = palette.textMuted) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = palette.primary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
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
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Grid of Names with Audio Playback on Click
        items(filteredNames, key = { it.number }) { item ->
            val isSpeaking = speakingAsmaNumber == item.number

            AsmaCardItem(
                item = item,
                isSpeaking = isSpeaking,
                onClick = {
                    // Kullanıcı isteği: Karta basınca anında hem seslendir hem detay aç
                    speakName(item)
                    selectedNameForDetail = item
                },
                onOpenDetail = {
                    speakName(item)
                    selectedNameForDetail = item
                },
                onQuickZikir = {
                    onSelectForDhikr(item)
                }
            )
        }
    }

    // Detail Bottom Sheet with Audio Pronunciation Button
    selectedNameForDetail?.let { item ->
        ModalBottomSheet(
            onDismissRequest = { selectedNameForDetail = null },
            containerColor = palette.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = palette.primary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${item.number}. İsm-i Şerîf • Ebced: ${item.ebcedValue}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.primary
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = item.arabic,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.secondary
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = item.transliteration,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    ),
                    textAlign = TextAlign.Center
                )

                // Sesli Dinle Butonu
                OutlinedButton(
                    onClick = { speakName(item) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.primary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(palette.primary, palette.accent))
                    )
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("İsmi Sesli Dinle (Yâ ${item.transliteration})")
                }

                Text(
                    text = item.turkishMeaning,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = palette.textSecondary,
                        lineHeight = 22.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Divider(color = Color.White.copy(alpha = 0.08f))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = palette.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Zikrin Fazileti ve Sırrı:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.accent
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.virtue,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = palette.textPrimary,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }

                Button(
                    onClick = {
                        onSelectForDhikr(item)
                        selectedNameForDetail = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bu İsmi Zikirmatikte Çek (${item.ebcedValue} Defa)",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun AsmaCardItem(
    item: AsmaUlHusna,
    isSpeaking: Boolean,
    onClick: () -> Unit,
    onOpenDetail: () -> Unit,
    onQuickZikir: () -> Unit
) {
    val palette = LocalAppPalette.current

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isSpeaking) palette.surfaceVariant else palette.surface,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                if (isSpeaking) listOf(palette.primary, palette.accent)
                else listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
            ),
            width = if (isSpeaking) 2.dp else 1.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(174.dp)
            .clickable(onClick = onClick)
            .testTag("asma_item_${item.number}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isSpeaking) palette.primary else palette.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${item.number}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSpeaking) Color.Black else palette.primary,
                            fontSize = 11.sp
                        )
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Speaker wave animation / icon
                    Surface(
                        shape = CircleShape,
                        color = if (isSpeaking) palette.accent.copy(alpha = 0.25f) else Color.Transparent,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                                contentDescription = "Seslendir",
                                tint = if (isSpeaking) palette.accent else palette.textMuted,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    // Hızlı Zikirmatik butonu
                    IconButton(
                        onClick = onQuickZikir,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = "Zikirmatikte Çek",
                            tint = palette.primary,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Detail button
                    IconButton(
                        onClick = onOpenDetail,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Detay",
                            tint = palette.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Arabic text
            Text(
                text = item.arabic,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSpeaking) palette.primary else palette.secondary
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = item.transliteration,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.turkishMeaning,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = palette.textSecondary,
                        fontSize = 10.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
