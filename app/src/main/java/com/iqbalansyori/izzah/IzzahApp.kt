package com.iqbalansyori.izzah

import android.app.Application
import com.iqbalansyori.izzah.di.appModule
import com.iqbalansyori.izzah.di.dataModule
import com.iqbalansyori.izzah.di.viewModelModule
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
    }
}
