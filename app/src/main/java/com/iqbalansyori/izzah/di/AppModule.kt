package com.iqbalansyori.izzah.di

import com.iqbalansyori.izzah.data.SystemTimeSource
import com.iqbalansyori.izzah.data.blocking.PermissionChecker
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
    single<LocaleSetter> { AppCompatLocaleSetter() }
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
}
