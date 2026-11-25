package com.example.admin.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.admin.data.CategoryItem
import com.example.admin.data.RemoteWallpaperItem
import com.example.admin.ui.theme.AdminBorderSubtle
import com.example.admin.ui.theme.AdminDanger
import com.example.admin.ui.theme.AdminNeonCyan
import com.example.admin.ui.theme.AdminRoyalPurple
import com.example.admin.ui.theme.AdminSoftViolet
import com.example.admin.ui.theme.AdminSuccess
import com.example.admin.ui.theme.AdminSurface
import com.example.admin.ui.theme.AdminSurfaceElevated
import com.example.admin.ui.theme.AdminTextSecondary
import com.example.admin.ui.theme.AdminWarning

@Composable
fun AdminWallpaperUploadScreen(
    categories: List<CategoryItem>,
    wallpapers: List<RemoteWallpaperItem>,
    isUploading: Boolean,
    uploadProgress: Float,
    uploadStatusText: String,
    onUploadImages: (List<Uri>, String, String, CategoryItem?, Boolean, Boolean) -> Unit,
    onTogglePublish: (RemoteWallpaperItem) -> Unit,
    onTogglePremium: (RemoteWallpaperItem) -> Unit,
    onDeleteWallpaper: (RemoteWallpaperItem) -> Unit
) {
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember(categories) {
        mutableStateOf(categories.firstOrNull { it.categoryId != "cat_live" } ?: categories.firstOrNull())
    }
    var isPremium by remember { mutableStateOf(false) }
    var publishNow by remember { mutableStateOf(true) }

    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedUris = uris
        }
    }

    val imageWallpapers = remember(wallpapers) {
        wallpapers.filter { it.mediaType == "IMAGE" }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_image_upload_list"),
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
                        text = "Upload HD & 4K Wallpapers",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )

                    FilledTonalButton(
                        onClick = {
                            galleryPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = !isUploading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("pick_gallery_images_btn")
                    ) {
                        Icon(Icons.Rounded.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            if (selectedUris.isEmpty()) "Select Multiple Images from Gallery"
                            else "${selectedUris.size} Image(s) Selected"
                        )
                    }

                    if (selectedUris.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(selectedUris) { uri ->
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Selected preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(width = 92.dp, height = 148.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .border(1.dp, AdminNeonCyan.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it.take(80) },
                        label = { Text("Wallpaper Title") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("image_title_input")
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
                        Text("Premium Only Wallpaper", color = Color.White)
                        Switch(checked = isPremium, onCheckedChange = { isPremium = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Publish Immediately to User App", color = Color.White)
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
                                color = AdminNeonCyan
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
                            onUploadImages(
                                selectedUris,
                                title,
                                description,
                                selectedCategory,
                                isPremium,
                                publishNow
                            )
                            selectedUris = emptyList()
                            title = ""
                            description = ""
                        },
                        enabled = !isUploading && selectedUris.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("upload_images_submit_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = AdminRoyalPurple),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Rounded.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Upload & Sync Wallpapers")
                    }
                }
            }
        }

        item {
            Text(
                text = "Published & Draft Image Wallpapers (${imageWallpapers.size})",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }

        items(imageWallpapers, key = { it.wallpaperId }) { item ->
            RemoteWallpaperManageRow(
                item = item,
                onTogglePublish = { onTogglePublish(item) },
                onTogglePremium = { onTogglePremium(item) },
                onDelete = { onDeleteWallpaper(item) }
            )
        }
    }
}

@Composable
fun RemoteWallpaperManageRow(
    item: RemoteWallpaperItem,
    onTogglePublish: () -> Unit,
    onTogglePremium: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = AdminSurfaceElevated,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AdminBorderSubtle, RoundedCornerShape(18.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AdminSurface)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.categoryName} · ${item.resolutionLabel}",
                    style = MaterialTheme.typography.labelMedium,
                    color = AdminTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${if (item.isPublished) "Published" else "Draft"} · ${if (item.isPremium) "Pro" else "Free"} · ${item.downloadCount} downloads",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (item.isPublished) AdminSuccess else AdminWarning,
                    fontWeight = FontWeight.Medium
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onTogglePremium) {
                    Icon(
                        imageVector = Icons.Rounded.WorkspacePremium,
                        contentDescription = "Toggle Free/Premium",
                        tint = if (item.isPremium) AdminSoftViolet else AdminTextSecondary
                    )
                }
                IconButton(onClick = onTogglePublish) {
                    Icon(
                        imageVector = if (item.isPublished) Icons.Rounded.Public else Icons.Rounded.VisibilityOff,
                        contentDescription = "Toggle Publish",
                        tint = if (item.isPublished) AdminSuccess else AdminWarning
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        tint = AdminDanger
                    )
                }
            }
        }
    }
}
