package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BillingConnectionStatus
import com.example.data.model.BillingState
import com.example.ui.components.AdBannerSlot
import com.example.ui.components.GlassCard
import com.example.ui.components.ProBadge
import com.example.ui.components.ProFeatureVideoShowcase
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LocalLauncherTokens
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.RoyalPurple

@Composable
fun PremiumScreen(
    billingState: BillingState,
    accountLabel: String? = null,
    paymentGatewayEnabled: Boolean = false,
    paymentProviderName: String = "",
    billingStatus: BillingConnectionStatus = billingState.connectionStatus,
    onOpenAccount: () -> Unit = {},
    onPurchasePlan: (String) -> Unit,
    onRestorePurchases: () -> Unit,
    onPreviewProSphere: () -> Unit = {},
    reduceMotion: Boolean = false,
    onOpenPrivacy: () -> Unit,
    onOpenTerms: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalLauncherTokens.current
    var selectedProductId by remember {
        mutableStateOf(billingState.offers.firstOrNull()?.productId ?: BillingState.SKU_YEARLY)
    }

    LaunchedEffect(billingState.offers) {
        if (billingState.offers.isNotEmpty() &&
            billingState.offers.none { it.productId == selectedProductId }
        ) {
            selectedProductId = billingState.offers.first().productId
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 780.dp)
                .testTag("premium_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Flagship Pro Sphere & Liquid Glass Hero Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0A0F24), Color(0xFF1E133D), Color(0xFF281E0B))
                            )
                        )
                        .border(
                            1.4.dp,
                            Brush.linearGradient(listOf(PremiumGold, ElectricCyan, RoyalPurple)),
                            RoundedCornerShape(26.dp)
                        )
                        .padding(22.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProBadge(label = if (billingState.isPremium) "PRO ACTIVE" else "PRO EDITION")
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                HeroChipPill(text = "3D Sphere")
                                HeroChipPill(text = "Liquid Glass")
                            }
                        }

                        Text(
                            text = if (billingState.isPremium) "Pro Studio Unlocked" else "3D Pro Sphere & Liquid Studio",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFAF5E8)
                        )
                        val expirySummary = remember(billingState.planExpiresAtMillis, billingState.activePlanName) {
                            val planTitle = billingState.activePlanName?.takeIf { it.isNotBlank() }
                            val exp = billingState.planExpiresAtMillis
                            val expLabel = if (exp != null && exp > 0L) {
                                val fmt = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                                "Valid until ${fmt.format(java.util.Date(exp))}"
                            } else {
                                null
                            }
                            listOfNotNull(billingState.purchaseSource.label, planTitle, expLabel).joinToString(" • ")
                        }

                        Text(
                            text = if (billingState.isPremium) {
                                expirySummary
                            } else {
                                "Orbit every installed app inside a 360° 3D Vortex Sphere, unlock Liquid Glass frost, 60+ Pro Themes & AMOLED Walls, and go completely ad-free."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFD6E0F5)
                        )

                        if (billingState.isPremium) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Button(
                                onClick = onPreviewProSphere,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PremiumGold,
                                    contentColor = Color(0xFF141006)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("premium_try_pro_sphere_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Public,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Activate 3D Pro Sphere", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 1b. Interactive Pro Plan Features Video Tour
            item {
                ProFeatureVideoShowcase(
                    isPremium = billingState.isPremium,
                    reduceMotion = reduceMotion,
                    onActionClick = {
                        if (billingState.isPremium) {
                            onPreviewProSphere()
                        } else {
                            onPurchasePlan(selectedProductId)
                        }
                    },
                    actionLabel = if (billingState.isPremium) "Activate 3D Pro Sphere" else "Unlock Selected Plan"
                )
            }

            // 2. Cloud Account Pro Vault Binding Card
            item {
                GlassCard(
                    onClick = onOpenAccount,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(tokens.primary.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CloudDone,
                                contentDescription = null,
                                tint = tokens.primary
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = accountLabel ?: "Cloud Pro Vault",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Syncs Pro status seamlessly across logins",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 3. Benefits Matrix Card
            item {
                GlassCard(contentPadding = PaddingValues(18.dp)) {
                    Text(
                        text = "Pro Exclusives",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    BenefitRow(
                        icon = Icons.Rounded.Public,
                        accent = ElectricCyan,
                        title = "3D Pro Sphere All-App Launcher",
                        subtitle = "Multi-band 360° spherical app vortex with live analog core & z-depth orbiting"
                    )
                    BenefitRow(
                        icon = Icons.Rounded.RocketLaunch,
                        accent = PremiumGold,
                        title = "Cyber Vortex & Violet Halo Engines",
                        subtitle = "3-axis holographic gyro rings & luxury champagne bezels"
                    )
                    BenefitRow(
                        icon = Icons.Rounded.BlurOn,
                        accent = Color(0xFF38BDF8),
                        title = "Liquid Glass Visual Suite",
                        subtitle = "Crystal specular borders & frosted caustic studio surfaces"
                    )
                    BenefitRow(
                        icon = Icons.Rounded.Wallpaper,
                        accent = RoyalPurple,
                        title = "Pro AMOLED Vault & Themes",
                        subtitle = "60 luxury 3D wallpapers, 60 full UI/widget theme suites & 6-column grid"
                    )
                    BenefitRow(
                        icon = Icons.Rounded.Verified,
                        accent = EmeraldSuccess,
                        title = "Ad-Free Launcher",
                        subtitle = "Zero banners across studio & launcher customization screens"
                    )
                }
            }

            // 4. Verified Checkout Info Card (when not already Pro)
            if (!billingState.isPremium) {
                item {
                    GlassCard(
                        modifier = Modifier.testTag("verified_checkout_info_card"),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(ElectricCyan.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Security,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (paymentGatewayEnabled) {
                                        "In-App Checkout (${paymentProviderName.ifBlank { "Gateway" }})"
                                    } else {
                                        billingStatus.label
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (paymentGatewayEnabled) {
                                        "Payment opens securely inside this app (never Chrome). Pro activates automatically after real payment & webhook verification."
                                    } else {
                                        "Pro activates automatically after verified payment confirmation."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 5. Membership Plans
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choose a Plan",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    billingState.offers.forEach { offer ->
                        val isSelected = selectedProductId == offer.productId
                        val displayPrice = offer.formattedPlayPrice ?: offer.fallbackPrice
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(tokens.cardRadius))
                                .background(
                                    if (isSelected) {
                                        Brush.linearGradient(
                                            listOf(
                                                tokens.primary.copy(alpha = 0.18f),
                                                PremiumGold.copy(alpha = 0.10f)
                                            )
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            listOf(
                                                tokens.surface.copy(alpha = if (tokens.liquidGlassEnabled) 0.64f else 1f),
                                                tokens.surface.copy(alpha = if (tokens.liquidGlassEnabled) 0.64f else 1f)
                                            )
                                        )
                                    }
                                )
                                .border(
                                    width = if (isSelected) 1.6.dp else 1.dp,
                                    color = if (isSelected) PremiumGold else tokens.borderSubtle,
                                    shape = RoundedCornerShape(tokens.cardRadius)
                                )
                                .clickable { selectedProductId = offer.productId }
                                .padding(16.dp)
                                .testTag("plan_${offer.productId}"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = offer.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    offer.badge?.let { ProBadge(label = it) }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = offer.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = offer.billingPeriodLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = tokens.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = displayPrice,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) PremiumGold else tokens.primary
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = PremiumGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. Status Message & Purchase CTA
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (billingState.statusMessage != null) {
                        GlassCard(contentPadding = PaddingValues(12.dp)) {
                            Text(
                                text = billingState.statusMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Button(
                        onClick = { onPurchasePlan(selectedProductId) },
                        enabled = !billingState.isProcessingPurchase && !billingState.isPremium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PremiumGold,
                            contentColor = Color(0xFF141006),
                            disabledContainerColor = EmeraldSuccess.copy(alpha = 0.22f),
                            disabledContentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("premium_subscribe_button")
                    ) {
                        if (billingState.isProcessingPurchase) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = if (billingState.isPremium) Icons.Rounded.CheckCircle else Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = if (billingState.isPremium) EmeraldSuccess else Color(0xFF141006)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    billingState.isPremium -> "Pro Active"
                                    paymentGatewayEnabled -> "Pay In-App & Unlock Pro"
                                    else -> "Unlock Studio Pro"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onRestorePurchases,
                        enabled = !billingState.isProcessingPurchase,
                        border = BorderStroke(1.dp, tokens.borderSubtle),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("premium_restore_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Restore,
                            contentDescription = null,
                            tint = tokens.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restore Purchases", fontWeight = FontWeight.Medium)
                    }
                }
            }

            // 7. Auto-hiding Ad Banner for Free users
            item {
                AdBannerSlot(isPremium = billingState.isPremium)
            }

            // 8. Terms & Privacy Links
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = onOpenTerms) { Text("Terms & Conditions") }
                    TextButton(onClick = onOpenPrivacy) { Text("Privacy Policy") }
                }
            }
        }
    }
}

@Composable
private fun HeroChipPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .border(0.7.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White
        )
    }
}

@Composable
private fun BenefitRow(
    icon: ImageVector,
    accent: Color,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(19.dp))
        }
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
