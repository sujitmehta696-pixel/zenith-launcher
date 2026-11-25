package com.example.ui.screens

import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.InstalledAppInfo
import com.example.data.model.ThemeConfig
import com.example.data.model.WallpaperItem
import com.example.data.wallpaper.WallpaperRenderer
import com.example.ui.components.AppIconItem
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ThemePreviewCard
import com.example.ui.components.WallpaperCard
import com.example.ui.viewmodel.FavoritesFilterTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    activeFilter: FavoritesFilterTab,
    favoriteWallpapers: List<WallpaperItem>,
    favoriteThemes: List<ThemeConfig>,
    favoriteApps: List<InstalledAppInfo>,
    activeThemeId: String,
    renderer: WallpaperRenderer,
    iconProvider: (String) -> Drawable?,
    onBack: () -> Unit,
    onFilterChange: (FavoritesFilterTab) -> Unit,
    onWallpaperClick: (WallpaperItem) -> Unit,
    onToggleWallpaperFav: (WallpaperItem) -> Unit,
    onThemeClick: (ThemeConfig) -> Unit,
    onToggleThemeFav: (ThemeConfig) -> Unit,
    onAppClick: (InstalledAppInfo) -> Unit,
    isPremiumUnlocked: Boolean = true,
    themedIconMode: Boolean = true,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Favorites") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("favorites_back")) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FavoritesFilterTab.entries.forEach { tab ->
                            FilterChip(
                                selected = activeFilter == tab,
                                onClick = { onFilterChange(tab) },
                                label = { Text(tab.label) }
                            )
                        }
                    }
                }

                val showApps = (activeFilter == FavoritesFilterTab.ALL || activeFilter == FavoritesFilterTab.APPS) && favoriteApps.isNotEmpty()
                val showWallpapers = (activeFilter == FavoritesFilterTab.ALL || activeFilter == FavoritesFilterTab.WALLPAPERS) && favoriteWallpapers.isNotEmpty()
                val showThemes = (activeFilter == FavoritesFilterTab.ALL || activeFilter == FavoritesFilterTab.THEMES) && favoriteThemes.isNotEmpty()

                if (!showApps && !showWallpapers && !showThemes) {
                    item {
                        EmptyStateView(
                            icon = Icons.Rounded.FavoriteBorder,
                            title = "No favorites saved yet",
                            subtitle = "Tap the heart icon on any wallpaper, theme, or long-press a launcher app to save it here."
                        )
                    }
                }

                if (showApps) {
                    item {
                        Text("Favorite Apps (${favoriteApps.size})", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(favoriteApps, key = { "fav_app_${it.packageName}" }) { app ->
                                AppIconItem(
                                    app = app,
                                    iconDrawable = iconProvider(app.packageName),
                                    iconSize = 54.dp,
                                    showLabel = true,
                                    themedIconMode = themedIconMode,
                                    onClick = { onAppClick(app) },
                                    onLongClick = {}
                                )
                            }
                        }
                    }
                }

                if (showWallpapers) {
                    item {
                        Text("Favorite Wallpapers (${favoriteWallpapers.size})", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(favoriteWallpapers, key = { "fav_wp_${it.id}" }) { wp ->
                                WallpaperCard(
                                    item = wp,
                                    renderer = renderer,
                                    isPremiumUnlocked = isPremiumUnlocked,
                                    onClick = { onWallpaperClick(wp) },
                                    onFavoriteClick = { onToggleWallpaperFav(wp) },
                                    modifier = Modifier.width(148.dp)
                                )
                            }
                        }
                    }
                }

                if (showThemes) {
                    item {
                        Text("Favorite Themes (${favoriteThemes.size})", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(favoriteThemes, key = { "fav_th_${it.id}" }) { theme ->
                                ThemePreviewCard(
                                    theme = theme,
                                    isActive = theme.id == activeThemeId,
                                    isPremiumUnlocked = isPremiumUnlocked,
                                    onClick = { onThemeClick(theme) },
                                    onFavoriteClick = { onToggleThemeFav(theme) },
                                    modifier = Modifier.width(210.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
