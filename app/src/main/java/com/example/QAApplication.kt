package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

object FirebaseConfigHelper {
    @Volatile
    var isRealConfig: Boolean = false
}

class QAApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:100000000000:android:abcdef0123456789")
                    .setProjectId("qa-notes-fallback")
                    .setApiKey("AIzaSyMockKeyForOfflineSafetyFallback00")
                    .build()
                FirebaseApp.initializeApp(this, options)
                FirebaseConfigHelper.isRealConfig = false
                Log.d("QAApplication", "Firebase fallback initialized (Offline Mode)")
            } else {
                val app = FirebaseApp.getInstance()
                val apiKey = app.options.apiKey
                val projectId = app.options.projectId
                val isMock = apiKey.contains("MockKey") || (projectId?.contains("fallback") == true)
                FirebaseConfigHelper.isRealConfig = !isMock
                Log.d("QAApplication", "Firebase initialized with real config: ${FirebaseConfigHelper.isRealConfig}")
            }
        } catch (e: Throwable) {
            FirebaseConfigHelper.isRealConfig = false
            Log.e("QAApplication", "Firebase initialization handled safely: ${e.message}")
        }
    }
}
