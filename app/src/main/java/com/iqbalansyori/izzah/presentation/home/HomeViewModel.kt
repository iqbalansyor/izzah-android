package com.iqbalansyori.izzah.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iqbalansyori.izzah.domain.TimeSource
import com.iqbalansyori.izzah.domain.logic.BarKind
import com.iqbalansyori.izzah.domain.logic.DayMath
import com.iqbalansyori.izzah.domain.localNow
import com.iqbalansyori.izzah.domain.model.JournalEntry
import com.iqbalansyori.izzah.domain.model.Prayer
import com.iqbalansyori.izzah.domain.repository.JournalRepository
import com.iqbalansyori.izzah.domain.repository.JourneyRepository
import com.iqbalansyori.izzah.domain.repository.PrayerTimesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toLocalDateTime

class HomeViewModel(
    journey: JourneyRepository,
    journal: JournalRepository,
    prayers: PrayerTimesRepository,
    private val time: TimeSource,
) : ViewModel() {
    private val selected = MutableStateFlow<Int?>(null)

    // the morning/evening dhikr label flips at Asr, so re-evaluate it while the screen is visible
    private val minute = flow {
        while (true) {
            emit(Unit)
            delay(60_000)
        }
    }

    private val base = combine(journey.observeJourney(), journey.observeToday(), prayers.observeToday(), minute) { j, today, times, _ ->
        val done = today.prayers
        val next = Prayer.entries.firstOrNull { it !in done }?.let { it to times?.get(it) }
        val asr = times?.get(Prayer.ASR) ?: FALLBACK_ASR
        HomeState(
            today = j.days.lastOrNull()?.date,
            kept = j.kept,
            target = j.target,
            bars = DayMath.homeBars(j),
            prayersToday = done,
            nextPrayer = next,
            morning = time.localNow().time < asr,
        )
    }

    val state: StateFlow<HomeState> = combine(base, selected, journal.observeEntries()) { s, sel, entries ->
        val bar = sel?.let { s.bars.getOrNull(it) }
        val day = bar?.day
        s.copy(
            selected = if (day != null) sel else null,
            sheetDay = day?.let {
                DayUi(it.number, it.date, bar.kind, it.prayers, it.checkin, entries.noteFor(it.date))
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeState())

    fun onAction(a: HomeAction) {
        when (a) {
            is HomeAction.TapBar -> {
                val bar = state.value.bars.getOrNull(a.index)
                if (bar == null || bar.kind == BarKind.FUTURE) return
                selected.update { if (it == a.index) null else a.index }
            }
            HomeAction.CloseSheet -> selected.value = null
        }
    }

    private fun List<JournalEntry>.noteFor(date: LocalDate): String? =
        filter { it.createdAt.toLocalDateTime(time.zone).date == date }.maxByOrNull { it.createdAt }?.note

    private companion object {
        val FALLBACK_ASR = LocalTime(15, 0)
    }
}
