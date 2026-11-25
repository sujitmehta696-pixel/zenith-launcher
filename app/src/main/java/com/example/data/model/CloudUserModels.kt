package com.example.data.model

import androidx.annotation.Keep
import androidx.compose.runtime.Immutable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "OS Studio Member",
    val email: String? = null,
    val handle: String? = null,
    val premiumActive: Boolean = false,
    val subscriptionActive: Boolean = false,
    val planId: String? = null,
    val planName: String? = null,
    val planStartedAtMillis: Long? = null,
    val planExpiresAtMillis: Long? = null,
    val activeProductId: String? = null,
    val purchaseSource: String = PurchaseSource.NONE.name,
    val purchaseTokenHash: String? = null,
    val activeThemeId: String = "theme_free_obsidian_core",
    val activeWallpaperId: String = "wp_free_amoled_eclipse",
    val launcherMode: String = LauncherMode.SPINNER.name,
    val spinnerStyle: String = SpinnerStyle.ORBITAL_DIAL.name,
    val swipeDownAction: String = GestureAction.OPEN_CONTROL_CENTER.name,
    val swipeUpAction: String = GestureAction.OPEN_APP_DRAWER.name,
    val doubleTapAction: String = GestureAction.LOCK_SCREEN.name,
    val longPressAction: String = GestureAction.OPEN_STUDIO.name,
    val pinchAction: String = GestureAction.OPEN_EDGE_PANEL.name,
    val isActive: Boolean = true,
    val isBlocked: Boolean = false,
    val appVersion: String = "1.0.0",
    @ServerTimestamp val lastActiveAt: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun resolvedPurchaseSource(): PurchaseSource =
        PurchaseSource.entries.firstOrNull { it.name == purchaseSource } ?: PurchaseSource.NONE

    fun isSubscriptionCurrentlyActive(nowMs: Long = System.currentTimeMillis()): Boolean {
        val src = resolvedPurchaseSource()
        if (src == PurchaseSource.NONE || src == PurchaseSource.REWARDED_AD_PASS || src == PurchaseSource.DEBUG_DEV) {
            return false
        }
        val activeFlag = premiumActive || subscriptionActive
        if (!activeFlag) return false
        val expiresAt = planExpiresAtMillis
        return expiresAt == null || expiresAt <= 0L || expiresAt > nowMs
    }

    fun resolvedLauncherMode(): LauncherMode =
        LauncherMode.entries.firstOrNull { it.name == launcherMode } ?: LauncherMode.SPINNER

    fun resolvedSpinnerStyle(): SpinnerStyle =
        SpinnerStyle.entries.firstOrNull { it.name == spinnerStyle } ?: SpinnerStyle.ORBITAL_DIAL

    fun resolvedSwipeDown(): GestureAction =
        GestureAction.entries.firstOrNull { it.name == swipeDownAction } ?: GestureAction.OPEN_CONTROL_CENTER

    fun resolvedSwipeUp(): GestureAction =
        GestureAction.entries.firstOrNull { it.name == swipeUpAction } ?: GestureAction.OPEN_APP_DRAWER

    fun resolvedDoubleTap(): GestureAction =
        GestureAction.entries.firstOrNull { it.name == doubleTapAction } ?: GestureAction.LOCK_SCREEN

    fun resolvedLongPress(): GestureAction =
        GestureAction.entries.firstOrNull { it.name == longPressAction } ?: GestureAction.OPEN_STUDIO

    fun resolvedPinch(): GestureAction =
        GestureAction.entries.firstOrNull { it.name == pinchAction } ?: GestureAction.OPEN_EDGE_PANEL
}

@Keep
@IgnoreExtraProperties
data class PurchaseRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val productId: String = "",
    val purchaseSource: String = PurchaseSource.NONE.name,
    val purchaseTokenHash: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
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
    val usdToInrRate: Double = 85.0
)

@Keep
@Immutable
data class RemoteSubscriptionPlan(
    val planId: String = "",
    val planName: String = "",
    val subtitle: String = "",
    val priceInr: Double = 0.0,
    val validityDays: Int = 30,
    val badge: String = "",
    val sortOrder: Int = 0,
    val isActive: Boolean = true
)

@Keep
@Immutable
data class RemoteCategory(
    val categoryId: String = "",
    val name: String = "",
    val description: String = "",
    val sortOrder: Int = 0
)

sealed interface CloudUiState<out T> {
    data object Loading : CloudUiState<Nothing>
    data class Success<T>(val data: T) : CloudUiState<T>
    data class Error(val message: String) : CloudUiState<Nothing>
}
