package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.data.model.LauncherBackgroundPreset
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.LocalLauncherTokens
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SoftViolet
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DynamicOrbitBackdrop(
    preset: LauncherBackgroundPreset = LocalLauncherTokens.current.backgroundPreset,
    reduceMotion: Boolean = LocalLauncherTokens.current.reduceMotion,
    modifier: Modifier = Modifier
) {
    val tokens = LocalLauncherTokens.current

    val phase = if (reduceMotion) {
        0.25f
    } else {
        val transition = rememberInfiniteTransition(label = "orbit_backdrop_glow")
        val anim by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 16000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "orbit_backdrop_phase"
        )
        anim
    }

    val orb1Color: Color
    val orb2Color: Color
    val orb3Color: Color

    when (preset) {
        LauncherBackgroundPreset.MIDNIGHT_COSMOS -> {
            orb1Color = RoyalPurple
            orb2Color = NeonCyan
            orb3Color = ElectricBlue
        }
        LauncherBackgroundPreset.LIQUID_GLASS -> {
            orb1Color = NeonCyan
            orb2Color = SoftViolet
            orb3Color = Color(0xFF38BDF8)
        }
        LauncherBackgroundPreset.ROYAL_NEBULA -> {
            orb1Color = SoftViolet
            orb2Color = ElectricBlue
            orb3Color = RoyalPurple
        }
        LauncherBackgroundPreset.ELECTRIC_AURORA -> {
            orb1Color = ElectricBlue
            orb2Color = NeonCyan
            orb3Color = SoftViolet
        }
        LauncherBackgroundPreset.PURE_OBSIDIAN -> {
            orb1Color = tokens.primary
            orb2Color = tokens.secondary
            orb3Color = MidnightBlack
        }
    }

    val baseBackground = if (tokens.isDark) MidnightBlack else tokens.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBackground)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val rad = phase * 2f * PI.toFloat()

            val strength = when {
                preset == LauncherBackgroundPreset.LIQUID_GLASS -> if (tokens.isDark) 0.28f else 0.18f
                !tokens.isDark -> 0.10f
                preset == LauncherBackgroundPreset.PURE_OBSIDIAN -> 0.11f
                else -> 0.24f
            }

            // Primary floating nebula glow
            val c1 = Offset(
                x = w * (0.28f + 0.14f * cos(rad)),
                y = h * (0.22f + 0.08f * sin(rad))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orb1Color.copy(alpha = strength), Color.Transparent),
                    center = c1,
                    radius = maxOf(w, h) * 0.62f
                ),
                center = c1,
                radius = maxOf(w, h) * 0.62f
            )

            // Secondary neon cyan / electric horizon glow
            val c2 = Offset(
                x = w * (0.76f - 0.15f * sin(rad)),
                y = h * (0.64f + 0.10f * cos(rad))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orb2Color.copy(alpha = strength * 0.85f), Color.Transparent),
                    center = c2,
                    radius = maxOf(w, h) * 0.54f
                ),
                center = c2,
                radius = maxOf(w, h) * 0.54f
            )

            // Deep core ambient bloom
            val c3 = Offset(
                x = w * 0.5f,
                y = h * (0.44f + 0.05f * sin(rad * 2f))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orb3Color.copy(alpha = strength * 0.55f), Color.Transparent),
                    center = c3,
                    radius = maxOf(w, h) * 0.45f
                ),
                center = c3,
                radius = maxOf(w, h) * 0.45f
            )
        }
    }
}
