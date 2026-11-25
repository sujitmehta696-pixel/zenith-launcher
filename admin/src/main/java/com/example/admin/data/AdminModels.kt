package com.example.admin.data

import androidx.annotation.Keep
import androidx.compose.runtime.Immutable

@Keep
@Immutable
data class AdminSession(
    val uid: String,
    val email: String,
    val displayName: String,
    val role: String
)

@Keep
@Immutable
data class CategoryItem(
    val categoryId: String,
    val name: String,
    val description: String,
    val sortOrder: Int,
    val isActive: Boolean,
    val updatedAtMillis: Long
)

@Keep
@Immutable
data class RemoteWallpaperItem(
    val wallpaperId: String,
    val title: String,
    val description: String,
    val categoryId: String,
    val categoryName: String,
    val mediaType: String, // "IMAGE" or "VIDEO"
    val mediaUrl: String,
    val thumbnailUrl: String,
    val storagePath: String,
    val thumbnailStoragePath: String?,
    val resolutionLabel: String,
    val fileSizeBytes: Long,
    val isPremium: Boolean,
    val isPublished: Boolean,
    val downloadCount: Long,
    val uploadedByUid: String,
    val updatedAtMillis: Long
)

@Keep
@Immutable
data class UserControlItem(
    val userId: String,
    val displayName: String,
    val email: String,
    val premiumActive: Boolean,
    val purchaseSource: String,
    val isActive: Boolean,
    val isBlocked: Boolean,
    val activeThemeId: String,
    val activeWallpaperId: String,
    val spinnerStyle: String,
    val lastActiveMillis: Long,
    val subscriptionActive: Boolean = false,
    val planName: String = "",
    val planExpiresAtMillis: Long = 0L
)

@Keep
@Immutable
data class AdminRoleItem(
    val adminUid: String,
    val email: String,
    val displayName: String,
    val role: String,
    val addedByUid: String
)

@Keep
@Immutable
data class RemoteAppConfig(
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String = "Scheduled maintenance is in progress.",
    val announcementBanner: String = "",
    val aiSearchEnabled: Boolean = true,
    val liveWallpapersEnabled: Boolean = true,
    val edgePanelEnabled: Boolean = true,
    val controlCenterEnabled: Boolean = true,
    val notificationBadgesEnabled: Boolean = true,
    val paymentGatewayEnabled: Boolean = false,
    val paymentProviderName: String = "FamGateway v2.0",
    val paymentBackendUrl: String = "",
    val paymentVerifyUrl: String = "",
    val paymentPublicKey: String = "",
    val paymentCurrency: String = "INR",
    val monthlyPriceLabel: String = "₹199 / month",
    val yearlyPriceLabel: String = "₹1,499 / year",
    val lifetimePriceLabel: String = "₹3,999 one-time",
    val monthlyAmount: String = "199",
    val yearlyAmount: String = "1499",
    val lifetimeAmount: String = "3999",
    val usdToInrRate: Double = 85.0,
    val updatedByUid: String = "",
    val updatedAtMillis: Long = 0L
)

@Keep
@Immutable
data class SubscriptionPlanItem(
    val planId: String = "",
    val planName: String = "",
    val subtitle: String = "",
    val priceInr: Double = 199.0,
    val validityDays: Int = 30,
    val badge: String = "",
    val sortOrder: Int = 1,
    val isActive: Boolean = true,
    val updatedAtMillis: Long = 0L
)

@Keep
@Immutable
data class PaymentGatewaySecrets(
    val providerName: String = "Custom Webhook",
    val secretApiKey: String = "",
    val webhookSecret: String = "",
    val updatedByUid: String = "",
    val updatedAtMillis: Long = 0L
)

@Keep
@Immutable
data class DashboardMetrics(
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
    val totalWallpapers: Int = 0,
    val totalLiveVideos: Int = 0,
    val totalDownloads: Long = 0L
)

val DEFAULT_LAUNCHER_CATEGORIES: List<Triple<String, String, String>> = listOf(
    Triple("cat_nature", "Nature", "Scenic landscapes, forests, and oceans"),
    Triple("cat_abstract", "Abstract", "3D fluid glass, waves, and geometric art"),
    Triple("cat_amoled", "AMOLED", "True-black battery-saving neon backdrops"),
    Triple("cat_cars", "Cars", "Supercars, motorsport, and night rides"),
    Triple("cat_anime", "Anime", "Cyberpunk anime and illustrated worlds"),
    Triple("cat_space", "Space", "Nebulae, orbital planets, and deep cosmos"),
    Triple("cat_3d", "3D", "Hyper-real 3D renders and depth scenes"),
    Triple("cat_live", "Live Wallpapers", "MP4 looping motion wallpapers"),
    Triple("cat_premium", "Premium Wallpapers", "Exclusive Pro 4K & Studio collections")
)
