package com.example.ui.components

import android.app.Activity
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AirplanemodeActive
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.LauncherNotificationListenerService
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.RoyalPurple
import kotlin.math.roundToInt

@Composable
fun PremiumControlPanel(
    onDismiss: () -> Unit,
    onFeedback: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager }
    val connectivityManager = remember { context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager }
    val notificationManager = remember { context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager }

    val torchCameraId = remember(cameraManager) {
        runCatching {
            cameraManager?.cameraIdList?.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        }.getOrNull()
    }

    var isTorchOn by remember { mutableStateOf(false) }

    DisposableEffect(cameraManager, torchCameraId) {
        val callback = if (cameraManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            object : CameraManager.TorchCallback() {
                override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                    if (cameraId == torchCameraId) {
                        isTorchOn = enabled
                    }
                }
            }
        } else null

        if (callback != null) {
            runCatching { cameraManager?.registerTorchCallback(callback, null) }
        }
        onDispose {
            if (callback != null) {
                runCatching { cameraManager?.unregisterTorchCallback(callback) }
            }
        }
    }

    val isWifiConnected = remember(connectivityManager) {
        runCatching {
            val caps = connectivityManager?.getNetworkCapabilities(connectivityManager.activeNetwork)
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        }.getOrDefault(false)
    }

    val isCellularConnected = remember(connectivityManager) {
        runCatching {
            val caps = connectivityManager?.getNetworkCapabilities(connectivityManager.activeNetwork)
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        }.getOrDefault(false)
    }

    val isBluetoothOn = remember(context) {
        runCatching {
            val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            @Suppress("DEPRECATION")
            val adapter = bm?.adapter ?: BluetoothAdapter.getDefaultAdapter()
            adapter?.isEnabled == true
        }.getOrDefault(false)
    }

    val isAirplaneOn = remember(context) {
        runCatching {
            Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1
        }.getOrDefault(false)
    }

    val isDndOn = remember(notificationManager) {
        runCatching {
            notificationManager?.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
        }.getOrDefault(false)
    }

    var isAutoRotateOn by remember(context) {
        mutableStateOf(
            runCatching {
                Settings.System.getInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, 1) == 1
            }.getOrDefault(true)
        )
    }

    val maxMusicVol = remember(audioManager) {
        (audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15).coerceAtLeast(1)
    }
    var mediaVolumeFraction by remember(audioManager, maxMusicVol) {
        mutableFloatStateOf(
            (audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 7f) / maxMusicVol.toFloat()
        )
    }

    var brightnessFraction by remember(context) {
        mutableFloatStateOf(
            runCatching {
                Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 180) / 255f
            }.getOrDefault(0.7f).coerceIn(0.05f, 1f)
        )
    }

    val notifAccessGranted = remember(context) {
        LauncherNotificationListenerService.isPermissionGranted(context)
    }

    fun openSystemAction(action: String) {
        runCatching {
            context.startActivity(Intent(action).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xD8050814))
                .clickable(onClick = onDismiss)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp)
                .testTag("premium_control_panel"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xF20D142B), Color(0xF2171235))
                        )
                    )
                    .border(
                        1.4.dp,
                        Brush.linearGradient(listOf(ElectricCyan.copy(alpha = 0.65f), RoyalPurple.copy(alpha = 0.65f))),
                        RoundedCornerShape(30.dp)
                    )
                    .clickable(enabled = false) {}
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Control Center",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Connectivity, Flashlight & System Controls",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.09f))
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close Control Center", tint = Color.White)
                    }
                }

                // Connectivity Grid Row 1: Wi-Fi, Mobile Data, Bluetooth
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ControlTile(
                        title = "Wi-Fi",
                        status = if (isWifiConnected) "Connected" else "Open Panel",
                        icon = Icons.Rounded.Wifi,
                        active = isWifiConnected,
                        accent = ElectricCyan,
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                openSystemAction(Settings.Panel.ACTION_WIFI)
                            } else {
                                openSystemAction(Settings.ACTION_WIFI_SETTINGS)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ControlTile(
                        title = "Mobile Data",
                        status = if (isCellularConnected) "Active" else "Data Settings",
                        icon = Icons.Rounded.SignalCellularAlt,
                        active = isCellularConnected,
                        accent = EmeraldSuccess,
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                openSystemAction(Settings.ACTION_DATA_USAGE_SETTINGS)
                            } else {
                                openSystemAction(Settings.ACTION_WIRELESS_SETTINGS)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ControlTile(
                        title = "Bluetooth",
                        status = if (isBluetoothOn) "On" else "Off",
                        icon = Icons.Rounded.Bluetooth,
                        active = isBluetoothOn,
                        accent = Color(0xFF38BDF8),
                        onClick = { openSystemAction(Settings.ACTION_BLUETOOTH_SETTINGS) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Connectivity Grid Row 2: Airplane, Do Not Disturb, Auto-Rotate, Torch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ControlTile(
                        title = "Airplane",
                        status = if (isAirplaneOn) "On" else "Off",
                        icon = Icons.Rounded.AirplanemodeActive,
                        active = isAirplaneOn,
                        accent = Color(0xFFF59E0B),
                        onClick = { openSystemAction(Settings.ACTION_AIRPLANE_MODE_SETTINGS) },
                        modifier = Modifier.weight(1f)
                    )
                    ControlTile(
                        title = "DND",
                        status = if (isDndOn) "Active" else "Off",
                        icon = Icons.Rounded.DoNotDisturbOn,
                        active = isDndOn,
                        accent = RoyalPurple,
                        onClick = { openSystemAction(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS) },
                        modifier = Modifier.weight(1f)
                    )
                    ControlTile(
                        title = "Rotate",
                        status = if (isAutoRotateOn) "Auto" else "Locked",
                        icon = Icons.Rounded.ScreenRotation,
                        active = isAutoRotateOn,
                        accent = ElectricCyan,
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(context)) {
                                val next = !isAutoRotateOn
                                Settings.System.putInt(
                                    context.contentResolver,
                                    Settings.System.ACCELEROMETER_ROTATION,
                                    if (next) 1 else 0
                                )
                                isAutoRotateOn = next
                            } else {
                                openSystemAction(Settings.ACTION_DISPLAY_SETTINGS)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Flashlight + Notification Badges Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ControlTile(
                        title = "Flashlight",
                        status = if (torchCameraId == null) "Unavailable" else if (isTorchOn) "Torch ON" else "Tap for ON",
                        icon = Icons.Rounded.FlashlightOn,
                        active = isTorchOn,
                        accent = PremiumGold,
                        onClick = {
                            if (cameraManager != null && torchCameraId != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                runCatching {
                                    val next = !isTorchOn
                                    cameraManager.setTorchMode(torchCameraId, next)
                                    isTorchOn = next
                                }.onFailure {
                                    onFeedback("Flashlight unavailable right now.")
                                }
                            } else {
                                onFeedback("Flashlight hardware unavailable on this device.")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    ControlTile(
                        title = "Badges",
                        status = if (notifAccessGranted) "Synced" else "Grant Access",
                        icon = Icons.Rounded.NotificationsActive,
                        active = notifAccessGranted,
                        accent = EmeraldSuccess,
                        onClick = {
                            openSystemAction(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Brightness Slider
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(0.8.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.Brightness6, contentDescription = null, tint = PremiumGold, modifier = Modifier.size(18.dp))
                            Text("Brightness", style = MaterialTheme.typography.labelLarge, color = Color.White)
                        }
                        Text(
                            text = "${(brightnessFraction * 100f).roundToInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = PremiumGold
                        )
                    }
                    Slider(
                        value = brightnessFraction,
                        onValueChange = { frac ->
                            brightnessFraction = frac
                            val act = context as? Activity
                            act?.window?.let { win ->
                                val lp = win.attributes
                                lp.screenBrightness = frac.coerceIn(0.05f, 1f)
                                win.attributes = lp
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(context)) {
                                runCatching {
                                    Settings.System.putInt(
                                        context.contentResolver,
                                        Settings.System.SCREEN_BRIGHTNESS,
                                        (frac * 255f).roundToInt().coerceIn(12, 255)
                                    )
                                }
                            }
                        },
                        colors = SliderDefaults.colors(thumbColor = PremiumGold, activeTrackColor = PremiumGold)
                    )
                }

                // Media Volume Slider
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(0.8.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.VolumeUp, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                            Text("Media Volume", style = MaterialTheme.typography.labelLarge, color = Color.White)
                        }
                        Text(
                            text = "${(mediaVolumeFraction * 100f).roundToInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan
                        )
                    }
                    Slider(
                        value = mediaVolumeFraction,
                        onValueChange = { frac ->
                            mediaVolumeFraction = frac
                            val vol = (frac * maxMusicVol).roundToInt().coerceIn(0, maxMusicVol)
                            runCatching {
                                audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0)
                            }
                        },
                        colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlTile(
    title: String,
    status: String,
    icon: ImageVector,
    active: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (active) accent.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.08f),
        label = "tile_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (active) accent.copy(alpha = 0.78f) else Color.White.copy(alpha = 0.16f),
        label = "tile_border"
    )
    val scale by animateFloatAsState(
        targetValue = if (active) 1.02f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "tile_scale"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .border(1.1.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (active) accent else Color.White.copy(alpha = 0.80f),
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) accent else Color.White.copy(alpha = 0.65f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
