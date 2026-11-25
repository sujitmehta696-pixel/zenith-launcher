package com.example.data.repository

import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import android.provider.Settings
import com.example.data.local.AppUsageEntity
import com.example.data.local.FavoriteAppEntity
import com.example.data.local.HiddenAppEntity
import com.example.data.local.LauncherMetaDao
import com.example.data.local.PinnedHomeAppEntity
import com.example.data.local.SearchHistoryEntity
import com.example.data.model.InstalledAppInfo
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class LauncherRepository(
    private val context: Context,
    private val launcherMetaDao: LauncherMetaDao
) {
    private val repoExceptionHandler = CoroutineExceptionHandler { _, _ -> }
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + repoExceptionHandler)
    private val rawInstalledApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    private val iconCache = ConcurrentHashMap<String, Drawable>()
    private val monitorsRegistered = AtomicBoolean(false)

    init {
        registerPackageMonitors()
    }

    private fun registerPackageMonitors() {
        if (!monitorsRegistered.compareAndSet(false, true)) return
        val appContext = context.applicationContext ?: context

        // 1. LauncherApps.Callback for instant package add/remove/update updates
        runCatching {
            val launcherApps = appContext.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
            launcherApps?.registerCallback(
                object : LauncherApps.Callback() {
                    override fun onPackageRemoved(packageName: String, user: UserHandle) {
                        invalidateAndRefresh(packageName)
                    }

                    override fun onPackageAdded(packageName: String, user: UserHandle) {
                        invalidateAndRefresh(packageName)
                    }

                    override fun onPackageChanged(packageName: String, user: UserHandle) {
                        invalidateAndRefresh(packageName)
                    }

                    override fun onPackagesAvailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) {
                        packageNames.forEach { iconCache.remove(it) }
                        repoScope.launch { refreshInstalledApps() }
                    }

                    override fun onPackagesUnavailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) {
                        packageNames.forEach { iconCache.remove(it) }
                        repoScope.launch { refreshInstalledApps() }
                    }
                },
                Handler(Looper.getMainLooper())
            )
        }

        // 2. Package BroadcastReceiver fallback
        runCatching {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_CHANGED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addDataScheme("package")
            }
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    val pkg = intent?.data?.schemeSpecificPart
                    invalidateAndRefresh(pkg)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appContext.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                appContext.registerReceiver(receiver, filter)
            }
        }
    }

    private fun invalidateAndRefresh(packageName: String?) {
        if (!packageName.isNullOrBlank()) {
            iconCache.remove(packageName)
        }
        repoScope.launch {
            refreshInstalledApps()
        }
    }

    val appsFlow: Flow<List<InstalledAppInfo>> = combine(
        rawInstalledApps,
        launcherMetaDao.observeFavoriteApps(),
        launcherMetaDao.observeHiddenApps(),
        launcherMetaDao.observePinnedHomeApps(),
        launcherMetaDao.observeAppUsage()
    ) { apps, favs, hidden, pinned, usage ->
        val favSet = favs.map { it.packageName }.toSet()
        val hiddenSet = hidden.map { it.packageName }.toSet()
        val pinnedMap = pinned.associateBy { it.packageName }
        val usageMap = usage.associateBy { it.packageName }

        apps.map { app ->
            val pin = pinnedMap[app.packageName]
            val u = usageMap[app.packageName]
            app.copy(
                isFavorite = app.packageName in favSet,
                isHidden = app.packageName in hiddenSet,
                isPinnedHome = pin != null,
                homePage = pin?.pageIndex ?: 0,
                homeSlot = pin?.slotIndex ?: -1,
                lastLaunchTimestamp = u?.lastLaunchTimestamp ?: 0L,
                launchCount = u?.launchCount ?: 0
            )
        }
    }

    val searchHistoryFlow: Flow<List<String>> =
        launcherMetaDao.observeSearchHistory().map { list -> list.map { it.query } }

    suspend fun refreshInstalledApps() = withContext(Dispatchers.IO) {
        try {
            val pm = context.packageManager
            val discovered = linkedMapOf<String, InstalledAppInfo>()

            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0L))
                } else {
                    @Suppress("DEPRECATION")
                    pm.queryIntentActivities(mainIntent, 0)
                }
            }.getOrDefault(emptyList())

            resolveInfos.forEach { info ->
                val actInfo = info.activityInfo ?: return@forEach
                val pkg = actInfo.packageName ?: return@forEach
                val act = actInfo.name ?: return@forEach
                val key = "$pkg/$act"
                if (!discovered.containsKey(key)) {
                    val label = runCatching { info.loadLabel(pm).toString() }.getOrNull()
                        ?.takeIf { it.isNotBlank() } ?: pkg
                    val isSys = ((actInfo.applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM) != 0
                    if (!iconCache.containsKey(pkg)) {
                        runCatching { info.loadIcon(pm) }.getOrNull()?.let { iconCache[pkg] = it }
                    }
                    discovered[key] = InstalledAppInfo(
                        packageName = pkg,
                        activityName = act,
                        label = label,
                        isSystemApp = isSys
                    )
                }
            }

            // Also query LauncherApps across user profiles (supports Work Profile & Xiaomi Dual Apps safely)
            runCatching {
                val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
                val userManager = context.getSystemService(Context.USER_SERVICE) as? UserManager
                val profiles = runCatching { userManager?.userProfiles }.getOrNull()
                    ?.takeIf { it.isNotEmpty() }
                    ?: listOf(Process.myUserHandle())

                profiles.forEach { profile ->
                    runCatching {
                        launcherApps?.getActivityList(null, profile).orEmpty().forEach { laInfo ->
                            val comp = laInfo.componentName
                            val key = "${comp.packageName}/${comp.className}"
                            if (!discovered.containsKey(key)) {
                                val label = laInfo.label?.toString()?.takeIf { it.isNotBlank() } ?: comp.packageName
                                val isSys = (laInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                                if (!iconCache.containsKey(comp.packageName)) {
                                    runCatching { laInfo.getBadgedIcon(0) }.getOrNull()?.let {
                                        iconCache[comp.packageName] = it
                                    }
                                }
                                discovered[key] = InstalledAppInfo(
                                    packageName = comp.packageName,
                                    activityName = comp.className,
                                    label = label,
                                    isSystemApp = isSys
                                )
                            }
                        }
                    }
                }
            }

            rawInstalledApps.value = discovered.values.sortedBy { it.label.lowercase() }
        } catch (_: Throwable) {
            // Protect background app discovery against OEM PackageManager/binder faults
        }
    }

    fun getAppIcon(packageName: String): Drawable? {
        iconCache[packageName]?.let { return it }
        return runCatching {
            context.packageManager.getApplicationIcon(packageName).also {
                iconCache[packageName] = it
            }
        }.getOrNull()
    }

    suspend fun launchApp(app: InstalledAppInfo): Result<Unit> {
        val result = runCatching {
            val component = ComponentName(app.packageName, app.activityName)
            val explicitIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                this.component = component
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }

            val launched = withContext(Dispatchers.Main.immediate) {
                val explicitOk = runCatching {
                    context.startActivity(explicitIntent)
                    true
                }.getOrDefault(false)

                if (explicitOk) {
                    true
                } else {
                    val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
                    val serviceOk = runCatching {
                        launcherApps?.startMainActivity(component, Process.myUserHandle(), null, null)
                        launcherApps != null
                    }.getOrDefault(false)

                    if (serviceOk) {
                        true
                    } else {
                        val fallbackIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                            ?.apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                            }
                            ?: throw IllegalStateException("App activity is no longer available")
                        context.startActivity(fallbackIntent)
                        true
                    }
                }
            }

            if (launched) {
                withContext(Dispatchers.IO) {
                    val existing = launcherMetaDao.getAppUsage(app.packageName)
                    launcherMetaDao.upsertAppUsage(
                        AppUsageEntity(
                            packageName = app.packageName,
                            lastLaunchTimestamp = System.currentTimeMillis(),
                            launchCount = (existing?.launchCount ?: 0) + 1
                        )
                    )
                }
            }
        }
        if (result.isFailure) {
            repoScope.launch { refreshInstalledApps() }
        }
        return result
    }

    fun queryAppShortcuts(packageName: String): List<ShortcutInfo> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return emptyList()
        return runCatching {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
                ?: return emptyList()
            if (!launcherApps.hasShortcutHostPermission()) return emptyList()
            val query = LauncherApps.ShortcutQuery().apply {
                setPackage(packageName)
                setQueryFlags(
                    LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
                )
            }
            launcherApps.getShortcuts(query, Process.myUserHandle()).orEmpty().take(4)
        }.getOrDefault(emptyList())
    }

    fun launchShortcut(shortcut: ShortcutInfo): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return false
        return runCatching {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
                ?: return false
            launcherApps.startShortcut(shortcut, null, null)
            true
        }.getOrDefault(false)
    }

    fun openAppInfo(packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }
    }

    fun uninstallApp(packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:$packageName")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }
    }

    fun isDefaultLauncher(): Boolean = runCatching {
        val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
        val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.resolveActivity(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        }
        resolveInfo?.activityInfo?.packageName == context.packageName
    }.getOrDefault(false)

    fun createDefaultLauncherIntent(): Intent = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_HOME)
            ) {
                return@runCatching roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
            }
        }
        Intent(Settings.ACTION_HOME_SETTINGS)
    }.getOrElse { Intent(Settings.ACTION_HOME_SETTINGS) }

    suspend fun toggleFavoriteApp(packageName: String, currentlyFavorite: Boolean) {
        if (currentlyFavorite) {
            launcherMetaDao.removeFavoriteApp(packageName)
        } else {
            launcherMetaDao.addFavoriteApp(FavoriteAppEntity(packageName))
        }
    }

    suspend fun toggleHiddenApp(packageName: String, currentlyHidden: Boolean) {
        if (currentlyHidden) {
            launcherMetaDao.unhideApp(packageName)
        } else {
            launcherMetaDao.hideApp(HiddenAppEntity(packageName))
        }
    }

    suspend fun pinAppToHome(packageName: String, pageIndex: Int, slotIndex: Int) {
        launcherMetaDao.pinApp(PinnedHomeAppEntity(packageName, pageIndex, slotIndex))
    }

    suspend fun unpinAppFromHome(packageName: String) {
        launcherMetaDao.unpinApp(packageName)
    }

    suspend fun clearPinnedApps() {
        launcherMetaDao.clearPinnedApps()
    }

    suspend fun recordSearchQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isNotEmpty()) {
            launcherMetaDao.upsertSearchQuery(SearchHistoryEntity(trimmed, System.currentTimeMillis()))
        }
    }

    suspend fun clearSearchHistory() {
        launcherMetaDao.clearSearchHistory()
    }
}
