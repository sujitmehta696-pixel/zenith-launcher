package com.example

import android.app.Application
import android.util.Log
import com.example.data.ads.AdManager
import com.example.data.billing.BillingManager
import com.example.data.billing.PaymentGatewayClient
import com.example.data.cloud.AiSearchRepository
import com.example.data.cloud.AuthRepository
import com.example.data.cloud.CloudAccountRepository
import com.example.data.cloud.RemoteContentRepository
import com.example.data.launcher.PhoneContentSearchHelper
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesDataStore
import com.example.data.repository.LauncherRepository
import com.example.data.repository.ThemeRepository
import com.example.data.repository.WallpaperRepository
import com.example.data.wallpaper.WallpaperRenderer
import com.example.data.wallpaper.WeatherRepository
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(private val application: Application) {
    val database: AppDatabase by lazy { AppDatabase.getInstance(application) }
    val preferencesDataStore: PreferencesDataStore by lazy { PreferencesDataStore(application) }
    val wallpaperRenderer: WallpaperRenderer by lazy { WallpaperRenderer(application) }
    val billingManager: BillingManager by lazy { BillingManager(application, preferencesDataStore) }
    val paymentGatewayClient: PaymentGatewayClient by lazy { PaymentGatewayClient() }
    val adManager: AdManager by lazy { AdManager(application) }

    private fun ensureFirebaseInitialized(): Boolean {
        return runCatching {
            if (FirebaseApp.getApps(application).isNotEmpty()) {
                true
            } else {
                FirebaseApp.initializeApp(application) != null
            }
        }.getOrDefault(false)
    }

    val firebaseAuth: FirebaseAuth? by lazy {
        if (!ensureFirebaseInitialized()) null
        else runCatching { Firebase.auth }.getOrNull()
    }

    val firestore: FirebaseFirestore? by lazy {
        if (!ensureFirebaseInitialized()) null
        else runCatching {
            FirebaseFirestore.getInstance(
                application.getString(R.string.firestore_database_id)
            )
        }.getOrNull()
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(authProvider = { firebaseAuth })
    }

    val cloudAccountRepository: CloudAccountRepository by lazy {
        CloudAccountRepository(
            dbProvider = { firestore },
            authProvider = { firebaseAuth }
        )
    }

    val remoteContentRepository: RemoteContentRepository by lazy {
        RemoteContentRepository(
            context = application,
            dbProvider = { firestore },
            authProvider = { firebaseAuth }
        )
    }

    val aiSearchRepository: AiSearchRepository by lazy { AiSearchRepository() }
    val phoneContentSearchHelper: PhoneContentSearchHelper by lazy { PhoneContentSearchHelper(application) }
    val weatherRepository: WeatherRepository by lazy { WeatherRepository() }

    val wallpaperRepository: WallpaperRepository by lazy {
        WallpaperRepository(
            context = application,
            wallpaperDao = database.wallpaperDao(),
            preferencesDataStore = preferencesDataStore,
            renderer = wallpaperRenderer,
            remoteContentRepository = remoteContentRepository
        )
    }
    val themeRepository: ThemeRepository by lazy {
        ThemeRepository(
            themeDao = database.themeDao(),
            preferencesDataStore = preferencesDataStore
        )
    }
    val launcherRepository: LauncherRepository by lazy {
        LauncherRepository(
            context = application,
            launcherMetaDao = database.launcherMetaDao()
        )
    }
}

class AndroidLauncherApp : Application() {
    lateinit var container: AppContainer
        private set

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("AndroidLauncherApp", "Unhandled background coroutine error prevented crash", throwable)
    }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

    override fun onCreate() {
        super.onCreate()
        runCatching {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        }
        container = AppContainer(this)
        appScope.launch {
            runCatching { container.wallpaperRepository.ensureSeeded() }
            runCatching { container.themeRepository.ensureSeeded() }
            runCatching { container.launcherRepository.refreshInstalledApps() }
        }
    }
}
