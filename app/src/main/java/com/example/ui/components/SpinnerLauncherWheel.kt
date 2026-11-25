package com.example.ui.components

import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.data.model.InstalledAppInfo
import com.example.data.model.SpinnerStyle
import com.example.ui.theme.LocalLauncherTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun SpinnerLauncherWheel(
    apps: List<InstalledAppInfo>,
    spinnerStyle: SpinnerStyle,
    iconProvider: (String) -> Drawable?,
    showLabels: Boolean,
    themedIconMode: Boolean = true,
    autoRollingEnabled: Boolean = true,
    rollingSpeed: Float = 1.0f,
    depth3DIntensity: Float = 0.85f,
    hapticFeedbackEnabled: Boolean = true,
    externalPulseTrigger: Int = 0,
    badgeCounts: Map<String, Int> = emptyMap(),
    onToggleAutoRolling: (() -> Unit)? = null,
    onAppClick: (InstalledAppInfo) -> Unit,
    onAppLongClick: (InstalledAppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalLauncherTokens.current
    val scope = rememberCoroutineScope()
    val totalApps = apps.size
    val isRobolectric = remember { Build.FINGERPRINT.contains("robolectric", ignoreCase = true) }

    val angleAnim = remember { Animatable(0f) }
    val pitchTiltAnim = remember { Animatable(24f) } // 3D tilt angle in degrees
    val zoomScaleAnim = remember { Animatable(1f) }
    var isZoomedIn by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var localAutoRollOverride by remember { mutableStateOf<Boolean?>(null) }
    val effectiveAutoRoll = localAutoRollOverride ?: autoRollingEnabled

    val animatedAngle = angleAnim.value
    val pitchDeg = pitchTiltAnim.value
    val depthFactor = depth3DIntensity.coerceIn(0f, 1f)

    val isProSphere = spinnerStyle == SpinnerStyle.PRO_3D_SPHERE

    // Continuous spherical track:
    // - Classic dials: up to 11 foreground icons cycling cleanly through ALL apps.
    // - Pro 3D Sphere: multi-latitude 3D spherical globe/tornado cloud cycling through ALL installed apps.
    val usePagedRotaryTrack = if (isProSphere) totalApps > 24 else totalApps > 10
    val slotSpacingDeg = remember(totalApps, isProSphere) {
        if (isProSphere) {
            when {
                totalApps <= 1 -> 360f
                totalApps <= 24 -> 360f / totalApps
                else -> 16f
            }
        } else {
            when {
                totalApps <= 1 -> 360f
                totalApps <= 10 -> 360f / totalApps
                else -> 36f
            }
        }
    }
    val trackCircumference = (totalApps.coerceAtLeast(1)) * slotSpacingDeg

    fun spinByDelta(deltaDeg: Float) {
        scope.launch {
            val target = angleAnim.value + deltaDeg
            val snapped = if (slotSpacingDeg > 0f && totalApps > 1) {
                (target / slotSpacingDeg).roundToInt() * slotSpacingDeg
            } else {
                target
            }
            if (tokens.reduceMotion) {
                angleAnim.snapTo(snapped)
            } else {
                angleAnim.animateTo(
                    targetValue = snapped,
                    animationSpec = spring(dampingRatio = 0.78f, stiffness = 200f)
                )
            }
        }
    }

    fun triggerLaunchZoomAndOpen(app: InstalledAppInfo) {
        if (tokens.reduceMotion) {
            onAppClick(app)
            return
        }
        scope.launch {
            zoomScaleAnim.animateTo(1.12f, animationSpec = tween(durationMillis = 90))
            onAppClick(app)
            zoomScaleAnim.animateTo(if (isZoomedIn) 1.15f else 1.0f, animationSpec = spring(stiffness = 300f))
        }
    }

    // Smooth continuous 3D auto-rotation on real Android devices when enabled
    LaunchedEffect(effectiveAutoRoll, rollingSpeed, tokens.reduceMotion, isDragging, totalApps) {
        if (!isRobolectric && effectiveAutoRoll && !tokens.reduceMotion && !isDragging && totalApps > 1) {
            delay(500L)
            var lastNanos = 0L
            while (isActive) {
                val frameNanos = withFrameNanos { it }
                if (lastNanos != 0L) {
                    val dtSec = ((frameNanos - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    val degPerSec = -16f * rollingSpeed.coerceIn(0.2f, 2.5f)
                    angleAnim.snapTo(angleAnim.value + degPerSec * dtSec)
                }
                lastNanos = frameNanos
            }
        }
    }

    // External spin trigger (GestureAction.SPIN_WHEEL or Quick Spin button)
    LaunchedEffect(externalPulseTrigger) {
        if (externalPulseTrigger > 0) {
            spinByDelta(-slotSpacingDeg * 4f)
        }
    }

    // Live ticking clock for central 3D sphere core
    var currentTime by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000L)
        }
    }
    val timeText = remember(currentTime) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentTime)
    }
    val secondsText = remember(currentTime) {
        SimpleDateFormat(":ss", Locale.getDefault()).format(currentTime)
    }
    val dateText = remember(currentTime) {
        SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(currentTime)
    }

    // Active front-most app (closest to 0 deg foreground spotlight)
    val focusedPair = remember(apps, animatedAngle, slotSpacingDeg, trackCircumference) {
        if (apps.isEmpty()) {
            null
        } else {
            var bestIdx = 0
            var bestDist = Float.MAX_VALUE
            apps.indices.forEach { i ->
                val rel = normalizeRelativeAngle(i * slotSpacingDeg + animatedAngle, trackCircumference)
                if (abs(rel) < bestDist) {
                    bestDist = abs(rel)
                    bestIdx = i
                }
            }
            bestIdx to apps[bestIdx]
        }
    }

    // No vibration while rolling apps (silent fluid 3D motion per user preference)
    @Suppress("UNUSED_EXPRESSION")
    hapticFeedbackEnabled

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("spinner_launcher_wheel"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val density = LocalDensity.current
            val diameterDp = minOf(maxWidth, 372.dp)
            val diameterPx = with(density) { diameterDp.toPx() }
            val orbitRadiusPx = diameterPx * 0.37f
            val pitchRad = (pitchDeg * (0.4f + 0.6f * depthFactor)) * PI.toFloat() / 180f

            Box(
                modifier = Modifier
                    .size(diameterDp)
                    .graphicsLayer {
                        val s = zoomScaleAnim.value
                        scaleX = s
                        scaleY = s
                    }
                    .pointerInput(totalApps, slotSpacingDeg, tokens.reduceMotion) {
                        var velocityXDegPerSec = 0f
                        var lastEventTimeMs = 0L

                        detectDragGestures(
                            onDragStart = {
                                isDragging = true
                                scope.launch { angleAnim.stop() }
                                lastEventTimeMs = System.currentTimeMillis()
                                velocityXDegPerSec = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val nowMs = System.currentTimeMillis()
                                val dtSec = ((nowMs - lastEventTimeMs).coerceAtLeast(8L)) / 1000f
                                // Horizontal drag rotates 360° 3D sphere; vertical drag tilts 3D orbital plane
                                val deltaDeg = (dragAmount.x / (size.width * 0.45f)) * 90f
                                val instantVel = deltaDeg / dtSec
                                velocityXDegPerSec = velocityXDegPerSec * 0.55f + instantVel * 0.45f
                                lastEventTimeMs = nowMs
                                scope.launch {
                                    angleAnim.snapTo(angleAnim.value + deltaDeg)
                                    val newPitch = (pitchTiltAnim.value + dragAmount.y * 0.14f).coerceIn(8f, 44f)
                                    pitchTiltAnim.snapTo(newPitch)
                                }
                            },
                            onDragEnd = {
                                isDragging = false
                                scope.launch {
                                    if (!tokens.reduceMotion && abs(velocityXDegPerSec) > 40f) {
                                        angleAnim.animateDecay(
                                            initialVelocity = velocityXDegPerSec.coerceIn(-1600f, 1600f),
                                            animationSpec = exponentialDecay(frictionMultiplier = 1.6f)
                                        )
                                    }
                                    if (slotSpacingDeg > 0f && totalApps > 1) {
                                        val nearestSlot = (angleAnim.value / slotSpacingDeg).roundToInt() * slotSpacingDeg
                                        if (tokens.reduceMotion) {
                                            angleAnim.snapTo(nearestSlot)
                                        } else {
                                            angleAnim.animateTo(
                                                targetValue = nearestSlot,
                                                animationSpec = spring(dampingRatio = 0.80f, stiffness = 240f)
                                            )
                                        }
                                    }
                                }
                            },
                            onDragCancel = { isDragging = false }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Multi-Ring 3D Perspective Orbital Canvas + Floor Reflection
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val outerR = size.minDimension * 0.465f
                    val trackR = size.minDimension * 0.37f
                    val innerR = size.minDimension * 0.245f
                    val ellipseRatio = (1f - depthFactor * 0.42f * sin(pitchRad)).coerceIn(0.46f, 0.96f)

                    // Deep radial 3D sphere bloom
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                tokens.primary.copy(alpha = 0.20f),
                                tokens.secondary.copy(alpha = 0.10f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = outerR * 1.12f
                        ),
                        radius = outerR * 1.12f,
                        center = center
                    )

                    // Floor holographic reflection ellipse beneath sphere
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                tokens.primary.copy(alpha = 0.14f * depthFactor),
                                tokens.secondary.copy(alpha = 0.05f * depthFactor),
                                Color.Transparent
                            ),
                            center = Offset(center.x, center.y + outerR * 0.86f),
                            radius = outerR * 0.72f
                        ),
                        topLeft = Offset(center.x - outerR * 0.65f, center.y + outerR * 0.72f),
                        size = Size(outerR * 1.30f, outerR * 0.26f)
                    )

                    // Tilted 3D elliptical orbit guide
                    drawOval(
                        color = tokens.borderSubtle.copy(alpha = 0.50f),
                        topLeft = Offset(center.x - trackR, center.y - trackR * ellipseRatio),
                        size = Size(trackR * 2f, trackR * 2f * ellipseRatio),
                        style = Stroke(width = 1.2.dp.toPx())
                    )

                    // Secondary tilted orbital ring for multi-plane 3D cage feel
                    drawOval(
                        brush = Brush.sweepGradient(
                            listOf(
                                tokens.primary.copy(alpha = 0.55f),
                                Color.Transparent,
                                tokens.secondary.copy(alpha = 0.55f),
                                Color.Transparent
                            )
                        ),
                        topLeft = Offset(center.x - outerR * 0.94f, center.y - outerR * 0.94f * ellipseRatio * 0.84f),
                        size = Size(outerR * 1.88f, outerR * 1.88f * ellipseRatio * 0.84f),
                        style = Stroke(width = 1.6.dp.toPx())
                    )

                    // Style-specific luminous 3D rings
                    when (spinnerStyle) {
                        SpinnerStyle.ORBITAL_DIAL -> {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(tokens.primary, tokens.secondary, Color.Transparent, tokens.accent, tokens.primary)
                                ),
                                startAngle = animatedAngle - 90f,
                                sweepAngle = 250f,
                                useCenter = false,
                                topLeft = Offset(center.x - outerR, center.y - outerR),
                                size = Size(outerR * 2f, outerR * 2f),
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = tokens.secondary.copy(alpha = 0.60f),
                                startAngle = -animatedAngle * 0.7f,
                                sweepAngle = 160f,
                                useCenter = false,
                                topLeft = Offset(center.x - innerR, center.y - innerR),
                                size = Size(innerR * 2f, innerR * 2f),
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        SpinnerStyle.CHRONOS_RING -> {
                            val ticks = 48
                            for (i in 0 until ticks) {
                                val rad = ((i * (360f / ticks)) + animatedAngle * 0.4f) * PI.toFloat() / 180f
                                val major = i % 4 == 0
                                val tickStart = outerR * if (major) 0.90f else 0.95f
                                val tickEnd = outerR
                                drawLine(
                                    color = if (major) tokens.primary.copy(alpha = 0.85f) else tokens.textSecondary.copy(alpha = 0.30f),
                                    start = Offset(center.x + cos(rad) * tickStart, center.y + sin(rad) * tickStart),
                                    end = Offset(center.x + cos(rad) * tickEnd, center.y + sin(rad) * tickEnd),
                                    strokeWidth = if (major) 2.2.dp.toPx() else 1.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                        SpinnerStyle.CYBER_ARC -> {
                            // Pro Cyber Vortex: 3-axis 3D gyroscope + quantum spokes + orbiting satellites + top spotlight
                            val vortexCyan = Color(0xFF00E5FF)
                            val vortexBlue = Color(0xFF168BFF)
                            val vortexPurple = Color(0xFF713BFF)

                            // Spotlight aura behind top focused app
                            val spotCenter = Offset(center.x, center.y - trackR * ellipseRatio * 0.92f)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    listOf(vortexCyan.copy(alpha = 0.34f), vortexPurple.copy(alpha = 0.14f), Color.Transparent),
                                    center = spotCenter,
                                    radius = outerR * 0.44f
                                ),
                                radius = outerR * 0.44f,
                                center = spotCenter
                            )

                            // 24 radial quantum telemetry ticks
                            for (i in 0 until 24) {
                                val rad = ((i * 15f) + animatedAngle * 0.35f) * PI.toFloat() / 180f
                                val major = i % 3 == 0
                                drawLine(
                                    color = if (major) vortexCyan.copy(alpha = 0.72f) else vortexBlue.copy(alpha = 0.28f),
                                    start = Offset(center.x + cos(rad) * (outerR * 0.87f), center.y + sin(rad) * (outerR * 0.87f)),
                                    end = Offset(center.x + cos(rad) * outerR, center.y + sin(rad) * outerR),
                                    strokeWidth = if (major) 2.2.dp.toPx() else 1.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }

                            // Outer segmented cyber ring
                            for (seg in 0 until 6) {
                                drawArc(
                                    color = when (seg % 3) {
                                        0 -> vortexCyan
                                        1 -> vortexBlue
                                        else -> vortexPurple
                                    },
                                    startAngle = animatedAngle + seg * 60f,
                                    sweepAngle = 42f,
                                    useCenter = false,
                                    topLeft = Offset(center.x - outerR, center.y - outerR),
                                    size = Size(outerR * 2f, outerR * 2f),
                                    style = Stroke(width = 3.8.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }

                            // Cross-tilted 3D elliptical gyro rings
                            val gyroTilt1 = (ellipseRatio * 0.72f).coerceIn(0.32f, 0.78f)
                            drawOval(
                                brush = Brush.sweepGradient(listOf(vortexCyan, Color.Transparent, vortexPurple, vortexCyan)),
                                topLeft = Offset(center.x - outerR * 0.90f, center.y - outerR * 0.90f * gyroTilt1),
                                size = Size(outerR * 1.80f, outerR * 1.80f * gyroTilt1),
                                style = Stroke(width = 2.dp.toPx())
                            )
                            // Counter-rotating inner holographic reticle
                            drawArc(
                                brush = Brush.sweepGradient(listOf(vortexPurple, vortexCyan, Color.Transparent, vortexBlue)),
                                startAngle = -animatedAngle * 1.35f,
                                sweepAngle = 240f,
                                useCenter = false,
                                topLeft = Offset(center.x - innerR * 1.08f, center.y - innerR * 1.08f),
                                size = Size(innerR * 2.16f, innerR * 2.16f),
                                style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // 4 luminous orbiting energy satellites
                            for (sat in 0 until 4) {
                                val satAngle = (animatedAngle * 1.2f + sat * 90f) * PI.toFloat() / 180f
                                val sx = center.x + cos(satAngle) * (outerR * 0.96f)
                                val sy = center.y + sin(satAngle) * (outerR * 0.96f * ellipseRatio)
                                drawCircle(color = vortexCyan.copy(alpha = 0.35f), radius = 6.dp.toPx(), center = Offset(sx, sy))
                                drawCircle(color = Color.White, radius = 2.6.dp.toPx(), center = Offset(sx, sy))
                            }
                        }
                        SpinnerStyle.LUXURY_HALO -> {
                            // Pro Violet Halo: Metallic champagne gold + soft violet 3D crown stage & cardinal jewels
                            val luxGold = Color(0xFFFACC15)
                            val luxViolet = Color(0xFF9D50FF)
                            val luxCyan = Color(0xFF00E5FF)

                            // Radiant top halo crown behind active app
                            val spotCenter = Offset(center.x, center.y - trackR * ellipseRatio * 0.92f)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    listOf(luxGold.copy(alpha = 0.32f), luxViolet.copy(alpha = 0.16f), Color.Transparent),
                                    center = spotCenter,
                                    radius = outerR * 0.48f
                                ),
                                radius = outerR * 0.48f,
                                center = spotCenter
                            )

                            // Champagne-gold outer precision bezel
                            drawCircle(
                                brush = Brush.linearGradient(listOf(luxGold, luxViolet, luxCyan, luxGold)),
                                radius = outerR,
                                center = center,
                                style = Stroke(width = 3.dp.toPx())
                            )
                            // Shimmering secondary crown bezel
                            drawArc(
                                brush = Brush.sweepGradient(listOf(Color.Transparent, luxGold, luxViolet, Color.Transparent)),
                                startAngle = animatedAngle * 0.7f,
                                sweepAngle = 260f,
                                useCenter = false,
                                topLeft = Offset(center.x - outerR * 0.92f, center.y - outerR * 0.92f),
                                size = Size(outerR * 1.84f, outerR * 1.84f),
                                style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Tilted 3D violet-gold halo ring
                            val haloSquash = (ellipseRatio * 0.68f).coerceIn(0.30f, 0.72f)
                            drawOval(
                                brush = Brush.linearGradient(listOf(luxGold.copy(alpha = 0.85f), luxViolet.copy(alpha = 0.75f))),
                                topLeft = Offset(center.x - outerR * 0.88f, center.y - outerR * 0.88f * haloSquash),
                                size = Size(outerR * 1.76f, outerR * 1.76f * haloSquash),
                                style = Stroke(width = 2.dp.toPx())
                            )
                            // Inner chronograph core ring
                            drawArc(
                                brush = Brush.sweepGradient(listOf(Color.Transparent, luxGold, luxViolet, Color.Transparent)),
                                startAngle = -animatedAngle * 0.9f,
                                sweepAngle = 230f,
                                useCenter = false,
                                topLeft = Offset(center.x - innerR * 1.05f, center.y - innerR * 1.05f),
                                size = Size(innerR * 2.10f, innerR * 2.10f),
                                style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Orbiting golden jewel nodes
                            for (jewel in 0 until 6) {
                                val jAngle = (-animatedAngle * 0.8f + jewel * 60f) * PI.toFloat() / 180f
                                val jx = center.x + cos(jAngle) * (outerR * 0.92f)
                                val jy = center.y + sin(jAngle) * (outerR * 0.92f * haloSquash)
                                drawCircle(color = luxGold.copy(alpha = 0.4f), radius = 5.5.dp.toPx(), center = Offset(jx, jy))
                                drawCircle(color = if (jewel % 2 == 0) luxGold else Color.White, radius = 2.5.dp.toPx(), center = Offset(jx, jy))
                            }
                        }
                        SpinnerStyle.PRO_3D_SPHERE -> {
                            // 3D Pro Sphere Vortex: vibrant multi-band aurora backdrop + latitude meridians + caustic rings
                            val auraViolet = Color(0xFF9333EA)
                            val auraMagenta = Color(0xFFEC4899)
                            val auraCyan = Color(0xFF00E5FF)
                            val auraGold = Color(0xFFFACC15)

                            // Vibrant spiral nebula swirl inside the sphere
                            drawCircle(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        auraViolet.copy(alpha = 0.42f),
                                        auraMagenta.copy(alpha = 0.36f),
                                        auraCyan.copy(alpha = 0.34f),
                                        Color(0xFF2563EB).copy(alpha = 0.38f),
                                        auraGold.copy(alpha = 0.24f),
                                        auraViolet.copy(alpha = 0.42f)
                                    ),
                                    center = center
                                ),
                                radius = outerR * 0.98f,
                                center = center
                            )

                            // Soft depth vignette so front & back icon layers pop cleanly
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF0F172A).copy(alpha = 0.38f),
                                        Color(0xFF3B0764).copy(alpha = 0.16f),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = outerR
                                ),
                                radius = outerR,
                                center = center
                            )

                            // 3D Latitude wireframe ellipses
                            val bands = listOf(0.45f, 0.78f, 1.0f)
                            bands.forEachIndexed { idx, bandScale ->
                                val bandR = outerR * 0.94f * bandScale
                                val squashedH = (bandR * (0.38f + idx * 0.16f) * (1f - 0.15f * sin(pitchRad))).coerceAtLeast(18f)
                                drawOval(
                                    brush = Brush.horizontalGradient(
                                        listOf(
                                            Color.Transparent,
                                            auraCyan.copy(alpha = 0.34f),
                                            auraGold.copy(alpha = 0.28f),
                                            Color.Transparent
                                        )
                                    ),
                                    topLeft = Offset(center.x - bandR, center.y - squashedH),
                                    size = Size(bandR * 2f, squashedH * 2f),
                                    style = Stroke(width = 1.2.dp.toPx())
                                )
                            }

                            // Rotating iridescent orbital halo
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(auraCyan, auraMagenta, auraGold, auraViolet, auraCyan)
                                ),
                                startAngle = animatedAngle,
                                sweepAngle = 300f,
                                useCenter = false,
                                topLeft = Offset(center.x - outerR * 0.99f, center.y - outerR * 0.99f),
                                size = Size(outerR * 1.98f, outerR * 1.98f),
                                style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                }

                // Central 3D Core Hub:
                // - Pro 3D Sphere uses a compact floating White Analog Clock Dial matching reference aesthetic
                // - Classic styles keep their full Digital Clock + Active App Launch Core unchanged
                if (isProSphere) {
                    val cal = remember(currentTime) {
                        Calendar.getInstance().apply { time = currentTime }
                    }
                    val hourFloat = (cal.get(Calendar.HOUR) % 12) + cal.get(Calendar.MINUTE) / 60f
                    val minuteFloat = cal.get(Calendar.MINUTE) + cal.get(Calendar.SECOND) / 60f
                    val secondFloat = cal.get(Calendar.SECOND).toFloat()

                    Column(
                        modifier = Modifier.zIndex(7f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(diameterDp * 0.23f)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFFFFFFFF), Color(0xFFE8EEF7))
                                    )
                                )
                                .border(
                                    width = 2.dp,
                                    brush = Brush.linearGradient(
                                        listOf(Color(0xFF00E5FF), Color(0xFFFACC15), Color(0xFF9333EA))
                                    ),
                                    shape = CircleShape
                                )
                                .clickable {
                                    val active = focusedPair?.second
                                    if (active != null) {
                                        triggerLaunchZoomAndOpen(active)
                                    } else {
                                        spinByDelta(-slotSpacingDeg * 3f)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                                val c = Offset(size.width / 2f, size.height / 2f)
                                val r = size.minDimension / 2f

                                // 12 minimalist hour ticks
                                for (h in 0 until 12) {
                                    val rad = (h * 30f - 90f) * PI.toFloat() / 180f
                                    drawLine(
                                        color = Color(0xFF334155).copy(alpha = if (h % 3 == 0) 0.85f else 0.40f),
                                        start = Offset(c.x + cos(rad) * (r * 0.78f), c.y + sin(rad) * (r * 0.78f)),
                                        end = Offset(c.x + cos(rad) * r, c.y + sin(rad) * r),
                                        strokeWidth = if (h % 3 == 0) 2.dp.toPx() else 1.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }

                                // Hour hand
                                val hRad = (hourFloat * 30f - 90f) * PI.toFloat() / 180f
                                drawLine(
                                    color = Color(0xFF0F172A),
                                    start = c,
                                    end = Offset(c.x + cos(hRad) * (r * 0.50f), c.y + sin(hRad) * (r * 0.50f)),
                                    strokeWidth = 2.6.dp.toPx(),
                                    cap = StrokeCap.Round
                                )

                                // Minute hand
                                val mRad = (minuteFloat * 6f - 90f) * PI.toFloat() / 180f
                                drawLine(
                                    color = Color(0xFF1E293B),
                                    start = c,
                                    end = Offset(c.x + cos(mRad) * (r * 0.74f), c.y + sin(mRad) * (r * 0.74f)),
                                    strokeWidth = 2.dp.toPx(),
                                    cap = StrokeCap.Round
                                )

                                // Second hand (crisp crimson accent)
                                val sRad = (secondFloat * 6f - 90f) * PI.toFloat() / 180f
                                drawLine(
                                    color = Color(0xFFEF4444),
                                    start = Offset(c.x - cos(sRad) * (r * 0.16f), c.y - sin(sRad) * (r * 0.16f)),
                                    end = Offset(c.x + cos(sRad) * (r * 0.82f), c.y + sin(sRad) * (r * 0.82f)),
                                    strokeWidth = 1.2.dp.toPx(),
                                    cap = StrokeCap.Round
                                )

                                drawCircle(color = Color(0xFFEF4444), radius = 2.6.dp.toPx(), center = c)
                            }
                        }

                        if (focusedPair != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xB8080B18))
                                    .border(0.9.dp, Color(0xFF00E5FF).copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                                    .clickable { triggerLaunchZoomAndOpen(focusedPair.second) }
                                    .padding(horizontal = 9.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${focusedPair.second.label} • ${focusedPair.first + 1}/$totalApps",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                } else {
                    val coreBorderBrush = if (spinnerStyle.isPremium) {
                        Brush.linearGradient(
                            listOf(Color(0xFFFACC15).copy(alpha = 0.85f), Color(0xFF00E5FF), Color(0xFF9D50FF))
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(tokens.primary.copy(alpha = 0.80f), tokens.secondary.copy(alpha = 0.55f))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(diameterDp * 0.39f)
                            .zIndex(5f)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        tokens.elevatedSurface.copy(alpha = 0.96f),
                                        tokens.surface.copy(alpha = 0.90f)
                                    )
                                )
                            )
                            .border(
                                width = if (spinnerStyle.isPremium) 2.2.dp else 1.6.dp,
                                brush = coreBorderBrush,
                                shape = CircleShape
                            )
                            .clickable {
                                val active = focusedPair?.second
                                if (active != null) {
                                    triggerLaunchZoomAndOpen(active)
                                } else {
                                    spinByDelta(-slotSpacingDeg * 3f)
                                }
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = timeText,
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = tokens.textPrimary
                                )
                                Text(
                                    text = secondsText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.primary,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                            Text(
                                text = dateText,
                                style = MaterialTheme.typography.labelSmall,
                                color = tokens.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (focusedPair != null) {
                                Spacer(modifier = Modifier.height(5.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(tokens.primary.copy(alpha = 0.18f))
                                        .border(0.9.dp, tokens.primary.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = focusedPair.second.label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = tokens.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${focusedPair.first + 1} / $totalApps",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.textSecondary
                                )
                            }
                        }
                    }
                }

                // 3D Spherical Orbiting App Icons
                if (apps.isNotEmpty()) {
                    if (isProSphere) {
                        // True Multi-Band 3D Spherical Cloud (Fibonacci Latitude Projection + Depth Sorting)
                        val sphereRadiusPx = diameterPx * 0.40f
                        val goldenLatStep = 2.3999632f // golden angle in radians for evenly spaced latitudes
                        apps.forEachIndexed { index, app ->
                            val relDeg = normalizeRelativeAngle(
                                index * slotSpacingDeg + animatedAngle,
                                trackCircumference
                            )
                            val isVisibleOnSphere = !usePagedRotaryTrack || abs(relDeg) <= 172f
                            if (isVisibleOnSphere) {
                                val isFocused = focusedPair?.first == index && abs(relDeg) < slotSpacingDeg * 0.55f

                                // Spread apps evenly across latitude bands [-66°..+66°] so icons form a rich 3D dome
                                val latFrac = if (totalApps <= 1) {
                                    0f
                                } else if (!usePagedRotaryTrack) {
                                    -0.78f + 1.56f * (index.toFloat() / (totalApps - 1).coerceAtLeast(1))
                                } else {
                                    sin(index * goldenLatStep) * 0.78f
                                }
                                val phiSin = latFrac.coerceIn(-0.82f, 0.82f)
                                val phiCos = sqrt((1f - phiSin * phiSin).coerceAtLeast(0.18f))

                                // Longitude: golden swirl when <=24 apps, or helical stream across all apps when >24
                                val lonDeg = if (!usePagedRotaryTrack) {
                                    index * 137.508f + animatedAngle
                                } else {
                                    relDeg * 1.12f
                                }
                                val lonRad = lonDeg * PI.toFloat() / 180f

                                // Apply gentle 3D vertical pitch tilt to spherical coordinates
                                val x3 = phiCos * sin(lonRad)
                                val z0 = phiCos * cos(lonRad)
                                val y0 = phiSin
                                val yTilted = y0 * cos(pitchRad * 0.45f) - z0 * sin(pitchRad * 0.45f)
                                val zTilted = y0 * sin(pitchRad * 0.45f) + z0 * cos(pitchRad * 0.45f) // -1 back .. +1 front

                                val dx = (x3 * sphereRadiusPx).roundToInt()
                                val dy = (yTilted * sphereRadiusPx).roundToInt()

                                val edgeFade = if (!usePagedRotaryTrack) {
                                    1f
                                } else {
                                    (1f - ((abs(relDeg) - 132f) / 40f).coerceIn(0f, 1f))
                                }

                                val depthNormalized = (zTilted + 1f) * 0.5f // 0 (rear) .. 1 (front)
                                val perspectiveScale = (0.56f + 0.58f * depthNormalized * (0.65f + 0.35f * depthFactor))
                                val finalScale = (if (isFocused) 1.18f else perspectiveScale).coerceIn(0.52f, 1.22f)
                                val depthAlpha = ((0.30f + 0.70f * depthNormalized) * edgeFade).coerceIn(0.22f, 1f)
                                val iconElevation = if (isFocused) 16.dp else (1f + 11f * depthNormalized * depthFactor).dp

                                // Center clock sits at zIndex(7f); foreground hemisphere (zTilted > 0.15) orbits above it,
                                // rear hemisphere (zTilted <= 0.15) orbits smoothly behind it!
                                val layerZ = if (isFocused) 22f else 1f + depthNormalized * 12f

                                AppIconItem(
                                    app = app,
                                    iconDrawable = iconProvider(app.packageName),
                                    iconSize = 40.dp,
                                    showLabel = showLabels && (isFocused || depthNormalized > 0.72f),
                                    labelColor = if (isFocused) Color(0xFF00E5FF) else Color.White,
                                    themedIconMode = themedIconMode,
                                    isHighlighted = isFocused,
                                    depthElevation = iconElevation,
                                    badgeCount = badgeCounts[app.packageName] ?: 0,
                                    onClick = { triggerLaunchZoomAndOpen(app) },
                                    onLongClick = { onAppLongClick(app) },
                                    modifier = Modifier
                                        .zIndex(layerZ)
                                        .offset { IntOffset(dx, dy) }
                                        .graphicsLayer {
                                            scaleX = finalScale
                                            scaleY = finalScale
                                            rotationY = -x3 * 28f * depthFactor
                                            rotationX = yTilted * 20f * depthFactor
                                            cameraDistance = 14f * density.density
                                        }
                                        .alpha(depthAlpha)
                                )
                            }
                        }
                    } else {
                        apps.forEachIndexed { index, app ->
                            val relDeg = normalizeRelativeAngle(
                                index * slotSpacingDeg + animatedAngle,
                                trackCircumference
                            )
                            val isVisibleOnDial = !usePagedRotaryTrack || abs(relDeg) <= 165f
                            if (isVisibleOnDial) {
                                val isFocused = focusedPair?.first == index && abs(relDeg) < slotSpacingDeg * 0.52f
                                // θ = 0° at foreground focus (bottom-front spotlight); ±180° at rear depth behind core
                                val thetaRad = relDeg * PI.toFloat() / 180f
                                val zDepth = cos(thetaRad) // +1 frontmost, -1 backmost
                                val sinTheta = sin(thetaRad)

                                // Subtle spherical orbital elevation wave so icons sit across multiple 3D planes
                                val verticalPerspective = (1f - depthFactor * 0.34f * sin(pitchRad)).coerceIn(0.54f, 0.96f)
                                val waveY = sin(thetaRad * 2f + index * 0.8f) * orbitRadiusPx * 0.10f * depthFactor

                                val dx = (sinTheta * orbitRadiusPx).roundToInt()
                                val dy = (-cos(thetaRad) * orbitRadiusPx * verticalPerspective + waveY).roundToInt()

                                val edgeFade = if (!usePagedRotaryTrack) {
                                    1f
                                } else {
                                    (1f - ((abs(relDeg) - 125f) / 40f).coerceIn(0f, 1f))
                                }

                                // 3D perspective scale & opacity governed by depth3DIntensity
                                val depthNormalized = (zDepth + 1f) * 0.5f // 0 (back) .. 1 (front)
                                val depthScale = (1f - 0.32f * depthFactor) + (0.36f * depthFactor * depthNormalized)
                                val finalScale = (if (isFocused) 1.20f else depthScale).coerceIn(0.62f, 1.24f)
                                val depthAlpha = ((1f - 0.42f * depthFactor) + (0.42f * depthFactor * depthNormalized)) * edgeFade
                                val iconElevation = if (isFocused) 16.dp else (2f + 10f * depthNormalized * depthFactor).dp

                                // Z-index puts front icons (> 5f) clearly in front of the central core (5f),
                                // and far-side icons (< 5f) orbiting behind the core!
                                val layerZ = if (isFocused) 20f else 1f + depthNormalized * 8f

                                AppIconItem(
                                    app = app,
                                    iconDrawable = iconProvider(app.packageName),
                                    iconSize = 44.dp,
                                    showLabel = showLabels && (isFocused || depthNormalized > 0.48f),
                                    labelColor = if (isFocused) tokens.primary else tokens.textPrimary,
                                    themedIconMode = themedIconMode,
                                    isHighlighted = isFocused,
                                    depthElevation = iconElevation,
                                    badgeCount = badgeCounts[app.packageName] ?: 0,
                                    onClick = { triggerLaunchZoomAndOpen(app) },
                                    onLongClick = { onAppLongClick(app) },
                                    modifier = Modifier
                                        .zIndex(layerZ)
                                        .offset { IntOffset(dx, dy) }
                                        .graphicsLayer {
                                            scaleX = finalScale
                                            scaleY = finalScale
                                            rotationY = -sinTheta * 24f * depthFactor
                                            rotationX = zDepth * 12f * depthFactor
                                            cameraDistance = 16f * density.density
                                        }
                                        .alpha(depthAlpha.coerceIn(0.25f, 1f))
                                )
                            }
                        }
                    }
                }
            }
        }

        // Compact 3D Controls Capsule: Prev | Play/Pause | Spin All | Zoom | Next
        if (totalApps > 1) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(tokens.surface.copy(alpha = 0.86f))
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(tokens.primary.copy(alpha = 0.45f), tokens.secondary.copy(alpha = 0.35f))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = { spinByDelta(slotSpacingDeg * 3f) },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("spinner_prev_step")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronLeft,
                        contentDescription = "Previous apps",
                        tint = tokens.textPrimary
                    )
                }

                IconButton(
                    onClick = {
                        localAutoRollOverride = !effectiveAutoRoll
                        onToggleAutoRolling?.invoke()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("spinner_auto_roll_toggle")
                ) {
                    Icon(
                        imageVector = if (effectiveAutoRoll) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (effectiveAutoRoll) "Pause rotation" else "Resume rotation",
                        tint = tokens.primary
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(tokens.primary.copy(alpha = 0.16f))
                        .clickable { spinByDelta(-slotSpacingDeg * 5f) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("spinner_quick_spin_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        tint = tokens.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Spin All ($totalApps)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = tokens.primary
                    )
                }

                IconButton(
                    onClick = {
                        isZoomedIn = !isZoomedIn
                        scope.launch {
                            val target = if (isZoomedIn) 1.15f else 1.0f
                            if (tokens.reduceMotion) {
                                zoomScaleAnim.snapTo(target)
                            } else {
                                zoomScaleAnim.animateTo(target, animationSpec = spring(dampingRatio = 0.78f, stiffness = 220f))
                            }
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("spinner_zoom_toggle")
                ) {
                    Icon(
                        imageVector = if (isZoomedIn) Icons.Rounded.ZoomOut else Icons.Rounded.ZoomIn,
                        contentDescription = if (isZoomedIn) "Zoom out" else "Zoom in",
                        tint = tokens.textPrimary
                    )
                }

                IconButton(
                    onClick = { spinByDelta(-slotSpacingDeg * 3f) },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("spinner_next_step")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "Next apps",
                        tint = tokens.textPrimary
                    )
                }
            }
        }
    }
}

private fun normalizeRelativeAngle(angleDeg: Float, circumferenceDeg: Float): Float {
    if (circumferenceDeg <= 0f) return 0f
    var mod = angleDeg % circumferenceDeg
    if (mod > circumferenceDeg / 2f) mod -= circumferenceDeg
    if (mod < -circumferenceDeg / 2f) mod += circumferenceDeg
    return mod
}
