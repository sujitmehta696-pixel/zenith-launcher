package com.example.data.cloud

import android.content.Context
import com.example.BuildConfig
import com.example.R
import com.example.data.model.LauncherSettingsState
import com.example.data.model.PurchaseRecord
import com.example.data.model.PurchaseSource
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

class CloudAccountRepository(
    private val dbProvider: () -> FirebaseFirestore?,
    private val authProvider: () -> FirebaseAuth? = { runCatching { Firebase.auth }.getOrNull() }
) {
    constructor(
        db: FirebaseFirestore,
        auth: FirebaseAuth = Firebase.auth
    ) : this({ db }, { auth })

    constructor(context: Context) : this(
        dbProvider = {
            runCatching {
                FirebaseFirestore.getInstance(
                    context.applicationContext.getString(R.string.firestore_database_id)
                )
            }.getOrNull()
        }
    )

    private fun dbOrNull(): FirebaseFirestore? = runCatching { dbProvider() }.getOrNull()
    private fun authOrNull(): FirebaseAuth? = runCatching { authProvider() }.getOrNull()

    private fun requireUserId(): String {
        return runCatching { authOrNull()?.currentUser?.uid }.getOrNull()
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val db = dbOrNull() ?: run {
            emit(null)
            return@flow
        }
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch" }
        val docRef = db.collection("users").document(userId)
        val path = docRef.path
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (!snapshot.exists()) null
                    else snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && authOrNull()?.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    fun observePurchaseHistory(userId: String): Flow<List<PurchaseRecord>> = flow {
        val db = dbOrNull() ?: run {
            emit(emptyList())
            return@flow
        }
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch" }
        val path = "users/$userId/purchases"
        emitAll(
            db.collection("users")
                .document(userId)
                .collection("purchases")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(PurchaseRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && authOrNull()?.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureOrRegisterProfile(
        user: FirebaseUser,
        customDisplayName: String?,
        customHandle: String?,
        initialSettings: LauncherSettingsState
    ): Result<UserProfile> {
        val db = dbOrNull()
            ?: return Result.failure(IllegalStateException("Cloud database unavailable on this device."))
        val uid = user.uid
        val docRef = db.collection("users").document(uid)
        val path = docRef.path
        return try {
            require(uid == requireUserId()) { "Authenticated user mismatch" }
            val existingSnap = docRef.get().await()
            val cleanedName = customDisplayName
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.take(60)
                ?: user.displayName?.trim()?.takeIf { it.isNotEmpty() }?.take(60)
                ?: "OS Studio Member"
            val cleanedHandle = customHandle
                ?.trim()
                ?.removePrefix("@")
                ?.replace(Regex("[^a-zA-Z0-9_.\\-]"), "")
                ?.take(32)
                ?.takeIf { it.length >= 2 }
            val cleanEmail = user.email?.trim()?.takeIf { it.isNotEmpty() }?.take(120)

            if (!existingSnap.exists()) {
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to cleanedName,
                    "email" to cleanEmail,
                    "handle" to cleanedHandle,
                    "premiumActive" to false,
                    "purchaseSource" to PurchaseSource.NONE.name,
                    "activeThemeId" to initialSettings.activeThemeId.take(80),
                    "activeWallpaperId" to initialSettings.activeWallpaperId.take(80),
                    "launcherMode" to initialSettings.launcherMode.name,
                    "spinnerStyle" to initialSettings.spinnerStyle.name,
                    "swipeDownAction" to initialSettings.swipeDownAction.name,
                    "swipeUpAction" to initialSettings.swipeUpAction.name,
                    "doubleTapAction" to initialSettings.doubleTapAction.name,
                    "longPressAction" to initialSettings.longPressAction.name,
                    "pinchAction" to initialSettings.pinchAction.name,
                    "isActive" to true,
                    "appVersion" to BuildConfig.VERSION_NAME.take(32),
                    "lastActiveAt" to FieldValue.serverTimestamp(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }

                docRef.set(payload).await()
                val created = docRef.get().await()
                    .toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    ?: UserProfile(userId = uid, displayName = cleanedName, email = cleanEmail, handle = cleanedHandle)
                Result.success(created)
            } else {
                val updates = mutableMapOf<String, Any>(
                    "isActive" to true,
                    "appVersion" to BuildConfig.VERSION_NAME.take(32),
                    "lastActiveAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (!customDisplayName.isNullOrBlank()) {
                    updates["displayName"] = cleanedName
                }
                if (cleanedHandle != null) {
                    updates["handle"] = cleanedHandle
                }
                if (cleanEmail != null) {
                    updates["email"] = cleanEmail
                }
                docRef.update(updates).await()
                val profile = docRef.get().await()
                    .toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    ?: UserProfile(userId = uid, displayName = cleanedName)
                Result.success(profile)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            handleFirestoreError(t, OperationType.WRITE, path)
            Result.failure(t)
        }
    }

    suspend fun syncCustomizationSettings(settings: LauncherSettingsState): Result<Unit> {
        val db = dbOrNull()
            ?: return Result.failure(IllegalStateException("Cloud database unavailable on this device."))
        var docPath = "users/${authOrNull()?.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "activeThemeId" to settings.activeThemeId.take(80),
                    "activeWallpaperId" to settings.activeWallpaperId.take(80),
                    "launcherMode" to settings.launcherMode.name,
                    "spinnerStyle" to settings.spinnerStyle.name,
                    "swipeDownAction" to settings.swipeDownAction.name,
                    "swipeUpAction" to settings.swipeUpAction.name,
                    "doubleTapAction" to settings.doubleTapAction.name,
                    "longPressAction" to settings.longPressAction.name,
                    "pinchAction" to settings.pinchAction.name,
                    "isActive" to true,
                    "appVersion" to BuildConfig.VERSION_NAME.take(32),
                    "lastActiveAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            handleFirestoreError(t, OperationType.UPDATE, docPath)
            Result.failure(t)
        }
    }

    suspend fun syncPremiumEntitlement(
        productId: String,
        source: PurchaseSource,
        rawPurchaseToken: String? = null,
        planName: String? = null,
        planStartedAtMillis: Long? = null,
        planExpiresAtMillis: Long? = null
    ): Result<Unit> {
        val db = dbOrNull()
            ?: return Result.failure(IllegalStateException("Cloud database unavailable on this device."))
        var docPath = "users/${authOrNull()?.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val cleanProductId = productId.trim().take(64).ifBlank { "android_launcher_pro_yearly" }
            val allowedSource = when (source) {
                PurchaseSource.GOOGLE_PLAY,
                PurchaseSource.PAYMENT_GATEWAY -> source
                else -> return Result.failure(IllegalStateException("Only real verified payments can be recorded."))
            }
            val tokenHash = rawPurchaseToken
                ?.takeIf { it.isNotBlank() }
                ?.let(::sha256)
                ?.take(64)

            val userRef = db.collection("users").document(uid)
            docPath = userRef.path
            val entitlementUpdates = mutableMapOf<String, Any>(
                "premiumActive" to true,
                "subscriptionActive" to true,
                "planId" to cleanProductId,
                "activeProductId" to cleanProductId,
                "purchaseSource" to allowedSource.name,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            planName?.trim()?.takeIf { it.isNotBlank() }?.take(60)?.let {
                entitlementUpdates["planName"] = it
            }
            if (planStartedAtMillis != null && planStartedAtMillis >= 0L) {
                entitlementUpdates["planStartedAtMillis"] = planStartedAtMillis
            }
            if (planExpiresAtMillis != null && planExpiresAtMillis >= 0L) {
                entitlementUpdates["planExpiresAtMillis"] = planExpiresAtMillis
            }
            if (tokenHash != null) {
                entitlementUpdates["purchaseTokenHash"] = tokenHash
            }
            userRef.update(entitlementUpdates).await()

            val receiptId = "rcpt_${System.currentTimeMillis()}"
            val receiptRef = userRef.collection("purchases").document(receiptId)
            val receiptPayload = mapOf(
                "userId" to uid,
                "productId" to cleanProductId,
                "purchaseSource" to allowedSource.name,
                "purchaseTokenHash" to tokenHash,
                "createdAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            receiptRef.set(receiptPayload).await()

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            handleFirestoreError(t, OperationType.UPDATE, docPath)
            Result.failure(t)
        }
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
