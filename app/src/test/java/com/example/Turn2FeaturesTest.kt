package com.example

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.billing.BillingManager
import com.example.data.catalog.FreeThemeCatalog
import com.example.data.catalog.FreeWallpaperCatalog
import com.example.data.local.PreferencesDataStore
import com.example.data.model.BillingState
import com.example.data.model.GestureAction
import com.example.data.model.InstalledAppInfo
import com.example.data.model.LauncherBackgroundPreset
import com.example.data.model.LauncherMode
import com.example.data.model.PurchaseSource
import com.example.data.model.RemoteAppConfig
import com.example.data.model.RemoteSubscriptionPlan
import com.example.data.model.SpinnerStyle
import com.example.data.model.UserProfile
import com.example.data.wallpaper.WallpaperTarget
import com.example.ui.components.AdBannerSlot
import com.example.ui.components.ProFeatureVideoShowcase
import com.example.ui.components.SpinnerLauncherWheel
import com.example.ui.components.ThemedHomeWidgetsStrip
import com.example.ui.screens.PremiumSplashScreen
import com.example.ui.theme.AndroidLauncherTheme
import com.example.ui.viewmodel.ThemeViewModel
import com.example.ui.viewmodel.WallpaperActionState
import com.example.ui.viewmodel.WallpaperViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Turn2FeaturesTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `preferences datastore persists gesture mappings and ecosystem theme toggles`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferencesDataStore(context)

        prefs.setSwipeDownAction(GestureAction.OPEN_QUICK_SETTINGS)
        prefs.setSwipeUpAction(GestureAction.SPIN_WHEEL)
        prefs.setDoubleTapAction(GestureAction.LOCK_SCREEN)
        prefs.setLongPressAction(GestureAction.OPEN_STUDIO)
        prefs.setThemeAppIcons(false)
        prefs.setThemeWidgets(false)
        prefs.setSyncThemeWallpaper(false)

        val state = prefs.settingsFlow.first()
        assertEquals(GestureAction.OPEN_QUICK_SETTINGS, state.swipeDownAction)
        assertEquals(GestureAction.SPIN_WHEEL, state.swipeUpAction)
        assertEquals(GestureAction.LOCK_SCREEN, state.doubleTapAction)
        assertEquals(GestureAction.OPEN_STUDIO, state.longPressAction)
        assertFalse(state.themeAppIcons)
        assertFalse(state.themeWidgets)
        assertFalse(state.syncThemeWallpaper)

        prefs.resetAllCustomizations()
        val resetState = prefs.settingsFlow.first()
        assertEquals(GestureAction.OPEN_CONTROL_CENTER, resetState.swipeDownAction)
        assertEquals(GestureAction.OPEN_APP_DRAWER, resetState.swipeUpAction)
        assertEquals(GestureAction.OPEN_EDGE_PANEL, resetState.pinchAction)
        assertTrue(resetState.themeAppIcons)
        assertTrue(resetState.themeWidgets)
        assertTrue(resetState.syncThemeWallpaper)
    }

    @Test
    fun `user profile cloud model resolves enums safely with fallback`() {
        val profile = UserProfile(
            userId = "uid_123",
            displayName = "Aarav",
            premiumActive = true,
            activeProductId = "android_launcher_pro_yearly",
            purchaseSource = PurchaseSource.GOOGLE_PLAY.name,
            launcherMode = LauncherMode.GRID.name,
            spinnerStyle = SpinnerStyle.LUXURY_HALO.name,
            swipeDownAction = GestureAction.OPEN_NOTIFICATIONS.name,
            swipeUpAction = GestureAction.SPIN_WHEEL.name,
            doubleTapAction = GestureAction.LOCK_SCREEN.name,
            longPressAction = "UNKNOWN_FUTURE_ACTION"
        )

        assertEquals(PurchaseSource.GOOGLE_PLAY, profile.resolvedPurchaseSource())
        assertEquals(LauncherMode.GRID, profile.resolvedLauncherMode())
        assertEquals(SpinnerStyle.LUXURY_HALO, profile.resolvedSpinnerStyle())
        assertEquals(GestureAction.OPEN_NOTIFICATIONS, profile.resolvedSwipeDown())
        assertEquals(GestureAction.SPIN_WHEEL, profile.resolvedSwipeUp())
        assertEquals(GestureAction.LOCK_SCREEN, profile.resolvedDoubleTap())
        assertEquals(GestureAction.OPEN_STUDIO, profile.resolvedLongPress())
    }

    @Test
    fun `spinner wheel spins through all 22 installed apps without capping at 8`() {
        val manyApps = (1..22).map { index ->
            InstalledAppInfo(
                packageName = "com.test.app$index",
                activityName = "MainActivity",
                label = "App $index",
                isSystemApp = false
            )
        }

        composeRule.setContent {
            AndroidLauncherTheme(activeTheme = FreeThemeCatalog.items.first()) {
                SpinnerLauncherWheel(
                    apps = manyApps,
                    spinnerStyle = SpinnerStyle.CYBER_ARC,
                    iconProvider = { null },
                    showLabels = true,
                    themedIconMode = true,
                    onAppClick = {},
                    onAppLongClick = {}
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("spinner_launcher_wheel").assertIsDisplayed()
        composeRule.onNodeWithText("Spin All (22)").assertIsDisplayed()
        composeRule.onNodeWithText("1 / 22").assertIsDisplayed()

        // Step wheel forward and confirm index rotates beyond index 1
        composeRule.onNodeWithTag("spinner_next_step").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("4 / 22").assertIsDisplayed()
    }

    @Test
    fun `themed home widgets strip displays telemetry widgets with active theme name`() {
        val activeTheme = FreeThemeCatalog.items[1]
        composeRule.setContent {
            AndroidLauncherTheme(activeTheme = activeTheme) {
                ThemedHomeWidgetsStrip(
                    themedWidgetsEnabled = true,
                    onPulseSpin = {},
                    onOpenStudio = {}
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("themed_widgets_strip").assertIsDisplayed()
        composeRule.onNodeWithText(activeTheme.name).assertIsDisplayed()
        composeRule.onNodeWithText("Quick Spin").assertIsDisplayed()
    }

    @Test
    fun `premium splash screen renders branding and triggers completion`() {
        var finished = false
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            AndroidLauncherTheme(activeTheme = FreeThemeCatalog.items.first()) {
                PremiumSplashScreen(
                    reduceMotion = false,
                    onFinished = { finished = true }
                )
            }
        }

        composeRule.mainClock.advanceTimeBy(450L)
        composeRule.onNodeWithTag("premium_splash_screen").assertIsDisplayed()
        composeRule.onNodeWithText("3D Rolling OS").assertIsDisplayed()
        composeRule.onNodeWithTag("splash_skip_button").performClick()
        assertTrue(finished)
    }

    @Test
    fun `billing manager keeps pro locked until verified payment transaction succeeds`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferencesDataStore(context)
        val billingManager = BillingManager(context, prefs)

        assertFalse(billingManager.billingState.value.isPremium)
        assertFalse(billingManager.activateVerifiedGatewayPurchase(BillingState.SKU_YEARLY, ""))
        assertFalse(billingManager.billingState.value.isPremium)

        assertTrue(billingManager.activateVerifiedGatewayPurchase(BillingState.SKU_YEARLY, "pay_verified_123"))
        val state = billingManager.billingState.value
        assertTrue(state.isPremium)
        assertEquals(PurchaseSource.PAYMENT_GATEWAY, state.purchaseSource)
        assertEquals(BillingState.SKU_YEARLY, state.activeProductId)
        assertEquals("pay_verified_123", state.lastPurchaseToken)
    }

    @Test
    fun `ad banner slot renders for free users and disappears for pro users`() {
        var isPro by androidx.compose.runtime.mutableStateOf(false)
        composeRule.setContent {
            AndroidLauncherTheme(activeTheme = FreeThemeCatalog.items.first()) {
                AdBannerSlot(isPremium = isPro)
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("ad_banner_slot").assertIsDisplayed()

        isPro = true
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("ad_banner_slot").assertDoesNotExist()
    }

    @Test
    fun `preferences datastore persists 3d rolling engine and background preset controls`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferencesDataStore(context)

        prefs.setAutoRollingEnabled(false)
        prefs.setRollingSpeed(2.0f)
        prefs.setDepth3DIntensity(0.4f)
        prefs.setBackgroundPreset(LauncherBackgroundPreset.ELECTRIC_AURORA)
        prefs.setHapticFeedbackEnabled(false)

        val state = prefs.settingsFlow.first()
        assertFalse(state.autoRollingEnabled)
        assertEquals(2.0f, state.rollingSpeed, 0.01f)
        assertEquals(0.4f, state.depth3DIntensity, 0.01f)
        assertEquals(LauncherBackgroundPreset.ELECTRIC_AURORA, state.backgroundPreset)
        assertFalse(state.hapticFeedbackEnabled)

        prefs.resetAllCustomizations()
        val reset = prefs.settingsFlow.first()
        assertTrue(reset.autoRollingEnabled)
        assertEquals(1.0f, reset.rollingSpeed, 0.01f)
        assertEquals(0.85f, reset.depth3DIntensity, 0.01f)
        assertEquals(LauncherBackgroundPreset.MIDNIGHT_COSMOS, reset.backgroundPreset)
        assertTrue(reset.hapticFeedbackEnabled)
    }

    @Test
    fun `3d rolling engine exposes auto roll pause resume and zoom toggles`() {
        val apps = (1..8).map { index ->
            InstalledAppInfo(
                packageName = "com.test.orbit$index",
                activityName = "MainActivity",
                label = "Orbit $index",
                isSystemApp = false
            )
        }
        var autoRollToggled = false
        composeRule.setContent {
            AndroidLauncherTheme(activeTheme = FreeThemeCatalog.items.first()) {
                SpinnerLauncherWheel(
                    apps = apps,
                    spinnerStyle = SpinnerStyle.ORBITAL_DIAL,
                    iconProvider = { null },
                    showLabels = true,
                    autoRollingEnabled = true,
                    rollingSpeed = 1.2f,
                    depth3DIntensity = 0.9f,
                    onToggleAutoRolling = { autoRollToggled = true },
                    onAppClick = {},
                    onAppLongClick = {}
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("spinner_auto_roll_toggle").assertIsDisplayed().performClick()
        assertTrue(autoRollToggled)
        composeRule.onNodeWithTag("spinner_zoom_toggle").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun `billing manager maps dynamic INR plans and computes validity expiry cleanly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferencesDataStore(context)
        val billingManager = BillingManager(context, prefs)

        val adminPlans = listOf(
            RemoteSubscriptionPlan(
                planId = "pro_90_days",
                planName = "Quarterly VIP",
                subtitle = "90 Days full unlock",
                priceInr = 499.0,
                validityDays = 90,
                badge = "Hot",
                sortOrder = 1,
                isActive = true
            )
        )
        billingManager.updateRemotePlans(adminPlans, RemoteAppConfig())
        val offers = billingManager.billingState.value.offers
        assertEquals(1, offers.size)
        assertEquals("Quarterly VIP", offers.first().title)
        assertEquals("499", offers.first().numericAmount)
        assertEquals(90, offers.first().validityDays)

        val startMs = 1_700_000_000_000L
        val expMs = startMs + 90L * 86_400_000L
        assertTrue(
            billingManager.activateVerifiedGatewayPurchase(
                productId = "pro_90_days",
                transactionId = "utr_998877",
                planName = "Quarterly VIP",
                planStartedAtMillis = startMs,
                planExpiresAtMillis = expMs + System.currentTimeMillis()
            )
        )
        assertTrue(billingManager.billingState.value.isPremium)
        assertEquals("Quarterly VIP", billingManager.billingState.value.activePlanName)
    }

    @Test
    fun `user profile enforces subscription expiration timestamp`() {
        val now = 2_000_000_000_000L
        val activeProfile = UserProfile(
            userId = "u1",
            premiumActive = true,
            subscriptionActive = true,
            purchaseSource = PurchaseSource.PAYMENT_GATEWAY.name,
            planExpiresAtMillis = now + 86_400_000L
        )
        assertTrue(activeProfile.isSubscriptionCurrentlyActive(nowMs = now))

        val expiredProfile = activeProfile.copy(planExpiresAtMillis = now - 1000L)
        assertFalse(expiredProfile.isSubscriptionCurrentlyActive(nowMs = now))
    }

    @Test
    fun `pro feature video showcase renders chapters play pause and cta`() {
        var ctaClicked = false
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            AndroidLauncherTheme(activeTheme = FreeThemeCatalog.items.first()) {
                ProFeatureVideoShowcase(
                    isPremium = false,
                    reduceMotion = false,
                    onActionClick = { ctaClicked = true },
                    actionLabel = "See Pro Plans"
                )
            }
        }

        composeRule.mainClock.advanceTimeBy(120L)
        composeRule.onNodeWithTag("pro_feature_video_showcase").assertIsDisplayed()
        composeRule.onNodeWithText("Pro Plan Video Tour").assertIsDisplayed()

        composeRule.onNodeWithTag("pro_video_chapter_2").performClick()
        composeRule.mainClock.advanceTimeBy(60L)
        composeRule.onAllNodesWithText("Cyber Vortex & Violet Halo")[0].assertIsDisplayed()

        composeRule.onNodeWithTag("pro_video_play_pause").performClick()
        composeRule.mainClock.advanceTimeBy(40L)

        composeRule.onNodeWithTag("pro_video_cta_button").performClick()
        assertTrue(ctaClicked)
    }

    @Test
    fun `paid-only gating blocks wallpaper and theme actions for free users`() {
        val app = ApplicationProvider.getApplicationContext<AndroidLauncherApp>()
        val container = app.container
        container.billingManager.clearCloudEntitlement()
        assertFalse(container.billingManager.billingState.value.isPremium)

        val wallpaperVm = WallpaperViewModel(container)
        val freeWallpaper = FreeWallpaperCatalog.items.first { !it.isPremium }
        wallpaperVm.applyWallpaper(freeWallpaper, WallpaperTarget.HOME)
        assertTrue(wallpaperVm.actionState.value is WallpaperActionState.Error)

        wallpaperVm.clearActionState()
        wallpaperVm.downloadWallpaper(freeWallpaper)
        assertTrue(wallpaperVm.actionState.value is WallpaperActionState.Error)

        val themeVm = ThemeViewModel(container)
        val freeTheme = FreeThemeCatalog.items.first { !it.isPremium }
        themeVm.applyTheme(freeTheme)
        assertTrue(themeVm.feedbackMessage.value?.contains("Pro", ignoreCase = true) == true)
    }
}
