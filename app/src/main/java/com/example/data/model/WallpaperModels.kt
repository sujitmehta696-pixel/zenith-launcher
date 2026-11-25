package com.example.data.model

import kotlinx.serialization.Serializable

enum class WallpaperCategory(val displayName: String) {
    ALL("All"),
    AMOLED("AMOLED"),
    NATURE("Nature"),
    ABSTRACT("Abstract"),
    MINIMAL("Minimal"),
    GRADIENT("Gradient"),
    SPACE("Space"),
    ARCHITECTURE("Architecture"),
    TECHNOLOGY("Technology"),
    ARTISTIC("Artistic"),
    CINEMATIC("Cinematic"),
    LUXURY("Luxury"),
    EXCLUSIVE("Exclusive")
}

enum class WallpaperSourceType {
    BUNDLED_RASTER,
    PROCEDURAL_STUDIO,
    REMOTE_IMAGE,
    REMOTE_VIDEO
}

enum class ProceduralStyle {
    ECLIPSE_HALO,
    LIQUID_WAVES,
    ALPINE_PEAKS,
    NEBULA_SPIRAL,
    BAUHAUS_MONO,
    CYBER_GRID,
    AURORA_RIBBONS,
    ARCHITECTURAL_ARCH,
    ORBITAL_RINGS,
    PRISM_SHARDS,
    TOPOGRAPHIC_LINES,
    CELLULAR_GLASS
}

data class WallpaperItem(
    val id: String,
    val name: String,
    val category: WallpaperCategory,
    val collection: String,
    val isPremium: Boolean,
    val resolution: String,
    val fileSizeLabel: String,
    val tags: List<String>,
    val license: String,
    val sourceType: WallpaperSourceType,
    val drawableResName: String?,
    val paletteHexes: List<Long>,
    val proceduralStyle: ProceduralStyle,
    val seed: Long,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val localFilePath: String? = null,
    val lastViewedAt: Long = 0L,
    val sortOrder: Int = 0,
    val remoteMediaUrl: String? = null,
    val remoteThumbnailUrl: String? = null,
    val categoryNameOverride: String? = null,
    val downloadCount: Long = 0L,
    val updatedAtMillis: Long = 0L
) {
    val isRemoteVideo: Boolean
        get() = sourceType == WallpaperSourceType.REMOTE_VIDEO

    val isRemoteContent: Boolean
        get() = sourceType == WallpaperSourceType.REMOTE_IMAGE || sourceType == WallpaperSourceType.REMOTE_VIDEO

    val displayCategoryLabel: String
        get() = categoryNameOverride?.takeIf { it.isNotBlank() } ?: category.displayName
}

@Serializable
data class CatalogCollectionManifest(
    val id: String,
    val title: String,
    val tier: String,
    val targetCount: Int
)

@Serializable
data class CatalogManifest(
    val schemaVersion: Int,
    val catalogName: String,
    val freeWallpapersBundled: Int,
    val freeThemesBundled: Int,
    val premiumWallpapersBundledStarter: Int,
    val premiumThemesBundledStarter: Int,
    val premiumWallpapersTargetCapacity: Int,
    val premiumThemesTargetCapacity: Int,
    val paginationPageSize: Int,
    val licenseNotice: String,
    val collections: List<CatalogCollectionManifest>
)
