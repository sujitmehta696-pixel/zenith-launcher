package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.CatalogManifest
import com.example.data.model.RemoteCategory
import com.example.data.model.WallpaperCategory
import com.example.data.model.WallpaperItem
import com.example.data.wallpaper.WallpaperRenderer
import com.example.ui.components.AdBannerSlot
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GlassCard
import com.example.ui.components.WallpaperCard
import com.example.ui.viewmodel.ContentTierFilter

@Composable
fun WallpapersScreen(
    wallpapers: List<WallpaperItem>,
    totalMatching: Int,
    recentlyViewed: List<WallpaperItem>,
    selectedCategory: WallpaperCategory,
    tierFilter: ContentTierFilter,
    searchQuery: String,
    manifest: CatalogManifest?,
    renderer: WallpaperRenderer,
    isPremium: Boolean = false,
    remoteCategories: List<RemoteCategory> = emptyList(),
    selectedRemoteCategoryName: String? = null,
    onSelectCategory: (WallpaperCategory) -> Unit,
    onSelectRemoteCategory: (String?) -> Unit = {},
    onSelectTier: (ContentTierFilter) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onLoadMore: () -> Unit,
    onWallpaperClick: (WallpaperItem) -> Unit,
    onToggleFavorite: (WallpaperItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 156.dp),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 960.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Wallpapers",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "$totalMatching items • AMOLED & 3D Art",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search AMOLED, alpine, obsidian…") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("wallpapers_search_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ContentTierFilter.entries.forEach { tier ->
                            FilterChip(
                                selected = tierFilter == tier,
                                onClick = { onSelectTier(tier) },
                                label = { Text(tier.label) },
                                modifier = Modifier.testTag("wp_tier_${tier.name}")
                            )
                        }
                    }

                    val customRemoteCategories = remoteCategories.filter { rc ->
                        rc.name.isNotBlank() &&
                            WallpaperCategory.entries.none { it.displayName.equals(rc.name, ignoreCase = true) }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(WallpaperCategory.entries) { category ->
                            FilterChip(
                                selected = selectedRemoteCategoryName == null && selectedCategory == category,
                                onClick = {
                                    onSelectRemoteCategory(null)
                                    onSelectCategory(category)
                                },
                                label = { Text(category.displayName) }
                            )
                        }
                        items(customRemoteCategories, key = { "rc_${it.categoryId}" }) { rc ->
                            val isSelected = selectedRemoteCategoryName.equals(rc.name, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    onSelectRemoteCategory(if (isSelected) null else rc.name)
                                },
                                label = { Text(rc.name) }
                            )
                        }
                    }
                }
            }

            if (recentlyViewed.isNotEmpty() && searchQuery.isBlank() && selectedCategory == WallpaperCategory.ALL && selectedRemoteCategoryName == null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Recently Viewed",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(recentlyViewed, key = { "recent_${it.id}" }) { wp ->
                                WallpaperCard(
                                    item = wp,
                                    renderer = renderer,
                                    onClick = { onWallpaperClick(wp) },
                                    onFavoriteClick = { onToggleFavorite(wp) },
                                    modifier = Modifier.width(120.dp),
                                    aspectRatio = 0.72f,
                                    isPremiumUnlocked = isPremium
                                )
                            }
                        }
                    }
                }
            }

            if (wallpapers.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateView(
                        icon = Icons.Rounded.Wallpaper,
                        title = "No matching wallpapers",
                        subtitle = "Try clearing your search filter or switching category.",
                        actionLabel = "Reset Filters",
                        onAction = {
                            onSelectRemoteCategory(null)
                            onSelectCategory(WallpaperCategory.ALL)
                            onSelectTier(ContentTierFilter.ALL)
                            onSearchQueryChange("")
                        }
                    )
                }
            } else {
                items(wallpapers, key = { it.id }) { wallpaper ->
                    WallpaperCard(
                        item = wallpaper,
                        renderer = renderer,
                        onClick = { onWallpaperClick(wallpaper) },
                        onFavoriteClick = { onToggleFavorite(wallpaper) },
                        isPremiumUnlocked = isPremium
                    )
                }
            }

            if (wallpapers.size < totalMatching) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        OutlinedButton(
                            onClick = onLoadMore,
                            modifier = Modifier.testTag("wallpapers_load_more")
                        ) {
                            Text("Load More (${wallpapers.size} of $totalMatching)")
                        }
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                AdBannerSlot(isPremium = isPremium)
            }
        }
    }
}
