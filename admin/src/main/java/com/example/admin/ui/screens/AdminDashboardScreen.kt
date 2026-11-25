package com.example.admin.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SettingsRemote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.admin.data.AdminSession
import com.example.admin.data.RemoteWallpaperItem
import com.example.admin.ui.theme.AdminBorderSubtle
import com.example.admin.ui.theme.AdminElectricBlue
import com.example.admin.ui.theme.AdminNeonCyan
import com.example.admin.ui.theme.AdminRoyalPurple
import com.example.admin.ui.theme.AdminSoftViolet
import com.example.admin.ui.theme.AdminSuccess
import com.example.admin.ui.theme.AdminSurface
import com.example.admin.ui.theme.AdminSurfaceElevated
import com.example.admin.ui.theme.AdminTextSecondary
import com.example.admin.ui.theme.AdminWarning
import com.example.admin.ui.viewmodel.AdminDashboardUiState

@Composable
fun AdminDashboardScreen(
    session: AdminSession,
    state: AdminDashboardUiState,
    onRefresh: () -> Unit,
    onNavigateUploadImages: () -> Unit,
    onNavigateUploadVideo: () -> Unit,
    onNavigateCategories: () -> Unit,
    onNavigateRemoteControl: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_dashboard_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AdminBorderSubtle, RoundedCornerShape(24.dp)),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF28195E),
                                    Color(0xFF142758),
                                    AdminSurface
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Welcome, ${session.displayName}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White
                                )
                                Text(
                                    text = "${session.email} · ${session.role}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AdminNeonCyan
                                )
                            }
                            IconButton(
                                onClick = onRefresh,
                                modifier = Modifier.testTag("admin_refresh_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "Refresh Live Sync",
                                    tint = Color.White
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onNavigateUploadImages,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("quick_upload_image_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = AdminRoyalPurple),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Rounded.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.size(6.dp))
                                Text("Add Images", maxLines = 1)
                            }
                            Button(
                                onClick = onNavigateUploadVideo,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("quick_upload_video_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = AdminElectricBlue),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Rounded.PlayCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.size(6.dp))
                                Text("Add Live MP4", maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricTile(
                    title = "Total Users",
                    value = state.metrics.totalUsers.toString(),
                    subtitle = "${state.metrics.activeUsers} active 24h",
                    icon = Icons.Rounded.Group,
                    accent = AdminNeonCyan,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    title = "Image Walls",
                    value = state.metrics.totalWallpapers.toString(),
                    subtitle = "${state.categories.size} categories",
                    icon = Icons.Rounded.Image,
                    accent = AdminSoftViolet,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricTile(
                    title = "Live Videos",
                    value = state.metrics.totalLiveVideos.toString(),
                    subtitle = "MP4 animated",
                    icon = Icons.Rounded.PlayCircle,
                    accent = AdminElectricBlue,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    title = "Downloads",
                    value = state.metrics.totalDownloads.toString(),
                    subtitle = "Live total",
                    icon = Icons.Rounded.CloudDownload,
                    accent = AdminSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = onNavigateCategories,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("dash_categories_btn")
                ) {
                    Icon(Icons.Rounded.Category, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Categories (${state.categories.size})")
                }
                FilledTonalButton(
                    onClick = onNavigateRemoteControl,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("dash_remote_btn")
                ) {
                    Icon(Icons.Rounded.SettingsRemote, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(if (state.globalConfig.maintenanceMode) "Maint: ON" else "App Controls")
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recently Uploaded Content",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = "${state.wallpapers.count { it.isPublished }} live",
                    style = MaterialTheme.typography.labelMedium,
                    color = AdminSuccess
                )
            }
        }

        if (state.wallpapers.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AdminSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AdminBorderSubtle, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = AdminNeonCyan,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "No remote wallpapers published yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Upload HD/4K images or MP4 live videos from Gallery. They appear instantly inside the User App.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AdminTextSecondary
                        )
                    }
                }
            }
        } else {
            items(state.wallpapers.take(12), key = { it.wallpaperId }) { item ->
                RecentUploadRow(item = item)
            }
        }
    }
}

@Composable
private fun MetricTile(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.border(1.dp, AdminBorderSubtle, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = AdminSurface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = AdminTextSecondary
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = accent
            )
        }
    }
}

@Composable
private fun RecentUploadRow(item: RemoteWallpaperItem) {
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
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AdminSurface)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.categoryName} · ${item.mediaType} · ${item.resolutionLabel}",
                    style = MaterialTheme.typography.labelMedium,
                    color = AdminTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (item.isPublished) "LIVE" else "DRAFT",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (item.isPublished) AdminSuccess else AdminWarning,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (item.isPremium) "PRO · ${item.downloadCount} dl" else "FREE · ${item.downloadCount} dl",
                    style = MaterialTheme.typography.labelMedium,
                    color = AdminTextSecondary
                )
            }
        }
    }
}
