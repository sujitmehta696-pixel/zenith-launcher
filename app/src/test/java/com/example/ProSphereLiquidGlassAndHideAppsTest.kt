package com.example

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AppearanceMode
import com.example.data.model.BillingState
import com.example.data.model.InstalledAppInfo
import com.example.data.model.LauncherBackgroundPreset
import com.example.data.model.LauncherSettingsState
import com.example.data.model.SpinnerStyle
import com.example.ui.components.SpinnerLauncherWheel
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AndroidLauncherTheme
import com.example.ui.viewmodel.LauncherViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.SearchFavoritesViewModel
import com.example.ui.viewmodel.ThemeViewModel
import com.example.ui.viewmodel.WallpaperViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProSphereLiquidGlassAndHideAppsTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `liquid glass preference and pro 3d sphere style persist in datastore`() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val prefs = (app as AndroidLauncherApp).container.preferencesDataStore

        assertTrue(prefs.settingsFlow.first().liquidGlassEnabled)

        prefs.setLiquidGlassEnabled(false)
        assertFalse(prefs.settingsFlow.first().liquidGlassEnabled)

        prefs.setSpinnerStyle(SpinnerStyle.PRO_3D_SPHERE)
        assertTrue(prefs.settingsFlow.first().spinnerStyle == SpinnerStyle.PRO_3D_SPHERE)
        assertTrue(SpinnerStyle.PRO_3D_SPHERE.isPremium)

        prefs.resetAllCustomizations()
        assertTrue(prefs.settingsFlow.first().liquidGlassEnabled)
        assertTrue(prefs.settingsFlow.first().spinnerStyle == SpinnerStyle.ORBITAL_DIAL)
    }

    @Test
    fun `pro 3d sphere spinner renders large installed app clouds cleanly`() {
        val sampleApps = (1..30).map { idx ->
            InstalledAppInfo(
                packageName = "com.demo.app$idx",
                activityName = "com.demo.Main$idx",
                label = "App $idx",
                isSystemApp = false
            )
        }

        composeRule.setContent {
            AndroidLauncherTheme(
                appearanceMode = AppearanceMode.DARK,
                reduceMotion = true,
                backgroundPreset = LauncherBackgroundPreset.LIQUID_GLASS,
                liquidGlassEnabled = true
            ) {
                SpinnerLauncherWheel(
                    apps = sampleApps,
                    spinnerStyle = SpinnerStyle.PRO_3D_SPHERE,
                    iconProvider = { null },
                    showLabels = true,
                    themedIconMode = true,
                    autoRollingEnabled = false,
                    onAppClick = {},
                    onAppLongClick = {}
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("spinner_launcher_wheel").assertIsDisplayed()
        composeRule.onNodeWithTag("spinner_quick_spin_button").assertIsDisplayed()
        composeRule.onNodeWithText("Spin All (30)").assertIsDisplayed()
    }

    @Test
    fun `home screen liquid glass toggle and settings hidden apps vault work end to end`() {
        val activity = composeRule.activity
        val container = (activity.application as AndroidLauncherApp).container

        composeRule.setContent {
            val mainVm: MainViewModel = viewModel(factory = MainViewModel.factory(container))
            val wallpaperVm: WallpaperViewModel = viewModel(factory = WallpaperViewModel.factory(container))
            val themeVm: ThemeViewModel = viewModel(factory = ThemeViewModel.factory(container))
            val launcherVm: LauncherViewModel = viewModel(factory = LauncherViewModel.factory(container))
            val searchFavVm: SearchFavoritesViewModel = viewModel(factory = SearchFavoritesViewModel.factory(container))

            val activeTheme by mainVm.activeTheme.collectAsStateWithLifecycle()
            val settings by mainVm.settingsState.collectAsStateWithLifecycle()

            androidx.compose.runtime.LaunchedEffect(Unit) {
                mainVm.setReduceMotion(true)
            }

            AndroidLauncherTheme(
                activeTheme = activeTheme,
                appearanceMode = settings.appearanceMode,
                reduceMotion = true,
                backgroundPreset = settings.backgroundPreset,
                liquidGlassEnabled = settings.liquidGlassEnabled
            ) {
                MainStudioRoot(
                    activity = activity,
                    container = container,
                    mainVm = mainVm,
                    wallpaperVm = wallpaperVm,
                    themeVm = themeVm,
                    launcherVm = launcherVm,
                    searchFavVm = searchFavVm,
                    accountLabel = "pro.tester@example.com"
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_screen_list")
            .performScrollToNode(hasTestTag("home_liquid_glass_toggle"))
        composeRule.onNodeWithTag("home_liquid_glass_toggle").performClick()
        composeRule.waitForIdle()

        // Navigate to Settings -> inspect Liquid Glass switch & Hidden Apps Vault row
        composeRule.onNodeWithTag("nav_settings").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_liquid_glass_switch").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("settings_hidden_apps_item"))
        composeRule.onNodeWithTag("settings_hidden_apps_item").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("hidden_apps_vault_dialog").assertIsDisplayed()
        composeRule.onNodeWithText("Done").performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun `hidden apps vault toggles hidden status and removes app from visible lists`() {
        var fakeApps by mutableStateOf(
            listOf(
                InstalledAppInfo("com.secret.bank", "Main", "Secret Bank", isSystemApp = false, isFavorite = true, isHidden = false),
                InstalledAppInfo("com.public.notes", "Main", "Public Notes", isSystemApp = false, isFavorite = true, isHidden = false)
            )
        )

        composeRule.setContent {
            AndroidLauncherTheme(reduceMotion = true, liquidGlassEnabled = true) {
                SettingsScreen(
                    settings = LauncherSettingsState(),
                    billingState = BillingState(),
                    cacheSizeLabel = "1.2 MB",
                    allApps = fakeApps,
                    hiddenApps = fakeApps.filter { it.isHidden },
                    onSelectAppearance = {},
                    onToggleHiddenApp = { target ->
                        fakeApps = fakeApps.map {
                            if (it.packageName == target.packageName) it.copy(isHidden = !it.isHidden) else it
                        }
                    },
                    onSetReduceMotion = {},
                    onSetAutoCache = {},
                    onSetWifiOnly = {},
                    onOpenLauncherHub = {},
                    onOpenFavorites = {},
                    onOpenPremium = {},
                    onRestorePurchases = {},
                    onClearCache = {},
                    onResetCustomization = {},
                    onOpenPrivacy = {},
                    onOpenTerms = {}
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("settings_hidden_apps_item"))
        composeRule.onNodeWithTag("settings_hidden_apps_item").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("hidden_app_toggle_com.secret.bank").performClick()
        composeRule.waitForIdle()

        assertTrue(fakeApps.first { it.packageName == "com.secret.bank" }.isHidden)
        assertFalse(fakeApps.first { it.packageName == "com.public.notes" }.isHidden)
    }
}
