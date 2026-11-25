package com.example

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent

class LauncherAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Minimal service used strictly on explicit user gesture to trigger GLOBAL_ACTION_LOCK_SCREEN
    }

    override fun onInterrupt() {
        // No continuous state to interrupt
    }

    override fun onDestroy() {
        if (instance === this) {
            instance = null
        }
        super.onDestroy()
    }

    fun lockScreenNow(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            runCatching { performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN) }.getOrDefault(false)
        } else {
            false
        }
    }

    fun expandNotificationsNow(): Boolean {
        return runCatching { performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS) }.getOrDefault(false)
    }

    fun expandQuickSettingsNow(): Boolean {
        return runCatching { performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS) }.getOrDefault(false)
    }

    companion object {
        @Volatile
        var instance: LauncherAccessibilityService? = null
            private set

        fun isServiceEnabled(context: Context): Boolean {
            if (instance != null) return true
            return runCatching {
                val expected = ComponentName(context, LauncherAccessibilityService::class.java).flattenToString()
                val enabledServices = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                ).orEmpty()
                enabledServices.split(':').any { it.equals(expected, ignoreCase = true) }
            }.getOrDefault(false)
        }
    }
}
