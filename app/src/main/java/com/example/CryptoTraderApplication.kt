package com.example

import android.app.Application
import com.example.data.local.CryptoDatabase
import com.example.service.NotificationHelper

class CryptoTraderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        // Warm up Room DB
        CryptoDatabase.getInstance(this)
    }
}
