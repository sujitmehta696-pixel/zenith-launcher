package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LocalLauncherTokens
import com.example.ui.theme.PremiumGold
import com.example.ui.viewmodel.GatewayCheckoutUiState

private class GatewayJsBridge(
    private val onPaymentEvent: (String?) -> Unit
) {
    @JavascriptInterface
    fun onPaymentCompleted(paymentId: String?) {
        onPaymentEvent(paymentId)
    }

    @JavascriptInterface
    fun onPaymentSuccess(paymentId: String?) {
        onPaymentEvent(paymentId)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InAppPaymentWebViewSheet(
    checkoutState: GatewayCheckoutUiState,
    providerName: String,
    onVerifyPayment: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val tokens = LocalLauncherTokens.current
    var pageProgress by remember { mutableIntStateOf(0) }
    var isPageLoading by remember { mutableStateOf(true) }
    var detectedPaymentId by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(tokens.background)
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("inapp_payment_webview_dialog")
        ) {
            // Top Secure Checkout Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tokens.surface.copy(alpha = 0.96f))
                    .border(0.8.dp, tokens.borderSubtle)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(EmeraldSuccess.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = "Secure in-app checkout",
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "In-App Secure Checkout",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (checkoutState.isAutoChecking) {
                            "$providerName • Auto-verifying every 3.5s"
                        } else {
                            "$providerName • Pro remains locked until verified"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("inapp_webview_close")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close checkout",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (isPageLoading || checkoutState.isLoading) {
                LinearProgressIndicator(
                    progress = { if (checkoutState.isLoading) 0.5f else (pageProgress / 100f).coerceIn(0.05f, 1f) },
                    color = ElectricCyan,
                    trackColor = tokens.borderSubtle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                )
            }

            // Body: Session creation spinner, error state, or full embedded WebView (NEVER Chrome)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val session = checkoutState.session
                when {
                    checkoutState.isLoading && session == null -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            CircularProgressIndicator(color = ElectricCyan)
                            Text(
                                text = "Connecting to $providerName checkout…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    checkoutState.errorMessage != null && session == null -> {
                        GlassCard(
                            contentPadding = PaddingValues(20.dp),
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Checkout Unavailable",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = checkoutState.errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                                Text("Close")
                            }
                        }
                    }

                    session != null -> {
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.javaScriptCanOpenWindowsAutomatically = false
                                    settings.setSupportMultipleWindows(false)
                                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                                    settings.useWideViewPort = true
                                    settings.loadWithOverviewMode = true

                                    addJavascriptInterface(
                                        GatewayJsBridge { paymentId ->
                                            post {
                                                detectedPaymentId = paymentId
                                                onVerifyPayment(paymentId)
                                            }
                                        },
                                        "AndroidGatewayBridge"
                                    )

                                    webChromeClient = object : WebChromeClient() {
                                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                            pageProgress = newProgress
                                        }
                                    }

                                    webViewClient = object : WebViewClient() {
                                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                            isPageLoading = true
                                            inspectUrlForCompletion(url)?.let { foundTx ->
                                                detectedPaymentId = foundTx
                                            }
                                        }

                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            isPageLoading = false
                                            val tx = inspectUrlForCompletion(url)
                                            if (tx != null) {
                                                detectedPaymentId = tx
                                                onVerifyPayment(tx)
                                            }
                                        }

                                        override fun shouldOverrideUrlLoading(
                                            view: WebView?,
                                            request: WebResourceRequest?
                                        ): Boolean {
                                            val uri = request?.url ?: return false
                                            val scheme = uri.scheme?.lowercase().orEmpty()
                                            return if (scheme == "http" || scheme == "https") {
                                                val tx = inspectUrlForCompletion(uri.toString())
                                                if (tx != null) {
                                                    detectedPaymentId = tx
                                                    onVerifyPayment(tx)
                                                }
                                                false // Always stay inside in-app WebView (NEVER Chrome!)
                                            } else if (scheme == "intent") {
                                                runCatching {
                                                    val parsed = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME)
                                                    // Strip browser fallback so Chrome is NEVER opened
                                                    parsed.removeExtra("browser_fallback_url")
                                                    val targetScheme = parsed.scheme?.lowercase().orEmpty()
                                                    val dataScheme = parsed.data?.scheme?.lowercase().orEmpty()
                                                    if (targetScheme !in setOf("http", "https") &&
                                                        dataScheme !in setOf("http", "https")
                                                    ) {
                                                        parsed.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        context.startActivity(parsed)
                                                    }
                                                }
                                                true
                                            } else if (scheme in setOf(
                                                    "upi", "tez", "phonepe", "paytmmp",
                                                    "bhim", "gpay", "credpay", "amazonpay"
                                                )
                                            ) {
                                                runCatching {
                                                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    }
                                                    context.startActivity(intent)
                                                }
                                                true
                                            } else {
                                                true
                                            }
                                        }
                                    }

                                    loadUrl(session.checkoutUrl)
                                }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("inapp_payment_webview")
                        )
                    }
                }
            }

            // Bottom Webhook / Verification Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tokens.surface.copy(alpha = 0.96f))
                    .border(0.8.dp, tokens.borderSubtle)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = checkoutState.verificationMessage
                            ?: "Complete Auto-UPI payment above — unlocks automatically.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ElectricCyan,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (checkoutState.isAutoChecking) {
                        Spacer(modifier = Modifier.width(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = EmeraldSuccess
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("inapp_webview_cancel_button")
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { onVerifyPayment(detectedPaymentId) },
                        enabled = !checkoutState.isLoading && checkoutState.session != null,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PremiumGold,
                            contentColor = Color(0xFF141006)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("inapp_webview_verify_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VerifiedUser,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verify & Unlock Pro", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun inspectUrlForCompletion(rawUrl: String?): String? {
    if (rawUrl.isNullOrBlank()) return null
    val uri = runCatching { Uri.parse(rawUrl) }.getOrNull() ?: return null
    val paymentId = uri.getQueryParameter("razorpay_payment_id")
        ?: uri.getQueryParameter("payment_id")
        ?: uri.getQueryParameter("transactionId")
        ?: uri.getQueryParameter("txnid")
        ?: uri.getQueryParameter("stripe_session_id")
    if (!paymentId.isNullOrBlank()) return paymentId

    val status = uri.getQueryParameter("status")?.lowercase().orEmpty()
    val paid = uri.getQueryParameter("paid")?.lowercase().orEmpty()
    if (status in setOf("paid", "success", "captured", "succeeded") || paid == "true") {
        return uri.getQueryParameter("orderId") ?: "callback_paid"
    }
    return null
}
