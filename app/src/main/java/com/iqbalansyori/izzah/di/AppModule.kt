package com.iqbalansyori.izzah.di

import com.iqbalansyori.izzah.data.SystemTimeSource
import com.iqbalansyori.izzah.data.blocking.ForegroundAppMonitor
import com.iqbalansyori.izzah.data.blocking.KeepRunningGuide
import com.iqbalansyori.izzah.data.blocking.OverlayController
import com.iqbalansyori.izzah.data.blocking.PermissionChecker
import com.iqbalansyori.izzah.data.repository.InstalledAppsRepositoryImpl
import com.iqbalansyori.izzah.domain.repository.InstalledAppsRepository
import com.iqbalansyori.izzah.data.disguise.DisguiseManager
import com.iqbalansyori.izzah.data.lock.AppLockManager
import com.iqbalansyori.izzah.data.locale.AppCompatLocaleSetter
import com.iqbalansyori.izzah.data.locale.LocaleSetter
import com.iqbalansyori.izzah.data.security.BiometricChecker
import com.iqbalansyori.izzah.domain.TimeSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single<TimeSource> { SystemTimeSource() }
    single { AppLockManager(get()) }
    single { PermissionChecker(androidContext()) }
    single { BiometricChecker(androidContext()) }
    single { DisguiseManager(androidContext()) }
    single { KeepRunningGuide(androidContext()) }
    single { ForegroundAppMonitor(androidContext()) }
    factory { OverlayController(androidContext()) }
    single<InstalledAppsRepository> { InstalledAppsRepositoryImpl(androidContext()) }
    single<LocaleSetter> { AppCompatLocaleSetter(androidContext()) }
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
}
