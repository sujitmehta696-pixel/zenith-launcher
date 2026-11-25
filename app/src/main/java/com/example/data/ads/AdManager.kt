package com.example.data.ads

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.example.R
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface RewardedAdUiState {
    data object Idle : RewardedAdUiState
    data object Loading : RewardedAdUiState
    data object Ready : RewardedAdUiState
    data object Showing : RewardedAdUiState
    data class Message(val text: String) : RewardedAdUiState
}

class AdManager(private val appContext: Context) {

    private val exceptionHandler = CoroutineExceptionHandler { _, t ->
        Log.w(TAG, "Ad background work safely caught error: ${t.message}")
    }
    private val adScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

    private val _rewardedState = MutableStateFlow<RewardedAdUiState>(RewardedAdUiState.Idle)
    val rewardedState: StateFlow<RewardedAdUiState> = _rewardedState.asStateFlow()

    @Volatile
    private var initialized = false

    @Volatile
    private var rewardedAd: RewardedAd? = null

    @Volatile
    private var isLoading = false

    fun initialize(context: Context = appContext) {
        if (initialized || isRobolectricEnvironment()) return
        val safeContext = context.applicationContext ?: context
        if (!isWebViewAvailable(safeContext)) {
            _rewardedState.value = RewardedAdUiState.Message("Ad component skipped on device without WebView.")
            return
        }
        initialized = true
        // Initialize MobileAds off the main UI thread to eliminate cold-start ANRs on OEM devices
        adScope.launch {
            runCatching {
                MobileAds.initialize(safeContext) {
                    adScope.launch {
                        withContext(Dispatchers.Main.immediate) {
                            preloadRewardedAd(safeContext)
                        }
                    }
                }
            }.onFailure { t ->
                Log.w(TAG, "MobileAds.initialize skipped safely: ${t.message}")
            }
        }
    }

    fun preloadRewardedAd(context: Context = appContext) {
        if (isLoading || rewardedAd != null || isRobolectricEnvironment()) return
        val safeContext = context.applicationContext ?: context
        if (!isWebViewAvailable(safeContext)) return

        isLoading = true
        _rewardedState.value = RewardedAdUiState.Loading

        try {
            val unitId = safeContext.getString(R.string.admob_rewarded_unit_id)
            val request = AdRequest.Builder().build()

            RewardedAd.load(
                safeContext,
                unitId,
                request,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isLoading = false
                        _rewardedState.value = RewardedAdUiState.Ready
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        rewardedAd = null
                        isLoading = false
                        _rewardedState.value = RewardedAdUiState.Message(
                            "Ad loading pending (${error.message}). Check internet or try again."
                        )
                    }
                }
            )
        } catch (t: Throwable) {
            isLoading = false
            _rewardedState.value = RewardedAdUiState.Message(
                t.localizedMessage ?: "Ad service unavailable on this device."
            )
        }
    }

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onStatusMessage: (String) -> Unit
    ) {
        if (activity.isFinishing || activity.isDestroyed) return
        val currentAd = rewardedAd
        if (currentAd == null) {
            onStatusMessage("Loading Rewarded Video Ad… Please tap again in a few seconds.")
            preloadRewardedAd(activity)
            return
        }

        _rewardedState.value = RewardedAdUiState.Showing
        try {
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    _rewardedState.value = RewardedAdUiState.Idle
                    preloadRewardedAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    _rewardedState.value = RewardedAdUiState.Message(adError.message)
                    onStatusMessage("Could not display ad: ${adError.message}")
                    preloadRewardedAd(activity)
                }
            }

            currentAd.show(activity) {
                onRewardEarned()
            }
        } catch (t: Throwable) {
            rewardedAd = null
            _rewardedState.value = RewardedAdUiState.Message(t.localizedMessage ?: "Unable to show ad")
            onStatusMessage("Could not display video ad right now. Please try again.")
        }
    }

    companion object {
        private const val TAG = "AdManager"

        fun isRobolectricEnvironment(): Boolean {
            return Build.FINGERPRINT.contains("robolectric", ignoreCase = true)
        }

        fun isWebViewAvailable(context: Context): Boolean {
            return runCatching {
                context.packageManager.hasSystemFeature(PackageManager.FEATURE_WEBVIEW)
            }.getOrDefault(false)
        }
    }
}
