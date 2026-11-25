package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MainUiNavigationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `main activity gates studio behind auth login and register screen when signed out`() {
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Android 3D Rolling launcher OS").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("auth_mode_SIGN_IN").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("auth_mode_REGISTER").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("auth_google_button").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Sign in with Google").assertIsDisplayed()

        // Switch to Register tab and verify display name & handle fields appear
        composeRule.onNodeWithTag("auth_mode_REGISTER").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("auth_register_name_input").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("auth_register_handle_input").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("auth_register_name_input").performTextInput("Karan Studio")
        composeRule.onNodeWithTag("auth_google_button").performScrollTo()
        composeRule.onNodeWithText("Register with Google").assertIsDisplayed()

        // Verify Play-compliant pre-auth Privacy Policy dialog opens cleanly
        composeRule.onNodeWithTag("auth_privacy_link").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("auth_legal_dialog").assertIsDisplayed()
        composeRule.onNodeWithText("Privacy Policy Overview").assertIsDisplayed()
        composeRule.onNodeWithText("Close").performClick()
    }
}
