package com.example.admin.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.admin.data.AdminAuthRepository
import com.example.admin.data.AdminFirestoreRepository
import com.example.admin.data.AdminRoleItem
import com.example.admin.data.AdminSession
import com.example.admin.data.AdminStorageRepository
import com.example.admin.data.CategoryItem
import com.example.admin.data.DashboardMetrics
import com.example.admin.data.PaymentGatewaySecrets
import com.example.admin.data.RemoteAppConfig
import com.example.admin.data.RemoteWallpaperItem
import com.example.admin.data.SubscriptionPlanItem
import com.example.admin.data.UserControlItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface AdminAuthState {
    data object Checking : AdminAuthState
    data object Unauthenticated : AdminAuthState
    data class Authenticated(val session: AdminSession) : AdminAuthState
    data class Error(val message: String) : AdminAuthState
}

data class AdminDashboardUiState(
    val categories: List<CategoryItem> = emptyList(),
    val wallpapers: List<RemoteWallpaperItem> = emptyList(),
    val users: List<UserControlItem> = emptyList(),
    val admins: List<AdminRoleItem> = emptyList(),
    val globalConfig: RemoteAppConfig = RemoteAppConfig(),
    val paymentSecrets: PaymentGatewaySecrets = PaymentGatewaySecrets(),
    val subscriptionPlans: List<SubscriptionPlanItem> = emptyList(),
    val metrics: DashboardMetrics = DashboardMetrics(),
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val uploadStatusText: String = "",
    val statusBanner: String? = null,
    val errorMessage: String? = null
)

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepo = AdminAuthRepository(application)
    private val firestoreRepo = AdminFirestoreRepository(application)
    private val storageRepo = AdminStorageRepository(application)

    private val _authState = MutableStateFlow<AdminAuthState>(AdminAuthState.Checking)
    val authState: StateFlow<AdminAuthState> = _authState.asStateFlow()

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    private var syncJobs: List<Job> = emptyList()

    init {
        viewModelScope.launch {
            val current = authRepo.currentUser()
            if (current == null) {
                _authState.value = AdminAuthState.Unauthenticated
            } else {
                val verification = authRepo.verifyAdminAccess(current)
                verification.onSuccess { session ->
                    _authState.value = AdminAuthState.Authenticated(session)
                    startRealtimeSync()
                }.onFailure { err ->
                    _authState.value = AdminAuthState.Error(err.localizedMessage ?: "Access denied.")
                }
            }
        }
    }

    fun signInWithGoogle(activityContext: Context) {
        viewModelScope.launch {
            _authState.value = AdminAuthState.Checking
            val result = authRepo.signInWithGoogle(activityContext)
            result.onSuccess { session ->
                _authState.value = AdminAuthState.Authenticated(session)
                startRealtimeSync()
            }.onFailure { err ->
                _authState.value = AdminAuthState.Error(err.localizedMessage ?: "Admin sign-in failed.")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            stopRealtimeSync()
            authRepo.signOut()
            _uiState.value = AdminDashboardUiState()
            _authState.value = AdminAuthState.Unauthenticated
        }
    }

    fun refreshSync() {
        if (_authState.value is AdminAuthState.Authenticated) {
            startRealtimeSync()
            postStatus("Live Firestore sync refreshed.")
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(statusBanner = null, errorMessage = null) }
    }

    private fun startRealtimeSync() {
        stopRealtimeSync()
        val j1 = viewModelScope.launch {
            firestoreRepo.observeCategories().collect { res ->
                res.onSuccess { list ->
                    _uiState.update { it.copy(categories = list) }
                }.onFailure { err ->
                    postError(err.localizedMessage ?: "Failed loading categories.")
                }
            }
        }
        val j2 = viewModelScope.launch {
            firestoreRepo.observeWallpapers().collect { res ->
                res.onSuccess { list ->
                    _uiState.update { state ->
                        state.copy(
                            wallpapers = list,
                            metrics = computeMetrics(state.users, list)
                        )
                    }
                }.onFailure { err ->
                    postError(err.localizedMessage ?: "Failed loading wallpapers.")
                }
            }
        }
        val j3 = viewModelScope.launch {
            firestoreRepo.observeUsers().collect { res ->
                res.onSuccess { list ->
                    _uiState.update { state ->
                        state.copy(
                            users = list,
                            metrics = computeMetrics(list, state.wallpapers)
                        )
                    }
                }.onFailure { err ->
                    postError(err.localizedMessage ?: "Failed loading users.")
                }
            }
        }
        val j4 = viewModelScope.launch {
            firestoreRepo.observeAdmins().collect { res ->
                res.onSuccess { list ->
                    _uiState.update { it.copy(admins = list) }
                }
            }
        }
        val j5 = viewModelScope.launch {
            firestoreRepo.observeGlobalConfig().collect { res ->
                res.onSuccess { cfg ->
                    _uiState.update { it.copy(globalConfig = cfg) }
                }
            }
        }
        val j6 = viewModelScope.launch {
            firestoreRepo.observePaymentSecrets().collect { res ->
                res.onSuccess { secrets ->
                    _uiState.update { it.copy(paymentSecrets = secrets) }
                }
            }
        }
        val j7 = viewModelScope.launch {
            firestoreRepo.observeSubscriptionPlans().collect { res ->
                res.onSuccess { plans ->
                    _uiState.update { it.copy(subscriptionPlans = plans) }
                }
            }
        }
        syncJobs = listOf(j1, j2, j3, j4, j5, j6, j7)
    }

    private fun stopRealtimeSync() {
        syncJobs.forEach { it.cancel() }
        syncJobs = emptyList()
    }

    private fun computeMetrics(
        users: List<UserControlItem>,
        wallpapers: List<RemoteWallpaperItem>
    ): DashboardMetrics {
        val now = System.currentTimeMillis()
        val activeThreshold = now - (24L * 60L * 60L * 1000L)
        val activeUsers = users.count { it.isActive || it.lastActiveMillis >= activeThreshold }
        val images = wallpapers.count { it.mediaType == "IMAGE" }
        val videos = wallpapers.count { it.mediaType == "VIDEO" }
        val downloads = wallpapers.sumOf { it.downloadCount }
        return DashboardMetrics(
            totalUsers = users.size,
            activeUsers = activeUsers,
            totalWallpapers = images,
            totalLiveVideos = videos,
            totalDownloads = downloads
        )
    }

    fun uploadImageWallpapers(
        uris: List<Uri>,
        baseTitle: String,
        description: String,
        category: CategoryItem?,
        isPremium: Boolean,
        publishNow: Boolean
    ) {
        if (uris.isEmpty()) {
            postError("Select at least one image from Gallery.")
            return
        }
        val catId = category?.categoryId ?: "cat_3d"
        val catName = category?.name ?: "3D"
        viewModelScope.launch {
            _uiState.update {
                it.copy(isUploading = true, uploadProgress = 0f, uploadStatusText = "Starting image upload...", errorMessage = null)
            }
            var successCount = 0
            val total = uris.size
            for ((index, uri) in uris.withIndex()) {
                val wallpaperId = "wp_img_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
                val title = if (total == 1) {
                    baseTitle.trim().ifBlank { "Studio Wallpaper" }
                } else {
                    "${baseTitle.trim().ifBlank { "Studio Pack" }} #${index + 1}"
                }
                _uiState.update {
                    it.copy(uploadStatusText = "Uploading image ${index + 1} of $total...")
                }
                val uploadRes = storageRepo.uploadImageWallpaper(wallpaperId, uri) { frac ->
                    val overall = (index.toFloat() + frac) / total.toFloat()
                    _uiState.update { it.copy(uploadProgress = overall.coerceIn(0f, 1f)) }
                }
                uploadRes.onSuccess { assets ->
                    val docRes = firestoreRepo.createWallpaperRecord(
                        wallpaperId = wallpaperId,
                        title = title,
                        description = description,
                        categoryId = catId,
                        categoryName = catName,
                        mediaType = "IMAGE",
                        mediaUrl = assets.mediaUrl,
                        thumbnailUrl = assets.thumbnailUrl,
                        storagePath = assets.storagePath,
                        thumbnailStoragePath = assets.thumbnailStoragePath,
                        resolutionLabel = assets.resolutionLabel,
                        fileSizeBytes = assets.fileSizeBytes,
                        isPremium = isPremium,
                        isPublished = publishNow
                    )
                    if (docRes.isSuccess) {
                        successCount++
                    } else {
                        postError(docRes.exceptionOrNull()?.localizedMessage ?: "Failed saving metadata.")
                    }
                }.onFailure { err ->
                    postError(err.localizedMessage ?: "Storage upload failed.")
                }
            }
            _uiState.update {
                it.copy(
                    isUploading = false,
                    uploadProgress = 0f,
                    uploadStatusText = "",
                    statusBanner = if (successCount > 0) "Uploaded & synced $successCount wallpaper(s)!" else it.statusBanner
                )
            }
        }
    }

    suspend fun inspectVideo(uri: Uri): Result<Pair<Bitmap?, String>> {
        return storageRepo.extractVideoInfoAndThumbnail(uri)
    }

    fun uploadLiveVideoWallpaper(
        videoUri: Uri?,
        title: String,
        description: String,
        category: CategoryItem?,
        isPremium: Boolean,
        publishNow: Boolean
    ) {
        if (videoUri == null) {
            postError("Select an MP4 video from Gallery first.")
            return
        }
        val catId = category?.categoryId ?: "cat_live"
        val catName = category?.name ?: "Live Wallpapers"
        viewModelScope.launch {
            val wallpaperId = "wp_vid_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
            _uiState.update {
                it.copy(
                    isUploading = true,
                    uploadProgress = 0.05f,
                    uploadStatusText = "Extracting thumbnail & uploading MP4...",
                    errorMessage = null
                )
            }
            val uploadRes = storageRepo.uploadLiveVideoWallpaper(wallpaperId, videoUri) { frac ->
                _uiState.update { it.copy(uploadProgress = frac.coerceIn(0f, 1f)) }
            }
            uploadRes.onSuccess { assets ->
                val docRes = firestoreRepo.createWallpaperRecord(
                    wallpaperId = wallpaperId,
                    title = title.trim().ifBlank { "Live Motion Loop" },
                    description = description,
                    categoryId = catId,
                    categoryName = catName,
                    mediaType = "VIDEO",
                    mediaUrl = assets.mediaUrl,
                    thumbnailUrl = assets.thumbnailUrl,
                    storagePath = assets.storagePath,
                    thumbnailStoragePath = assets.thumbnailStoragePath,
                    resolutionLabel = assets.resolutionLabel,
                    fileSizeBytes = assets.fileSizeBytes,
                    isPremium = isPremium,
                    isPublished = publishNow
                )
                docRes.onSuccess {
                    postStatus("Live MP4 wallpaper uploaded & synced!")
                }.onFailure { err ->
                    postError(err.localizedMessage ?: "Failed saving video metadata.")
                }
            }.onFailure { err ->
                postError(err.localizedMessage ?: "Video upload failed.")
            }
            _uiState.update {
                it.copy(isUploading = false, uploadProgress = 0f, uploadStatusText = "")
            }
        }
    }

    fun toggleWallpaperPublish(item: RemoteWallpaperItem) {
        viewModelScope.launch {
            firestoreRepo.updateWallpaperPublishAndTier(
                wallpaperId = item.wallpaperId,
                isPublished = !item.isPublished,
                isPremium = item.isPremium
            ).onSuccess {
                postStatus(if (!item.isPublished) "Published '${item.title}' live." else "Unpublished '${item.title}'.")
            }.onFailure { err ->
                postError(err.localizedMessage ?: "Update failed.")
            }
        }
    }

    fun toggleWallpaperTier(item: RemoteWallpaperItem) {
        viewModelScope.launch {
            firestoreRepo.updateWallpaperPublishAndTier(
                wallpaperId = item.wallpaperId,
                isPublished = item.isPublished,
                isPremium = !item.isPremium
            ).onSuccess {
                postStatus("'${item.title}' set to ${if (!item.isPremium) "Premium" else "Free"}.")
            }.onFailure { err ->
                postError(err.localizedMessage ?: "Tier update failed.")
            }
        }
    }

    fun deleteWallpaper(item: RemoteWallpaperItem) {
        viewModelScope.launch {
            storageRepo.deleteStorageObjects(
                listOfNotNull(item.storagePath, item.thumbnailStoragePath)
            )
            firestoreRepo.deleteWallpaperDoc(item.wallpaperId).onSuccess {
                postStatus("Deleted '${item.title}'.")
            }.onFailure { err ->
                postError(err.localizedMessage ?: "Delete failed.")
            }
        }
    }

    fun seedDefaultCategories() {
        viewModelScope.launch {
            firestoreRepo.seedDefaultCategories().onSuccess { count ->
                postStatus(
                    if (count > 0) "Seeded $count default launcher categories."
                    else "All 9 default categories already exist."
                )
            }.onFailure { err ->
                postError(err.localizedMessage ?: "Category seeding failed.")
            }
        }
    }

    fun saveCategory(
        categoryId: String?,
        name: String,
        description: String,
        sortOrder: Int,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            firestoreRepo.saveCategory(categoryId, name, description, sortOrder, isActive)
                .onSuccess { postStatus("Category '$name' saved.") }
                .onFailure { err -> postError(err.localizedMessage ?: "Save category failed.") }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            firestoreRepo.deleteCategory(categoryId)
                .onSuccess { postStatus("Category deleted.") }
                .onFailure { err -> postError(err.localizedMessage ?: "Delete category failed.") }
        }
    }

    fun updateUserAccess(user: UserControlItem, grantPro: Boolean, block: Boolean) {
        viewModelScope.launch {
            firestoreRepo.updateUserModeration(user.userId, grantPro, block)
                .onSuccess {
                    postStatus("Updated ${user.displayName}: Pro=$grantPro, Blocked=$block")
                }
                .onFailure { err ->
                    postError(err.localizedMessage ?: "User update failed.")
                }
        }
    }

    fun addAdmin(uid: String, email: String, name: String, role: String) {
        viewModelScope.launch {
            firestoreRepo.addAdminRole(uid, email, name, role)
                .onSuccess { postStatus("Added admin '$email'.") }
                .onFailure { err -> postError(err.localizedMessage ?: "Failed adding admin.") }
        }
    }

    fun removeAdmin(adminUid: String) {
        viewModelScope.launch {
            firestoreRepo.removeAdminRole(adminUid)
                .onSuccess { postStatus("Removed admin role.") }
                .onFailure { err -> postError(err.localizedMessage ?: "Failed removing admin.") }
        }
    }

    fun saveGlobalConfig(config: RemoteAppConfig) {
        viewModelScope.launch {
            firestoreRepo.saveGlobalConfig(config)
                .onSuccess { postStatus("Global app controls pushed live to User App!") }
                .onFailure { err -> postError(err.localizedMessage ?: "Config update failed.") }
        }
    }

    fun savePaymentGateway(
        config: RemoteAppConfig,
        secretApiKey: String,
        webhookSecret: String
    ) {
        if (config.paymentGatewayEnabled) {
            val url = config.paymentBackendUrl.trim()
            if (!url.startsWith("https://") && !url.startsWith("http://")) {
                postError("Enter a valid Backend URL starting with https:// before enabling Payment Gateway.")
                return
            }
        }
        viewModelScope.launch {
            val cfgRes = firestoreRepo.saveGlobalConfig(config)
            val secRes = firestoreRepo.savePaymentSecrets(
                providerName = config.paymentProviderName,
                secretApiKey = secretApiKey,
                webhookSecret = webhookSecret
            )
            if (cfgRes.isSuccess && secRes.isSuccess) {
                postStatus(
                    if (config.paymentGatewayEnabled) {
                        "Payment Gateway (${config.paymentProviderName}) & Admin Secrets saved live!"
                    } else {
                        "Payment Gateway settings saved (currently OFF; Play Billing fallback active)."
                    }
                )
            } else {
                val err = cfgRes.exceptionOrNull() ?: secRes.exceptionOrNull()
                postError(err?.localizedMessage ?: "Failed saving payment gateway settings.")
            }
        }
    }

    fun saveSubscriptionPlan(
        planId: String?,
        planName: String,
        subtitle: String,
        priceInr: Double,
        validityDays: Int,
        badge: String,
        sortOrder: Int,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            firestoreRepo.saveSubscriptionPlan(
                planId = planId,
                planName = planName,
                subtitle = subtitle,
                priceInr = priceInr,
                validityDays = validityDays,
                badge = badge,
                sortOrder = sortOrder,
                isActive = isActive
            ).onSuccess {
                postStatus("Subscription plan '$planName' (₹${priceInr.toInt()} / ${validityDays}d) pushed live!")
            }.onFailure { err ->
                postError(err.localizedMessage ?: "Failed saving subscription plan.")
            }
        }
    }

    fun deleteSubscriptionPlan(planId: String) {
        viewModelScope.launch {
            firestoreRepo.deleteSubscriptionPlan(planId)
                .onSuccess { postStatus("Subscription plan deleted.") }
                .onFailure { err -> postError(err.localizedMessage ?: "Failed deleting subscription plan.") }
        }
    }

    fun seedDefaultPlansInr() {
        viewModelScope.launch {
            firestoreRepo.seedDefaultSubscriptionPlansInr()
                .onSuccess { count ->
                    postStatus(
                        if (count > 0) "Seeded $count default INR subscription plans!"
                        else "Default INR subscription plans already exist."
                    )
                }
                .onFailure { err -> postError(err.localizedMessage ?: "Failed seeding INR plans.") }
        }
    }

    private fun postStatus(msg: String) {
        _uiState.update { it.copy(statusBanner = msg, errorMessage = null) }
    }

    private fun postError(msg: String) {
        _uiState.update { it.copy(errorMessage = msg) }
    }
}
