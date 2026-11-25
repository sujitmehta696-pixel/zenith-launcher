package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.catalog.FreeThemeCatalog
import com.example.data.model.AppearanceMode
import com.example.data.model.LauncherBackgroundPreset
import com.example.data.model.ThemeCardStyle
import com.example.data.model.ThemeConfig
import com.example.data.model.ThemeIconStyle

@Immutable
data class LauncherThemeTokens(
    val themeId: String,
    val themeName: String,
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val elevatedSurface: Color,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val borderSubtle: Color,
    val cardStyle: ThemeCardStyle,
    val iconStyle: ThemeIconStyle,
    val cardRadius: Dp,
    val reduceMotion: Boolean,
    val backgroundPreset: LauncherBackgroundPreset = LauncherBackgroundPreset.MIDNIGHT_COSMOS,
    val liquidGlassEnabled: Boolean = true
)

val LocalLauncherTokens = staticCompositionLocalOf {
    LauncherThemeTokens(
        themeId = "theme_free_obsidian_core",
        themeName = "Obsidian Core",
        isDark = true,
        background = MidnightBlack,
        surface = MidnightSurface,
        elevatedSurface = ElevatedMidnight,
        primary = NeonCyan,
        secondary = RoyalPurple,
        accent = SoftViolet,
        textPrimary = PremiumWhite,
        textSecondary = MutedSlateText,
        borderSubtle = Color.White.copy(alpha = 0.14f),
        cardStyle = ThemeCardStyle.GLASSMORPHIC,
        iconStyle = ThemeIconStyle.SQUIRCLE,
        cardRadius = 20.dp,
        reduceMotion = false,
        backgroundPreset = LauncherBackgroundPreset.MIDNIGHT_COSMOS,
        liquidGlassEnabled = true
    )
}

@Composable
fun AndroidLauncherTheme(
    activeTheme: ThemeConfig = FreeThemeCatalog.items.first(),
    appearanceMode: AppearanceMode = AppearanceMode.DARK,
    reduceMotion: Boolean = false,
    backgroundPreset: LauncherBackgroundPreset = LauncherBackgroundPreset.MIDNIGHT_COSMOS,
    liquidGlassEnabled: Boolean = true,
    iconStyleOverride: ThemeIconStyle? = null,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val effectiveDark = when (appearanceMode) {
        AppearanceMode.DARK -> true
        AppearanceMode.LIGHT -> false
        AppearanceMode.SYSTEM -> systemDark
    }

    val primary = Color(activeTheme.primaryHex)
    val secondary = Color(activeTheme.secondaryHex)
    val accent = Color(activeTheme.accentHex)

    val bg: Color
    val surf: Color
    val elevated: Color
    val textMain: Color
    val textMuted: Color
    val border: Color

    if (effectiveDark == activeTheme.isDarkTheme) {
        bg = Color(activeTheme.backgroundHex)
        surf = Color(activeTheme.surfaceHex)
        elevated = if (effectiveDark) surf.blendWith(Color.White, 0.06f) else surf.blendWith(Color.Black, 0.03f)
        textMain = Color(activeTheme.textHex)
        textMuted = textMain.copy(alpha = 0.68f)
        border = if (effectiveDark) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.09f)
    } else if (effectiveDark) {
        bg = MidnightBlack
        surf = MidnightSurface
        elevated = ElevatedMidnight
        textMain = PremiumWhite
        textMuted = MutedSlateText
        border = Color.White.copy(alpha = 0.14f)
    } else {
        bg = LightCanvas
        surf = LightSurface
        elevated = Color(0xFFE7ECF8)
        textMain = LightInkText
        textMuted = Color(0xFF475569)
        border = Color.Black.copy(alpha = 0.09f)
    }

    val onPrimaryColor = if (primary.luminance() > 0.45f) Color(0xFF080B18) else Color.White

    val colorScheme = if (effectiveDark) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimaryColor,
            primaryContainer = primary.copy(alpha = 0.22f),
            onPrimaryContainer = textMain,
            secondary = secondary,
            onSecondary = Color.White,
            secondaryContainer = secondary.copy(alpha = 0.20f),
            onSecondaryContainer = textMain,
            tertiary = accent,
            background = bg,
            onBackground = textMain,
            surface = surf,
            onSurface = textMain,
            surfaceVariant = elevated,
            onSurfaceVariant = textMuted,
            outline = border,
            error = CoralError,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimaryColor,
            primaryContainer = primary.copy(alpha = 0.14f),
            onPrimaryContainer = textMain,
            secondary = secondary,
            onSecondary = Color.White,
            secondaryContainer = secondary.copy(alpha = 0.14f),
            onSecondaryContainer = textMain,
            tertiary = accent,
            background = bg,
            onBackground = textMain,
            surface = surf,
            onSurface = textMain,
            surfaceVariant = elevated,
            onSurfaceVariant = textMuted,
            outline = border,
            error = CoralError,
            onError = Color.White
        )
    }

    val radius = activeTheme.cardRadiusDp.dp
    val shapes = Shapes(
        small = RoundedCornerShape((activeTheme.cardRadiusDp * 0.6f).dp),
        medium = RoundedCornerShape(radius),
        large = RoundedCornerShape((activeTheme.cardRadiusDp * 1.25f).dp)
    )

    val tokens = LauncherThemeTokens(
        themeId = activeTheme.id,
        themeName = activeTheme.name,
        isDark = effectiveDark,
        background = bg,
        surface = surf,
        elevatedSurface = elevated,
        primary = primary,
        secondary = secondary,
        accent = accent,
        textPrimary = textMain,
        textSecondary = textMuted,
        borderSubtle = border,
        cardStyle = activeTheme.cardStyle,
        iconStyle = iconStyleOverride ?: activeTheme.iconStyle,
        cardRadius = radius,
        reduceMotion = reduceMotion,
        backgroundPreset = backgroundPreset,
        liquidGlassEnabled = liquidGlassEnabled
    )

    CompositionLocalProvider(LocalLauncherTokens provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = buildLauncherTypography(activeTheme.fontStyle),
            shapes = shapes,
            content = content
        )
    }
}

private fun Color.blendWith(other: Color, ratio: Float): Color {
    val inv = 1f - ratio
    return Color(
        red = red * inv + other.red * ratio,
        green = green * inv + other.green * ratio,
        blue = blue * inv + other.blue * ratio,
        alpha = alpha
    )
}
