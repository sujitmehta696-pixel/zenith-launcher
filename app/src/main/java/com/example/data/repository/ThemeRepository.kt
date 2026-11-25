package com.example.data.repository

import com.example.data.catalog.FreeThemeCatalog
import com.example.data.catalog.PremiumCatalog
import com.example.data.local.PreferencesDataStore
import com.example.data.local.ThemeDao
import com.example.data.local.ThemeEntity
import com.example.data.model.ThemeCategory
import com.example.data.model.ThemeConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class ThemeRepository(
    private val themeDao: ThemeDao,
    private val preferencesDataStore: PreferencesDataStore
) {
    suspend fun ensureSeeded() {
        if (themeDao.getCount() == 0) {
            val combined = (FreeThemeCatalog.items + PremiumCatalog.themeItems)
                .map { ThemeEntity.fromDomain(it) }
            themeDao.insertAllIgnore(combined)
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeTheme: Flow<ThemeConfig> = preferencesDataStore.settingsFlow
        .flatMapLatest { prefs ->
            themeDao.observeById(prefs.activeThemeId).map { entity ->
                entity?.toDomain() ?: FreeThemeCatalog.items.first()
            }
        }

    fun queryThemes(
        category: ThemeCategory,
        premiumFilter: Boolean?,
        searchQuery: String,
        limit: Int
    ): Flow<List<ThemeConfig>> {
        val catParam = if (category == ThemeCategory.ALL) null else category.name
        return themeDao.queryThemes(
            category = catParam,
            premiumFilter = premiumFilter,
            searchQuery = searchQuery.trim(),
            limit = limit
        ).map { list -> list.map { it.toDomain() } }
    }

    fun countMatching(
        category: ThemeCategory,
        premiumFilter: Boolean?,
        searchQuery: String
    ): Flow<Int> {
        val catParam = if (category == ThemeCategory.ALL) null else category.name
        return themeDao.countMatchingThemes(
            category = catParam,
            premiumFilter = premiumFilter,
            searchQuery = searchQuery.trim()
        )
    }

    fun observeTheme(id: String): Flow<ThemeConfig?> =
        themeDao.observeById(id).map { it?.toDomain() }

    fun observeFavorites(): Flow<List<ThemeConfig>> =
        themeDao.observeFavorites().map { list -> list.map { it.toDomain() } }

    fun observeFeaturedFree(): Flow<List<ThemeConfig>> =
        themeDao.observeFeaturedFree().map { list -> list.map { it.toDomain() } }

    fun observeFeaturedPremium(): Flow<List<ThemeConfig>> =
        themeDao.observeFeaturedPremium().map { list -> list.map { it.toDomain() } }

    suspend fun applyTheme(themeId: String) {
        preferencesDataStore.setActiveThemeId(themeId)
    }

    suspend fun resetToDefaultTheme() {
        preferencesDataStore.setActiveThemeId(PreferencesDataStore.DEFAULT_THEME_ID)
    }

    suspend fun toggleFavorite(id: String, current: Boolean) {
        themeDao.updateFavorite(id, !current)
    }
}
