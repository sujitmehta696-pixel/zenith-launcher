package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.ProceduralStyle
import com.example.data.model.ThemeCardStyle
import com.example.data.model.ThemeCategory
import com.example.data.model.ThemeConfig
import com.example.data.model.ThemeFontStyle
import com.example.data.model.ThemeIconStyle
import com.example.data.model.WallpaperCategory
import com.example.data.model.WallpaperItem
import com.example.data.model.WallpaperSourceType

@Entity(
    tableName = "wallpapers",
    indices = [
        Index(value = ["category"]),
        Index(value = ["isPremium"]),
        Index(value = ["isFavorite"]),
        Index(value = ["sortOrder"])
    ]
)
data class WallpaperEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val collection: String,
    val isPremium: Boolean,
    val resolution: String,
    val fileSizeLabel: String,
    val tagsJoined: String,
    val license: String,
    val sourceType: String,
    val drawableResName: String?,
    val paletteJoined: String,
    val proceduralStyle: String,
    val seed: Long,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val localFilePath: String? = null,
    val lastViewedAt: Long = 0L,
    val sortOrder: Int = 0
) {
    fun toDomain(): WallpaperItem = WallpaperItem(
        id = id,
        name = name,
        category = runCatching { WallpaperCategory.valueOf(category) }.getOrDefault(WallpaperCategory.ABSTRACT),
        collection = collection,
        isPremium = isPremium,
        resolution = resolution,
        fileSizeLabel = fileSizeLabel,
        tags = tagsJoined.split("|").filter { it.isNotBlank() },
        license = license,
        sourceType = runCatching { WallpaperSourceType.valueOf(sourceType) }.getOrDefault(WallpaperSourceType.PROCEDURAL_STUDIO),
        drawableResName = drawableResName,
        paletteHexes = paletteJoined.split(",").mapNotNull { it.trim().toLongOrNull() },
        proceduralStyle = runCatching { ProceduralStyle.valueOf(proceduralStyle) }.getOrDefault(ProceduralStyle.LIQUID_WAVES),
        seed = seed,
        isFavorite = isFavorite,
        isDownloaded = isDownloaded,
        localFilePath = localFilePath,
        lastViewedAt = lastViewedAt,
        sortOrder = sortOrder
    )

    companion object {
        fun fromDomain(item: WallpaperItem): WallpaperEntity = WallpaperEntity(
            id = item.id,
            name = item.name,
            category = item.category.name,
            collection = item.collection,
            isPremium = item.isPremium,
            resolution = item.resolution,
            fileSizeLabel = item.fileSizeLabel,
            tagsJoined = item.tags.joinToString("|"),
            license = item.license,
            sourceType = item.sourceType.name,
            drawableResName = item.drawableResName,
            paletteJoined = item.paletteHexes.joinToString(","),
            proceduralStyle = item.proceduralStyle.name,
            seed = item.seed,
            isFavorite = item.isFavorite,
            isDownloaded = item.isDownloaded,
            localFilePath = item.localFilePath,
            lastViewedAt = item.lastViewedAt,
            sortOrder = item.sortOrder
        )
    }
}

@Entity(
    tableName = "themes",
    indices = [
        Index(value = ["category"]),
        Index(value = ["isPremium"]),
        Index(value = ["isFavorite"]),
        Index(value = ["sortOrder"])
    ]
)
data class ThemeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: String,
    val isPremium: Boolean,
    val isDarkTheme: Boolean,
    val backgroundHex: Long,
    val surfaceHex: Long,
    val primaryHex: Long,
    val secondaryHex: Long,
    val accentHex: Long,
    val textHex: Long,
    val cardStyle: String,
    val iconStyle: String,
    val fontStyle: String,
    val cardRadiusDp: Int,
    val associatedWallpaperId: String?,
    val isFavorite: Boolean = false,
    val sortOrder: Int = 0
) {
    fun toDomain(): ThemeConfig = ThemeConfig(
        id = id,
        name = name,
        description = description,
        category = runCatching { ThemeCategory.valueOf(category) }.getOrDefault(ThemeCategory.DARK),
        isPremium = isPremium,
        isDarkTheme = isDarkTheme,
        backgroundHex = backgroundHex,
        surfaceHex = surfaceHex,
        primaryHex = primaryHex,
        secondaryHex = secondaryHex,
        accentHex = accentHex,
        textHex = textHex,
        cardStyle = runCatching { ThemeCardStyle.valueOf(cardStyle) }.getOrDefault(ThemeCardStyle.GLASSMORPHIC),
        iconStyle = runCatching { ThemeIconStyle.valueOf(iconStyle) }.getOrDefault(ThemeIconStyle.SQUIRCLE),
        fontStyle = runCatching { ThemeFontStyle.valueOf(fontStyle) }.getOrDefault(ThemeFontStyle.PLUS_JAKARTA_SANS),
        cardRadiusDp = cardRadiusDp,
        associatedWallpaperId = associatedWallpaperId,
        isFavorite = isFavorite,
        sortOrder = sortOrder
    )

    companion object {
        fun fromDomain(theme: ThemeConfig): ThemeEntity = ThemeEntity(
            id = theme.id,
            name = theme.name,
            description = theme.description,
            category = theme.category.name,
            isPremium = theme.isPremium,
            isDarkTheme = theme.isDarkTheme,
            backgroundHex = theme.backgroundHex,
            surfaceHex = theme.surfaceHex,
            primaryHex = theme.primaryHex,
            secondaryHex = theme.secondaryHex,
            accentHex = theme.accentHex,
            textHex = theme.textHex,
            cardStyle = theme.cardStyle.name,
            iconStyle = theme.iconStyle.name,
            fontStyle = theme.fontStyle.name,
            cardRadiusDp = theme.cardRadiusDp,
            associatedWallpaperId = theme.associatedWallpaperId,
            isFavorite = theme.isFavorite,
            sortOrder = theme.sortOrder
        )
    }
}

@Entity(tableName = "favorite_apps")
data class FavoriteAppEntity(
    @PrimaryKey val packageName: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "hidden_apps")
data class HiddenAppEntity(
    @PrimaryKey val packageName: String,
    val hiddenAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "pinned_home_apps")
data class PinnedHomeAppEntity(
    @PrimaryKey val packageName: String,
    val pageIndex: Int = 0,
    val slotIndex: Int = 0
)

@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey val packageName: String,
    val lastLaunchTimestamp: Long = 0L,
    val launchCount: Int = 0
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val query: String,
    val timestamp: Long = System.currentTimeMillis()
)
