package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.alarm.AlarmPermissionHelper
import com.example.ui.theme.LocalAppPalette

/**
 * Startup permission handler that displays a user-friendly 'Location Permission Rationale'
 * dialog before requesting system permissions, explaining clearly why location is needed.
 */
@Composable
fun StartupPermissionHandler(
    onPermissionsHandled: () -> Unit
) {
    val context = LocalContext.current

    val permissionsToRequest = remember {
        val list = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        list
    }

    var showDialog by remember {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        mutableStateOf(!hasFine && !hasCoarse)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        showDialog = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !AlarmPermissionHelper.canScheduleExactAlarms(context)) {
            AlarmPermissionHelper.openExactAlarmSettings(context)
        }
        onPermissionsHandled()
    }

    if (showDialog) {
        LocationPermissionRationaleDialog(
            onGrantClick = {
                launcher.launch(permissionsToRequest.toTypedArray())
            },
            onDismissClick = {
                showDialog = false
                onPermissionsHandled()
            }
        )
    }
}

/**
 * Standalone, user-friendly Location Permission Rationale Dialog.
 * Explains clearly why location access is requested in simple terms:
 * 1. Accurate prayer times based on precise sun angles.
 * 2. Exact Qibla bearing calculation towards the Kaaba.
 * 3. Finding nearby mosques and walking distances.
 * 4. 100% on-device local calculation guarantee (privacy).
 */
@Composable
fun LocationPermissionRationaleDialog(
    onGrantClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismissClick,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = palette.surface,
            tonalElevation = 6.dp,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(
                    listOf(palette.primary.copy(alpha = 0.6f), Color.Transparent)
                )
            ),
            modifier = modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 480.dp)
                .padding(vertical = 16.dp)
                .testTag("location_permission_rationale_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(palette.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismissClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("close_rationale_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = palette.textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Title & Subtitle
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Konum İzni Neden Gerekli?",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Ezanla, ibadetlerinizi tam vaktinde ve doğru yönde ifa edebilmeniz için konum erişimine ihtiyaç duyar:",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = palette.textSecondary,
                            lineHeight = 20.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                // Rationale Items
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RationaleItem(
                        icon = Icons.Default.AccessTime,
                        iconTint = palette.primary,
                        title = "Hassas Namaz Vakitleri",
                        description = "Bulunduğunuz noktanın coğrafi koordinatlarına (enlem/boylam) göre güneşin doğuş, batış ve zeval açıları hesaplanır; saniyesi saniyesine vakitler sunulur.",
                        palette = palette
                    )

                    RationaleItem(
                        icon = Icons.Default.Explore,
                        iconTint = palette.accent,
                        title = "Doğru Kıble Pusulası",
                        description = "Kabe-i Muazzama'nın bulunduğunuz konuma göre yön açısı ve pusula derecesi sapmasız olarak tespit edilir.",
                        palette = palette
                    )

                    RationaleItem(
                        icon = Icons.Default.Place,
                        iconTint = Color(0xFF4CAF50),
                        title = "Yakındaki Camiler",
                        description = "Çevrenizdeki camileri, uzaklıklarını ve yol tariflerini haritada kolayca bulmanıza yardımcı olur.",
                        palette = palette
                    )
                }

                // Privacy guarantee note
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Gizlilik Güvencesi: Konumunuz yalnızca cihazınızda yerel hesaplama amacıyla kullanılır; asla harici sunuculara iletilmez veya paylaşılmaz.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = palette.textSecondary,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onGrantClick,
                        colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("grant_location_permission_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Konum İznini Ver",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        )
                    }

                    TextButton(
                        onClick = onDismissClick,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("dismiss_location_permission_button")
                    ) {
                        Text(
                            text = "Şimdilik Şehir Seçerek Devam Et",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = palette.textMuted,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RationaleItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String,
    palette: com.example.ui.theme.AppPalette
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = palette.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = palette.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                )
            }
        }
    }
}
