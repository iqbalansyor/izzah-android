package com.iqbalansyori.izzah

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.iqbalansyori.izzah.data.lock.AppLockManager
import com.iqbalansyori.izzah.data.notify.NotificationChannels
import com.iqbalansyori.izzah.domain.repository.BlockingController
import com.iqbalansyori.izzah.domain.repository.ReminderScheduler
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import com.iqbalansyori.izzah.di.appModule
import com.iqbalansyori.izzah.di.dataModule
import com.iqbalansyori.izzah.di.viewModelModule
import com.iqbalansyori.izzah.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class IzzahApp : Application() {
    override fun onCreate() {
        super.onCreate()
        System.loadLibrary("sqlcipher")
        startKoin {
            androidContext(this@IzzahApp)
            modules(appModule, dataModule, viewModelModule)
        }
        NotificationChannels.create(this)
        val lock = get<AppLockManager>()
        val settings = get<SettingsRepository>()
        val scope = get<CoroutineScope>()
        val reminders = get<ReminderScheduler>()
        val blocking = get<BlockingController>()
        // reminders depend on the toggles and the location, so any change (and the first emission at start) replans them
        scope.launch {
            settings.settings.map { listOf(it.notifPrayer, it.notifDzikir, it.notifCheckin, it.location) }.distinctUntilChanged().collect { reminders.rescheduleAll() }
        }
        // blocking depends on what is locked, the schedule and the lock mode, so any change (and the first emission) re-evaluates it
        scope.launch {
            settings.settings.map { Triple(it.lockedPackages, it.slots, it.lockMode) }.distinctUntilChanged().collect { blocking.evaluate() }
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) = lock.onBackground()

            override fun onStart(owner: LifecycleOwner) {
                scope.launch { lock.onForeground(settings.current().pinHash != null) }
            }
        })
    }
}
