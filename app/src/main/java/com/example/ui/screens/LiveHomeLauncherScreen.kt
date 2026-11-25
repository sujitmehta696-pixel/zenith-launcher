package com.example.ui.screens

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.launcher.GestureController
import com.example.data.launcher.GestureExecutionResult
import com.example.data.model.AppSortOrder
import com.example.data.model.GestureAction
import com.example.data.model.InstalledAppInfo
import com.example.data.model.LauncherMode
import com.example.data.model.LauncherSettingsState
import com.example.data.model.NavigationMode
import com.example.data.model.WallpaperItem
import com.example.data.wallpaper.LiveWeatherSnapshot
import com.example.data.wallpaper.WallpaperRenderer
import com.example.ui.components.AppIconItem
import com.example.ui.components.DynamicOrbitBackdrop
import com.example.ui.components.EdgePanelOverlay
import com.example.ui.components.GlassCard
import com.example.ui.components.PremiumControlPanel
import com.example.ui.components.SpinnerLauncherWheel
import com.example.ui.components.ThemedHomeWidgetsStrip
import com.example.ui.components.WallpaperThumbnailImage
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.LocalLauncherTokens
import kotlinx.coroutines.launch

private const val APPWIDGET_HOST_ID = 4096

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveHomeLauncherScreen(
    settings: LauncherSettingsState,
    homeApps: List<InstalledAppInfo>,
    allVisibleApps: List<InstalledAppInfo>,
    drawerApps: List<InstalledAppInfo>,
    activeWallpaper: WallpaperItem?,
    wallpaperRenderer: WallpaperRenderer,
    searchQuery: String,
    sortOrder: AppSortOrder,
    statusBanner: String? = null,
    badgeCounts: Map<String, Int> = emptyMap(),
    notificationBadgesEnabled: Boolean = true,
    edgePanelEnabled: Boolean = true,
    controlCenterEnabled: Boolean = true,
    liveWeather: LiveWeatherSnapshot? = null,
    iconProvider: (String) -> Drawable?,
    queryShortcuts: (String) -> List<ShortcutInfo>,
    onLaunchShortcut: (ShortcutInfo) -> Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSortOrderChange: (AppSortOrder) -> Unit,
    onLaunchApp: (InstalledAppInfo) -> Unit,
    onToggleFavorite: (InstalledAppInfo) -> Unit,
    onToggleHidden: (InstalledAppInfo) -> Unit,
    onPinToHome: (InstalledAppInfo) -> Unit,
    onUnpinFromHome: (InstalledAppInfo) -> Unit,
    onOpenAppInfo: (String) -> Unit,
    onUninstallApp: (String) -> Unit,
    onToggleAutoRolling: () -> Unit = {},
    onClearStatusBanner: () -> Unit = {},
    onOpenUniversalSearch: () -> Unit = {},
    onOpenStudioSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tokens = LocalLauncherTokens.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val pagerState = rememberPagerState(pageCount = { 3 })
    var showDrawer by remember { mutableStateOf(false) }
    var showEdgePanel by remember { mutableStateOf(false) }
    var showControlCenter by remember { mutableStateOf(false) }
    var selectedAppForMenu by remember { mutableStateOf<InstalledAppInfo?>(null) }
    var shortcutsForMenu by remember { mutableStateOf<List<ShortcutInfo>>(emptyList()) }
    var favoritesOnlyInDrawer by remember { mutableStateOf(false) }
    var externalSpinTrigger by remember { mutableIntStateOf(0) }
    var accessibilityDialogMessage by remember { mutableStateOf<String?>(null) }

    val effectiveBadgeCounts = if (notificationBadgesEnabled) badgeCounts else emptyMap()

    LaunchedEffect(statusBanner) {
        if (statusBanner != null) {
            snackbarHostState.showSnackbar(statusBanner)
            onClearStatusBanner()
        }
    }

    fun dispatchGesture(action: GestureAction) {
        when (action) {
            GestureAction.NONE -> Unit
            GestureAction.OPEN_APP_DRAWER -> showDrawer = true
            GestureAction.OPEN_SEARCH -> showDrawer = true
            GestureAction.SPIN_WHEEL -> externalSpinTrigger++
            GestureAction.OPEN_CONTROL_CENTER -> {
                if (controlCenterEnabled) {
                    showControlCenter = true
                } else {
                    dispatchGesture(GestureAction.OPEN_QUICK_SETTINGS)
                }
            }
            GestureAction.OPEN_EDGE_PANEL -> {
                if (edgePanelEnabled) {
                    showEdgePanel = true
                }
            }
            GestureAction.OPEN_STUDIO -> onOpenStudioSettings()
            GestureAction.OPEN_NOTIFICATIONS,
            GestureAction.OPEN_QUICK_SETTINGS,
            GestureAction.LOCK_SCREEN -> {
                when (val result = GestureController.executeSystemGesture(context, action)) {
                    GestureExecutionResult.HandledInLauncher -> Unit
                    is GestureExecutionResult.SystemActionExecuted -> Unit
                    is GestureExecutionResult.NeedsAccessibilityLockPermission -> {
                        accessibilityDialogMessage = result.message
                    }
                    is GestureExecutionResult.InfoFallback -> {
                        scope.launch { snackbarHostState.showSnackbar(result.message) }
                    }
                }
            }
        }
    }

    // Real Android AppWidgetHost setup for Page 2 with existing widget restoration (safely guarded for restricted OEM profiles)
    val appWidgetManager = remember { runCatching { AppWidgetManager.getInstance(context) }.getOrNull() }
    val appWidgetHost = remember { runCatching { AppWidgetHost(context, APPWIDGET_HOST_ID) }.getOrNull() }
    val boundWidgetIds = remember {
        mutableStateListOf<Int>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appWidgetHost != null) {
                runCatching { appWidgetHost.appWidgetIds.toList() }
                    .getOrDefault(emptyList())
                    .forEach { add(it) }
            }
        }
    }
    var pendingWidgetId by remember { mutableStateOf<Int?>(null) }

    DisposableEffect(appWidgetHost) {
        runCatching { appWidgetHost?.startListening() }
        onDispose { runCatching { appWidgetHost?.stopListening() } }
    }

    val widgetPickLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val widgetId = pendingWidgetId ?: return@rememberLauncherForActivityResult
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            if (!boundWidgetIds.contains(widgetId)) {
                boundWidgetIds.add(widgetId)
            }
        } else {
            runCatching { appWidgetHost?.deleteAppWidgetId(widgetId) }
        }
        pendingWidgetId = null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(settings.doubleTapAction, settings.longPressAction) {
                detectTapGestures(
                    onDoubleTap = { dispatchGesture(settings.doubleTapAction) },
                    onLongPress = { dispatchGesture(settings.longPressAction) }
                )
            }
            .pointerInput(settings.swipeDownAction, settings.swipeUpAction) {
                var totalVerticalDrag = 0f
                var gestureFired = false
                detectVerticalDragGestures(
                    onDragStart = {
                        totalVerticalDrag = 0f
                        gestureFired = false
                    },
                    onVerticalDrag = { _, dragAmount ->
                        totalVerticalDrag += dragAmount
                        if (!gestureFired && totalVerticalDrag > 85f) {
                            gestureFired = true
                            dispatchGesture(settings.swipeDownAction)
                        } else if (!gestureFired && totalVerticalDrag < -85f) {
                            gestureFired = true
                            dispatchGesture(settings.swipeUpAction)
                        }
                    }
                )
            }
            .pointerInput(settings.pinchAction) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    var pinchZoom = 1f
                    var pinchFired = false
                    do {
                        val event = awaitPointerEvent()
                        if (event.changes.count { it.pressed } >= 2) {
                            pinchZoom *= event.calculateZoom()
                            if (!pinchFired && (pinchZoom < 0.78f || pinchZoom > 1.28f)) {
                                pinchFired = true
                                dispatchGesture(settings.pinchAction)
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
    ) {
        // Dynamic Futuristic Lighting Backdrop
        DynamicOrbitBackdrop(
            preset = settings.backgroundPreset,
            reduceMotion = settings.reduceMotion
        )

        // Theme-Paired Backdrop Wallpaper Layer
        if (settings.syncThemeWallpaper && activeWallpaper != null) {
            WallpaperThumbnailImage(
                item = activeWallpaper,
                renderer = wallpaperRenderer,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.58f)
            )
        }

        // Atmospheric Theme Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x66080B18),
                            Color(0x33080B18),
                            Color(0x99080B18)
                        )
                    )
                )
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Search & Quick Settings Shortcut
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(tokens.surface.copy(alpha = 0.78f))
                            .border(1.dp, tokens.borderSubtle, RoundedCornerShape(24.dp))
                            .clickable { showDrawer = true }
                            .padding(horizontal = 16.dp)
                            .testTag("live_home_search_pill"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search apps",
                            tint = tokens.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Search apps",
                            style = MaterialTheme.typography.bodyMedium,
                            color = tokens.textSecondary
                        )
                    }

                    if (controlCenterEnabled) {
                        IconButton(
                            onClick = { showControlCenter = true },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(tokens.surface.copy(alpha = 0.78f))
                                .border(1.dp, tokens.borderSubtle, CircleShape)
                                .testTag("live_home_control_center_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = "Control Center",
                                tint = ElectricCyan
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenStudioSettings,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(tokens.surface.copy(alpha = 0.78f))
                            .border(1.dp, tokens.borderSubtle, CircleShape)
                            .testTag("live_home_studio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = tokens.textPrimary
                        )
                    }
                }

                // Multi-Page Workspace Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { page ->
                    when (page) {
                        0 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ThemedHomeWidgetsStrip(
                                    themedWidgetsEnabled = settings.themeWidgets,
                                    weather = liveWeather,
                                    onPulseSpin = { externalSpinTrigger++ },
                                    onOpenStudio = onOpenStudioSettings
                                )

                                when (settings.launcherMode) {
                                    LauncherMode.SPINNER -> {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            SpinnerLauncherWheel(
                                                apps = allVisibleApps.ifEmpty { homeApps },
                                                spinnerStyle = settings.spinnerStyle,
                                                iconProvider = iconProvider,
                                                showLabels = settings.showIconLabels,
                                                themedIconMode = settings.themeAppIcons,
                                                badgeCounts = effectiveBadgeCounts,
                                                autoRollingEnabled = settings.autoRollingEnabled,
                                                rollingSpeed = settings.rollingSpeed,
                                                depth3DIntensity = settings.depth3DIntensity,
                                                hapticFeedbackEnabled = settings.hapticFeedbackEnabled,
                                                externalPulseTrigger = externalSpinTrigger,
                                                onToggleAutoRolling = onToggleAutoRolling,
                                                onAppClick = onLaunchApp,
                                                onAppLongClick = { app ->
                                                    shortcutsForMenu = queryShortcuts(app.packageName)
                                                    selectedAppForMenu = app
                                                }
                                            )
                                        }
                                    }
                                    LauncherMode.GRID -> {
                                        val pageApps = homeApps.take(settings.gridColumns * settings.gridRows)
                                        LazyVerticalGrid(
                                            columns = GridCells.Fixed(settings.gridColumns),
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            items(pageApps, key = { "p0_${it.packageName}_${it.activityName}" }) { app ->
                                                AppIconItem(
                                                    app = app,
                                                    iconDrawable = iconProvider(app.packageName),
                                                    iconSize = settings.iconSizeDp.dp,
                                                    showLabel = settings.showIconLabels,
                                                    labelColor = Color.White,
                                                    themedIconMode = settings.themeAppIcons,
                                                    badgeCount = effectiveBadgeCounts[app.packageName] ?: 0,
                                                    onClick = { onLaunchApp(app) },
                                                    onLongClick = {
                                                        shortcutsForMenu = queryShortcuts(app.packageName)
                                                        selectedAppForMenu = app
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    LauncherMode.MINIMAL -> {
                                        LazyColumn(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            items(homeApps.take(8), key = { "min_${it.packageName}_${it.activityName}" }) { app ->
                                                GlassCard(
                                                    onClick = { onLaunchApp(app) },
                                                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = app.label,
                                                            style = MaterialTheme.typography.titleLarge,
                                                            color = tokens.textPrimary
                                                        )
                                                        if (app.isFavorite) {
                                                            Icon(
                                                                imageVector = Icons.Rounded.Favorite,
                                                                contentDescription = null,
                                                                tint = Color(0xFFF43F5E),
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            val pageCapacity = settings.gridColumns * settings.gridRows
                            val pageApps = homeApps.drop(8).take(pageCapacity).ifEmpty { homeApps.take(pageCapacity) }
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(settings.gridColumns),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(pageApps, key = { "p1_${it.packageName}" }) { app ->
                                    AppIconItem(
                                        app = app,
                                        iconDrawable = iconProvider(app.packageName),
                                        iconSize = settings.iconSizeDp.dp,
                                        showLabel = settings.showIconLabels,
                                        labelColor = Color.White,
                                        themedIconMode = settings.themeAppIcons,
                                        badgeCount = effectiveBadgeCounts[app.packageName] ?: 0,
                                        onClick = { onLaunchApp(app) },
                                        onLongClick = {
                                            shortcutsForMenu = queryShortcuts(app.packageName)
                                            selectedAppForMenu = app
                                        }
                                    )
                                }
                            }
                        }

                        else -> {
                            // Page 2: Themed Telemetry + Android AppWidgetHost Workspace
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    ThemedHomeWidgetsStrip(
                                        themedWidgetsEnabled = settings.themeWidgets,
                                        weather = liveWeather,
                                        onPulseSpin = { externalSpinTrigger++ },
                                        onOpenStudio = onOpenStudioSettings
                                    )
                                }
                                item {
                                    GlassCard(contentPadding = PaddingValues(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "System Widgets",
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            OutlinedButton(
                                                onClick = {
                                                    val host = appWidgetHost
                                                    if (host == null) {
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar("System widget host unavailable on this device profile.")
                                                        }
                                                    } else {
                                                        runCatching {
                                                            val id = host.allocateAppWidgetId()
                                                            val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
                                                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                                                            }
                                                            if (pickIntent.resolveActivity(context.packageManager) != null) {
                                                                pendingWidgetId = id
                                                                widgetPickLauncher.launch(pickIntent)
                                                            } else {
                                                                host.deleteAppWidgetId(id)
                                                                scope.launch {
                                                                    snackbarHostState.showSnackbar("System widget picker unavailable on this device.")
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Rounded.Add, contentDescription = null)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Add")
                                            }
                                        }
                                    }
                                }

                                items(boundWidgetIds, key = { it }) { widgetId ->
                                    val info = runCatching { appWidgetManager?.getAppWidgetInfo(widgetId) }.getOrNull()
                                    if (info != null && appWidgetHost != null) {
                                        GlassCard(contentPadding = PaddingValues(8.dp)) {
                                            AndroidView(
                                                factory = { ctx ->
                                                    runCatching {
                                                        appWidgetHost.createView(ctx, widgetId, info).apply {
                                                            setAppWidget(widgetId, info)
                                                            layoutParams = ViewGroup.LayoutParams(
                                                                ViewGroup.LayoutParams.MATCH_PARENT,
                                                                360
                                                            )
                                                        }
                                                    }.getOrElse {
                                                        android.widget.TextView(ctx).apply {
                                                            text = "Widget unavailable"
                                                            setPadding(24, 24, 24, 24)
                                                        }
                                                    }
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(180.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Page Indicator + Minimal Bottom Dock
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(3) { idx ->
                            val active = pagerState.currentPage == idx
                            Box(
                                modifier = Modifier
                                    .size(width = if (active) 20.dp else 7.dp, height = 7.dp)
                                    .clip(CircleShape)
                                    .background(if (active) tokens.primary else Color.White.copy(alpha = 0.35f))
                            )
                        }
                    }

                    val dockApps = remember(drawerApps) {
                        val favs = drawerApps.filter { it.isFavorite }
                        (favs + drawerApps).distinctBy { it.packageName }.take(4)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(tokens.surface.copy(alpha = 0.84f))
                            .border(1.dp, tokens.borderSubtle, RoundedCornerShape(28.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        dockApps.forEach { app ->
                            AppIconItem(
                                app = app,
                                iconDrawable = iconProvider(app.packageName),
                                iconSize = 44.dp,
                                showLabel = false,
                                themedIconMode = settings.themeAppIcons,
                                badgeCount = effectiveBadgeCounts[app.packageName] ?: 0,
                                onClick = { onLaunchApp(app) },
                                onLongClick = {
                                    shortcutsForMenu = queryShortcuts(app.packageName)
                                    selectedAppForMenu = app
                                }
                            )
                        }

                        IconButton(
                            onClick = { showDrawer = true },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(tokens.primary.copy(alpha = 0.22f))
                                .testTag("live_home_open_drawer")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Apps,
                                contentDescription = "Open App Drawer",
                                tint = tokens.primary
                            )
                        }
                    }

                    // Dual Navigation Mode Bar on Live Home: Classic 3-Button Bar OR Gesture Pill
                    if (settings.navigationMode == NavigationMode.BUTTONS) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(tokens.surface.copy(alpha = 0.88f))
                                .border(1.dp, tokens.borderSubtle, RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 2.dp)
                                .testTag("live_home_button_nav_bar"),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onOpenStudioSettings,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("live_nav_btn_back")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Open Studio",
                                    tint = Color.White
                                )
                            }
                            IconButton(
                                onClick = {
                                    scope.launch { pagerState.animateScrollToPage(0) }
                                    externalSpinTrigger++
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("live_nav_btn_home")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Home,
                                    contentDescription = "Home & Spin",
                                    tint = tokens.primary
                                )
                            }
                            IconButton(
                                onClick = { showDrawer = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("live_nav_btn_drawer")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Apps,
                                    contentDescription = "All Apps",
                                    tint = Color.White
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .clip(CircleShape)
                                .background(tokens.surface.copy(alpha = 0.65f))
                                .border(0.8.dp, tokens.primary.copy(alpha = 0.42f), CircleShape)
                                .clickable { dispatchGesture(settings.swipeUpAction) }
                                .padding(horizontal = 18.dp, vertical = 6.dp)
                                .testTag("live_home_gesture_nav_pill")
                        ) {
                            Text(
                                text = "↑ ${settings.swipeUpAction.displayName}  •  ↓ ${settings.swipeDownAction.displayName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.88f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            if (edgePanelEnabled) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(12.dp)
                        .height(92.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                        .background(ElectricCyan.copy(alpha = 0.42f))
                        .clickable { showEdgePanel = true }
                        .testTag("edge_panel_handle")
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        EdgePanelOverlay(
            visible = showEdgePanel && edgePanelEnabled,
            favoriteApps = drawerApps.filter { it.isFavorite }.ifEmpty { homeApps },
            badgeCounts = badgeCounts,
            notificationBadgesEnabled = notificationBadgesEnabled,
            themedIconMode = settings.themeAppIcons,
            iconProvider = iconProvider,
            onDismiss = { showEdgePanel = false },
            onLaunchApp = onLaunchApp,
            onOpenControlCenter = { showControlCenter = true },
            onOpenSearch = onOpenUniversalSearch,
            onOpenDrawer = { showDrawer = true },
            onOpenStudio = onOpenStudioSettings
        )

        if (showControlCenter && controlCenterEnabled) {
            PremiumControlPanel(
                onDismiss = { showControlCenter = false },
                onFeedback = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
            )
        }
    }

    // App Drawer Sheet
    if (showDrawer) {
        ModalBottomSheet(onDismissRequest = { showDrawer = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(540.dp)
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Apps (${drawerApps.size})", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                        FilterChip(
                            selected = false,
                            onClick = {
                                showDrawer = false
                                onOpenUniversalSearch()
                            },
                            leadingIcon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp)) },
                            label = { Text("AI Search") }
                        )
                        IconButton(onClick = { showDrawer = false }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close drawer")
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search installed apps") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppSortOrder.entries.forEach { order ->
                            FilterChip(
                                selected = sortOrder == order,
                                onClick = { onSortOrderChange(order) },
                                label = { Text(order.displayName) }
                            )
                        }
                        FilterChip(
                            selected = favoritesOnlyInDrawer,
                            onClick = { favoritesOnlyInDrawer = !favoritesOnlyInDrawer },
                            label = { Text("Favorites") }
                        )
                    }

                    val shownApps = if (favoritesOnlyInDrawer) drawerApps.filter { it.isFavorite } else drawerApps
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(settings.gridColumns),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(shownApps, key = { "drawer_${it.packageName}_${it.activityName}" }) { app ->
                            AppIconItem(
                                app = app,
                                iconDrawable = iconProvider(app.packageName),
                                iconSize = settings.iconSizeDp.dp,
                                showLabel = true,
                                themedIconMode = settings.themeAppIcons,
                                badgeCount = effectiveBadgeCounts[app.packageName] ?: 0,
                                onClick = {
                                    showDrawer = false
                                    onLaunchApp(app)
                                },
                                onLongClick = {
                                    shortcutsForMenu = queryShortcuts(app.packageName)
                                    selectedAppForMenu = app
                                }
                            )
                        }
                    }
                }
            }
        }

    accessibilityDialogMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { accessibilityDialogMessage = null },
            title = { Text("Accessibility Permission") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(
                    onClick = {
                        accessibilityDialogMessage = null
                        GestureController.openAccessibilitySettings(context)
                    }
                ) {
                    Text("Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { accessibilityDialogMessage = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Long-Press App Context Sheet
    selectedAppForMenu?.let { app ->
        ModalBottomSheet(onDismissRequest = { selectedAppForMenu = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && shortcutsForMenu.isNotEmpty()) {
                    shortcutsForMenu.forEach { shortcut ->
                        ListItem(
                            headlineContent = { Text(shortcut.shortLabel?.toString() ?: "Shortcut") },
                            supportingContent = { Text("Shortcut") },
                            modifier = Modifier.clickable {
                                onLaunchShortcut(shortcut)
                                selectedAppForMenu = null
                            }
                        )
                    }
                }

                ListItem(
                    headlineContent = { Text(if (app.isPinnedHome) "Unpin from Home" else "Pin to Home") },
                    leadingContent = { Icon(Icons.Rounded.PushPin, contentDescription = null) },
                    modifier = Modifier.clickable {
                        if (app.isPinnedHome) onUnpinFromHome(app) else onPinToHome(app)
                        selectedAppForMenu = null
                    }
                )

                ListItem(
                    headlineContent = { Text(if (app.isFavorite) "Remove Favorite" else "Add Favorite") },
                    leadingContent = {
                        Icon(
                            if (app.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.clickable {
                        onToggleFavorite(app)
                        selectedAppForMenu = null
                    }
                )

                ListItem(
                    headlineContent = { Text("Hide App") },
                    leadingContent = { Icon(Icons.Rounded.VisibilityOff, contentDescription = null) },
                    modifier = Modifier.clickable {
                        onToggleHidden(app)
                        selectedAppForMenu = null
                    }
                )

                ListItem(
                    headlineContent = { Text("App Info") },
                    leadingContent = { Icon(Icons.Rounded.Info, contentDescription = null) },
                    modifier = Modifier.clickable {
                        onOpenAppInfo(app.packageName)
                        selectedAppForMenu = null
                    }
                )

                if (!app.isSystemApp) {
                    ListItem(
                        headlineContent = { Text("Uninstall") },
                        leadingContent = { Icon(Icons.Rounded.Delete, contentDescription = null) },
                        modifier = Modifier.clickable {
                            onUninstallApp(app.packageName)
                            selectedAppForMenu = null
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
