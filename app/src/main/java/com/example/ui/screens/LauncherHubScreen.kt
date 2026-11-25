package com.example.ui.screens

import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.GestureAction
import com.example.data.model.InstalledAppInfo
import com.example.data.model.LauncherMode
import com.example.data.model.LauncherSettingsState
import com.example.data.model.SpinnerStyle
import com.example.data.model.ThemeIconStyle
import com.example.ui.components.AppIconItem
import com.example.ui.components.GestureSettingsPanel
import com.example.ui.components.GlassCard
import com.example.ui.components.HiddenAppsVaultDialog
import com.example.ui.components.ProBadge
import com.example.ui.components.SpinnerLauncherWheel
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LocalLauncherTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherHubScreen(
    settings: LauncherSettingsState,
    isDefaultLauncher: Boolean,
    isPremiumUnlocked: Boolean,
    homeApps: List<InstalledAppInfo>,
    allApps: List<InstalledAppInfo>,
    hiddenApps: List<InstalledAppInfo>,
    badgeCounts: Map<String, Int> = emptyMap(),
    notificationBadgesEnabled: Boolean = true,
    iconProvider: (String) -> Drawable?,
    onOpenLiveHome: () -> Unit,
    onRequestDefaultLauncher: () -> Unit,
    onSelectMode: (LauncherMode) -> Unit,
    onSelectSpinnerStyle: (SpinnerStyle) -> Unit,
    onSetGridSize: (Int, Int) -> Unit,
    onSetIconSize: (Int) -> Unit,
    onSetShowLabels: (Boolean) -> Unit,
    onSetSwipeDown: (GestureAction) -> Unit,
    onSetSwipeUp: (GestureAction) -> Unit,
    onSetDoubleTap: (GestureAction) -> Unit,
    onSetLongPress: (GestureAction) -> Unit,
    onSetPinch: (GestureAction) -> Unit = {},
    onSelectIconStyle: (ThemeIconStyle?) -> Unit = {},
    onSetThemeAppIcons: (Boolean) -> Unit,
    onSetThemeWidgets: (Boolean) -> Unit,
    onSetSyncThemeWallpaper: (Boolean) -> Unit,
    onSetLiquidGlass: (Boolean) -> Unit = {},
    onToggleAutoRolling: () -> Unit = {},
    onLaunchApp: (InstalledAppInfo) -> Unit,
    onToggleHiddenApp: (InstalledAppInfo) -> Unit,
    onToggleFavoriteApp: (InstalledAppInfo) -> Unit = {},
    onPinAppToHome: (InstalledAppInfo) -> Unit = {},
    onUnpinAppFromHome: (InstalledAppInfo) -> Unit = {},
    onOpenAppInfo: (String) -> Unit = {},
    onOpenGestureConfig: () -> Unit = {},
    onUnlockPremium: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalLauncherTokens.current
    var showHiddenDialog by remember { mutableStateOf(false) }
    var selectedAppForMenu by remember { mutableStateOf<InstalledAppInfo?>(null) }
    val allVisibleApps = remember(allApps) { allApps.filter { !it.isHidden } }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .testTag("launcher_hub_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "3D Launcher",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (!isPremiumUnlocked) {
                        ProBadge(label = "PAID ONLY")
                    }
                }
            }

            if (!isPremiumUnlocked) {
                item {
                    GlassCard(
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.testTag("launcher_paid_required_banner")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = "Paid Pro Required",
                                tint = Color(0xFFD4AF37),
                                modifier = Modifier.size(28.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Paid-Only Launcher & Wallpapers",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Active Pro subscription required to open 3D Home, set default launcher, or apply wallpapers & themes.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onUnlockPremium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("launcher_banner_unlock_pro")
                        ) {
                            Text("Unlock Pro Plan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Default Launcher Status + Open Live Home
            item {
                GlassCard(contentPadding = PaddingValues(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                !isPremiumUnlocked -> Icons.Rounded.Lock
                                isDefaultLauncher -> Icons.Rounded.CheckCircle
                                else -> Icons.Rounded.Home
                            },
                            contentDescription = null,
                            tint = when {
                                !isPremiumUnlocked -> Color(0xFFD4AF37)
                                isDefaultLauncher -> EmeraldSuccess
                                else -> tokens.primary
                            }
                        )
                        Text(
                            text = when {
                                !isPremiumUnlocked -> "Pro Required to Launch 3D Home"
                                isDefaultLauncher -> "Default Home Active"
                                else -> "Set as Default Launcher"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { if (isPremiumUnlocked) onOpenLiveHome() else onUnlockPremium() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("launcher_open_live_home")
                        ) {
                            if (!isPremiumUnlocked) {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(if (isPremiumUnlocked) "Open Home" else "Unlock to Open")
                        }
                        if (!isDefaultLauncher) {
                            OutlinedButton(
                                onClick = { if (isPremiumUnlocked) onRequestDefaultLauncher() else onUnlockPremium() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("launcher_set_default_button")
                            ) {
                                Text(if (isPremiumUnlocked) "Set Default" else "Pro Required")
                            }
                        }
                    }
                }
            }

            // Live Interactive 3D Rolling Preview
            item {
                GlassCard(contentPadding = PaddingValues(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = settings.launcherMode.displayName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${allVisibleApps.size} apps",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(if (isPremiumUnlocked) 1f else 0.35f)
                        ) {
                            when (settings.launcherMode) {
                                LauncherMode.SPINNER -> {
                                    SpinnerLauncherWheel(
                                        apps = allVisibleApps.ifEmpty { homeApps },
                                        spinnerStyle = settings.spinnerStyle,
                                        iconProvider = iconProvider,
                                        showLabels = settings.showIconLabels,
                                        themedIconMode = settings.themeAppIcons,
                                        badgeCounts = if (notificationBadgesEnabled) badgeCounts else emptyMap(),
                                        autoRollingEnabled = settings.autoRollingEnabled,
                                        rollingSpeed = settings.rollingSpeed,
                                        depth3DIntensity = settings.depth3DIntensity,
                                        hapticFeedbackEnabled = settings.hapticFeedbackEnabled,
                                        onToggleAutoRolling = {
                                            if (isPremiumUnlocked) onToggleAutoRolling() else onUnlockPremium()
                                        },
                                        onAppClick = { app ->
                                            if (isPremiumUnlocked) onLaunchApp(app) else onUnlockPremium()
                                        },
                                        onAppLongClick = { app ->
                                            if (isPremiumUnlocked) selectedAppForMenu = app else onUnlockPremium()
                                        }
                                    )
                                }
                                LauncherMode.GRID -> {
                                    val cols = settings.gridColumns
                                    val previewApps = homeApps.take(cols * 2)
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        previewApps.chunked(cols).forEach { rowApps ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceEvenly
                                            ) {
                                                rowApps.forEach { app ->
                                                    AppIconItem(
                                                        app = app,
                                                        iconDrawable = iconProvider(app.packageName),
                                                        iconSize = settings.iconSizeDp.dp,
                                                        showLabel = settings.showIconLabels,
                                                        themedIconMode = settings.themeAppIcons,
                                                        badgeCount = if (notificationBadgesEnabled) badgeCounts[app.packageName] ?: 0 else 0,
                                                        onClick = {
                                                            if (isPremiumUnlocked) onLaunchApp(app) else onUnlockPremium()
                                                        },
                                                        onLongClick = {
                                                            if (isPremiumUnlocked) selectedAppForMenu = app else onUnlockPremium()
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                LauncherMode.MINIMAL -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        homeApps.take(5).forEach { app ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(tokens.elevatedSurface)
                                                    .clickable {
                                                        if (isPremiumUnlocked) onLaunchApp(app) else onUnlockPremium()
                                                    }
                                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = app.label,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                Text(
                                                    text = if (isPremiumUnlocked) "Open" else "Paid",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = tokens.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (!isPremiumUnlocked) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xA6060914))
                                    .clickable(onClick = onUnlockPremium)
                                    .padding(20.dp)
                                    .testTag("launcher_preview_lock_overlay"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = "Locked",
                                        tint = Color(0xFFD4AF37),
                                        modifier = Modifier.size(34.dp)
                                    )
                                    Text(
                                        text = "3D Launcher Locked",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Tap to unlock Paid Pro Plan",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Gesture Settings Panel + Full Gesture Studio Button
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GestureSettingsPanel(
                        settings = settings,
                        onSetSwipeDown = onSetSwipeDown,
                        onSetSwipeUp = onSetSwipeUp,
                        onSetDoubleTap = onSetDoubleTap,
                        onSetLongPress = onSetLongPress,
                        onSetPinch = onSetPinch
                    )
                    OutlinedButton(
                        onClick = onOpenGestureConfig,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("launcher_open_gesture_config")
                    ) {
                        Text("Open Full Gesture & Navigation Lab")
                    }
                }
            }

            // Full-Ecosystem Theme Controls
            item {
                GlassCard(
                    modifier = Modifier.testTag("ecosystem_theme_controls"),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.Palette, contentDescription = null, tint = tokens.primary)
                            Text("Theme Engine", style = MaterialTheme.typography.titleMedium)
                        }
                        if (!isPremiumUnlocked) {
                            ProBadge(label = "PAID")
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Theme App Icons", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = settings.themeAppIcons,
                            onCheckedChange = { enabled ->
                                if (isPremiumUnlocked) onSetThemeAppIcons(enabled) else onUnlockPremium()
                            },
                            modifier = Modifier.testTag("switch_theme_app_icons")
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Theme Widgets", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = settings.themeWidgets,
                            onCheckedChange = { enabled ->
                                if (isPremiumUnlocked) onSetThemeWidgets(enabled) else onUnlockPremium()
                            },
                            modifier = Modifier.testTag("switch_theme_widgets")
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sync Theme Wallpaper", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = settings.syncThemeWallpaper,
                            onCheckedChange = { enabled ->
                                if (isPremiumUnlocked) onSetSyncThemeWallpaper(enabled) else onUnlockPremium()
                            },
                            modifier = Modifier.testTag("switch_sync_theme_wallpaper")
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Liquid Glass Refraction", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = settings.liquidGlassEnabled,
                            onCheckedChange = { enabled ->
                                if (isPremiumUnlocked) onSetLiquidGlass(enabled) else onUnlockPremium()
                            },
                            modifier = Modifier.testTag("switch_liquid_glass")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Icon Pack Shape",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = tokens.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = settings.iconStyleOverride == null,
                                onClick = {
                                    if (isPremiumUnlocked) onSelectIconStyle(null) else onUnlockPremium()
                                },
                                label = { Text("Theme Default") }
                            )
                        }
                        items(ThemeIconStyle.entries) { iconStyle ->
                            FilterChip(
                                selected = settings.iconStyleOverride == iconStyle,
                                onClick = {
                                    if (isPremiumUnlocked) onSelectIconStyle(iconStyle) else onUnlockPremium()
                                },
                                label = { Text(iconStyle.displayName) },
                                modifier = Modifier.testTag("icon_pack_${iconStyle.name}")
                            )
                        }
                    }
                }
            }

            // Mode Selector
            item {
                GlassCard(contentPadding = PaddingValues(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Layout Mode", style = MaterialTheme.typography.titleMedium)
                        if (!isPremiumUnlocked) ProBadge(label = "PAID")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LauncherMode.entries.forEach { mode ->
                            FilterChip(
                                selected = settings.launcherMode == mode,
                                onClick = {
                                    if (isPremiumUnlocked) onSelectMode(mode) else onUnlockPremium()
                                },
                                label = { Text(mode.displayName) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("launcher_mode_${mode.name}")
                            )
                        }
                    }
                }
            }

            // 3D Orbital Styles
            item {
                GlassCard(contentPadding = PaddingValues(16.dp)) {
                    Text("3D Orbit Styles", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(SpinnerStyle.entries) { style ->
                            val locked = !isPremiumUnlocked
                            val selected = settings.spinnerStyle == style
                            Column(
                                modifier = Modifier
                                    .width(156.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (selected) tokens.primary.copy(alpha = 0.16f)
                                        else tokens.elevatedSurface
                                    )
                                    .border(
                                        width = if (selected) 1.5.dp else 1.dp,
                                        color = if (selected) tokens.primary else tokens.borderSubtle,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        if (locked) onUnlockPremium() else onSelectSpinnerStyle(style)
                                    }
                                    .padding(12.dp)
                                    .testTag("spinner_style_${style.name}")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = style.displayName,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (locked) {
                                        Icon(
                                            imageVector = Icons.Rounded.Lock,
                                            contentDescription = "Pro locked",
                                            tint = Color(0xFFD4AF37),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = style.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                ProBadge(
                                    label = when {
                                        style == SpinnerStyle.PRO_3D_SPHERE -> "SPHERE PRO"
                                        style.isPremium -> "PRO"
                                        else -> "PAID"
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Grid & Icon Geometry
            item {
                GlassCard(contentPadding = PaddingValues(16.dp)) {
                    Text("Grid & Icons", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(10.dp))

                    val maxCols = 6
                    Text("Columns: ${settings.gridColumns}", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = settings.gridColumns.toFloat(),
                        onValueChange = { cols ->
                            if (!isPremiumUnlocked) {
                                onUnlockPremium()
                            } else {
                                val target = cols.toInt().coerceIn(3, maxCols)
                                onSetGridSize(target, settings.gridRows)
                            }
                        },
                        valueRange = 3f..maxCols.toFloat(),
                        steps = (maxCols - 4).coerceAtLeast(0)
                    )

                    Text("Rows: ${settings.gridRows}", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = settings.gridRows.toFloat(),
                        onValueChange = { rows ->
                            if (!isPremiumUnlocked) {
                                onUnlockPremium()
                            } else {
                                onSetGridSize(settings.gridColumns, rows.toInt())
                            }
                        },
                        valueRange = 4f..7f,
                        steps = 2
                    )

                    Text("Icon Size: ${settings.iconSizeDp} dp", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = settings.iconSizeDp.toFloat(),
                        onValueChange = { size ->
                            if (!isPremiumUnlocked) onUnlockPremium() else onSetIconSize(size.toInt())
                        },
                        valueRange = 44f..72f
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show App Labels", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = settings.showIconLabels,
                            onCheckedChange = { show ->
                                if (!isPremiumUnlocked) onUnlockPremium() else onSetShowLabels(show)
                            }
                        )
                    }
                }
            }

            // Hidden Apps
            item {
                val hiddenUniqueCount = remember(hiddenApps) { hiddenApps.distinctBy { it.packageName }.size }
                GlassCard(
                    onClick = { if (isPremiumUnlocked) showHiddenDialog = true else onUnlockPremium() },
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.testTag("launcher_manage_hidden_apps")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Rounded.VisibilityOff, contentDescription = null, tint = tokens.primary)
                            Text("Hidden Apps ($hiddenUniqueCount)", style = MaterialTheme.typography.titleMedium)
                        }
                        Text(
                            text = if (isPremiumUnlocked) "Manage" else "Pro Only",
                            style = MaterialTheme.typography.labelLarge,
                            color = tokens.primary
                        )
                    }
                }
            }
        }
    }

    if (showHiddenDialog) {
        HiddenAppsVaultDialog(
            allApps = allApps,
            onToggleHiddenApp = onToggleHiddenApp,
            onDismiss = { showHiddenDialog = false }
        )
    }

    selectedAppForMenu?.let { app ->
        ModalBottomSheet(onDismissRequest = { selectedAppForMenu = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
                ListItem(
                    headlineContent = { Text(if (app.isPinnedHome) "Unpin from Home" else "Pin to Home") },
                    leadingContent = { Icon(Icons.Rounded.PushPin, contentDescription = null) },
                    modifier = Modifier.clickable {
                        if (app.isPinnedHome) onUnpinAppFromHome(app) else onPinAppToHome(app)
                        selectedAppForMenu = null
                    }
                )
                ListItem(
                    headlineContent = { Text(if (app.isFavorite) "Remove Favorite" else "Add Favorite") },
                    leadingContent = {
                        Icon(
                            if (app.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.clickable {
                        onToggleFavoriteApp(app)
                        selectedAppForMenu = null
                    }
                )
                ListItem(
                    headlineContent = { Text("Hide App") },
                    leadingContent = { Icon(Icons.Rounded.VisibilityOff, contentDescription = null) },
                    modifier = Modifier.clickable {
                        onToggleHiddenApp(app)
                        selectedAppForMenu = null
                    }
                )
                ListItem(
                    headlineContent = { Text("App Info") },
                    leadingContent = { Icon(Icons.Rounded.Info, contentDescription = null) },
                    modifier = Modifier.clickable {
                        onOpenAppInfo(app.packageName)
                        selectedAppForMenu = null
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
