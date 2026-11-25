package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.AppContainer
import com.example.data.model.CloudUiState
import com.example.data.model.PurchaseRecord
import com.example.data.model.PurchaseSource
import com.example.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CloudAccountViewModel(
    private val container: AppContainer,
    private val currentUserId: String,
    initialCustomName: String?,
    initialCustomHandle: String?
) : ViewModel() {

    private var hasAppliedInitialCloudState = false
    private var lastSyncedProductKey: String? = null

    val profileState: StateFlow<CloudUiState<UserProfile?>> =
        container.cloudAccountRepository.observeUserProfile(currentUserId)
            .map<UserProfile?, CloudUiState<UserProfile?>> { profile ->
                if (profile != null) {
                    applyCloudProfileToLocal(profile)
                }
                CloudUiState.Success(profile)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing user profile", rootError)
                emit(CloudUiState.Error(rootError.message ?: "Failed to sync account profile"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = CloudUiState.Loading
            )

    val purchaseHistoryState: StateFlow<CloudUiState<List<PurchaseRecord>>> =
        container.cloudAccountRepository.observePurchaseHistory(currentUserId)
            .map<List<PurchaseRecord>, CloudUiState<List<PurchaseRecord>>> { CloudUiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing purchase history", rootError)
                emit(CloudUiState.Error(rootError.message ?: "Failed to load purchase history"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = CloudUiState.Loading
            )

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus.asStateFlow()

    init {
        viewModelScope.launch {
            val user = container.authRepository.currentUser()
            if (user != null && user.uid == currentUserId) {
                val initialSettings = container.preferencesDataStore.settingsFlow.first()
                container.cloudAccountRepository.ensureOrRegisterProfile(
                    user = user,
                    customDisplayName = initialCustomName,
                    customHandle = initialCustomHandle,
                    initialSettings = initialSettings
                ).onFailure { err ->
                    _syncStatus.value = err.localizedMessage ?: "Could not initialize cloud profile"
                }
            }
        }

        // Automatically upload verified local Play or Gateway purchases to Firestore
        viewModelScope.launch {
            container.billingManager.billingState
                .map {
                    Triple(
                        Triple(it.isPremium, it.activeProductId, it.purchaseSource),
                        Triple(it.activePlanName, it.planStartedAtMillis, it.planExpiresAtMillis),
                        it.lastPurchaseToken
                    )
                }
                .distinctUntilChanged()
                .collect { (entitlement, planMeta, token) ->
                    val (isPro, productId, source) = entitlement
                    val (planName, startedAt, expiresAt) = planMeta
                    if (isPro && (source == PurchaseSource.GOOGLE_PLAY || source == PurchaseSource.PAYMENT_GATEWAY)) {
                        val key = "${productId}_${source.name}_${expiresAt ?: 0L}"
                        if (lastSyncedProductKey != key) {
                            lastSyncedProductKey = key
                            container.cloudAccountRepository.syncPremiumEntitlement(
                                productId = productId ?: "android_launcher_pro_yearly",
                                source = source,
                                rawPurchaseToken = token,
                                planName = planName,
                                planStartedAtMillis = startedAt,
                                planExpiresAtMillis = expiresAt
                            )
                        }
                    }
                }
        }
    }

    private suspend fun applyCloudProfileToLocal(profile: UserProfile) {
        val effectivePro = profile.isSubscriptionCurrentlyActive()
        container.billingManager.applyCloudEntitlement(
            isPremium = effectivePro,
            productId = profile.activeProductId ?: profile.planId,
            source = profile.resolvedPurchaseSource(),
            planName = profile.planName,
            planStartedAtMillis = profile.planStartedAtMillis,
            planExpiresAtMillis = profile.planExpiresAtMillis
        )
        if (!hasAppliedInitialCloudState) {
            hasAppliedInitialCloudState = true
            container.preferencesDataStore.applyCloudSyncedSettings(
                activeThemeId = profile.activeThemeId,
                activeWallpaperId = profile.activeWallpaperId,
                launcherMode = profile.resolvedLauncherMode(),
                spinnerStyle = profile.resolvedSpinnerStyle(),
                swipeDownAction = profile.resolvedSwipeDown(),
                swipeUpAction = profile.resolvedSwipeUp(),
                doubleTapAction = profile.resolvedDoubleTap(),
                longPressAction = profile.resolvedLongPress(),
                pinchAction = profile.resolvedPinch()
            )
        }
    }

    fun syncCurrentCustomizationToCloud() {
        viewModelScope.launch {
            val settings = container.preferencesDataStore.settingsFlow.first()
            container.cloudAccountRepository.syncCustomizationSettings(settings)
                .onSuccess { _syncStatus.value = "Theme, gestures & launcher setup synced to cloud" }
                .onFailure { err -> _syncStatus.value = err.localizedMessage ?: "Cloud sync failed" }
        }
    }

    suspend fun syncSettingsInBackground(settings: com.example.data.model.LauncherSettingsState) {
        val currentProfile = (profileState.value as? CloudUiState.Success)?.data ?: return
        val alreadyMatches =
            currentProfile.activeThemeId == settings.activeThemeId &&
                currentProfile.activeWallpaperId == settings.activeWallpaperId &&
                currentProfile.launcherMode == settings.launcherMode.name &&
                currentProfile.spinnerStyle == settings.spinnerStyle.name &&
                currentProfile.swipeDownAction == settings.swipeDownAction.name &&
                currentProfile.swipeUpAction == settings.swipeUpAction.name &&
                currentProfile.doubleTapAction == settings.doubleTapAction.name &&
                currentProfile.longPressAction == settings.longPressAction.name &&
                currentProfile.pinchAction == settings.pinchAction.name
        if (!alreadyMatches) {
            container.cloudAccountRepository.syncCustomizationSettings(settings)
        }
    }

    fun clearSyncStatus() {
        _syncStatus.value = null
    }

    companion object {
        private const val TAG = "CloudAccountVM"
        private const val STOP_TIMEOUT_MILLIS = 5000L

        fun factory(
            container: AppContainer,
            currentUserId: String,
            customName: String?,
            customHandle: String?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CloudAccountViewModel(
                    container = container,
                    currentUserId = currentUserId,
                    initialCustomName = customName,
                    initialCustomHandle = customHandle
                ) as T
            }
        }
    }
}
