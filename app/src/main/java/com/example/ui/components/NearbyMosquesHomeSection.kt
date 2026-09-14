package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.City
import com.example.model.NearbyMosque
import com.example.ui.theme.LocalAppPalette
import java.util.Locale

/**
 * Ana ekranda bütünü ve görsel ahengi bozmadan yer alan zarif "Yakın Camiler" vitrini.
 * En yakın camileri mesafeleri, özellikleri ve hızlı yol tarifi butonu ile listeler.
 */
@Composable
fun NearbyMosquesHomeSection(
    currentCity: City,
    onOpenMosquesScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = LocalAppPalette.current

    // Bulunulan şehre göre en yakın 3 camiyi hazırla
    val homeMosques = remember(currentCity) {
        listOf(
            NearbyMosque(
                id = "home_mosque_1",
                name = "${currentCity.name} Merkez Camii",
                city = currentCity.name,
                distanceMeters = 180,
                address = "Merkez Mah. Cami Sk. No:2, ${currentCity.name}",
                latitude = currentCity.latitude + 0.0014,
                longitude = currentCity.longitude + 0.0012,
                features = listOf("Vakit Namazları", "Şadırvan", "Kadınlar Bölümü"),
                isHistorical = false
            ),
            NearbyMosque(
                id = "home_mosque_2",
                name = "${currentCity.name} Tarihî Çarşı Camii",
                city = currentCity.name,
                distanceMeters = 340,
                address = "Çarşı Meydanı No:14, ${currentCity.name}",
                latitude = currentCity.latitude - 0.0022,
                longitude = currentCity.longitude + 0.0021,
                features = listOf("Tarihî Doku", "Cuma Cemaati", "Abdesthane"),
                isHistorical = true
            ),
            NearbyMosque(
                id = "home_mosque_3",
                name = "${currentCity.name} Fatih Camii",
                city = currentCity.name,
                distanceMeters = 520,
                address = "Fatih Mah. Barış Cad. No:8, ${currentCity.name}",
                latitude = currentCity.latitude + 0.0035,
                longitude = currentCity.longitude - 0.0031,
                features = listOf("Geniş Avlu", "Otopark", "Klimalı"),
                isHistorical = false
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Bölüm Başlığı
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mosque,
                    contentDescription = null,
                    tint = palette.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "YAKIN CAMİLER",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.primary,
                        letterSpacing = 1.sp
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onOpenMosquesScreen() }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Tümünü Gör",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = palette.accent,
                        fontWeight = FontWeight.Bold
                    )
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Tümünü Gör",
                    tint = palette.accent,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Camilerin kompakt kart listesi
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            homeMosques.forEach { mosque ->
                HomeMosqueItemCard(
                    mosque = mosque,
                    onClick = onOpenMosquesScreen,
                    onDirectionsClick = {
                        openMosqueNavigation(context, mosque)
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeMosqueItemCard(
    mosque: NearbyMosque,
    onClick: () -> Unit,
    onDirectionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current

    Surface(
        color = palette.surface,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp,
        border = BorderStroke(
            width = 1.dp,
            color = palette.primary.copy(alpha = if (palette.isDark) 0.18f else 0.10f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cami İkon Rozeti
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(palette.primary.copy(alpha = if (palette.isDark) 0.20f else 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (mosque.isHistorical) Icons.Default.Mosque else Icons.Default.Place,
                    contentDescription = null,
                    tint = palette.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Cami Bilgileri
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = mosque.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = palette.textPrimary,
                            fontSize = 14.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (mosque.isHistorical) {
                        Surface(
                            color = palette.accent.copy(alpha = if (palette.isDark) 0.22f else 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Tarihî",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = palette.accent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Mesafe ve Adres
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Mesafe Rozeti
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = null,
                            tint = palette.secondary,
                            modifier = Modifier.size(13.dp)
                        )
                        val distText = if (mosque.distanceMeters < 1000) {
                            "${mosque.distanceMeters} m"
                        } else {
                            String.format(Locale.getDefault(), "%.1f km", mosque.distanceMeters / 1000.0)
                        }
                        Text(
                            text = distText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = palette.secondary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall.copy(color = palette.textMuted)
                    )

                    Text(
                        text = mosque.address,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = palette.textSecondary,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Yol Tarifi Hızlı Butonu
            IconButton(
                onClick = onDirectionsClick,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(palette.primary.copy(alpha = if (palette.isDark) 0.15f else 0.08f))
            ) {
                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = "Yol Tarifi Al",
                    tint = palette.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Google Haritalar veya varsayılan harita uygulaması üzerinden yol tarifi başlatır.
 */
private fun openMosqueNavigation(context: Context, mosque: NearbyMosque) {
    try {
        val navUri = Uri.parse("google.navigation:q=${mosque.latitude},${mosque.longitude}&mode=w")
        val intent = Intent(Intent.ACTION_VIEW, navUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        val geoUri = Uri.parse("geo:${mosque.latitude},${mosque.longitude}?q=${Uri.encode(mosque.name)}")
        val fallbackIntent = Intent(Intent.ACTION_VIEW, geoUri)
        context.startActivity(fallbackIntent)
    }
}
