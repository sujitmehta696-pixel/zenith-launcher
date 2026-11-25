package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GlassCard
import com.example.ui.components.ProBadge
import com.example.ui.screens.LiveHomeLauncherScreen
import com.example.ui.theme.AndroidLauncherTheme
import com.example.ui.theme.LocalLauncherTokens
import com.example.ui.theme.PremiumGold
import com.example.ui.viewmodel.LauncherViewModel
import com.example.ui.viewmodel.MainViewModel

class LauncherActivity : ComponentActivity() {

    private lateinit var launcherVm: LauncherViewModel
    private lateinit var mainVm: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as AndroidLauncherApp).container
        mainVm = ViewModelProvider(this, MainViewModel.factory(appContainer))[MainViewModel::class.java]
        launcherVm = ViewModelProvider(this, LauncherViewModel.factory(appContainer))[LauncherViewModel::class.java]

        setContent {
            val activeTheme by mainVm.activeTheme.collectAsStateWithLifecycle()
            val activeWallpaper by mainVm.activeWallpaper.collectAsStateWithLifecycle()
            val settings by mainVm.settingsState.collectAsStateWithLifecycle()
            val billingState by mainVm.billingState.collectAsStateWithLifecycle()
            val remoteConfig by mainVm.remoteConfig.collectAsStateWithLifecycle()
            val liveWeather by mainVm.weatherSnapshot.collectAsStateWithLifecycle()
            val homeApps by launcherVm.homeApps.collectAsStateWithLifecycle()
            val allApps by launcherVm.allApps.collectAsStateWithLifecycle()
            val drawerApps by launcherVm.drawerApps.collectAsStateWithLifecycle()
            val searchQuery by launcherVm.searchQuery.collectAsStateWithLifecycle()
            val sortOrder by launcherVm.sortOrder.collectAsStateWithLifecycle()
            val statusBanner by launcherVm.statusBanner.collectAsStateWithLifecycle()
            val badgeCounts by launcherVm.notificationBadgeCounts.collectAsStateWithLifecycle()
            val allVisibleApps = remember(allApps) { allApps.filter { !it.isHidden } }

            // Pressing Back on a real Android HOME launcher stays on Home
            BackHandler(enabled = launcherVm.isDefaultLauncher.value) {
                // Keep user on Home screen cleanly when acting as default launcher
            }

            AndroidLauncherTheme(
                activeTheme = activeTheme,
                appearanceMode = settings.appearanceMode,
                reduceMotion = settings.reduceMotion,
                backgroundPreset = settings.backgroundPreset,
                liquidGlassEnabled = settings.liquidGlassEnabled,
                iconStyleOverride = settings.iconStyleOverride
            ) {
                if (!billingState.isPremium) {
                    PaidLauncherGateScreen(
                        onUnlockPro = {
                            if (!isFinishing && !isDestroyed) {
                                val proIntent = Intent(this, MainActivity::class.java).apply {
                                    putExtra(MainActivity.EXTRA_OPEN_PREMIUM, true)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                }
                                runCatching { startActivity(proIntent) }
                                if (!launcherVm.isDefaultLauncher.value) {
                                    finish()
                                }
                            }
                        },
                        onBackToStudio = {
                            if (!isFinishing && !isDestroyed) {
                                val studioIntent = Intent(this, MainActivity::class.java).apply {
                                    putExtra(MainActivity.EXTRA_OPEN_LAUNCHER_HUB, true)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                }
                                runCatching { startActivity(studioIntent) }
                                if (!launcherVm.isDefaultLauncher.value) {
                                    finish()
                                }
                            }
                        }
                    )
                } else {
                    LiveHomeLauncherScreen(
                        settings = settings,
                        homeApps = homeApps,
                        allVisibleApps = allVisibleApps,
                        drawerApps = drawerApps,
                        activeWallpaper = activeWallpaper,
                        wallpaperRenderer = appContainer.wallpaperRenderer,
                        searchQuery = searchQuery,
                        sortOrder = sortOrder,
                        statusBanner = statusBanner,
                        badgeCounts = badgeCounts,
                        notificationBadgesEnabled = remoteConfig.notificationBadgesEnabled,
                        edgePanelEnabled = remoteConfig.edgePanelEnabled,
                        controlCenterEnabled = remoteConfig.controlCenterEnabled,
                        liveWeather = liveWeather,
                        iconProvider = launcherVm::getAppIcon,
                        queryShortcuts = launcherVm::queryAppShortcuts,
                        onLaunchShortcut = launcherVm::launchShortcut,
                        onSearchQueryChange = launcherVm::updateSearchQuery,
                        onSortOrderChange = launcherVm::setSortOrder,
                        onLaunchApp = launcherVm::launchApp,
                        onToggleFavorite = launcherVm::toggleFavorite,
                        onToggleHidden = launcherVm::toggleHidden,
                        onPinToHome = { app -> launcherVm.pinToHome(app, pageIndex = 0, slotIndex = 0) },
                        onUnpinFromHome = launcherVm::unpinFromHome,
                        onOpenAppInfo = launcherVm::openAppInfo,
                        onUninstallApp = launcherVm::uninstallApp,
                        onToggleAutoRolling = { launcherVm.toggleAutoRolling(settings.autoRollingEnabled) },
                        onClearStatusBanner = launcherVm::clearStatusBanner,
                        onOpenUniversalSearch = {
                            if (!isFinishing && !isDestroyed) {
                                val searchIntent = Intent(this, MainActivity::class.java).apply {
                                    putExtra(MainActivity.EXTRA_OPEN_SEARCH, true)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                }
                                runCatching { startActivity(searchIntent) }
                            }
                        },
                        onOpenStudioSettings = {
                            if (!isFinishing && !isDestroyed) {
                                val studioIntent = Intent(this, MainActivity::class.java).apply {
                                    putExtra(MainActivity.EXTRA_OPEN_LAUNCHER_HUB, true)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                }
                                runCatching { startActivity(studioIntent) }
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::launcherVm.isInitialized) {
            launcherVm.refreshAppsAndDefaultStatus()
        }
    }
}

@Composable
private fun PaidLauncherGateScreen(
    onUnlockPro: () -> Unit,
    onBackToStudio: () -> Unit
) {
    val tokens = LocalLauncherTokens.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(tokens.background)
            .padding(24.dp)
            .testTag("paid_launcher_gate_screen"),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier.widthIn(max = 520.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ProBadge(label = "PAID ONLY")
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = "3D Launcher Locked",
                    tint = PremiumGold,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "3D Rolling Launcher Locked",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "An active paid Pro subscription is required to use the 3D Rolling Launcher, set it as your default home screen, or apply wallpapers and themes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onUnlockPro,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("launcher_gate_unlock_pro")
                ) {
                    Text("Unlock Pro Subscription", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onBackToStudio,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("launcher_gate_back_studio")
                ) {
                    Text("Back to Studio")
                }
            }
        }
    }
}

