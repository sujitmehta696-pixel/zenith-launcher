package com.example.ui.screens

import android.graphics.drawable.Drawable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.data.launcher.PhoneContentItem
import com.example.data.launcher.PhoneContentType
import com.example.data.model.InstalledAppInfo
import com.example.data.model.ThemeConfig
import com.example.data.model.WallpaperItem
import com.example.data.wallpaper.WallpaperRenderer
import com.example.ui.components.AppIconItem
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GlassCard
import com.example.ui.components.ThemePreviewCard
import com.example.ui.components.WallpaperCard
import com.example.ui.theme.ElectricCyan
import com.example.ui.viewmodel.AiSearchUiState
import com.example.ui.viewmodel.SearchFilterTab

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    query: String,
    activeFilter: SearchFilterTab,
    searchHistory: List<String>,
    wallpapers: List<WallpaperItem>,
    themes: List<ThemeConfig>,
    apps: List<InstalledAppInfo>,
    phoneContentMatches: List<PhoneContentItem> = emptyList(),
    aiSearchState: AiSearchUiState = AiSearchUiState.Idle,
    aiSearchEnabled: Boolean = true,
    activeThemeId: String,
    renderer: WallpaperRenderer,
    iconProvider: (String) -> Drawable?,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSubmitQuery: (String) -> Unit,
    onAskAiSearch: (String) -> Unit = {},
    onOpenPhoneContent: (PhoneContentItem) -> Unit = {},
    onClearQuery: () -> Unit,
    onClearHistory: () -> Unit,
    onFilterChange: (SearchFilterTab) -> Unit,
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
                title = { Text("Universal Search") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("search_back")) {
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        placeholder = { Text("Search wallpapers, themes, categories, apps…") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = onClearQuery, modifier = Modifier.testTag("search_clear")) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSubmitQuery(query) }),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("universal_search_input")
                    )
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(SearchFilterTab.entries) { tab ->
                            FilterChip(
                                selected = activeFilter == tab,
                                onClick = { onFilterChange(tab) },
                                label = { Text(tab.label) }
                            )
                        }
                    }
                }

                if (query.isBlank()) {
                    if (searchHistory.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Recent Searches", style = MaterialTheme.typography.titleMedium)
                                TextButton(onClick = onClearHistory) { Text("Clear") }
                            }
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                searchHistory.forEach { term ->
                                    AssistChip(
                                        onClick = { onSubmitQuery(term) },
                                        leadingIcon = {
                                            Icon(Icons.Rounded.History, contentDescription = null)
                                        },
                                        label = { Text(term) }
                                    )
                                }
                            }
                        }
                    } else {
                        item {
                            EmptyStateView(
                                icon = Icons.Rounded.Search,
                                title = "Search Everything",
                                subtitle = "Search installed apps, wallpapers, themes, phone settings & files, or ask Gemini AI."
                            )
                        }
                    }
                } else {
                    if (aiSearchEnabled) {
                        item {
                            GlassCard(
                                contentPadding = PaddingValues(14.dp),
                                modifier = Modifier.testTag("ai_search_card")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.AutoAwesome,
                                            contentDescription = null,
                                            tint = ElectricCyan
                                        )
                                        Text(
                                            text = "AI Smart Search",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Button(
                                        onClick = { onAskAiSearch(query) },
                                        enabled = aiSearchState !is AiSearchUiState.Loading,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("ask_ai_search_button")
                                    ) {
                                        Text(if (aiSearchState is AiSearchUiState.Loading) "Thinking…" else "Ask AI")
                                    }
                                }

                                when (aiSearchState) {
                                    AiSearchUiState.Idle -> {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Tap Ask AI for instant launcher tips, matching app suggestions & settings.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    AiSearchUiState.Loading -> {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.width(18.dp).height(18.dp))
                                            Text("Analyzing with Gemini AI…", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                    is AiSearchUiState.Error -> {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = aiSearchState.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                    is AiSearchUiState.Success -> {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = aiSearchState.result.answerSummary,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        val suggestions = aiSearchState.result.suggestedAppKeywords +
                                            aiSearchState.result.suggestedWallpaperCategories
                                        if (suggestions.isNotEmpty() || aiSearchState.result.suggestedSystemSetting != null) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                suggestions.distinct().forEach { kw ->
                                                    AssistChip(
                                                        onClick = { onSubmitQuery(kw) },
                                                        label = { Text(kw) }
                                                    )
                                                }
                                                aiSearchState.result.suggestedSystemSetting?.let { settingLabel ->
                                                    AssistChip(
                                                        onClick = { onSubmitQuery(settingLabel) },
                                                        leadingIcon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
                                                        label = { Text(settingLabel) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val showApps = (activeFilter == SearchFilterTab.ALL || activeFilter == SearchFilterTab.APPS) && apps.isNotEmpty()
                    val showPhone = (activeFilter == SearchFilterTab.ALL || activeFilter == SearchFilterTab.PHONE) && phoneContentMatches.isNotEmpty()
                    val showWallpapers = (activeFilter == SearchFilterTab.ALL || activeFilter == SearchFilterTab.WALLPAPERS) && wallpapers.isNotEmpty()
                    val showThemes = (activeFilter == SearchFilterTab.ALL || activeFilter == SearchFilterTab.THEMES) && themes.isNotEmpty()

                    if (!showApps && !showPhone && !showWallpapers && !showThemes && !aiSearchEnabled) {
                        item {
                            EmptyStateView(
                                icon = Icons.Rounded.Search,
                                title = "No results for \"$query\"",
                                subtitle = "Try another keyword like AMOLED, minimal, Wi-Fi, or an installed app name.",
                                actionLabel = "Clear Search",
                                onAction = onClearQuery
                            )
                        }
                    }

                    if (showPhone) {
                        item {
                            Text("Phone Settings & Files (${phoneContentMatches.size})", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                phoneContentMatches.forEach { item ->
                                    GlassCard(
                                        onClick = { onOpenPhoneContent(item) },
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (item.type == PhoneContentType.SYSTEM_SETTING) Icons.Rounded.Settings else Icons.Rounded.Folder,
                                                contentDescription = null,
                                                tint = ElectricCyan
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(item.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                                                Text(
                                                    text = "${item.type.badge} • ${item.subtitle}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showApps) {
                        item {
                            Text("Installed Apps (${apps.size})", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(apps, key = { "s_app_${it.packageName}" }) { app ->
                                    AppIconItem(
                                        app = app,
                                        iconDrawable = iconProvider(app.packageName),
                                        iconSize = 52.dp,
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
                            Text("Wallpapers (${wallpapers.size})", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(wallpapers, key = { "s_wp_${it.id}" }) { wp ->
                                    WallpaperCard(
                                        item = wp,
                                        renderer = renderer,
                                        isPremiumUnlocked = isPremiumUnlocked,
                                        onClick = {
                                            onSubmitQuery(query)
                                            onWallpaperClick(wp)
                                        },
                                        onFavoriteClick = { onToggleWallpaperFav(wp) },
                                        modifier = Modifier.width(142.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (showThemes) {
                        item {
                            Text("Themes (${themes.size})", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(themes, key = { "s_th_${it.id}" }) { theme ->
                                    ThemePreviewCard(
                                        theme = theme,
                                        isActive = theme.id == activeThemeId,
                                        isPremiumUnlocked = isPremiumUnlocked,
                                        onClick = {
                                            onSubmitQuery(query)
                                            onThemeClick(theme)
                                        },
                                        onFavoriteClick = { onToggleThemeFav(theme) },
                                        modifier = Modifier.width(205.dp)
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
