package com.iqbalansyori.izzah.data.blocking

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.iqbalansyori.izzah.domain.repository.BlockingController
import com.iqbalansyori.izzah.domain.repository.ReminderScheduler
import org.koin.core.context.GlobalContext

// an app update stops the service just like a reboot does, so both restore reminders and blocking
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        // a receiver is not a Koin component, so the dependencies come from the global instance
        val koin = GlobalContext.get()
        koin.get<ReminderScheduler>().rescheduleAll()
        koin.get<BlockingController>().evaluate()
    }
}
