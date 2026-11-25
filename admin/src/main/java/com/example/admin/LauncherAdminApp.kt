package com.example.admin

import android.app.Application
import com.google.firebase.FirebaseApp

class LauncherAdminApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
