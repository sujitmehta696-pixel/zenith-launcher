package com.example

import android.app.Application
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import androidx.activity.ComponentActivity
import androidx.test.core.app.ApplicationProvider
import com.example.data.ads.AdManager
import com.example.data.billing.BillingManager
import com.example.data.cloud.AuthRepository
import com.example.data.cloud.CloudAccountRepository
import com.example.data.cloud.GoogleSignInResult
import com.example.data.local.PreferencesDataStore
import com.example.data.model.LauncherSettingsState
import com.example.data.model.PurchaseSource
import com.example.ui.components.rasterizeAppIconSafely
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DeviceCrashResilienceTest {

    @Test
    fun `auth and cloud repositories degrade cleanly when Firebase is unavailable on OEM device`() = runBlocking(Dispatchers.IO) {
        val authRepo = AuthRepository { null }
        val cloudRepo = CloudAccountRepository(dbProvider = { null }, authProvider = { null })

        assertNull(authRepo.currentUser())
        assertNull(authRepo.authStateFlow().first())
        assertNull(cloudRepo.observeUserProfile("missing_uid").first())
        assertTrue(cloudRepo.observePurchaseHistory("missing_uid").first().isEmpty())

        val syncRes = cloudRepo.syncCustomizationSettings(LauncherSettingsState())
        assertTrue(syncRes.isFailure)

        val billingRes = cloudRepo.syncPremiumEntitlement(
            productId = "android_launcher_pro_yearly",
            source = PurchaseSource.GOOGLE_PLAY,
            rawPurchaseToken = "token"
        )
        assertTrue(billingRes.isFailure)

        val appContext = ApplicationProvider.getApplicationContext<Application>()
        assertFalse(authRepo.attemptAutoSignIn(appContext))
        val signInRes = authRepo.signInWithGoogle(appContext)
        assertTrue(signInRes is GoogleSignInResult.Error)
    }

    @Test
    fun `ad manager and billing manager recover safely without throwing when store or webview missing`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val prefs = PreferencesDataStore(app)
        val adManager = AdManager(app)
        val billingManager = BillingManager(app, prefs)

        val activity = Robolectric.buildActivity(ComponentActivity::class.java).create().get()
        adManager.initialize(activity)
        adManager.preloadRewardedAd(activity)
        adManager.showRewardedAd(
            activity = activity,
            onRewardEarned = {},
            onStatusMessage = {}
        )

        billingManager.refreshPurchasesSilently()
        billingManager.restorePurchases()
        billingManager.launchPurchaseFlow(activity, "android_launcher_pro_yearly")
        assertFalse(billingManager.billingState.value.isProcessingPurchase)
    }

    @Test
    fun `rasterizeAppIconSafely catches faulty OEM drawables without crashing UI`() {
        val faultyDrawable = object : Drawable() {
            override fun draw(canvas: Canvas) {
                throw IllegalStateException("Simulated malformed OEM adaptive icon")
            }

            override fun setAlpha(alpha: Int) = Unit
            override fun setColorFilter(colorFilter: ColorFilter?) = Unit

            @Deprecated("Deprecated in Java")
            override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
        }

        val result = rasterizeAppIconSafely("com.oem.faulty.app", faultyDrawable)
        assertNull(result)
        assertNull(rasterizeAppIconSafely("com.oem.null.app", null))
    }

    @Test
    fun `system role accessibility and main activity startup never throw unhandled exceptions`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val container = (app as AndroidLauncherApp).container

        val accessibilityEnabled = LauncherAccessibilityService.isServiceEnabled(app)
        assertFalse(accessibilityEnabled)
        // Must evaluate safely without SecurityException/binder crash (may be true when single HOME handler)
        val defaultCheckSucceeded = runCatching { container.launcherRepository.isDefaultLauncher() }.isSuccess
        assertTrue(defaultCheckSucceeded)

        val intent = container.launcherRepository.createDefaultLauncherIntent()
        assertTrue(intent.action != null)

        val mainController = Robolectric.buildActivity(MainActivity::class.java).create()
        val mainActivity = mainController.get()
        assertEquals("AndroidLauncherApp", app.javaClass.simpleName)
        assertFalse(mainActivity.isFinishing)
    }
}
