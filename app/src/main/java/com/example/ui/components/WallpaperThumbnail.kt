package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.WallpaperItem
import com.example.data.wallpaper.WallpaperRenderer
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.LocalLauncherTokens

private sealed interface BitmapLoadState {
    data object Loading : BitmapLoadState
    data class Ready(val bitmap: Bitmap) : BitmapLoadState
    data object Failed : BitmapLoadState
}

@Composable
fun WallpaperThumbnailImage(
    item: WallpaperItem,
    renderer: WallpaperRenderer,
    fullPreview: Boolean = false,
    autoCache: Boolean = true,
    modifier: Modifier = Modifier
) {
    val state by produceState<BitmapLoadState>(initialValue = BitmapLoadState.Loading, key1 = item.id, key2 = fullPreview) {
        value = BitmapLoadState.Loading
        val result = runCatching {
            if (fullPreview) renderer.getFullPreviewBitmap(item)
            else renderer.getThumbnailBitmap(item, autoDiskCache = autoCache)
        }
        value = result.fold(
            onSuccess = { BitmapLoadState.Ready(it) },
            onFailure = { BitmapLoadState.Failed }
        )
    }

    val fallbackColors = item.paletteHexes.take(2).map { Color(it) }.ifEmpty {
        listOf(Color(0xFF0A0F1A), Color(0xFF1E293B))
    }

    val remoteThumb = item.remoteThumbnailUrl?.takeIf { it.isNotBlank() }
        ?: item.remoteMediaUrl?.takeIf { it.isNotBlank() && !item.isRemoteVideo }

    if (item.isRemoteContent && remoteThumb != null) {
        Box(
            modifier = modifier.background(Brush.verticalGradient(fallbackColors)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = remoteThumb,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        return
    }

    Box(
        modifier = modifier.background(Brush.verticalGradient(fallbackColors)),
        contentAlignment = Alignment.Center
    ) {
        when (val s = state) {
            BitmapLoadState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            BitmapLoadState.Failed -> {
                Icon(
                    imageVector = if (item.isRemoteVideo) Icons.Rounded.PlayCircle else Icons.Rounded.BrokenImage,
                    contentDescription = "Failed to load ${item.name}",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(28.dp)
                )
            }
            is BitmapLoadState.Ready -> {
                val safeImage = if (!s.bitmap.isRecycled) {
                    runCatching { s.bitmap.asImageBitmap() }.getOrNull()
                } else {
                    null
                }
                if (safeImage != null) {
                    Image(
                        bitmap = safeImage,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun WallpaperCard(
    item: WallpaperItem,
    renderer: WallpaperRenderer,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 0.64f,
    isPremiumUnlocked: Boolean = true
) {
    val tokens = LocalLauncherTokens.current
    val shape = RoundedCornerShape(tokens.cardRadius)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .clip(shape)
            .border(1.dp, tokens.borderSubtle, shape)
            .clickable(onClick = onClick)
            .testTag("wallpaper_card_${item.id}")
    ) {
        WallpaperThumbnailImage(
            item = item,
            renderer = renderer,
            modifier = Modifier.fillMaxSize()
        )

        // Subtle scrim for legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.25f),
                        0.45f to Color.Transparent,
                        1.0f to Color.Black.copy(alpha = 0.82f)
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                if (item.isPremium || !isPremiumUnlocked) {
                    ProBadge(label = if (item.isPremium) "PRO" else "PAID")
                }
                if (item.isRemoteVideo) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.68f))
                            .border(0.8.dp, ElectricCyan.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayCircle,
                            contentDescription = "Live Video",
                            tint = ElectricCyan,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "LIVE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                } else if (!item.isPremium && item.isDownloaded) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                            .padding(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DownloadDone,
                            contentDescription = "Downloaded",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .testTag("fav_wallpaper_${item.id}")
            ) {
                Icon(
                    imageVector = if (item.isFavorite) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (item.isFavorite) "Remove ${item.name} from favorites" else "Save ${item.name} to favorites",
                    tint = if (item.isFavorite) Color(0xFFF43F5E) else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${item.displayCategoryLabel} • ${item.resolution}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.78f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
