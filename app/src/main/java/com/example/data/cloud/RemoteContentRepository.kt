package com.example.data.cloud

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.example.LiveOrbitVideoWallpaperService
import com.example.R
import com.example.data.model.ProceduralStyle
import com.example.data.model.RemoteAppConfig
import com.example.data.model.RemoteCategory
import com.example.data.model.RemoteSubscriptionPlan
import com.example.data.model.WallpaperCategory
import com.example.data.model.WallpaperItem
import com.example.data.model.WallpaperSourceType
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.abs

class RemoteContentRepository(
    private val context: Context,
    private val dbProvider: () -> FirebaseFirestore? = {
        runCatching {
            FirebaseFirestore.getInstance(
                context.applicationContext.getString(R.string.firestore_database_id)
            )
        }.getOrNull()
    },
    private val authProvider: () -> FirebaseAuth? = { runCatching { Firebase.auth }.getOrNull() }
) {
    private val remoteDownloadsDir: File by lazy {
        File(context.applicationContext.filesDir, "remote_wallpapers").apply { mkdirs() }
    }

    private fun isSignedIn(): Boolean = runCatching { authProvider()?.currentUser != null }.getOrDefault(false)

    fun observeRemoteAppConfig(): Flow<RemoteAppConfig> = flow {
        val db = dbProvider()
        if (db == null || !isSignedIn()) {
            emit(RemoteAppConfig())
            return@flow
        }
        val docRef = db.collection("app_config").document("global")
        emitAll(
            docRef.snapshots()
                .map { doc -> doc.toRemoteAppConfig() }
                .catch { error ->
                    val fsError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsError == null && error is CancellationException) throw error
                    Log.w(TAG, "observeRemoteAppConfig fallback", error)
                    emit(RemoteAppConfig())
                }
        )
    }

    fun observeSubscriptionPlans(): Flow<List<RemoteSubscriptionPlan>> = flow {
        val db = dbProvider()
        if (db == null || !isSignedIn()) {
            emit(emptyList())
            return@flow
        }
        emitAll(
            db.collection("subscription_plans")
                .whereEqualTo("isActive", true)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        val planName = doc.getString("planName")?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                        val priceInr = (doc.get("priceInr") as? Number)?.toDouble() ?: return@mapNotNull null
                        if (priceInr <= 0.0) return@mapNotNull null
                        RemoteSubscriptionPlan(
                            planId = doc.getString("planId") ?: doc.id,
                            planName = planName,
                            subtitle = doc.getString("subtitle").orEmpty(),
                            priceInr = priceInr,
                            validityDays = ((doc.get("validityDays") as? Number)?.toInt() ?: 30).coerceIn(1, 36500),
                            badge = doc.getString("badge").orEmpty(),
                            sortOrder = (doc.get("sortOrder") as? Number)?.toInt() ?: 0,
                            isActive = doc.getBoolean("isActive") ?: true
                        )
                    }.sortedWith(compareBy<RemoteSubscriptionPlan> { it.sortOrder }.thenBy { it.priceInr })
                }
                .catch { error ->
                    val fsError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsError == null && error is CancellationException) throw error
                    Log.w(TAG, "observeSubscriptionPlans fallback", error)
                    emit(emptyList())
                }
        )
    }

    fun observeActiveCategories(): Flow<List<RemoteCategory>> = flow {
        val db = dbProvider()
        if (db == null || !isSignedIn()) {
            emit(emptyList())
            return@flow
        }
        emitAll(
            db.collection("wallpaper_categories")
                .whereEqualTo("isActive", true)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        val name = doc.getString("name") ?: return@mapNotNull null
                        RemoteCategory(
                            categoryId = doc.getString("categoryId") ?: doc.id,
                            name = name,
                            description = doc.getString("description").orEmpty(),
                            sortOrder = (doc.get("sortOrder") as? Number)?.toInt() ?: 0
                        )
                    }.sortedWith(compareBy<RemoteCategory> { it.sortOrder }.thenBy { it.name })
                }
                .catch { error ->
                    val fsError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsError == null && error is CancellationException) throw error
                    Log.w(TAG, "observeActiveCategories fallback", error)
                    emit(emptyList())
                }
        )
    }

    fun observePublishedWallpapers(): Flow<List<WallpaperItem>> = flow {
        val db = dbProvider()
        if (db == null || !isSignedIn()) {
            emit(emptyList())
            return@flow
        }
        emitAll(
            db.collection("remote_wallpapers")
                .whereEqualTo("isPublished", true)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        doc.toRemoteWallpaperItem()
                    }.sortedByDescending { it.updatedAtMillis }
                }
                .catch { error ->
                    val fsError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsError == null && error is CancellationException) throw error
                    Log.w(TAG, "observePublishedWallpapers fallback", error)
                    emit(emptyList())
                }
        )
    }

    suspend fun downloadRemoteWallpaper(item: WallpaperItem): Result<String> = withContext(Dispatchers.IO) {
        val urlString = item.remoteMediaUrl?.takeIf { it.isNotBlank() }
            ?: return@withContext Result.failure(IllegalStateException("Missing remote media URL."))
        runCatching {
            val ext = if (item.isRemoteVideo) "mp4" else "jpg"
            val targetFile = File(remoteDownloadsDir, "${item.id}.$ext")

            val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
                connectTimeout = 20_000
                readTimeout = 30_000
            }
            conn.inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            conn.disconnect()

            if (item.isRemoteVideo) {
                val activeVideo = LiveOrbitVideoWallpaperService.activeVideoFile(context)
                targetFile.copyTo(activeVideo, overwrite = true)
            }

            // Copy to public MediaStore gallery directory on Android 10+ (zero permissions)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                runCatching {
                    val mime = if (item.isRemoteVideo) "video/mp4" else "image/jpeg"
                    val collection = if (item.isRemoteVideo) {
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else {
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                    val dir = if (item.isRemoteVideo) {
                        "${Environment.DIRECTORY_MOVIES}/Android 3D Rolling Launcher"
                    } else {
                        "${Environment.DIRECTORY_PICTURES}/Android 3D Rolling Launcher"
                    }
                    val values = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, "${item.id}.$ext")
                        put(MediaStore.MediaColumns.MIME_TYPE, mime)
                        put(MediaStore.MediaColumns.RELATIVE_PATH, dir)
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                    val resolver = context.contentResolver
                    val uri = resolver.insert(collection, values)
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { out ->
                            targetFile.inputStream().use { it.copyTo(out) }
                        }
                        values.clear()
                        values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                        resolver.update(uri, values, null, null)
                    }
                }
            }

            incrementDownloadCount(item.id)
            targetFile.absolutePath
        }
    }

    suspend fun prepareLiveVideoForWallpaper(item: WallpaperItem): Result<File> = withContext(Dispatchers.IO) {
        val localExisting = item.localFilePath?.let { File(it) }?.takeIf { it.exists() && it.length() > 0 }
        val file = if (localExisting != null) {
            localExisting
        } else {
            val res = downloadRemoteWallpaper(item)
            if (res.isFailure) return@withContext Result.failure(res.exceptionOrNull()!!)
            File(res.getOrThrow())
        }
        val activeVideo = LiveOrbitVideoWallpaperService.activeVideoFile(context)
        file.copyTo(activeVideo, overwrite = true)
        Result.success(activeVideo)
    }

    suspend fun incrementDownloadCount(wallpaperId: String) {
        val db = dbProvider() ?: return
        if (!isSignedIn()) return
        runCatching {
            db.collection("remote_wallpapers").document(wallpaperId).update(
                mapOf(
                    "downloadCount" to FieldValue.increment(1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }.onFailure {
            Log.w(TAG, "incrementDownloadCount skipped for $wallpaperId", it)
        }
    }

    private fun DocumentSnapshot.toRemoteAppConfig(): RemoteAppConfig {
        if (!exists()) return RemoteAppConfig()
        return RemoteAppConfig(
            maintenanceMode = getBoolean("maintenanceMode") ?: false,
            maintenanceMessage = getString("maintenanceMessage")
                ?: "Scheduled maintenance is in progress.",
            announcementBanner = getString("announcementBanner").orEmpty(),
            aiSearchEnabled = getBoolean("aiSearchEnabled") ?: true,
            liveWallpapersEnabled = getBoolean("liveWallpapersEnabled") ?: true,
            edgePanelEnabled = getBoolean("edgePanelEnabled") ?: true,
            controlCenterEnabled = getBoolean("controlCenterEnabled") ?: true,
            notificationBadgesEnabled = getBoolean("notificationBadgesEnabled") ?: true,
            paymentGatewayEnabled = getBoolean("paymentGatewayEnabled") ?: false,
            paymentProviderName = getString("paymentProviderName") ?: "FamGateway v2.0",
            paymentBackendUrl = getString("paymentBackendUrl").orEmpty(),
            paymentVerifyUrl = getString("paymentVerifyUrl").orEmpty(),
            paymentPublicKey = getString("paymentPublicKey").orEmpty(),
            paymentCurrency = getString("paymentCurrency") ?: "INR",
            monthlyPriceLabel = getString("monthlyPriceLabel") ?: "₹199 / month",
            yearlyPriceLabel = getString("yearlyPriceLabel") ?: "₹1,499 / year",
            lifetimePriceLabel = getString("lifetimePriceLabel") ?: "₹3,999 one-time",
            monthlyAmount = getString("monthlyAmount") ?: "199",
            yearlyAmount = getString("yearlyAmount") ?: "1499",
            lifetimeAmount = getString("lifetimeAmount") ?: "3999",
            usdToInrRate = (get("usdToInrRate") as? Number)?.toDouble()?.takeIf { it > 0.0 } ?: 85.0
        )
    }

    private fun DocumentSnapshot.toRemoteWallpaperItem(): WallpaperItem? {
        val id = getString("wallpaperId") ?: id
        val title = getString("title") ?: return null
        val mediaUrl = getString("mediaUrl") ?: return null
        val thumbUrl = getString("thumbnailUrl") ?: mediaUrl
        val mediaType = getString("mediaType") ?: "IMAGE"
        val categoryName = getString("categoryName").orEmpty().ifBlank { "Abstract" }
        val fileBytes = (get("fileSizeBytes") as? Number)?.toLong() ?: 0L
        val mbLabel = if (fileBytes > 0L) {
            String.format(java.util.Locale.US, "%.1f MB", fileBytes / (1024.0 * 1024.0))
        } else {
            "Cloud"
        }
        val mappedCategory = mapCategoryName(categoryName)
        return WallpaperItem(
            id = id,
            name = title,
            category = mappedCategory,
            collection = categoryName,
            isPremium = getBoolean("isPremium") ?: false,
            resolution = getString("resolutionLabel") ?: if (mediaType == "VIDEO") "Live MP4" else "4K HD",
            fileSizeLabel = mbLabel,
            tags = listOfNotNull(categoryName, mediaType, getString("description")?.take(40)),
            license = "Cloud Admin Sync",
            sourceType = if (mediaType == "VIDEO") WallpaperSourceType.REMOTE_VIDEO else WallpaperSourceType.REMOTE_IMAGE,
            drawableResName = null,
            paletteHexes = listOf(0xFF080B18, 0xFF713BFF, 0xFF168BFF, 0xFF00E5FF),
            proceduralStyle = ProceduralStyle.NEBULA_SPIRAL,
            seed = abs(id.hashCode().toLong()),
            remoteMediaUrl = mediaUrl,
            remoteThumbnailUrl = thumbUrl,
            categoryNameOverride = categoryName,
            downloadCount = (get("downloadCount") as? Number)?.toLong() ?: 0L,
            updatedAtMillis = getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?.toDate()?.time ?: 0L
        )
    }

    private fun mapCategoryName(name: String): WallpaperCategory {
        val n = name.trim().lowercase()
        return when {
            n.contains("amoled") -> WallpaperCategory.AMOLED
            n.contains("nature") -> WallpaperCategory.NATURE
            n.contains("abstract") -> WallpaperCategory.ABSTRACT
            n.contains("space") -> WallpaperCategory.SPACE
            n.contains("minimal") -> WallpaperCategory.MINIMAL
            n.contains("3d") -> WallpaperCategory.TECHNOLOGY
            n.contains("anime") -> WallpaperCategory.ARTISTIC
            n.contains("car") -> WallpaperCategory.CINEMATIC
            n.contains("premium") -> WallpaperCategory.EXCLUSIVE
            else -> WallpaperCategory.ABSTRACT
        }
    }

    companion object {
        private const val TAG = "RemoteContentRepo"
    }
}
