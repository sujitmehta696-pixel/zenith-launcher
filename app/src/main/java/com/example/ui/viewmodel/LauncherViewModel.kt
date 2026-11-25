package com.example.ui.viewmodel

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.LauncherNotificationListenerService
import com.example.data.model.AppSortOrder
import com.example.data.model.GestureAction
import com.example.data.model.InstalledAppInfo
import com.example.data.model.LauncherMode
import com.example.data.model.NavigationMode
import com.example.data.model.SpinnerStyle
import com.example.data.model.ThemeIconStyle
import com.example.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LauncherViewModel(private val container: AppContainer) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(AppSortOrder.ALPHABETICAL)
    val sortOrder: StateFlow<AppSortOrder> = _sortOrder.asStateFlow()

    private val _isDefaultLauncher = MutableStateFlow(container.launcherRepository.isDefaultLauncher())
    val isDefaultLauncher: StateFlow<Boolean> = _isDefaultLauncher.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    val notificationBadgeCounts: StateFlow<Map<String, Int>> =
        LauncherNotificationListenerService.badgeCounts

    val allApps: StateFlow<List<InstalledAppInfo>> =
        container.launcherRepository.appsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val drawerApps: StateFlow<List<InstalledAppInfo>> = combine(
        container.launcherRepository.appsFlow,
        _searchQuery,
        _sortOrder
    ) { apps, query, sort ->
        val visible = apps.filter { !it.isHidden }
        val filtered = if (query.isBlank()) {
            visible
        } else {
            visible.filter {
                it.label.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
            }
        }
        when (sort) {
            AppSortOrder.ALPHABETICAL -> filtered.sortedBy { it.label.lowercase() }
            AppSortOrder.RECENTLY_USED -> filtered.sortedWith(
                compareByDescending<InstalledAppInfo> { it.lastLaunchTimestamp }
                    .thenBy { it.label.lowercase() }
            )
            AppSortOrder.MOST_USED -> filtered.sortedWith(
                compareByDescending<InstalledAppInfo> { it.launchCount }
                    .thenBy { it.label.lowercase() }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val homeApps: StateFlow<List<InstalledAppInfo>> =
        container.launcherRepository.appsFlow
            .combine(container.preferencesDataStore.settingsFlow) { apps, prefs ->
                val visible = apps.filter { !it.isHidden }
                val pinned = visible.filter { it.isPinnedHome }.sortedWith(
                    compareBy<InstalledAppInfo> { it.homePage }.thenBy { it.homeSlot }
                )
                if (pinned.isNotEmpty()) {
                    pinned
                } else {
                    // Default curated workspace slots from actual installed apps on the device
                    val capacity = (prefs.gridColumns * prefs.gridRows).coerceAtLeast(12)
                    visible.take(capacity)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val hiddenApps: StateFlow<List<InstalledAppInfo>> =
        container.launcherRepository.appsFlow
            .combine(_searchQuery) { apps, _ -> apps.filter { it.isHidden } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun refreshAppsAndDefaultStatus() {
        _isDefaultLauncher.value = container.launcherRepository.isDefaultLauncher()
        viewModelScope.launch {
            container.launcherRepository.refreshInstalledApps()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(order: AppSortOrder) {
        _sortOrder.value = order
    }

    fun setLauncherMode(mode: LauncherMode) {
        viewModelScope.launch {
            container.preferencesDataStore.setLauncherMode(mode)
        }
    }

    fun setSpinnerStyle(style: SpinnerStyle) {
        viewModelScope.launch {
            container.preferencesDataStore.setSpinnerStyle(style)
        }
    }

    fun setGridSize(columns: Int, rows: Int) {
        viewModelScope.launch {
            container.preferencesDataStore.setGridSize(columns, rows)
        }
    }

    fun setIconSizeDp(sizeDp: Int) {
        viewModelScope.launch {
            container.preferencesDataStore.setIconSizeDp(sizeDp)
        }
    }

    fun setShowIconLabels(show: Boolean) {
        viewModelScope.launch {
            container.preferencesDataStore.setShowIconLabels(show)
        }
    }

    fun setSwipeDownAction(action: com.example.data.model.GestureAction) {
        viewModelScope.launch {
            container.preferencesDataStore.setSwipeDownAction(action)
        }
    }

    fun setSwipeUpAction(action: com.example.data.model.GestureAction) {
        viewModelScope.launch {
            container.preferencesDataStore.setSwipeUpAction(action)
        }
    }

    fun setDoubleTapAction(action: com.example.data.model.GestureAction) {
        viewModelScope.launch {
            container.preferencesDataStore.setDoubleTapAction(action)
        }
    }

    fun setLongPressAction(action: GestureAction) {
        viewModelScope.launch {
            container.preferencesDataStore.setLongPressAction(action)
        }
    }

    fun setPinchAction(action: GestureAction) {
        viewModelScope.launch {
            container.preferencesDataStore.setPinchAction(action)
        }
    }

    fun setIconStyleOverride(style: ThemeIconStyle?) {
        viewModelScope.launch {
            container.preferencesDataStore.setIconStyleOverride(style)
        }
    }

    fun setNavigationMode(mode: NavigationMode) {
        viewModelScope.launch {
            container.preferencesDataStore.setNavigationMode(mode)
        }
    }

    fun resetGestureDefaults() {
        viewModelScope.launch {
            container.preferencesDataStore.setSwipeDownAction(GestureAction.OPEN_CONTROL_CENTER)
            container.preferencesDataStore.setSwipeUpAction(GestureAction.OPEN_APP_DRAWER)
            container.preferencesDataStore.setDoubleTapAction(GestureAction.LOCK_SCREEN)
            container.preferencesDataStore.setLongPressAction(GestureAction.OPEN_STUDIO)
            container.preferencesDataStore.setPinchAction(GestureAction.OPEN_EDGE_PANEL)
            _statusBanner.value = "Home gestures restored to smart defaults"
        }
    }

    fun setThemeAppIcons(enabled: Boolean) {
        viewModelScope.launch {
            container.preferencesDataStore.setThemeAppIcons(enabled)
        }
    }

    fun setThemeWidgets(enabled: Boolean) {
        viewModelScope.launch {
            container.preferencesDataStore.setThemeWidgets(enabled)
        }
    }

    fun setSyncThemeWallpaper(enabled: Boolean) {
        viewModelScope.launch {
            container.preferencesDataStore.setSyncThemeWallpaper(enabled)
        }
    }

    fun setLiquidGlassEnabled(enabled: Boolean) {
        viewModelScope.launch {
            container.preferencesDataStore.setLiquidGlassEnabled(enabled)
        }
    }

    fun setAutoRollingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            container.preferencesDataStore.setAutoRollingEnabled(enabled)
        }
    }

    fun toggleAutoRolling(currentlyEnabled: Boolean) {
        setAutoRollingEnabled(!currentlyEnabled)
    }

    fun setRollingSpeed(speed: Float) {
        viewModelScope.launch {
            container.preferencesDataStore.setRollingSpeed(speed)
        }
    }

    fun setDepth3DIntensity(depth: Float) {
        viewModelScope.launch {
            container.preferencesDataStore.setDepth3DIntensity(depth)
        }
    }

    fun getAppIcon(packageName: String): Drawable? =
        container.launcherRepository.getAppIcon(packageName)

    fun launchApp(app: InstalledAppInfo) {
        viewModelScope.launch {
            container.launcherRepository.launchApp(app).onFailure { err ->
                _statusBanner.value = "Unable to launch ${app.label}: ${err.message}"
            }
        }
    }

    fun queryAppShortcuts(packageName: String): List<ShortcutInfo> =
        container.launcherRepository.queryAppShortcuts(packageName)

    fun launchShortcut(shortcut: ShortcutInfo): Boolean =
        container.launcherRepository.launchShortcut(shortcut)

    fun openAppInfo(packageName: String) {
        container.launcherRepository.openAppInfo(packageName)
    }

    fun uninstallApp(packageName: String) {
        container.launcherRepository.uninstallApp(packageName)
    }

    fun toggleFavorite(app: InstalledAppInfo) {
        viewModelScope.launch {
            container.launcherRepository.toggleFavoriteApp(app.packageName, app.isFavorite)
        }
    }

    fun toggleHidden(app: InstalledAppInfo) {
        viewModelScope.launch {
            container.launcherRepository.toggleHiddenApp(app.packageName, app.isHidden)
            _statusBanner.value = if (app.isHidden) {
                "Restored ${app.label} to launcher"
            } else {
                "Hidden ${app.label} from launcher, drawer & search"
            }
        }
    }

    fun pinToHome(app: InstalledAppInfo, pageIndex: Int = 0, slotIndex: Int = 0) {
        viewModelScope.launch {
            container.launcherRepository.pinAppToHome(app.packageName, pageIndex, slotIndex)
            _statusBanner.value = "Pinned ${app.label} to Home"
        }
    }

    fun unpinFromHome(app: InstalledAppInfo) {
        viewModelScope.launch {
            container.launcherRepository.unpinAppFromHome(app.packageName)
            _statusBanner.value = "Removed ${app.label} from Home"
        }
    }

    fun createDefaultLauncherIntent(): Intent =
        container.launcherRepository.createDefaultLauncherIntent()

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LauncherViewModel(container) as T
                }
            }
    }
}
