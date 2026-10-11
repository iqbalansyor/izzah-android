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
