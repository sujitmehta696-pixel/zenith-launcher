package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object LauncherRoute

@Serializable
data object WallpapersRoute

@Serializable
data object ThemesRoute

@Serializable
data object PremiumRoute

@Serializable
data object SettingsRoute

@Serializable
data object SearchRoute

@Serializable
data object FavoritesRoute

@Serializable
data class WallpaperDetailRoute(val wallpaperId: String)

@Serializable
data class ThemeDetailRoute(val themeId: String)

@Serializable
data class LegalRoute(val docType: String) // "privacy" or "terms"

@Serializable
data object AccountRoute

@Serializable
data object GestureConfigRoute
