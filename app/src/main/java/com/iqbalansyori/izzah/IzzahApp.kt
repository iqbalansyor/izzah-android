package com.iqbalansyori.izzah

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.iqbalansyori.izzah.data.lock.AppLockManager
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
        val lock = get<AppLockManager>()
        val settings = get<SettingsRepository>()
        val scope = get<CoroutineScope>()
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) = lock.onBackground()

            override fun onStart(owner: LifecycleOwner) {
                scope.launch { lock.onForeground(settings.current().pinHash != null) }
            }
        })
    }
}
