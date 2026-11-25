package com.example.data.launcher

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class PhoneContentType(val badge: String) {
    SYSTEM_SETTING("Setting"),
    MEDIA_FILE("File")
}

data class PhoneContentItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: PhoneContentType,
    val settingsAction: String? = null
)

class PhoneContentSearchHelper(private val context: Context) {

    private val systemShortcuts = listOf(
        PhoneContentItem("set_wifi", "Wi-Fi & Internet Settings", "Network & Wireless", PhoneContentType.SYSTEM_SETTING, Settings.ACTION_WIFI_SETTINGS),
        PhoneContentItem("set_bt", "Bluetooth Connections", "Paired devices & audio", PhoneContentType.SYSTEM_SETTING, Settings.ACTION_BLUETOOTH_SETTINGS),
        PhoneContentItem("set_display", "Display & Brightness", "Screen timeout & dark mode", PhoneContentType.SYSTEM_SETTING, Settings.ACTION_DISPLAY_SETTINGS),
        PhoneContentItem("set_sound", "Sound & Vibration", "Media, ring & DND volume", PhoneContentType.SYSTEM_SETTING, Settings.ACTION_SOUND_SETTINGS),
        PhoneContentItem("set_battery", "Battery & Power Saver", "Usage & optimization", PhoneContentType.SYSTEM_SETTING, Settings.ACTION_BATTERY_SAVER_SETTINGS),
        PhoneContentItem("set_apps", "Installed Apps & Defaults", "App info & permissions", PhoneContentType.SYSTEM_SETTING, Settings.ACTION_APPLICATION_SETTINGS),
        PhoneContentItem("set_accessibility", "Accessibility & Gestures", "Lock screen & shade actions", PhoneContentType.SYSTEM_SETTING, Settings.ACTION_ACCESSIBILITY_SETTINGS),
        PhoneContentItem("set_notifications", "Notification Access", "Enable app icon badges", PhoneContentType.SYSTEM_SETTING, Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS),
        PhoneContentItem("set_location", "Location Services", "Weather & GPS preferences", PhoneContentType.SYSTEM_SETTING, Settings.ACTION_LOCATION_SOURCE_SETTINGS),
        PhoneContentItem("set_security", "Security & Lock Screen", "Biometrics & screen lock", PhoneContentType.SYSTEM_SETTING, Settings.ACTION_SECURITY_SETTINGS)
    )

    suspend fun searchPhoneContent(query: String): List<PhoneContentItem> = withContext(Dispatchers.IO) {
        val clean = query.trim().lowercase()
        if (clean.isBlank()) return@withContext emptyList()

        val matchedSettings = systemShortcuts.filter {
            it.title.lowercase().contains(clean) || it.subtitle.lowercase().contains(clean)
        }

        val matchedMedia = queryOwnMediaStoreFiles(clean)
        matchedSettings + matchedMedia
    }

    private fun queryOwnMediaStoreFiles(query: String): List<PhoneContentItem> {
        val results = mutableListOf<PhoneContentItem>()
        runCatching {
            val uri = MediaStore.Files.getContentUri("external")
            val projection = arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.MIME_TYPE
            )
            val selection = "LOWER(${MediaStore.MediaColumns.DISPLAY_NAME}) LIKE ?"
            val args = arrayOf("%$query%")
            context.contentResolver.query(uri, projection, selection, args, null)?.use { cursor ->
                val idCol = cursor.getColumnIndex(MediaStore.MediaColumns._ID)
                val nameCol = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                val mimeCol = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
                while (cursor.moveToNext() && results.size < 8) {
                    val fileId = if (idCol >= 0) cursor.getLong(idCol) else results.size.toLong()
                    val name = if (nameCol >= 0) cursor.getString(nameCol) else null
                    val mime = if (mimeCol >= 0) cursor.getString(mimeCol) else "File"
                    if (!name.isNullOrBlank()) {
                        results.add(
                            PhoneContentItem(
                                id = "media_$fileId",
                                title = name,
                                subtitle = mime ?: "Phone file",
                                type = PhoneContentType.MEDIA_FILE
                            )
                        )
                    }
                }
            }
        }
        return results
    }

    fun openSystemShortcut(item: PhoneContentItem): Boolean {
        val action = item.settingsAction ?: return false
        return runCatching {
            context.startActivity(Intent(action).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
            true
        }.getOrDefault(false)
    }
}
