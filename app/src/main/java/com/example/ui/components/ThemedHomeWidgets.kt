package com.example.ui.components

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ThemeCardStyle
import com.example.data.wallpaper.LiveWeatherSnapshot
import com.example.ui.theme.LocalLauncherTokens
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DeviceTelemetrySnapshot(
    val batteryPercent: Int,
    val isCharging: Boolean,
    val ramUsedFraction: Float,
    val ramFreeLabel: String,
    val storageUsedFraction: Float,
    val storageFreeLabel: String
)

fun readDeviceTelemetry(context: Context): DeviceTelemetrySnapshot {
    val batteryIntent = runCatching {
        context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }.getOrNull()
    val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    val batteryPct = if (level >= 0 && scale > 0) ((level * 100f) / scale).toInt() else 100
    val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
        status == BatteryManager.BATTERY_STATUS_FULL

    val memInfo = ActivityManager.MemoryInfo()
    runCatching {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        am?.getMemoryInfo(memInfo)
    }
    val totalMemGb = (memInfo.totalMem / (1024f * 1024f * 1024f)).coerceAtLeast(1f)
    val availMemGb = (memInfo.availMem / (1024f * 1024f * 1024f)).coerceAtLeast(0f)
    val ramUsedFrac = ((totalMemGb - availMemGb) / totalMemGb).coerceIn(0f, 1f)

    val stat = runCatching { StatFs(Environment.getDataDirectory().path) }.getOrNull()
    val totalBytes = stat?.totalBytes?.toFloat()?.coerceAtLeast(1f) ?: 1f
    val availBytes = stat?.availableBytes?.toFloat()?.coerceAtLeast(0f) ?: 0f
    val storageUsedFrac = ((totalBytes - availBytes) / totalBytes).coerceIn(0f, 1f)
    val storageFreeGb = availBytes / (1024f * 1024f * 1024f)

    return DeviceTelemetrySnapshot(
        batteryPercent = batteryPct,
        isCharging = charging,
        ramUsedFraction = ramUsedFrac,
        ramFreeLabel = String.format(Locale.US, "%.1f GB free", availMemGb),
        storageUsedFraction = storageUsedFrac,
        storageFreeLabel = String.format(Locale.US, "%.0f GB free", storageFreeGb)
    )
}

@Composable
fun ThemedHomeWidgetsStrip(
    themedWidgetsEnabled: Boolean,
    weather: LiveWeatherSnapshot? = null,
    onPulseSpin: () -> Unit,
    onOpenStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tokens = LocalLauncherTokens.current

    var now by remember { mutableStateOf(Date()) }
    var telemetry by remember { mutableStateOf(readDeviceTelemetry(context)) }
    var tickCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            now = Date()
            if (tickCount % 10 == 0) {
                telemetry = readDeviceTelemetry(context)
            }
            tickCount++
            delay(1000L)
        }
    }

    val shape = RoundedCornerShape(tokens.cardRadius)
    val containerBrush = if (themedWidgetsEnabled) {
        when (tokens.cardStyle) {
            ThemeCardStyle.GRADIENT_BORDER -> Brush.linearGradient(
                listOf(tokens.surface.copy(alpha = 0.88f), tokens.primary.copy(alpha = 0.14f))
            )
            ThemeCardStyle.SOLID_ELEVATED -> Brush.linearGradient(
                listOf(tokens.elevatedSurface.copy(alpha = 0.94f), tokens.surface.copy(alpha = 0.94f))
            )
            ThemeCardStyle.OUTLINED_MINIMAL -> Brush.linearGradient(
                listOf(tokens.background.copy(alpha = 0.72f), tokens.background.copy(alpha = 0.72f))
            )
            ThemeCardStyle.GLASSMORPHIC -> Brush.linearGradient(
                listOf(tokens.surface.copy(alpha = 0.78f), tokens.secondary.copy(alpha = 0.14f))
            )
        }
    } else {
        Brush.linearGradient(listOf(Color(0xCC121620), Color(0xCC121620)))
    }

    val borderModifier = if (themedWidgetsEnabled && tokens.cardStyle == ThemeCardStyle.GRADIENT_BORDER) {
        Modifier.border(
            1.4.dp,
            Brush.linearGradient(listOf(tokens.primary, tokens.secondary)),
            shape
        )
    } else if (themedWidgetsEnabled) {
        Modifier.border(1.dp, tokens.primary.copy(alpha = 0.35f), shape)
    } else {
        Modifier.border(1.dp, Color.White.copy(alpha = 0.12f), shape)
    }

    val accentColor = if (themedWidgetsEnabled) tokens.primary else Color.White
    val secondaryColor = if (themedWidgetsEnabled) tokens.secondary else Color(0xFF94A3B8)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("themed_widgets_strip"),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Widget 1: Chronos & Theme Widget
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(shape)
                .background(containerBrush)
                .then(borderModifier)
                .clickable(onClick = onOpenStudio)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.45f)
                    .height(2.5.dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(accentColor, secondaryColor)))
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Palette,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (themedWidgetsEnabled) tokens.themeName else "System Widget",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = if (telemetry.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryFull,
                        contentDescription = null,
                        tint = secondaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${telemetry.batteryPercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.textPrimary
                    )
                }
            }

            Text(
                text = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(now),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = tokens.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (weather != null) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(secondaryColor.copy(alpha = 0.16f))
                            .padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WbSunny,
                            contentDescription = "Weather",
                            tint = secondaryColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = weather.temperatureLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = tokens.textPrimary
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.16f))
                        .border(0.8.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .clickable(onClick = onPulseSpin)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RocketLaunch,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Quick Spin",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = accentColor
                    )
                }
            }
        }

        // Widget 2: Live Device Telemetry Widget (RAM & Storage)
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(shape)
                .background(containerBrush)
                .then(borderModifier)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Memory,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "RAM • ${telemetry.ramFreeLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            LinearProgressIndicator(
                progress = { telemetry.ramUsedFraction },
                color = accentColor,
                trackColor = tokens.borderSubtle,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Storage,
                    contentDescription = null,
                    tint = secondaryColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Disk • ${telemetry.storageFreeLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            LinearProgressIndicator(
                progress = { telemetry.storageUsedFraction },
                color = secondaryColor,
                trackColor = tokens.borderSubtle,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(CircleShape)
            )
        }
    }
}
