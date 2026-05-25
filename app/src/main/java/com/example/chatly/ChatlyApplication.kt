package com.example.chatly

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp

class ChatlyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Khởi tạo Firebase
        FirebaseApp.initializeApp(this)
    }
}