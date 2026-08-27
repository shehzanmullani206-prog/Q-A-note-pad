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
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:100000000000:android:abcdef0123456789")
                    .setProjectId("qa-notes-fallback")
                    .setApiKey("AIzaSyMockKeyForOfflineSafetyFallback00")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d("QAApplication", "FirebaseApp initialized with fallback config")
            }
        } catch (e: Throwable) {
            Log.e("QAApplication", "Firebase initialization handled safely: ${e.message}")
        }
    }
}
