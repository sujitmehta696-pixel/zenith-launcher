package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.data.cloud.GoogleSignInResult
import com.example.data.model.CloudUiState
import com.example.data.model.NavigationMode
import com.example.ui.components.DynamicOrbitBackdrop
import com.example.ui.components.GlassCard
import com.example.ui.components.InAppPaymentWebViewSheet
import com.example.ui.navigation.AccountRoute
import com.example.ui.navigation.FavoritesRoute
import com.example.ui.navigation.GestureConfigRoute
import com.example.ui.navigation.HomeRoute
import com.example.ui.navigation.LegalRoute
import com.example.ui.navigation.SearchRoute
import com.example.ui.navigation.ThemeDetailRoute
import com.example.ui.navigation.WallpaperDetailRoute
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.GestureConfigScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LauncherHubScreen
import com.example.ui.screens.LegalScreen
import com.example.ui.screens.PremiumScreen
import com.example.ui.screens.PremiumSplashScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ThemeDetailScreen
import com.example.ui.screens.ThemesScreen
import com.example.ui.screens.WallpaperDetailScreen
import com.example.ui.screens.WallpapersScreen
import com.example.ui.theme.AndroidLauncherTheme
import com.example.ui.theme.LocalLauncherTokens
import com.example.ui.viewmodel.CloudAccountViewModel
import com.example.ui.viewmodel.LauncherViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.SearchFavoritesViewModel
import com.example.ui.viewmodel.ThemeViewModel
import com.example.ui.viewmodel.WallpaperViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        runCatching { installSplashScreen() }
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        enableEdgeToEdge()

        val appContainer = (application as AndroidLauncherApp).container
        appContainer.adManager.initialize(this)
        val openLauncherTabOnStart = intent?.getBooleanExtra(EXTRA_OPEN_LAUNCHER_HUB, false) == true
        val openSearchOnStart = intent?.getBooleanExtra(EXTRA_OPEN_SEARCH, false) == true
        val openPremiumTabOnStart = intent?.getBooleanExtra(EXTRA_OPEN_PREMIUM, false) == true

        setContent {
            val mainVm: MainViewModel = viewModel(factory = MainViewModel.factory(appContainer))
            val wallpaperVm: WallpaperViewModel = viewModel(factory = WallpaperViewModel.factory(appContainer))
            val themeVm: ThemeViewModel = viewModel(factory = ThemeViewModel.factory(appContainer))
            val launcherVm: LauncherViewModel = viewModel(factory = LauncherViewModel.factory(appContainer))
            val searchFavVm: SearchFavoritesViewModel = viewModel(factory = SearchFavoritesViewModel.factory(appContainer))

            val activeTheme by mainVm.activeTheme.collectAsStateWithLifecycle()
            val settings by mainVm.settingsState.collectAsStateWithLifecycle()
            val remoteConfig by mainVm.remoteConfig.collectAsStateWithLifecycle()
            val gatewayCheckoutState by mainVm.gatewayCheckoutState.collectAsStateWithLifecycle()
            val scope = rememberCoroutineScope()

            val currentUser by appContainer.authRepository.authStateFlow()
                .collectAsStateWithLifecycle(initialValue = appContainer.authRepository.currentUser())

            var showSplash by remember {
                mutableStateOf(!openLauncherTabOnStart && !openSearchOnStart && !openPremiumTabOnStart)
            }
            var authLoading by remember { mutableStateOf(false) }
            var authError by remember { mutableStateOf<String?>(null) }
            var pendingRegisterName by remember { mutableStateOf<String?>(null) }
            var pendingRegisterHandle by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(showSplash, currentUser) {
                if (!showSplash && currentUser == null && !isFinishing && !isDestroyed) {
                    delay(250L)
                    if (!isFinishing && !isDestroyed) {
                        appContainer.authRepository.attemptAutoSignIn(this@MainActivity)
                    }
                }
            }

            // Re-check store billing whenever user returns to app
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        mainVm.refreshPurchasesSilently()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            AndroidLauncherTheme(
                activeTheme = activeTheme,
                appearanceMode = settings.appearanceMode,
                reduceMotion = settings.reduceMotion,
                backgroundPreset = settings.backgroundPreset,
                liquidGlassEnabled = settings.liquidGlassEnabled,
                iconStyleOverride = settings.iconStyleOverride
            ) {
                val signedInUser = currentUser
                if (showSplash) {
                    PremiumSplashScreen(
                        reduceMotion = settings.reduceMotion,
                        onFinished = { showSplash = false }
                    )
                } else if (signedInUser == null) {
                    AuthScreen(
                        isLoading = authLoading,
                        errorMessage = authError,
                        onGoogleAuthSubmit = { _, name, handle ->
                            pendingRegisterName = name.takeIf { it.isNotBlank() }
                            pendingRegisterHandle = handle.takeIf { it.isNotBlank() }
                            authLoading = true
                            authError = null
                            scope.launch {
                                when (val res = appContainer.authRepository.signInWithGoogle(this@MainActivity)) {
                                    is GoogleSignInResult.Success -> {
                                        authLoading = false
                                    }
                                    GoogleSignInResult.Cancelled -> {
                                        authLoading = false
                                    }
                                    is GoogleSignInResult.Error -> {
                                        authLoading = false
                                        authError = res.message
                                    }
                                }
                            }
                        }
                    )
                } else {
                    key(signedInUser.uid) {
                        val cloudVm: CloudAccountViewModel = viewModel(
                            key = signedInUser.uid,
                            factory = CloudAccountViewModel.factory(
                                container = appContainer,
                                currentUserId = signedInUser.uid,
                                customName = pendingRegisterName,
                                customHandle = pendingRegisterHandle
                            )
                        )
                        val profileState by cloudVm.profileState.collectAsStateWithLifecycle()
                        val cloudProfile = (profileState as? CloudUiState.Success)?.data

                        LaunchedEffect(
                            cloudProfile?.premiumActive,
                            cloudProfile?.subscriptionActive,
                            cloudProfile?.planExpiresAtMillis,
                            gatewayCheckoutState
                        ) {
                            if (cloudProfile?.isSubscriptionCurrentlyActive() == true && gatewayCheckoutState != null) {
                                mainVm.verifyAndCompleteGatewayCheckout(
                                    paymentIdFromGateway = cloudProfile.activeProductId ?: cloudProfile.planId,
                                    cloudProfileAlreadyPro = true,
                                    planName = cloudProfile.planName,
                                    planStartedAtMillis = cloudProfile.planStartedAtMillis,
                                    planExpiresAtMillis = cloudProfile.planExpiresAtMillis
                                )
                            }
                        }

                        LaunchedEffect(
                            settings.activeThemeId,
                            settings.activeWallpaperId,
                            settings.launcherMode,
                            settings.spinnerStyle,
                            settings.swipeDownAction,
                            settings.swipeUpAction,
                            settings.doubleTapAction,
                            settings.longPressAction,
                            settings.pinchAction,
                            settings.iconStyleOverride
                        ) {
                            delay(700L)
                            cloudVm.syncSettingsInBackground(settings)
                        }

                        val accountLabel = when (val state = profileState) {
                            is CloudUiState.Success -> state.data?.displayName
                                ?: signedInUser.displayName
                                ?: signedInUser.email
                            else -> signedInUser.displayName ?: signedInUser.email
                        }

                        val signOutAction: () -> Unit = {
                            scope.launch {
                                pendingRegisterName = null
                                pendingRegisterHandle = null
                                appContainer.billingManager.clearCloudEntitlement()
                                appContainer.authRepository.signOut(this@MainActivity)
                            }
                        }

                        when {
                            remoteConfig.maintenanceMode -> {
                                FullScreenLockNotice(
                                    icon = Icons.Rounded.Build,
                                    title = "Maintenance in Progress",
                                    message = remoteConfig.maintenanceMessage,
                                    actionLabel = null,
                                    onAction = null
                                )
                            }
                            cloudProfile?.isBlocked == true -> {
                                FullScreenLockNotice(
                                    icon = Icons.Rounded.Block,
                                    title = "Account Restricted",
                                    message = "Access to this account has been restricted by the administrator.",
                                    actionLabel = "Sign Out",
                                    onAction = signOutAction
                                )
                            }
                            else -> {
                                MainStudioRoot(
                                    activity = this,
                                    container = appContainer,
                                    mainVm = mainVm,
                                    wallpaperVm = wallpaperVm,
                                    themeVm = themeVm,
                                    launcherVm = launcherVm,
                                    searchFavVm = searchFavVm,
                                    cloudVm = cloudVm,
                                    accountLabel = accountLabel,
                                    onSignOut = signOutAction,
                                    openLauncherTabOnStart = openLauncherTabOnStart,
                                    openSearchOnStart = openSearchOnStart,
                                    openPremiumTabOnStart = openPremiumTabOnStart
                                )

                                gatewayCheckoutState?.let { checkoutState ->
                                    InAppPaymentWebViewSheet(
                                        checkoutState = checkoutState,
                                        providerName = remoteConfig.paymentProviderName,
                                        onVerifyPayment = { paymentId ->
                                            mainVm.verifyAndCompleteGatewayCheckout(paymentIdFromGateway = paymentId)
                                        },
                                        onDismiss = mainVm::dismissGatewayCheckout
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_OPEN_LAUNCHER_HUB = "open_launcher_hub"
        const val EXTRA_OPEN_SEARCH = "open_universal_search"
        const val EXTRA_OPEN_PREMIUM = "open_premium_plans"
    }
}

@Composable
private fun FullScreenLockNotice(
    icon: ImageVector,
    title: String,
    message: String,
    actionLabel: String?,
    onAction: (() -> Unit)?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalLauncherTokens.current.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = LocalLauncherTokens.current.primary,
                    modifier = Modifier.size(44.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                if (actionLabel != null && onAction != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(onClick = onAction, modifier = Modifier.fillMaxWidth()) {
                        Text(actionLabel)
                    }
                }
            }
        }
    }
}

private data class TopLevelNavItem(
    val pageIndex: Int,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
internal fun MainStudioRoot(
    activity: ComponentActivity,
    container: AppContainer,
    mainVm: MainViewModel,
    wallpaperVm: WallpaperViewModel,
    themeVm: ThemeViewModel,
    launcherVm: LauncherViewModel,
    searchFavVm: SearchFavoritesViewModel,
    cloudVm: CloudAccountViewModel? = null,
    accountLabel: String? = null,
    onSignOut: () -> Unit = {},
    openLauncherTabOnStart: Boolean = false,
    openSearchOnStart: Boolean = false,
    openPremiumTabOnStart: Boolean = false
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val scope = rememberCoroutineScope()
    val tokens = LocalLauncherTokens.current

    val topLevelItems = remember {
        listOf(
            TopLevelNavItem(0, "Home", Icons.Rounded.Home, Icons.Outlined.Home, "nav_home"),
            TopLevelNavItem(1, "Launcher", Icons.Rounded.RocketLaunch, Icons.Outlined.RocketLaunch, "nav_launcher"),
            TopLevelNavItem(2, "Walls", Icons.Rounded.Wallpaper, Icons.Outlined.Wallpaper, "nav_wallpapers"),
            TopLevelNavItem(3, "Themes", Icons.Rounded.Palette, Icons.Outlined.Palette, "nav_themes"),
            TopLevelNavItem(4, "Pro", Icons.Rounded.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_premium"),
            TopLevelNavItem(5, "Settings", Icons.Rounded.Settings, Icons.Outlined.Settings, "nav_settings")
        )
    }

    val pagerState = rememberPagerState(
        initialPage = when {
            openPremiumTabOnStart -> 4
            openLauncherTabOnStart -> 1
            else -> 0
        },
        pageCount = { topLevelItems.size }
    )

    val isTopLevel = currentDestination?.hasRoute<HomeRoute>() == true || currentDestination == null

    val defaultRoleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        launcherVm.refreshAppsAndDefaultStatus()
    }

    fun navigateTopPage(index: Int) {
        if (!isTopLevel) {
            navController.popBackStack(HomeRoute, inclusive = false)
        }
        scope.launch {
            pagerState.animateScrollToPage(index.coerceIn(0, topLevelItems.lastIndex))
        }
    }

    // Gesture Back inside top-level Studio glides back to Home tab before exiting
    BackHandler(enabled = isTopLevel && pagerState.currentPage != 0) {
        navigateTopPage(0)
    }

    LaunchedEffect(openLauncherTabOnStart) {
        if (openLauncherTabOnStart) {
            pagerState.scrollToPage(1)
        }
    }

    LaunchedEffect(openPremiumTabOnStart) {
        if (openPremiumTabOnStart) {
            pagerState.scrollToPage(4)
        }
    }

    LaunchedEffect(openSearchOnStart) {
        if (openSearchOnStart) {
            navController.navigate(SearchRoute)
        }
    }

    val globalSnackbarHost = remember { SnackbarHostState() }

    fun openLiveHomeActivity() {
        if (!mainVm.billingState.value.isPremium) {
            scope.launch {
                globalSnackbarHost.showSnackbar("Paid Pro plan required to unlock 3D Launcher & Wallpapers.")
            }
            navigateTopPage(4)
            return
        }
        val intent = Intent(activity, LauncherActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        runCatching { activity.startActivity(intent) }
    }

    val launcherBanner by launcherVm.statusBanner.collectAsStateWithLifecycle()
    val studioSettings by mainVm.settingsState.collectAsStateWithLifecycle()
    val remoteConfig by mainVm.remoteConfig.collectAsStateWithLifecycle()
    val weatherSnapshot by mainVm.weatherSnapshot.collectAsStateWithLifecycle()
    val dismissedAnnouncement by mainVm.dismissedAnnouncement.collectAsStateWithLifecycle()
    val badgeCounts by launcherVm.notificationBadgeCounts.collectAsStateWithLifecycle()

    LaunchedEffect(launcherBanner) {
        launcherBanner?.let { msg ->
            globalSnackbarHost.showSnackbar(msg)
            launcherVm.clearStatusBanner()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        DynamicOrbitBackdrop(
            preset = studioSettings.backgroundPreset,
            reduceMotion = studioSettings.reduceMotion
        )

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(hostState = globalSnackbarHost) },
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (isTopLevel && !useNavigationRail) {
                    if (studioSettings.navigationMode == NavigationMode.GESTURE) {
                        FloatingGestureDock(
                            items = topLevelItems,
                            selectedPage = pagerState.currentPage,
                            onSelectPage = ::navigateTopPage
                        )
                    } else {
                        ClassicButtonNavigationDock(
                            items = topLevelItems,
                            selectedPage = pagerState.currentPage,
                            onSelectPage = ::navigateTopPage,
                            onBackPress = {
                                if (pagerState.currentPage != 0) {
                                    navigateTopPage(0)
                                } else {
                                    runCatching { activity.onBackPressedDispatcher.onBackPressed() }
                                }
                            },
                            onHomePress = { navigateTopPage(0) },
                            onOpenLiveHome = ::openLiveHomeActivity
                        )
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isTopLevel && useNavigationRail) {
                    NavigationRail(
                        containerColor = tokens.surface.copy(alpha = 0.85f),
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        topLevelItems.forEach { item ->
                            val selected = pagerState.currentPage == item.pageIndex
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopPage(item.pageIndex) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    NavHost(
                        navController = navController,
                        startDestination = HomeRoute
                    ) {
                        composable<HomeRoute> {
                            // Swipe left/right across all 6 Studio screens in Gesture mode, or lock to buttons in Button mode
                            HorizontalPager(
                                state = pagerState,
                                userScrollEnabled = studioSettings.navigationMode == NavigationMode.GESTURE,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("studio_gesture_pager")
                            ) { pageIndex ->
                                when (pageIndex) {
                                    0 -> {
                                        val activeTheme by mainVm.activeTheme.collectAsStateWithLifecycle()
                                        val activeWallpaper by mainVm.activeWallpaper.collectAsStateWithLifecycle()
                                        val settings by mainVm.settingsState.collectAsStateWithLifecycle()
                                        val billingState by mainVm.billingState.collectAsStateWithLifecycle()
                                        val freeWps by mainVm.featuredFreeWallpapers.collectAsStateWithLifecycle()
                                        val proWps by mainVm.featuredPremiumWallpapers.collectAsStateWithLifecycle()
                                        val freeThemes by mainVm.featuredFreeThemes.collectAsStateWithLifecycle()
                                        val proThemes by mainVm.featuredPremiumThemes.collectAsStateWithLifecycle()

                                        HomeScreen(
                                            activeTheme = activeTheme,
                                            activeWallpaper = activeWallpaper,
                                            settings = settings,
                                            billingState = billingState,
                                            featuredFreeWallpapers = freeWps,
                                            featuredPremiumWallpapers = proWps,
                                            featuredFreeThemes = freeThemes,
                                            featuredPremiumThemes = proThemes,
                                            renderer = container.wallpaperRenderer,
                                            announcementBanner = remoteConfig.announcementBanner
                                                .takeIf { it.isNotBlank() && it != dismissedAnnouncement }
                                                .orEmpty(),
                                            weatherSnapshot = weatherSnapshot,
                                            onDismissAnnouncement = mainVm::dismissAnnouncement,
                                            onOpenSearch = { navController.navigate(SearchRoute) },
                                            onOpenFavorites = { navController.navigate(FavoritesRoute) },
                                            onOpenAccount = { navController.navigate(AccountRoute) },
                                            onOpenLauncherHub = { navigateTopPage(1) },
                                            onOpenGestureConfig = { navController.navigate(GestureConfigRoute) },
                                            onToggleNavigationMode = {
                                                val next = if (settings.navigationMode == NavigationMode.GESTURE) {
                                                    NavigationMode.BUTTONS
                                                } else {
                                                    NavigationMode.GESTURE
                                                }
                                                mainVm.setNavigationMode(next)
                                            },
                                            onOpenLiveHome = ::openLiveHomeActivity,
                                            onOpenWallpapers = { navigateTopPage(2) },
                                            onOpenThemes = { navigateTopPage(3) },
                                            onOpenPremium = { navigateTopPage(4) },
                                            onSetLiquidGlass = mainVm::setLiquidGlassEnabled,
                                            onWallpaperClick = { navController.navigate(WallpaperDetailRoute(it.id)) },
                                            onToggleWallpaperFav = { wallpaperVm.toggleFavorite(it) },
                                            onThemeClick = { navController.navigate(ThemeDetailRoute(it.id)) },
                                            onToggleThemeFav = { themeVm.toggleFavorite(it) }
                                        )
                                    }
                                    1 -> {
                                        val settings by mainVm.settingsState.collectAsStateWithLifecycle()
                                        val billingState by mainVm.billingState.collectAsStateWithLifecycle()
                                        val isDefault by launcherVm.isDefaultLauncher.collectAsStateWithLifecycle()
                                        val homeApps by launcherVm.homeApps.collectAsStateWithLifecycle()
                                        val allApps by launcherVm.allApps.collectAsStateWithLifecycle()
                                        val hiddenApps by launcherVm.hiddenApps.collectAsStateWithLifecycle()

                                        LaunchedEffect(Unit) {
                                            launcherVm.refreshAppsAndDefaultStatus()
                                        }

                                        LauncherHubScreen(
                                            settings = settings,
                                            isDefaultLauncher = isDefault,
                                            isPremiumUnlocked = billingState.isPremium,
                                            homeApps = homeApps,
                                            allApps = allApps,
                                            hiddenApps = hiddenApps,
                                            badgeCounts = badgeCounts,
                                            notificationBadgesEnabled = remoteConfig.notificationBadgesEnabled,
                                            iconProvider = launcherVm::getAppIcon,
                                            onOpenLiveHome = ::openLiveHomeActivity,
                                            onRequestDefaultLauncher = {
                                                if (!mainVm.billingState.value.isPremium) {
                                                    scope.launch {
                                                        globalSnackbarHost.showSnackbar("Paid Pro plan required to set default 3D Launcher.")
                                                    }
                                                    navigateTopPage(4)
                                                } else {
                                                    runCatching {
                                                        defaultRoleLauncher.launch(launcherVm.createDefaultLauncherIntent())
                                                    }
                                                }
                                            },
                                            onSelectMode = launcherVm::setLauncherMode,
                                            onSelectSpinnerStyle = launcherVm::setSpinnerStyle,
                                            onSetGridSize = launcherVm::setGridSize,
                                            onSetIconSize = launcherVm::setIconSizeDp,
                                            onSetShowLabels = launcherVm::setShowIconLabels,
                                            onSetSwipeDown = launcherVm::setSwipeDownAction,
                                            onSetSwipeUp = launcherVm::setSwipeUpAction,
                                            onSetDoubleTap = launcherVm::setDoubleTapAction,
                                            onSetLongPress = launcherVm::setLongPressAction,
                                            onSetPinch = launcherVm::setPinchAction,
                                            onSelectIconStyle = launcherVm::setIconStyleOverride,
                                            onSetThemeAppIcons = launcherVm::setThemeAppIcons,
                                            onSetThemeWidgets = launcherVm::setThemeWidgets,
                                            onSetSyncThemeWallpaper = launcherVm::setSyncThemeWallpaper,
                                            onSetLiquidGlass = launcherVm::setLiquidGlassEnabled,
                                            onToggleAutoRolling = { launcherVm.toggleAutoRolling(settings.autoRollingEnabled) },
                                            onLaunchApp = launcherVm::launchApp,
                                            onToggleHiddenApp = launcherVm::toggleHidden,
                                            onToggleFavoriteApp = launcherVm::toggleFavorite,
                                            onPinAppToHome = { app -> launcherVm.pinToHome(app, pageIndex = 0, slotIndex = 0) },
                                            onUnpinAppFromHome = launcherVm::unpinFromHome,
                                            onOpenAppInfo = launcherVm::openAppInfo,
                                            onOpenGestureConfig = { navController.navigate(GestureConfigRoute) },
                                            onUnlockPremium = { navigateTopPage(4) }
                                        )
                                    }
                                    2 -> {
                                        val wallpapers by wallpaperVm.wallpapers.collectAsStateWithLifecycle()
                                        val total by wallpaperVm.totalMatchingCount.collectAsStateWithLifecycle()
                                        val recent by wallpaperVm.recentlyViewed.collectAsStateWithLifecycle()
                                        val category by wallpaperVm.selectedCategory.collectAsStateWithLifecycle()
                                        val selectedRemoteCat by wallpaperVm.selectedRemoteCategoryName.collectAsStateWithLifecycle()
                                        val remoteCats by wallpaperVm.remoteCategories.collectAsStateWithLifecycle()
                                        val tier by wallpaperVm.tierFilter.collectAsStateWithLifecycle()
                                        val query by wallpaperVm.searchQuery.collectAsStateWithLifecycle()
                                        val billingState by mainVm.billingState.collectAsStateWithLifecycle()

                                        WallpapersScreen(
                                            wallpapers = wallpapers,
                                            totalMatching = total,
                                            recentlyViewed = recent,
                                            selectedCategory = category,
                                            tierFilter = tier,
                                            searchQuery = query,
                                            manifest = wallpaperVm.manifest,
                                            renderer = container.wallpaperRenderer,
                                            isPremium = billingState.isPremium,
                                            remoteCategories = remoteCats,
                                            selectedRemoteCategoryName = selectedRemoteCat,
                                            onSelectCategory = wallpaperVm::selectCategory,
                                            onSelectRemoteCategory = wallpaperVm::selectRemoteCategory,
                                            onSelectTier = wallpaperVm::selectTier,
                                            onSearchQueryChange = wallpaperVm::updateSearchQuery,
                                            onLoadMore = wallpaperVm::loadMore,
                                            onWallpaperClick = { navController.navigate(WallpaperDetailRoute(it.id)) },
                                            onToggleFavorite = wallpaperVm::toggleFavorite
                                        )
                                    }
                                    3 -> {
                                        val themes by themeVm.themes.collectAsStateWithLifecycle()
                                        val total by themeVm.totalMatchingCount.collectAsStateWithLifecycle()
                                        val activeTheme by mainVm.activeTheme.collectAsStateWithLifecycle()
                                        val category by themeVm.selectedCategory.collectAsStateWithLifecycle()
                                        val tier by themeVm.tierFilter.collectAsStateWithLifecycle()
                                        val query by themeVm.searchQuery.collectAsStateWithLifecycle()
                                        val billingState by mainVm.billingState.collectAsStateWithLifecycle()

                                        ThemesScreen(
                                            themes = themes,
                                            totalMatching = total,
                                            activeThemeId = activeTheme.id,
                                            selectedCategory = category,
                                            tierFilter = tier,
                                            searchQuery = query,
                                            manifest = wallpaperVm.manifest,
                                            isPremium = billingState.isPremium,
                                            onSelectCategory = themeVm::selectCategory,
                                            onSelectTier = themeVm::selectTier,
                                            onSearchQueryChange = themeVm::updateSearchQuery,
                                            onLoadMore = themeVm::loadMore,
                                            onThemeClick = { navController.navigate(ThemeDetailRoute(it.id)) },
                                            onToggleFavorite = themeVm::toggleFavorite,
                                            onResetTheme = themeVm::resetTheme
                                        )
                                    }
                                    4 -> {
                                        val billingState by mainVm.billingState.collectAsStateWithLifecycle()
                                        PremiumScreen(
                                            billingState = billingState,
                                            accountLabel = accountLabel,
                                            paymentGatewayEnabled = remoteConfig.paymentGatewayEnabled &&
                                                remoteConfig.paymentBackendUrl.isNotBlank(),
                                            paymentProviderName = remoteConfig.paymentProviderName,
                                            onOpenAccount = { navController.navigate(AccountRoute) },
                                            onPurchasePlan = { sku -> mainVm.purchasePlan(activity, sku) },
                                            onRestorePurchases = mainVm::restorePurchases,
                                            onPreviewProSphere = {
                                                launcherVm.setLauncherMode(com.example.data.model.LauncherMode.SPINNER)
                                                launcherVm.setSpinnerStyle(com.example.data.model.SpinnerStyle.PRO_3D_SPHERE)
                                                navigateTopPage(1)
                                            },
                                            reduceMotion = studioSettings.reduceMotion,
                                            onOpenPrivacy = { navController.navigate(LegalRoute("privacy")) },
                                            onOpenTerms = { navController.navigate(LegalRoute("terms")) }
                                        )
                                    }
                                    else -> {
                                        val settings by mainVm.settingsState.collectAsStateWithLifecycle()
                                        val billingState by mainVm.billingState.collectAsStateWithLifecycle()
                                        val allApps by launcherVm.allApps.collectAsStateWithLifecycle()
                                        val hiddenApps by launcherVm.hiddenApps.collectAsStateWithLifecycle()
                                        SettingsScreen(
                                            settings = settings,
                                            billingState = billingState,
                                            cacheSizeLabel = mainVm.getCacheSizeFormatted(),
                                            accountLabel = accountLabel,
                                            allApps = allApps,
                                            hiddenApps = hiddenApps,
                                            onSelectAppearance = mainVm::setAppearanceMode,
                                            onSelectNavigationMode = mainVm::setNavigationMode,
                                            onSetLiquidGlass = mainVm::setLiquidGlassEnabled,
                                            onToggleHiddenApp = launcherVm::toggleHidden,
                                            onSetReduceMotion = mainVm::setReduceMotion,
                                            onSetAutoCache = mainVm::setAutoCacheThumbnails,
                                            onSetWifiOnly = mainVm::setWifiOnlyDownloads,
                                            onSetAutoRolling = mainVm::setAutoRollingEnabled,
                                            onSetRollingSpeed = mainVm::setRollingSpeed,
                                            onSetDepth3D = mainVm::setDepth3DIntensity,
                                            onSetIconSize = launcherVm::setIconSizeDp,
                                            onSelectBackgroundPreset = mainVm::setBackgroundPreset,
                                            onSetHapticFeedback = mainVm::setHapticFeedbackEnabled,
                                            onOpenAccount = { navController.navigate(AccountRoute) },
                                            onOpenLauncherHub = { navigateTopPage(1) },
                                            onOpenGestureConfig = { navController.navigate(GestureConfigRoute) },
                                            onOpenFavorites = { navController.navigate(FavoritesRoute) },
                                            onOpenPremium = { navigateTopPage(4) },
                                            onRestorePurchases = mainVm::restorePurchases,
                                            onClearCache = mainVm::clearWallpaperCache,
                                            onResetCustomization = mainVm::resetAllCustomizations,
                                            onOpenPrivacy = { navController.navigate(LegalRoute("privacy")) },
                                            onOpenTerms = { navController.navigate(LegalRoute("terms")) }
                                        )
                                    }
                                }
                            }
                        }

                        composable<AccountRoute> {
                            val billingState by mainVm.billingState.collectAsStateWithLifecycle()
                            val profileState = cloudVm?.profileState?.collectAsStateWithLifecycle()?.value
                                ?: CloudUiState.Success(null)
                            val receiptsState = cloudVm?.purchaseHistoryState?.collectAsStateWithLifecycle()?.value
                                ?: CloudUiState.Success(emptyList())
                            val syncStatus = cloudVm?.syncStatus?.collectAsStateWithLifecycle()?.value

                            AccountScreen(
                                profileState = profileState,
                                purchaseHistoryState = receiptsState,
                                billingState = billingState,
                                syncStatus = syncStatus,
                                onBack = { navController.popBackStack() },
                                onSyncNow = { cloudVm?.syncCurrentCustomizationToCloud() },
                                onUpgradePro = {
                                    navController.popBackStack()
                                    navigateTopPage(4)
                                },
                                onSignOut = onSignOut
                            )
                        }

                        composable<SearchRoute> {
                            val query by searchFavVm.searchQuery.collectAsStateWithLifecycle()
                            val filter by searchFavVm.searchFilter.collectAsStateWithLifecycle()
                            val history by searchFavVm.searchHistory.collectAsStateWithLifecycle()
                            val wps by searchFavVm.matchingWallpapers.collectAsStateWithLifecycle()
                            val ths by searchFavVm.matchingThemes.collectAsStateWithLifecycle()
                            val apps by searchFavVm.matchingApps.collectAsStateWithLifecycle()
                            val phoneMatches by searchFavVm.phoneContentMatches.collectAsStateWithLifecycle()
                            val aiState by searchFavVm.aiSearchState.collectAsStateWithLifecycle()
                            val activeTheme by mainVm.activeTheme.collectAsStateWithLifecycle()
                            val settings by mainVm.settingsState.collectAsStateWithLifecycle()
                            val billingState by mainVm.billingState.collectAsStateWithLifecycle()

                            SearchScreen(
                                query = query,
                                activeFilter = filter,
                                searchHistory = history,
                                wallpapers = wps,
                                themes = ths,
                                apps = apps,
                                phoneContentMatches = phoneMatches,
                                aiSearchState = aiState,
                                aiSearchEnabled = remoteConfig.aiSearchEnabled,
                                activeThemeId = activeTheme.id,
                                isPremiumUnlocked = billingState.isPremium,
                                renderer = container.wallpaperRenderer,
                                iconProvider = launcherVm::getAppIcon,
                                onBack = { navController.popBackStack() },
                                onQueryChange = searchFavVm::updateSearchQuery,
                                onSubmitQuery = searchFavVm::submitSearch,
                                onAskAiSearch = searchFavVm::askAiSearch,
                                onOpenPhoneContent = searchFavVm::openPhoneContentItem,
                                onClearQuery = searchFavVm::clearSearch,
                                onClearHistory = searchFavVm::clearSearchHistory,
                                onFilterChange = searchFavVm::setSearchFilter,
                                onWallpaperClick = { navController.navigate(WallpaperDetailRoute(it.id)) },
                                onToggleWallpaperFav = wallpaperVm::toggleFavorite,
                                onThemeClick = { navController.navigate(ThemeDetailRoute(it.id)) },
                                onToggleThemeFav = themeVm::toggleFavorite,
                                onAppClick = launcherVm::launchApp,
                                themedIconMode = settings.themeAppIcons
                            )
                        }

                        composable<FavoritesRoute> {
                            val filter by searchFavVm.favoritesFilter.collectAsStateWithLifecycle()
                            val favWps by searchFavVm.favoriteWallpapers.collectAsStateWithLifecycle()
                            val favThemes by searchFavVm.favoriteThemes.collectAsStateWithLifecycle()
                            val favApps by searchFavVm.favoriteApps.collectAsStateWithLifecycle()
                            val activeTheme by mainVm.activeTheme.collectAsStateWithLifecycle()
                            val settings by mainVm.settingsState.collectAsStateWithLifecycle()
                            val billingState by mainVm.billingState.collectAsStateWithLifecycle()

                            FavoritesScreen(
                                activeFilter = filter,
                                favoriteWallpapers = favWps,
                                favoriteThemes = favThemes,
                                favoriteApps = favApps,
                                activeThemeId = activeTheme.id,
                                isPremiumUnlocked = billingState.isPremium,
                                renderer = container.wallpaperRenderer,
                                iconProvider = launcherVm::getAppIcon,
                                onBack = { navController.popBackStack() },
                                onFilterChange = searchFavVm::setFavoritesFilter,
                                onWallpaperClick = { navController.navigate(WallpaperDetailRoute(it.id)) },
                                onToggleWallpaperFav = wallpaperVm::toggleFavorite,
                                onThemeClick = { navController.navigate(ThemeDetailRoute(it.id)) },
                                onToggleThemeFav = themeVm::toggleFavorite,
                                onAppClick = launcherVm::launchApp,
                                themedIconMode = settings.themeAppIcons
                            )
                        }

                        composable<WallpaperDetailRoute> { entry ->
                            val route: WallpaperDetailRoute = entry.toRoute()
                            val wallpaper by wallpaperVm.observeWallpaper(route.wallpaperId)
                                .collectAsStateWithLifecycle(initialValue = null)
                            val billingState by mainVm.billingState.collectAsStateWithLifecycle()
                            val actionState by wallpaperVm.actionState.collectAsStateWithLifecycle()

                            WallpaperDetailScreen(
                                wallpaper = wallpaper,
                                isPremiumUnlocked = billingState.isPremium,
                                actionState = actionState,
                                renderer = container.wallpaperRenderer,
                                liveWallpapersEnabled = remoteConfig.liveWallpapersEnabled,
                                onMarkViewed = wallpaperVm::markViewed,
                                onBack = { navController.popBackStack() },
                                onToggleFavorite = wallpaperVm::toggleFavorite,
                                onDownload = wallpaperVm::downloadWallpaper,
                                onApply = wallpaperVm::applyWallpaper,
                                onApplyLiveVideo = { item ->
                                    wallpaperVm.applyLiveVideoWallpaper(activity, item) { liveIntent ->
                                        runCatching { activity.startActivity(liveIntent) }
                                    }
                                },
                                onUnlockPremium = {
                                    navController.popBackStack()
                                    navigateTopPage(4)
                                },
                                onClearActionState = wallpaperVm::clearActionState
                            )
                        }

                        composable<ThemeDetailRoute> { entry ->
                            val route: ThemeDetailRoute = entry.toRoute()
                            val theme by themeVm.observeTheme(route.themeId)
                                .collectAsStateWithLifecycle(initialValue = null)
                            val activeTheme by mainVm.activeTheme.collectAsStateWithLifecycle()
                            val billingState by mainVm.billingState.collectAsStateWithLifecycle()
                            val feedback by themeVm.feedbackMessage.collectAsStateWithLifecycle()

                            ThemeDetailScreen(
                                theme = theme,
                                activeThemeId = activeTheme.id,
                                isPremiumUnlocked = billingState.isPremium,
                                feedbackMessage = feedback,
                                onBack = { navController.popBackStack() },
                                onApplyTheme = themeVm::applyTheme,
                                onResetTheme = themeVm::resetTheme,
                                onToggleFavorite = themeVm::toggleFavorite,
                                onUnlockPremium = {
                                    navController.popBackStack()
                                    navigateTopPage(4)
                                },
                                onClearFeedback = themeVm::clearFeedback
                            )
                        }

                        composable<LegalRoute> { entry ->
                            val route: LegalRoute = entry.toRoute()
                            LegalScreen(
                                docType = route.docType,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable<GestureConfigRoute> {
                            val settings by mainVm.settingsState.collectAsStateWithLifecycle()
                            GestureConfigScreen(
                                settings = settings,
                                onBack = { navController.popBackStack() },
                                onSetSwipeUp = launcherVm::setSwipeUpAction,
                                onSetSwipeDown = launcherVm::setSwipeDownAction,
                                onSetDoubleTap = launcherVm::setDoubleTapAction,
                                onSetLongPress = launcherVm::setLongPressAction,
                                onSetPinch = launcherVm::setPinchAction,
                                onSetNavigationMode = mainVm::setNavigationMode,
                                onResetGestures = launcherVm::resetGestureDefaults,
                                onOpenLiveHome = ::openLiveHomeActivity
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingGestureDock(
    items: List<TopLevelNavItem>,
    selectedPage: Int,
    onSelectPage: (Int) -> Unit
) {
    val tokens = LocalLauncherTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(tokens.surface.copy(alpha = if (tokens.isDark) 0.88f else 0.94f))
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            tokens.primary.copy(alpha = 0.35f),
                            tokens.borderSubtle,
                            tokens.secondary.copy(alpha = 0.35f)
                        )
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = selectedPage == item.pageIndex
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (selected) tokens.primary.copy(alpha = 0.16f) else Color.Transparent
                        )
                        .clickable { onSelectPage(item.pageIndex) }
                        .testTag(item.testTag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        tint = if (selected) tokens.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (selected) tokens.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Subtle swipe gesture pill indicator
        Box(
            modifier = Modifier
                .width(52.dp)
                .height(3.5.dp)
                .clip(CircleShape)
                .background(tokens.primary.copy(alpha = 0.48f))
        )
    }
}

@Composable
private fun ClassicButtonNavigationDock(
    items: List<TopLevelNavItem>,
    selectedPage: Int,
    onSelectPage: (Int) -> Unit,
    onBackPress: () -> Unit,
    onHomePress: () -> Unit,
    onOpenLiveHome: () -> Unit
) {
    val tokens = LocalLauncherTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(tokens.surface.copy(alpha = if (tokens.isDark) 0.94f else 0.97f))
            .navigationBarsPadding()
            .testTag("studio_button_dock")
    ) {
        // Tactile 3-Button Navigation Action Strip (Back, Home, 3D Home)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.8.dp, tokens.borderSubtle)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onBackPress)
                    .testTag("nav_btn_back"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Back",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onHomePress)
                    .testTag("nav_btn_home"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = "Home Tab",
                    tint = tokens.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Home",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = tokens.primary
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpenLiveHome)
                    .testTag("nav_btn_live_launcher"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.RocketLaunch,
                    contentDescription = "Launch 3D Home",
                    tint = tokens.secondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "3D Home",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Classic Material 3 NavigationBar for primary tabs
        NavigationBar(
            containerColor = Color.Transparent,
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            items.forEach { item ->
                val selected = selectedPage == item.pageIndex
                NavigationBarItem(
                    selected = selected,
                    onClick = { onSelectPage(item.pageIndex) },
                    icon = {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier.testTag(item.testTag)
                )
            }
        }
    }
}

