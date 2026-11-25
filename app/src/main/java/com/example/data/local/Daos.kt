package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WallpaperDao {
    @Query("SELECT COUNT(*) FROM wallpapers")
    suspend fun getCount(): Int

    @Query("SELECT * FROM wallpapers ORDER BY sortOrder ASC LIMIT :limit OFFSET :offset")
    fun getWallpapersPaged(limit: Int, offset: Int): Flow<List<WallpaperEntity>>

    @Query(
        """
        SELECT * FROM wallpapers
        WHERE (:category IS NULL OR category = :category)
          AND (:premiumFilter IS NULL OR isPremium = :premiumFilter)
          AND (:searchQuery = '' OR name LIKE '%' || :searchQuery || '%' OR tagsJoined LIKE '%' || :searchQuery || '%' OR collection LIKE '%' || :searchQuery || '%')
        ORDER BY sortOrder ASC
        LIMIT :limit
        """
    )
    fun queryWallpapers(
        category: String?,
        premiumFilter: Boolean?,
        searchQuery: String,
        limit: Int
    ): Flow<List<WallpaperEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM wallpapers
        WHERE (:category IS NULL OR category = :category)
          AND (:premiumFilter IS NULL OR isPremium = :premiumFilter)
          AND (:searchQuery = '' OR name LIKE '%' || :searchQuery || '%' OR tagsJoined LIKE '%' || :searchQuery || '%' OR collection LIKE '%' || :searchQuery || '%')
        """
    )
    fun countMatchingWallpapers(
        category: String?,
        premiumFilter: Boolean?,
        searchQuery: String
    ): Flow<Int>

    @Query("SELECT * FROM wallpapers WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<WallpaperEntity?>

    @Query("SELECT * FROM wallpapers WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): WallpaperEntity?

    @Query("SELECT * FROM wallpapers WHERE isFavorite = 1 ORDER BY sortOrder ASC")
    fun observeFavorites(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE lastViewedAt > 0 ORDER BY lastViewedAt DESC LIMIT 12")
    fun observeRecentlyViewed(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE isDownloaded = 1 ORDER BY sortOrder ASC")
    fun observeDownloaded(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE isPremium = 0 ORDER BY sortOrder ASC LIMIT 10")
    fun observeFeaturedFree(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE isPremium = 1 ORDER BY sortOrder ASC LIMIT 10")
    fun observeFeaturedPremium(): Flow<List<WallpaperEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(items: List<WallpaperEntity>)

    @Query("UPDATE wallpapers SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE wallpapers SET lastViewedAt = :timestamp WHERE id = :id")
    suspend fun updateLastViewed(id: String, timestamp: Long)

    @Query("UPDATE wallpapers SET isDownloaded = :downloaded, localFilePath = :filePath WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, downloaded: Boolean, filePath: String?)

    @Query("UPDATE wallpapers SET isDownloaded = 0, localFilePath = NULL")
    suspend fun clearAllDownloadPaths()
}

@Dao
interface ThemeDao {
    @Query("SELECT COUNT(*) FROM themes")
    suspend fun getCount(): Int

    @Query(
        """
        SELECT * FROM themes
        WHERE (:category IS NULL OR category = :category)
          AND (:premiumFilter IS NULL OR isPremium = :premiumFilter)
          AND (:searchQuery = '' OR name LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%' OR category LIKE '%' || :searchQuery || '%')
        ORDER BY sortOrder ASC
        LIMIT :limit
        """
    )
    fun queryThemes(
        category: String?,
        premiumFilter: Boolean?,
        searchQuery: String,
        limit: Int
    ): Flow<List<ThemeEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM themes
        WHERE (:category IS NULL OR category = :category)
          AND (:premiumFilter IS NULL OR isPremium = :premiumFilter)
          AND (:searchQuery = '' OR name LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%' OR category LIKE '%' || :searchQuery || '%')
        """
    )
    fun countMatchingThemes(
        category: String?,
        premiumFilter: Boolean?,
        searchQuery: String
    ): Flow<Int>

    @Query("SELECT * FROM themes WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<ThemeEntity?>

    @Query("SELECT * FROM themes WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ThemeEntity?

    @Query("SELECT * FROM themes WHERE isFavorite = 1 ORDER BY sortOrder ASC")
    fun observeFavorites(): Flow<List<ThemeEntity>>

    @Query("SELECT * FROM themes WHERE isPremium = 0 ORDER BY sortOrder ASC LIMIT 10")
    fun observeFeaturedFree(): Flow<List<ThemeEntity>>

    @Query("SELECT * FROM themes WHERE isPremium = 1 ORDER BY sortOrder ASC LIMIT 10")
    fun observeFeaturedPremium(): Flow<List<ThemeEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(items: List<ThemeEntity>)

    @Query("UPDATE themes SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)
}

@Dao
interface LauncherMetaDao {
    @Query("SELECT * FROM favorite_apps ORDER BY addedAt DESC")
    fun observeFavoriteApps(): Flow<List<FavoriteAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavoriteApp(entity: FavoriteAppEntity)

    @Query("DELETE FROM favorite_apps WHERE packageName = :packageName")
    suspend fun removeFavoriteApp(packageName: String)

    @Query("SELECT * FROM hidden_apps")
    fun observeHiddenApps(): Flow<List<HiddenAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun hideApp(entity: HiddenAppEntity)

    @Query("DELETE FROM hidden_apps WHERE packageName = :packageName")
    suspend fun unhideApp(packageName: String)

    @Query("SELECT * FROM pinned_home_apps ORDER BY pageIndex ASC, slotIndex ASC")
    fun observePinnedHomeApps(): Flow<List<PinnedHomeAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun pinApp(entity: PinnedHomeAppEntity)

    @Query("DELETE FROM pinned_home_apps WHERE packageName = :packageName")
    suspend fun unpinApp(packageName: String)

    @Query("DELETE FROM pinned_home_apps")
    suspend fun clearPinnedApps()

    @Query("SELECT * FROM app_usage")
    fun observeAppUsage(): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage WHERE packageName = :packageName LIMIT 1")
    suspend fun getAppUsage(packageName: String): AppUsageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAppUsage(entity: AppUsageEntity)

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 12")
    fun observeSearchHistory(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSearchQuery(entity: SearchHistoryEntity)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()
}
