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
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.CatalogManifest
import com.example.data.model.ThemeCategory
import com.example.data.model.ThemeConfig
import com.example.ui.components.AdBannerSlot
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GlassCard
import com.example.ui.components.ThemePreviewCard
import com.example.ui.viewmodel.ContentTierFilter

@Composable
fun ThemesScreen(
    themes: List<ThemeConfig>,
    totalMatching: Int,
    activeThemeId: String,
    selectedCategory: ThemeCategory,
    tierFilter: ContentTierFilter,
    searchQuery: String,
    manifest: CatalogManifest?,
    isPremium: Boolean = false,
    onSelectCategory: (ThemeCategory) -> Unit,
    onSelectTier: (ContentTierFilter) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onLoadMore: () -> Unit,
    onThemeClick: (ThemeConfig) -> Unit,
    onToggleFavorite: (ThemeConfig) -> Unit,
    onResetTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 166.dp),
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Themes",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "$totalMatching themes • Icons, Widgets & UI",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            TextButton(
                                onClick = onResetTheme,
                                modifier = Modifier.testTag("themes_reset_button")
                            ) {
                                Icon(Icons.Rounded.RestartAlt, contentDescription = null)
                                Text("Reset")
                            }
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search AMOLED, minimal, serif, light…") },
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
                            .testTag("themes_search_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ContentTierFilter.entries.forEach { tier ->
                            FilterChip(
                                selected = tierFilter == tier,
                                onClick = { onSelectTier(tier) },
                                label = { Text(tier.label) },
                                modifier = Modifier.testTag("theme_tier_${tier.name}")
                            )
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ThemeCategory.entries) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { onSelectCategory(category) },
                                label = { Text(category.displayName) }
                            )
                        }
                    }
                }
            }

            if (themes.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyStateView(
                        icon = Icons.Rounded.Palette,
                        title = "No matching themes",
                        subtitle = "Try clearing filters or searching another color style.",
                        actionLabel = "Reset Filters",
                        onAction = {
                            onSelectCategory(ThemeCategory.ALL)
                            onSelectTier(ContentTierFilter.ALL)
                            onSearchQueryChange("")
                        }
                    )
                }
            } else {
                items(themes, key = { it.id }) { theme ->
                    ThemePreviewCard(
                        theme = theme,
                        isActive = theme.id == activeThemeId,
                        onClick = { onThemeClick(theme) },
                        onFavoriteClick = { onToggleFavorite(theme) },
                        isPremiumUnlocked = isPremium
                    )
                }
            }

            if (themes.size < totalMatching) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        OutlinedButton(
                            onClick = onLoadMore,
                            modifier = Modifier.testTag("themes_load_more")
                        ) {
                            Text("Load More (${themes.size} of $totalMatching)")
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
