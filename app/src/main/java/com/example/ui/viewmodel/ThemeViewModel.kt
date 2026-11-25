package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppearanceMode
import com.example.data.model.ThemeCategory
import com.example.data.model.ThemeConfig
import com.example.AppContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ThemeViewModel(private val container: AppContainer) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(ThemeCategory.ALL)
    val selectedCategory: StateFlow<ThemeCategory> = _selectedCategory.asStateFlow()

    private val _tierFilter = MutableStateFlow(ContentTierFilter.ALL)
    val tierFilter: StateFlow<ContentTierFilter> = _tierFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _pageLimit = MutableStateFlow(20)

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val themes: StateFlow<List<ThemeConfig>> = combine(
        _selectedCategory,
        _tierFilter,
        _searchQuery,
        _pageLimit
    ) { category, tier, query, limit ->
        Quad(category, tier, query, limit)
    }.flatMapLatest { (category, tier, query, limit) ->
        val premiumFlag = when (tier) {
            ContentTierFilter.ALL -> null
            ContentTierFilter.FREE -> false
            ContentTierFilter.PREMIUM -> true
        }
        container.themeRepository.queryThemes(category, premiumFlag, query, limit)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val totalMatchingCount: StateFlow<Int> = combine(
        _selectedCategory,
        _tierFilter,
        _searchQuery
    ) { category, tier, query ->
        Triple(category, tier, query)
    }.flatMapLatest { (category, tier, query) ->
        val premiumFlag = when (tier) {
            ContentTierFilter.ALL -> null
            ContentTierFilter.FREE -> false
            ContentTierFilter.PREMIUM -> true
        }
        container.themeRepository.countMatching(category, premiumFlag, query)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    fun selectCategory(category: ThemeCategory) {
        _selectedCategory.value = category
        _pageLimit.value = 20
    }

    fun selectTier(tier: ContentTierFilter) {
        _tierFilter.value = tier
        _pageLimit.value = 20
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _pageLimit.value = 20
    }

    fun loadMore() {
        _pageLimit.update { it + 20 }
    }

    fun observeTheme(id: String): Flow<ThemeConfig?> =
        container.themeRepository.observeTheme(id)

    fun applyTheme(theme: ThemeConfig) {
        if (!container.billingManager.billingState.value.isPremium) {
            _feedbackMessage.value = "Paid Pro subscription required to apply themes."
            return
        }
        viewModelScope.launch {
            container.themeRepository.applyTheme(theme.id)
            container.preferencesDataStore.setAppearanceMode(
                if (theme.isDarkTheme) AppearanceMode.DARK else AppearanceMode.LIGHT
            )
            val syncWallpaper = container.preferencesDataStore.settingsFlow.first().syncThemeWallpaper
            if (syncWallpaper) {
                theme.associatedWallpaperId?.let { wpId ->
                    container.preferencesDataStore.setActiveWallpaperId(wpId)
                }
            }
            _feedbackMessage.value = if (syncWallpaper && theme.associatedWallpaperId != null) {
                "Applied '${theme.name}' across UI, widgets, icons & wallpaper"
            } else {
                "Applied '${theme.name}' across UI, widgets & icons"
            }
        }
    }

    fun resetTheme() {
        viewModelScope.launch {
            container.themeRepository.resetToDefaultTheme()
            container.preferencesDataStore.setAppearanceMode(AppearanceMode.DARK)
            _feedbackMessage.value = "Reset to Obsidian Core"
        }
    }

    fun toggleFavorite(theme: ThemeConfig) {
        viewModelScope.launch {
            container.themeRepository.toggleFavorite(theme.id, theme.isFavorite)
        }
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ThemeViewModel(container) as T
                }
            }
    }
}
