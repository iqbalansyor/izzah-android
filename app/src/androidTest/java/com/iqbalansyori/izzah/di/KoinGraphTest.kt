package com.iqbalansyori.izzah.di

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import assertk.assertThat
import assertk.assertions.isNotNull
import com.iqbalansyori.izzah.data.prayer.CityCatalog
import com.iqbalansyori.izzah.domain.repository.JedaRepository
import com.iqbalansyori.izzah.domain.repository.JournalRepository
import com.iqbalansyori.izzah.domain.repository.JourneyRepository
import com.iqbalansyori.izzah.domain.repository.PrayerTimesRepository
import com.iqbalansyori.izzah.domain.repository.SettingsRepository
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication

@RunWith(AndroidJUnit4::class)
class KoinGraphTest {
    @Test
    fun graphResolves() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        System.loadLibrary("sqlcipher")
        val koin = koinApplication {
            androidContext(ctx)
            modules(appModule, dataModule, viewModelModule)
        }.koin
        assertThat(koin.get<JourneyRepository>()).isNotNull()
        assertThat(koin.get<JournalRepository>()).isNotNull()
        assertThat(koin.get<JedaRepository>()).isNotNull()
        assertThat(koin.get<SettingsRepository>()).isNotNull()
        assertThat(koin.get<PrayerTimesRepository>()).isNotNull()
        assertThat(koin.get<CityCatalog>().cities.size).isNotNull()
    }
}
