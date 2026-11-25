package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.AppContainer
import com.example.LiveOrbitVideoWallpaperService
import com.example.data.model.CatalogManifest
import com.example.data.model.RemoteCategory
import com.example.data.model.WallpaperCategory
import com.example.data.model.WallpaperItem
import com.example.data.wallpaper.WallpaperTarget
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ContentTierFilter(val label: String) {
    ALL("All"),
    FREE("Free"),
    PREMIUM("Pro")
}

sealed interface WallpaperActionState {
    data object Idle : WallpaperActionState
    data class Loading(val message: String) : WallpaperActionState
    data class Success(val message: String) : WallpaperActionState
    data class Error(val message: String) : WallpaperActionState
}

class WallpaperViewModel(private val container: AppContainer) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(WallpaperCategory.ALL)
    val selectedCategory: StateFlow<WallpaperCategory> = _selectedCategory.asStateFlow()

    private val _selectedRemoteCategoryName = MutableStateFlow<String?>(null)
    val selectedRemoteCategoryName: StateFlow<String?> = _selectedRemoteCategoryName.asStateFlow()

    private val _tierFilter = MutableStateFlow(ContentTierFilter.ALL)
    val tierFilter: StateFlow<ContentTierFilter> = _tierFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _pageLimit = MutableStateFlow(20)

    private val _actionState = MutableStateFlow<WallpaperActionState>(WallpaperActionState.Idle)
    val actionState: StateFlow<WallpaperActionState> = _actionState.asStateFlow()

    val manifest: CatalogManifest? = container.wallpaperRepository.manifest

    @OptIn(ExperimentalCoroutinesApi::class)
    val remoteCategories: StateFlow<List<RemoteCategory>> =
        container.authRepository.authStateFlow()
            .flatMapLatest { user ->
                if (user == null) flowOf(emptyList())
                else container.remoteContentRepository.observeActiveCategories()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val wallpapers: StateFlow<List<WallpaperItem>> = combine(
        _selectedCategory,
        _selectedRemoteCategoryName,
        _tierFilter,
        _searchQuery,
        _pageLimit
    ) { category, remoteCatName, tier, query, limit ->
        Tuple5(category, remoteCatName, tier, query, limit)
    }.flatMapLatest { (category, remoteCatName, tier, query, limit) ->
        val premiumFlag = when (tier) {
            ContentTierFilter.ALL -> null
            ContentTierFilter.FREE -> false
            ContentTierFilter.PREMIUM -> true
        }
        container.wallpaperRepository.queryWallpapers(
            category = category,
            premiumFilter = premiumFlag,
            searchQuery = if (remoteCatName != null && query.isBlank()) remoteCatName else query,
            limit = limit
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val totalMatchingCount: StateFlow<Int> = combine(
        _selectedCategory,
        _selectedRemoteCategoryName,
        _tierFilter,
        _searchQuery
    ) { category, remoteCatName, tier, query ->
        Quad(category, remoteCatName, tier, query)
    }.flatMapLatest { (category, remoteCatName, tier, query) ->
        val premiumFlag = when (tier) {
            ContentTierFilter.ALL -> null
            ContentTierFilter.FREE -> false
            ContentTierFilter.PREMIUM -> true
        }
        container.wallpaperRepository.countMatching(
            category = category,
            premiumFilter = premiumFlag,
            searchQuery = if (remoteCatName != null && query.isBlank()) remoteCatName else query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val recentlyViewed: StateFlow<List<WallpaperItem>> =
        container.wallpaperRepository.observeRecentlyViewed().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectCategory(category: WallpaperCategory) {
        _selectedCategory.value = category
        _selectedRemoteCategoryName.value = null
        _pageLimit.value = 20
    }

    fun selectRemoteCategory(categoryName: String?) {
        _selectedRemoteCategoryName.value = categoryName
        if (categoryName != null) {
            _selectedCategory.value = WallpaperCategory.ALL
        }
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

    fun observeWallpaper(id: String): Flow<WallpaperItem?> =
        container.wallpaperRepository.observeWallpaper(id)

    fun markViewed(id: String) {
        viewModelScope.launch {
            container.wallpaperRepository.markViewed(id)
        }
    }

    fun toggleFavorite(item: WallpaperItem) {
        viewModelScope.launch {
            container.wallpaperRepository.toggleFavorite(item.id, item.isFavorite)
        }
    }

    fun downloadWallpaper(item: WallpaperItem) {
        if (!container.billingManager.billingState.value.isPremium) {
            _actionState.value = WallpaperActionState.Error(
                "Paid Pro subscription required to download or apply wallpapers."
            )
            return
        }
        viewModelScope.launch {
            _actionState.value = WallpaperActionState.Loading(
                if (item.isRemoteVideo) "Downloading Live MP4 wallpaper…" else "Saving high-res wallpaper…"
            )
            container.wallpaperRepository.downloadWallpaper(item)
                .onSuccess {
                    _actionState.value = WallpaperActionState.Success("Saved '${item.name}' to Gallery")
                }
                .onFailure { err ->
                    _actionState.value = WallpaperActionState.Error(err.message ?: "Download failed")
                }
        }
    }

    fun applyWallpaper(item: WallpaperItem, target: WallpaperTarget) {
        if (!container.billingManager.billingState.value.isPremium) {
            _actionState.value = WallpaperActionState.Error(
                "Paid Pro subscription required to apply wallpapers."
            )
            return
        }
        viewModelScope.launch {
            val label = when (target) {
                WallpaperTarget.HOME -> "Home Screen"
                WallpaperTarget.LOCK -> "Lock Screen"
                WallpaperTarget.BOTH -> "Home & Lock Screen"
            }
            _actionState.value = WallpaperActionState.Loading("Applying to $label…")
            container.wallpaperRepository.applyWallpaper(item, target)
                .onSuccess {
                    _actionState.value = WallpaperActionState.Success("Applied '${item.name}' to $label")
                }
                .onFailure { err ->
                    _actionState.value = WallpaperActionState.Error(err.message ?: "Could not apply wallpaper")
                }
        }
    }

    fun applyLiveVideoWallpaper(context: Context, item: WallpaperItem, onLaunchPicker: (Intent) -> Unit) {
        if (!container.billingManager.billingState.value.isPremium) {
            _actionState.value = WallpaperActionState.Error(
                "Paid Pro subscription required to apply Live MP4 wallpapers."
            )
            return
        }
        viewModelScope.launch {
            _actionState.value = WallpaperActionState.Loading("Preparing Live MP4 Engine…")
            container.wallpaperRepository.prepareLiveVideoWallpaper(item)
                .onSuccess {
                    _actionState.value = WallpaperActionState.Success("Live MP4 ready! Confirm on the next screen.")
                    onLaunchPicker(LiveOrbitVideoWallpaperService.createSetLiveWallpaperIntent(context))
                }
                .onFailure { err ->
                    _actionState.value = WallpaperActionState.Error(err.message ?: "Could not prepare live video.")
                }
        }
    }

    fun clearActionState() {
        _actionState.value = WallpaperActionState.Idle
    }

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
    private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return WallpaperViewModel(container) as T
                }
            }
    }
}
