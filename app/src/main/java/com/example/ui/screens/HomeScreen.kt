package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import com.example.data.model.BillingState
import com.example.data.model.LauncherSettingsState
import com.example.data.model.NavigationMode
import com.example.data.model.ThemeConfig
import com.example.data.model.WallpaperItem
import com.example.data.wallpaper.LiveWeatherSnapshot
import com.example.data.wallpaper.WallpaperRenderer
import com.example.ui.components.AdBannerSlot
import com.example.ui.components.GlassCard
import com.example.ui.components.ProBadge
import com.example.ui.components.ProFeatureVideoShowcase
import com.example.ui.components.SectionHeader
import com.example.ui.components.ThemePreviewCard
import com.example.ui.components.WallpaperCard
import com.example.ui.components.WallpaperThumbnailImage
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LocalLauncherTokens
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.RoyalPurple
import java.util.Calendar

@Composable
fun HomeScreen(
    activeTheme: ThemeConfig,
    activeWallpaper: WallpaperItem?,
    settings: LauncherSettingsState,
    billingState: BillingState,
    featuredFreeWallpapers: List<WallpaperItem>,
    featuredPremiumWallpapers: List<WallpaperItem>,
    featuredFreeThemes: List<ThemeConfig>,
    featuredPremiumThemes: List<ThemeConfig>,
    renderer: WallpaperRenderer,
    announcementBanner: String = "",
    weatherSnapshot: LiveWeatherSnapshot? = null,
    onDismissAnnouncement: (String) -> Unit = {},
    onOpenSearch: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenAccount: () -> Unit = {},
    onOpenLauncherHub: () -> Unit,
    onOpenGestureConfig: () -> Unit = {},
    onToggleNavigationMode: () -> Unit = {},
    onOpenLiveHome: () -> Unit,
    onOpenWallpapers: () -> Unit,
    onOpenThemes: () -> Unit,
    onOpenPremium: () -> Unit,
    onSetLiquidGlass: (Boolean) -> Unit = {},
    onWallpaperClick: (WallpaperItem) -> Unit,
    onToggleWallpaperFav: (WallpaperItem) -> Unit,
    onThemeClick: (ThemeConfig) -> Unit,
    onToggleThemeFav: (ThemeConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalLauncherTokens.current
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Late night studio"
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .testTag("home_screen_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Flagship Crystal Studio Masthead
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (billingState.isPremium) PremiumGold.copy(alpha = 0.16f)
                                        else tokens.primary.copy(alpha = 0.14f)
                                    )
                                    .border(
                                        0.8.dp,
                                        if (billingState.isPremium) PremiumGold.copy(alpha = 0.45f)
                                        else tokens.primary.copy(alpha = 0.35f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (billingState.isPremium) PremiumGold else EmeraldSuccess)
                                )
                                Text(
                                    text = "$greeting • ${if (billingState.isPremium) "PRO STUDIO" else "3D OS STUDIO"}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (billingState.isPremium) PremiumGold else tokens.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Text(
                            text = "Android 3D Rolling launcher OS",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MastheadIconCircle(
                            icon = Icons.Rounded.Search,
                            contentDescription = "Search wallpapers, themes, and apps",
                            tint = MaterialTheme.colorScheme.onSurface,
                            onClick = onOpenSearch,
                            testTag = "home_search_button"
                        )
                        MastheadIconCircle(
                            icon = Icons.Rounded.Favorite,
                            contentDescription = "Open Favorites",
                            tint = Color(0xFFF43F5E),
                            onClick = onOpenFavorites,
                            testTag = "home_favorites_button"
                        )
                        MastheadIconCircle(
                            icon = Icons.Rounded.Person,
                            contentDescription = "Open Account & Cloud Vault",
                            tint = tokens.primary,
                            onClick = onOpenAccount,
                            testTag = "home_account_button"
                        )
                    }
                }
            }

            // Optional Remote Announcement Banner from Admin Panel
            if (announcementBanner.isNotBlank()) {
                item {
                    GlassCard(
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("home_announcement_banner")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Campaign,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = announcementBanner,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onDismissAnnouncement(announcementBanner) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Dismiss announcement",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Hero Active Setup Showcase Card (Liquid Glass Frame)
            item {
                GlassCard(
                    cornerRadius = 26.dp,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Phone Mockup Frame
                        Box(
                            modifier = Modifier
                                .size(width = 98.dp, height = 158.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .border(
                                    width = 1.6.dp,
                                    brush = Brush.verticalGradient(
                                        listOf(tokens.primary, ElectricCyan, tokens.secondary)
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    activeWallpaper?.let(onWallpaperClick) ?: onOpenWallpapers()
                                }
                        ) {
                            if (activeWallpaper != null) {
                                WallpaperThumbnailImage(
                                    item = activeWallpaper,
                                    renderer = renderer,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                listOf(RoyalPurple.copy(alpha = 0.30f), ElectricCyan.copy(alpha = 0.25f))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Wallpaper,
                                        contentDescription = null,
                                        tint = tokens.primary
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(6.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xB8060914))
                                    .border(0.7.dp, Color.White.copy(alpha = 0.30f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = settings.spinnerStyle.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ACTIVE 3D SETUP",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = tokens.primary
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(tokens.primary))
                                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(tokens.secondary))
                                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(tokens.accent))
                                }
                            }

                            Text(
                                text = activeTheme.name,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = activeWallpaper?.name ?: "Dynamic Orbit Horizon",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SetupStatPill(text = settings.launcherMode.displayName)
                                SetupStatPill(text = "${settings.gridColumns}×${settings.gridRows}")
                                if (weatherSnapshot != null) {
                                    SetupStatPill(text = weatherSnapshot.temperatureLabel)
                                } else {
                                    SetupStatPill(
                                        text = if (settings.liquidGlassEnabled) "Glass" else "Matte"
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onOpenLiveHome,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("home_open_live_launcher"),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (billingState.isPremium) Icons.Rounded.Home else Icons.Rounded.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (billingState.isPremium) "Launch Home" else "Unlock Home",
                                        maxLines = 1,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                FilledTonalButton(
                                    onClick = onOpenLauncherHub,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("home_customize_launcher"),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Tune,
                                        contentDescription = "Customize launcher",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Liquid Glass Studio Quick Surface Card
            item {
                GlassCard(
                    onClick = { onSetLiquidGlass(!settings.liquidGlassEnabled) },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    modifier = Modifier.testTag("home_liquid_glass_toggle")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(ElectricCyan.copy(alpha = 0.25f), RoyalPurple.copy(alpha = 0.22f))
                                        )
                                    )
                                    .border(0.9.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.BlurOn,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Liquid Glass UI",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (settings.liquidGlassEnabled) "Translucent crystal frost active" else "Classic solid surfaces active",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Switch(
                            checked = settings.liquidGlassEnabled,
                            onCheckedChange = onSetLiquidGlass
                        )
                    }
                }
            }

            // 4. Home Gestures & App Navigation Control Strip
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassCard(
                        onClick = onOpenGestureConfig,
                        contentPadding = PaddingValues(14.dp),
                        modifier = Modifier
                            .weight(1.15f)
                            .testTag("home_open_gestures")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(tokens.primary.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Gesture,
                                    contentDescription = null,
                                    tint = tokens.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Configure",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = tokens.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Home Gestures",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "↑ ${settings.swipeUpAction.displayName} • ↓ ${settings.swipeDownAction.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "2× Tap: ${settings.doubleTapAction.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    GlassCard(
                        onClick = onToggleNavigationMode,
                        contentPadding = PaddingValues(14.dp),
                        modifier = Modifier
                            .weight(0.85f)
                            .testTag("home_toggle_navigation_mode")
                    ) {
                        val isGestureNav = settings.navigationMode == NavigationMode.GESTURE
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(tokens.secondary.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isGestureNav) Icons.Rounded.Swipe else Icons.Rounded.ViewAgenda,
                                    contentDescription = null,
                                    tint = tokens.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Switch",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = tokens.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Navigation",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (isGestureNav) "Gesture Bar" else "Button Bar",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = tokens.primary,
                            maxLines = 1
                        )
                        Text(
                            text = if (isGestureNav) "Tap for Buttons" else "Tap for Gestures",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 5. Quick Access Studio Bento Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickStudioTile(
                        title = "3D Hub",
                        subtitle = if (billingState.isPremium) settings.spinnerStyle.displayName else "Pro Required",
                        icon = Icons.Rounded.RocketLaunch,
                        accent = tokens.primary,
                        onClick = onOpenLauncherHub,
                        modifier = Modifier.weight(1f)
                    )
                    QuickStudioTile(
                        title = "Wallpapers",
                        subtitle = if (billingState.isPremium) "AMOLED + 3D" else "Pro Only",
                        icon = Icons.Rounded.Wallpaper,
                        accent = ElectricCyan,
                        onClick = onOpenWallpapers,
                        modifier = Modifier.weight(1f)
                    )
                    QuickStudioTile(
                        title = "Themes",
                        subtitle = if (billingState.isPremium) "Icons & UI" else "Pro Only",
                        icon = Icons.Rounded.Palette,
                        accent = RoyalPurple,
                        onClick = onOpenThemes,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 6. Pro Exclusives Upgrade Banner (featuring Pro 3D Sphere Launcher)
            if (!billingState.isPremium) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(tokens.cardRadius))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF0E1226),
                                        Color(0xFF23163D),
                                        Color(0xFF281F0C)
                                    )
                                )
                            )
                            .border(
                                1.2.dp,
                                Brush.linearGradient(listOf(PremiumGold, ElectricCyan, RoyalPurple)),
                                RoundedCornerShape(tokens.cardRadius)
                            )
                            .clickable(onClick = onOpenPremium)
                            .padding(18.dp)
                            .testTag("home_upgrade_premium_banner")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                ProBadge(label = "3D PRO SPHERE")
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Unlock 3D Pro Sphere & Halo",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFFAF5E8)
                                )
                                Text(
                                    text = "Full 360° tornado app globe, 60+ Pro packs & zero ads",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFD6CBB0)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = onOpenPremium,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PremiumGold,
                                    contentColor = Color(0xFF141006)
                                ),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pro", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 6b. Interactive Pro Plan Features Video Tour
            item {
                ProFeatureVideoShowcase(
                    isPremium = billingState.isPremium,
                    reduceMotion = settings.reduceMotion,
                    onActionClick = onOpenPremium,
                    actionLabel = if (billingState.isPremium) "Open Pro Studio" else "See Pro Plans"
                )
            }

            // 7. Featured Studio Wallpapers
            item {
                SectionHeader(
                    title = "Studio Wallpapers",
                    actionLabel = if (billingState.isPremium) "See All" else "Pro Only",
                    onActionClick = onOpenWallpapers
                )
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(featuredFreeWallpapers, key = { it.id }) { item ->
                        WallpaperCard(
                            item = item,
                            renderer = renderer,
                            isPremiumUnlocked = billingState.isPremium,
                            onClick = { onWallpaperClick(item) },
                            onFavoriteClick = { onToggleWallpaperFav(item) },
                            modifier = Modifier.width(148.dp)
                        )
                    }
                }
            }

            // 8. Featured Studio Themes
            item {
                SectionHeader(
                    title = "Studio Themes",
                    actionLabel = if (billingState.isPremium) "Browse 50" else "Pro Only",
                    onActionClick = onOpenThemes
                )
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(featuredFreeThemes, key = { it.id }) { theme ->
                        ThemePreviewCard(
                            theme = theme,
                            isActive = theme.id == activeTheme.id,
                            isPremiumUnlocked = billingState.isPremium,
                            onClick = { onThemeClick(theme) },
                            onFavoriteClick = { onToggleThemeFav(theme) },
                            modifier = Modifier.width(210.dp)
                        )
                    }
                }
            }

            // 9. Featured Pro Wallpapers & Themes
            item {
                SectionHeader(
                    title = "Pro Exclusives",
                    actionLabel = if (billingState.isPremium) "Explore" else "Unlock",
                    onActionClick = onOpenPremium
                )
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(featuredPremiumWallpapers, key = { it.id }) { item ->
                        WallpaperCard(
                            item = item,
                            renderer = renderer,
                            isPremiumUnlocked = billingState.isPremium,
                            onClick = { onWallpaperClick(item) },
                            onFavoriteClick = { onToggleWallpaperFav(item) },
                            modifier = Modifier.width(148.dp)
                        )
                    }
                }
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(featuredPremiumThemes, key = { it.id }) { theme ->
                        ThemePreviewCard(
                            theme = theme,
                            isActive = theme.id == activeTheme.id,
                            isPremiumUnlocked = billingState.isPremium,
                            onClick = { onThemeClick(theme) },
                            onFavoriteClick = { onToggleThemeFav(theme) },
                            modifier = Modifier.width(210.dp)
                        )
                    }
                }
            }

            item {
                AdBannerSlot(isPremium = billingState.isPremium)
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun MastheadIconCircle(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val tokens = LocalLauncherTokens.current
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(tokens.surface.copy(alpha = if (tokens.liquidGlassEnabled) 0.65f else 0.88f))
            .border(
                width = 1.dp,
                color = if (tokens.liquidGlassEnabled) Color.White.copy(alpha = if (tokens.isDark) 0.28f else 0.72f)
                else tokens.borderSubtle,
                shape = CircleShape
            )
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}

@Composable
private fun SetupStatPill(text: String) {
    val tokens = LocalLauncherTokens.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(tokens.elevatedSurface.copy(alpha = if (tokens.liquidGlassEnabled) 0.68f else 1f))
            .border(0.8.dp, tokens.borderSubtle, RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun QuickStudioTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        contentPadding = PaddingValues(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(accent.copy(alpha = 0.16f))
                .border(0.8.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
