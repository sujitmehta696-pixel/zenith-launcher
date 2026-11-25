package com.example.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.AppContainer
import com.example.data.ads.RewardedAdUiState
import com.example.data.billing.GatewayCheckoutSession
import com.example.data.catalog.FreeThemeCatalog
import com.example.data.model.AppearanceMode
import com.example.data.model.BillingState
import com.example.data.model.LauncherBackgroundPreset
import com.example.data.model.LauncherSettingsState
import com.example.data.model.NavigationMode
import com.example.data.model.PurchaseSource
import com.example.data.model.RemoteAppConfig
import com.example.data.model.RemoteSubscriptionPlan
import com.example.data.model.ThemeConfig
import com.example.data.model.WallpaperItem
import com.example.data.wallpaper.LiveWeatherSnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class GatewayCheckoutUiState(
    val isLoading: Boolean = false,
    val session: GatewayCheckoutSession? = null,
    val productId: String = "",
    val errorMessage: String? = null,
    val verificationMessage: String? = null,
    val isAutoChecking: Boolean = false
)

class MainViewModel(private val container: AppContainer) : ViewModel() {

    val settingsState: StateFlow<LauncherSettingsState> =
        container.preferencesDataStore.settingsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LauncherSettingsState()
        )

    val activeTheme: StateFlow<ThemeConfig> =
        container.themeRepository.activeTheme.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FreeThemeCatalog.items.first()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeWallpaper: StateFlow<WallpaperItem?> =
        container.preferencesDataStore.settingsFlow
            .flatMapLatest { prefs ->
                container.wallpaperRepository.observeWallpaper(prefs.activeWallpaperId)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val remoteConfig: StateFlow<RemoteAppConfig> =
        container.authRepository.authStateFlow()
            .flatMapLatest { user ->
                if (user == null) kotlinx.coroutines.flow.flowOf(RemoteAppConfig())
                else container.remoteContentRepository.observeRemoteAppConfig()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = RemoteAppConfig()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val remotePlans: StateFlow<List<RemoteSubscriptionPlan>> =
        container.authRepository.authStateFlow()
            .flatMapLatest { user ->
                if (user == null) kotlinx.coroutines.flow.flowOf(emptyList())
                else container.remoteContentRepository.observeSubscriptionPlans()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _weatherSnapshot = MutableStateFlow<LiveWeatherSnapshot?>(null)
    val weatherSnapshot: StateFlow<LiveWeatherSnapshot?> = _weatherSnapshot.asStateFlow()

    private val _dismissedAnnouncement = MutableStateFlow<String?>(null)
    val dismissedAnnouncement: StateFlow<String?> = _dismissedAnnouncement.asStateFlow()

    private val _gatewayCheckoutState = MutableStateFlow<GatewayCheckoutUiState?>(null)
    val gatewayCheckoutState: StateFlow<GatewayCheckoutUiState?> = _gatewayCheckoutState.asStateFlow()
    private var gatewayPollJob: Job? = null

    val billingState: StateFlow<BillingState> = container.billingManager.billingState
    val rewardedAdState: StateFlow<RewardedAdUiState> = container.adManager.rewardedState

    private val _adStatusMessage = MutableStateFlow<String?>(null)
    val adStatusMessage: StateFlow<String?> = _adStatusMessage.asStateFlow()

    val featuredFreeWallpapers: StateFlow<List<WallpaperItem>> =
        container.wallpaperRepository.observeFeaturedFree().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val featuredPremiumWallpapers: StateFlow<List<WallpaperItem>> =
        container.wallpaperRepository.observeFeaturedPremium().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val featuredFreeThemes: StateFlow<List<ThemeConfig>> =
        container.themeRepository.observeFeaturedFree().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val featuredPremiumThemes: StateFlow<List<ThemeConfig>> =
        container.themeRepository.observeFeaturedPremium().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            combine(remotePlans, remoteConfig) { plans, cfg -> plans to cfg }
                .collectLatest { (plans, cfg) ->
                    container.billingManager.updateRemotePlans(plans, cfg)
                }
        }
        viewModelScope.launch {
            _weatherSnapshot.value = container.weatherRepository.fetchCurrentWeather()
        }
    }

    fun refreshWeather() {
        viewModelScope.launch {
            _weatherSnapshot.value = container.weatherRepository.fetchCurrentWeather()
        }
    }

    fun dismissAnnouncement(text: String) {
        _dismissedAnnouncement.value = text
    }

    fun setAppearanceMode(mode: AppearanceMode) {
        viewModelScope.launch {
            container.preferencesDataStore.setAppearanceMode(mode)
        }
    }

    fun setReduceMotion(reduce: Boolean) {
        viewModelScope.launch {
            container.preferencesDataStore.setReduceMotion(reduce)
        }
    }

    fun setAutoCacheThumbnails(enabled: Boolean) {
        viewModelScope.launch {
            container.preferencesDataStore.setAutoCacheThumbnails(enabled)
        }
    }

    fun setWifiOnlyDownloads(enabled: Boolean) {
        viewModelScope.launch {
            container.preferencesDataStore.setWifiOnlyDownloads(enabled)
        }
    }

    fun setAutoRollingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            container.preferencesDataStore.setAutoRollingEnabled(enabled)
        }
    }

    fun setRollingSpeed(speed: Float) {
        viewModelScope.launch {
            container.preferencesDataStore.setRollingSpeed(speed)
        }
    }

    fun setDepth3DIntensity(depth: Float) {
        viewModelScope.launch {
            container.preferencesDataStore.setDepth3DIntensity(depth)
        }
    }

    fun setBackgroundPreset(preset: LauncherBackgroundPreset) {
        viewModelScope.launch {
            container.preferencesDataStore.setBackgroundPreset(preset)
        }
    }

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        viewModelScope.launch {
            container.preferencesDataStore.setHapticFeedbackEnabled(enabled)
        }
    }

    fun setNavigationMode(mode: NavigationMode) {
        viewModelScope.launch {
            container.preferencesDataStore.setNavigationMode(mode)
        }
    }

    fun setLiquidGlassEnabled(enabled: Boolean) {
        viewModelScope.launch {
            container.preferencesDataStore.setLiquidGlassEnabled(enabled)
        }
    }

    fun purchasePlan(activity: Activity, productId: String) {
        val cfg = remoteConfig.value
        if (cfg.paymentGatewayEnabled && cfg.paymentBackendUrl.isNotBlank()) {
            startGatewayCheckout(productId)
        } else {
            container.billingManager.launchPurchaseFlow(activity, productId)
        }
    }

    fun startGatewayCheckout(productId: String) {
        val cfg = remoteConfig.value
        val user = container.authRepository.currentUser()
        if (user == null) {
            _gatewayCheckoutState.value = GatewayCheckoutUiState(
                isLoading = false,
                productId = productId,
                errorMessage = "Please sign in with Google before starting payment."
            )
            return
        }
        val offer = billingState.value.offers.firstOrNull { it.productId == productId }
        val amount = offer?.numericAmount ?: "1499"

        gatewayPollJob?.cancel()
        viewModelScope.launch {
            _gatewayCheckoutState.value = GatewayCheckoutUiState(
                isLoading = true,
                productId = productId
            )
            val idToken = runCatching { user.getIdToken(false).await()?.token }.getOrNull()
                ?.takeIf { it.isNotBlank() }
                ?: runCatching { user.getIdToken(true).await()?.token }.getOrNull()
            val result = container.paymentGatewayClient.createCheckoutSession(
                config = cfg,
                userId = user.uid,
                userEmail = user.email,
                displayName = user.displayName,
                productId = productId,
                amount = amount,
                firebaseIdToken = idToken
            )
            result.onSuccess { session ->
                _gatewayCheckoutState.value = GatewayCheckoutUiState(
                    isLoading = false,
                    session = session,
                    productId = productId,
                    isAutoChecking = true,
                    verificationMessage = "Auto-checking UPI status every 3.5s…"
                )
                startAutoVerifyPolling(productId = productId, orderId = session.orderId)
            }.onFailure { err ->
                _gatewayCheckoutState.value = GatewayCheckoutUiState(
                    isLoading = false,
                    productId = productId,
                    errorMessage = err.localizedMessage ?: "Failed to create checkout session."
                )
            }
        }
    }

    private fun startAutoVerifyPolling(productId: String, orderId: String) {
        gatewayPollJob?.cancel()
        gatewayPollJob = viewModelScope.launch {
            while (isActive) {
                delay(3_500L)
                val activeState = _gatewayCheckoutState.value ?: break
                if (activeState.session?.orderId != orderId) break
                val user = container.authRepository.currentUser() ?: break
                val idToken = runCatching { user.getIdToken(false).await()?.token }.getOrNull()
                    ?.takeIf { it.isNotBlank() }
                    ?: runCatching { user.getIdToken(true).await()?.token }.getOrNull()
                val res = container.paymentGatewayClient.verifyPaymentStatus(
                    config = remoteConfig.value,
                    userId = user.uid,
                    orderId = orderId,
                    paymentId = null,
                    productId = productId,
                    firebaseIdToken = idToken
                )
                val verify = res.getOrNull() ?: continue
                if (verify.isPaidAndVerified && verify.transactionId.isNotBlank()) {
                    val resolvedPlanId = verify.planId ?: productId
                    val unlocked = container.billingManager.activateVerifiedGatewayPurchase(
                        productId = resolvedPlanId,
                        transactionId = verify.transactionId,
                        planName = verify.planName,
                        planStartedAtMillis = verify.planStartedAtMillis,
                        planExpiresAtMillis = verify.planExpiresAtMillis
                    )
                    if (unlocked) {
                        container.cloudAccountRepository.syncPremiumEntitlement(
                            productId = resolvedPlanId,
                            source = PurchaseSource.PAYMENT_GATEWAY,
                            rawPurchaseToken = verify.transactionId,
                            planName = verify.planName,
                            planStartedAtMillis = verify.planStartedAtMillis,
                            planExpiresAtMillis = verify.planExpiresAtMillis
                        )
                        _gatewayCheckoutState.value = null
                        break
                    }
                }
            }
        }
    }

    fun verifyAndCompleteGatewayCheckout(
        paymentIdFromGateway: String? = null,
        cloudProfileAlreadyPro: Boolean = false,
        planName: String? = null,
        planStartedAtMillis: Long? = null,
        planExpiresAtMillis: Long? = null
    ) {
        val current = _gatewayCheckoutState.value ?: return
        val session = current.session
        val productId = current.productId.ifBlank { BillingState.SKU_YEARLY }

        if (cloudProfileAlreadyPro) {
            val txId = paymentIdFromGateway?.takeIf { it.isNotBlank() } ?: (session?.orderId ?: "webhook_verified")
            if (container.billingManager.activateVerifiedGatewayPurchase(
                    productId = productId,
                    transactionId = txId,
                    planName = planName,
                    planStartedAtMillis = planStartedAtMillis,
                    planExpiresAtMillis = planExpiresAtMillis
                )
            ) {
                gatewayPollJob?.cancel()
                _gatewayCheckoutState.value = null
            }
            return
        }

        val user = container.authRepository.currentUser() ?: return
        val cfg = remoteConfig.value

        viewModelScope.launch {
            _gatewayCheckoutState.value = current.copy(
                isLoading = true,
                verificationMessage = "Checking real payment via FamGateway…"
            )
            val idToken = runCatching { user.getIdToken(false).await()?.token }.getOrNull()
            val res = container.paymentGatewayClient.verifyPaymentStatus(
                config = cfg,
                userId = user.uid,
                orderId = session?.orderId.orEmpty(),
                paymentId = paymentIdFromGateway,
                productId = productId,
                firebaseIdToken = idToken
            )
            res.onSuccess { verify ->
                if (verify.isPaidAndVerified && verify.transactionId.isNotBlank()) {
                    val resolvedPlanId = verify.planId ?: productId
                    val unlocked = container.billingManager.activateVerifiedGatewayPurchase(
                        productId = resolvedPlanId,
                        transactionId = verify.transactionId,
                        planName = verify.planName,
                        planStartedAtMillis = verify.planStartedAtMillis,
                        planExpiresAtMillis = verify.planExpiresAtMillis
                    )
                    if (unlocked) {
                        container.cloudAccountRepository.syncPremiumEntitlement(
                            productId = resolvedPlanId,
                            source = PurchaseSource.PAYMENT_GATEWAY,
                            rawPurchaseToken = verify.transactionId,
                            planName = verify.planName,
                            planStartedAtMillis = verify.planStartedAtMillis,
                            planExpiresAtMillis = verify.planExpiresAtMillis
                        )
                        gatewayPollJob?.cancel()
                        _gatewayCheckoutState.value = null
                    }
                } else {
                    _gatewayCheckoutState.value = current.copy(
                        isLoading = false,
                        isAutoChecking = true,
                        verificationMessage = verify.message
                    )
                }
            }.onFailure { err ->
                _gatewayCheckoutState.value = current.copy(
                    isLoading = false,
                    isAutoChecking = true,
                    verificationMessage = err.localizedMessage ?: "Verification pending. Pro stays locked until payment succeeds."
                )
            }
        }
    }

    fun dismissGatewayCheckout() {
        gatewayPollJob?.cancel()
        gatewayPollJob = null
        _gatewayCheckoutState.value = null
    }

    override fun onCleared() {
        gatewayPollJob?.cancel()
        super.onCleared()
    }

    fun restorePurchases() {
        container.billingManager.restorePurchases()
    }

    fun refreshPurchasesSilently() {
        container.billingManager.refreshPurchasesSilently()
    }

    fun clearAdStatusMessage() {
        _adStatusMessage.value = null
    }

    fun clearBillingStatus() {
        container.billingManager.clearStatusMessage()
    }

    fun resetAllCustomizations() {
        viewModelScope.launch {
            container.preferencesDataStore.resetAllCustomizations()
            container.launcherRepository.clearPinnedApps()
        }
    }

    fun getCacheSizeFormatted(): String = container.wallpaperRepository.getCacheSizeFormatted()

    fun clearWallpaperCache(onDone: () -> Unit) {
        viewModelScope.launch {
            container.wallpaperRepository.clearCache()
            onDone()
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(container) as T
                }
            }
    }
}
