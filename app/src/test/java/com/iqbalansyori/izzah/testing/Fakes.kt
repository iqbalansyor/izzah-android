package com.iqbalansyori.izzah.testing

import com.iqbalansyori.izzah.domain.TimeSource
import com.iqbalansyori.izzah.domain.model.DayLog
import com.iqbalansyori.izzah.domain.model.Settings
import com.iqbalansyori.izzah.domain.repository.DayLogStore
import com.iqbalansyori.izzah.domain.repository.JournalDayStore
import com.iqbalansyori.izzah.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.Duration

class FakeTimeSource(private var instant: Instant, override val zone: TimeZone) : TimeSource {
    override fun now(): Instant = instant
    fun advanceTo(i: Instant) { instant = i }
    fun advanceBy(d: Duration) { instant += d }
}

class FakeSettingsRepository(initial: Settings = Settings()) : SettingsRepository {
    private val state = MutableStateFlow(initial)
    override val settings: Flow<Settings> = state
    override suspend fun current(): Settings = state.value
    override suspend fun update(transform: (Settings) -> Settings) { state.value = transform(state.value) }
    override suspend fun clear() { state.value = Settings() }
}

class FakeDayLogStore : DayLogStore {
    private val state = MutableStateFlow<Map<LocalDate, DayLog>>(emptyMap())
    override fun observeAll(): Flow<List<DayLog>> = state.map { it.values.toList() }
    override suspend fun get(d: LocalDate): DayLog? = state.value[d]
    override suspend fun upsert(d: DayLog) { state.value = state.value + (d.date to d) }
}

class FakeJournalDayStore(days: Set<LocalDate> = emptySet()) : JournalDayStore {
    private val state = MutableStateFlow(days)
    override fun observeDays(): Flow<Set<LocalDate>> = state
}

class FakeJourneyRepository(journey: com.iqbalansyori.izzah.domain.logic.Journey, today: DayLog = DayLog(LocalDate(2026, 10, 1))) :
    com.iqbalansyori.izzah.domain.repository.JourneyRepository {
    val journeyFlow = MutableStateFlow(journey)
    val todayFlow = MutableStateFlow(today)
    override fun observeJourney(): Flow<com.iqbalansyori.izzah.domain.logic.Journey> = journeyFlow
    override fun observeToday(): Flow<DayLog> = todayFlow
    override suspend fun togglePrayer(p: com.iqbalansyori.izzah.domain.model.Prayer) {
        todayFlow.value = todayFlow.value.copy(prayers = todayFlow.value.prayers.let { if (p in it) it - p else it + p })
    }
    override suspend fun logSlip(triggers: Set<com.iqbalansyori.izzah.domain.model.Trigger>) {
        todayFlow.value = todayFlow.value.copy(slip = true, triggers = todayFlow.value.triggers + triggers)
    }
}

class FakeJournalRepository(entries: List<com.iqbalansyori.izzah.domain.model.JournalEntry> = emptyList()) :
    com.iqbalansyori.izzah.domain.repository.JournalRepository {
    val entriesFlow = MutableStateFlow(entries)
    override fun observeEntries(): Flow<List<com.iqbalansyori.izzah.domain.model.JournalEntry>> = entriesFlow
    override suspend fun add(mood: com.iqbalansyori.izzah.domain.model.Mood?, note: String) {
        val id = (entriesFlow.value.maxOfOrNull { it.id } ?: 0L) + 1
        entriesFlow.value = entriesFlow.value + com.iqbalansyori.izzah.domain.model.JournalEntry(id, Instant.fromEpochMilliseconds(id * 1000), mood, note)
    }
}

class FakePrayerTimesRepository(times: Map<com.iqbalansyori.izzah.domain.model.Prayer, kotlinx.datetime.LocalTime>? = null) :
    com.iqbalansyori.izzah.domain.repository.PrayerTimesRepository {
    val timesFlow = MutableStateFlow(times)
    override fun observeToday(): Flow<Map<com.iqbalansyori.izzah.domain.model.Prayer, kotlinx.datetime.LocalTime>?> = timesFlow
    override fun timesFor(date: LocalDate) = timesFlow.value
    var lastLocation: com.iqbalansyori.izzah.domain.model.GeoPoint? = null
    override suspend fun setLocation(p: com.iqbalansyori.izzah.domain.model.GeoPoint) {
        lastLocation = p
    }
}

class FakeJedaRepository(events: List<com.iqbalansyori.izzah.domain.model.JedaEvent> = emptyList()) :
    com.iqbalansyori.izzah.domain.repository.JedaRepository {
    val eventsFlow = MutableStateFlow(events)
    override fun observeEvents(): Flow<List<com.iqbalansyori.izzah.domain.model.JedaEvent>> = eventsFlow
    override suspend fun start(): Long {
        val id = (eventsFlow.value.maxOfOrNull { it.id } ?: 0L) + 1
        eventsFlow.value = eventsFlow.value + com.iqbalansyori.izzah.domain.model.JedaEvent(id, kotlinx.datetime.Instant.fromEpochMilliseconds(0), false)
        return id
    }
    override suspend fun complete(id: Long) {
        eventsFlow.value = eventsFlow.value.map { if (it.id == id) it.copy(completed = true) else it }
    }
}
