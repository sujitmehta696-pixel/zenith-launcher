package com.example.admin.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.admin.data.CategoryItem
import com.example.admin.data.RemoteWallpaperItem
import com.example.admin.ui.theme.AdminBorderSubtle
import com.example.admin.ui.theme.AdminElectricBlue
import com.example.admin.ui.theme.AdminNeonCyan
import com.example.admin.ui.theme.AdminSurface
import com.example.admin.ui.theme.AdminTextSecondary

@Composable
fun AdminVideoUploadScreen(
    categories: List<CategoryItem>,
    wallpapers: List<RemoteWallpaperItem>,
    isUploading: Boolean,
    uploadProgress: Float,
    uploadStatusText: String,
    inspectVideo: suspend (Uri) -> Result<Pair<Bitmap?, String>>,
    onUploadVideo: (Uri?, String, String, CategoryItem?, Boolean, Boolean) -> Unit,
    onTogglePublish: (RemoteWallpaperItem) -> Unit,
    onTogglePremium: (RemoteWallpaperItem) -> Unit,
    onDeleteWallpaper: (RemoteWallpaperItem) -> Unit
) {
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var extractedThumb by remember { mutableStateOf<Bitmap?>(null) }
    var detectedResolution by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember(categories) {
        mutableStateOf(categories.firstOrNull { it.categoryId == "cat_live" } ?: categories.firstOrNull())
    }
    var isPremium by remember { mutableStateOf(false) }
    var publishNow by remember { mutableStateOf(true) }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedVideoUri = uri
        }
    }

    LaunchedEffect(selectedVideoUri) {
        val uri = selectedVideoUri
        if (uri != null) {
            inspectVideo(uri).onSuccess { (bmp, res) ->
                extractedThumb = bmp
                detectedResolution = res
            }
        } else {
            extractedThumb = null
            detectedResolution = ""
        }
    }

    val liveVideos = remember(wallpapers) {
        wallpapers.filter { it.mediaType == "VIDEO" }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_video_upload_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = AdminSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AdminBorderSubtle, RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Upload Live MP4 Video Wallpaper",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )

                    FilledTonalButton(
                        onClick = {
                            videoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        },
                        enabled = !isUploading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("pick_gallery_video_btn")
                    ) {
                        Icon(Icons.Rounded.VideoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            if (selectedVideoUri == null) "Select MP4 Video from Gallery"
                            else "Change Selected MP4 Video"
                        )
                    }

                    selectedVideoUri?.let { uri ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 130.dp, height = 200.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, AdminNeonCyan.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                            ) {
                                AndroidView(
                                    factory = { ctx ->
                                        VideoView(ctx).apply {
                                            setVideoURI(uri)
                                            setOnPreparedListener { mp ->
                                                mp.isLooping = true
                                                mp.setVolume(0f, 0f)
                                                start()
                                            }
                                        }
                                    },
                                    update = { view ->
                                        view.setVideoURI(uri)
                                        view.start()
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Auto-Generated Frame",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AdminNeonCyan
                                )
                                extractedThumb?.let { bmp ->
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = "Auto video thumbnail",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(92.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                }
                                if (detectedResolution.isNotBlank()) {
                                    Text(
                                        text = detectedResolution,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = AdminTextSecondary
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it.take(80) },
                        label = { Text("Live Wallpaper Title") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("video_title_input")
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it.take(400) },
                        label = { Text("Description (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (categories.isNotEmpty()) {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.labelMedium,
                            color = AdminTextSecondary
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categories, key = { it.categoryId }) { cat ->
                                FilterChip(
                                    selected = selectedCategory?.categoryId == cat.categoryId,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat.name) }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Premium Only Live Wallpaper", color = Color.White)
                        Switch(checked = isPremium, onCheckedChange = { isPremium = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Publish Immediately", color = Color.White)
                        Switch(checked = publishNow, onCheckedChange = { publishNow = it })
                    }

                    if (isUploading) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            LinearProgressIndicator(
                                progress = { uploadProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                color = AdminElectricBlue
                            )
                            Text(
                                text = "$uploadStatusText (${(uploadProgress * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelMedium,
                                color = AdminNeonCyan
                            )
                        }
                    }

                    Button(
                        onClick = {
                            onUploadVideo(
                                selectedVideoUri,
                                title,
                                description,
                                selectedCategory,
                                isPremium,
                                publishNow
                            )
                            selectedVideoUri = null
                            title = ""
                            description = ""
                        },
                        enabled = !isUploading && selectedVideoUri != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("upload_video_submit_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = AdminElectricBlue),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Rounded.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Upload & Publish Live MP4")
                    }
                }
            }
        }

        item {
            Text(
                text = "Live Video Wallpapers (${liveVideos.size})",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }

        items(liveVideos, key = { it.wallpaperId }) { item ->
            RemoteWallpaperManageRow(
                item = item,
                onTogglePublish = { onTogglePublish(item) },
                onTogglePremium = { onTogglePremium(item) },
                onDelete = { onDeleteWallpaper(item) }
            )
        }
    }
}
