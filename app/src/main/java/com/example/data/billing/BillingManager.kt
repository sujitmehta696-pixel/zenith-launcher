package com.example.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.example.data.local.PreferencesDataStore
import com.example.data.model.BillingConnectionStatus
import com.example.data.model.BillingState
import com.example.data.model.PremiumProductOffer
import com.example.data.model.PurchaseSource
import com.example.data.model.RemoteAppConfig
import com.example.data.model.RemoteSubscriptionPlan
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Enforces strict real-payment Pro activation:
 * Pro stays locked until a verified Google Play purchase, a backend-verified Payment Gateway
 * transaction (PAYMENT_GATEWAY), or an explicit Admin Cloud Grant (ADMIN_GRANT).
 * Debug toggles and free/rewarded bypasses never activate Pro.
 */
class BillingManager(
    private val context: Context,
    @Suppress("UNUSED_PARAMETER") private val preferencesDataStore: PreferencesDataStore
) : PurchasesUpdatedListener {

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.w("BillingManager", "Billing coroutine safely recovered", throwable)
    }
    @Suppress("unused")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate + exceptionHandler)

    private val _billingState = MutableStateFlow(BillingState())
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    private val productDetailsMap = mutableMapOf<String, ProductDetails>()
    private var playPurchaseVerified = false
    private var playActiveProductId: String? = null
    private var playPurchaseToken: String? = null

    private var gatewayPurchaseVerified = false
    private var gatewayActiveProductId: String? = null
    private var gatewayTransactionToken: String? = null
    private var gatewayActivePlanName: String? = null
    private var gatewayStartedAtMillis: Long? = null
    private var gatewayExpiresAtMillis: Long? = null

    private var cloudPurchaseVerified = false
    private var cloudActiveProductId: String? = null
    private var cloudPurchaseSource: PurchaseSource = PurchaseSource.NONE
    private var cloudActivePlanName: String? = null
    private var cloudStartedAtMillis: Long? = null
    private var cloudExpiresAtMillis: Long? = null

    private val billingClient: BillingClient? = try {
        BillingClient.newBuilder(context.applicationContext ?: context)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .enablePrepaidPlans()
                    .build()
            )
            .build()
    } catch (_: Throwable) {
        null
    }

    init {
        connect()
    }

    fun connect() {
        val client = billingClient ?: run {
            _billingState.update {
                it.copy(connectionStatus = BillingConnectionStatus.UNAVAILABLE, statusMessage = null)
            }
            return
        }
        val ready = runCatching { client.isReady }.getOrDefault(false)
        if (ready) {
            queryProductsAndPurchases()
            return
        }
        _billingState.update {
            it.copy(connectionStatus = BillingConnectionStatus.CONNECTING, statusMessage = null)
        }
        try {
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    try {
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            _billingState.update {
                                it.copy(connectionStatus = BillingConnectionStatus.CONNECTED)
                            }
                            queryProductsAndPurchases()
                        } else {
                            _billingState.update {
                                it.copy(
                                    connectionStatus = BillingConnectionStatus.UNAVAILABLE,
                                    statusMessage = null
                                )
                            }
                        }
                    } catch (_: Throwable) {
                        _billingState.update {
                            it.copy(connectionStatus = BillingConnectionStatus.UNAVAILABLE, statusMessage = null)
                        }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    _billingState.update {
                        it.copy(connectionStatus = BillingConnectionStatus.DISCONNECTED)
                    }
                }
            })
        } catch (_: Throwable) {
            _billingState.update {
                it.copy(connectionStatus = BillingConnectionStatus.UNAVAILABLE, statusMessage = null)
            }
        }
    }

    private fun queryProductsAndPurchases() {
        querySubscriptionProducts()
        queryInAppProducts()
        restorePurchases()
    }

    private fun querySubscriptionProducts() {
        val client = billingClient ?: return
        try {
            val subsList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(BillingState.SKU_YEARLY)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build(),
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(BillingState.SKU_MONTHLY)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
            val params = QueryProductDetailsParams.newBuilder().setProductList(subsList).build()
            client.queryProductDetailsAsync(params) { result, detailsList ->
                runCatching {
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        detailsList.forEach { details ->
                            productDetailsMap[details.productId] = details
                        }
                        refreshOfferPrices()
                    }
                }
            }
        } catch (_: Throwable) {
        }
    }

    private fun queryInAppProducts() {
        val client = billingClient ?: return
        try {
            val inAppList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(BillingState.SKU_LIFETIME)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
            val params = QueryProductDetailsParams.newBuilder().setProductList(inAppList).build()
            client.queryProductDetailsAsync(params) { result, detailsList ->
                runCatching {
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        detailsList.forEach { details ->
                            productDetailsMap[details.productId] = details
                        }
                        refreshOfferPrices()
                    }
                }
            }
        } catch (_: Throwable) {
        }
    }

    fun updateRemoteGatewayPricing(config: RemoteAppConfig) {
        updateRemotePlans(emptyList(), config)
    }

    fun updateRemotePlans(plans: List<RemoteSubscriptionPlan>, config: RemoteAppConfig) {
        val inrFormat = NumberFormat.getIntegerInstance(Locale("en", "IN"))
        fun formatInr(amount: Double): String {
            return if (amount % 1.0 == 0.0) {
                "₹${inrFormat.format(amount.toLong())}"
            } else {
                "₹${String.format(Locale.US, "%.2f", amount)}"
            }
        }
        fun amountString(amount: Double): String {
            return if (amount % 1.0 == 0.0) amount.toLong().toString()
            else String.format(Locale.US, "%.2f", amount)
        }
        fun validityLabel(days: Int): String = when {
            days >= 36500 -> "Lifetime ($days Days)"
            days == 365 -> "365 Days (1 Year)"
            days == 30 -> "30 Days (1 Month)"
            else -> "$days Days Validity"
        }

        _billingState.update { state ->
            val updatedOffers = if (plans.isNotEmpty()) {
                plans.map { plan ->
                    val existing = state.offers.firstOrNull { it.productId == plan.planId }
                    val priceText = "${formatInr(plan.priceInr)} / ${plan.validityDays}d"
                    PremiumProductOffer(
                        productId = plan.planId,
                        title = plan.planName,
                        subtitle = plan.subtitle.ifBlank { "Full Pro 3D Sphere, Liquid Glass & Cloud Wallpapers" },
                        fallbackPrice = if (plan.validityDays >= 36500) "${formatInr(plan.priceInr)} one-time" else priceText,
                        numericAmount = amountString(plan.priceInr),
                        billingPeriodLabel = validityLabel(plan.validityDays),
                        isSubscription = plan.validityDays < 36500,
                        badge = plan.badge.takeIf { it.isNotBlank() },
                        formattedPlayPrice = existing?.formattedPlayPrice,
                        isPlayProductLoaded = existing?.isPlayProductLoaded ?: false,
                        validityDays = plan.validityDays,
                        priceInr = plan.priceInr
                    )
                }
            } else {
                BillingState.defaultOffers().map { defaultOffer ->
                    val existing = state.offers.firstOrNull { it.productId == defaultOffer.productId }
                    val merged = when (defaultOffer.productId) {
                        BillingState.SKU_MONTHLY -> {
                            val amt = config.monthlyAmount.toDoubleOrNull() ?: defaultOffer.priceInr
                            defaultOffer.copy(
                                fallbackPrice = config.monthlyPriceLabel.ifBlank { "${formatInr(amt)} / month" },
                                numericAmount = config.monthlyAmount.ifBlank { amountString(amt) },
                                priceInr = amt
                            )
                        }
                        BillingState.SKU_YEARLY -> {
                            val amt = config.yearlyAmount.toDoubleOrNull() ?: defaultOffer.priceInr
                            defaultOffer.copy(
                                fallbackPrice = config.yearlyPriceLabel.ifBlank { "${formatInr(amt)} / year" },
                                numericAmount = config.yearlyAmount.ifBlank { amountString(amt) },
                                priceInr = amt
                            )
                        }
                        BillingState.SKU_LIFETIME -> {
                            val amt = config.lifetimeAmount.toDoubleOrNull() ?: defaultOffer.priceInr
                            defaultOffer.copy(
                                fallbackPrice = config.lifetimePriceLabel.ifBlank { "${formatInr(amt)} one-time" },
                                numericAmount = config.lifetimeAmount.ifBlank { amountString(amt) },
                                priceInr = amt
                            )
                        }
                        else -> defaultOffer
                    }
                    merged.copy(
                        formattedPlayPrice = existing?.formattedPlayPrice,
                        isPlayProductLoaded = existing?.isPlayProductLoaded ?: false
                    )
                }
            }
            state.copy(offers = updatedOffers)
        }
    }

    private fun refreshOfferPrices() {
        _billingState.update { state ->
            val updatedOffers = state.offers.map { offer ->
                val details = productDetailsMap[offer.productId]
                if (details == null) {
                    offer
                } else {
                    val formattedPrice = if (offer.isSubscription) {
                        details.subscriptionOfferDetails
                            ?.firstOrNull()
                            ?.pricingPhases
                            ?.pricingPhaseList
                            ?.firstOrNull()
                            ?.formattedPrice
                    } else {
                        details.oneTimePurchaseOfferDetails?.formattedPrice
                    }
                    offer.copy(
                        formattedPlayPrice = formattedPrice,
                        isPlayProductLoaded = formattedPrice != null
                    )
                }
            }
            state.copy(offers = updatedOffers)
        }
    }

    fun launchPurchaseFlow(activity: Activity, productId: String) {
        val client = billingClient
        if (client == null || !runCatching { client.isReady }.getOrDefault(false)) {
            _billingState.update {
                it.copy(
                    statusMessage = "Store billing service is unavailable on this device. Please configure or use the In-App Payment Gateway."
                )
            }
            connect()
            return
        }

        val details = productDetailsMap[productId]
        if (details == null) {
            _billingState.update {
                it.copy(
                    statusMessage = "Store SKU is syncing. Complete checkout via the In-App Payment Gateway when enabled."
                )
            }
            return
        }

        try {
            val productParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)

            if (details.productType == BillingClient.ProductType.SUBS) {
                val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
                if (offerToken.isNullOrBlank()) {
                    _billingState.update {
                        it.copy(statusMessage = "No active subscription offer token configured for $productId.")
                    }
                    return
                }
                productParamsBuilder.setOfferToken(offerToken)
            }

            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productParamsBuilder.build()))
                .build()

            _billingState.update { it.copy(isProcessingPurchase = true, statusMessage = null) }
            val result = client.launchBillingFlow(activity, flowParams)
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                _billingState.update {
                    it.copy(
                        isProcessingPurchase = false,
                        statusMessage = "Purchase flow could not start: ${result.debugMessage}"
                    )
                }
            }
        } catch (t: Throwable) {
            _billingState.update {
                it.copy(
                    isProcessingPurchase = false,
                    statusMessage = t.localizedMessage ?: "Purchase flow unavailable on this device."
                )
            }
        }
    }

    /**
     * Activates Pro ONLY after the backend/webhook verifies real payment completion
     * with a genuine transaction ID. Never unlocks on empty/unverified results.
     */
    fun activateVerifiedGatewayPurchase(
        productId: String,
        transactionId: String,
        planName: String? = null,
        planStartedAtMillis: Long? = null,
        planExpiresAtMillis: Long? = null
    ): Boolean {
        val cleanTx = transactionId.trim()
        if (cleanTx.isBlank()) {
            _billingState.update {
                it.copy(
                    isProcessingPurchase = false,
                    statusMessage = "Payment not verified yet. Pro remains locked until real payment succeeds."
                )
            }
            return false
        }
        val cleanProductId = productId.ifBlank { BillingState.SKU_YEARLY }
        val matchingOffer = _billingState.value.offers.firstOrNull { it.productId == cleanProductId }
        val resolvedStart = planStartedAtMillis ?: System.currentTimeMillis()
        val resolvedExpiry = planExpiresAtMillis ?: (
            resolvedStart + (matchingOffer?.validityDays ?: 365).coerceIn(1, 36500) * 86_400_000L
        )

        gatewayPurchaseVerified = true
        gatewayActiveProductId = cleanProductId
        gatewayTransactionToken = cleanTx
        gatewayActivePlanName = planName?.takeIf { it.isNotBlank() } ?: matchingOffer?.title ?: "Pro Subscription"
        gatewayStartedAtMillis = resolvedStart
        gatewayExpiresAtMillis = resolvedExpiry
        _billingState.update {
            it.copy(
                isProcessingPurchase = false,
                statusMessage = "Payment verified ($cleanTx)! ${gatewayActivePlanName} unlocked."
            )
        }
        recomputeEntitlement()
        return true
    }

    fun restorePurchases() {
        val client = billingClient
        if (client == null || !runCatching { client.isReady }.getOrDefault(false)) {
            _billingState.update {
                it.copy(
                    statusMessage = "Checking Cloud Vault for verified Pro purchases..."
                )
            }
            recomputeEntitlement()
            connect()
            return
        }

        _billingState.update { it.copy(isProcessingPurchase = true, statusMessage = "Checking Pro purchases…") }
        var verifiedId: String? = null
        var verifiedToken: String? = null

        try {
            val subsParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
            client.queryPurchasesAsync(subsParams) { subsResult, subsPurchases ->
                runCatching {
                    if (subsResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        subsPurchases.firstOrNull { isValidPurchased(it) }?.let { purchase ->
                            acknowledgeIfNeeded(purchase)
                            verifiedId = purchase.products.firstOrNull()
                            verifiedToken = purchase.purchaseToken
                        }
                    }

                    val inAppParams = QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                    client.queryPurchasesAsync(inAppParams) { inAppResult, inAppPurchases ->
                        runCatching {
                            if (verifiedId == null && inAppResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                inAppPurchases.firstOrNull { isValidPurchased(it) }?.let { purchase ->
                                    acknowledgeIfNeeded(purchase)
                                    verifiedId = purchase.products.firstOrNull()
                                    verifiedToken = purchase.purchaseToken
                                }
                            }

                            playPurchaseVerified = verifiedId != null
                            playActiveProductId = verifiedId
                            playPurchaseToken = verifiedToken
                            _billingState.update {
                                it.copy(
                                    isProcessingPurchase = false,
                                    statusMessage = if (verifiedId != null || cloudPurchaseVerified || gatewayPurchaseVerified) {
                                        "Restored verified Pro entitlement."
                                    } else {
                                        "No verified purchases found for this account."
                                    }
                                )
                            }
                            recomputeEntitlement()
                        }.onFailure {
                            _billingState.update { it.copy(isProcessingPurchase = false) }
                        }
                    }
                }.onFailure {
                    _billingState.update { it.copy(isProcessingPurchase = false) }
                }
            }
        } catch (_: Throwable) {
            _billingState.update {
                it.copy(
                    isProcessingPurchase = false,
                    statusMessage = "Store billing check unavailable on this device."
                )
            }
        }
    }

    fun refreshPurchasesSilently() {
        val client = billingClient
        if (client == null || !runCatching { client.isReady }.getOrDefault(false)) {
            recomputeEntitlement()
            return
        }
        var verifiedId: String? = null
        var verifiedToken: String? = null

        try {
            val subsParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
            client.queryPurchasesAsync(subsParams) { subsResult, subsPurchases ->
                runCatching {
                    if (subsResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        subsPurchases.firstOrNull { isValidPurchased(it) }?.let { purchase ->
                            acknowledgeIfNeeded(purchase)
                            verifiedId = purchase.products.firstOrNull()
                            verifiedToken = purchase.purchaseToken
                        }
                    }
                    val inAppParams = QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                    client.queryPurchasesAsync(inAppParams) { inAppResult, inAppPurchases ->
                        runCatching {
                            if (verifiedId == null && inAppResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                inAppPurchases.firstOrNull { isValidPurchased(it) }?.let { purchase ->
                                    acknowledgeIfNeeded(purchase)
                                    verifiedId = purchase.products.firstOrNull()
                                    verifiedToken = purchase.purchaseToken
                                }
                            }
                            if (verifiedId != null) {
                                playPurchaseVerified = true
                                playActiveProductId = verifiedId
                                playPurchaseToken = verifiedToken
                                recomputeEntitlement()
                            }
                        }
                    }
                }
            }
        } catch (_: Throwable) {
            recomputeEntitlement()
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        runCatching {
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    val valid = purchases.orEmpty().firstOrNull { isValidPurchased(it) }
                    if (valid != null) {
                        acknowledgeIfNeeded(valid)
                        playPurchaseVerified = true
                        playActiveProductId = valid.products.firstOrNull()
                        playPurchaseToken = valid.purchaseToken
                        _billingState.update {
                            it.copy(
                                isProcessingPurchase = false,
                                statusMessage = "Pro unlocked & syncing to your Cloud Vault!"
                            )
                        }
                        recomputeEntitlement()
                    } else {
                        _billingState.update {
                            it.copy(
                                isProcessingPurchase = false,
                                statusMessage = "Purchase is pending payment verification."
                            )
                        }
                    }
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    _billingState.update {
                        it.copy(
                            isProcessingPurchase = false,
                            statusMessage = "Purchase cancelled."
                        )
                    }
                }
                else -> {
                    _billingState.update {
                        it.copy(
                            isProcessingPurchase = false,
                            statusMessage = "Purchase error (${billingResult.responseCode}): ${billingResult.debugMessage}"
                        )
                    }
                }
            }
        }.onFailure {
            _billingState.update { it.copy(isProcessingPurchase = false) }
        }
    }

    private fun isValidPurchased(purchase: Purchase): Boolean {
        return purchase.purchaseState == Purchase.PurchaseState.PURCHASED
    }

    private fun acknowledgeIfNeeded(purchase: Purchase) {
        val client = billingClient ?: return
        if (!purchase.isAcknowledged) {
            runCatching {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                client.acknowledgePurchase(params) { }
            }
        }
    }

    fun applyCloudEntitlement(
        isPremium: Boolean,
        productId: String?,
        source: PurchaseSource,
        planName: String? = null,
        planStartedAtMillis: Long? = null,
        planExpiresAtMillis: Long? = null
    ) {
        val validCloudSource = when (source) {
            PurchaseSource.GOOGLE_PLAY,
            PurchaseSource.PAYMENT_GATEWAY,
            PurchaseSource.ADMIN_GRANT,
            PurchaseSource.CLOUD_ACCOUNT -> source
            PurchaseSource.NONE,
            PurchaseSource.DEBUG_DEV,
            PurchaseSource.REWARDED_AD_PASS -> PurchaseSource.NONE
        }
        val notExpired = planExpiresAtMillis == null || planExpiresAtMillis <= 0L || planExpiresAtMillis > System.currentTimeMillis()
        cloudPurchaseVerified = isPremium && validCloudSource != PurchaseSource.NONE && notExpired
        cloudActiveProductId = if (cloudPurchaseVerified) productId else null
        cloudPurchaseSource = if (cloudPurchaseVerified) validCloudSource else PurchaseSource.NONE
        cloudActivePlanName = if (cloudPurchaseVerified) planName else null
        cloudStartedAtMillis = if (cloudPurchaseVerified) planStartedAtMillis else null
        cloudExpiresAtMillis = if (cloudPurchaseVerified) planExpiresAtMillis else null
        recomputeEntitlement()
    }

    fun clearCloudEntitlement() {
        cloudPurchaseVerified = false
        cloudActiveProductId = null
        cloudPurchaseSource = PurchaseSource.NONE
        cloudActivePlanName = null
        cloudStartedAtMillis = null
        cloudExpiresAtMillis = null
        gatewayPurchaseVerified = false
        gatewayActiveProductId = null
        gatewayTransactionToken = null
        gatewayActivePlanName = null
        gatewayStartedAtMillis = null
        gatewayExpiresAtMillis = null
        recomputeEntitlement()
    }

    fun clearStatusMessage() {
        _billingState.update { it.copy(statusMessage = null) }
    }

    private fun recomputeEntitlement() {
        val gatewayStillActive = gatewayPurchaseVerified && (
            gatewayExpiresAtMillis == null || gatewayExpiresAtMillis!! <= 0L || gatewayExpiresAtMillis!! > System.currentTimeMillis()
        )
        val cloudStillActive = cloudPurchaseVerified && cloudPurchaseSource != PurchaseSource.NONE && (
            cloudExpiresAtMillis == null || cloudExpiresAtMillis!! <= 0L || cloudExpiresAtMillis!! > System.currentTimeMillis()
        )
        val source = when {
            playPurchaseVerified -> PurchaseSource.GOOGLE_PLAY
            gatewayStillActive -> PurchaseSource.PAYMENT_GATEWAY
            cloudStillActive -> cloudPurchaseSource
            else -> PurchaseSource.NONE
        }
        val activeId = when (source) {
            PurchaseSource.GOOGLE_PLAY -> playActiveProductId
            PurchaseSource.PAYMENT_GATEWAY -> gatewayActiveProductId
            PurchaseSource.ADMIN_GRANT,
            PurchaseSource.CLOUD_ACCOUNT -> cloudActiveProductId
            PurchaseSource.NONE,
            PurchaseSource.REWARDED_AD_PASS,
            PurchaseSource.DEBUG_DEV -> null
        }
        val planName = when (source) {
            PurchaseSource.PAYMENT_GATEWAY -> gatewayActivePlanName
            PurchaseSource.ADMIN_GRANT,
            PurchaseSource.CLOUD_ACCOUNT -> cloudActivePlanName
            PurchaseSource.GOOGLE_PLAY -> _billingState.value.offers.firstOrNull { it.productId == activeId }?.title
            else -> null
        }
        val startedAt = when (source) {
            PurchaseSource.PAYMENT_GATEWAY -> gatewayStartedAtMillis
            PurchaseSource.ADMIN_GRANT,
            PurchaseSource.CLOUD_ACCOUNT -> cloudStartedAtMillis
            else -> null
        }
        val expiresAt = when (source) {
            PurchaseSource.PAYMENT_GATEWAY -> gatewayExpiresAtMillis
            PurchaseSource.ADMIN_GRANT,
            PurchaseSource.CLOUD_ACCOUNT -> cloudExpiresAtMillis
            else -> null
        }
        val token = when (source) {
            PurchaseSource.GOOGLE_PLAY -> playPurchaseToken
            PurchaseSource.PAYMENT_GATEWAY -> gatewayTransactionToken
            else -> null
        }
        _billingState.update {
            it.copy(
                isPremium = source != PurchaseSource.NONE,
                activeProductId = activeId,
                activePlanName = planName,
                planStartedAtMillis = startedAt,
                planExpiresAtMillis = expiresAt,
                purchaseSource = source,
                lastPurchaseToken = token,
                rewardedTrialExpiresAtMs = null,
                debugOverrideEnabled = false
            )
        }
    }
}
