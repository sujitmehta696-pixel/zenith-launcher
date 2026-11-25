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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BillingState
import com.example.data.model.CloudUiState
import com.example.data.model.PurchaseRecord
import com.example.data.model.UserProfile
import com.example.ui.components.GlassCard
import com.example.ui.components.ProBadge
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LocalLauncherTokens
import com.example.ui.theme.PremiumGold
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    profileState: CloudUiState<UserProfile?>,
    purchaseHistoryState: CloudUiState<List<PurchaseRecord>>,
    billingState: BillingState,
    syncStatus: String?,
    onBack: () -> Unit,
    onSyncNow: () -> Unit,
    onUpgradePro: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalLauncherTokens.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Account & Cloud Vault") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("account_back")) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 780.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    when (profileState) {
                        CloudUiState.Loading -> {
                            GlassCard(contentPadding = PaddingValues(20.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                    Text("Loading cloud studio profile…", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        is CloudUiState.Error -> {
                            GlassCard(contentPadding = PaddingValues(18.dp)) {
                                Text(
                                    text = "Cloud Sync Warning: ${profileState.message}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFFCA5A5)
                                )
                            }
                        }
                        is CloudUiState.Success -> {
                            val profile = profileState.data
                            GlassCard(contentPadding = PaddingValues(18.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(tokens.primary.copy(alpha = 0.2f))
                                            .border(1.5.dp, tokens.primary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Person,
                                            contentDescription = null,
                                            tint = tokens.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = profile?.displayName ?: "OS Studio Member",
                                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                            )
                                            if (billingState.isPremium || profile?.isSubscriptionCurrentlyActive() == true) {
                                                ProBadge(label = "PRO")
                                            }
                                        }
                                        if (!profile?.handle.isNullOrBlank()) {
                                            Text(
                                                text = "@${profile.handle}",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = tokens.primary
                                            )
                                        }
                                        if (!profile?.email.isNullOrBlank()) {
                                            Text(
                                                text = profile.email.orEmpty(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(tokens.elevatedSurface)
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Verified,
                                            contentDescription = null,
                                            tint = if (billingState.isPremium) EmeraldSuccess else tokens.textSecondary
                                        )
                                        Column {
                                            Text(
                                                text = if (billingState.isPremium) {
                                                    billingState.activePlanName ?: "Premium Unlocked"
                                                } else {
                                                    "Free Standard Tier"
                                                },
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = "Source: ${billingState.purchaseSource.label}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (!billingState.isPremium) {
                                        Button(
                                            onClick = onUpgradePro,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = PremiumGold,
                                                contentColor = Color(0xFF141006)
                                            )
                                        ) {
                                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Upgrade")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    GlassCard(contentPadding = PaddingValues(16.dp)) {
                        Text("Cloud Sync", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Backs up active theme, wallpaper, 3D dial & gestures.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (syncStatus != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = syncStatus,
                                style = MaterialTheme.typography.labelMedium,
                                color = tokens.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onSyncNow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("account_sync_now_button")
                        ) {
                            Icon(Icons.Rounded.CloudSync, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sync Current Setup to Cloud")
                        }
                    }
                }

                item {
                    Text("Verified Pro Receipt History", style = MaterialTheme.typography.titleMedium)
                }

                when (purchaseHistoryState) {
                    CloudUiState.Loading -> {
                        item {
                            GlassCard(contentPadding = PaddingValues(16.dp)) {
                                Text("Loading receipts…", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    is CloudUiState.Error -> {
                        item {
                            GlassCard(contentPadding = PaddingValues(16.dp)) {
                                Text(
                                    text = purchaseHistoryState.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFFCA5A5)
                                )
                            }
                        }
                    }
                    is CloudUiState.Success -> {
                        val records = purchaseHistoryState.data
                        if (records.isEmpty()) {
                            item {
                                GlassCard(contentPadding = PaddingValues(16.dp)) {
                                    Text(
                                        text = "No cloud purchase receipts recorded yet. Purchasing Pro binds your entitlement to this account.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(records, key = { it.id }) { record ->
                                GlassCard(contentPadding = PaddingValues(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Rounded.ReceiptLong, contentDescription = null, tint = PremiumGold)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(record.productId, style = MaterialTheme.typography.titleSmall)
                                            Text(
                                                text = "Verified via ${record.purchaseSource}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        val dateStr = record.createdAt?.toDate()?.let {
                                            SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(it)
                                        } ?: "Pending"
                                        Text(
                                            text = dateStr,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = tokens.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedButton(
                        onClick = onSignOut,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("account_sign_out_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out")
                    }
                }
            }
        }
    }
}
