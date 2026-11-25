package com.example.admin.data

import android.content.Context
import android.util.Log
import com.example.admin.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AdminFirestoreRepository(private val context: Context) {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id))
    }

    companion object {
        private const val TAG = "AdminFirestoreRepo"
    }

    private fun requireUid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("Admin must be signed in.")

    fun observeCategories(): Flow<Result<List<CategoryItem>>> = callbackFlow {
        val registration = firestore.collection("wallpaper_categories")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "observeCategories failed", error)
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val items = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    doc.toCategoryItem()
                }.sortedWith(compareBy<CategoryItem> { it.sortOrder }.thenBy { it.name })
                trySend(Result.success(items))
            }
        awaitClose { registration.remove() }
    }

    fun observeWallpapers(): Flow<Result<List<RemoteWallpaperItem>>> = callbackFlow {
        val registration = firestore.collection("remote_wallpapers")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "observeWallpapers failed", error)
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val items = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    doc.toRemoteWallpaperItem()
                }.sortedByDescending { it.updatedAtMillis }
                trySend(Result.success(items))
            }
        awaitClose { registration.remove() }
    }

    fun observeUsers(): Flow<Result<List<UserControlItem>>> = callbackFlow {
        val registration = firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "observeUsers failed", error)
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val items = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    doc.toUserControlItem()
                }.sortedByDescending { it.lastActiveMillis }
                trySend(Result.success(items))
            }
        awaitClose { registration.remove() }
    }

    fun observeAdmins(): Flow<Result<List<AdminRoleItem>>> = callbackFlow {
        val registration = firestore.collection("admins")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "observeAdmins failed", error)
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val items = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    AdminRoleItem(
                        adminUid = doc.getString("adminUid") ?: doc.id,
                        email = doc.getString("email").orEmpty(),
                        displayName = doc.getString("displayName").orEmpty(),
                        role = doc.getString("role") ?: "CONTENT_ADMIN",
                        addedByUid = doc.getString("addedByUid").orEmpty()
                    )
                }.sortedBy { it.email }
                trySend(Result.success(items))
            }
        awaitClose { registration.remove() }
    }

    fun observeGlobalConfig(): Flow<Result<RemoteAppConfig>> = callbackFlow {
        val registration = firestore.collection("app_config").document("global")
            .addSnapshotListener { doc, error ->
                if (error != null) {
                    Log.e(TAG, "observeGlobalConfig failed", error)
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val config = if (doc != null && doc.exists()) {
                    RemoteAppConfig(
                        maintenanceMode = doc.getBoolean("maintenanceMode") ?: false,
                        maintenanceMessage = doc.getString("maintenanceMessage")
                            ?: "Scheduled maintenance is in progress.",
                        announcementBanner = doc.getString("announcementBanner").orEmpty(),
                        aiSearchEnabled = doc.getBoolean("aiSearchEnabled") ?: true,
                        liveWallpapersEnabled = doc.getBoolean("liveWallpapersEnabled") ?: true,
                        edgePanelEnabled = doc.getBoolean("edgePanelEnabled") ?: true,
                        controlCenterEnabled = doc.getBoolean("controlCenterEnabled") ?: true,
                        notificationBadgesEnabled = doc.getBoolean("notificationBadgesEnabled") ?: true,
                        paymentGatewayEnabled = doc.getBoolean("paymentGatewayEnabled") ?: false,
                        paymentProviderName = doc.getString("paymentProviderName") ?: "FamGateway v2.0",
                        paymentBackendUrl = doc.getString("paymentBackendUrl").orEmpty(),
                        paymentVerifyUrl = doc.getString("paymentVerifyUrl").orEmpty(),
                        paymentPublicKey = doc.getString("paymentPublicKey").orEmpty(),
                        paymentCurrency = doc.getString("paymentCurrency") ?: "INR",
                        monthlyPriceLabel = doc.getString("monthlyPriceLabel") ?: "₹199 / month",
                        yearlyPriceLabel = doc.getString("yearlyPriceLabel") ?: "₹1,499 / year",
                        lifetimePriceLabel = doc.getString("lifetimePriceLabel") ?: "₹3,999 one-time",
                        monthlyAmount = doc.getString("monthlyAmount") ?: "199",
                        yearlyAmount = doc.getString("yearlyAmount") ?: "1499",
                        lifetimeAmount = doc.getString("lifetimeAmount") ?: "3999",
                        usdToInrRate = (doc.get("usdToInrRate") as? Number)?.toDouble() ?: 85.0,
                        updatedByUid = doc.getString("updatedByUid").orEmpty(),
                        updatedAtMillis = doc.getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            ?.toDate()?.time ?: 0L
                    )
                } else {
                    RemoteAppConfig()
                }
                trySend(Result.success(config))
            }
        awaitClose { registration.remove() }
    }

    fun observePaymentSecrets(): Flow<Result<PaymentGatewaySecrets>> = callbackFlow {
        val registration = firestore.collection("admin_secrets").document("payment_gateway")
            .addSnapshotListener { doc, error ->
                if (error != null) {
                    Log.e(TAG, "observePaymentSecrets failed", error)
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val secrets = if (doc != null && doc.exists()) {
                    PaymentGatewaySecrets(
                        providerName = doc.getString("providerName") ?: "Custom Webhook",
                        secretApiKey = doc.getString("secretApiKey").orEmpty(),
                        webhookSecret = doc.getString("webhookSecret").orEmpty(),
                        updatedByUid = doc.getString("updatedByUid").orEmpty(),
                        updatedAtMillis = doc.getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            ?.toDate()?.time ?: 0L
                    )
                } else {
                    PaymentGatewaySecrets()
                }
                trySend(Result.success(secrets))
            }
        awaitClose { registration.remove() }
    }

    fun observeSubscriptionPlans(): Flow<Result<List<SubscriptionPlanItem>>> = callbackFlow {
        val registration = firestore.collection("subscription_plans")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "observeSubscriptionPlans failed", error)
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val items = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    doc.toSubscriptionPlanItem()
                }.sortedWith(compareBy<SubscriptionPlanItem> { it.sortOrder }.thenBy { it.priceInr })
                trySend(Result.success(items))
            }
        awaitClose { registration.remove() }
    }

    suspend fun saveCategory(
        categoryId: String?,
        name: String,
        description: String,
        sortOrder: Int,
        isActive: Boolean
    ): Result<String> {
        return try {
            requireUid()
            val cleanName = name.trim().take(40)
            require(cleanName.isNotBlank()) { "Category name cannot be empty." }
            val cleanDesc = description.trim().take(160)
            val safeOrder = sortOrder.coerceIn(0, 10000)

            if (categoryId.isNullOrBlank()) {
                val slug = cleanName.lowercase()
                    .replace(Regex("[^a-z0-9]+"), "_")
                    .trim('_')
                    .take(24)
                    .ifBlank { "cat" }
                val newId = "cat_${slug}_${UUID.randomUUID().toString().take(6)}"
                val payload = hashMapOf<String, Any>(
                    "categoryId" to newId,
                    "name" to cleanName,
                    "description" to cleanDesc,
                    "sortOrder" to safeOrder,
                    "isActive" to isActive,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                firestore.collection("wallpaper_categories").document(newId).set(payload).await()
                Result.success(newId)
            } else {
                val updates = hashMapOf<String, Any>(
                    "name" to cleanName,
                    "description" to cleanDesc,
                    "sortOrder" to safeOrder,
                    "isActive" to isActive,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                firestore.collection("wallpaper_categories").document(categoryId).update(updates).await()
                Result.success(categoryId)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(friendlyError(e))
        }
    }

    suspend fun seedDefaultCategories(): Result<Int> {
        return try {
            requireUid()
            val existing = firestore.collection("wallpaper_categories").get().await()
            val existingIds = existing.documents.map { it.id }.toSet()
            var createdCount = 0
            DEFAULT_LAUNCHER_CATEGORIES.forEachIndexed { idx, (id, name, desc) ->
                if (!existingIds.contains(id)) {
                    val payload = hashMapOf<String, Any>(
                        "categoryId" to id,
                        "name" to name,
                        "description" to desc,
                        "sortOrder" to (idx + 1),
                        "isActive" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    firestore.collection("wallpaper_categories").document(id).set(payload).await()
                    createdCount++
                }
            }
            Result.success(createdCount)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(friendlyError(e))
        }
    }

    suspend fun deleteCategory(categoryId: String): Result<Unit> {
        return try {
            requireUid()
            firestore.collection("wallpaper_categories").document(categoryId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(friendlyError(e))
        }
    }

    suspend fun createWallpaperRecord(
        wallpaperId: String,
        title: String,
        description: String,
        categoryId: String,
        categoryName: String,
        mediaType: String,
        mediaUrl: String,
        thumbnailUrl: String,
        storagePath: String,
        thumbnailStoragePath: String?,
        resolutionLabel: String,
        fileSizeBytes: Long,
        isPremium: Boolean,
        isPublished: Boolean
    ): Result<Unit> {
        return try {
            val uid = requireUid()
            val payload = hashMapOf<String, Any>(
                "wallpaperId" to wallpaperId,
                "title" to title.trim().ifBlank { "Studio Wallpaper" }.take(80),
                "description" to description.trim().take(400),
                "categoryId" to categoryId,
                "categoryName" to categoryName.trim().ifBlank { "3D" }.take(40),
                "mediaType" to mediaType,
                "mediaUrl" to mediaUrl,
                "thumbnailUrl" to thumbnailUrl,
                "storagePath" to storagePath,
                "resolutionLabel" to resolutionLabel.take(40),
                "fileSizeBytes" to fileSizeBytes.coerceIn(0L, 100_000_000L),
                "isPremium" to isPremium,
                "isPublished" to isPublished,
                "downloadCount" to 0L,
                "uploadedByUid" to uid,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!thumbnailStoragePath.isNullOrBlank()) {
                payload["thumbnailStoragePath"] = thumbnailStoragePath
            }
            firestore.collection("remote_wallpapers").document(wallpaperId).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(friendlyError(e))
        }
    }

    suspend fun updateWallpaperPublishAndTier(
        wallpaperId: String,
        isPublished: Boolean,
        isPremium: Boolean
    ): Result<Unit> {
        return try {
            requireUid()
            val updates = hashMapOf<String, Any>(
                "isPublished" to isPublished,
                "isPremium" to isPremium,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("remote_wallpapers").document(wallpaperId).update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(friendlyError(e))
        }
    }

    suspend fun deleteWallpaperDoc(wallpaperId: String): Result<Unit> {
        return try {
            requireUid()
            firestore.collection("remote_wallpapers").document(wallpaperId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(friendlyError(e))
        }
    }

    suspend fun updateUserModeration(
        userId: String,
        premiumActive: Boolean,
        isBlocked: Boolean
    ): Result<Unit> {
        return try {
            requireUid()
            val now = System.currentTimeMillis()
            val updates = hashMapOf<String, Any>(
                "premiumActive" to premiumActive,
                "subscriptionActive" to premiumActive,
                "planId" to if (premiumActive) "admin_pro_grant" else "free_tier",
                "planName" to if (premiumActive) "Admin Pro Grant" else "Free Tier",
                "planStartedAtMillis" to if (premiumActive) now else 0L,
                "planExpiresAtMillis" to if (premiumActive) (now + 365L * 86_400_000L) else 0L,
                "activeProductId" to if (premiumActive) "admin_pro_grant" else "free_tier",
                "purchaseSource" to if (premiumActive) "ADMIN_GRANT" else "NONE",
                "isBlocked" to isBlocked,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("users").document(userId).update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(friendlyError(e))
        }
    }

    suspend fun addAdminRole(
        targetUid: String,
        email: String,
        displayName: String,
        role: String
    ): Result<Unit> {
        return try {
            val uid = requireUid()
            val cleanUid = targetUid.trim()
            val cleanEmail = email.trim()
            require(cleanUid.matches(Regex("^[a-zA-Z0-9_\\-]{1,128}$"))) {
                "Enter a valid Firebase UID (letters, numbers, -, _)."
            }
            require(cleanEmail.length in 3..120) { "Enter a valid email address." }
            val payload = hashMapOf<String, Any>(
                "adminUid" to cleanUid,
                "email" to cleanEmail,
                "displayName" to displayName.trim().ifBlank { "Admin" }.take(60),
                "role" to if (role == "SUPER_ADMIN") "SUPER_ADMIN" else "CONTENT_ADMIN",
                "addedByUid" to uid,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("admins").document(cleanUid).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(friendlyError(e))
        }
    }

    suspend fun removeAdminRole(adminUid: String): Result<Unit> {
        return try {
            requireUid()
            firestore.collection("admins").document(adminUid).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(friendlyError(e))
        }
    }

    suspend fun saveGlobalConfig(config: RemoteAppConfig): Result<Unit> {
        return try {
            val uid = requireUid()
            val docRef = firestore.collection("app_config").document("global")
            val payload = hashMapOf<String, Any>(
                "configId" to "global",
                "maintenanceMode" to config.maintenanceMode,
                "maintenanceMessage" to config.maintenanceMessage.trim().take(240),
                "announcementBanner" to config.announcementBanner.trim().take(240),
                "aiSearchEnabled" to config.aiSearchEnabled,
                "liveWallpapersEnabled" to config.liveWallpapersEnabled,
                "edgePanelEnabled" to config.edgePanelEnabled,
                "controlCenterEnabled" to config.controlCenterEnabled,
                "notificationBadgesEnabled" to config.notificationBadgesEnabled,
                "paymentGatewayEnabled" to config.paymentGatewayEnabled,
                "paymentProviderName" to config.paymentProviderName.trim().ifBlank { "Custom Webhook" }.take(40),
                "paymentBackendUrl" to config.paymentBackendUrl.trim().take(500),
                "paymentVerifyUrl" to config.paymentVerifyUrl.trim().take(500),
                "paymentPublicKey" to config.paymentPublicKey.trim().take(200),
                "paymentCurrency" to config.paymentCurrency.trim().ifBlank { "INR" }.take(8),
                "monthlyPriceLabel" to config.monthlyPriceLabel.trim().ifBlank { "₹199 / month" }.take(24),
                "yearlyPriceLabel" to config.yearlyPriceLabel.trim().ifBlank { "₹1,499 / year" }.take(24),
                "lifetimePriceLabel" to config.lifetimePriceLabel.trim().ifBlank { "₹3,999 one-time" }.take(24),
                "monthlyAmount" to config.monthlyAmount.trim().ifBlank { "199" }.take(16),
                "yearlyAmount" to config.yearlyAmount.trim().ifBlank { "1499" }.take(16),
                "lifetimeAmount" to config.lifetimeAmount.trim().ifBlank { "3999" }.take(16),
                "usdToInrRate" to config.usdToInrRate.coerceIn(1.0, 10000.0),
                "updatedByUid" to uid,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(friendlyError(e))
        }
    }

    suspend fun savePaymentSecrets(
        providerName: String,
        secretApiKey: String,
        webhookSecret: String
    ): Result<Unit> {
        return try {
            val uid = requireUid()
            val payload = hashMapOf<String, Any>(
                "secretId" to "payment_gateway",
                "providerName" to providerName.trim().ifBlank { "FamGateway v2.0" }.take(40),
                "secretApiKey" to secretApiKey.trim().take(300),
                "webhookSecret" to webhookSecret.trim().take(300),
                "updatedByUid" to uid,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("admin_secrets").document("payment_gateway").set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(friendlyError(e))
        }
    }

    suspend fun saveSubscriptionPlan(
        planId: String?,
        planName: String,
        subtitle: String,
        priceInr: Double,
        validityDays: Int,
        badge: String,
        sortOrder: Int,
        isActive: Boolean
    ): Result<String> {
        return try {
            val uid = requireUid()
            val cleanName = planName.trim().take(60)
            require(cleanName.isNotBlank()) { "Plan Name cannot be empty." }
            val safePrice = priceInr.coerceIn(1.0, 1_000_000.0)
            val safeDays = validityDays.coerceIn(1, 36_500)
            val cleanSubtitle = subtitle.trim().take(180)
            val cleanBadge = badge.trim().take(24)
            val safeOrder = sortOrder.coerceIn(0, 10000)

            if (planId.isNullOrBlank()) {
                val slug = cleanName.lowercase()
                    .replace(Regex("[^a-z0-9]+"), "_")
                    .trim('_')
                    .take(24)
                    .ifBlank { "plan" }
                val newId = "plan_${slug}_${UUID.randomUUID().toString().take(6)}"
                val payload = hashMapOf<String, Any>(
                    "planId" to newId,
                    "planName" to cleanName,
                    "subtitle" to cleanSubtitle,
                    "priceInr" to safePrice,
                    "validityDays" to safeDays,
                    "badge" to cleanBadge,
                    "sortOrder" to safeOrder,
                    "isActive" to isActive,
                    "updatedByUid" to uid,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                firestore.collection("subscription_plans").document(newId).set(payload).await()
                Result.success(newId)
            } else {
                val updates = hashMapOf<String, Any>(
                    "planName" to cleanName,
                    "subtitle" to cleanSubtitle,
                    "priceInr" to safePrice,
                    "validityDays" to safeDays,
                    "badge" to cleanBadge,
                    "sortOrder" to safeOrder,
                    "isActive" to isActive,
                    "updatedByUid" to uid,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                firestore.collection("subscription_plans").document(planId).update(updates).await()
                Result.success(planId)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(friendlyError(e))
        }
    }

    suspend fun deleteSubscriptionPlan(planId: String): Result<Unit> {
        return try {
            requireUid()
            firestore.collection("subscription_plans").document(planId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(friendlyError(e))
        }
    }

    suspend fun seedDefaultSubscriptionPlansInr(): Result<Int> {
        return try {
            val uid = requireUid()
            val defaults = listOf(
                mapOf(
                    "planId" to "android_launcher_pro_monthly",
                    "planName" to "Pro Monthly",
                    "subtitle" to "Full access to Pro 3D Sphere, 4K live videos & Edge Panel",
                    "priceInr" to 199.0,
                    "validityDays" to 30,
                    "badge" to "Popular",
                    "sortOrder" to 1
                ),
                mapOf(
                    "planId" to "android_launcher_pro_yearly",
                    "planName" to "Pro Annual",
                    "subtitle" to "365 days Pro unlock with all themes & zero ads",
                    "priceInr" to 1499.0,
                    "validityDays" to 365,
                    "badge" to "Best Value",
                    "sortOrder" to 2
                ),
                mapOf(
                    "planId" to "android_launcher_pro_lifetime",
                    "planName" to "Studio Lifetime",
                    "subtitle" to "Long-term permanent Pro Studio access",
                    "priceInr" to 3999.0,
                    "validityDays" to 3650,
                    "badge" to "Forever",
                    "sortOrder" to 3
                )
            )
            var created = 0
            defaults.forEach { entry ->
                val id = entry["planId"] as String
                val docRef = firestore.collection("subscription_plans").document(id)
                val snap = docRef.get().await()
                if (!snap.exists()) {
                    docRef.set(
                        entry + mapOf(
                            "isActive" to true,
                            "updatedByUid" to uid,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                    created++
                }
            }
            Result.success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(friendlyError(e))
        }
    }

    private fun DocumentSnapshot.toCategoryItem(): CategoryItem? {
        val name = getString("name") ?: return null
        return CategoryItem(
            categoryId = getString("categoryId") ?: id,
            name = name,
            description = getString("description").orEmpty(),
            sortOrder = (get("sortOrder") as? Number)?.toInt() ?: 0,
            isActive = getBoolean("isActive") ?: true,
            updatedAtMillis = getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?.toDate()?.time ?: 0L
        )
    }

    private fun DocumentSnapshot.toRemoteWallpaperItem(): RemoteWallpaperItem? {
        val title = getString("title") ?: return null
        return RemoteWallpaperItem(
            wallpaperId = getString("wallpaperId") ?: id,
            title = title,
            description = getString("description").orEmpty(),
            categoryId = getString("categoryId").orEmpty(),
            categoryName = getString("categoryName").orEmpty(),
            mediaType = getString("mediaType") ?: "IMAGE",
            mediaUrl = getString("mediaUrl").orEmpty(),
            thumbnailUrl = getString("thumbnailUrl").orEmpty(),
            storagePath = getString("storagePath").orEmpty(),
            thumbnailStoragePath = getString("thumbnailStoragePath"),
            resolutionLabel = getString("resolutionLabel") ?: "HD",
            fileSizeBytes = (get("fileSizeBytes") as? Number)?.toLong() ?: 0L,
            isPremium = getBoolean("isPremium") ?: false,
            isPublished = getBoolean("isPublished") ?: true,
            downloadCount = (get("downloadCount") as? Number)?.toLong() ?: 0L,
            uploadedByUid = getString("uploadedByUid").orEmpty(),
            updatedAtMillis = getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?.toDate()?.time ?: 0L
        )
    }

    private fun DocumentSnapshot.toUserControlItem(): UserControlItem {
        return UserControlItem(
            userId = getString("userId") ?: id,
            displayName = getString("displayName") ?: "User",
            email = getString("email").orEmpty(),
            premiumActive = getBoolean("premiumActive") ?: false,
            purchaseSource = getString("purchaseSource") ?: "NONE",
            isActive = getBoolean("isActive") ?: false,
            isBlocked = getBoolean("isBlocked") ?: false,
            activeThemeId = getString("activeThemeId").orEmpty(),
            activeWallpaperId = getString("activeWallpaperId").orEmpty(),
            spinnerStyle = getString("spinnerStyle") ?: "ORBITAL_DIAL",
            lastActiveMillis = getTimestamp("lastActiveAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?.toDate()?.time
                ?: getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    ?.toDate()?.time
                ?: 0L,
            subscriptionActive = getBoolean("subscriptionActive") ?: false,
            planName = getString("planName").orEmpty(),
            planExpiresAtMillis = (get("planExpiresAtMillis") as? Number)?.toLong() ?: 0L
        )
    }

    private fun DocumentSnapshot.toSubscriptionPlanItem(): SubscriptionPlanItem? {
        val name = getString("planName") ?: return null
        return SubscriptionPlanItem(
            planId = getString("planId") ?: id,
            planName = name,
            subtitle = getString("subtitle").orEmpty(),
            priceInr = (get("priceInr") as? Number)?.toDouble() ?: 199.0,
            validityDays = (get("validityDays") as? Number)?.toInt() ?: 30,
            badge = getString("badge").orEmpty(),
            sortOrder = (get("sortOrder") as? Number)?.toInt() ?: 0,
            isActive = getBoolean("isActive") ?: true,
            updatedAtMillis = getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?.toDate()?.time ?: 0L
        )
    }

    private fun friendlyError(e: Exception): Exception {
        val msg = if (e is FirebaseFirestoreException) {
            "Firestore error (${e.code.name}): ${e.localizedMessage}"
        } else {
            e.localizedMessage ?: "Firestore operation failed."
        }
        return IllegalStateException(msg, e)
    }
}
