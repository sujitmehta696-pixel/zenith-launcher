package com.example.ui.components

import android.graphics.drawable.Drawable
import android.os.Build
import android.util.LruCache
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.data.model.InstalledAppInfo
import com.example.data.model.ThemeIconStyle
import com.example.ui.theme.LocalLauncherTokens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private object AppIconBitmapCache {
    private val cache = LruCache<String, ImageBitmap>(180)

    fun get(packageName: String): ImageBitmap? = cache.get(packageName)

    fun put(packageName: String, bitmap: ImageBitmap) {
        cache.put(packageName, bitmap)
    }
}

internal fun rasterizeAppIconSafely(packageName: String, drawable: Drawable?): ImageBitmap? {
    if (drawable == null) return null
    AppIconBitmapCache.get(packageName)?.let { return it }
    return try {
        val img = drawable.toBitmap(width = 128, height = 128).asImageBitmap()
        AppIconBitmapCache.put(packageName, img)
        img
    } catch (_: Throwable) {
        null
    }
}

fun iconShapeFor(style: ThemeIconStyle): Shape = when (style) {
    ThemeIconStyle.CIRCLE -> CircleShape
    ThemeIconStyle.SQUIRCLE -> RoundedCornerShape(percent = 30)
    ThemeIconStyle.ROUNDED_SQUARE -> RoundedCornerShape(12.dp)
    ThemeIconStyle.TEARDROP -> RoundedCornerShape(
        topStartPercent = 50,
        topEndPercent = 50,
        bottomEndPercent = 50,
        bottomStartPercent = 12
    )
    ThemeIconStyle.CRYSTAL_GLASS -> RoundedCornerShape(16.dp)
    ThemeIconStyle.HEX_TECH -> GenericShape { size, _ ->
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = minOf(cx, cy)
        for (i in 0 until 6) {
            val angle = (i * 60.0 - 30.0) * PI / 180.0
            val x = cx + (r * cos(angle)).toFloat()
            val y = cy + (r * sin(angle)).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIconItem(
    app: InstalledAppInfo,
    iconDrawable: Drawable?,
    iconSize: Dp,
    showLabel: Boolean,
    labelColor: Color = MaterialTheme.colorScheme.onBackground,
    themedIconMode: Boolean = true,
    isHighlighted: Boolean = false,
    depthElevation: Dp = if (isHighlighted) 14.dp else 4.dp,
    badgeCount: Int = 0,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalLauncherTokens.current
    val clipShape = iconShapeFor(tokens.iconStyle)
    val isRobolectric = remember { Build.FINGERPRINT.contains("robolectric", ignoreCase = true) }
    val cachedBitmap = remember(app.packageName, iconDrawable) {
        AppIconBitmapCache.get(app.packageName) ?: if (isRobolectric) {
            rasterizeAppIconSafely(app.packageName, iconDrawable)
        } else {
            null
        }
    }
    val bitmap by produceState<ImageBitmap?>(
        initialValue = cachedBitmap,
        key1 = app.packageName,
        key2 = iconDrawable
    ) {
        if (value == null && iconDrawable != null) {
            value = withContext(Dispatchers.Default) {
                rasterizeAppIconSafely(app.packageName, iconDrawable)
            }
        }
    }

    val iconTintFilter = remember(themedIconMode, tokens.primary) {
        if (!themedIconMode) {
            null
        } else {
            val matrix = ColorMatrix().apply {
                setToSaturation(0.92f)
            }
            ColorFilter.colorMatrix(matrix)
        }
    }

    val borderBrush = if (isHighlighted) {
        Brush.linearGradient(listOf(tokens.primary, Color.White.copy(alpha = 0.85f), tokens.secondary))
    } else if (themedIconMode) {
        Brush.linearGradient(
            listOf(
                tokens.primary.copy(alpha = 0.70f),
                tokens.secondary.copy(alpha = 0.45f)
            )
        )
    } else {
        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.25f), tokens.borderSubtle))
    }

    val plateBrush = if (themedIconMode) {
        Brush.radialGradient(
            listOf(
                tokens.primary.copy(alpha = if (isHighlighted) 0.34f else 0.22f),
                tokens.surface.copy(alpha = 0.90f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(tokens.surface.copy(alpha = 0.78f), tokens.surface.copy(alpha = 0.88f))
        )
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 4.dp, vertical = 5.dp)
            .testTag("app_item_${app.packageName}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .shadow(
                    elevation = depthElevation,
                    shape = clipShape,
                    ambientColor = if (isHighlighted) tokens.primary else tokens.secondary,
                    spotColor = if (isHighlighted) tokens.primary else tokens.secondary
                )
                .clip(clipShape)
                .background(plateBrush)
                .border(
                    width = if (isHighlighted) 2.dp else if (themedIconMode) 1.3.dp else 0.9.dp,
                    brush = borderBrush,
                    shape = clipShape
                ),
            contentAlignment = Alignment.Center
        ) {
            val decodedBitmap = bitmap
            if (decodedBitmap != null) {
                Image(
                    bitmap = decodedBitmap,
                    contentDescription = app.label,
                    contentScale = ContentScale.Crop,
                    colorFilter = iconTintFilter,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = app.label.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = tokens.primary
                )
            }

            // Glass specular top sheen for crisp 3D depth
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = if (tokens.iconStyle == ThemeIconStyle.CRYSTAL_GLASS || isHighlighted) 0.30f else 0.16f),
                                Color.Transparent,
                                if (themedIconMode) tokens.secondary.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.10f)
                            )
                        )
                    )
            )

            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                        .border(0.8.dp, Color.White, CircleShape)
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }
        }
        if (showLabel) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = app.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isHighlighted) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = labelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 76.dp)
            )
        }
    }
}
