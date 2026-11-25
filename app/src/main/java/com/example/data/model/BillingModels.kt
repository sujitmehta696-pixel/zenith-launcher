package com.example.data.model

enum class BillingConnectionStatus(val label: String) {
    DISCONNECTED("Not Connected"),
    CONNECTING("Connecting to Store Billing…"),
    CONNECTED("Store Billing Ready"),
    UNAVAILABLE("Store Billing Unavailable")
}

enum class PurchaseSource(val label: String) {
    NONE("Free Plan"),
    GOOGLE_PLAY("Play Billing Verified"),
    PAYMENT_GATEWAY("Gateway Payment Verified"),
    ADMIN_GRANT("Admin Pro Grant"),
    CLOUD_ACCOUNT("Synced Cloud Account"),
    REWARDED_AD_PASS("Rewarded Pass (Disabled)"),
    DEBUG_DEV("Debug Preview (Disabled)")
}

data class PremiumProductOffer(
    val productId: String,
    val title: String,
    val subtitle: String,
    val fallbackPrice: String,
    val numericAmount: String,
    val billingPeriodLabel: String,
    val isSubscription: Boolean,
    val badge: String? = null,
    val formattedPlayPrice: String? = null,
    val isPlayProductLoaded: Boolean = false,
    val validityDays: Int = 365,
    val priceInr: Double = 1499.0
)

data class BillingState(
    val isPremium: Boolean = false,
    val activeProductId: String? = null,
    val activePlanName: String? = null,
    val planStartedAtMillis: Long? = null,
    val planExpiresAtMillis: Long? = null,
    val purchaseSource: PurchaseSource = PurchaseSource.NONE,
    val lastPurchaseToken: String? = null,
    val connectionStatus: BillingConnectionStatus = BillingConnectionStatus.DISCONNECTED,
    val offers: List<PremiumProductOffer> = defaultOffers(),
    val isProcessingPurchase: Boolean = false,
    val statusMessage: String? = null,
    val rewardedTrialExpiresAtMs: Long? = null,
    val debugOverrideEnabled: Boolean = false
) {
    companion object {
        const val SKU_MONTHLY = "android_launcher_pro_monthly"
        const val SKU_YEARLY = "android_launcher_pro_yearly"
        const val SKU_LIFETIME = "android_launcher_pro_lifetime"

        fun defaultOffers(): List<PremiumProductOffer> = listOf(
            PremiumProductOffer(
                productId = SKU_YEARLY,
                title = "Pro Annual",
                subtitle = "Full access to Pro 3D Sphere, 4K live videos & Edge Panel",
                fallbackPrice = "₹1,499 / year",
                numericAmount = "1499",
                billingPeriodLabel = "365 Days Validity",
                isSubscription = true,
                badge = "Best Value",
                validityDays = 365,
                priceInr = 1499.0
            ),
            PremiumProductOffer(
                productId = SKU_MONTHLY,
                title = "Pro Monthly",
                subtitle = "Flexible monthly access to all Pro themes & styles",
                fallbackPrice = "₹199 / month",
                numericAmount = "199",
                billingPeriodLabel = "30 Days Validity",
                isSubscription = true,
                validityDays = 30,
                priceInr = 199.0
            ),
            PremiumProductOffer(
                productId = SKU_LIFETIME,
                title = "Studio Lifetime",
                subtitle = "One-time payment with permanent Pro entitlement",
                fallbackPrice = "₹3,999 one-time",
                numericAmount = "3999",
                billingPeriodLabel = "Lifetime (36,500 Days)",
                isSubscription = false,
                badge = "Forever",
                validityDays = 36500,
                priceInr = 3999.0
            )
        )
    }
}
