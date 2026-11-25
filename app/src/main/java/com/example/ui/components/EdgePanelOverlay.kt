package com.example.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.InstalledAppInfo
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.LocalLauncherTokens
import com.example.ui.theme.RoyalPurple

@Composable
fun EdgePanelOverlay(
    visible: Boolean,
    favoriteApps: List<InstalledAppInfo>,
    badgeCounts: Map<String, Int>,
    notificationBadgesEnabled: Boolean,
    themedIconMode: Boolean,
    iconProvider: (String) -> Drawable?,
    onDismiss: () -> Unit,
    onLaunchApp: (InstalledAppInfo) -> Unit,
    onOpenControlCenter: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenStudio: () -> Unit
) {
    val tokens = LocalLauncherTokens.current

    if (!visible) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("edge_panel_overlay")
    ) {
        // Scrim tap to dismiss
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        )

        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(animationSpec = tween(220)) { it } + fadeIn(),
            exit = slideOutHorizontally(animationSpec = tween(180)) { it } + fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            val totalUnread = remember(badgeCounts, notificationBadgesEnabled) {
                if (notificationBadgesEnabled) badgeCounts.values.sum() else 0
            }

            Column(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(top = 12.dp, bottom = 12.dp, end = 8.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xEB0B1124),
                                Color(0xEB15102E)
                            )
                        )
                    )
                    .border(
                        1.2.dp,
                        Brush.verticalGradient(listOf(ElectricCyan.copy(alpha = 0.65f), RoyalPurple.copy(alpha = 0.65f))),
                        RoundedCornerShape(28.dp)
                    )
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Edge Panel",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = if (totalUnread > 0) "$totalUnread active badges" else "Quick OS Shortcuts",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close Edge Panel",
                            tint = Color.White
                        )
                    }
                }

                // Quick Actions 2x2 Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EdgeQuickButton(
                        icon = Icons.Rounded.Tune,
                        label = "Controls",
                        accent = ElectricCyan,
                        onClick = {
                            onDismiss()
                            onOpenControlCenter()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    EdgeQuickButton(
                        icon = Icons.Rounded.Search,
                        label = "AI Search",
                        accent = RoyalPurple,
                        onClick = {
                            onDismiss()
                            onOpenSearch()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EdgeQuickButton(
                        icon = Icons.Rounded.Apps,
                        label = "All Apps",
                        accent = tokens.primary,
                        onClick = {
                            onDismiss()
                            onOpenDrawer()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    EdgeQuickButton(
                        icon = Icons.Rounded.Settings,
                        label = "Studio",
                        accent = Color(0xFF10B981),
                        onClick = {
                            onDismiss()
                            onOpenStudio()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (totalUnread > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ElectricCyan.copy(alpha = 0.14f))
                            .border(0.8.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$totalUnread unread notifications across apps",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Text(
                    text = "Quick Launch Apps",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White.copy(alpha = 0.85f)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(favoriteApps.take(10), key = { "edge_${it.packageName}" }) { app ->
                        val count = if (notificationBadgesEnabled) badgeCounts[app.packageName] ?: 0 else 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .clickable {
                                    onDismiss()
                                    onLaunchApp(app)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AppIconItem(
                                app = app,
                                iconDrawable = iconProvider(app.packageName),
                                iconSize = 40.dp,
                                showLabel = false,
                                themedIconMode = themedIconMode,
                                badgeCount = count,
                                onClick = {
                                    onDismiss()
                                    onLaunchApp(app)
                                },
                                onLongClick = {}
                            )
                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (count > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EdgeQuickButton(
    icon: ImageVector,
    label: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.16f))
            .border(0.9.dp, accent.copy(alpha = 0.40f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = accent,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
