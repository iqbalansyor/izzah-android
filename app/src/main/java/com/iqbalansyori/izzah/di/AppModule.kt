package com.iqbalansyori.izzah.di

import com.iqbalansyori.izzah.data.SystemTimeSource
import com.iqbalansyori.izzah.domain.TimeSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val appModule = module {
    single<TimeSource> { SystemTimeSource() }
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
}
