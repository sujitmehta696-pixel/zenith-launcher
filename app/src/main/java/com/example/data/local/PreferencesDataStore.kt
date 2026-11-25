package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppearanceMode
import com.example.data.model.GestureAction
import com.example.data.model.LauncherBackgroundPreset
import com.example.data.model.LauncherMode
import com.example.data.model.LauncherSettingsState
import com.example.data.model.NavigationMode
import com.example.data.model.SpinnerStyle
import com.example.data.model.ThemeIconStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.launcherDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "android_launcher_prefs"
)

class PreferencesDataStore(private val context: Context) {

    private object Keys {
        val ACTIVE_THEME_ID = stringPreferencesKey("active_theme_id")
        val ACTIVE_WALLPAPER_ID = stringPreferencesKey("active_wallpaper_id")
        val LAUNCHER_MODE = stringPreferencesKey("launcher_mode")
        val SPINNER_STYLE = stringPreferencesKey("spinner_style")
        val GRID_COLUMNS = intPreferencesKey("grid_columns")
        val GRID_ROWS = intPreferencesKey("grid_rows")
        val ICON_SIZE_DP = intPreferencesKey("icon_size_dp")
        val SHOW_ICON_LABELS = booleanPreferencesKey("show_icon_labels")
        val APPEARANCE_MODE = stringPreferencesKey("appearance_mode")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val AUTO_CACHE_THUMBNAILS = booleanPreferencesKey("auto_cache_thumbnails")
        val WIFI_ONLY_DOWNLOADS = booleanPreferencesKey("wifi_only_downloads")
        val SWIPE_DOWN_ACTION = stringPreferencesKey("swipe_down_action")
        val SWIPE_UP_ACTION = stringPreferencesKey("swipe_up_action")
        val DOUBLE_TAP_ACTION = stringPreferencesKey("double_tap_action")
        val LONG_PRESS_ACTION = stringPreferencesKey("long_press_action")
        val THEME_APP_ICONS = booleanPreferencesKey("theme_app_icons")
        val THEME_WIDGETS = booleanPreferencesKey("theme_widgets")
        val SYNC_THEME_WALLPAPER = booleanPreferencesKey("sync_theme_wallpaper")
        val AUTO_ROLLING_ENABLED = booleanPreferencesKey("auto_rolling_enabled")
        val ROLLING_SPEED = floatPreferencesKey("rolling_speed")
        val DEPTH_3D_INTENSITY = floatPreferencesKey("depth_3d_intensity")
        val BACKGROUND_PRESET = stringPreferencesKey("background_preset")
        val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")
        val NAVIGATION_MODE = stringPreferencesKey("navigation_mode")
        val LIQUID_GLASS_ENABLED = booleanPreferencesKey("liquid_glass_enabled")
        val PINCH_ACTION = stringPreferencesKey("pinch_action")
        val ICON_STYLE_OVERRIDE = stringPreferencesKey("icon_style_override")
    }

    val settingsFlow: Flow<LauncherSettingsState> = context.launcherDataStore.data.map { prefs ->
        LauncherSettingsState(
            launcherMode = prefs[Keys.LAUNCHER_MODE]
                ?.let { raw -> LauncherMode.entries.firstOrNull { it.name == raw } }
                ?: LauncherMode.SPINNER,
            spinnerStyle = prefs[Keys.SPINNER_STYLE]
                ?.let { raw -> SpinnerStyle.entries.firstOrNull { it.name == raw } }
                ?: SpinnerStyle.ORBITAL_DIAL,
            navigationMode = prefs[Keys.NAVIGATION_MODE]
                ?.let { raw -> NavigationMode.entries.firstOrNull { it.name == raw } }
                ?: NavigationMode.GESTURE,
            gridColumns = (prefs[Keys.GRID_COLUMNS] ?: 4).coerceIn(3, 6),
            gridRows = (prefs[Keys.GRID_ROWS] ?: 5).coerceIn(4, 7),
            iconSizeDp = (prefs[Keys.ICON_SIZE_DP] ?: 56).coerceIn(44, 72),
            showIconLabels = prefs[Keys.SHOW_ICON_LABELS] ?: true,
            appearanceMode = prefs[Keys.APPEARANCE_MODE]
                ?.let { raw -> AppearanceMode.entries.firstOrNull { it.name == raw } }
                ?: AppearanceMode.DARK,
            reduceMotion = prefs[Keys.REDUCE_MOTION] ?: false,
            autoCacheThumbnails = prefs[Keys.AUTO_CACHE_THUMBNAILS] ?: true,
            wifiOnlyDownloads = prefs[Keys.WIFI_ONLY_DOWNLOADS] ?: false,
            activeThemeId = prefs[Keys.ACTIVE_THEME_ID] ?: DEFAULT_THEME_ID,
            activeWallpaperId = prefs[Keys.ACTIVE_WALLPAPER_ID] ?: DEFAULT_WALLPAPER_ID,
            swipeDownAction = prefs[Keys.SWIPE_DOWN_ACTION]
                ?.let { raw -> GestureAction.entries.firstOrNull { it.name == raw } }
                ?: GestureAction.OPEN_NOTIFICATIONS,
            swipeUpAction = prefs[Keys.SWIPE_UP_ACTION]
                ?.let { raw -> GestureAction.entries.firstOrNull { it.name == raw } }
                ?: GestureAction.OPEN_APP_DRAWER,
            doubleTapAction = prefs[Keys.DOUBLE_TAP_ACTION]
                ?.let { raw -> GestureAction.entries.firstOrNull { it.name == raw } }
                ?: GestureAction.LOCK_SCREEN,
            longPressAction = prefs[Keys.LONG_PRESS_ACTION]
                ?.let { raw -> GestureAction.entries.firstOrNull { it.name == raw } }
                ?: GestureAction.OPEN_STUDIO,
            pinchAction = prefs[Keys.PINCH_ACTION]
                ?.let { raw -> GestureAction.entries.firstOrNull { it.name == raw } }
                ?: GestureAction.OPEN_EDGE_PANEL,
            iconStyleOverride = prefs[Keys.ICON_STYLE_OVERRIDE]
                ?.let { raw -> ThemeIconStyle.entries.firstOrNull { it.name == raw } },
            themeAppIcons = prefs[Keys.THEME_APP_ICONS] ?: true,
            themeWidgets = prefs[Keys.THEME_WIDGETS] ?: true,
            syncThemeWallpaper = prefs[Keys.SYNC_THEME_WALLPAPER] ?: true,
            liquidGlassEnabled = prefs[Keys.LIQUID_GLASS_ENABLED] ?: true,
            autoRollingEnabled = prefs[Keys.AUTO_ROLLING_ENABLED] ?: true,
            rollingSpeed = (prefs[Keys.ROLLING_SPEED] ?: 1.0f).coerceIn(0.2f, 2.5f),
            depth3DIntensity = (prefs[Keys.DEPTH_3D_INTENSITY] ?: 0.85f).coerceIn(0.0f, 1.0f),
            backgroundPreset = prefs[Keys.BACKGROUND_PRESET]
                ?.let { raw -> LauncherBackgroundPreset.entries.firstOrNull { it.name == raw } }
                ?: LauncherBackgroundPreset.MIDNIGHT_COSMOS,
            hapticFeedbackEnabled = prefs[Keys.HAPTIC_FEEDBACK_ENABLED] ?: true
        )
    }

    suspend fun setActiveThemeId(themeId: String) {
        context.launcherDataStore.edit { it[Keys.ACTIVE_THEME_ID] = themeId }
    }

    suspend fun setActiveWallpaperId(wallpaperId: String) {
        context.launcherDataStore.edit { it[Keys.ACTIVE_WALLPAPER_ID] = wallpaperId }
    }

    suspend fun setLauncherMode(mode: LauncherMode) {
        context.launcherDataStore.edit { it[Keys.LAUNCHER_MODE] = mode.name }
    }

    suspend fun setSpinnerStyle(style: SpinnerStyle) {
        context.launcherDataStore.edit { it[Keys.SPINNER_STYLE] = style.name }
    }

    suspend fun setGridSize(columns: Int, rows: Int) {
        context.launcherDataStore.edit {
            it[Keys.GRID_COLUMNS] = columns.coerceIn(3, 6)
            it[Keys.GRID_ROWS] = rows.coerceIn(4, 7)
        }
    }

    suspend fun setIconSizeDp(sizeDp: Int) {
        context.launcherDataStore.edit { it[Keys.ICON_SIZE_DP] = sizeDp.coerceIn(44, 72) }
    }

    suspend fun setShowIconLabels(show: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_ICON_LABELS] = show }
    }

    suspend fun setAppearanceMode(mode: AppearanceMode) {
        context.launcherDataStore.edit { it[Keys.APPEARANCE_MODE] = mode.name }
    }

    suspend fun setReduceMotion(reduce: Boolean) {
        context.launcherDataStore.edit { it[Keys.REDUCE_MOTION] = reduce }
    }

    suspend fun setAutoCacheThumbnails(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.AUTO_CACHE_THUMBNAILS] = enabled }
    }

    suspend fun setWifiOnlyDownloads(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.WIFI_ONLY_DOWNLOADS] = enabled }
    }

    suspend fun setSwipeDownAction(action: GestureAction) {
        context.launcherDataStore.edit { it[Keys.SWIPE_DOWN_ACTION] = action.name }
    }

    suspend fun setSwipeUpAction(action: GestureAction) {
        context.launcherDataStore.edit { it[Keys.SWIPE_UP_ACTION] = action.name }
    }

    suspend fun setDoubleTapAction(action: GestureAction) {
        context.launcherDataStore.edit { it[Keys.DOUBLE_TAP_ACTION] = action.name }
    }

    suspend fun setLongPressAction(action: GestureAction) {
        context.launcherDataStore.edit { it[Keys.LONG_PRESS_ACTION] = action.name }
    }

    suspend fun setThemeAppIcons(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.THEME_APP_ICONS] = enabled }
    }

    suspend fun setThemeWidgets(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.THEME_WIDGETS] = enabled }
    }

    suspend fun setSyncThemeWallpaper(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.SYNC_THEME_WALLPAPER] = enabled }
    }

    suspend fun setAutoRollingEnabled(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.AUTO_ROLLING_ENABLED] = enabled }
    }

    suspend fun setRollingSpeed(speed: Float) {
        context.launcherDataStore.edit { it[Keys.ROLLING_SPEED] = speed.coerceIn(0.2f, 2.5f) }
    }

    suspend fun setDepth3DIntensity(depth: Float) {
        context.launcherDataStore.edit { it[Keys.DEPTH_3D_INTENSITY] = depth.coerceIn(0.0f, 1.0f) }
    }

    suspend fun setBackgroundPreset(preset: LauncherBackgroundPreset) {
        context.launcherDataStore.edit { it[Keys.BACKGROUND_PRESET] = preset.name }
    }

    suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.HAPTIC_FEEDBACK_ENABLED] = enabled }
    }

    suspend fun setNavigationMode(mode: NavigationMode) {
        context.launcherDataStore.edit { it[Keys.NAVIGATION_MODE] = mode.name }
    }

    suspend fun setLiquidGlassEnabled(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.LIQUID_GLASS_ENABLED] = enabled }
    }

    suspend fun setPinchAction(action: GestureAction) {
        context.launcherDataStore.edit { it[Keys.PINCH_ACTION] = action.name }
    }

    suspend fun setIconStyleOverride(style: ThemeIconStyle?) {
        context.launcherDataStore.edit { prefs ->
            if (style == null) prefs.remove(Keys.ICON_STYLE_OVERRIDE)
            else prefs[Keys.ICON_STYLE_OVERRIDE] = style.name
        }
    }

    suspend fun applyCloudSyncedSettings(
        activeThemeId: String,
        activeWallpaperId: String,
        launcherMode: LauncherMode,
        spinnerStyle: SpinnerStyle,
        swipeDownAction: GestureAction,
        swipeUpAction: GestureAction,
        doubleTapAction: GestureAction,
        longPressAction: GestureAction,
        pinchAction: GestureAction? = null
    ) {
        context.launcherDataStore.edit { prefs ->
            prefs[Keys.ACTIVE_THEME_ID] = activeThemeId
            prefs[Keys.ACTIVE_WALLPAPER_ID] = activeWallpaperId
            prefs[Keys.LAUNCHER_MODE] = launcherMode.name
            prefs[Keys.SPINNER_STYLE] = spinnerStyle.name
            prefs[Keys.SWIPE_DOWN_ACTION] = swipeDownAction.name
            prefs[Keys.SWIPE_UP_ACTION] = swipeUpAction.name
            prefs[Keys.DOUBLE_TAP_ACTION] = doubleTapAction.name
            prefs[Keys.LONG_PRESS_ACTION] = longPressAction.name
            if (pinchAction != null) {
                prefs[Keys.PINCH_ACTION] = pinchAction.name
            }
        }
    }

    suspend fun resetAllCustomizations() {
        context.launcherDataStore.edit { prefs ->
            prefs[Keys.ACTIVE_THEME_ID] = DEFAULT_THEME_ID
            prefs[Keys.ACTIVE_WALLPAPER_ID] = DEFAULT_WALLPAPER_ID
            prefs[Keys.LAUNCHER_MODE] = LauncherMode.SPINNER.name
            prefs[Keys.SPINNER_STYLE] = SpinnerStyle.ORBITAL_DIAL.name
            prefs[Keys.GRID_COLUMNS] = 4
            prefs[Keys.GRID_ROWS] = 5
            prefs[Keys.ICON_SIZE_DP] = 56
            prefs[Keys.SHOW_ICON_LABELS] = true
            prefs[Keys.APPEARANCE_MODE] = AppearanceMode.DARK.name
            prefs[Keys.REDUCE_MOTION] = false
            prefs[Keys.SWIPE_DOWN_ACTION] = GestureAction.OPEN_CONTROL_CENTER.name
            prefs[Keys.SWIPE_UP_ACTION] = GestureAction.OPEN_APP_DRAWER.name
            prefs[Keys.DOUBLE_TAP_ACTION] = GestureAction.LOCK_SCREEN.name
            prefs[Keys.LONG_PRESS_ACTION] = GestureAction.OPEN_STUDIO.name
            prefs[Keys.PINCH_ACTION] = GestureAction.OPEN_EDGE_PANEL.name
            prefs.remove(Keys.ICON_STYLE_OVERRIDE)
            prefs[Keys.THEME_APP_ICONS] = true
            prefs[Keys.THEME_WIDGETS] = true
            prefs[Keys.SYNC_THEME_WALLPAPER] = true
            prefs[Keys.AUTO_ROLLING_ENABLED] = true
            prefs[Keys.ROLLING_SPEED] = 1.0f
            prefs[Keys.DEPTH_3D_INTENSITY] = 0.85f
            prefs[Keys.BACKGROUND_PRESET] = LauncherBackgroundPreset.MIDNIGHT_COSMOS.name
            prefs[Keys.HAPTIC_FEEDBACK_ENABLED] = true
            prefs[Keys.NAVIGATION_MODE] = NavigationMode.GESTURE.name
            prefs[Keys.LIQUID_GLASS_ENABLED] = true
        }
    }

    companion object {
        const val DEFAULT_THEME_ID = "theme_free_obsidian_core"
        const val DEFAULT_WALLPAPER_ID = "wp_free_amoled_eclipse"
    }
}
