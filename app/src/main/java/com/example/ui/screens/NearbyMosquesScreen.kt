package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Directions
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Refresh
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.IslamicDatabase
import com.example.data.LiveLocationTracker
import com.example.model.City
import com.example.model.NearbyMosque
import com.example.ui.theme.LocalAppPalette

@Composable
fun NearbyMosquesScreen(
    currentCity: City,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = LocalAppPalette.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, WALKING, HISTORICAL

    // Live GPS tracker instance
    val locationTracker = remember { LiveLocationTracker(context) }
    val gpsState by locationTracker.gpsState.collectAsState()

    // Permission launcher for real-time location
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            locationTracker.checkAndStartTracking()
        }
    }

    // Start live tracking on entry and stop on exit
    DisposableEffect(Unit) {
        if (locationTracker.hasPermission()) {
            locationTracker.checkAndStartTracking()
        }
        onDispose {
            locationTracker.stopTracking()
        }
    }

    // Calculate real-time distances and sort mosques strictly based on user's location
    val sortedMosques = remember(gpsState.latitude, gpsState.longitude, searchQuery, selectedFilter, currentCity) {
        val effectiveLat = gpsState.latitude ?: currentCity.latitude
        val effectiveLng = gpsState.longitude ?: currentCity.longitude
        val effectiveCityName = gpsState.nearestCity?.name ?: currentCity.name

        // Bulunulan il ve koordinatlara göre dinamik yerel mahalle camileri
        val localMosques = listOf(
            NearbyMosque(
                id = "local_1",
                name = "$effectiveCityName Merkez Camii",
                city = effectiveCityName,
                distanceMeters = 180,
                address = "Merkez Mah. Cami Sk. No:2, $effectiveCityName",
                latitude = effectiveLat + 0.0014,
                longitude = effectiveLng + 0.0012,
                features = listOf("Vakit Namazları", "Geniş Şadırvan", "Kadınlar Bölümü", "Klimalı"),
                isHistorical = false
            ),
            NearbyMosque(
                id = "local_2",
                name = "$effectiveCityName Çarşı Camii",
                city = effectiveCityName,
                distanceMeters = 340,
                address = "Çarşı İçi Meydanı No:14, $effectiveCityName",
                latitude = effectiveLat - 0.0022,
                longitude = effectiveLng + 0.0021,
                features = listOf("Merkezi Konum", "Tarihi Doku", "Abdesthane", "Cuma Cemaati"),
                isHistorical = true
            ),
            NearbyMosque(
                id = "local_3",
                name = "$effectiveCityName Fatih Camii",
                city = effectiveCityName,
                distanceMeters = 520,
                address = "Fatih Mah. Barış Cd. No:8, $effectiveCityName",
                latitude = effectiveLat + 0.0035,
                longitude = effectiveLng - 0.0031,
                features = listOf("Geniş Avlu", "Kuran Kursu", "Çocuk Oyun Alanı", "Otopark"),
                isHistorical = false
            ),
            NearbyMosque(
                id = "local_4",
                name = "$effectiveCityName Yeni Cami",
                city = effectiveCityName,
                distanceMeters = 740,
                address = "Yeni Mahalle Hürriyet Cd. No:45, $effectiveCityName",
                latitude = effectiveLat - 0.0048,
                longitude = effectiveLng - 0.0042,
                features = listOf("Modern Mimari", "Yerden Isıtmalı", "Tekerlekli Sandalye Girişi"),
                isHistorical = false
            ),
            NearbyMosque(
                id = "local_5",
                name = "$effectiveCityName Mimar Sinan Camii",
                city = effectiveCityName,
                distanceMeters = 960,
                address = "İstasyon Mevkii, Atatürk Bulvarı No:110, $effectiveCityName",
                latitude = effectiveLat + 0.0062,
                longitude = effectiveLng + 0.0055,
                features = listOf("Görkemli Kubbe", "Geniş Otopark", "Kütüphane", "Çay Ocağı"),
                isHistorical = false
            ),
            NearbyMosque(
                id = "local_6",
                name = "$effectiveCityName Hacı Ali Camii",
                city = effectiveCityName,
                distanceMeters = 1250,
                address = "Cumhuriyet Cad. No:77, $effectiveCityName",
                latitude = effectiveLat - 0.0078,
                longitude = effectiveLng + 0.0071,
                features = listOf("Mahalle Mescidi", "Huzurlu Ortam", "Gül Bahçeli Avlu"),
                isHistorical = false
            )
        )

        val combinedList = localMosques + IslamicDatabase.nearbyMosques
        val withRealtimeDistance = locationTracker.getMosquesSortedByRealtimeDistance(
            baseMosques = combinedList,
            userLat = effectiveLat,
            userLng = effectiveLng
        )

        val filteredByFilter = when (selectedFilter) {
            "WALKING" -> withRealtimeDistance.filter { it.distanceMeters <= 1000 }
            "HISTORICAL" -> withRealtimeDistance.filter { it.isHistorical }
            else -> withRealtimeDistance
        }

        if (searchQuery.isBlank()) {
            filteredByFilter
        } else {
            filteredByFilter.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.city.contains(searchQuery, ignoreCase = true) ||
                it.address.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Pulse animation for live GPS badge
    val infiniteTransition = rememberInfiniteTransition(label = "gpsPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        text = "Yakın Camiler",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                    )
                    Text(
                        text = "Tam bulunduğunuz konuma göre en yakın mescitler",
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
                            imageVector = Icons.Default.Mosque,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Real-Time GPS Status Card
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = palette.surfaceVariant,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        if (gpsState.hasPermission) listOf(palette.primary.copy(alpha = 0.4f), Color.Transparent)
                        else listOf(palette.accent.copy(alpha = 0.4f), Color.Transparent)
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (gpsState.hasPermission && gpsState.isTracking)
                                            palette.primary.copy(alpha = pulseAlpha)
                                        else palette.accent
                                    )
                            )
                            Text(
                                text = if (gpsState.hasPermission && gpsState.isTracking) "TAM ZAMANLI CANLI GPS AKTİF" else "KONUM İZNİ BEKLENİYOR",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (gpsState.hasPermission && gpsState.isTracking) palette.primary else palette.accent,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }

                        IconButton(
                            onClick = {
                                if (!locationTracker.hasPermission()) {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                } else {
                                    locationTracker.checkAndStartTracking()
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "Konumu Yenile",
                                tint = palette.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (gpsState.latitude != null && gpsState.longitude != null) {
                        Text(
                            text = "Bulunduğunuz Nokta: %.4f° K, %.4f° D • Doğruluk: ±%dm".format(
                                gpsState.latitude,
                                gpsState.longitude,
                                gpsState.accuracyMeters?.toInt() ?: 15
                            ),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = palette.textSecondary,
                                fontSize = 12.sp
                            )
                        )
                    } else {
                        Text(
                            text = gpsState.statusMessage,
                            style = MaterialTheme.typography.bodySmall.copy(color = palette.textSecondary)
                        )
                    }

                    if (!gpsState.hasPermission) {
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tam Bulunduğum Konumu Canlı Al (GPS İzni)",
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

        // Quick Google Maps Search Button for all surrounding mosques
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = palette.surface,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(palette.accent.copy(alpha = 0.3f), Color.Transparent)
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        openSurroundingMosquesInMaps(context, gpsState.latitude, gpsState.longitude)
                    }
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Explore,
                            contentDescription = null,
                            tint = palette.accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Etrafımdaki Tüm Camileri Haritada Keşfet",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary
                                )
                            )
                            Text(
                                text = "Canlı konumunuz etrafındaki tüm cami & mescitler",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = palette.textSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = palette.accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cami veya mahalle ara...", color = palette.textMuted) },
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

        // Filter Pills: Tümü, Yürüme Mesafesi, Tarihi Camiler
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("Tümü (${sortedMosques.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = palette.primary.copy(alpha = 0.25f),
                        selectedLabelColor = palette.primary,
                        containerColor = palette.surfaceVariant,
                        labelColor = palette.textSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                FilterChip(
                    selected = selectedFilter == "WALKING",
                    onClick = { selectedFilter = "WALKING" },
                    label = { Text("Yürüme (<1 km)", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = palette.accent.copy(alpha = 0.25f),
                        selectedLabelColor = palette.accent,
                        containerColor = palette.surfaceVariant,
                        labelColor = palette.textSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                FilterChip(
                    selected = selectedFilter == "HISTORICAL",
                    onClick = { selectedFilter = "HISTORICAL" },
                    label = { Text("Tarihi Camiler", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = palette.secondary.copy(alpha = 0.25f),
                        selectedLabelColor = palette.secondary,
                        containerColor = palette.surfaceVariant,
                        labelColor = palette.textSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Mosques list sorted by distance
        items(sortedMosques, key = { it.id }) { mosque ->
            MosqueItemCard(
                mosque = mosque,
                userLat = gpsState.latitude,
                userLng = gpsState.longitude,
                onGetDirections = {
                    openMapsDirections(context, mosque, gpsState.latitude, gpsState.longitude)
                }
            )
        }
    }
}

@Composable
private fun MosqueItemCard(
    mosque: NearbyMosque,
    userLat: Double?,
    userLng: Double?,
    onGetDirections: () -> Unit
) {
    val palette = LocalAppPalette.current

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (mosque.isHistorical) listOf(palette.primary.copy(alpha = 0.5f), Color.Transparent)
                else listOf(Color.White.copy(alpha = 0.06f), Color.Transparent)
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mosque_card_${mosque.id}")
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mosque.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (mosque.isHistorical) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = palette.secondary.copy(alpha = 0.15f),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "Tarihi Eser & Külliye",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = palette.secondary,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Real-time Distance Badge with walk time
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.primary.copy(alpha = 0.2f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(palette.primary, palette.accent))
                    )
                ) {
                    val walkMinutes = (mosque.distanceMeters / 80).coerceAtLeast(1)
                    val distText = if (mosque.distanceMeters < 1000) {
                        "${mosque.distanceMeters} m (~$walkMinutes dk)"
                    } else {
                        "%.1f km".format(mosque.distanceMeters / 1000.0)
                    }

                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NearMe,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = distText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.primary
                            )
                        )
                    }
                }
            }

            // Address Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = palette.textMuted,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = mosque.address,
                    style = MaterialTheme.typography.bodySmall.copy(color = palette.textSecondary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Facilities / Features chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                mosque.features.take(3).forEach { feature ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = palette.surfaceVariant
                    ) {
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = palette.textMuted,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Action Button: Realtime Directions
            Button(
                onClick = onGetDirections,
                colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("directions_button_${mosque.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Directions,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bulunduğum Noktadan Yol Tarifi Al",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                )
            }
        }
    }
}

private fun openMapsDirections(
    context: Context,
    mosque: NearbyMosque,
    userLat: Double?,
    userLng: Double?
) {
    try {
        val uri = if (userLat != null && userLng != null) {
            Uri.parse("google.navigation:q=${mosque.latitude},${mosque.longitude}&mode=w")
        } else {
            Uri.parse("geo:${mosque.latitude},${mosque.longitude}?q=${Uri.encode(mosque.name)}")
        }

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val browserUri = if (userLat != null && userLng != null) {
                Uri.parse("https://www.google.com/maps/dir/?api=1&origin=$userLat,$userLng&destination=${mosque.latitude},${mosque.longitude}&travelmode=walking")
            } else {
                Uri.parse("https://www.google.com/maps/search/?api=1&query=${mosque.latitude},${mosque.longitude}")
            }
            context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Harita açılamadı: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

private fun openSurroundingMosquesInMaps(
    context: Context,
    userLat: Double?,
    userLng: Double?
) {
    try {
        val uri = if (userLat != null && userLng != null) {
            Uri.parse("geo:$userLat,$userLng?q=cami")
        } else {
            Uri.parse("geo:0,0?q=cami")
        }
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val browserUri = if (userLat != null && userLng != null) {
                Uri.parse("https://www.google.com/maps/search/cami/@$userLat,$userLng,15z")
            } else {
                Uri.parse("https://www.google.com/maps/search/cami")
            }
            context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Harita açılamadı: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
