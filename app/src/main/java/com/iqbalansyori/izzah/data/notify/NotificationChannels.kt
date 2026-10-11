package com.iqbalansyori.izzah.data.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.iqbalansyori.izzah.R
import com.iqbalansyori.izzah.data.locale.localized

object NotificationChannels {
    const val REMINDERS = "reminders"
    const val PROTECT = "protect"

    // names are read through the localized context so the channel list in system settings follows the app language
    fun create(context: Context) {
        val localized = context.localized()
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(REMINDERS, localized.getString(R.string.notif_channel_reminders), NotificationManager.IMPORTANCE_DEFAULT),
        )
        manager.createNotificationChannel(
            NotificationChannel(PROTECT, localized.getString(R.string.notif_channel_protect), NotificationManager.IMPORTANCE_LOW),
        )
    }
}
