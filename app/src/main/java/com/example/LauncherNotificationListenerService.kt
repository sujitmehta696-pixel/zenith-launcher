package com.example

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tracks real active notification badge counts per package name when notification access is granted.
 */
class LauncherNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        recountActiveNotifications()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        recountActiveNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        recountActiveNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        _badgeCounts.value = emptyMap()
    }

    private fun recountActiveNotifications() {
        val counts = runCatching {
            activeNotifications
                ?.filter { !it.isOngoing }
                ?.groupingBy { it.packageName }
                ?.eachCount()
                ?: emptyMap()
        }.getOrDefault(emptyMap())
        _badgeCounts.value = counts
    }

    companion object {
        private val _badgeCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
        val badgeCounts: StateFlow<Map<String, Int>> = _badgeCounts.asStateFlow()

        fun isPermissionGranted(context: Context): Boolean {
            val cn = ComponentName(context, LauncherNotificationListenerService::class.java)
            val flat = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            return flat.split(":").any {
                ComponentName.unflattenFromString(it) == cn || it.contains(cn.flattenToShortString())
            }
        }

        fun openNotificationListenerSettings(context: Context) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { context.startActivity(intent) }
        }
    }
}
