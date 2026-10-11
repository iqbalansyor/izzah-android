package com.iqbalansyori.izzah.domain.repository

import com.iqbalansyori.izzah.domain.logic.Journey
import com.iqbalansyori.izzah.domain.model.DayLog
import com.iqbalansyori.izzah.domain.model.GeoPoint
import com.iqbalansyori.izzah.domain.model.JedaEvent
import com.iqbalansyori.izzah.domain.model.JournalEntry
import com.iqbalansyori.izzah.domain.model.Mood
import com.iqbalansyori.izzah.domain.model.Prayer
import com.iqbalansyori.izzah.domain.model.Settings
import com.iqbalansyori.izzah.domain.model.Trigger
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

interface DayLogStore {
    fun observeAll(): Flow<List<DayLog>>
    suspend fun get(d: LocalDate): DayLog?
    suspend fun upsert(d: DayLog)
}

interface JournalDayStore {
    fun observeDays(): Flow<Set<LocalDate>>
}

interface SettingsRepository {
    val settings: Flow<Settings>
    suspend fun current(): Settings
    suspend fun update(transform: (Settings) -> Settings)
    suspend fun clear()
}

interface JourneyRepository {
    fun observeJourney(): Flow<Journey>
    fun observeToday(): Flow<DayLog>
    suspend fun togglePrayer(p: Prayer)
    suspend fun logSlip(triggers: Set<Trigger>)
}

interface JournalRepository {
    fun observeEntries(): Flow<List<JournalEntry>>
    suspend fun add(mood: Mood?, note: String)
}

interface JedaRepository {
    fun observeEvents(): Flow<List<JedaEvent>>
    suspend fun start(): Long
    suspend fun complete(id: Long)
}

interface PrayerTimesRepository {
    fun observeToday(): Flow<Map<Prayer, LocalTime>?>
    fun timesFor(date: LocalDate): Map<Prayer, LocalTime>?
    suspend fun setLocation(p: GeoPoint)
}
