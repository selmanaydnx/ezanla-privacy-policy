package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.IslamicDatabase
import com.example.model.IslamicVideo
import com.example.ui.theme.LocalAppPalette

@Composable
fun IslamicVideosScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = LocalAppPalette.current
    val allVideos = IslamicDatabase.islamicVideos
    val categories = listOf("Tümü", "Vaaz & Sohbet", "Kuran Tilaveti", "Tefsir Dersi", "Siyer-i Nebî", "Ramazan Özel")
    var selectedCategory by remember { mutableStateOf("Tümü") }

    val filteredVideos = remember(selectedCategory) {
        if (selectedCategory == "Tümü") allVideos
        else allVideos.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        text = "Dini Videolar",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                    )
                    Text(
                        text = "Sohbetler, Kuran tilavetleri ve tefsir dersleri",
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
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Categories Carousel
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = palette.primary,
                            selectedLabelColor = Color.Black,
                            containerColor = palette.surface,
                            labelColor = palette.textSecondary
                        )
                    )
                }
            }
        }

        // Videos List
        items(filteredVideos, key = { it.id }) { video ->
            VideoCardItem(
                video = video,
                onClick = { openVideo(context, video) }
            )
        }
    }
}

@Composable
private fun VideoCardItem(
    video: IslamicVideo,
    onClick: () -> Unit
) {
    val palette = LocalAppPalette.current

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("video_card_${video.id}")
    ) {
        Column {
            // Thumbnail with Duration & Play Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                )

                // Big Center Play Icon
                Surface(
                    shape = CircleShape,
                    color = palette.primary.copy(alpha = 0.85f),
                    modifier = Modifier
                        .size(54.dp)
                        .align(Alignment.Center)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Oynat",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Duration badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.8f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = video.duration,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                // Category badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = palette.surface.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = video.category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = palette.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Info Details
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = video.speaker,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = palette.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Text(
                    text = video.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = palette.textSecondary,
                        lineHeight = 16.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun openVideo(context: Context, video: IslamicVideo) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.videoUrl))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Video açılamadı: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
