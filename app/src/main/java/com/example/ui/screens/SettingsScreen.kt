package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.data.model.AppearanceMode
import com.example.data.model.BillingState
import com.example.data.model.InstalledAppInfo
import com.example.data.model.LauncherBackgroundPreset
import com.example.data.model.LauncherSettingsState
import com.example.data.model.NavigationMode
import com.example.ui.components.GlassCard
import com.example.ui.components.HiddenAppsVaultDialog
import com.example.ui.components.ProBadge
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LocalLauncherTokens
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.RoyalPurple
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    settings: LauncherSettingsState,
    billingState: BillingState,
    cacheSizeLabel: String,
    accountLabel: String? = null,
    allApps: List<InstalledAppInfo> = emptyList(),
    hiddenApps: List<InstalledAppInfo> = emptyList(),
    onSelectAppearance: (AppearanceMode) -> Unit,
    onSelectNavigationMode: (NavigationMode) -> Unit = {},
    onSetLiquidGlass: (Boolean) -> Unit = {},
    onToggleHiddenApp: (InstalledAppInfo) -> Unit = {},
    onSetReduceMotion: (Boolean) -> Unit,
    onSetAutoCache: (Boolean) -> Unit,
    onSetWifiOnly: (Boolean) -> Unit,
    onSetAutoRolling: (Boolean) -> Unit = {},
    onSetRollingSpeed: (Float) -> Unit = {},
    onSetDepth3D: (Float) -> Unit = {},
    onSetIconSize: (Int) -> Unit = {},
    onSelectBackgroundPreset: (LauncherBackgroundPreset) -> Unit = {},
    onSetHapticFeedback: (Boolean) -> Unit = {},
    onOpenAccount: () -> Unit = {},
    onOpenLauncherHub: () -> Unit,
    onOpenGestureConfig: () -> Unit = {},
    onOpenFavorites: () -> Unit,
    onOpenPremium: () -> Unit,
    onRestorePurchases: () -> Unit,
    onClearCache: (onDone: () -> Unit) -> Unit,
    onResetCustomization: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenTerms: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tokens = LocalLauncherTokens.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var currentCache by remember(cacheSizeLabel) { mutableStateOf(cacheSizeLabel) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showHiddenVaultDialog by remember { mutableStateOf(false) }
    val hiddenUniqueCount = remember(hiddenApps) { hiddenApps.distinctBy { it.packageName }.size }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 800.dp)
                .testTag("settings_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Studio Settings Masthead Card
            item {
                GlassCard(
                    cornerRadius = 24.dp,
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Studio Settings",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${settings.spinnerStyle.displayName} • ${if (settings.liquidGlassEnabled) "Liquid Glass" else "Classic"}",
                                style = MaterialTheme.typography.labelMedium,
                                color = tokens.primary
                            )
                        }
                        if (billingState.isPremium) {
                            ProBadge(label = "PRO ACTIVE")
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(tokens.primary.copy(alpha = 0.14f))
                                    .clickable(onClick = onOpenPremium)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Go Pro",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = tokens.primary
                                )
                            }
                        }
                    }
                }
            }

            // 2. Appearance, Liquid Glass & Backdrop
            item {
                SectionEyebrow("SURFACE & APPEARANCE")
                Spacer(modifier = Modifier.height(6.dp))
                GlassCard(contentPadding = PaddingValues(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            SettingsTintedIcon(Icons.Rounded.BlurOn, ElectricCyan)
                            Column {
                                Text(
                                    text = "Liquid Glass Refraction",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "Crystal frosted cards & prism borders",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = settings.liquidGlassEnabled,
                            onCheckedChange = onSetLiquidGlass,
                            modifier = Modifier.testTag("settings_liquid_glass_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Appearance Mode",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppearanceMode.entries.forEach { mode ->
                            FilterChip(
                                selected = settings.appearanceMode == mode,
                                onClick = { onSelectAppearance(mode) },
                                label = { Text(mode.displayName) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("appearance_${mode.name}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "App Navigation Style",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NavigationMode.entries.forEach { navMode ->
                            FilterChip(
                                selected = settings.navigationMode == navMode,
                                onClick = { onSelectNavigationMode(navMode) },
                                label = { Text(if (navMode == NavigationMode.GESTURE) "Gesture Bar" else "Button Bar") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("settings_nav_mode_${navMode.name}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Background Lighting",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(LauncherBackgroundPreset.entries) { preset ->
                            FilterChip(
                                selected = settings.backgroundPreset == preset,
                                onClick = { onSelectBackgroundPreset(preset) },
                                label = { Text(preset.displayName) },
                                modifier = Modifier.testTag("settings_bg_${preset.name}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Animation Effects", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = !settings.reduceMotion,
                            onCheckedChange = { enabled -> onSetReduceMotion(!enabled) },
                            modifier = Modifier.testTag("settings_animations_switch")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Haptic Feedback", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = settings.hapticFeedbackEnabled,
                            onCheckedChange = onSetHapticFeedback,
                            modifier = Modifier.testTag("settings_haptic_switch")
                        )
                    }
                }
            }

            // 3. 3D Rolling Engine Controls
            item {
                SectionEyebrow("3D ROLLING & SPHERE ENGINE")
                Spacer(modifier = Modifier.height(6.dp))
                GlassCard(contentPadding = PaddingValues(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rolling Animation",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                        )
                        Switch(
                            checked = settings.autoRollingEnabled,
                            onCheckedChange = onSetAutoRolling,
                            modifier = Modifier.testTag("settings_auto_roll_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    SliderHeaderRow("Rotation Speed", String.format(Locale.US, "%.1fx", settings.rollingSpeed))
                    Slider(
                        value = settings.rollingSpeed,
                        onValueChange = onSetRollingSpeed,
                        valueRange = 0.2f..2.5f,
                        modifier = Modifier.testTag("settings_speed_slider")
                    )

                    SliderHeaderRow("3D Depth", "${(settings.depth3DIntensity * 100f).roundToInt()}%")
                    Slider(
                        value = settings.depth3DIntensity,
                        onValueChange = onSetDepth3D,
                        valueRange = 0.0f..1.0f,
                        modifier = Modifier.testTag("settings_depth_slider")
                    )

                    SliderHeaderRow("Icon Size", "${settings.iconSizeDp} dp")
                    Slider(
                        value = settings.iconSizeDp.toFloat(),
                        onValueChange = { onSetIconSize(it.toInt()) },
                        valueRange = 44f..72f,
                        modifier = Modifier.testTag("settings_icon_size_slider")
                    )
                }
            }

            // 4. Gestures, Hidden Apps Vault & Storage
            item {
                SectionEyebrow("PRIVACY, GESTURES & STORAGE")
                Spacer(modifier = Modifier.height(6.dp))
                GlassCard(contentPadding = PaddingValues(4.dp)) {
                    val transparentItemColors = ListItemDefaults.colors(containerColor = Color.Transparent)

                    ListItem(
                        colors = transparentItemColors,
                        headlineContent = { Text("Hidden Apps Vault ($hiddenUniqueCount)") },
                        supportingContent = { Text("Hide apps across 3D Wheel, Grid, Dock, Drawer & Search") },
                        leadingContent = { SettingsTintedIcon(Icons.Rounded.VisibilityOff, RoyalPurple) },
                        modifier = Modifier
                            .clickable { showHiddenVaultDialog = true }
                            .testTag("settings_hidden_apps_item")
                    )
                    HorizontalDivider(color = tokens.borderSubtle.copy(alpha = 0.5f))
                    ListItem(
                        colors = transparentItemColors,
                        headlineContent = { Text("Home Gestures & Navigation") },
                        supportingContent = {
                            Text("↑ ${settings.swipeUpAction.displayName} • ↓ ${settings.swipeDownAction.displayName} • 2× ${settings.doubleTapAction.displayName}")
                        },
                        leadingContent = { SettingsTintedIcon(Icons.Rounded.Tune, ElectricCyan) },
                        modifier = Modifier
                            .clickable(onClick = onOpenGestureConfig)
                            .testTag("settings_open_gesture_config")
                    )
                    HorizontalDivider(color = tokens.borderSubtle.copy(alpha = 0.5f))
                    ListItem(
                        colors = transparentItemColors,
                        headlineContent = { Text("3D Launcher Hub") },
                        supportingContent = {
                            Text("${settings.launcherMode.displayName} • ${settings.spinnerStyle.displayName}")
                        },
                        leadingContent = { SettingsTintedIcon(Icons.Rounded.AutoAwesome, tokens.primary) },
                        modifier = Modifier.clickable(onClick = onOpenLauncherHub)
                    )
                    HorizontalDivider(color = tokens.borderSubtle.copy(alpha = 0.5f))
                    ListItem(
                        colors = transparentItemColors,
                        headlineContent = { Text("Auto-Cache Thumbnails") },
                        trailingContent = {
                            Switch(
                                checked = settings.autoCacheThumbnails,
                                onCheckedChange = onSetAutoCache
                            )
                        }
                    )
                    HorizontalDivider(color = tokens.borderSubtle.copy(alpha = 0.5f))
                    ListItem(
                        colors = transparentItemColors,
                        headlineContent = { Text("Wi-Fi Only Downloads") },
                        trailingContent = {
                            Switch(
                                checked = settings.wifiOnlyDownloads,
                                onCheckedChange = onSetWifiOnly
                            )
                        }
                    )
                    HorizontalDivider(color = tokens.borderSubtle.copy(alpha = 0.5f))
                    ListItem(
                        colors = transparentItemColors,
                        headlineContent = { Text("Clear Wallpaper Cache") },
                        supportingContent = { Text(currentCache) },
                        leadingContent = { SettingsTintedIcon(Icons.Rounded.CleaningServices, EmeraldSuccess) },
                        modifier = Modifier
                            .clickable {
                                onClearCache {
                                    currentCache = "0.00 MB"
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Cache cleared")
                                    }
                                }
                            }
                            .testTag("settings_clear_cache")
                    )
                }
            }

            // 5. Account, Favorites & Membership
            item {
                SectionEyebrow("ACCOUNT & MEMBERSHIP")
                Spacer(modifier = Modifier.height(6.dp))
                GlassCard(contentPadding = PaddingValues(4.dp)) {
                    val transparentItemColors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    ListItem(
                        colors = transparentItemColors,
                        headlineContent = { Text("Account & Cloud Vault") },
                        supportingContent = { Text(accountLabel ?: "Cloud sync & receipts") },
                        leadingContent = { SettingsTintedIcon(Icons.Rounded.Person, tokens.primary) },
                        modifier = Modifier
                            .clickable(onClick = onOpenAccount)
                            .testTag("settings_account_item")
                    )
                    HorizontalDivider(color = tokens.borderSubtle.copy(alpha = 0.5f))
                    ListItem(
                        colors = transparentItemColors,
                        headlineContent = { Text("Favorites") },
                        leadingContent = { SettingsTintedIcon(Icons.Rounded.Favorite, Color(0xFFF43F5E)) },
                        modifier = Modifier.clickable(onClick = onOpenFavorites)
                    )
                    HorizontalDivider(color = tokens.borderSubtle.copy(alpha = 0.5f))
                    ListItem(
                        colors = transparentItemColors,
                        headlineContent = { Text("Pro Plan") },
                        supportingContent = {
                            Text(if (billingState.isPremium) billingState.purchaseSource.label else "Upgrade for 3D Pro Sphere & zero ads")
                        },
                        leadingContent = { SettingsTintedIcon(Icons.Rounded.AutoAwesome, PremiumGold) },
                        modifier = Modifier.clickable(onClick = onOpenPremium)
                    )
                    HorizontalDivider(color = tokens.borderSubtle.copy(alpha = 0.5f))
                    ListItem(
                        colors = transparentItemColors,
                        headlineContent = { Text("Restore Purchases") },
                        leadingContent = { SettingsTintedIcon(Icons.Rounded.Restore, ElectricCyan) },
                        modifier = Modifier.clickable {
                            onRestorePurchases()
                            scope.launch {
                                snackbarHostState.showSnackbar("Checking saved purchases…")
                            }
                        }
                    )
                    HorizontalDivider(color = tokens.borderSubtle.copy(alpha = 0.5f))
                    ListItem(
                        colors = transparentItemColors,
                        headlineContent = { Text("Reset Settings") },
                        leadingContent = { SettingsTintedIcon(Icons.Rounded.RestartAlt, Color(0xFFF59E0B)) },
                        modifier = Modifier
                            .clickable { showResetDialog = true }
                            .testTag("settings_reset_customization")
                    )
                }
            }

            // 6. Legal, Support & About
            item {
                SectionEyebrow("SUPPORT & ABOUT")
                Spacer(modifier = Modifier.height(6.dp))
                GlassCard(contentPadding = PaddingValues(4.dp)) {
                    val transparentItemColors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    ListItem(
                        colors = transparentItemColors,
                        headlineContent = { Text("Privacy Policy") },
                        leadingContent = { SettingsTintedIcon(Icons.Rounded.Policy, tokens.primary) },
                        modifier = Modifier.clickable(onClick = onOpenPrivacy)
                    )
                    HorizontalDivider(color = tokens.borderSubtle.copy(alpha = 0.5f))
                    ListItem(
                        colors = transparentItemColors,
                        headlineContent = { Text("Terms & Conditions") },
                        leadingContent = { SettingsTintedIcon(Icons.Rounded.Description, RoyalPurple) },
                        modifier = Modifier.clickable(onClick = onOpenTerms)
                    )
                    HorizontalDivider(color = tokens.borderSubtle.copy(alpha = 0.5f))
                    ListItem(
                        colors = transparentItemColors,
                        headlineContent = { Text("Support") },
                        supportingContent = { Text("support@androidlauncher.app") },
                        leadingContent = { SettingsTintedIcon(Icons.Rounded.Email, ElectricCyan) },
                        modifier = Modifier.clickable {
                            val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:support@androidlauncher.app")
                                putExtra(Intent.EXTRA_SUBJECT, "Android 3D Rolling launcher OS v${BuildConfig.VERSION_NAME}")
                            }
                            runCatching { context.startActivity(mailIntent) }.onFailure {
                                scope.launch {
                                    snackbarHostState.showSnackbar("support@androidlauncher.app")
                                }
                            }
                        }
                    )
                    HorizontalDivider(color = tokens.borderSubtle.copy(alpha = 0.5f))
                    ListItem(
                        colors = transparentItemColors,
                        headlineContent = { Text("About") },
                        supportingContent = { Text("v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})") },
                        leadingContent = { SettingsTintedIcon(Icons.Rounded.Info, EmeraldSuccess) },
                        modifier = Modifier.clickable { showAboutDialog = true }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showHiddenVaultDialog) {
        HiddenAppsVaultDialog(
            allApps = allApps,
            onToggleHiddenApp = onToggleHiddenApp,
            onDismiss = { showHiddenVaultDialog = false }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Settings?") },
            text = { Text("Restores default theme, Liquid Glass, 3D speed, depth, icon size, and gestures.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onResetCustomization()
                        scope.launch {
                            snackbarHostState.showSnackbar("Settings reset")
                        }
                    }
                ) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Android 3D Rolling launcher OS") },
            text = {
                Text(
                    "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n" +
                        "Package: ${BuildConfig.APPLICATION_ID}"
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun SectionEyebrow(title: String) {
    val tokens = LocalLauncherTokens.current
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = tokens.primary,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun SliderHeaderRow(label: String, valueText: String) {
    val tokens = LocalLauncherTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(tokens.primary.copy(alpha = 0.14f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = tokens.primary
            )
        }
    }
}

@Composable
private fun SettingsTintedIcon(icon: ImageVector, tint: Color) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(tint.copy(alpha = 0.14f))
            .border(0.7.dp, tint.copy(alpha = 0.30f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}
