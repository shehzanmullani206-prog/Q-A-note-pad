package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class QAApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                // Safe default initialization if google-services.json was omitted
                val options = FirebaseOptions.Builder()
                    .setApplicationId(packageName)
                    .setProjectId("qa-notes-" + packageName.takeLast(6))
                    .setApiKey("AIzaSyMockKeyForOfflineSafetyFallback")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d("QAApplication", "FirebaseApp initialized with fallback config")
            }
        } catch (e: Exception) {
            Log.e("QAApplication", "Firebase initialization caught safely: ${e.message}")
        }
    }
}
