package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.ThemeCardStyle
import com.example.ui.theme.LocalLauncherTokens
import com.example.ui.theme.PremiumGold

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    cornerRadius: Dp = LocalLauncherTokens.current.cardRadius,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalLauncherTokens.current
    val shape = RoundedCornerShape(cornerRadius)
    val isLiquidGlass = tokens.liquidGlassEnabled

    val containerColor = when {
        isLiquidGlass -> tokens.surface.copy(alpha = if (tokens.isDark) 0.56f else 0.72f)
        tokens.cardStyle == ThemeCardStyle.GLASSMORPHIC -> tokens.surface.copy(alpha = if (tokens.isDark) 0.72f else 0.85f)
        tokens.cardStyle == ThemeCardStyle.SOLID_ELEVATED -> tokens.surface
        tokens.cardStyle == ThemeCardStyle.OUTLINED_MINIMAL -> tokens.background
        else -> tokens.surface.copy(alpha = if (tokens.isDark) 0.82f else 0.92f)
    }

    val borderModifier = when {
        isLiquidGlass -> Modifier.border(
            width = 1.15.dp,
            brush = Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = if (tokens.isDark) 0.42f else 0.92f),
                    tokens.primary.copy(alpha = if (tokens.isDark) 0.28f else 0.34f),
                    Color.White.copy(alpha = if (tokens.isDark) 0.08f else 0.45f),
                    tokens.secondary.copy(alpha = if (tokens.isDark) 0.24f else 0.22f)
                )
            ),
            shape = shape
        )
        tokens.cardStyle == ThemeCardStyle.GRADIENT_BORDER -> Modifier.border(
            width = 1.2.dp,
            brush = Brush.linearGradient(
                listOf(
                    tokens.primary.copy(alpha = 0.65f),
                    tokens.secondary.copy(alpha = 0.35f),
                    tokens.accent.copy(alpha = 0.50f)
                )
            ),
            shape = shape
        )
        tokens.cardStyle == ThemeCardStyle.OUTLINED_MINIMAL -> Modifier.border(
            width = 1.dp,
            color = tokens.borderSubtle,
            shape = shape
        )
        tokens.cardStyle == ThemeCardStyle.GLASSMORPHIC -> Modifier.border(
            width = 1.dp,
            brush = Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = if (tokens.isDark) 0.16f else 0.60f),
                    Color.White.copy(alpha = if (tokens.isDark) 0.04f else 0.20f)
                )
            ),
            shape = shape
        )
        else -> Modifier.border(
            width = 0.5.dp,
            color = tokens.borderSubtle.copy(alpha = 0.6f),
            shape = shape
        )
    }

    val sheenModifier = if (isLiquidGlass) {
        Modifier.background(
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = if (tokens.isDark) 0.12f else 0.44f),
                    Color.Transparent,
                    tokens.primary.copy(alpha = if (tokens.isDark) 0.05f else 0.04f)
                )
            )
        )
    } else {
        Modifier
    }

    Surface(
        modifier = modifier
            .clip(shape)
            .then(borderModifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shape,
        color = containerColor,
        tonalElevation = if (!isLiquidGlass && tokens.cardStyle == ThemeCardStyle.SOLID_ELEVATED) 4.dp else 0.dp
    ) {
        Column(
            modifier = sheenModifier.padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun ProBadge(
    modifier: Modifier = Modifier,
    label: String = "PRO"
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(PremiumGold.copy(alpha = 0.92f), Color(0xFFF59E0B))
                )
            )
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFF141006),
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF141006)
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
