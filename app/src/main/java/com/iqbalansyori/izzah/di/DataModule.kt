package com.iqbalansyori.izzah.di

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.iqbalansyori.izzah.data.db.IzzahDatabase
import com.iqbalansyori.izzah.data.locale.LocaleFormats
import com.iqbalansyori.izzah.data.prayer.CityCatalog
import com.iqbalansyori.izzah.data.prayer.LocationProvider
import com.iqbalansyori.izzah.data.prayer.PrayerTimesRepositoryImpl
import com.iqbalansyori.izzah.data.repository.DayLogStoreImpl
import com.iqbalansyori.izzah.data.repository.JedaRepositoryImpl
import com.iqbalansyori.izzah.data.repository.JourneyRepositoryImpl
import com.iqbalansyori.izzah.data.repository.JournalRepositoryImpl
import com.iqbalansyori.izzah.data.security.KeystorePassphraseProvider
import com.iqbalansyori.izzah.data.settings.SettingsRepositoryImpl
import com.iqbalansyori.izzah.domain.repository.DayLogStore
import com.iqbalansyori.izzah.domain.repository.JedaRepository
import com.iqbalansyori.izzah.domain.repository.JournalDayStore
import com.iqbalansyori.izzah.domain.repository.JournalRepository
import com.iqbalansyori.izzah.domain.repository.JourneyRepository
import com.iqbalansyori.izzah.domain.repository.PrayerTimesRepository
import com.iqbalansyori.izzah.domain.repository.SettingsRepository
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.binds
import org.koin.dsl.module
import java.util.Locale

private val Context.settingsStore: DataStore<Preferences> by preferencesDataStore(name = "izzah_settings")

val dataModule = module {
    single { KeystorePassphraseProvider(androidContext()) }
    // Room needs the key before it opens; the Keystore round-trip is a few ms, once per process
    single { IzzahDatabase.build(androidContext(), runBlocking { get<KeystorePassphraseProvider>().passphrase() }) }
    single { get<IzzahDatabase>().dayLog() }
    single { get<IzzahDatabase>().journal() }
    single { get<IzzahDatabase>().jeda() }

    single<SettingsRepository> { SettingsRepositoryImpl(androidContext().settingsStore) }
    single<DayLogStore> { DayLogStoreImpl(get()) }
    single { JournalRepositoryImpl(get(), get()) } binds arrayOf(JournalRepository::class, JournalDayStore::class)
    single<JourneyRepository> { JourneyRepositoryImpl(get(), get(), get(), get()) }
    single<JedaRepository> { JedaRepositoryImpl(get(), get()) }
    single<PrayerTimesRepository> { PrayerTimesRepositoryImpl(get(), get(), get()) }
    single { LocationProvider(androidContext()) }
    single { CityCatalog(androidContext().assets.open("cities.json").bufferedReader().use { it.readText() }) }
    single { LocaleFormats { AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault() } }
}
