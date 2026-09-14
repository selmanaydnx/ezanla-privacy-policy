package com.example.ui.screens

import android.app.WallpaperManager
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.data.IslamicDatabase
import com.example.model.IslamicWallpaper
import com.example.ui.theme.LocalAppPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun WallpapersScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = LocalAppPalette.current
    val allWallpapers = remember { IslamicDatabase.islamicWallpapers }
    var selectedCategory by remember { mutableStateOf("Tümü") }
    var selectedWallpaperForPreview by remember { mutableStateOf<IslamicWallpaper?>(null) }
    val scope = rememberCoroutineScope()
    var isSettingWallpaper by remember { mutableStateOf(false) }

    val categories = remember {
        listOf("Tümü", "Kâbe & Mekke", "Medine-i Münevvere", "Camiler & Kubbeler", "Hat Sanatı", "Kudüs & Miras", "Gece & Maneviyat")
    }

    val filteredWallpapers = remember(selectedCategory) {
        if (selectedCategory == "Tümü") allWallpapers else allWallpapers.filter { it.category == selectedCategory }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                        text = "İslâmî Duvar Kağıtları",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                    )
                    Text(
                        text = "${filteredWallpapers.size} adet yüksek çözünürlüklü manevi görsel",
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
                            imageVector = Icons.Default.Wallpaper,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Category Filter Chips
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        onClick = { selectedCategory = cat },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) palette.primary else palette.surface,
                        border = if (isSelected) null else CardDefaults.outlinedCardBorder()
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF1E1400) else palette.textPrimary
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Grid of Wallpapers
        items(filteredWallpapers, key = { it.id }) { item ->
            WallpaperGridItem(
                wallpaper = item,
                onClick = { selectedWallpaperForPreview = item }
            )
        }
    }

    // Fullscreen Preview & Set Wallpaper Dialog
    selectedWallpaperForPreview?.let { item ->
        Dialog(
            onDismissRequest = {
                if (!isSettingWallpaper) selectedWallpaperForPreview = null
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Full High-Res Image with smooth loading & spiritual fallback
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.imageUrl)
                        .crossfade(300)
                        .build(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF0F141C)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = palette.primary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    },
                    error = {
                        WallpaperFallbackCanvas(wallpaper = item)
                    }
                )

                // Top Vignette Shade
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                            )
                        )
                )

                // Close Button Top End
                IconButton(
                    onClick = { selectedWallpaperForPreview = null },
                    enabled = !isSettingWallpaper,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 40.dp, end = 16.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat",
                        tint = Color.White
                    )
                }

                // Category Tag Top Start
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 44.dp, start = 16.dp)
                ) {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = palette.secondary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                // Bottom Floating Control Panel
                Surface(
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = palette.surface.copy(alpha = 0.96f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                        )

                        Text(
                            text = "Duvar kağıdı uygulamak istediğiniz hedefi seçiniz:",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = palette.textSecondary
                            )
                        )

                        // 3 Options: Home Screen, Lock Screen, Both
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Ana Ekran
                            Button(
                                onClick = {
                                    scope.launch {
                                        isSettingWallpaper = true
                                        applyWallpaper(context, item, WallpaperTarget.HOME) { success ->
                                            isSettingWallpaper = false
                                            if (success) {
                                                Toast.makeText(context, "Ana ekran duvar kağıdı yapıldı!", Toast.LENGTH_LONG).show()
                                                selectedWallpaperForPreview = null
                                            } else {
                                                Toast.makeText(context, "Duvar kağıdı uygulanamadı.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                },
                                enabled = !isSettingWallpaper,
                                colors = ButtonDefaults.buttonColors(containerColor = palette.surfaceVariant),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("set_wallpaper_home")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = palette.primary, modifier = Modifier.size(16.dp))
                                    Text("Ana Ekran", fontSize = 12.sp, color = palette.textPrimary, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            // Kilit Ekranı
                            Button(
                                onClick = {
                                    scope.launch {
                                        isSettingWallpaper = true
                                        applyWallpaper(context, item, WallpaperTarget.LOCK) { success ->
                                            isSettingWallpaper = false
                                            if (success) {
                                                Toast.makeText(context, "Kilit ekranı duvar kağıdı yapıldı!", Toast.LENGTH_LONG).show()
                                                selectedWallpaperForPreview = null
                                            } else {
                                                Toast.makeText(context, "Duvar kağıdı uygulanamadı.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                },
                                enabled = !isSettingWallpaper,
                                colors = ButtonDefaults.buttonColors(containerColor = palette.surfaceVariant),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("set_wallpaper_lock")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = palette.primary, modifier = Modifier.size(16.dp))
                                    Text("Kilit Ekranı", fontSize = 12.sp, color = palette.textPrimary, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        // Primary Button: Her İkisine de Uygula
                        Button(
                            onClick = {
                                scope.launch {
                                    isSettingWallpaper = true
                                    applyWallpaper(context, item, WallpaperTarget.BOTH) { success ->
                                        isSettingWallpaper = false
                                        if (success) {
                                            Toast.makeText(context, "Duvar kağıdı telefonunuza başarıyla uygulandı!", Toast.LENGTH_LONG).show()
                                            selectedWallpaperForPreview = null
                                        } else {
                                            Toast.makeText(context, "Duvar kağıdı uygulanamadı.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            enabled = !isSettingWallpaper,
                            colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("set_wallpaper_both")
                        ) {
                            if (isSettingWallpaper) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color(0xFF1A1200))
                            } else {
                                Icon(Icons.Default.Wallpaper, contentDescription = null, tint = Color(0xFF1A1200))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Telefon Duvar Kağıdı Yap (Her İkisi)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1A1200)
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

@Composable
private fun WallpaperGridItem(
    wallpaper: IslamicWallpaper,
    onClick: () -> Unit
) {
    val palette = LocalAppPalette.current

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = palette.surface,
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .clickable(onClick = onClick)
            .testTag("wallpaper_item_${wallpaper.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(wallpaper.imageUrl)
                    .crossfade(300)
                    .build(),
                contentDescription = wallpaper.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(palette.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = palette.primary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                error = {
                    WallpaperFallbackCanvas(wallpaper = wallpaper)
                }
            )

            // Gradient shade
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Category tag top-start
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = wallpaper.category,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.secondary
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }

            // Bottom title and "Duvar Kağıdı Yap" hint
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = wallpaper.title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Wallpaper,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Duvar Kağıdı Yap",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = palette.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun WallpaperFallbackCanvas(
    wallpaper: IslamicWallpaper,
    modifier: Modifier = Modifier
) {
    val baseColor = Color(wallpaper.accentColorHex)
    val darkBg = Color(0xFF0D1117)
    val midBg = Color(0xFF161B22)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(darkBg, midBg, baseColor.copy(alpha = 0.35f), darkBg)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.42f)
            val maxR = size.width * 0.45f
            for (step in 1..4) {
                drawCircle(
                    color = baseColor.copy(alpha = 0.08f * step),
                    radius = maxR * (step / 4f),
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = baseColor.copy(alpha = 0.2f),
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Wallpaper,
                        contentDescription = null,
                        tint = baseColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Text(
                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = baseColor,
                    fontSize = 15.sp
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = wallpaper.title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 12.sp
                ),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

private enum class WallpaperTarget {
    HOME,
    LOCK,
    BOTH
}

private suspend fun applyWallpaper(
    context: Context,
    wallpaper: IslamicWallpaper,
    target: WallpaperTarget,
    onResult: (Boolean) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            var bitmap: android.graphics.Bitmap? = null
            try {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(wallpaper.imageUrl)
                    .allowHardware(false)
                    .build()
                val result = (loader.execute(request) as? SuccessResult)?.drawable
                bitmap = (result as? BitmapDrawable)?.bitmap
            } catch (_: Exception) {
                bitmap = null
            }

            // Eğer ağ bağlantısı yoksa veya indirme tamamlanamazsa sanatsal İslami duvar kağıdı oluştur (Her zaman çalışır)
            if (bitmap == null) {
                bitmap = createIslamicWallpaperBitmap(wallpaper)
            }

            val wallpaperManager = WallpaperManager.getInstance(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                when (target) {
                    WallpaperTarget.HOME -> wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                    WallpaperTarget.LOCK -> wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                    WallpaperTarget.BOTH -> wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                }
            } else {
                wallpaperManager.setBitmap(bitmap)
            }
            withContext(Dispatchers.Main) { onResult(true) }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { onResult(false) }
        }
    }
}

/**
 * Çevrimdışı durumlarda veya ağ gecikmelerinde anında uygulanan yüksek çözünürlüklü İslâmî duvar kağıdı üretici
 */
private fun createIslamicWallpaperBitmap(wallpaper: IslamicWallpaper): android.graphics.Bitmap {
    val width = 1080
    val height = 1920
    val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

    // Arka plan derin manevi gradyan
    val baseColor = wallpaper.accentColorHex.toInt()
    val darkColor = android.graphics.Color.argb(255, 12, 14, 22)
    val gradient = android.graphics.LinearGradient(
        0f, 0f, 0f, height.toFloat(),
        intArrayOf(darkColor, baseColor, darkColor),
        floatArrayOf(0f, 0.5f, 1f),
        android.graphics.Shader.TileMode.CLAMP
    )
    paint.shader = gradient
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    paint.shader = null

    // Geometrik İslami Desen Çizgileri
    paint.color = android.graphics.Color.argb(40, 255, 255, 255)
    paint.style = android.graphics.Paint.Style.STROKE
    paint.strokeWidth = 3f
    val centerX = width / 2f
    val centerY = height / 2f

    for (r in 100..500 step 80) {
        canvas.drawCircle(centerX, centerY, r.toFloat(), paint)
    }

    // Altın Kufi / Hat Yazısı & Başlık
    val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(230, 245, 215, 110)
        textSize = 58f
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    canvas.drawText("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", centerX, centerY - 80f, textPaint)

    textPaint.textSize = 44f
    textPaint.color = android.graphics.Color.WHITE
    canvas.drawText(wallpaper.title, centerX, centerY + 80f, textPaint)

    textPaint.textSize = 32f
    textPaint.color = android.graphics.Color.argb(180, 200, 200, 200)
    canvas.drawText("İslâmî Duvar Kağıtları • ${wallpaper.category}", centerX, centerY + 150f, textPaint)

    return bitmap
}
