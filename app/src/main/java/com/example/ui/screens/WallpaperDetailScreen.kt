package com.example.ui.screens

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.WallpaperItem
import com.example.data.wallpaper.WallpaperRenderer
import com.example.data.wallpaper.WallpaperTarget
import com.example.ui.components.GlassCard
import com.example.ui.components.ProBadge
import com.example.ui.components.WallpaperThumbnailImage
import com.example.ui.viewmodel.WallpaperActionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WallpaperDetailScreen(
    wallpaper: WallpaperItem?,
    isPremiumUnlocked: Boolean,
    actionState: WallpaperActionState,
    renderer: WallpaperRenderer,
    liveWallpapersEnabled: Boolean = true,
    onMarkViewed: (String) -> Unit,
    onBack: () -> Unit,
    onToggleFavorite: (WallpaperItem) -> Unit,
    onDownload: (WallpaperItem) -> Unit,
    onApply: (WallpaperItem, WallpaperTarget) -> Unit,
    onApplyLiveVideo: ((WallpaperItem) -> Unit)? = null,
    onUnlockPremium: () -> Unit,
    onClearActionState: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showApplySheet by remember { mutableStateOf(false) }
    var showInfoSheet by remember { mutableStateOf(false) }

    LaunchedEffect(wallpaper?.id) {
        wallpaper?.id?.let(onMarkViewed)
    }

    LaunchedEffect(actionState) {
        when (actionState) {
            is WallpaperActionState.Success -> {
                snackbarHostState.showSnackbar(actionState.message)
                onClearActionState()
            }
            is WallpaperActionState.Error -> {
                snackbarHostState.showSnackbar(actionState.message)
                onClearActionState()
            }
            else -> Unit
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (wallpaper == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val locked = !isPremiumUnlocked

        Box(modifier = Modifier.fillMaxSize()) {
            // Full-screen wallpaper preview (with live looping MP4 video overlay when applicable)
            WallpaperThumbnailImage(
                item = wallpaper,
                renderer = renderer,
                fullPreview = true,
                modifier = Modifier.fillMaxSize()
            )
            if (wallpaper.isRemoteVideo && !wallpaper.remoteMediaUrl.isNullOrBlank()) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            setOnPreparedListener { mp ->
                                mp.isLooping = true
                                mp.setVolume(0f, 0f)
                                start()
                            }
                            setOnErrorListener { _, _, _ -> true }
                            runCatching {
                                setVideoURI(Uri.parse(wallpaper.remoteMediaUrl))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Top & Bottom gradient overlays for crisp legibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Black.copy(alpha = 0.55f),
                            0.25f to Color.Transparent,
                            0.62f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.90f)
                        )
                    )
            )

            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .testTag("wallpaper_detail_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { showInfoSheet = true },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .testTag("wallpaper_detail_info")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = "Wallpaper information",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { onToggleFavorite(wallpaper) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .testTag("wallpaper_detail_favorite")
                    ) {
                        Icon(
                            imageVector = if (wallpaper.isFavorite) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (wallpaper.isFavorite) Color(0xFFF43F5E) else Color.White
                        )
                    }
                }
            }

            // Bottom Action Dock
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (wallpaper.isPremium) {
                        ProBadge()
                    }
                    if (wallpaper.isRemoteVideo) {
                        ProBadge(label = "LIVE MP4")
                    }
                    Text(
                        text = wallpaper.collection,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                Text(
                    text = wallpaper.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )

                Text(
                    text = "${wallpaper.displayCategoryLabel} • ${wallpaper.resolution} • ${wallpaper.fileSizeLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f)
                )

                if (actionState is WallpaperActionState.Loading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Text(
                            text = actionState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (locked) {
                        Button(
                            onClick = onUnlockPremium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("wallpaper_unlock_pro_button")
                        ) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Unlock Pro to Download & Apply")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onDownload(wallpaper) },
                            enabled = actionState !is WallpaperActionState.Loading,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("wallpaper_download_button")
                        ) {
                            Icon(
                                imageVector = if (wallpaper.isDownloaded) Icons.Rounded.DownloadDone else Icons.Rounded.Download,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (wallpaper.isDownloaded) "Saved" else "Download",
                                color = Color.White
                            )
                        }

                        if (wallpaper.isRemoteVideo && liveWallpapersEnabled && onApplyLiveVideo != null) {
                            Button(
                                onClick = { onApplyLiveVideo(wallpaper) },
                                enabled = actionState !is WallpaperActionState.Loading,
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(52.dp)
                                    .testTag("wallpaper_apply_button")
                            ) {
                                Icon(Icons.Rounded.PlayCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Set Live Video")
                            }
                        } else {
                            Button(
                                onClick = { showApplySheet = true },
                                enabled = actionState !is WallpaperActionState.Loading,
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(52.dp)
                                    .testTag("wallpaper_apply_button")
                            ) {
                                Icon(Icons.Rounded.PhoneAndroid, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Set Wallpaper")
                            }
                        }
                    }
                }
            }
        }

        if (showApplySheet) {
            ModalBottomSheet(onDismissRequest = { showApplySheet = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Apply '${wallpaper.name}'",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Choose where Android should apply this wallpaper.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            showApplySheet = false
                            onApply(wallpaper, WallpaperTarget.HOME)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("apply_wp_home")
                    ) {
                        Icon(Icons.Rounded.Home, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Home Screen")
                    }
                    FilledTonalButton(
                        onClick = {
                            showApplySheet = false
                            onApply(wallpaper, WallpaperTarget.LOCK)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("apply_wp_lock")
                    ) {
                        Icon(Icons.Rounded.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lock Screen")
                    }
                    OutlinedButton(
                        onClick = {
                            showApplySheet = false
                            onApply(wallpaper, WallpaperTarget.BOTH)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("apply_wp_both")
                    ) {
                        Icon(Icons.Rounded.PhoneAndroid, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Both Home & Lock Screen")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        if (showInfoSheet) {
            ModalBottomSheet(onDismissRequest = { showInfoSheet = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = wallpaper.name, style = MaterialTheme.typography.titleLarge)
                    GlassCard(contentPadding = PaddingValues(14.dp)) {
                        InfoLine("Category", wallpaper.category.displayName)
                        InfoLine("Collection", wallpaper.collection)
                        InfoLine("Resolution", wallpaper.resolution)
                        InfoLine("Estimated Size", wallpaper.fileSizeLabel)
                        InfoLine("License", wallpaper.license)
                        if (wallpaper.localFilePath != null) {
                            InfoLine("Saved Path", wallpaper.localFilePath)
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        wallpaper.tags.forEach { tag ->
                            AssistChip(onClick = {}, label = { Text("#$tag") })
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
