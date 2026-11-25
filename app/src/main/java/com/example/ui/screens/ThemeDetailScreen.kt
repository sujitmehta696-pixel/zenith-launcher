package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.ThemeConfig
import com.example.ui.components.GlassCard
import com.example.ui.components.ProBadge
import com.example.ui.components.iconShapeFor
import com.example.ui.theme.toFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeDetailScreen(
    theme: ThemeConfig?,
    activeThemeId: String,
    isPremiumUnlocked: Boolean,
    feedbackMessage: String?,
    onBack: () -> Unit,
    onApplyTheme: (ThemeConfig) -> Unit,
    onResetTheme: () -> Unit,
    onToggleFavorite: (ThemeConfig) -> Unit,
    onUnlockPremium: () -> Unit,
    onClearFeedback: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedbackMessage) {
        if (feedbackMessage != null) {
            snackbarHostState.showSnackbar(feedbackMessage)
            onClearFeedback()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(theme?.name ?: "Theme Details") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("theme_detail_back")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (theme != null) {
                        IconButton(
                            onClick = { onToggleFavorite(theme) },
                            modifier = Modifier.testTag("theme_detail_favorite")
                        ) {
                            Icon(
                                imageVector = if (theme.isFavorite) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite theme",
                                tint = if (theme.isFavorite) Color(0xFFF43F5E) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (theme == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val bg = Color(theme.backgroundHex)
        val surface = Color(theme.surfaceHex)
        val primary = Color(theme.primaryHex)
        val secondary = Color(theme.secondaryHex)
        val accent = Color(theme.accentHex)
        val textColor = Color(theme.textHex)
        val isActive = theme.id == activeThemeId
        val locked = !isPremiumUnlocked

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Live Simulated Mockup Rendered in Target Theme
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(theme.cardRadiusDp.dp))
                        .background(bg)
                        .border(
                            1.5.dp,
                            primary.copy(alpha = 0.45f),
                            RoundedCornerShape(theme.cardRadiusDp.dp)
                        )
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "12:45",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontFamily = theme.fontStyle.toFontFamily()
                                ),
                                color = textColor
                            )
                            Text(
                                text = theme.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = theme.fontStyle.toFontFamily()
                                ),
                                color = primary
                            )
                        }
                        if (theme.isPremium) ProBadge()
                    }

                    // Simulated Full-Ecosystem Widget + App Surface Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(theme.cardRadiusDp.dp))
                            .background(surface)
                            .border(
                                1.dp,
                                primary.copy(alpha = 0.35f),
                                RoundedCornerShape(theme.cardRadiusDp.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SYSTEM WIDGET",
                                style = MaterialTheme.typography.labelSmall,
                                color = primary
                            )
                            Text(
                                text = "Battery 92% • RAM 4.2 GB Free",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = theme.fontStyle.toFontFamily()
                                ),
                                color = textColor
                            )
                        }
                        Text(
                            text = if (theme.associatedWallpaperId != null) "Wall Paired" else "UI + Icons",
                            style = MaterialTheme.typography.labelSmall,
                            color = secondary
                        )
                    }

                    // Simulated App Surface Card & Chromatic App Icons
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(theme.cardRadiusDp.dp))
                            .background(surface)
                            .border(
                                1.dp,
                                textColor.copy(alpha = 0.14f),
                                RoundedCornerShape(theme.cardRadiusDp.dp)
                            )
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = theme.description,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = theme.fontStyle.toFontFamily()
                            ),
                            color = textColor
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("Dialer", "Notes", "Music", "Camera").forEachIndexed { idx, label ->
                                val swatch = when (idx) {
                                    0 -> primary
                                    1 -> secondary
                                    2 -> accent
                                    else -> primary.copy(alpha = 0.7f)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(iconShapeFor(theme.iconStyle))
                                            .background(swatch.copy(alpha = 0.85f))
                                            .border(1.4.dp, primary.copy(alpha = 0.7f), iconShapeFor(theme.iconStyle)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label.take(1),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = bg
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = textColor.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Palette & Specification Breakdown
                GlassCard(contentPadding = PaddingValues(16.dp)) {
                    Text("Palette & Design Tokens", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SwatchDot("Background", bg)
                        SwatchDot("Surface", surface)
                        SwatchDot("Primary", primary)
                        SwatchDot("Secondary", secondary)
                        SwatchDot("Accent", accent)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    SpecRow("Category", theme.category.displayName)
                    SpecRow("UI Appearance", if (theme.isDarkTheme) "Dark / AMOLED" else "Light")
                    SpecRow("Card Style", theme.cardStyle.displayName)
                    SpecRow("Icon Shape", theme.iconStyle.displayName)
                    SpecRow("Typography", theme.fontStyle.displayName)
                    SpecRow("Corner Radius", "${theme.cardRadiusDp} dp")
                }

                // Action Buttons
                if (locked) {
                    Button(
                        onClick = onUnlockPremium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("theme_unlock_pro_button")
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unlock Pro to Apply Theme")
                    }
                } else {
                    Button(
                        onClick = { onApplyTheme(theme) },
                        enabled = !isActive,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("theme_apply_button")
                    ) {
                        if (isActive) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Currently Applied")
                        } else {
                            Text("Apply Theme (UI, Widgets & Icons)")
                        }
                    }
                }

                OutlinedButton(
                    onClick = onResetTheme,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("theme_detail_reset_button")
                ) {
                    Icon(Icons.Rounded.RestartAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset to Default Theme")
                }
            }
        }
    }
}

@Composable
private fun SwatchDot(label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
