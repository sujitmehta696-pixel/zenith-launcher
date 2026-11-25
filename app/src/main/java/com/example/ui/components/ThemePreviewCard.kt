package com.example.ui.components

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
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ThemeConfig
import com.example.ui.theme.toFontFamily

@Composable
fun ThemePreviewCard(
    theme: ThemeConfig,
    isActive: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPremiumUnlocked: Boolean = true
) {
    val bg = Color(theme.backgroundHex)
    val surface = Color(theme.surfaceHex)
    val primary = Color(theme.primaryHex)
    val secondary = Color(theme.secondaryHex)
    val accent = Color(theme.accentHex)
    val textColor = Color(theme.textHex)
    val radius = theme.cardRadiusDp.coerceIn(8, 24).dp
    val shape = RoundedCornerShape(radius)

    val borderBrush = if (isActive) {
        Brush.linearGradient(listOf(primary, accent))
    } else {
        Brush.linearGradient(listOf(textColor.copy(alpha = 0.14f), textColor.copy(alpha = 0.06f)))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bg)
            .border(width = if (isActive) 2.dp else 1.dp, brush = borderBrush, shape = shape)
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag("theme_card_${theme.id}")
    ) {
        // Mini-UI Preview Stage rendered directly in the Theme's actual palette & typography
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .clip(RoundedCornerShape((theme.cardRadiusDp * 0.7f).coerceAtLeast(6f).dp))
                .background(
                    Brush.linearGradient(
                        listOf(surface, bg.copy(alpha = 0.95f))
                    )
                )
                .border(
                    1.dp,
                    primary.copy(alpha = 0.28f),
                    RoundedCornerShape((theme.cardRadiusDp * 0.7f).coerceAtLeast(6f).dp)
                )
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Aa",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = theme.fontStyle.toFontFamily()
                        ),
                        color = textColor
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(11.dp).clip(CircleShape).background(primary))
                        Box(Modifier.size(11.dp).clip(CircleShape).background(secondary))
                        Box(Modifier.size(11.dp).clip(CircleShape).background(accent))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Simulated dock strip inside preview
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { idx ->
                        val iconColor = when (idx) {
                            0 -> primary
                            1 -> secondary
                            2 -> accent
                            else -> textColor.copy(alpha = 0.35f)
                        }
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(iconShapeFor(theme.iconStyle))
                                .background(iconColor.copy(alpha = 0.85f))
                        )
                    }
                }
            }

            if (theme.isPremium || !isPremiumUnlocked) {
                ProBadge(
                    label = if (theme.isPremium) "PRO" else "PAID",
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            } else if (isActive) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Active theme",
                    tint = primary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = theme.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = theme.fontStyle.toFontFamily()
                    ),
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${theme.category.displayName} • ${theme.cardStyle.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.68f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("fav_theme_${theme.id}")
            ) {
                Icon(
                    imageVector = if (theme.isFavorite) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite ${theme.name}",
                    tint = if (theme.isFavorite) Color(0xFFF43F5E) else textColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
