package com.example.data.billing

import com.example.data.model.RemoteAppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLEncoder

data class GatewayCheckoutSession(
    val checkoutUrl: String,
    val orderId: String
)

data class GatewayVerifyResponse(
    val isPaidAndVerified: Boolean,
    val transactionId: String,
    val message: String,
    val planId: String? = null,
    val planName: String? = null,
    val planStartedAtMillis: Long? = null,
    val planExpiresAtMillis: Long? = null,
    val subscriptionActive: Boolean = false
)

class PaymentGatewayClient {

    suspend fun createCheckoutSession(
        config: RemoteAppConfig,
        userId: String,
        userEmail: String?,
        displayName: String? = null,
        productId: String,
        amount: String,
        firebaseIdToken: String?
    ): Result<GatewayCheckoutSession> = withContext(Dispatchers.IO) {
        val createUrl = resolveCreateOrderUrl(config.paymentBackendUrl)
        if (!createUrl.startsWith("https://") && !createUrl.startsWith("http://")) {
            return@withContext Result.failure(
                IllegalStateException("Admin has not configured a valid FamGateway Backend URL yet.")
            )
        }

        runCatching {
            val payload = JSONObject().apply {
                put("userId", userId)
                put("email", userEmail.orEmpty())
                put("displayName", displayName.orEmpty())
                put("productId", productId)
                put("planId", productId)
                put("amount", amount)
                put("currency", "INR")
                put("publicKey", config.paymentPublicKey)
                put("provider", config.paymentProviderName)
                if (!firebaseIdToken.isNullOrBlank()) {
                    put("firebaseIdToken", firebaseIdToken)
                }
            }

            val conn = (URL(createUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 20_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
                if (!firebaseIdToken.isNullOrBlank()) {
                    setRequestProperty("Authorization", "Bearer $firebaseIdToken")
                }
            }

            conn.outputStream.use { os ->
                os.write(payload.toString().toByteArray(Charsets.UTF_8))
            }

            val code = conn.responseCode
            val contentType = conn.contentType.orEmpty().lowercase()
            val body = runCatching {
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }.getOrDefault("")
            conn.disconnect()

            if (code in 200..299 && body.trimStart().startsWith("{")) {
                val json = JSONObject(body)
                val checkoutUrl = json.optString("checkout_url")
                    .ifBlank { json.optString("checkoutUrl") }
                    .ifBlank { json.optString("payment_url") }
                    .ifBlank { json.optString("paymentUrl") }
                    .ifBlank { json.optString("url") }
                if (checkoutUrl.isBlank()) {
                    val errMsg = json.optString("message").ifBlank { json.optString("error") }
                    throw IllegalStateException(
                        errMsg.ifBlank { "Backend did not return a FamGateway checkout_url." }
                    )
                }
                val orderId = json.optString("order_id")
                    .ifBlank { json.optString("orderId") }
                    .ifBlank { json.optString("sessionId") }
                    .ifBlank { "ord_${System.currentTimeMillis()}" }
                GatewayCheckoutSession(checkoutUrl = checkoutUrl, orderId = orderId)
            } else if (code in 200..399 && contentType.contains("text/html")) {
                GatewayCheckoutSession(
                    checkoutUrl = buildQueryCheckoutUrl(createUrl, userId, productId, amount, "INR"),
                    orderId = "ord_${System.currentTimeMillis()}"
                )
            } else if (code == 405) {
                GatewayCheckoutSession(
                    checkoutUrl = buildQueryCheckoutUrl(createUrl, userId, productId, amount, "INR"),
                    orderId = "ord_${System.currentTimeMillis()}"
                )
            } else {
                val errMsg = runCatching {
                    JSONObject(body).optString("message").ifBlank { JSONObject(body).optString("error") }
                }.getOrDefault("")
                throw IllegalStateException(
                    errMsg.ifBlank { "Gateway endpoint returned HTTP $code." }
                )
            }
        }
    }

    suspend fun verifyPaymentStatus(
        config: RemoteAppConfig,
        userId: String,
        orderId: String,
        paymentId: String?,
        productId: String,
        firebaseIdToken: String?
    ): Result<GatewayVerifyResponse> = withContext(Dispatchers.IO) {
        val cleanOrderId = orderId.trim()
        val verifyUrl = resolveVerifyOrderUrl(config, cleanOrderId)
        if (!verifyUrl.startsWith("https://") && !verifyUrl.startsWith("http://")) {
            return@withContext Result.success(
                GatewayVerifyResponse(
                    isPaidAndVerified = false,
                    transactionId = "",
                    message = "Waiting for FamGateway webhook confirmation…"
                )
            )
        }

        runCatching {
            val payload = JSONObject().apply {
                put("userId", userId)
                put("orderId", cleanOrderId)
                put("order_id", cleanOrderId)
                put("paymentId", paymentId.orEmpty())
                put("productId", productId)
                put("planId", productId)
                if (!firebaseIdToken.isNullOrBlank()) {
                    put("firebaseIdToken", firebaseIdToken)
                }
            }

            val conn = (URL(verifyUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 20_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
                if (!firebaseIdToken.isNullOrBlank()) {
                    setRequestProperty("Authorization", "Bearer $firebaseIdToken")
                }
            }

            conn.outputStream.use { os ->
                os.write(payload.toString().toByteArray(Charsets.UTF_8))
            }

            val code = conn.responseCode
            val body = runCatching {
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }.getOrDefault("")
            conn.disconnect()

            if (code !in 200..299 || !body.trimStart().startsWith("{")) {
                return@runCatching GatewayVerifyResponse(
                    isPaidAndVerified = false,
                    transactionId = "",
                    message = "Waiting for UPI payment confirmation (HTTP $code)…"
                )
            }

            val json = JSONObject(body)
            val statusStr = json.optString("status").lowercase()
            val paidFlag = json.optBoolean("paid", false)
            val verifiedFlag = json.optBoolean("verified", false)
            val subscriptionActive = json.optBoolean("subscriptionActive", false)
            val statusPaid = statusStr in setOf("paid", "captured", "succeeded", "success", "completed", "active")
            val txId = json.optString("transactionId")
                .ifBlank { json.optString("transaction_id") }
                .ifBlank { json.optString("utr") }
                .ifBlank { json.optString("paymentId") }
                .ifBlank { paymentId.orEmpty() }
                .ifBlank { cleanOrderId }

            val confirmed = (subscriptionActive || (verifiedFlag && paidFlag) || (verifiedFlag && statusPaid) || (paidFlag && statusPaid)) &&
                txId.isNotBlank()

            val planId = json.optString("planId").takeIf { it.isNotBlank() }
            val planName = json.optString("planName").takeIf { it.isNotBlank() }
            val startedAt = json.optLong("planStartedAtMillis", -1L).takeIf { it > 0L }
            val expiresAt = json.optLong("planExpiresAtMillis", -1L).takeIf { it > 0L }

            GatewayVerifyResponse(
                isPaidAndVerified = confirmed,
                transactionId = if (confirmed) txId else "",
                message = json.optString("message").ifBlank {
                    if (confirmed) "Payment verified! Subscription activated automatically."
                    else "Waiting for Auto-UPI payment confirmation…"
                },
                planId = planId,
                planName = planName,
                planStartedAtMillis = startedAt,
                planExpiresAtMillis = expiresAt,
                subscriptionActive = subscriptionActive || confirmed
            )
        }
    }

    private fun resolveCreateOrderUrl(rawBackendUrl: String): String {
        val trimmed = rawBackendUrl.trim().trimEnd('/')
        if (!trimmed.startsWith("https://") && !trimmed.startsWith("http://")) return ""
        val parsed = runCatching { URI(trimmed) }.getOrNull() ?: return trimmed
        val path = parsed.path.orEmpty().lowercase()
        return if (
            path.endsWith("/create-order") ||
            path.endsWith("/create-checkout") ||
            path.endsWith(".php")
        ) {
            trimmed
        } else {
            "$trimmed/api/create-order"
        }
    }

    private fun resolveVerifyOrderUrl(config: RemoteAppConfig, orderId: String): String {
        val rawVerify = config.paymentVerifyUrl.trim().trimEnd('/')
        val baseUrl = if (rawVerify.startsWith("https://") || rawVerify.startsWith("http://")) {
            rawVerify
        } else {
            val backend = config.paymentBackendUrl.trim().trimEnd('/')
            if (!backend.startsWith("https://") && !backend.startsWith("http://")) {
                ""
            } else {
                val uri = runCatching { URI(backend) }.getOrNull()
                if (uri != null && !uri.host.isNullOrBlank()) {
                    val portPart = if (uri.port != -1) ":${uri.port}" else ""
                    "${uri.scheme}://${uri.host}$portPart/api/verify-order.php"
                } else {
                    "$backend/api/verify-order.php"
                }
            }
        }
        if (baseUrl.isBlank()) return ""
        return if (orderId.isNotBlank() && !baseUrl.contains("order_id=")) {
            val sep = if (baseUrl.contains("?")) "&" else "?"
            "$baseUrl${sep}order_id=${URLEncoder.encode(orderId, "UTF-8")}"
        } else {
            baseUrl
        }
    }

    private fun buildQueryCheckoutUrl(
        baseUrl: String,
        userId: String,
        productId: String,
        amount: String,
        currency: String
    ): String {
        val sep = if (baseUrl.contains("?")) "&" else "?"
        fun enc(v: String) = URLEncoder.encode(v, "UTF-8")
        return "$baseUrl${sep}userId=${enc(userId)}&productId=${enc(productId)}&amount=${enc(amount)}&currency=${enc(currency)}"
    }
}
