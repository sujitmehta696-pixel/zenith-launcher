package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.rounded.ViewSidebar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.LauncherAccessibilityService
import com.example.data.launcher.GestureController
import com.example.data.launcher.GestureExecutionResult
import com.example.data.model.GestureAction
import com.example.data.model.LauncherSettingsState
import com.example.data.model.NavigationMode
import com.example.ui.components.GestureSlot
import com.example.ui.components.GlassCard
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LocalLauncherTokens
import kotlinx.coroutines.launch

private data class GesturePreset(
    val id: String,
    val label: String,
    val swipeUp: GestureAction,
    val swipeDown: GestureAction,
    val doubleTap: GestureAction,
    val longPress: GestureAction,
    val pinch: GestureAction = GestureAction.OPEN_EDGE_PANEL
)

private val gesturePresets = listOf(
    GesturePreset(
        id = "3D_ORBIT",
        label = "3D Orbit Pro",
        swipeUp = GestureAction.OPEN_APP_DRAWER,
        swipeDown = GestureAction.OPEN_CONTROL_CENTER,
        doubleTap = GestureAction.LOCK_SCREEN,
        longPress = GestureAction.OPEN_STUDIO,
        pinch = GestureAction.OPEN_EDGE_PANEL
    ),
    GesturePreset(
        id = "ONE_HANDED",
        label = "One-Handed",
        swipeUp = GestureAction.OPEN_SEARCH,
        swipeDown = GestureAction.OPEN_QUICK_SETTINGS,
        doubleTap = GestureAction.SPIN_WHEEL,
        longPress = GestureAction.OPEN_APP_DRAWER,
        pinch = GestureAction.OPEN_CONTROL_CENTER
    ),
    GesturePreset(
        id = "MINIMAL",
        label = "Minimal Focus",
        swipeUp = GestureAction.OPEN_APP_DRAWER,
        swipeDown = GestureAction.OPEN_NOTIFICATIONS,
        doubleTap = GestureAction.LOCK_SCREEN,
        longPress = GestureAction.NONE,
        pinch = GestureAction.NONE
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureConfigScreen(
    settings: LauncherSettingsState,
    onBack: () -> Unit,
    onSetSwipeUp: (GestureAction) -> Unit,
    onSetSwipeDown: (GestureAction) -> Unit,
    onSetDoubleTap: (GestureAction) -> Unit,
    onSetLongPress: (GestureAction) -> Unit,
    onSetPinch: (GestureAction) -> Unit = {},
    onSetNavigationMode: (NavigationMode) -> Unit,
    onResetGestures: () -> Unit,
    onOpenLiveHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tokens = LocalLauncherTokens.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    var editingSlot by remember { mutableStateOf<GestureSlot?>(null) }
    var lastTestTitle by remember { mutableStateOf("Try a gesture here") }
    var lastTestSubtitle by remember { mutableStateOf("Swipe Up, Swipe Down, Double Tap, or Long Press") }
    var lastTestIcon by remember { mutableStateOf(Icons.Rounded.TouchApp) }
    val pulseProgress = remember { Animatable(0f) }

    val needsAccessibility = settings.swipeUpAction.requiresAccessibility() ||
        settings.swipeDownAction.requiresAccessibility() ||
        settings.doubleTapAction.requiresAccessibility() ||
        settings.longPressAction.requiresAccessibility() ||
        settings.pinchAction.requiresAccessibility()

    var accessibilityEnabled by remember(needsAccessibility) {
        mutableStateOf(LauncherAccessibilityService.isServiceEnabled(context))
    }

    DisposableEffect(lifecycleOwner, needsAccessibility) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                accessibilityEnabled = LauncherAccessibilityService.isServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun triggerPreviewPulse(slot: GestureSlot, action: GestureAction) {
        lastTestTitle = "${slot.title} → ${action.displayName}"
        lastTestSubtitle = action.description
        lastTestIcon = action.icon()
        scope.launch {
            pulseProgress.snapTo(0f)
            pulseProgress.animateTo(1f, animationSpec = tween(550, easing = FastOutSlowInEasing))
        }
    }

    fun executeSystemTest(action: GestureAction) {
        when (val result = GestureController.executeSystemGesture(context, action)) {
            GestureExecutionResult.HandledInLauncher -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Tested '${action.displayName}' — ready on your Home screen!")
                }
            }
            is GestureExecutionResult.SystemActionExecuted -> {
                scope.launch { snackbarHostState.showSnackbar(result.message) }
            }
            is GestureExecutionResult.NeedsAccessibilityLockPermission -> {
                scope.launch { snackbarHostState.showSnackbar(result.message) }
            }
            is GestureExecutionResult.InfoFallback -> {
                scope.launch { snackbarHostState.showSnackbar(result.message) }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("gesture_config_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
        ) {
            // Pinned Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(tokens.surface)
                            .border(1.dp, tokens.borderSubtle, CircleShape)
                            .testTag("gesture_config_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Column {
                        Text(
                            text = "Gestures & Navigation",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Home actions & app navigation style",
                            style = MaterialTheme.typography.labelMedium,
                            color = tokens.primary
                        )
                    }
                }

                TextButton(
                    onClick = {
                        onResetGestures()
                        scope.launch {
                            snackbarHostState.showSnackbar("Default home gestures restored")
                        }
                    },
                    modifier = Modifier.testTag("gesture_config_reset")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset")
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("gesture_config_list"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

            // Interactive Live Gesture Test Pad
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(168.dp)
                        .clip(RoundedCornerShape(tokens.cardRadius))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    tokens.surface.copy(alpha = 0.92f),
                                    tokens.primary.copy(alpha = 0.14f),
                                    tokens.secondary.copy(alpha = 0.14f)
                                )
                            )
                        )
                        .border(
                            1.2.dp,
                            Brush.horizontalGradient(
                                listOf(
                                    tokens.primary.copy(alpha = 0.55f),
                                    tokens.secondary.copy(alpha = 0.45f)
                                )
                            ),
                            RoundedCornerShape(tokens.cardRadius)
                        )
                        .pointerInput(
                            settings.doubleTapAction,
                            settings.longPressAction
                        ) {
                            detectTapGestures(
                                onDoubleTap = {
                                    triggerPreviewPulse(GestureSlot.DOUBLE_TAP, settings.doubleTapAction)
                                },
                                onLongPress = {
                                    triggerPreviewPulse(GestureSlot.LONG_PRESS, settings.longPressAction)
                                }
                            )
                        }
                        .pointerInput(settings.swipeUpAction, settings.swipeDownAction) {
                            var dragDelta = 0f
                            var fired = false
                            detectVerticalDragGestures(
                                onDragStart = {
                                    dragDelta = 0f
                                    fired = false
                                },
                                onVerticalDrag = { _, dragAmount ->
                                    dragDelta += dragAmount
                                    if (!fired && dragDelta < -60f) {
                                        fired = true
                                        triggerPreviewPulse(GestureSlot.SWIPE_UP, settings.swipeUpAction)
                                    } else if (!fired && dragDelta > 60f) {
                                        fired = true
                                        triggerPreviewPulse(GestureSlot.SWIPE_DOWN, settings.swipeDownAction)
                                    }
                                }
                            )
                        }
                        .testTag("gesture_live_test_pad"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val p = pulseProgress.value
                        val center = Offset(size.width / 2f, size.height / 2f)
                        if (p in 0.01f..0.99f) {
                            drawCircle(
                                color = tokens.primary.copy(alpha = (1f - p) * 0.45f),
                                radius = size.minDimension * (0.25f + p * 0.55f),
                                center = center,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                        drawCircle(
                            color = tokens.secondary.copy(alpha = 0.14f),
                            radius = size.minDimension * 0.36f,
                            center = center,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(tokens.primary.copy(alpha = 0.18f))
                                .border(1.dp, tokens.primary.copy(alpha = 0.45f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = lastTestIcon,
                                contentDescription = null,
                                tint = tokens.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        AnimatedContent(targetState = lastTestTitle, label = "gesture_pad_title") { title ->
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = lastTestSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Quick Presets Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Quick Gesture Presets",
                        style = MaterialTheme.typography.titleMedium
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(gesturePresets, key = { it.id }) { preset ->
                            val isMatching = settings.swipeUpAction == preset.swipeUp &&
                                settings.swipeDownAction == preset.swipeDown &&
                                settings.doubleTapAction == preset.doubleTap &&
                                settings.longPressAction == preset.longPress
                            FilterChip(
                                selected = isMatching,
                                onClick = {
                                    onSetSwipeUp(preset.swipeUp)
                                    onSetSwipeDown(preset.swipeDown)
                                    onSetDoubleTap(preset.doubleTap)
                                    onSetLongPress(preset.longPress)
                                    onSetPinch(preset.pinch)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Applied '${preset.label}' preset")
                                    }
                                },
                                label = { Text(preset.label) },
                                modifier = Modifier.testTag("gesture_preset_${preset.id}")
                            )
                        }
                    }
                }
            }

            // Home Screen Gestures Section
            item {
                Text(
                    text = "Home Screen Gestures",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GestureSlotConfigCard(
                        slot = GestureSlot.SWIPE_UP,
                        subtitle = "Slide upward anywhere on the Home screen",
                        action = settings.swipeUpAction,
                        onConfigure = { editingSlot = GestureSlot.SWIPE_UP },
                        onTestAction = { executeSystemTest(settings.swipeUpAction) }
                    )
                    GestureSlotConfigCard(
                        slot = GestureSlot.SWIPE_DOWN,
                        subtitle = "Pull downward on the Home screen",
                        action = settings.swipeDownAction,
                        onConfigure = { editingSlot = GestureSlot.SWIPE_DOWN },
                        onTestAction = { executeSystemTest(settings.swipeDownAction) }
                    )
                    GestureSlotConfigCard(
                        slot = GestureSlot.DOUBLE_TAP,
                        subtitle = "Tap twice quickly on empty Home space",
                        action = settings.doubleTapAction,
                        onConfigure = { editingSlot = GestureSlot.DOUBLE_TAP },
                        onTestAction = { executeSystemTest(settings.doubleTapAction) }
                    )
                    GestureSlotConfigCard(
                        slot = GestureSlot.LONG_PRESS,
                        subtitle = "Press and hold on the Home workspace",
                        action = settings.longPressAction,
                        onConfigure = { editingSlot = GestureSlot.LONG_PRESS },
                        onTestAction = { executeSystemTest(settings.longPressAction) }
                    )
                    GestureSlotConfigCard(
                        slot = GestureSlot.PINCH,
                        subtitle = "Pinch inward with two fingers on Home",
                        action = settings.pinchAction,
                        onConfigure = { editingSlot = GestureSlot.PINCH },
                        onTestAction = { executeSystemTest(settings.pinchAction) }
                    )
                }
            }

            // App Navigation Style Switcher (Gesture vs Buttons)
            item {
                GlassCard(contentPadding = PaddingValues(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Swipe,
                            contentDescription = null,
                            tint = tokens.primary
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "App Navigation Mode",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Choose swipe gesture bar or classic clickable navigation buttons",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NavigationModeCard(
                            mode = NavigationMode.GESTURE,
                            selected = settings.navigationMode == NavigationMode.GESTURE,
                            icon = Icons.Rounded.Gesture,
                            badgeText = "Swipe Pill",
                            onClick = { onSetNavigationMode(NavigationMode.GESTURE) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("gesture_nav_mode_GESTURE")
                        )
                        NavigationModeCard(
                            mode = NavigationMode.BUTTONS,
                            selected = settings.navigationMode == NavigationMode.BUTTONS,
                            icon = Icons.Rounded.ViewAgenda,
                            badgeText = "3-Button + Bar",
                            onClick = { onSetNavigationMode(NavigationMode.BUTTONS) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("gesture_nav_mode_BUTTONS")
                        )
                    }
                }
            }

            // Accessibility Status Banner
            item {
                GlassCard(contentPadding = PaddingValues(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    if (accessibilityEnabled) EmeraldSuccess.copy(alpha = 0.16f)
                                    else tokens.primary.copy(alpha = 0.16f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (accessibilityEnabled) Icons.Rounded.CheckCircle else Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = if (accessibilityEnabled) EmeraldSuccess else tokens.primary
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (accessibilityEnabled) {
                                    "System Gesture Service Active"
                                } else {
                                    "Accessibility Permission Recommended"
                                },
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = if (accessibilityEnabled) {
                                    "Lock Screen, Notification Shade, and Quick Settings gestures are enabled."
                                } else {
                                    "Needed on Android to trigger Lock Screen, Notification Shade, and Quick Settings."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (!accessibilityEnabled) {
                            OutlinedButton(
                                onClick = { GestureController.openAccessibilitySettings(context) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("gesture_enable_accessibility_button")
                            ) {
                                Text("Enable Service")
                            }
                        }
                        Button(
                            onClick = onOpenLiveHome,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("gesture_try_live_home_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Home,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test on Home")
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }

    editingSlot?.let { slot ->
        val currentAction = when (slot) {
            GestureSlot.SWIPE_UP -> settings.swipeUpAction
            GestureSlot.SWIPE_DOWN -> settings.swipeDownAction
            GestureSlot.DOUBLE_TAP -> settings.doubleTapAction
            GestureSlot.LONG_PRESS -> settings.longPressAction
            GestureSlot.PINCH -> settings.pinchAction
        }
        ModalBottomSheet(onDismissRequest = { editingSlot = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${slot.title} Action",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Select what happens when you ${slot.title.lowercase()} on the Home screen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(
                    modifier = Modifier
                        .height(380.dp)
                        .testTag("gesture_sheet_action_list"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(GestureAction.entries, key = { it.name }) { action ->
                        val selected = action == currentAction
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (selected) tokens.primary.copy(alpha = 0.16f)
                                    else tokens.elevatedSurface
                                )
                                .border(
                                    width = if (selected) 1.4.dp else 1.dp,
                                    color = if (selected) tokens.primary else tokens.borderSubtle,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    when (slot) {
                                        GestureSlot.SWIPE_UP -> onSetSwipeUp(action)
                                        GestureSlot.SWIPE_DOWN -> onSetSwipeDown(action)
                                        GestureSlot.DOUBLE_TAP -> onSetDoubleTap(action)
                                        GestureSlot.LONG_PRESS -> onSetLongPress(action)
                                        GestureSlot.PINCH -> onSetPinch(action)
                                    }
                                    triggerPreviewPulse(slot, action)
                                    editingSlot = null
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                .testTag("gesture_sheet_option_${slot.name}_${action.name}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selected) tokens.primary.copy(alpha = 0.22f)
                                        else tokens.surface
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = action.icon(),
                                    contentDescription = null,
                                    tint = if (selected) tokens.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = action.displayName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = action.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = tokens.primary
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun GestureSlotConfigCard(
    slot: GestureSlot,
    subtitle: String,
    action: GestureAction,
    onConfigure: () -> Unit,
    onTestAction: () -> Unit
) {
    val tokens = LocalLauncherTokens.current
    GlassCard(
        onClick = onConfigure,
        contentPadding = PaddingValues(14.dp),
        modifier = Modifier.testTag("gesture_card_${slot.name}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(tokens.primary.copy(alpha = 0.14f))
                    .border(1.dp, tokens.primary.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = slot.icon,
                    contentDescription = null,
                    tint = tokens.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = slot.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(tokens.secondary.copy(alpha = 0.16f))
                            .border(1.dp, tokens.secondary.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = action.icon(),
                            contentDescription = null,
                            tint = tokens.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = action.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (action.requiresAccessibility()) {
                        Text(
                            text = "Test",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = tokens.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onTestAction)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Text(
                text = "Edit",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = tokens.primary
            )
        }
    }
}

@Composable
private fun NavigationModeCard(
    mode: NavigationMode,
    selected: Boolean,
    icon: ImageVector,
    badgeText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalLauncherTokens.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (selected) tokens.primary.copy(alpha = 0.16f) else tokens.elevatedSurface
            )
            .border(
                width = if (selected) 1.6.dp else 1.dp,
                color = if (selected) tokens.primary else tokens.borderSubtle,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) tokens.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (selected) tokens.primary.copy(alpha = 0.22f)
                        else Color.White.copy(alpha = 0.08f)
                    )
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) tokens.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = mode.displayName,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = mode.description,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun GestureAction.icon(): ImageVector = when (this) {
    GestureAction.NONE -> Icons.Rounded.Block
    GestureAction.OPEN_NOTIFICATIONS -> Icons.Rounded.Notifications
    GestureAction.OPEN_QUICK_SETTINGS -> Icons.Rounded.Tune
    GestureAction.OPEN_CONTROL_CENTER -> Icons.Rounded.Tune
    GestureAction.OPEN_EDGE_PANEL -> Icons.Rounded.ViewSidebar
    GestureAction.LOCK_SCREEN -> Icons.Rounded.Lock
    GestureAction.OPEN_APP_DRAWER -> Icons.Rounded.Apps
    GestureAction.OPEN_SEARCH -> Icons.Rounded.Search
    GestureAction.SPIN_WHEEL -> Icons.Rounded.RocketLaunch
    GestureAction.OPEN_STUDIO -> Icons.Rounded.Settings
}

private fun GestureAction.requiresAccessibility(): Boolean =
    this == GestureAction.LOCK_SCREEN ||
        this == GestureAction.OPEN_NOTIFICATIONS ||
        this == GestureAction.OPEN_QUICK_SETTINGS
