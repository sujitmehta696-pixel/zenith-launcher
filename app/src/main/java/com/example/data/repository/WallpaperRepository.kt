package com.example.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.data.catalog.FreeWallpaperCatalog
import com.example.data.catalog.PremiumCatalog
import com.example.data.cloud.RemoteContentRepository
import com.example.data.local.PreferencesDataStore
import com.example.data.local.WallpaperDao
import com.example.data.local.WallpaperEntity
import com.example.data.model.CatalogManifest
import com.example.data.model.WallpaperCategory
import com.example.data.model.WallpaperItem
import com.example.data.wallpaper.WallpaperRenderer
import com.example.data.wallpaper.WallpaperTarget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.Locale

class WallpaperRepository(
    private val context: Context,
    private val wallpaperDao: WallpaperDao,
    private val preferencesDataStore: PreferencesDataStore,
    private val renderer: WallpaperRenderer,
    private val remoteContentRepository: RemoteContentRepository? = null
) {
    val manifest: CatalogManifest? by lazy { PremiumCatalog.loadManifest(context) }

    suspend fun ensureSeeded() {
        if (wallpaperDao.getCount() == 0) {
            val combined = (FreeWallpaperCatalog.items + PremiumCatalog.wallpaperItems)
                .map { WallpaperEntity.fromDomain(it) }
            wallpaperDao.insertAllIgnore(combined)
        }
    }

    fun observePublishedRemoteWallpapers(): Flow<List<WallpaperItem>> =
        remoteContentRepository?.observePublishedWallpapers() ?: flowOf(emptyList())

    fun queryWallpapers(
        category: WallpaperCategory,
        premiumFilter: Boolean?,
        searchQuery: String,
        limit: Int
    ): Flow<List<WallpaperItem>> {
        val catParam = if (category == WallpaperCategory.ALL) null else category.name
        val cleanQuery = searchQuery.trim()
        val localFlow = wallpaperDao.queryWallpapers(
            category = catParam,
            premiumFilter = premiumFilter,
            searchQuery = cleanQuery,
            limit = limit
        ).map { list -> list.map { it.toDomain() } }

        val remoteFlow = observePublishedRemoteWallpapers()
        return combine(localFlow, remoteFlow) { local, remote ->
            val filteredRemote = remote.filter { item ->
                val catOk = category == WallpaperCategory.ALL ||
                    item.category == category ||
                    item.categoryNameOverride.equals(category.displayName, ignoreCase = true)
                val tierOk = premiumFilter == null || item.isPremium == premiumFilter
                val queryOk = cleanQuery.isBlank() ||
                    item.name.contains(cleanQuery, ignoreCase = true) ||
                    item.displayCategoryLabel.contains(cleanQuery, ignoreCase = true) ||
                    item.tags.any { it.contains(cleanQuery, ignoreCase = true) }
                catOk && tierOk && queryOk
            }
            (filteredRemote + local).distinctBy { it.id }.take(limit.coerceAtLeast(20))
        }
    }

    fun countMatching(
        category: WallpaperCategory,
        premiumFilter: Boolean?,
        searchQuery: String
    ): Flow<Int> {
        val catParam = if (category == WallpaperCategory.ALL) null else category.name
        val cleanQuery = searchQuery.trim()
        val localCountFlow = wallpaperDao.countMatchingWallpapers(
            category = catParam,
            premiumFilter = premiumFilter,
            searchQuery = cleanQuery
        )
        return combine(localCountFlow, observePublishedRemoteWallpapers()) { localCount, remote ->
            val remoteCount = remote.count { item ->
                val catOk = category == WallpaperCategory.ALL ||
                    item.category == category ||
                    item.categoryNameOverride.equals(category.displayName, ignoreCase = true)
                val tierOk = premiumFilter == null || item.isPremium == premiumFilter
                val queryOk = cleanQuery.isBlank() ||
                    item.name.contains(cleanQuery, ignoreCase = true) ||
                    item.displayCategoryLabel.contains(cleanQuery, ignoreCase = true)
                catOk && tierOk && queryOk
            }
            localCount + remoteCount
        }
    }

    fun observeWallpaper(id: String): Flow<WallpaperItem?> {
        return combine(
            wallpaperDao.observeById(id).map { it?.toDomain() },
            observePublishedRemoteWallpapers()
        ) { local, remoteList ->
            remoteList.firstOrNull { it.id == id } ?: local
        }
    }

    fun observeFavorites(): Flow<List<WallpaperItem>> =
        wallpaperDao.observeFavorites().map { list -> list.map { it.toDomain() } }

    fun observeRecentlyViewed(): Flow<List<WallpaperItem>> =
        wallpaperDao.observeRecentlyViewed().map { list -> list.map { it.toDomain() } }

    fun observeDownloaded(): Flow<List<WallpaperItem>> =
        wallpaperDao.observeDownloaded().map { list -> list.map { it.toDomain() } }

    fun observeFeaturedFree(): Flow<List<WallpaperItem>> =
        combine(
            wallpaperDao.observeFeaturedFree().map { list -> list.map { it.toDomain() } },
            observePublishedRemoteWallpapers()
        ) { local, remote ->
            (remote.filter { !it.isPremium } + local).distinctBy { it.id }.take(8)
        }

    fun observeFeaturedPremium(): Flow<List<WallpaperItem>> =
        combine(
            wallpaperDao.observeFeaturedPremium().map { list -> list.map { it.toDomain() } },
            observePublishedRemoteWallpapers()
        ) { local, remote ->
            (remote.filter { it.isPremium } + local).distinctBy { it.id }.take(8)
        }

    suspend fun markViewed(id: String) {
        wallpaperDao.updateLastViewed(id, System.currentTimeMillis())
    }

    suspend fun toggleFavorite(id: String, current: Boolean) {
        wallpaperDao.updateFavorite(id, !current)
    }

    suspend fun downloadWallpaper(item: WallpaperItem): Result<String> {
        val prefs = preferencesDataStore.settingsFlow.first()
        if (prefs.wifiOnlyDownloads && !isOnWifi()) {
            return Result.failure(
                IllegalStateException("Wi-Fi Only Downloads is enabled in Settings. Connect to Wi-Fi or disable the restriction.")
            )
        }
        return if (item.isRemoteContent && remoteContentRepository != null) {
            remoteContentRepository.downloadRemoteWallpaper(item)
        } else {
            val result = renderer.downloadWallpaper(item)
            result.onSuccess { path ->
                wallpaperDao.updateDownloadStatus(item.id, true, path)
            }
            result
        }
    }

    suspend fun applyWallpaper(item: WallpaperItem, target: WallpaperTarget): Result<Unit> {
        val result = renderer.applySystemWallpaper(item, target)
        result.onSuccess {
            preferencesDataStore.setActiveWallpaperId(item.id)
            if (item.isRemoteContent) {
                remoteContentRepository?.incrementDownloadCount(item.id)
            }
        }
        return result
    }

    suspend fun prepareLiveVideoWallpaper(item: WallpaperItem): Result<File> {
        val repo = remoteContentRepository
            ?: return Result.failure(IllegalStateException("Remote repository unavailable."))
        val res = repo.prepareLiveVideoForWallpaper(item)
        if (res.isSuccess) {
            preferencesDataStore.setActiveWallpaperId(item.id)
        }
        return res
    }

    fun getCacheSizeFormatted(): String {
        val bytes = renderer.calculateCacheBytes()
        val mb = bytes / (1024.0 * 1024.0)
        return String.format(Locale.US, "%.2f MB", mb)
    }

    suspend fun clearCache() {
        renderer.clearCache()
        wallpaperDao.clearAllDownloadPaths()
    }

    private fun isOnWifi(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
}
