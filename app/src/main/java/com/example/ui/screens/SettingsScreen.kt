package com.example.ui.screens

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import java.io.File
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm.AlarmPermissionHelper
import com.example.audio.AdhanAudioPlayer
import com.example.data.AppPreferencesRepository
import com.example.data.LocationVerificationHelper
import com.example.data.QuranAudioPlayer
import com.example.data.LocationVerificationState
import com.example.model.AppTheme
import com.example.model.City
import com.example.model.CompassDialStyle
import com.example.model.DarkModePreference
import com.example.model.PrayerTimeItem
import com.example.model.PrayerType
import com.example.model.ThemeMotif
import com.example.ui.components.ThemeMotifBadge
import com.example.ui.components.ThemeMotifWatermark
import com.example.ui.theme.LocalAppPalette
import com.example.widget.PrayerWidgetProvider
import com.example.widget.SmallPrayerWidgetProvider
import com.example.widget.WidgetThemeRenderer
import com.example.worker.PrayerWorkManagerHelper

enum class SettingsFilter(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    ALL("Tümü", Icons.Default.AllInclusive),
    PRAYER("Vakit & Ezan", Icons.Default.NotificationsActive),
    APPEARANCE("Görünüm", Icons.Default.Palette),
    LOCATION("Konum & Kıble", Icons.Default.LocationOn),
    SYSTEM("Sistem & İzinler", Icons.Default.Tune)
}

@Composable
fun SettingsScreen(
    selectedTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    darkModePreference: DarkModePreference = DarkModePreference.SYSTEM,
    onDarkModePreferenceChange: (DarkModePreference) -> Unit = {},
    isEyeComfortEnabled: Boolean = true,
    onToggleEyeComfort: () -> Unit = {},
    isLargeFontEnabled: Boolean = false,
    onToggleLargeFont: () -> Unit = {},
    selectedCity: City,
    onCityChange: (City) -> Unit,
    calculationMethod: String,
    onCalculationMethodChange: (String) -> Unit,
    precautionMinutes: Int,
    onPrecautionMinutesChange: (Int) -> Unit,
    isEarlyReminderEnabled: Boolean,
    onToggleEarlyReminder: () -> Unit,
    selectedEzanVoice: String,
    onEzanVoiceChange: (String) -> Unit,
    prayerEzanVoices: Map<PrayerType, String> = emptyMap(),
    onPrayerEzanVoiceChange: (PrayerType, String) -> Unit = { _, _ -> },
    isPlayingEzan: Boolean = false,
    activePlayingVoiceName: String? = null,
    onTestSound: (String?) -> Unit = {},
    onStopSound: () -> Unit = {},
    isVibrationEnabled: Boolean,
    onToggleVibration: () -> Unit,
    locationState: LocationVerificationState,
    onVerifyLocation: () -> Unit,
    widgetThemeOption: String = "FOLLOW_APP",
    onWidgetThemeOptionChange: (String) -> Unit = {},
    widgetMotifOption: String = "FOLLOW_THEME",
    onWidgetMotifOptionChange: (String) -> Unit = {},
    widgetBackgroundOpacity: Int = 92,
    onWidgetBackgroundOpacityChange: (Int) -> Unit = {},
    widgetMotifWatermarkEnabled: Boolean = true,
    onToggleWidgetMotifWatermark: () -> Unit = {},
    currentPrayerTimeItem: PrayerTimeItem? = null,
    nextPrayerTimeItem: PrayerTimeItem? = null,
    countdownText: String = "--:--",
    compassDialStyle: CompassDialStyle = CompassDialStyle.CLASSIC_KAABA,
    onCompassDialStyleChange: (CompassDialStyle) -> Unit = {},
    isCompassHapticEnabled: Boolean = true,
    onToggleCompassHaptic: () -> Unit = {},
    isCompassSoundEnabled: Boolean = false,
    onToggleCompassSound: () -> Unit = {},
    isCompassLevelEnabled: Boolean = true,
    onToggleCompassLevel: () -> Unit = {},
    isCompassTrueNorthEnabled: Boolean = true,
    onToggleCompassTrueNorth: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var currentFilter by remember { mutableStateOf(SettingsFilter.ALL) }

    // Runtime Permission Launcher for GPS Location
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            onVerifyLocation()
            Toast.makeText(context, "Konum izni verildi, konum doğrulanıyor...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Konum tespiti için izin gerekli", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Column(modifier = Modifier.padding(top = 4.dp)) {
            Text(
                text = "Ayarlar",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
            )
            Text(
                text = "Konum, tema, ezan ve hesaplama tercihleri",
                style = MaterialTheme.typography.bodySmall.copy(color = palette.textSecondary)
            )
        }

        // Kategori Hızlı Filtreleri (Ayarları daha kullanışlı & erişilebilir hale getirir)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsFilter.entries.forEach { filter ->
                val isSelected = currentFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { currentFilter = filter },
                    label = {
                        Text(
                            text = filter.label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = filter.icon,
                            contentDescription = filter.label,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = palette.primary,
                        selectedLabelColor = Color.Black,
                        selectedLeadingIconColor = Color.Black,
                        containerColor = palette.surfaceVariant.copy(alpha = 0.5f),
                        labelColor = palette.textSecondary,
                        iconColor = palette.textSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // ==========================================
        // 1. KONUM & GPS DOĞRULAMASI
        // ==========================================
        if (currentFilter == SettingsFilter.ALL || currentFilter == SettingsFilter.LOCATION) {
            SettingsSectionCard(
            title = "KONUM DOĞRULAMASI & GPS",
            icon = Icons.Default.GpsFixed,
            palette = palette
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Status Row
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = palette.surfaceVariant.copy(alpha = 0.6f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(
                                if (locationState is LocationVerificationState.Verified) palette.accent else palette.primary,
                                Color.Transparent
                            )
                        )
                    )
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (locationState is LocationVerificationState.Verified) Icons.Default.CheckCircle else Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (locationState is LocationVerificationState.Verified) palette.accent else palette.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = when (locationState) {
                                        is LocationVerificationState.Verified -> "GPS Doğrulandı: ${locationState.nearestCity.name}"
                                        is LocationVerificationState.Checking -> "Konum Aranıyor..."
                                        is LocationVerificationState.PermissionNeeded -> "Konum İzni Gerekli"
                                        is LocationVerificationState.Error -> "Doğrulama Başarısız"
                                        LocationVerificationState.Idle -> "Seçili Şehir: ${selectedCity.name}"
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = palette.textPrimary
                                    )
                                )
                                Text(
                                    text = when (locationState) {
                                        is LocationVerificationState.Verified -> "Uzaklık: ${"%.1f".format(locationState.distanceKm)} km | Koordinat: ${"%.4f".format(locationState.detectedLat)}°, ${"%.4f".format(locationState.detectedLng)}°"
                                        is LocationVerificationState.Checking -> "Uydulardan hassas koordinat alınıyor..."
                                        is LocationVerificationState.PermissionNeeded -> "Otomatik en yakın il tespiti için izin verin"
                                        is LocationVerificationState.Error -> locationState.message
                                        LocationVerificationState.Idle -> "Diyanet takvimi koordinatlarına göre hesaplanıyor"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(color = palette.textSecondary, fontSize = 11.sp)
                                )
                            }
                        }
                    }
                }

                // Verify Button
                Button(
                    onClick = {
                        if (!LocationVerificationHelper.hasLocationPermission(context)) {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        } else {
                            onVerifyLocation()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("verify_location_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = palette.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = Color(0xFF1E1400),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GPS ile Konumu Doğrula & En Yakın İli Bul",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(0xFF1E1400),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // If verified, option to apply nearest city
                if (locationState is LocationVerificationState.Verified && locationState.nearestCity.id != selectedCity.id) {
                    OutlinedButton(
                        onClick = {
                            onCityChange(locationState.nearestCity)
                            Toast.makeText(context, "${locationState.nearestCity.name} şehri uygulandı", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.accent),
                        border = ButtonDefaults.outlinedButtonBorder().copy(brush = Brush.horizontalGradient(listOf(palette.accent, palette.primary)))
                    ) {
                        Text(
                            text = "Tespit Edilen '${locationState.nearestCity.name}' Şehrini Ayarla",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
        }

        // ==========================================
        // 2. UYGULAMA YAZI BOYUTU (ERİŞİLEBİLİRLİK)
        // ==========================================
        if (currentFilter == SettingsFilter.ALL || currentFilter == SettingsFilter.APPEARANCE) {
            SettingsSectionCard(
            title = "UYGULAMA YAZI BOYUTU (ERİŞİLEBİLİRLİK)",
            icon = Icons.Filled.FormatSize,
            palette = palette
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Bilgilendirme Rozeti
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.primary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TextFields,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isLargeFontEnabled) "Büyük Okunabilir Yazılar Aktif" else "Standart Yazı Boyutu",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary
                                )
                            )
                            Text(
                                text = if (isLargeFontEnabled) "Tüm dua, ayet, vakit ve menüler %25 daha büyük ve net gösterilir." else "Yazıları büyütmek için aşağıdaki seçeneği etkinleştirebilirsiniz.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = palette.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                // Yazı Büyütme Switch Satırı
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isLargeFontEnabled) palette.primary.copy(alpha = 0.15f) else palette.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(
                        width = if (isLargeFontEnabled) 1.5.dp else 1.dp,
                        color = if (isLargeFontEnabled) palette.primary else palette.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onToggleLargeFont() }
                        .testTag("large_font_switch_container")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isLargeFontEnabled) palette.primary.copy(alpha = 0.25f) else palette.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ZoomIn,
                                contentDescription = null,
                                tint = if (isLargeFontEnabled) palette.primary else palette.textSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Uygulama yazılarını büyüt",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary
                                )
                            )
                            Text(
                                text = "Küçük harfleri ve ince yazıları okumakta zorlananlar için büyük, ferah ve okunaklı hale dönüştürür.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = palette.textSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }

                        Switch(
                            checked = isLargeFontEnabled,
                            onCheckedChange = { onToggleLargeFont() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = palette.surface,
                                checkedTrackColor = palette.primary,
                                uncheckedThumbColor = palette.textMuted,
                                uncheckedTrackColor = palette.surfaceVariant
                            )
                        )
                    }
                }

                // Canlı Metin Önizleme Kutusu
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, palette.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Canlı Okunabilirlik Önizlemesi:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = palette.textSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = palette.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "«Namaz dinin direğidir, mü'minin miracıdır.»",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = palette.textPrimary,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        )
                    }
                }
            }
        }
        }

        // ==========================================
        // 3. İSLAMİ SANAT & RENK TEMASI
        // ==========================================
        if (currentFilter == SettingsFilter.ALL || currentFilter == SettingsFilter.APPEARANCE) {
            SettingsSectionCard(
            title = "İSLAMİ SANAT & RENK TEMASI",
            icon = Icons.Outlined.Palette,
            palette = palette
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppTheme.entries.forEach { themeOption ->
                    val isSelected = themeOption == selectedTheme
                    Surface(
                        onClick = { onThemeChange(themeOption) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) palette.surfaceVariant else palette.surfaceVariant.copy(alpha = 0.4f),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = if (isSelected) Brush.horizontalGradient(listOf(palette.primary, palette.accent))
                            else Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.06f), Color.Transparent))
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("theme_item_${themeOption.id}")
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            // Arka plan tematik filigran motifi (mobilde taşmaz)
                            ThemeMotifWatermark(
                                motif = themeOption.motif,
                                primaryColor = themeOption.previewPrimary,
                                accentColor = themeOption.previewAccent,
                                alpha = if (isSelected) 0.18f else 0.08f,
                                modifier = Modifier
                                    .size(72.dp)
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 40.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    // Temaya Özel Sanatsal Motif Rozeti
                                    ThemeMotifBadge(
                                        motif = themeOption.motif,
                                        primaryColor = themeOption.previewPrimary,
                                        accentColor = themeOption.previewAccent,
                                        size = 40.dp,
                                        backgroundColor = themeOption.previewBg
                                    )

                                    Column(modifier = Modifier.weight(1f, fill = false)) {
                                        Text(
                                            text = themeOption.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) palette.secondary else palette.textPrimary
                                            )
                                        )
                                        Text(
                                            text = themeOption.subtitle,
                                            style = MaterialTheme.typography.labelSmall.copy(color = palette.textSecondary, fontSize = 11.sp)
                                        )
                                        // Temanın özgün İslami motif adı
                                        Text(
                                            text = "Motif: ${themeOption.motifTitle}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSelected) palette.primary else palette.textMuted,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Seçili",
                                        tint = palette.accent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        }

        // ==========================================
        // 4. GÖRÜNÜM MODU & GÖZ SAĞLIĞI
        // ==========================================
        if (currentFilter == SettingsFilter.ALL || currentFilter == SettingsFilter.APPEARANCE) {
            SettingsSectionCard(
            title = "GÖRÜNÜM MODU & GÖZ DİNLENDİRME",
            icon = Icons.Outlined.DarkMode,
            palette = palette
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Karanlık / Aydınlık Mod:",
                    style = MaterialTheme.typography.labelMedium.copy(color = palette.textSecondary)
                )

                DarkModePreference.entries.forEach { pref ->
                    val isSelected = pref == darkModePreference
                    Surface(
                        onClick = { onDarkModePreferenceChange(pref) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) palette.surfaceVariant else palette.surfaceVariant.copy(alpha = 0.4f),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = if (isSelected) Brush.horizontalGradient(listOf(palette.primary, palette.accent))
                            else Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.06f), Color.Transparent))
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dark_mode_option_${pref.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pref.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) palette.secondary else palette.textPrimary
                                    )
                                )
                                Text(
                                    text = pref.subtitle,
                                    style = MaterialTheme.typography.labelSmall.copy(color = palette.textSecondary, fontSize = 11.sp)
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Seçili",
                                    tint = palette.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp))

                // Göz Dinlendirme Kalkanı
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = palette.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Visibility,
                                contentDescription = null,
                                tint = if (isEyeComfortEnabled) palette.primary else palette.textSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Göz Dinlendirme Kalkanı",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = palette.textPrimary
                                    )
                                )
                                Text(
                                    text = "Gözü yormayan yumuşak renk tonları ve mavi ışık koruması",
                                    style = MaterialTheme.typography.labelSmall.copy(color = palette.textSecondary, fontSize = 11.sp)
                                )
                            }
                        }
                        Switch(
                            checked = isEyeComfortEnabled,
                            onCheckedChange = { onToggleEyeComfort() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = palette.primary,
                                checkedTrackColor = palette.primary.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("toggle_eye_comfort")
                        )
                    }
                }
            }
        }
        }

        // ==========================================
        // 3. EZAN & BİLDİRİM SESLERİ
        // ==========================================
        if (currentFilter == SettingsFilter.ALL || currentFilter == SettingsFilter.PRAYER) {
            SettingsSectionCard(
            title = "EZAN SESİ & HATIRLATICI",
            icon = Icons.Outlined.VolumeUp,
            palette = palette
        ) {
            val voices = AdhanAudioPlayer.AVAILABLE_VOICES

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Genel Ezan Melodisi:",
                    style = MaterialTheme.typography.labelMedium.copy(color = palette.textSecondary)
                )

                // Voice chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    voices.take(8).forEach { voice ->
                        val isSelected = voice == selectedEzanVoice
                        val isVoicePlaying = isPlayingEzan && activePlayingVoiceName == voice

                        Surface(
                            onClick = { onEzanVoiceChange(voice) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) palette.surfaceVariant else Color.Transparent,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = if (isSelected) Brush.horizontalGradient(listOf(palette.primary, Color.Transparent))
                                else Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.05f), Color.Transparent))
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (isVoicePlaying) onStopSound() else onTestSound(voice)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isVoicePlaying) Icons.Default.Stop else Icons.Default.VolumeUp,
                                            contentDescription = "Dinle",
                                            tint = if (isVoicePlaying) palette.primary else palette.textMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = voice,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (isSelected) palette.secondary else palette.textPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = palette.primary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                // Test Sound Button (Interactive Ezan Okuma & Durdurma)
                Button(
                    onClick = {
                        if (isPlayingEzan) {
                            onStopSound()
                        } else {
                            onTestSound(selectedEzanVoice)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlayingEzan) palette.primary else palette.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("test_sound_button")
                ) {
                    Icon(
                        imageVector = if (isPlayingEzan) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (isPlayingEzan) Color.White else palette.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPlayingEzan) "Ezan Sesini Durdur" else "Sesi Test Et (Ezanı Dinle & Titreşim)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (isPlayingEzan) Color.White else palette.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                HorizontalDivider(
                    color = if (palette.isDark) Color.White.copy(alpha = 0.08f) else palette.textMuted.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // =========================================================
                // HER VAKİT İÇİN AYRI EZAN MAKAMI & BİLDİRİM SESİ MENÜSÜ
                // =========================================================
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = palette.primary, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Vakit Bazlı Ezan Makamları & Sesler:",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                        )
                    }
                    Text(
                        text = "Her namaz vakti için geleneksel Türk musikisi makamı veya özel ezan sesi seçebilirsiniz.",
                        style = MaterialTheme.typography.bodySmall.copy(color = palette.textMuted, fontSize = 11.sp)
                    )

                    val makamOptionsByPrayer = mapOf(
                        PrayerType.IMSAK to listOf(
                            "Saba Makamı (Sabah Ezanı)",
                            "Hüzzam Makamı (Duygulu Sabah)",
                            "İstanbul Ezanı (Saba Makamı - Sabah)",
                            "Mekke-i Mükerreme Ezanı (Kâbe-i Muazzama)",
                            "Medine-i Münevvere Ezanı (Mescid-i Nebevî)",
                            "Kudüs Mescid-i Aksâ Ezanı",
                            "Kahire Ezanı (Şeyh Abdulbasit)",
                            "Kısa Zil / Bip",
                            "Sessiz"
                        ),
                        PrayerType.GUNES to listOf(
                            "Kısa Ney Sesi (Kerâhat Çıkışı)",
                            "Kısa Ney Taksimi (Mistik Dinlendirici)",
                            "Hafif Çan / Bip",
                            "Gündoğumu Bildirim Tonu",
                            "Sessiz"
                        ),
                        PrayerType.OGLE to listOf(
                            "Rast Makamı (Klasik Türk Öğle Ezanı)",
                            "Hicaz Makamı (Duygulu Ezan)",
                            "Nihavend Makamı (Ferahlatıcı Türk Ezanı)",
                            "Mekke-i Mükerreme Ezanı (Kâbe-i Muazzama)",
                            "Medine-i Münevvere Ezanı (Mescid-i Nebevî)",
                            "Mısır Ezanı (Şeyh Mustafa İsmail)",
                            "Kısa Zil / Bip",
                            "Sessiz"
                        ),
                        PrayerType.IKINDI to listOf(
                            "Hicaz Makamı (Duygulu İkindi Ezanı)",
                            "Uşşak Makamı (Huzur Dolu Yatsı Ezanı)",
                            "Mekke-i Mükerreme Ezanı (Kâbe-i Muazzama)",
                            "Medine-i Münevvere Ezanı (Mescid-i Nebevî)",
                            "Kudüs Mescid-i Aksâ Ezanı",
                            "Kısa Zil / Bip",
                            "Sessiz"
                        ),
                        PrayerType.AKSAM to listOf(
                            "Segâh Makamı (Akşam Ezanı - Hızlı & Coşkulu)",
                            "Rast Makamı (Klasik Türk Öğle Ezanı)",
                            "Kısa İftar Zili / Ramazan Top Sesi",
                            "Mekke-i Mükerreme Ezanı (Kâbe-i Muazzama)",
                            "Medine-i Münevvere Ezanı (Mescid-i Nebevî)",
                            "Sessiz"
                        ),
                        PrayerType.YATSI to listOf(
                            "Uşşak Makamı (Huzur Dolu Yatsı Ezanı)",
                            "Bayatî / Rast Makamı",
                            "Hicaz Makamı (Duygulu İkindi Ezanı)",
                            "Mekke-i Mükerreme Ezanı (Kâbe-i Muazzama)",
                            "Medine-i Münevvere Ezanı (Mescid-i Nebevî)",
                            "Kısa Ney Taksimi (Mistik Dinlendirici)",
                            "Sessiz"
                        )
                    )

                    var selectedPrayerForVoiceDialog by remember { mutableStateOf<PrayerType?>(null) }

                    PrayerType.entries.forEach { prayer ->
                        val currentVoice = prayerEzanVoices[prayer] ?: when (prayer) {
                            PrayerType.IMSAK -> "Saba Makamı (Sabah Ezanı)"
                            PrayerType.GUNES -> "Kısa Ney Sesi (Kerâhat Çıkışı)"
                            PrayerType.OGLE -> "Rast Makamı (Klasik Türk Öğle Ezanı)"
                            PrayerType.IKINDI -> "Hicaz Makamı (Duygulu İkindi Ezanı)"
                            PrayerType.AKSAM -> "Segâh Makamı (Akşam Ezanı - Hızlı & Coşkulu)"
                            PrayerType.YATSI -> "Uşşak Makamı (Huzur Dolu Yatsı Ezanı)"
                        }

                        val isThisRowPlaying = isPlayingEzan && activePlayingVoiceName == currentVoice

                        Surface(
                            onClick = { selectedPrayerForVoiceDialog = prayer },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isThisRowPlaying) palette.primary.copy(alpha = 0.12f) else palette.surfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (isThisRowPlaying) palette.primary.copy(alpha = 0.6f) else if (palette.isDark) Color.White.copy(alpha = 0.08f) else palette.textMuted.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("prayer_voice_${prayer.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isThisRowPlaying) palette.primary else palette.primary.copy(alpha = 0.15f),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = when (prayer) {
                                                    PrayerType.IMSAK -> "İ"
                                                    PrayerType.GUNES -> "G"
                                                    PrayerType.OGLE -> "Ö"
                                                    PrayerType.IKINDI -> "İ"
                                                    PrayerType.AKSAM -> "A"
                                                    PrayerType.YATSI -> "Y"
                                                },
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isThisRowPlaying) Color.White else palette.primary
                                                )
                                            )
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = when (prayer) {
                                                PrayerType.IMSAK -> "İmsak (Sabah)"
                                                PrayerType.GUNES -> "Güneş (Kerâhat)"
                                                PrayerType.OGLE -> "Öğle"
                                                PrayerType.IKINDI -> "İkindi"
                                                PrayerType.AKSAM -> if (com.example.calc.PrayerCalculationEngine.isRamadan()) "Akşam (İftar)" else "Akşam"
                                                PrayerType.YATSI -> "Yatsı"
                                            },
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = palette.textPrimary
                                            )
                                        )
                                        Text(
                                            text = currentVoice,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = palette.secondary,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            if (isThisRowPlaying) {
                                                onStopSound()
                                            } else {
                                                onTestSound(currentVoice)
                                                Toast.makeText(context, "${prayer.titleTr} ezanı okunuyor...", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isThisRowPlaying) Icons.Default.Stop else Icons.Default.VolumeUp,
                                            contentDescription = "Test Et",
                                            tint = if (isThisRowPlaying) palette.primary else palette.secondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = "Seç",
                                        tint = palette.textMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Dialog for selecting Makam for a specific prayer
                    if (selectedPrayerForVoiceDialog != null) {
                        val activePrayer = selectedPrayerForVoiceDialog!!
                        val options = makamOptionsByPrayer[activePrayer] ?: voices
                        val currentChoice = prayerEzanVoices[activePrayer] ?: options.first()

                        AlertDialog(
                            onDismissRequest = { selectedPrayerForVoiceDialog = null },
                            containerColor = palette.surface,
                            title = {
                                Text(
                                    text = "${activePrayer.titleTr} Ezanı & Makamı",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = palette.textPrimary
                                    )
                                )
                            },
                            text = {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.verticalScroll(rememberScrollState())
                                ) {
                                    options.forEach { opt ->
                                        val isChosen = opt == currentChoice
                                        val isOptPlaying = isPlayingEzan && activePlayingVoiceName == opt

                                        Surface(
                                            onClick = {
                                                onPrayerEzanVoiceChange(activePrayer, opt)
                                                selectedPrayerForVoiceDialog = null
                                                Toast.makeText(context, "${activePrayer.titleTr} için '$opt' seçildi", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isChosen) palette.primary.copy(alpha = 0.15f) else palette.surfaceVariant,
                                            border = BorderStroke(
                                                1.dp,
                                                if (isChosen) palette.primary else Color.Transparent
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    IconButton(
                                                        onClick = {
                                                            if (isOptPlaying) onStopSound() else onTestSound(opt)
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isOptPlaying) Icons.Default.Stop else Icons.Default.VolumeUp,
                                                            contentDescription = "Dinle",
                                                            tint = if (isOptPlaying) palette.primary else palette.textMuted,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                    Text(
                                                        text = opt,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            color = if (isChosen) palette.primary else palette.textPrimary,
                                                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    )
                                                }
                                                if (isChosen) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = palette.primary, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { selectedPrayerForVoiceDialog = null }) {
                                    Text("Kapat", color = palette.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }
                }

                // Early Reminder Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Vakitten 15 Dakika Önce Hatırlat",
                            style = MaterialTheme.typography.bodyMedium.copy(color = palette.textPrimary, fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Namaz vakti girmeden hazırlık bildirimi gönderir",
                            style = MaterialTheme.typography.labelSmall.copy(color = palette.textSecondary, fontSize = 11.sp)
                        )
                    }
                    Switch(
                        checked = isEarlyReminderEnabled,
                        onCheckedChange = { onToggleEarlyReminder() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = palette.primary,
                            checkedTrackColor = palette.surfaceVariant
                        )
                    )
                }
            }
        }
        }

        // ==========================================
        // 3B. ARKA PLAN SERVİSİ & PİL GÜVENİLİRLİĞİ (WORKMANAGER & SCHEDULE_EXACT_ALARM)
        // ==========================================
        val bgPrefs = remember { AppPreferencesRepository(context) }
        var exactAlarmGranted by remember {
            mutableStateOf(AlarmPermissionHelper.canScheduleExactAlarms(context))
        }
        var batteryOptimizationIgnored by remember {
            mutableStateOf(AlarmPermissionHelper.isIgnoringBatteryOptimizations(context))
        }
        var lastSyncTimestamp by remember {
            mutableStateOf(bgPrefs.getLastBackgroundSyncTime())
        }

        if (currentFilter == SettingsFilter.ALL || currentFilter == SettingsFilter.SYSTEM) {
            SettingsSectionCard(
            title = "ARKA PLAN SERVİSİ VE PİL KORUMASI",
            icon = Icons.Default.Security,
            palette = palette
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Özellikle Xiaomi (MIUI/HyperOS), Huawei, Samsung ve Oppo gibi agresif pil tasarrufu uygulayan cihazlarda ezan sesinin gecikmemesi ve kesilmemesi için WorkManager arka plan güvencesi devrededir.",
                    style = MaterialTheme.typography.bodySmall.copy(color = palette.textSecondary, lineHeight = 18.sp)
                )

                // 1. SCHEDULE_EXACT_ALARM İzni
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (exactAlarmGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (exactAlarmGranted) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Tam Vaktinde Alarm İzni (SCHEDULE_EXACT_ALARM)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary
                                )
                            )
                        }

                        Text(
                            text = if (exactAlarmGranted) {
                                "İzin Etkin: Ezan alarmları saniyesi saniyesine vaktinde çalacak şekilde zamanlanmıştır."
                            } else {
                                "İzin Gerekli: Android 12 ve üzeri sistemlerde ezanın tam vaktinde çalabilmesi için Alarmlar ve Hatırlatıcılar izni verilmelidir."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (exactAlarmGranted) palette.textSecondary else Color(0xFFFF9800),
                                fontSize = 12.sp
                            )
                        )

                        if (!exactAlarmGranted) {
                            Button(
                                onClick = {
                                    AlarmPermissionHelper.openExactAlarmSettings(context)
                                    exactAlarmGranted = AlarmPermissionHelper.canScheduleExactAlarms(context)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(
                                    text = "İzni Etkinleştir",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.Black)
                                )
                            }
                        }
                    }
                }

                // 2. Pil Tasarrufu Muafiyeti
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (batteryOptimizationIgnored) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (batteryOptimizationIgnored) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Agresif Pil Tasarrufu Koruması",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary
                                )
                            )
                        }

                        Text(
                            text = if (batteryOptimizationIgnored) {
                                "Pil Koruma Muafiyeti Aktif: Cihaz derin uyku moduna geçse bile sistem ezan servisini sonlandıramaz."
                            } else {
                                "Pil Optimizasyonu Açık: Cihazınız ekran kapalıyken ezan alarmlarını uykuya alabilir. Kesintisiz ezan için pil kısıtlamasını kaldırınız."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (batteryOptimizationIgnored) palette.textSecondary else Color(0xFFFF9800),
                                fontSize = 12.sp
                            )
                        )

                        if (!batteryOptimizationIgnored) {
                            OutlinedButton(
                                onClick = {
                                    AlarmPermissionHelper.openBatteryOptimizationSettings(context)
                                    batteryOptimizationIgnored = AlarmPermissionHelper.isIgnoringBatteryOptimizations(context)
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(
                                    text = "Pil Kısıtlamasını Kaldır",
                                    style = MaterialTheme.typography.labelMedium.copy(color = palette.primary, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                // 3. WorkManager Periyodik Senkronizasyon Durumu
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = palette.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "WorkManager Periyodik Servis: Aktif",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary
                                )
                            )
                        }

                        Text(
                            text = "Son Arka Plan Eşitlemesi: ${PrayerWorkManagerHelper.formatLastSyncTime(lastSyncTimestamp)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = palette.textSecondary,
                                fontSize = 12.sp
                            )
                        )

                        Button(
                            onClick = {
                                PrayerWorkManagerHelper.enqueuePeriodicPrayerSync(context)
                                PrayerWorkManagerHelper.enqueueImmediateSync(context)
                                exactAlarmGranted = AlarmPermissionHelper.canScheduleExactAlarms(context)
                                batteryOptimizationIgnored = AlarmPermissionHelper.isIgnoringBatteryOptimizations(context)
                                lastSyncTimestamp = System.currentTimeMillis()
                                Toast.makeText(context, "WorkManager servisi ve ezan alarmları anında güncellendi.", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("refresh_workmanager_service_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Alarmları ve Arka Plan Servisini Şimdi Yenile",
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
        }

        // ==========================================
        // 4. HESAPLAMA METODU & İHTİYAT (TEMKİN)
        // ==========================================
        if (currentFilter == SettingsFilter.ALL || currentFilter == SettingsFilter.PRAYER) {
            SettingsSectionCard(
            title = "VAKİT HESAPLAMA VE TEMKİN",
            icon = Icons.Default.Tune,
            palette = palette
        ) {
            val methods = listOf(
                "Diyanet İşleri Başkanlığı (Türkiye Standart)",
                "Fazilet Takvimi (Klasik İlimler)",
                "Mısır Genel Heyeti (Egyptian General)"
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Hesaplama Algoritması:",
                    style = MaterialTheme.typography.labelMedium.copy(color = palette.textSecondary)
                )

                methods.forEach { method ->
                    val isSelected = method == calculationMethod
                    Surface(
                        onClick = { onCalculationMethodChange(method) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) palette.surfaceVariant else Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = method,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isSelected) palette.primary else palette.textPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = palette.primary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.08f))

                // ==========================================
                // TEMKİN / İHTİYAT PAYI AYARI (HER CİHAZA UYGUN & ERGONOMİK)
                // ==========================================
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(palette.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Temkin Ayarı",
                                    tint = palette.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Temkin / İhtiyat Payı",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = palette.textPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = if (precautionMinutes == 0) "Diyanet Takvimi ile Tam Uyumlu (0 dk)"
                                           else if (precautionMinutes > 0) "Vakitlere +$precautionMinutes dakika ihtiyat eklendi"
                                           else "Vakitlerden $precautionMinutes dakika erken hesaplanıyor",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (precautionMinutes == 0) palette.textSecondary else palette.accent,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        // Stepper Denetleyicisi (En az 44-48dp erişilebilirlik ve geniş basma alanı)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = palette.surfaceVariant.copy(alpha = 0.7f),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(
                                    listOf(palette.primary.copy(alpha = 0.3f), palette.accent.copy(alpha = 0.2f))
                                )
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                IconButton(
                                    onClick = { if (precautionMinutes > -10) onPrecautionMinutesChange(precautionMinutes - 1) },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .testTag("precaution_minus_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Temkin Dakikasını Azalt",
                                        tint = if (precautionMinutes > -10) palette.textPrimary else palette.textMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Text(
                                    text = if (precautionMinutes > 0) "+$precautionMinutes dk" else "$precautionMinutes dk",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (precautionMinutes != 0) palette.accent else palette.primary
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )

                                IconButton(
                                    onClick = { if (precautionMinutes < 10) onPrecautionMinutesChange(precautionMinutes + 1) },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .testTag("precaution_plus_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Temkin Dakikasını Artır",
                                        tint = if (precautionMinutes < 10) palette.textPrimary else palette.textMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Hızlı Seçim Butonları (Tek dokunuşla temkin ayarla)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(-2, -1, 0, 1, 2, 3, 5).forEach { minute ->
                            val isSelected = precautionMinutes == minute
                            Surface(
                                onClick = { onPrecautionMinutesChange(minute) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) palette.primary else palette.surfaceVariant.copy(alpha = 0.5f),
                                border = if (isSelected) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                ) {
                                    Text(
                                        text = when (minute) {
                                            0 -> "0 dk (Diyanet)"
                                            in 1..10 -> "+$minute dk"
                                            else -> "$minute dk"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.Black else palette.textSecondary
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

        // ==========================================
        // 5. ZİKİRMATİK TİTREŞİM AYARI
        // ==========================================
        if (currentFilter == SettingsFilter.ALL || currentFilter == SettingsFilter.SYSTEM) {
            SettingsSectionCard(
            title = "ZİKİRMATİK DOKUNSAL GERİ BİLDİRİM",
            icon = Icons.Default.Vibration,
            palette = palette
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dokunma & Tur Titreşimi",
                        style = MaterialTheme.typography.bodyMedium.copy(color = palette.textPrimary, fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Her tesbihat dokunuşunda ve 33/99 hedeflerinde titreşim ile bildir",
                        style = MaterialTheme.typography.labelSmall.copy(color = palette.textSecondary, fontSize = 11.sp)
                    )
                }
                Switch(
                    checked = isVibrationEnabled,
                    onCheckedChange = { onToggleVibration() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = palette.primary,
                        checkedTrackColor = palette.surfaceVariant
                    )
                )
            }
        }
        }

        // ==========================================
        // 6. ANA EKRAN WİDGET'I ÖZELLEŞTİRME
        // ==========================================
        if (currentFilter == SettingsFilter.ALL || currentFilter == SettingsFilter.APPEARANCE || currentFilter == SettingsFilter.SYSTEM) {
            SettingsSectionCard(
            title = "ANA EKRAN WİDGET'I ÖZELLEŞTİRME",
            icon = Icons.Default.Widgets,
            palette = palette
        ) {
            val effectiveTheme = WidgetThemeRenderer.resolveWidgetTheme(widgetThemeOption, selectedTheme)
            val effectiveMotif = WidgetThemeRenderer.resolveWidgetMotif(widgetMotifOption, effectiveTheme)
            val widgetColors = WidgetThemeRenderer.getWidgetColors(effectiveTheme)
            val effectiveBgColor = Color(widgetColors.bgEnd).copy(alpha = (widgetBackgroundOpacity / 100f).coerceIn(0.4f, 1f))
            val effectiveBgStart = Color(widgetColors.bgStart).copy(alpha = (widgetBackgroundOpacity / 100f).coerceIn(0.4f, 1f))
            val effectivePrimary = Color(widgetColors.primary)
            val effectiveAccent = Color(widgetColors.accent)
            val effectiveTextPrimary = Color(widgetColors.textPrimary)
            val effectiveTextSecondary = Color(widgetColors.textSecondary)

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Telefonunuzun ana ekranındaki widget'ın renk paletini, İslami geometrik motifini ve şeffaflık seviyesini özelleştirin.",
                    style = MaterialTheme.typography.bodySmall.copy(color = palette.textSecondary, lineHeight = 18.sp)
                )

                // -------------------------------------------------------------
                // CANLI WİDGET ÖNİZLEMESİ (Interactive Live Preview)
                // -------------------------------------------------------------
                var previewWidgetSize by remember { mutableStateOf(0) } // 0: Geniş (4x2), 1: Küçük (2x2)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CANLI WİDGET ÖNİZLEMESİ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.primary,
                                letterSpacing = 1.sp
                            )
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Text(
                                text = "Birebir Görünüm",
                                style = MaterialTheme.typography.labelSmall.copy(color = palette.textMuted, fontSize = 10.sp)
                            )
                        }
                    }

                    // Format Switcher: Geniş (4x2) vs Küçük (2x2)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(palette.surfaceVariant.copy(alpha = 0.5f))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            onClick = { previewWidgetSize = 0 },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(9.dp),
                            color = if (previewWidgetSize == 0) palette.primary else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 7.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Dashboard,
                                    contentDescription = null,
                                    tint = if (previewWidgetSize == 0) Color.Black else palette.textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Geniş (4x2)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (previewWidgetSize == 0) FontWeight.Bold else FontWeight.Normal,
                                        color = if (previewWidgetSize == 0) Color.Black else palette.textSecondary
                                    )
                                )
                            }
                        }

                        Surface(
                            onClick = { previewWidgetSize = 1 },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(9.dp),
                            color = if (previewWidgetSize == 1) palette.primary else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 7.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Widgets,
                                    contentDescription = null,
                                    tint = if (previewWidgetSize == 1) Color.Black else palette.textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Küçük (2x2)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (previewWidgetSize == 1) FontWeight.Bold else FontWeight.Normal,
                                        color = if (previewWidgetSize == 1) Color.Black else palette.textSecondary
                                    )
                                )
                            }
                        }
                    }

                    if (previewWidgetSize == 1) {
                        // Simulated Android Home Screen Compact / Small Widget Card (2x2)
                        Box(
                            modifier = Modifier
                                .widthIn(max = 240.dp)
                                .fillMaxWidth(0.72f)
                                .align(Alignment.CenterHorizontally)
                                .clip(RoundedCornerShape(22.dp))
                                .border(
                                    width = 1.5.dp,
                                    brush = Brush.linearGradient(
                                        listOf(
                                            effectivePrimary.copy(alpha = 0.55f),
                                            effectiveAccent.copy(alpha = 0.25f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(22.dp)
                                )
                                .background(
                                    Brush.verticalGradient(
                                        listOf(effectiveBgStart, effectiveBgColor)
                                    )
                                )
                        ) {
                            // Background Motif Watermark inside Small Preview
                            if (widgetMotifWatermarkEnabled) {
                                ThemeMotifWatermark(
                                    motif = effectiveMotif,
                                    primaryColor = effectivePrimary,
                                    accentColor = effectiveAccent,
                                    alpha = if (widgetColors.isDark) 0.16f else 0.10f,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .align(Alignment.CenterEnd)
                                        .offset(x = 24.dp)
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Top Row: Mosque Icon, City Name, Refresh Icon
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mosque,
                                        contentDescription = null,
                                        tint = effectivePrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = selectedCity.name,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = effectiveTextPrimary,
                                            fontSize = 12.sp
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Yenile",
                                        tint = effectiveTextSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }

                                // Center: Next Prayer & Countdown Box
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(effectiveAccent.copy(alpha = 0.14f))
                                        .border(1.dp, effectiveAccent.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                                        .padding(vertical = 10.dp, horizontal = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = "SIRADAKİ VAKİT",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = effectiveAccent
                                            )
                                        )
                                        Text(
                                            text = "${nextPrayerTimeItem?.type?.titleTr?.uppercase() ?: "İKİNDİ"} ${nextPrayerTimeItem?.timeFormatted ?: "16:48"}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = effectiveTextPrimary,
                                                fontSize = 13.5.sp
                                            )
                                        )
                                        Text(
                                            text = countdownText.ifEmpty { "01 sa 45 dk kaldı" },
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = effectiveAccent,
                                                fontSize = 9.5.sp
                                            )
                                        )
                                    }
                                }

                                // Bottom Row: Current prayer & Motif badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Şu an: ${currentPrayerTimeItem?.type?.titleTr ?: "Öğle"} (${currentPrayerTimeItem?.timeFormatted ?: "13:12"})",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = effectiveTextSecondary,
                                            fontSize = 9.sp
                                        ),
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = effectivePrimary.copy(alpha = 0.14f),
                                        border = androidx.compose.foundation.BorderStroke(0.8.dp, effectivePrimary.copy(alpha = 0.35f))
                                    ) {
                                        Text(
                                            text = "✦ ${effectiveTheme.motifTitle.take(8)}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = effectivePrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 8.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Simulated Android Home Screen Wide Widget Card (4x2)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .border(
                                    width = 1.5.dp,
                                    brush = Brush.linearGradient(
                                        listOf(
                                            effectivePrimary.copy(alpha = 0.55f),
                                            effectiveAccent.copy(alpha = 0.25f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(22.dp)
                                )
                                .background(
                                    Brush.verticalGradient(
                                        listOf(effectiveBgStart, effectiveBgColor)
                                    )
                                )
                        ) {
                        // Background Motif Watermark inside Preview
                        if (widgetMotifWatermarkEnabled) {
                            ThemeMotifWatermark(
                                motif = effectiveMotif,
                                primaryColor = effectivePrimary,
                                accentColor = effectiveAccent,
                                alpha = if (widgetColors.isDark) 0.16f else 0.10f,
                                modifier = Modifier
                                    .size(170.dp)
                                    .align(Alignment.CenterEnd)
                                    .offset(x = 24.dp)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Top Row: Mosque Icon, City Name, Hijri Date, Motif Tag, Refresh Icon
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mosque,
                                    contentDescription = null,
                                    tint = effectivePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = selectedCity.name,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = effectiveTextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "12 Eylül • 1 Rebiülahir",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = effectiveTextSecondary,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                // Motif Badge Pill
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = effectivePrimary.copy(alpha = 0.14f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, effectivePrimary.copy(alpha = 0.35f))
                                ) {
                                    Text(
                                        text = "✦ ${effectiveTheme.motifTitle.take(12)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = effectivePrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Yenile",
                                    tint = effectiveTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Middle Cards: Current Prayer & Next Prayer Countdown
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Left Card: Current Prayer
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(effectivePrimary.copy(alpha = 0.14f))
                                        .border(1.dp, effectivePrimary.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "ŞU ANKİ VAKİT",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = effectivePrimary
                                            )
                                        )
                                        Text(
                                            text = currentPrayerTimeItem?.type?.titleTr?.uppercase() ?: "ÖĞLE",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = effectiveTextPrimary
                                            )
                                        )
                                        Text(
                                            text = currentPrayerTimeItem?.timeFormatted ?: "13:12",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = effectiveTextSecondary
                                            )
                                        )
                                    }
                                }

                                // Right Card: Next Prayer & Countdown
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(effectiveAccent.copy(alpha = 0.14f))
                                        .border(1.dp, effectiveAccent.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "SIRADAKİ VAKİT",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = effectiveAccent
                                            )
                                        )
                                        Text(
                                            text = "${nextPrayerTimeItem?.type?.titleTr?.uppercase() ?: "İKİNDİ"} (${nextPrayerTimeItem?.timeFormatted ?: "16:48"})",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = effectiveTextPrimary,
                                                fontSize = 12.sp
                                            ),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = countdownText.ifEmpty { "01 sa 45 dk kaldı" },
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = effectiveAccent
                                            )
                                        )
                                    }
                                }
                            }

                            // Bottom Mini Prayer Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(widgetColors.surface).copy(alpha = 0.65f))
                                    .border(1.dp, effectivePrimary.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                    .padding(vertical = 4.dp, horizontal = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    listOf(
                                        "İmsak" to "05:12",
                                        "Güneş" to "06:40",
                                        "Öğle" to "13:12",
                                        "İkindi" to "16:48",
                                        "Akşam" to "19:35",
                                        "Yatsı" to "20:56"
                                    ).forEachIndexed { idx, pair ->
                                        val isCurrent = idx == 2
                                        val isNext = idx == 3
                                        val itemColor = when {
                                            isCurrent -> effectivePrimary
                                            isNext -> effectiveAccent
                                            else -> effectiveTextSecondary.copy(alpha = 0.7f)
                                        }
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(horizontal = 2.dp)
                                        ) {
                                            Text(
                                                text = pair.first,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 8.sp,
                                                    fontWeight = if (isCurrent || isNext) FontWeight.Bold else FontWeight.Normal,
                                                    color = itemColor
                                                )
                                            )
                                            Text(
                                                text = pair.second,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.sp,
                                                    fontWeight = if (isCurrent || isNext) FontWeight.Bold else FontWeight.Normal,
                                                    color = itemColor
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

                HorizontalDivider(color = palette.surfaceVariant.copy(alpha = 0.6f), thickness = 1.dp)

                // -------------------------------------------------------------
                // 1. WİDGET RENK TEMASI SEÇİMİ
                // -------------------------------------------------------------
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Outlined.Palette, contentDescription = null, tint = palette.primary, modifier = Modifier.size(16.dp))
                        Text(
                            text = "1. Widget Renk Teması",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        )
                    }

                    // Follow App Theme Option
                    val isFollowApp = widgetThemeOption == "FOLLOW_APP"
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isFollowApp) palette.primary.copy(alpha = 0.16f) else palette.surfaceVariant.copy(alpha = 0.45f),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isFollowApp) 1.5.dp else 1.dp,
                            color = if (isFollowApp) palette.primary else palette.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onWidgetThemeOptionChange("FOLLOW_APP") }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = if (isFollowApp) palette.primary else palette.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Uygulama Temasıyla Otomatik Senkron",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFollowApp) palette.primary else palette.textPrimary
                                    )
                                )
                                Text(
                                    text = "Uygulamada seçili olan temayı (${selectedTheme.title}) ana ekrana yansıtır",
                                    style = MaterialTheme.typography.labelSmall.copy(color = palette.textSecondary)
                                )
                            }
                            if (isFollowApp) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Seçili",
                                    tint = palette.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Horizontal Scrollable Row of the 10 Islamic Color Themes
                    Text(
                        text = "Veya bağımsız bir tema seçin:",
                        style = MaterialTheme.typography.labelSmall.copy(color = palette.textSecondary)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppTheme.entries.forEach { theme ->
                            val isSelected = widgetThemeOption == theme.name
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) theme.previewPrimary.copy(alpha = 0.18f) else palette.surfaceVariant.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) theme.previewPrimary else palette.surfaceVariant
                                ),
                                modifier = Modifier
                                    .width(135.dp)
                                    .clickable { onWidgetThemeOptionChange(theme.name) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Color swatch preview row
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(theme.previewBg)
                                                .border(1.dp, theme.previewPrimary.copy(alpha = 0.5f), CircleShape)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(theme.previewPrimary)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(theme.previewAccent)
                                        )
                                    }

                                    Text(
                                        text = theme.title,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) theme.previewPrimary else palette.textPrimary
                                        ),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = theme.subtitle,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = palette.textSecondary,
                                            fontSize = 9.sp
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = palette.surfaceVariant.copy(alpha = 0.6f), thickness = 1.dp)

                // -------------------------------------------------------------
                // 2. İSLAMİ GEOMETRİK MOTİF SEÇİMİ
                // -------------------------------------------------------------
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = palette.primary, modifier = Modifier.size(16.dp))
                        Text(
                            text = "2. İslami Geometrik Motif",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        )
                    }

                    // Follow Theme Motif Option
                    val isFollowMotif = widgetMotifOption == "FOLLOW_THEME"
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isFollowMotif) palette.primary.copy(alpha = 0.16f) else palette.surfaceVariant.copy(alpha = 0.45f),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isFollowMotif) 1.5.dp else 1.dp,
                            color = if (isFollowMotif) palette.primary else palette.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onWidgetMotifOptionChange("FOLLOW_THEME") }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterVintage,
                                contentDescription = null,
                                tint = if (isFollowMotif) palette.primary else palette.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Temanın Kendi Motifini Kullan",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFollowMotif) palette.primary else palette.textPrimary
                                    )
                                )
                                Text(
                                    text = "Seçili temaya ait otantik motifi (${effectiveTheme.motifTitle}) uygular",
                                    style = MaterialTheme.typography.labelSmall.copy(color = palette.textSecondary)
                                )
                            }
                            if (isFollowMotif) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Seçili",
                                    tint = palette.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // 10 Motif Options Horizontal Row
                    Text(
                        text = "Veya dilediğiniz motifi belirleyin:",
                        style = MaterialTheme.typography.labelSmall.copy(color = palette.textSecondary)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            ThemeMotif.SELJUK_STAR to ("Selçuklu 8'li Yıldız" to "Sekizgen yıldız geometrisi"),
                            ThemeMotif.OTTOMAN_TULIP to ("Osmanlı Lâlesi" to "Tevhid simgesi saray lâlesi"),
                            ThemeMotif.KISVE_GOLD to ("Kisve-i Şerîf" to "Kâbe örtüsü altın bordürü"),
                            ThemeMotif.RAVZA_DOME to ("Kubbe-i Hadrâ" to "Nebevî yeşil kubbe & selvi"),
                            ThemeMotif.ALHAMBRA_ARCH to ("Elhamra Kemeri" to "Endülüs mukarnas kemeri"),
                            ThemeMotif.ARABESQUE_SUN to ("Güneş Şemse" to "12 ışınlı geometrik rozet"),
                            ThemeMotif.MADINAH_ROSE to ("Medine Gülü" to "8 taç yapraklı Peygamber gülü"),
                            ThemeMotif.QUDS_OCTAGON to ("Kudüs Sekizgeni" to "Kubbet-üs Sahrâ mimarisi"),
                            ThemeMotif.TEZHIP_MARBLE to ("Mermer Tezhip" to "Zarif mermer kakma altın"),
                            ThemeMotif.TOPKAPI_RUMI to ("Topkapı Çintemani" to "Üç benek çintemani & rûmî")
                        ).forEach { (motif, meta) ->
                            val isSelected = widgetMotifOption == motif.name
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) palette.primary.copy(alpha = 0.18f) else palette.surfaceVariant.copy(alpha = 0.45f),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) palette.primary else palette.surfaceVariant
                                ),
                                modifier = Modifier
                                    .width(140.dp)
                                    .clickable { onWidgetMotifOptionChange(motif.name) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    ThemeMotifBadge(
                                        motif = motif,
                                        primaryColor = palette.primary,
                                        accentColor = palette.accent,
                                        size = 36.dp
                                    )
                                    Text(
                                        text = meta.first,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) palette.primary else palette.textPrimary,
                                            textAlign = TextAlign.Center
                                        ),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = meta.second,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = palette.textSecondary,
                                            fontSize = 9.sp,
                                            textAlign = TextAlign.Center
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // Watermark Motif Switch Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(palette.surfaceVariant.copy(alpha = 0.4f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Arka Planda Motif Filigranı",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = palette.textPrimary)
                            )
                            Text(
                                text = "Widget arka planında hafif parlak İslami motif silüeti gösterir",
                                style = MaterialTheme.typography.labelSmall.copy(color = palette.textSecondary)
                            )
                        }
                        Switch(
                            checked = widgetMotifWatermarkEnabled,
                            onCheckedChange = { onToggleWidgetMotifWatermark() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = palette.primary,
                                checkedTrackColor = palette.surfaceVariant
                            )
                        )
                    }
                }

                HorizontalDivider(color = palette.surfaceVariant.copy(alpha = 0.6f), thickness = 1.dp)

                // -------------------------------------------------------------
                // 3. ARKA PLAN SAYDAMLIK / ŞEFFAFLIK AYARI
                // -------------------------------------------------------------
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Opacity, contentDescription = null, tint = palette.primary, modifier = Modifier.size(16.dp))
                        Text(
                            text = "3. Arka Plan Saydamlığı (Buzlu Cam)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            100 to "%100 Opak",
                            92 to "%90 Mat Buzlu",
                            75 to "%75 Yarı Saydam",
                            60 to "%60 Cam Efekti"
                        ).forEach { (opacityVal, label) ->
                            val isSelected = widgetBackgroundOpacity == opacityVal
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) palette.primary.copy(alpha = 0.2f) else palette.surfaceVariant.copy(alpha = 0.4f),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) palette.primary else palette.surfaceVariant
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onWidgetBackgroundOpacityChange(opacityVal) }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) palette.primary else palette.textSecondary,
                                            fontSize = 10.sp,
                                            textAlign = TextAlign.Center
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = palette.surfaceVariant.copy(alpha = 0.6f), thickness = 1.dp)

                // -------------------------------------------------------------
                // 4. BUTONLAR: WİDGET EKLE & ANINDA SENKRONİZE ET
                // -------------------------------------------------------------
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Ana Ekrana Widget Ekle:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = palette.textSecondary)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Pin Large Widget Button (4x2)
                        FilledTonalButton(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
                                    val myProvider = ComponentName(context, PrayerWidgetProvider::class.java)
                                    if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported) {
                                        appWidgetManager.requestPinAppWidget(myProvider, null, null)
                                        Toast.makeText(context, "Geniş widget (4x2) ekleme penceresi açılıyor...", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Ana ekrana basılı tutup 'Widget'lar' bölümünden 'Ezanla Widget'ı seçebilirsiniz.", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Ana ekrana basılı tutup 'Widget'lar' bölümünden 'Ezanla Widget'ı seçebilirsiniz.", Toast.LENGTH_LONG).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = palette.primary.copy(alpha = 0.22f),
                                contentColor = palette.primary
                            ),
                            modifier = Modifier.weight(1f).testTag("button_pin_large_widget")
                        ) {
                            Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Geniş (4x2)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Pin Small Widget Button (2x2)
                        FilledTonalButton(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
                                    val smallProvider = ComponentName(context, SmallPrayerWidgetProvider::class.java)
                                    if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported) {
                                        appWidgetManager.requestPinAppWidget(smallProvider, null, null)
                                        Toast.makeText(context, "Küçük widget (2x2) ekleme penceresi açılıyor...", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Ana ekrana basılı tutup 'Widget'lar' bölümünden 'Ezanla Mini'yi seçebilirsiniz.", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Ana ekrana basılı tutup 'Widget'lar' bölümünden 'Ezanla Mini'yi seçebilirsiniz.", Toast.LENGTH_LONG).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = palette.accent.copy(alpha = 0.22f),
                                contentColor = palette.accent
                            ),
                            modifier = Modifier.weight(1f).testTag("button_pin_small_widget")
                        ) {
                            Icon(Icons.Default.Widgets, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Küçük (2x2)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Update & Sync Active Widgets Button
                    OutlinedButton(
                        onClick = {
                            PrayerWidgetProvider.updateAllWidgets(context)
                            Toast.makeText(context, "Tüm widget'lar yeni tema ve motifinizle senkronize edildi!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, palette.primary.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.primary),
                        modifier = Modifier.fillMaxWidth().testTag("button_sync_widget")
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tüm Widget'ları Şimdi Güncelle & Senkronize Et", fontWeight = FontWeight.SemiBold)
                    }
                }

                Text(
                    text = "💡 İpucu: Geniş (4x2) veya Küçük (2x2) widget'ları ana ekranınıza ekleyebilir, vakitleri ve geri sayımı dilediğiniz boyutta takip edebilirsiniz.",
                    style = MaterialTheme.typography.labelSmall.copy(color = palette.textSecondary.copy(alpha = 0.8f), fontSize = 11.sp, lineHeight = 16.sp)
                )
            }
        }
        }

        // ==========================================
        // 7. KIBLE PUSULASI VE KADRAN SEÇENEKLERİ
        // ==========================================
        if (currentFilter == SettingsFilter.ALL || currentFilter == SettingsFilter.LOCATION) {
            SettingsSectionCard(
            title = "KIBLE PUSULASI VE KADRAN SEÇENEKLERİ",
            icon = Icons.Default.Explore,
            palette = palette
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Pusula Kadran Tarzı:",
                    style = MaterialTheme.typography.labelMedium.copy(color = palette.textSecondary)
                )

                // 4 Kadran Stili
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompassDialStyle.entries.forEach { style ->
                        val isSelected = style == compassDialStyle
                        Surface(
                            onClick = { onCompassDialStyleChange(style) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) palette.primary.copy(alpha = 0.18f) else palette.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) palette.primary else palette.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = style.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) palette.primary else palette.textPrimary
                                    )
                                )
                                Text(
                                    text = style.subtitle,
                                    style = MaterialTheme.typography.labelSmall.copy(color = palette.textSecondary, fontSize = 11.sp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = palette.surfaceVariant.copy(alpha = 0.6f))

                // Titreşim
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kıbleye Kilitlenme Titreşimi",
                            style = MaterialTheme.typography.bodyMedium.copy(color = palette.textPrimary, fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Kâbe yönü hizalandığında hafif haptik titreşim verir",
                            style = MaterialTheme.typography.labelSmall.copy(color = palette.textSecondary)
                        )
                    }
                    Switch(
                        checked = isCompassHapticEnabled,
                        onCheckedChange = { onToggleCompassHaptic() },
                        colors = SwitchDefaults.colors(checkedThumbColor = palette.primary, checkedTrackColor = palette.primary.copy(alpha = 0.4f))
                    )
                }

                // Sesli Uyarı
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sesli Kilitlenme Tonu (Bip)",
                            style = MaterialTheme.typography.bodyMedium.copy(color = palette.textPrimary, fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "İstikamet bulunduğunda hafif sinyal sesi çalar",
                            style = MaterialTheme.typography.labelSmall.copy(color = palette.textSecondary)
                        )
                    }
                    Switch(
                        checked = isCompassSoundEnabled,
                        onCheckedChange = { onToggleCompassSound() },
                        colors = SwitchDefaults.colors(checkedThumbColor = palette.primary, checkedTrackColor = palette.primary.copy(alpha = 0.4f))
                    )
                }

                // Su Terazisi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Su Terazisi (Düzlük Sensörü)",
                            style = MaterialTheme.typography.bodyMedium.copy(color = palette.textPrimary, fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Telefonun yere paralel tutulmasını denetler",
                            style = MaterialTheme.typography.labelSmall.copy(color = palette.textSecondary)
                        )
                    }
                    Switch(
                        checked = isCompassLevelEnabled,
                        onCheckedChange = { onToggleCompassLevel() },
                        colors = SwitchDefaults.colors(checkedThumbColor = palette.primary, checkedTrackColor = palette.primary.copy(alpha = 0.4f))
                    )
                }

                // Coğrafi Kuzey
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Coğrafi Gerçek Kuzey Düzeltmesi",
                            style = MaterialTheme.typography.bodyMedium.copy(color = palette.textPrimary, fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Manyetik kutup farkını coğrafi koordinatlara göre dengeler",
                            style = MaterialTheme.typography.labelSmall.copy(color = palette.textSecondary)
                        )
                    }
                    Switch(
                        checked = isCompassTrueNorthEnabled,
                        onCheckedChange = { onToggleCompassTrueNorth() },
                        colors = SwitchDefaults.colors(checkedThumbColor = palette.primary, checkedTrackColor = palette.primary.copy(alpha = 0.4f))
                    )
                }
            }
        }
        }

        // ==========================================
        // 8. ÇEVRİMDIŞI KUR'AN & SES ÖNBELLEĞİ
        // ==========================================
        var cacheRefreshKey by remember { mutableStateOf(0) }
        var isPreloadingAudio by remember { mutableStateOf(false) }
        val clipboardManager = LocalClipboardManager.current

        val audioCacheDir = remember(cacheRefreshKey) {
            File(context.cacheDir, "quran_audio").apply { if (!exists()) mkdirs() }
        }
        val cachedFiles = remember(cacheRefreshKey) {
            audioCacheDir.listFiles { f -> f.isFile && f.extension == "mp3" && f.length() > 5000 } ?: emptyArray()
        }
        val cachedCount = cachedFiles.size
        val cachedBytes = cachedFiles.sumOf { it.length() }
        val cachedSizeMb = remember(cachedBytes) {
            String.format(java.util.Locale.US, "%.1f MB", cachedBytes / (1024f * 1024f))
        }

        if (currentFilter == SettingsFilter.ALL || currentFilter == SettingsFilter.SYSTEM) {
            SettingsSectionCard(
            title = "ÇEVRİMDIŞI KUR'AN & HAFIZA YÖNETİMİ",
            icon = Icons.Outlined.CloudDownload,
            palette = palette
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Dinlenen sureler internetsiz erişim için cihaz hafızasında hafif formatta önbelleğe alınır. En sık okunan kısa sureleri tek tıkla çevrimdışı kullanım için indirebilirsiniz.",
                    style = MaterialTheme.typography.bodySmall.copy(color = palette.textSecondary, lineHeight = 18.sp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Önbellekteki Sureler",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                        )
                        Text(
                            text = "$cachedCount Sure indirildi ($cachedSizeMb)",
                            style = MaterialTheme.typography.labelSmall.copy(color = palette.primary, fontWeight = FontWeight.Bold)
                        )
                    }
                    if (cachedCount > 0) {
                        IconButton(
                            onClick = {
                                audioCacheDir.listFiles()?.forEach { it.delete() }
                                cacheRefreshKey++
                                Toast.makeText(context, "Kur'an ses önbelleği temizlendi", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Önbelleği Temizle", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                FilledTonalButton(
                    onClick = {
                        if (!isPreloadingAudio) {
                            isPreloadingAudio = true
                            val qPlayer = QuranAudioPlayer(context)
                            qPlayer.preloadPopularSurahs {
                                isPreloadingAudio = false
                                cacheRefreshKey++
                                Toast.makeText(context, "Fâtiha, Yâsîn, Mülk, Nebe, İhlâs, Felak ve Nâs sureleri çevrimdışı dinleme için kaydedildi!", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isPreloadingAudio,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = palette.primary.copy(alpha = 0.15f),
                        contentColor = palette.primary
                    )
                ) {
                    if (isPreloadingAudio) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = palette.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Popüler Sureler İndiriliyor...", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    } else {
                        Icon(Icons.Outlined.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Popüler Sureleri İndir (Fâtiha, Yâsîn, Mülk, Nebe...)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
        }

        // ==========================================
        // 9. GİZLİLİK POLİTİKASI (PRIVACY POLICY)
        // ==========================================
        var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
        val privacyPolicyUrl = "https://aistudio.google.com/ezanla-privacy-policy"

        if (currentFilter == SettingsFilter.ALL || currentFilter == SettingsFilter.SYSTEM) {
            SettingsSectionCard(
                title = "GİZLİLİK VE YASAL BİLGİLENDİRME",
                icon = Icons.Outlined.Security,
                palette = palette
            ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Google Play Store standartlarına uygun olarak kişisel verileriniz hiçbir sunucuya aktarılmaz. İzinler yalnızca cihaz üzerinde yerel hizmetler için kullanılır.",
                    style = MaterialTheme.typography.bodySmall.copy(color = palette.textSecondary, lineHeight = 18.sp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showPrivacyPolicyDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Outlined.Lock, contentDescription = null, tint = palette.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gizlilik Politikası", style = MaterialTheme.typography.labelMedium.copy(color = palette.primary, fontWeight = FontWeight.Bold))
                    }

                    FilledTonalButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(privacyPolicyUrl))
                            Toast.makeText(context, "Gizlilik politikası bağlantısı kopyalandı", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("URL Kopyala", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
        }

        if (showPrivacyPolicyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyPolicyDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.Security, contentDescription = null, tint = palette.primary)
                        Text(
                            text = "Gizlilik Politikası",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = palette.primary)
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Ezanla Veri Güvenliği Taahhüdü",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        )
                        Text(
                            text = "1. Konum Verileri (ACCESS_FINE_LOCATION):\nNamaz vakitleri ve Kıble pusulası hesaplaması için anlık koordinat kullanılır. Konum verisi asla harici sunuculara iletilmez, satılmaz veya reklam amaçlı işlenmez.\n\n" +
                                    "2. Bildirimler ve Alarmlar (SCHEDULE_EXACT_ALARM, POST_NOTIFICATIONS):\nSadece ezan ve vakit hatırlatıcı alarmların tam vaktinde çalabilmesi için kullanılır.\n\n" +
                                    "3. İnternet ve Ağ Erişimi:\nDiyanet takvimi güncellemeleri ve Kur'an tilavet seslerinin aktarımı için kullanılır.\n\n" +
                                    "4. Yerel Hafıza (Önbellekleme):\nDinlenen Kur'an sureleri internetsiz çevrimdışı dinleme rahatlığı sunmak üzere cihazın geçici önbelleğinde tutulur.\n\n" +
                                    "5. İletişim:\nSorularınız ve geri bildirimleriniz için: developmentaydn@gmail.com",
                            style = MaterialTheme.typography.bodySmall.copy(color = palette.textSecondary, lineHeight = 20.sp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPrivacyPolicyDialog = false }) {
                        Text("Kapat", color = palette.primary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(privacyPolicyUrl))
                            Toast.makeText(context, "Bağlantı kopyalandı: $privacyPolicyUrl", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Bağlantıyı Kopyala")
                    }
                }
            )
        }

        // ==========================================
        // 10. UYGULAMA BİLGİSİ
        // ==========================================
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = palette.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = palette.primary, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Ezanla v2.0",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = palette.primary)
                    )
                }
                Text(
                    text = "• Diyanet İşleri Başkanlığı takvimi ile tam uyumlu algoritma\n• 81 İl ve dünya merkezleri GPS ile desteklenir\n• Google Play Store uyumlu gizlilik & offline önbellek desteği",
                    style = MaterialTheme.typography.bodySmall.copy(color = palette.textSecondary, lineHeight = 18.sp)
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    palette: com.example.ui.theme.AppPalette,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(palette.primary.copy(alpha = 0.3f), Color.Transparent)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = palette.primary, modifier = Modifier.size(18.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.primary,
                        letterSpacing = 0.8.sp
                    )
                )
            }

            content()
        }
    }
}
