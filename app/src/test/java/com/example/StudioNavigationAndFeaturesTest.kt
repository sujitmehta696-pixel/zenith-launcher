package com.example

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.AndroidLauncherTheme
import com.example.ui.viewmodel.LauncherViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.SearchFavoritesViewModel
import com.example.ui.viewmodel.ThemeViewModel
import com.example.ui.viewmodel.WallpaperViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StudioNavigationAndFeaturesTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `studio navigation covers all 6 tabs account screen and gesture panel`() {
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

            AndroidLauncherTheme(
                activeTheme = activeTheme,
                appearanceMode = settings.appearanceMode,
                reduceMotion = settings.reduceMotion
            ) {
                MainStudioRoot(
                    activity = activity,
                    container = container,
                    mainVm = mainVm,
                    wallpaperVm = wallpaperVm,
                    themeVm = themeVm,
                    launcherVm = launcherVm,
                    searchFavVm = searchFavVm,
                    accountLabel = "studio.tester@example.com"
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("studio_gesture_pager").assertIsDisplayed()
        composeRule.onNodeWithTag("home_open_live_launcher").assertIsDisplayed()

        // Account vault screen navigation
        composeRule.onNodeWithTag("home_account_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("account_sync_now_button").assertIsDisplayed()
        composeRule.onNodeWithTag("account_back").performClick()
        composeRule.waitForIdle()

        // Universal search screen navigation
        composeRule.onNodeWithTag("home_search_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("universal_search_input").assertIsDisplayed()
        composeRule.onNodeWithTag("search_back").performClick()
        composeRule.waitForIdle()

        // Launcher Hub tab -> scroll to Gesture Settings & Ecosystem Theme Controls
        composeRule.onNodeWithTag("nav_launcher").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("launcher_open_live_home").assertIsDisplayed()
        composeRule.onNodeWithTag("launcher_hub_list")
            .performScrollToNode(hasTestTag("gesture_settings_panel"))
        composeRule.onNodeWithTag("gesture_settings_panel").assertIsDisplayed()
        composeRule.onNodeWithTag("gesture_row_SWIPE_DOWN").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("gesture_option_SWIPE_DOWN_OPEN_QUICK_SETTINGS").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("launcher_hub_list")
            .performScrollToNode(hasTestTag("ecosystem_theme_controls"))
        composeRule.onNodeWithTag("switch_theme_app_icons").assertIsDisplayed()

        // Wallpapers tab
        composeRule.onNodeWithTag("nav_wallpapers").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wallpapers_search_input").assertIsDisplayed()

        // Themes tab
        composeRule.onNodeWithTag("nav_themes").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("themes_search_input").assertIsDisplayed()

        // Premium tab — verify removed dev/Play Console cards stay gone
        composeRule.onNodeWithTag("nav_premium").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("premium_list")
            .performScrollToNode(hasTestTag("premium_subscribe_button"))
        composeRule.onNodeWithTag("premium_subscribe_button").assertIsDisplayed()
        composeRule.onNodeWithTag("beginner_payment_setup_card").assertDoesNotExist()
        composeRule.onNodeWithTag("debug_premium_switch").assertDoesNotExist()

        // Settings tab
        composeRule.onNodeWithTag("nav_settings").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("appearance_DARK").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("settings_account_item"))
        composeRule.onNodeWithTag("settings_account_item").assertIsDisplayed()
    }

    @Test
    fun `gesture configuration screen and button vs gesture navigation switch work end to end`() {
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

            AndroidLauncherTheme(
                activeTheme = activeTheme,
                appearanceMode = settings.appearanceMode,
                reduceMotion = settings.reduceMotion
            ) {
                MainStudioRoot(
                    activity = activity,
                    container = container,
                    mainVm = mainVm,
                    wallpaperVm = wallpaperVm,
                    themeVm = themeVm,
                    launcherVm = launcherVm,
                    searchFavVm = searchFavVm,
                    accountLabel = "gesture.tester@example.com"
                )
            }
        }

        composeRule.waitForIdle()

        // 1. Toggle Navigation Mode right from Home Screen (Gesture -> Buttons)
        composeRule.onNodeWithTag("home_screen_list")
            .performScrollToNode(hasTestTag("home_toggle_navigation_mode"))
        composeRule.onNodeWithTag("home_toggle_navigation_mode").performClick()
        composeRule.waitUntil(timeoutMillis = 4000) {
            composeRule.onAllNodes(hasTestTag("studio_button_dock"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("studio_button_dock").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_btn_back").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_btn_home").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_btn_live_launcher").assertIsDisplayed()

        // 2. Open dedicated Gesture & Navigation Configuration Screen from Home
        composeRule.onNodeWithTag("home_screen_list")
            .performScrollToNode(hasTestTag("home_open_gestures"))
        composeRule.onNodeWithTag("home_open_gestures").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("gesture_config_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("gesture_live_test_pad").assertIsDisplayed()

        // Customize Swipe Up
        composeRule.onNodeWithTag("gesture_config_list")
            .performScrollToNode(hasTestTag("gesture_card_SWIPE_UP"))
        composeRule.onNodeWithTag("gesture_card_SWIPE_UP").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("gesture_sheet_action_list")
            .performScrollToNode(hasTestTag("gesture_sheet_option_SWIPE_UP_OPEN_SEARCH"))
        composeRule.onNodeWithTag("gesture_sheet_option_SWIPE_UP_OPEN_SEARCH").performClick()
        composeRule.waitForIdle()

        // Customize Swipe Down
        composeRule.onNodeWithTag("gesture_config_list")
            .performScrollToNode(hasTestTag("gesture_card_SWIPE_DOWN"))
        composeRule.onNodeWithTag("gesture_card_SWIPE_DOWN").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("gesture_sheet_action_list")
            .performScrollToNode(hasTestTag("gesture_sheet_option_SWIPE_DOWN_OPEN_QUICK_SETTINGS"))
        composeRule.onNodeWithTag("gesture_sheet_option_SWIPE_DOWN_OPEN_QUICK_SETTINGS").performClick()
        composeRule.waitForIdle()

        // Customize Double Tap
        composeRule.onNodeWithTag("gesture_config_list")
            .performScrollToNode(hasTestTag("gesture_card_DOUBLE_TAP"))
        composeRule.onNodeWithTag("gesture_card_DOUBLE_TAP").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("gesture_sheet_action_list")
            .performScrollToNode(hasTestTag("gesture_sheet_option_DOUBLE_TAP_SPIN_WHEEL"))
        composeRule.onNodeWithTag("gesture_sheet_option_DOUBLE_TAP_SPIN_WHEEL").performClick()
        composeRule.waitForIdle()

        // Switch Navigation Mode back to Gesture inside GestureConfigScreen
        composeRule.onNodeWithTag("gesture_config_list")
            .performScrollToNode(hasTestTag("gesture_nav_mode_GESTURE"))
        composeRule.onNodeWithTag("gesture_nav_mode_GESTURE").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("gesture_config_back").performClick()
        composeRule.waitUntil(timeoutMillis = 4000) {
            composeRule.onAllNodes(hasTestTag("studio_button_dock"))
                .fetchSemanticsNodes().isEmpty()
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("studio_button_dock").assertDoesNotExist()
        composeRule.onNodeWithTag("nav_home").assertIsDisplayed()
    }
}

