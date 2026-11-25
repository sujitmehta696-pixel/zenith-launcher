package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.AppContainer
import com.example.data.cloud.AiSearchResult
import com.example.data.launcher.PhoneContentItem
import com.example.data.model.InstalledAppInfo
import com.example.data.model.ThemeCategory
import com.example.data.model.ThemeConfig
import com.example.data.model.WallpaperCategory
import com.example.data.model.WallpaperItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SearchFilterTab(val label: String) {
    ALL("All"),
    WALLPAPERS("Wallpapers"),
    THEMES("Themes"),
    APPS("Apps"),
    PHONE("Phone")
}

enum class FavoritesFilterTab(val label: String) {
    ALL("All"),
    WALLPAPERS("Wallpapers"),
    THEMES("Themes"),
    APPS("Apps")
}

sealed interface AiSearchUiState {
    data object Idle : AiSearchUiState
    data object Loading : AiSearchUiState
    data class Success(val result: AiSearchResult) : AiSearchUiState
    data class Error(val message: String) : AiSearchUiState
}

class SearchFavoritesViewModel(private val container: AppContainer) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchFilter = MutableStateFlow(SearchFilterTab.ALL)
    val searchFilter: StateFlow<SearchFilterTab> = _searchFilter.asStateFlow()

    private val _favoritesFilter = MutableStateFlow(FavoritesFilterTab.ALL)
    val favoritesFilter: StateFlow<FavoritesFilterTab> = _favoritesFilter.asStateFlow()

    private val _aiSearchState = MutableStateFlow<AiSearchUiState>(AiSearchUiState.Idle)
    val aiSearchState: StateFlow<AiSearchUiState> = _aiSearchState.asStateFlow()

    val searchHistory: StateFlow<List<String>> =
        container.launcherRepository.searchHistoryFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val matchingWallpapers: StateFlow<List<WallpaperItem>> = _searchQuery
        .flatMapLatest { q ->
            container.wallpaperRepository.queryWallpapers(
                category = WallpaperCategory.ALL,
                premiumFilter = null,
                searchQuery = q,
                limit = 40
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val matchingThemes: StateFlow<List<ThemeConfig>> = _searchQuery
        .flatMapLatest { q ->
            container.themeRepository.queryThemes(
                category = ThemeCategory.ALL,
                premiumFilter = null,
                searchQuery = q,
                limit = 40
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val matchingApps: StateFlow<List<InstalledAppInfo>> = combine(
        container.launcherRepository.appsFlow,
        _searchQuery
    ) { apps, q ->
        val trimmed = q.trim()
        if (trimmed.isEmpty()) {
            emptyList()
        } else {
            apps.filter {
                !it.isHidden && (it.label.contains(trimmed, ignoreCase = true) ||
                    it.packageName.contains(trimmed, ignoreCase = true))
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val phoneContentMatches: StateFlow<List<PhoneContentItem>> = _searchQuery
        .mapLatest { q -> container.phoneContentSearchHelper.searchPhoneContent(q) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteWallpapers: StateFlow<List<WallpaperItem>> =
        container.wallpaperRepository.observeFavorites().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteThemes: StateFlow<List<ThemeConfig>> =
        container.themeRepository.observeFavorites().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteApps: StateFlow<List<InstalledAppInfo>> =
        container.launcherRepository.appsFlow
            .combine(_favoritesFilter) { apps, _ -> apps.filter { it.isFavorite && !it.isHidden } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _aiSearchState.value = AiSearchUiState.Idle
        }
    }

    fun submitSearch(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            container.launcherRepository.recordSearchQuery(query)
        }
    }

    fun askAiSearch(query: String) {
        val clean = query.trim()
        if (clean.isBlank()) return
        viewModelScope.launch {
            _aiSearchState.value = AiSearchUiState.Loading
            val appLabels = matchingApps.value.ifEmpty { emptyList() }.map { it.label }
            val result = container.aiSearchRepository.searchWithAi(clean, appLabels)
            _aiSearchState.value = result.fold(
                onSuccess = { AiSearchUiState.Success(it) },
                onFailure = { AiSearchUiState.Error(it.localizedMessage ?: "AI Search failed.") }
            )
        }
    }

    fun openPhoneContentItem(item: PhoneContentItem) {
        container.phoneContentSearchHelper.openSystemShortcut(item)
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _aiSearchState.value = AiSearchUiState.Idle
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            container.launcherRepository.clearSearchHistory()
        }
    }

    fun setSearchFilter(tab: SearchFilterTab) {
        _searchFilter.value = tab
    }

    fun setFavoritesFilter(tab: FavoritesFilterTab) {
        _favoritesFilter.value = tab
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SearchFavoritesViewModel(container) as T
                }
            }
    }
}
