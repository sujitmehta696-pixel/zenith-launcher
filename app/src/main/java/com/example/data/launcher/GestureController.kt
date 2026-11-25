package com.example.data.launcher

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.example.LauncherAccessibilityService
import com.example.data.model.GestureAction

sealed interface GestureExecutionResult {
    data object HandledInLauncher : GestureExecutionResult
    data class SystemActionExecuted(val message: String) : GestureExecutionResult
    data class NeedsAccessibilityLockPermission(val message: String) : GestureExecutionResult
    data class InfoFallback(val message: String) : GestureExecutionResult
}

object GestureController {

    fun executeSystemGesture(context: Context, action: GestureAction): GestureExecutionResult {
        return when (action) {
            GestureAction.NONE -> GestureExecutionResult.HandledInLauncher
            GestureAction.OPEN_APP_DRAWER,
            GestureAction.OPEN_SEARCH,
            GestureAction.SPIN_WHEEL,
            GestureAction.OPEN_CONTROL_CENTER,
            GestureAction.OPEN_EDGE_PANEL,
            GestureAction.OPEN_STUDIO -> GestureExecutionResult.HandledInLauncher
            GestureAction.OPEN_NOTIFICATIONS -> {
                val service = LauncherAccessibilityService.instance
                if (service != null && service.expandNotificationsNow()) {
                    GestureExecutionResult.SystemActionExecuted("Notification shade expanded")
                } else {
                    GestureExecutionResult.NeedsAccessibilityLockPermission(
                        "Enable 3D Rolling OS Gestures in Accessibility Settings to allow notification shade gestures."
                    )
                }
            }
            GestureAction.OPEN_QUICK_SETTINGS -> {
                val service = LauncherAccessibilityService.instance
                if (service != null && service.expandQuickSettingsNow()) {
                    GestureExecutionResult.SystemActionExecuted("Quick Settings expanded")
                } else {
                    GestureExecutionResult.NeedsAccessibilityLockPermission(
                        "Enable 3D Rolling OS Gestures in Accessibility Settings to allow Quick Settings gestures."
                    )
                }
            }
            GestureAction.LOCK_SCREEN -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    GestureExecutionResult.InfoFallback(
                        "Lock Screen gesture requires Android 9 (API 28) or higher."
                    )
                } else {
                    val service = LauncherAccessibilityService.instance
                    if (service != null && service.lockScreenNow()) {
                        GestureExecutionResult.SystemActionExecuted("Display locked")
                    } else {
                        GestureExecutionResult.NeedsAccessibilityLockPermission(
                            "Enable Android 3D Rolling launcher OS in Accessibility Settings to allow screen lock gestures."
                        )
                    }
                }
            }
        }
    }

    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }
    }
}
