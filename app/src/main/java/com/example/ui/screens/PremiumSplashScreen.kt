package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.LocalLauncherTokens
import com.example.ui.theme.PremiumGold
import kotlinx.coroutines.launch

@Composable
fun PremiumSplashScreen(
    reduceMotion: Boolean,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalLauncherTokens.current
    val revealAnim = remember { Animatable(0f) }
    val ringSpinAnim = remember { Animatable(0f) }
    val exitAlpha = remember { Animatable(1f) }

    LaunchedEffect(reduceMotion) {
        val animDuration = if (reduceMotion) 160 else 900
        val spinJob = launch {
            ringSpinAnim.animateTo(
                targetValue = 360f,
                animationSpec = tween(durationMillis = animDuration + 250, easing = FastOutSlowInEasing)
            )
        }
        revealAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = animDuration, easing = FastOutSlowInEasing)
        )
        exitAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = if (reduceMotion) 80 else 240,
                delayMillis = if (reduceMotion) 40 else 180,
                easing = LinearEasing
            )
        )
        spinJob.cancel()
        onFinished()
    }

    val p = revealAnim.value
    val scale = 0.88f + 0.12f * p

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(exitAlpha.value)
            .background(Color(0xFF080B18))
            .clickable(onClick = onFinished)
            .testTag("premium_splash_screen")
    ) {
        // Luxury dark orbital backdrop artwork
        Image(
            painter = painterResource(id = R.drawable.img_auth_backdrop),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.55f * p)
        )

        // Deep vignette & radial glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            tokens.primary.copy(alpha = 0.14f),
                            Color(0xD905070B),
                            Color(0xFA04060A)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Brand Spec Capsule
            Row(
                modifier = Modifier
                    .alpha(p)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xAA101522))
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(PremiumGold.copy(alpha = 0.55f), ElectricCyan.copy(alpha = 0.45f))
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = PremiumGold,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "OS SPINNER ENGINE • EDITION 36",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.4.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFFE2E8F0)
                )
            }

            // Centerpiece Chronograph Crest + Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(22.dp),
                modifier = Modifier
                    .scale(scale)
                    .alpha(p)
            ) {
                Box(
                    modifier = Modifier.size(196.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val outerR = size.minDimension * 0.47f
                        val midR = size.minDimension * 0.40f

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    PremiumGold.copy(alpha = 0.20f),
                                    ElectricCyan.copy(alpha = 0.10f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = outerR * 1.06f
                            ),
                            radius = outerR * 1.06f,
                            center = center
                        )

                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(
                                    PremiumGold,
                                    ElectricCyan,
                                    Color.Transparent,
                                    PremiumGold
                                )
                            ),
                            startAngle = ringSpinAnim.value - 90f,
                            sweepAngle = 270f,
                            useCenter = false,
                            topLeft = Offset(center.x - outerR, center.y - outerR),
                            size = Size(outerR * 2f, outerR * 2f),
                            style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                        )

                        drawArc(
                            color = ElectricCyan.copy(alpha = 0.65f),
                            startAngle = -ringSpinAnim.value * 0.7f + 40f,
                            sweepAngle = 155f,
                            useCenter = false,
                            topLeft = Offset(center.x - midR, center.y - midR),
                            size = Size(midR * 2f, midR * 2f),
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(124.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF070A12))
                            .border(
                                1.6.dp,
                                Brush.linearGradient(listOf(PremiumGold, ElectricCyan)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_splash_crest),
                            contentDescription = "Android 3D Rolling launcher OS Emblem",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "ANDROID 3D ROLLING LAUNCHER",
                        style = MaterialTheme.typography.labelLarge.copy(
                            letterSpacing = 2.6.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = PremiumGold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "3D Rolling OS",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Android 3D Rolling launcher OS • Glassmorphism",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFB4C0D6),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 320.dp)
                    )
                }
            }

            // Bottom Initialization Progress Bar & Tap Hint
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 320.dp)
                    .alpha(p)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = p.coerceIn(0.08f, 1f))
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(ElectricCyan, PremiumGold)
                                )
                            )
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Calibrating Spinner & Themes…",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = PremiumGold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onFinished)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("splash_skip_button")
                    )
                }
            }
        }
    }
}
