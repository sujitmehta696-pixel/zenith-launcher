package com.example.data.model

enum class LauncherMode(val displayName: String, val description: String) {
    SPINNER("3D Orbit", "360° rolling 3D app globe"),
    GRID("Grid", "Multi-page icon grid"),
    MINIMAL("Minimal", "Clean typographic focus list")
}

enum class SpinnerStyle(
    val displayName: String,
    val isPremium: Boolean,
    val description: String
) {
    ORBITAL_DIAL("3D Orbital", false, "Multi-layer neon cyan & royal purple sphere"),
    CHRONOS_RING("Chronos 3D", false, "Precision chronometer rings with depth tilt"),
    CYBER_ARC("Cyber Vortex", true, "Segmented electric blue 3D holographic cage"),
    LUXURY_HALO("Violet Halo", true, "Luminous soft-violet 3D orbital halo"),
    PRO_3D_SPHERE("3D Pro Sphere", true, "Full 360° tornado cloud of ALL installed apps")
}

enum class LauncherBackgroundPreset(val displayName: String, val accentHex: Long, val secondaryHex: Long) {
    MIDNIGHT_COSMOS("Midnight Cosmos", 0xFF713BFF, 0xFF00E5FF),
    LIQUID_GLASS("Liquid Glass", 0xFF38BDF8, 0xFFA855F7),
    ROYAL_NEBULA("Royal Nebula", 0xFF9D50FF, 0xFF168BFF),
    ELECTRIC_AURORA("Electric Aurora", 0xFF168BFF, 0xFF00E5FF),
    PURE_OBSIDIAN("Pure Midnight", 0xFF080B18, 0xFF1E293B)
}

enum class AppearanceMode(val displayName: String) {
    SYSTEM("System"),
    DARK("Dark"),
    LIGHT("Light")
}

enum class AppSortOrder(val displayName: String) {
    ALPHABETICAL("A–Z"),
    RECENTLY_USED("Recent"),
    MOST_USED("Frequent")
}

enum class NavigationMode(val displayName: String, val description: String) {
    GESTURE("Gesture Navigation", "Swipe between pages with frosted gesture pill"),
    BUTTONS("Navigation Buttons", "Classic Back, Home & Hub buttons with tab bar")
}

enum class GestureAction(val displayName: String, val description: String) {
    NONE("None", "No action"),
    OPEN_NOTIFICATIONS("Notifications", "Expand notification shade"),
    OPEN_QUICK_SETTINGS("Quick Settings", "Open system quick panel"),
    OPEN_CONTROL_CENTER("Control Center", "Open animated Launcher Control Center"),
    OPEN_EDGE_PANEL("Edge Panel", "Open slide-out quick shortcuts panel"),
    LOCK_SCREEN("Lock Screen", "Lock display via Accessibility"),
    OPEN_APP_DRAWER("App Drawer", "Open all apps drawer"),
    OPEN_SEARCH("AI & App Search", "Open AI + phone content + app search"),
    SPIN_WHEEL("3D Spin Pulse", "Impulse rotate 3D launcher"),
    OPEN_STUDIO("Launcher Studio", "Open customization hub")
}

data class InstalledAppInfo(
    val packageName: String,
    val activityName: String,
    val label: String,
    val isSystemApp: Boolean,
    val isFavorite: Boolean = false,
    val isPinnedHome: Boolean = false,
    val homePage: Int = 0,
    val homeSlot: Int = -1,
    val isHidden: Boolean = false,
    val lastLaunchTimestamp: Long = 0L,
    val launchCount: Int = 0
)

data class LauncherSettingsState(
    val launcherMode: LauncherMode = LauncherMode.SPINNER,
    val spinnerStyle: SpinnerStyle = SpinnerStyle.ORBITAL_DIAL,
    val navigationMode: NavigationMode = NavigationMode.GESTURE,
    val gridColumns: Int = 4,
    val gridRows: Int = 5,
    val iconSizeDp: Int = 56,
    val showIconLabels: Boolean = true,
    val appearanceMode: AppearanceMode = AppearanceMode.DARK,
    val reduceMotion: Boolean = false,
    val autoCacheThumbnails: Boolean = true,
    val wifiOnlyDownloads: Boolean = false,
    val activeThemeId: String = "theme_free_obsidian_core",
    val activeWallpaperId: String = "wp_free_amoled_eclipse",
    val swipeDownAction: GestureAction = GestureAction.OPEN_CONTROL_CENTER,
    val swipeUpAction: GestureAction = GestureAction.OPEN_APP_DRAWER,
    val doubleTapAction: GestureAction = GestureAction.LOCK_SCREEN,
    val longPressAction: GestureAction = GestureAction.OPEN_STUDIO,
    val pinchAction: GestureAction = GestureAction.OPEN_EDGE_PANEL,
    val iconStyleOverride: ThemeIconStyle? = null,
    val themeAppIcons: Boolean = true,
    val themeWidgets: Boolean = true,
    val syncThemeWallpaper: Boolean = true,
    val liquidGlassEnabled: Boolean = true,
    // 3D Rolling Engine Controls
    val autoRollingEnabled: Boolean = true,
    val rollingSpeed: Float = 1.0f,
    val depth3DIntensity: Float = 0.85f,
    val backgroundPreset: LauncherBackgroundPreset = LauncherBackgroundPreset.MIDNIGHT_COSMOS,
    val hapticFeedbackEnabled: Boolean = true
)
