package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Pinch
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.LauncherAccessibilityService
import com.example.data.launcher.GestureController
import com.example.data.model.GestureAction
import com.example.data.model.LauncherSettingsState
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LocalLauncherTokens

enum class GestureSlot(val title: String, val icon: ImageVector) {
    SWIPE_DOWN("Swipe Down", Icons.Rounded.ArrowDownward),
    SWIPE_UP("Swipe Up", Icons.Rounded.ArrowUpward),
    DOUBLE_TAP("Double Tap", Icons.Rounded.TouchApp),
    LONG_PRESS("Long Press", Icons.Rounded.Gesture),
    PINCH("Pinch (2 Fingers)", Icons.Rounded.Pinch)
}

@Composable
fun GestureSettingsPanel(
    settings: LauncherSettingsState,
    onSetSwipeDown: (GestureAction) -> Unit,
    onSetSwipeUp: (GestureAction) -> Unit,
    onSetDoubleTap: (GestureAction) -> Unit,
    onSetLongPress: (GestureAction) -> Unit,
    onSetPinch: (GestureAction) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tokens = LocalLauncherTokens.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var activeSlotDialog by remember { mutableStateOf<GestureSlot?>(null) }

    val usesLockAction = settings.swipeDownAction == GestureAction.LOCK_SCREEN ||
        settings.swipeUpAction == GestureAction.LOCK_SCREEN ||
        settings.doubleTapAction == GestureAction.LOCK_SCREEN ||
        settings.longPressAction == GestureAction.LOCK_SCREEN ||
        settings.pinchAction == GestureAction.LOCK_SCREEN

    var accessibilityEnabled by remember(usesLockAction) {
        mutableStateOf(LauncherAccessibilityService.isServiceEnabled(context))
    }

    DisposableEffect(lifecycleOwner, usesLockAction) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                accessibilityEnabled = LauncherAccessibilityService.isServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    GlassCard(
        modifier = modifier.testTag("gesture_settings_panel"),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Rounded.Gesture, contentDescription = null, tint = tokens.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text("Gesture Settings", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Map home screen swipes & taps to instant OS actions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        GestureMappingRow(
            slot = GestureSlot.SWIPE_DOWN,
            selectedAction = settings.swipeDownAction,
            onClick = { activeSlotDialog = GestureSlot.SWIPE_DOWN }
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
        GestureMappingRow(
            slot = GestureSlot.SWIPE_UP,
            selectedAction = settings.swipeUpAction,
            onClick = { activeSlotDialog = GestureSlot.SWIPE_UP }
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
        GestureMappingRow(
            slot = GestureSlot.DOUBLE_TAP,
            selectedAction = settings.doubleTapAction,
            onClick = { activeSlotDialog = GestureSlot.DOUBLE_TAP }
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
        GestureMappingRow(
            slot = GestureSlot.LONG_PRESS,
            selectedAction = settings.longPressAction,
            onClick = { activeSlotDialog = GestureSlot.LONG_PRESS }
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
        GestureMappingRow(
            slot = GestureSlot.PINCH,
            selectedAction = settings.pinchAction,
            onClick = { activeSlotDialog = GestureSlot.PINCH }
        )

        if (usesLockAction) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(tokens.elevatedSurface)
                    .border(
                        1.dp,
                        if (accessibilityEnabled) EmeraldSuccess.copy(alpha = 0.5f) else tokens.primary.copy(alpha = 0.4f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (accessibilityEnabled) Icons.Rounded.CheckCircle else Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = if (accessibilityEnabled) EmeraldSuccess else tokens.primary,
                    modifier = Modifier.size(20.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (accessibilityEnabled) "Lock Screen Gesture Active" else "Accessibility Service Required for Lock",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = if (accessibilityEnabled) {
                            "3D Rolling OS Gesture Lock is active."
                        } else {
                            "Android requires Accessibility Service permission to lock the screen without device admin."
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!accessibilityEnabled) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = { GestureController.openAccessibilitySettings(context) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Enable in Accessibility Settings")
                        }
                    }
                }
            }
        }
    }

    activeSlotDialog?.let { slot ->
        val currentSelection = when (slot) {
            GestureSlot.SWIPE_DOWN -> settings.swipeDownAction
            GestureSlot.SWIPE_UP -> settings.swipeUpAction
            GestureSlot.DOUBLE_TAP -> settings.doubleTapAction
            GestureSlot.LONG_PRESS -> settings.longPressAction
            GestureSlot.PINCH -> settings.pinchAction
        }
        AlertDialog(
            onDismissRequest = { activeSlotDialog = null },
            title = { Text("${slot.title} Action") },
            text = {
                LazyColumn(
                    modifier = Modifier.height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(GestureAction.entries) { action ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    when (slot) {
                                        GestureSlot.SWIPE_DOWN -> onSetSwipeDown(action)
                                        GestureSlot.SWIPE_UP -> onSetSwipeUp(action)
                                        GestureSlot.DOUBLE_TAP -> onSetDoubleTap(action)
                                        GestureSlot.LONG_PRESS -> onSetLongPress(action)
                                        GestureSlot.PINCH -> onSetPinch(action)
                                    }
                                    activeSlotDialog = null
                                }
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .testTag("gesture_option_${slot.name}_${action.name}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = action == currentSelection,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(action.displayName, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = action.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeSlotDialog = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun GestureMappingRow(
    slot: GestureSlot,
    selectedAction: GestureAction,
    onClick: () -> Unit
) {
    val tokens = LocalLauncherTokens.current
    ListItem(
        headlineContent = { Text(slot.title, style = MaterialTheme.typography.bodyLarge) },
        supportingContent = { Text(selectedAction.displayName, color = tokens.primary) },
        leadingContent = {
            Icon(
                imageVector = slot.icon,
                contentDescription = null,
                tint = tokens.secondary
            )
        },
        trailingContent = {
            Text(
                text = "Change",
                style = MaterialTheme.typography.labelLarge,
                color = tokens.primary
            )
        },
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("gesture_row_${slot.name}")
    )
}
