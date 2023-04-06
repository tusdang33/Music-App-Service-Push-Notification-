package com.kaizm.learnmusicplayer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi

class AppNotification : Application() {
    companion object {
        const val CHANNEL_ID = "1"
        const val CHANNEL_NAME = "MUSIC CHANNEL"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        createNotification()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification() {
        val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
            setSound(null, null)
        }
        getSystemService(NotificationManager::class.java).apply {
            createNotificationChannel(notificationChannel)
        }
    }
}