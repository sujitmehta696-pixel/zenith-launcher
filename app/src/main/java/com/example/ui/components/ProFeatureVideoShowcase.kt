package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LocalLauncherTokens
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.RoyalPurple
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class ProVideoChapter(
    val index: Int,
    val chipLabel: String,
    val title: String,
    val description: String,
    val accent: Color
)

private val PRO_VIDEO_CHAPTERS = listOf(
    ProVideoChapter(
        index = 0,
        chipLabel = "3D Sphere",
        title = "360° 3D Pro Sphere Launcher",
        description = "Orbit all installed apps around a living 3D spherical dial with z-depth & analog core.",
        accent = PremiumGold
    ),
    ProVideoChapter(
        index = 1,
        chipLabel = "Liquid Glass",
        title = "Liquid Glass Refraction UI",
        description = "Specular crystal borders, frosted depth layers, and fluid home surface polish.",
        accent = ElectricCyan
    ),
    ProVideoChapter(
        index = 2,
        chipLabel = "Vortex Halo",
        title = "Cyber Vortex & Violet Halo",
        description = "3-axis holographic gyro rings & champagne gold bezels for your rolling launcher.",
        accent = RoyalPurple
    ),
    ProVideoChapter(
        index = 3,
        chipLabel = "4K Live Walls",
        title = "60+ Pro Themes & Live MP4 Walls",
        description = "AMOLED 4K wallpapers, synced live video backgrounds, and 6-column Pro grids.",
        accent = Color(0xFF38BDF8)
    ),
    ProVideoChapter(
        index = 4,
        chipLabel = "Ad-Free",
        title = "100% Ad-Free + Cloud Sync",
        description = "Zero ad banners across Studio screens with automatic subscription cloud restore.",
        accent = EmeraldSuccess
    )
)

private const val SCENE_DURATION_SECONDS = 4.0f
private const val TOTAL_VIDEO_SECONDS = 20.0f

@Composable
fun ProFeatureVideoShowcase(
    isPremium: Boolean,
    reduceMotion: Boolean = false,
    onActionClick: () -> Unit = {},
    actionLabel: String = if (isPremium) "Try 3D Pro Sphere" else "View Pro Plans",
    modifier: Modifier = Modifier
) {
    val tokens = LocalLauncherTokens.current
    val isRobolectric = remember {
        android.os.Build.FINGERPRINT.contains("robolectric", ignoreCase = true)
    }
    var isPlaying by remember { mutableStateOf(true) }
    var elapsedSec by remember { mutableFloatStateOf(0f) }
    var showFullscreen by remember { mutableStateOf(false) }

    LaunchedEffect(isPlaying, reduceMotion) {
        if (!isPlaying || isRobolectric) return@LaunchedEffect
        var lastNanos = withFrameNanos { it }
        while (isPlaying) {
            val now = withFrameNanos { it }
            val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.1f)
            lastNanos = now
            val speed = if (reduceMotion) 0.55f else 1.0f
            elapsedSec = (elapsedSec + dt * speed) % TOTAL_VIDEO_SECONDS
        }
    }

    val currentSceneIndex = ((elapsedSec / SCENE_DURATION_SECONDS).toInt()).coerceIn(0, PRO_VIDEO_CHAPTERS.lastIndex)
    val currentChapter = PRO_VIDEO_CHAPTERS[currentSceneIndex]
    val sceneLocalProgress = ((elapsedSec % SCENE_DURATION_SECONDS) / SCENE_DURATION_SECONDS).coerceIn(0f, 1f)

    GlassCard(
        cornerRadius = 24.dp,
        contentPadding = PaddingValues(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("pro_feature_video_showcase")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayCircle,
                        contentDescription = null,
                        tint = PremiumGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "Pro Plan Video Tour",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Watch what unlocks with a paid Pro plan",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                ProBadge(label = "PRO TOUR")
            }

            // 16:9 Cinema Video Viewport
            ProVideoStageViewport(
                chapter = currentChapter,
                sceneProgress = sceneLocalProgress,
                totalProgress = (elapsedSec / TOTAL_VIDEO_SECONDS).coerceIn(0f, 1f),
                elapsedSec = elapsedSec,
                isPlaying = isPlaying,
                onTogglePlay = { isPlaying = !isPlaying },
                onRestart = {
                    elapsedSec = 0f
                    isPlaying = true
                },
                onOpenFullscreen = { showFullscreen = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )

            // Interactive Video Chapter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PRO_VIDEO_CHAPTERS.forEach { chapter ->
                    val selected = chapter.index == currentSceneIndex
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selected) chapter.accent.copy(alpha = 0.20f)
                                else tokens.elevatedSurface
                            )
                            .border(
                                width = if (selected) 1.2.dp else 0.8.dp,
                                color = if (selected) chapter.accent else tokens.borderSubtle,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                elapsedSec = chapter.index * SCENE_DURATION_SECONDS + 0.05f
                                isPlaying = true
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("pro_video_chapter_${chapter.index}")
                    ) {
                        Text(
                            text = "${chapter.index + 1}. ${chapter.chipLabel}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (selected) chapter.accent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Active Chapter Caption + Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = currentChapter.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentChapter.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPremium) ElectricCyan else PremiumGold,
                        contentColor = Color(0xFF10131C)
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("pro_video_cta_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                }
            }
        }
    }

    if (showFullscreen) {
        Dialog(
            onDismissRequest = { showFullscreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF050812))
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProBadge(label = "PRO CINEMA TOUR")
                        Text(
                            text = currentChapter.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = { showFullscreen = false },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("pro_video_fullscreen_close")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close fullscreen video",
                            tint = Color.White
                        )
                    }
                }

                ProVideoStageViewport(
                    chapter = currentChapter,
                    sceneProgress = sceneLocalProgress,
                    totalProgress = (elapsedSec / TOTAL_VIDEO_SECONDS).coerceIn(0f, 1f),
                    elapsedSec = elapsedSec,
                    isPlaying = isPlaying,
                    onTogglePlay = { isPlaying = !isPlaying },
                    onRestart = {
                        elapsedSec = 0f
                        isPlaying = true
                    },
                    onOpenFullscreen = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 10f)
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = currentChapter.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFD6E0F5)
                    )
                    Button(
                        onClick = {
                            showFullscreen = false
                            onActionClick()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PremiumGold,
                            contentColor = Color(0xFF141006)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(actionLabel, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProVideoStageViewport(
    chapter: ProVideoChapter,
    sceneProgress: Float,
    totalProgress: Float,
    elapsedSec: Float,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onRestart: () -> Unit,
    onOpenFullscreen: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF060A18), Color(0xFF14102E), Color(0xFF091322))
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(listOf(chapter.accent, ElectricCyan, RoyalPurple)),
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        // Real-time 60fps animated feature scene canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w * 0.5f
            val cy = h * 0.48f
            val angleRad = (sceneProgress * 2f * PI).toFloat()

            // Ambient radial glow behind scene
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(chapter.accent.copy(alpha = 0.28f), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = w * 0.45f
                ),
                radius = w * 0.45f,
                center = Offset(cx, cy)
            )

            when (chapter.index) {
                0 -> {
                    // Scene 1: 3D Pro Sphere All-App Orbiting Globe
                    val r = h * 0.34f
                    drawCircle(
                        color = PremiumGold.copy(alpha = 0.28f),
                        radius = r,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    for (band in -1..1) {
                        val yOff = band * r * 0.42f
                        val bandRx = r * cos(band * 0.52f)
                        drawOval(
                            color = ElectricCyan.copy(alpha = 0.32f),
                            topLeft = Offset(cx - bandRx, cy + yOff - r * 0.18f),
                            size = Size(bandRx * 2f, r * 0.36f),
                            style = Stroke(width = 1.4.dp.toPx())
                        )
                    }
                    val appColors = listOf(
                        PremiumGold, ElectricCyan, RoyalPurple, EmeraldSuccess,
                        Color(0xFFF43F5E), Color(0xFF38BDF8), Color(0xFFFB923C), Color(0xFFA855F7)
                    )
                    appColors.forEachIndexed { idx, orbColor ->
                        val theta = angleRad + idx * (2f * PI.toFloat() / appColors.size)
                        val lat = ((idx % 3) - 1) * 0.48f
                        val ox = cx + r * 0.92f * cos(theta) * cos(lat)
                        val oy = cy + r * 0.56f * sin(lat) + r * 0.26f * sin(theta)
                        val depth = (sin(theta) + 1.4f) / 2.4f
                        val orbRadius = (8.dp.toPx() + 6.dp.toPx() * depth)
                        drawCircle(
                            color = orbColor.copy(alpha = 0.35f + 0.65f * depth),
                            radius = orbRadius,
                            center = Offset(ox, oy)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.7f * depth),
                            radius = orbRadius * 0.36f,
                            center = Offset(ox, oy)
                        )
                    }
                }

                1 -> {
                    // Scene 2: Liquid Glass Refraction Sweep
                    val cardW = w * 0.56f
                    val cardH = h * 0.48f
                    val sweep = (sceneProgress * 1.4f) - 0.2f
                    val offset1 = Offset(cx - cardW * 0.58f + sin(angleRad) * 14f, cy - cardH * 0.52f)
                    val offset2 = Offset(cx - cardW * 0.32f - sin(angleRad) * 14f, cy - cardH * 0.28f)

                    drawRoundRect(
                        color = RoyalPurple.copy(alpha = 0.32f),
                        topLeft = offset1,
                        size = Size(cardW, cardH),
                        cornerRadius = CornerRadius(24f, 24f)
                    )
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.14f),
                                ElectricCyan.copy(alpha = 0.28f),
                                Color.White.copy(alpha = 0.08f)
                            ),
                            start = Offset(w * sweep, 0f),
                            end = Offset(w * (sweep + 0.35f), h)
                        ),
                        topLeft = offset2,
                        size = Size(cardW, cardH),
                        cornerRadius = CornerRadius(26f, 26f)
                    )
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.55f),
                        topLeft = offset2,
                        size = Size(cardW, cardH),
                        cornerRadius = CornerRadius(26f, 26f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                2 -> {
                    // Scene 3: Cyber Vortex & Violet Halo Gyro Rings
                    val ringRadii = listOf(h * 0.36f, h * 0.28f, h * 0.20f)
                    val ringColors = listOf(ElectricCyan, RoyalPurple, PremiumGold)
                    ringRadii.forEachIndexed { idx, radius ->
                        val tilt = if (idx % 2 == 0) 0.55f + 0.25f * sin(angleRad) else 0.72f - 0.25f * cos(angleRad)
                        drawOval(
                            color = ringColors[idx].copy(alpha = 0.85f),
                            topLeft = Offset(cx - radius, cy - radius * tilt),
                            size = Size(radius * 2f, radius * 2f * tilt),
                            style = Stroke(width = (3 - idx * 0.5f).dp.toPx())
                        )
                    }
                    drawCircle(
                        color = PremiumGold,
                        radius = 10.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                }

                3 -> {
                    // Scene 4: 60+ Pro Themes & Live MP4 Wallpapers
                    val phoneW = w * 0.19f
                    val phoneH = h * 0.58f
                    val cards = listOf(
                        Triple(-phoneW * 1.15f, RoyalPurple, Color(0xFF00E5FF)),
                        Triple(0f, PremiumGold, Color(0xFF713BFF)),
                        Triple(phoneW * 1.15f, ElectricCyan, Color(0xFF10B981))
                    )
                    cards.forEachIndexed { idx, (dx, c1, c2) ->
                        val bounce = sin(angleRad + idx * 1.2f) * 8.dp.toPx()
                        val left = cx + dx - phoneW * 0.5f
                        val top = cy - phoneH * 0.52f + bounce
                        drawRoundRect(
                            brush = Brush.verticalGradient(listOf(c1.copy(alpha = 0.8f), c2.copy(alpha = 0.7f))),
                            topLeft = Offset(left, top),
                            size = Size(phoneW, phoneH),
                            cornerRadius = CornerRadius(18f, 18f)
                        )
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.55f),
                            topLeft = Offset(left, top),
                            size = Size(phoneW, phoneH),
                            cornerRadius = CornerRadius(18f, 18f),
                            style = Stroke(width = 1.6.dp.toPx())
                        )
                    }
                }

                else -> {
                    // Scene 5: 100% Ad-Free + Cloud Pro Vault Shield
                    val r = h * 0.27f
                    val shieldPath = Path().apply {
                        moveTo(cx, cy - r)
                        lineTo(cx + r * 0.82f, cy - r * 0.55f)
                        lineTo(cx + r * 0.72f, cy + r * 0.35f)
                        lineTo(cx, cy + r * 0.95f)
                        lineTo(cx - r * 0.72f, cy + r * 0.35f)
                        lineTo(cx - r * 0.82f, cy - r * 0.55f)
                        close()
                    }
                    drawPath(
                        path = shieldPath,
                        brush = Brush.verticalGradient(
                            listOf(EmeraldSuccess.copy(alpha = 0.45f), ElectricCyan.copy(alpha = 0.25f))
                        )
                    )
                    drawPath(
                        path = shieldPath,
                        color = EmeraldSuccess,
                        style = Stroke(width = 2.4.dp.toPx())
                    )
                    val pulse = r * (1.0f + 0.35f * sceneProgress)
                    drawCircle(
                        color = EmeraldSuccess.copy(alpha = (1f - sceneProgress) * 0.5f),
                        radius = pulse,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }

        // Top HUD bar inside video frame
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xAA050812))
                    .border(0.8.dp, chapter.accent.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = chapter.accent,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = chapter.chipLabel.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            if (onOpenFullscreen != null) {
                IconButton(
                    onClick = onOpenFullscreen,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("pro_video_fullscreen")
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xAA050812)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Fullscreen,
                            contentDescription = "Expand Pro video",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }

        // Animated Chapter Title Overlay + Video Transport Controls at Bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xD8050812), Color(0xF0050812))
                    )
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AnimatedContent(
                targetState = chapter.title,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "pro_video_title"
            ) { title ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            LinearProgressIndicator(
                progress = { totalProgress },
                color = chapter.accent,
                trackColor = Color.White.copy(alpha = 0.20f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.5.dp)
                    .clip(CircleShape)
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
                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("pro_video_play_pause")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "Pause Pro tour" else "Play Pro tour",
                            tint = PremiumGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(
                        onClick = onRestart,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Replay,
                            contentDescription = "Restart Pro tour",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                val curSec = elapsedSec.toInt().coerceIn(0, TOTAL_VIDEO_SECONDS.toInt())
                val totSec = TOTAL_VIDEO_SECONDS.toInt()
                Text(
                    text = String.format(java.util.Locale.US, "00:%02d / 00:%02d", curSec, totSec),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFFD6E0F5)
                )
            }
        }
    }
}
