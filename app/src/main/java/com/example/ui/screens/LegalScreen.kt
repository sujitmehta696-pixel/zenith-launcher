package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen(
    docType: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPrivacy = docType == "privacy"
    val title = if (isPrivacy) "Privacy Policy" else "Terms & Conditions"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = if (isPrivacy) PRIVACY_TEXT else TERMS_TEXT,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

private const val PRIVACY_TEXT = """
ANDROID 3D ROLLING LAUNCHER OS — PRIVACY POLICY

1. Local & Cloud Personalization
Android 3D Rolling launcher OS stores your active theme, wallpaper selection, favorites, hidden apps list, search history, gesture mappings, and 3D launcher preferences locally using Android DataStore and Room SQLite, and optionally backs up your profile, active theme, gestures, and Pro entitlement to Firebase Cloud Firestore when signed in with Google.

2. Installed Applications Visibility
Android 3D Rolling launcher OS queries launchable applications via Android's standard PackageManager and LauncherApps APIs solely to render your 3D Rolling Home Screen and App Drawer. We do not upload, sell, or transmit your installed app list to any external server.

3. Wallpapers & Media Storage
Downloading a wallpaper saves an image file to your device's Pictures/Android 3D Rolling Launcher directory via Android MediaStore without requesting broad photo gallery read permissions.

4. Accessibility Service Usage (Optional)
If you enable optional system gestures (Lock Screen, Notifications shade, Quick Settings), Android's AccessibilityService permission is used exclusively to dispatch that explicit system global action on demand. No screen content, keystrokes, or personal UI data are ever read or transmitted.

5. Store Billing & Cloud Vault
Paid Pro subscriptions sync with your signed-in Studio Account so Pro stays active across devices until your plan validity ends. Android 3D Rolling launcher OS does not collect or store payment card details.

6. Advertising (Google AdMob)
The Free version displays Banner Ads to support free launcher themes and wallpapers—watching ads applies to the free tier only and never unlocks Pro plans. Activating a paid Pro subscription automatically hides all ads.

7. Contact
Questions or privacy requests: support@androidlauncher.app
"""

private const val TERMS_TEXT = """
ANDROID 3D ROLLING LAUNCHER OS — TERMS & CONDITIONS

1. License
Android 3D Rolling launcher OS grants you a personal, non-exclusive, revocable license to use the 3D launcher application, bundled studio themes, widget styling, icon shapes, and original studio wallpapers on your Android devices.

2. Free & Pro Entitlements
The Free tier includes 50 studio wallpapers, 50 dynamic themes, full-ecosystem widget & icon theming, gesture navigation, and the 3D All-Apps Rolling, Grid, and Minimal launcher modes. Pro entitlements unlock additional curated collections, Cyber Vortex & Violet Halo 3D gyroscope styles, and 6-column grids.

3. Subscriptions & Billing
Where supported by your device app store (including Indus Appstore & Android store billing), subscriptions renew automatically unless canceled before the end of the current billing period.

4. System Capabilities
Default home-screen assignment, app shortcuts, gesture global actions via AccessibilityService, and home-screen widget hosting rely on official Android OS APIs and device manufacturer compatibility.

5. Support
For billing or technical support, contact support@androidlauncher.app.
"""
