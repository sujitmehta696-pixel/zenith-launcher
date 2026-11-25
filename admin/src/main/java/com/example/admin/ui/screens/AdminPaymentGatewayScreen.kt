package com.example.admin.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CurrencyRupee
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Webhook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.admin.data.PaymentGatewaySecrets
import com.example.admin.data.RemoteAppConfig
import com.example.admin.data.SubscriptionPlanItem
import com.example.admin.ui.theme.AdminBorderSubtle
import com.example.admin.ui.theme.AdminDanger
import com.example.admin.ui.theme.AdminElectricBlue
import com.example.admin.ui.theme.AdminNeonCyan
import com.example.admin.ui.theme.AdminRoyalPurple
import com.example.admin.ui.theme.AdminSuccess
import com.example.admin.ui.theme.AdminSurface
import com.example.admin.ui.theme.AdminSurfaceElevated
import com.example.admin.ui.theme.AdminTextSecondary
import kotlin.math.roundToInt

private val GATEWAY_PROVIDERS = listOf(
    "FamGateway v2.0",
    "Razorpay",
    "Cashfree",
    "PhonePe",
    "Custom Webhook"
)

@Composable
fun AdminPaymentGatewayScreen(
    config: RemoteAppConfig,
    secrets: PaymentGatewaySecrets,
    plans: List<SubscriptionPlanItem>,
    onSaveGateway: (RemoteAppConfig, String, String) -> Unit,
    onSavePlan: (
        planId: String?,
        planName: String,
        subtitle: String,
        priceInr: Double,
        validityDays: Int,
        badge: String,
        sortOrder: Int,
        isActive: Boolean
    ) -> Unit,
    onDeletePlan: (String) -> Unit,
    onSeedDefaultPlans: () -> Unit
) {
    var draftConfig by remember { mutableStateOf(config) }
    var secretApiKey by remember { mutableStateOf(secrets.secretApiKey) }
    var webhookSecret by remember { mutableStateOf(secrets.webhookSecret) }
    var showSecretKey by remember { mutableStateOf(false) }
    var showWebhookSecret by remember { mutableStateOf(false) }

    // USD -> INR Converter state
    var usdInput by remember { mutableStateOf("9.99") }
    var rateInput by remember { mutableStateOf(config.usdToInrRate.toString()) }

    // Subscription Plan Editor state
    var editingPlanId by remember { mutableStateOf<String?>(null) }
    var planNameInput by remember { mutableStateOf("") }
    var planPriceInrInput by remember { mutableStateOf("") }
    var planValidityDaysInput by remember { mutableStateOf("30") }
    var planBadgeInput by remember { mutableStateOf("") }
    var planSubtitleInput by remember { mutableStateOf("") }
    var planSortOrderInput by remember { mutableStateOf("1") }
    var planIsActive by remember { mutableStateOf(true) }

    LaunchedEffect(config) {
        draftConfig = config
        rateInput = config.usdToInrRate.toString()
    }
    LaunchedEffect(secrets) {
        secretApiKey = secrets.secretApiKey
        webhookSecret = secrets.webhookSecret
    }

    val usdRate = rateInput.toDoubleOrNull()?.coerceIn(1.0, 10000.0) ?: 85.0
    val convertedInr = ((usdInput.toDoubleOrNull() ?: 0.0) * usdRate).roundToInt().coerceAtLeast(0)

    fun formatInrPriceLabel(amountText: String, suffix: String): String {
        val numeric = amountText.trim().toDoubleOrNull()?.roundToInt()
        return if (numeric != null && numeric > 0) "₹$numeric$suffix" else "₹$amountText$suffix"
    }

    fun resetPlanForm() {
        editingPlanId = null
        planNameInput = ""
        planPriceInrInput = ""
        planValidityDaysInput = "30"
        planBadgeInput = ""
        planSubtitleInput = ""
        planSortOrderInput = ((plans.size + 1).coerceAtLeast(1)).toString()
        planIsActive = true
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_payment_gateway_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. FamGateway v2.0 & Backend Endpoint Configuration
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = AdminSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AdminBorderSubtle, RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Payments,
                            contentDescription = null,
                            tint = AdminNeonCyan
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "FamGateway v2.0 (Auto-UPI)",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Text(
                                text = "Checkout opens in-app WebView (UPI intent only, no Chrome/QR). Webhook & 3.5s verify auto-unlocks subscriptions.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AdminTextSecondary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable In-App Auto-UPI Gateway",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (draftConfig.paymentGatewayEnabled) {
                                    "Active — Unlocks subscription only after FamGateway verify/webhook"
                                } else {
                                    "Disabled — Uses Google Play Billing only"
                                },
                                color = if (draftConfig.paymentGatewayEnabled) AdminSuccess else AdminTextSecondary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Switch(
                            checked = draftConfig.paymentGatewayEnabled,
                            onCheckedChange = { draftConfig = draftConfig.copy(paymentGatewayEnabled = it) },
                            modifier = Modifier.testTag("gateway_enabled_switch")
                        )
                    }

                    Text(
                        text = "Gateway Provider",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GATEWAY_PROVIDERS.forEach { provider ->
                            FilterChip(
                                selected = draftConfig.paymentProviderName.equals(provider, ignoreCase = true),
                                onClick = {
                                    draftConfig = draftConfig.copy(
                                        paymentProviderName = provider,
                                        paymentCurrency = "INR"
                                    )
                                },
                                label = { Text(provider) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = draftConfig.paymentBackendUrl,
                        onValueChange = { raw ->
                            val clean = raw.take(500)
                            val autoVerify = if (clean.isNotBlank() && draftConfig.paymentVerifyUrl.isBlank()) {
                                val base = clean.trim().trimEnd('/')
                                if (base.endsWith("/api/create-order")) {
                                    base.removeSuffix("/api/create-order") + "/api/verify-order.php"
                                } else if (!base.contains("/api/")) {
                                    "$base/api/verify-order.php"
                                } else {
                                    draftConfig.paymentVerifyUrl
                                }
                            } else {
                                draftConfig.paymentVerifyUrl
                            }
                            draftConfig = draftConfig.copy(
                                paymentBackendUrl = clean,
                                paymentVerifyUrl = autoVerify
                            )
                        },
                        label = { Text("Node.js Backend URL (Base or /api/create-order)") },
                        placeholder = { Text("https://your-backend.onrender.com") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gateway_backend_url_input")
                    )

                    OutlinedTextField(
                        value = draftConfig.paymentVerifyUrl,
                        onValueChange = { draftConfig = draftConfig.copy(paymentVerifyUrl = it.take(500)) },
                        label = { Text("Verify Order Polling URL (/api/verify-order.php)") },
                        placeholder = { Text("https://your-backend.onrender.com/api/verify-order.php") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gateway_verify_url_input")
                    )

                    if (draftConfig.paymentBackendUrl.isNotBlank()) {
                        val trimmedBase = draftConfig.paymentBackendUrl.trim().trimEnd('/')
                            .removeSuffix("/api/create-order")
                            .removeSuffix("/api/payments/create-checkout")
                        Text(
                            text = "Webhook URL for FamGateway Dashboard: $trimmedBase/famgateway-webhook",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = AdminNeonCyan
                        )
                    }
                }
            }
        }

        // 2. Admin-Only Private FamGateway Secret API Key Vault
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = AdminSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AdminElectricBlue.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = AdminElectricBlue
                        )
                        Column {
                            Text(
                                text = "FamGateway Secret Vault (/admin_secrets)",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Strictly hidden from User App — read only by Admin APK & Node.js backend.",
                                style = MaterialTheme.typography.labelMedium,
                                color = AdminTextSecondary
                            )
                        }
                    }

                    OutlinedTextField(
                        value = secretApiKey,
                        onValueChange = { secretApiKey = it.take(300) },
                        label = { Text("FamGateway Secret API Key") },
                        placeholder = { Text("fg_live_...") },
                        singleLine = true,
                        visualTransformation = if (showSecretKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showSecretKey = !showSecretKey }) {
                                Icon(
                                    imageVector = if (showSecretKey) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = "Toggle secret key visibility"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gateway_secret_key_input")
                    )

                    OutlinedTextField(
                        value = webhookSecret,
                        onValueChange = { webhookSecret = it.take(300) },
                        label = { Text("FamGateway Webhook Secret") },
                        placeholder = { Text("whsec_...") },
                        singleLine = true,
                        visualTransformation = if (showWebhookSecret) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showWebhookSecret = !showWebhookSecret }) {
                                Icon(
                                    imageVector = if (showWebhookSecret) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = "Toggle webhook secret visibility"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gateway_webhook_secret_input")
                    )

                    Button(
                        onClick = {
                            val cleanRate = rateInput.toDoubleOrNull()?.coerceIn(1.0, 10000.0) ?: 85.0
                            onSaveGateway(
                                draftConfig.copy(
                                    paymentCurrency = "INR",
                                    usdToInrRate = cleanRate
                                ),
                                secretApiKey,
                                webhookSecret
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_payment_gateway_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = AdminRoyalPurple),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Rounded.Save, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Save FamGateway Settings & Secrets")
                    }
                }
            }
        }

        // 3. USD -> INR Converter & Editable Built-in Pro INR Pricing
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = AdminSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AdminBorderSubtle, RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.CurrencyRupee, contentDescription = null, tint = AdminSuccess)
                        Column {
                            Text(
                                text = "INR (₹) Converter & Pro Plan Price Editor",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Convert USD to INR and set any custom Pro plan price in ₹.",
                                style = MaterialTheme.typography.labelMedium,
                                color = AdminTextSecondary
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = usdInput,
                            onValueChange = { usdInput = it.take(12) },
                            label = { Text("USD ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("usd_converter_input")
                        )
                        OutlinedTextField(
                            value = rateInput,
                            onValueChange = { rateInput = it.take(10) },
                            label = { Text("1 USD = ₹ Rate") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("usd_rate_input")
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = AdminSurfaceElevated,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Converted INR: ₹$convertedInr",
                                color = AdminSuccess,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(
                                onClick = {
                                    if (convertedInr > 0) {
                                        planPriceInrInput = convertedInr.toString()
                                    }
                                }
                            ) {
                                Text("Use in Plan", color = AdminNeonCyan)
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = draftConfig.monthlyAmount,
                            onValueChange = { amt ->
                                val clean = amt.take(16)
                                draftConfig = draftConfig.copy(
                                    monthlyAmount = clean,
                                    monthlyPriceLabel = formatInrPriceLabel(clean, " / month")
                                )
                            },
                            label = { Text("Monthly ₹") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = draftConfig.yearlyAmount,
                            onValueChange = { amt ->
                                val clean = amt.take(16)
                                draftConfig = draftConfig.copy(
                                    yearlyAmount = clean,
                                    yearlyPriceLabel = formatInrPriceLabel(clean, " / year")
                                )
                            },
                            label = { Text("Yearly ₹") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = draftConfig.lifetimeAmount,
                            onValueChange = { amt ->
                                val clean = amt.take(16)
                                draftConfig = draftConfig.copy(
                                    lifetimeAmount = clean,
                                    lifetimePriceLabel = formatInrPriceLabel(clean, " once")
                                )
                            },
                            label = { Text("Lifetime ₹") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val cleanRate = rateInput.toDoubleOrNull()?.coerceIn(1.0, 10000.0) ?: 85.0
                            val m = ((draftConfig.monthlyAmount.toDoubleOrNull() ?: 2.99) * cleanRate).roundToInt().coerceAtLeast(1)
                            val y = ((draftConfig.yearlyAmount.toDoubleOrNull() ?: 19.99) * cleanRate).roundToInt().coerceAtLeast(1)
                            val l = ((draftConfig.lifetimeAmount.toDoubleOrNull() ?: 49.99) * cleanRate).roundToInt().coerceAtLeast(1)
                            draftConfig = draftConfig.copy(
                                paymentCurrency = "INR",
                                usdToInrRate = cleanRate,
                                monthlyAmount = m.toString(),
                                monthlyPriceLabel = "₹$m / month",
                                yearlyAmount = y.toString(),
                                yearlyPriceLabel = "₹$y / year",
                                lifetimeAmount = l.toString(),
                                lifetimePriceLabel = "₹$l once"
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Convert Current Amounts to ₹ INR & Preview")
                    }

                    Button(
                        onClick = {
                            val cleanRate = rateInput.toDoubleOrNull()?.coerceIn(1.0, 10000.0) ?: 85.0
                            onSaveGateway(
                                draftConfig.copy(paymentCurrency = "INR", usdToInrRate = cleanRate),
                                secretApiKey,
                                webhookSecret
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AdminElectricBlue)
                    ) {
                        Text("Save INR Prices Live")
                    }
                }
            }
        }

        // 4. Custom Subscription Plans Editor (/subscription_plans)
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = AdminSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AdminNeonCyan.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (editingPlanId == null) "Add / Edit INR Subscription Plans" else "Editing Plan",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Plan Name, Price ₹ (any amount), and Validity Days sync live to User App.",
                                style = MaterialTheme.typography.labelMedium,
                                color = AdminTextSecondary
                            )
                        }
                        if (plans.isEmpty()) {
                            TextButton(onClick = onSeedDefaultPlans) {
                                Text("Seed ₹ Plans", color = AdminNeonCyan)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = planNameInput,
                        onValueChange = { planNameInput = it.take(60) },
                        label = { Text("Plan Name (e.g. Pro 30 Days)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("plan_name_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = planPriceInrInput,
                            onValueChange = { planPriceInrInput = it.take(10) },
                            label = { Text("Price (₹ INR)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("plan_price_inr_input")
                        )
                        OutlinedTextField(
                            value = planValidityDaysInput,
                            onValueChange = { planValidityDaysInput = it.take(6) },
                            label = { Text("Validity Days") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("plan_validity_days_input")
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = planBadgeInput,
                            onValueChange = { planBadgeInput = it.take(24) },
                            label = { Text("Badge (optional)") },
                            placeholder = { Text("Best Value") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = planSortOrderInput,
                            onValueChange = { planSortOrderInput = it.take(4) },
                            label = { Text("Sort Order") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(0.7f)
                        )
                    }

                    OutlinedTextField(
                        value = planSubtitleInput,
                        onValueChange = { planSubtitleInput = it.take(180) },
                        label = { Text("Description / Benefits") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Visible in User App", color = Color.White)
                        Switch(checked = planIsActive, onCheckedChange = { planIsActive = it })
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (editingPlanId != null) {
                            OutlinedButton(
                                onClick = ::resetPlanForm,
                                modifier = Modifier.weight(0.5f)
                            ) {
                                Text("Cancel")
                            }
                        }
                        Button(
                            onClick = {
                                val price = planPriceInrInput.trim().toDoubleOrNull() ?: 199.0
                                val days = planValidityDaysInput.trim().toIntOrNull() ?: 30
                                val order = planSortOrderInput.trim().toIntOrNull() ?: (plans.size + 1)
                                onSavePlan(
                                    editingPlanId,
                                    planNameInput.ifBlank { "Pro ${days}d" },
                                    planSubtitleInput.ifBlank { "Auto-UPI Pro unlock for $days days" },
                                    price,
                                    days,
                                    planBadgeInput,
                                    order,
                                    planIsActive
                                )
                                resetPlanForm()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_plan_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = AdminRoyalPurple)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null)
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(if (editingPlanId == null) "Save Plan Live" else "Update Plan")
                        }
                    }
                }
            }
        }

        // 5. Existing Live Subscription Plans
        items(plans, key = { it.planId }) { plan ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AdminSurfaceElevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AdminBorderSubtle, RoundedCornerShape(18.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${plan.planName} • ₹${plan.priceInr.roundToInt()} / ${plan.validityDays} days",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${if (plan.isActive) "Active" else "Hidden"} • ${plan.subtitle}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (plan.isActive) AdminSuccess else AdminTextSecondary
                        )
                    }
                    Row {
                        IconButton(
                            onClick = {
                                editingPlanId = plan.planId
                                planNameInput = plan.planName
                                planPriceInrInput = plan.priceInr.roundToInt().toString()
                                planValidityDaysInput = plan.validityDays.toString()
                                planBadgeInput = plan.badge
                                planSubtitleInput = plan.subtitle
                                planSortOrderInput = plan.sortOrder.toString()
                                planIsActive = plan.isActive
                            }
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit plan", tint = AdminNeonCyan)
                        }
                        IconButton(onClick = { onDeletePlan(plan.planId) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete plan", tint = AdminDanger)
                        }
                    }
                }
            }
        }

        // 6. Contract Reference Card
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AdminSurfaceElevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AdminBorderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.Webhook, contentDescription = null, tint = AdminNeonCyan)
                        Text(
                            text = "FamGateway v2.0 Node.js Contract",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                    }
                    Text(
                        text = """
                        1) POST /api/create-order -> authenticates user, reads ₹ price from /subscription_plans, calls famgateway.in/api/create-order with Admin secret API key.
                        2) User App opens checkout_url inside In-App WebView (UPI intent:// / upi:// only; NEVER Chrome).
                        3) Webhook (/famgateway-webhook) + 3.5s polling (/api/verify-order.php) write:
                           subscriptionActive=true, planId, planName, planStartedAtMillis, planExpiresAtMillis to /users/{uid}.
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = AdminTextSecondary
                    )
                }
            }
        }
    }
}
