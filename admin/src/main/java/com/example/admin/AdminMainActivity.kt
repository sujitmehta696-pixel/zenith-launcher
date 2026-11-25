package com.example.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.SettingsRemote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.admin.ui.screens.AdminCategoryScreen
import com.example.admin.ui.screens.AdminDashboardScreen
import com.example.admin.ui.screens.AdminLoginScreen
import com.example.admin.ui.screens.AdminPaymentGatewayScreen
import com.example.admin.ui.screens.AdminRemoteControlScreen
import com.example.admin.ui.screens.AdminVideoUploadScreen
import com.example.admin.ui.screens.AdminWallpaperUploadScreen
import com.example.admin.ui.theme.AdminDanger
import com.example.admin.ui.theme.AdminMidnight
import com.example.admin.ui.theme.AdminNeonCyan
import com.example.admin.ui.theme.AdminSuccess
import com.example.admin.ui.theme.AdminSurface
import com.example.admin.ui.theme.AdminTheme
import com.example.admin.ui.viewmodel.AdminAuthState
import com.example.admin.ui.viewmodel.AdminViewModel

private enum class AdminTab(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Rounded.Dashboard),
    IMAGES("Wallpapers", Icons.Rounded.Image),
    VIDEOS("Live Video", Icons.Rounded.PlayCircle),
    CATEGORIES("Categories", Icons.Rounded.Category),
    PAYMENTS("Gateway", Icons.Rounded.Payments),
    CONTROLS("Controls", Icons.Rounded.SettingsRemote)
}

class AdminMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminTheme {
                AdminRootShell()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminRootShell(vm: AdminViewModel = viewModel()) {
    val authState by vm.authState.collectAsStateWithLifecycle()
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val activityContext = LocalContext.current
    var currentTab by rememberSaveable { mutableStateOf(AdminTab.DASHBOARD) }

    when (val auth = authState) {
        AdminAuthState.Checking -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AdminMidnight),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = AdminNeonCyan)
                    Text("Verifying Admin Session...", color = Color.White)
                }
            }
        }

        AdminAuthState.Unauthenticated -> {
            AdminLoginScreen(
                isLoading = false,
                errorMessage = null,
                onGoogleSignIn = { vm.signInWithGoogle(activityContext) }
            )
        }

        is AdminAuthState.Error -> {
            AdminLoginScreen(
                isLoading = false,
                errorMessage = auth.message,
                onGoogleSignIn = { vm.signInWithGoogle(activityContext) }
            )
        }

        is AdminAuthState.Authenticated -> {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(AdminMidnight, Color(0xFF0E132B), AdminMidnight)
                        )
                    )
            ) {
                val useRail = maxWidth >= 680.dp

                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    containerColor = Color.Transparent,
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = currentTab.label,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = AdminSurface.copy(alpha = 0.85f)
                            ),
                            actions = {
                                IconButton(
                                    onClick = { vm.signOut() },
                                    modifier = Modifier.testTag("admin_sign_out_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                                        contentDescription = "Sign Out",
                                        tint = Color.White
                                    )
                                }
                            }
                        )
                    },
                    bottomBar = {
                        if (!useRail) {
                            NavigationBar(containerColor = AdminSurface) {
                                AdminTab.entries.forEach { tab ->
                                    NavigationBarItem(
                                        selected = currentTab == tab,
                                        onClick = { currentTab = tab },
                                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                                        label = { Text(tab.label, maxLines = 1) },
                                        modifier = Modifier.testTag("admin_tab_${tab.name.lowercase()}")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (useRail) {
                            NavigationRail(
                                containerColor = AdminSurface,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                AdminTab.entries.forEach { tab ->
                                    NavigationRailItem(
                                        selected = currentTab == tab,
                                        onClick = { currentTab = tab },
                                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                                        label = { Text(tab.label) },
                                        modifier = Modifier.testTag("admin_rail_${tab.name.lowercase()}")
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .widthIn(max = 760.dp)
                            ) {
                                val banner = uiState.errorMessage ?: uiState.statusBanner
                                val isError = uiState.errorMessage != null
                                if (!banner.isNullOrBlank()) {
                                    Surface(
                                        color = if (isError) AdminDanger.copy(alpha = 0.2f) else AdminSuccess.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = banner,
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.weight(1f)
                                            )
                                            TextButton(onClick = { vm.clearMessages() }) {
                                                Text("Dismiss", color = AdminNeonCyan)
                                            }
                                        }
                                    }
                                }

                                Box(modifier = Modifier.weight(1f)) {
                                    when (currentTab) {
                                        AdminTab.DASHBOARD -> AdminDashboardScreen(
                                            session = auth.session,
                                            state = uiState,
                                            onRefresh = vm::refreshSync,
                                            onNavigateUploadImages = { currentTab = AdminTab.IMAGES },
                                            onNavigateUploadVideo = { currentTab = AdminTab.VIDEOS },
                                            onNavigateCategories = { currentTab = AdminTab.CATEGORIES },
                                            onNavigateRemoteControl = { currentTab = AdminTab.CONTROLS }
                                        )

                                        AdminTab.IMAGES -> AdminWallpaperUploadScreen(
                                            categories = uiState.categories,
                                            wallpapers = uiState.wallpapers,
                                            isUploading = uiState.isUploading,
                                            uploadProgress = uiState.uploadProgress,
                                            uploadStatusText = uiState.uploadStatusText,
                                            onUploadImages = vm::uploadImageWallpapers,
                                            onTogglePublish = vm::toggleWallpaperPublish,
                                            onTogglePremium = vm::toggleWallpaperTier,
                                            onDeleteWallpaper = vm::deleteWallpaper
                                        )

                                        AdminTab.VIDEOS -> AdminVideoUploadScreen(
                                            categories = uiState.categories,
                                            wallpapers = uiState.wallpapers,
                                            isUploading = uiState.isUploading,
                                            uploadProgress = uiState.uploadProgress,
                                            uploadStatusText = uiState.uploadStatusText,
                                            inspectVideo = vm::inspectVideo,
                                            onUploadVideo = vm::uploadLiveVideoWallpaper,
                                            onTogglePublish = vm::toggleWallpaperPublish,
                                            onTogglePremium = vm::toggleWallpaperTier,
                                            onDeleteWallpaper = vm::deleteWallpaper
                                        )

                                        AdminTab.CATEGORIES -> AdminCategoryScreen(
                                            categories = uiState.categories,
                                            onSeedDefaults = vm::seedDefaultCategories,
                                            onSaveCategory = vm::saveCategory,
                                            onDeleteCategory = vm::deleteCategory
                                        )

                                        AdminTab.PAYMENTS -> AdminPaymentGatewayScreen(
                                            config = uiState.globalConfig,
                                            secrets = uiState.paymentSecrets,
                                            plans = uiState.subscriptionPlans,
                                            onSaveGateway = vm::savePaymentGateway,
                                            onSavePlan = vm::saveSubscriptionPlan,
                                            onDeletePlan = vm::deleteSubscriptionPlan,
                                            onSeedDefaultPlans = vm::seedDefaultPlansInr
                                        )

                                        AdminTab.CONTROLS -> AdminRemoteControlScreen(
                                            config = uiState.globalConfig,
                                            users = uiState.users,
                                            admins = uiState.admins,
                                            onSaveGlobalConfig = vm::saveGlobalConfig,
                                            onUpdateUserAccess = vm::updateUserAccess,
                                            onAddAdmin = vm::addAdmin,
                                            onRemoveAdmin = vm::removeAdmin
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
