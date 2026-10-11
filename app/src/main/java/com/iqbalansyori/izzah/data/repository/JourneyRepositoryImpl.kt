package com.iqbalansyori.izzah.data.repository

import com.iqbalansyori.izzah.domain.TimeSource
import com.iqbalansyori.izzah.domain.logic.DayMath
import com.iqbalansyori.izzah.domain.logic.Journey
import com.iqbalansyori.izzah.domain.model.DayLog
import com.iqbalansyori.izzah.domain.model.Prayer
import com.iqbalansyori.izzah.domain.model.Trigger
import com.iqbalansyori.izzah.domain.repository.DayLogStore
import com.iqbalansyori.izzah.domain.repository.JourneyRepository
import com.iqbalansyori.izzah.domain.repository.JournalDayStore
import com.iqbalansyori.izzah.domain.repository.SettingsRepository
import com.iqbalansyori.izzah.domain.today
import com.iqbalansyori.izzah.domain.todayFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class JourneyRepositoryImpl(
    private val store: DayLogStore,
    private val journalDays: JournalDayStore,
    private val settings: SettingsRepository,
    private val time: TimeSource,
) : JourneyRepository {

    override fun observeJourney(): Flow<Journey> =
        combine(time.todayFlow(), store.observeAll(), journalDays.observeDays(), settings.settings) { today, logs, jd, s ->
            DayMath.journey(s.startDate ?: today, today, logs.associateBy { it.date }, jd, s.target)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeToday(): Flow<DayLog> = time.todayFlow().flatMapLatest { today ->
        store.observeAll().map { logs -> logs.firstOrNull { it.date == today } ?: DayLog(today) }
    }

    override suspend fun togglePrayer(p: Prayer) {
        val today = time.today()
        val log = store.get(today) ?: DayLog(today)
        store.upsert(log.copy(prayers = if (p in log.prayers) log.prayers - p else log.prayers + p))
    }

    override suspend fun logSlip(triggers: Set<Trigger>) {
        val today = time.today()
        val log = store.get(today) ?: DayLog(today)
        store.upsert(log.copy(slip = true, triggers = log.triggers + triggers))
    }
}
