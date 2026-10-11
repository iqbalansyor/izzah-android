package com.iqbalansyori.izzah.di

import com.iqbalansyori.izzah.data.SystemTimeSource
import com.iqbalansyori.izzah.data.lock.AppLockManager
import com.iqbalansyori.izzah.domain.TimeSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val appModule = module {
    single<TimeSource> { SystemTimeSource() }
    single { AppLockManager(get()) }
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
}
