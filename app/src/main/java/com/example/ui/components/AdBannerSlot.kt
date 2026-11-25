package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.data.ads.AdManager
import com.example.ui.theme.LocalLauncherTokens
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdBannerSlot(
    isPremium: Boolean,
    modifier: Modifier = Modifier
) {
    // Auto-hide all ads immediately when user has an active paid Pro subscription
    if (isPremium) return

    val context = LocalContext.current
    val tokens = LocalLauncherTokens.current
    val canShowWebViewAds = remember(context) {
        !AdManager.isRobolectricEnvironment() && AdManager.isWebViewAvailable(context)
    }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ad_banner_slot"),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SPONSORED • REMOVE ADS WITH PRO",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.textSecondary
                )
            }

            if (!canShowWebViewAds) {
                Text(
                    text = if (AdManager.isRobolectricEnvironment()) {
                        "AdMob Banner Ready (Live on Android device)"
                    } else {
                        "Upgrade to Pro for full-screen distraction-free mode"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp),
                    factory = { ctx ->
                        try {
                            AdView(ctx).apply {
                                setAdSize(AdSize.BANNER)
                                adUnitId = ctx.getString(R.string.admob_banner_unit_id)
                                try {
                                    loadAd(AdRequest.Builder().build())
                                } catch (_: Throwable) {
                                }
                            }
                        } catch (_: Throwable) {
                            android.widget.Space(ctx)
                        }
                    }
                )
            }
        }
    }
}
