package com.iqbalansyori.izzah.presentation.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iqbalansyori.izzah.domain.TimeSource
import com.iqbalansyori.izzah.domain.logic.BarKind
import com.iqbalansyori.izzah.domain.logic.DayMath
import com.iqbalansyori.izzah.domain.logic.InsightCalculator
import com.iqbalansyori.izzah.domain.repository.JedaRepository
import com.iqbalansyori.izzah.domain.repository.JournalRepository
import com.iqbalansyori.izzah.domain.repository.JourneyRepository
import com.iqbalansyori.izzah.domain.repository.SettingsRepository
import com.iqbalansyori.izzah.presentation.home.toUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDateTime

class ProgressViewModel(
    journey: JourneyRepository,
    jeda: JedaRepository,
    journal: JournalRepository,
    private val settings: SettingsRepository,
    private val time: TimeSource,
) : ViewModel() {
    private val selected = MutableStateFlow<Int?>(null)

    private val base = combine(journey.observeJourney(), jeda.observeEvents(), settings.settings) { j, events, s ->
        val done = events.filter { it.completed }
        ProgressState(
            kept = j.kept,
            target = j.target,
            dayCount = j.dayCount,
            slips = j.slips,
            percent = j.percent,
            grid = DayMath.gridBars(j),
            prayerPercent = j.prayerPercent,
            checkinDays = j.checkinDays,
            jedaCompleted = done.size,
            insight = InsightCalculator.busiestWindow(done.map { it.startedAt.toLocalDateTime(time.zone) }),
            reminderOn = s.notifCheckin,
        )
    }

    val state: StateFlow<ProgressState> = combine(base, selected, journal.observeEntries()) { s, sel, entries ->
        val bar = sel?.let { s.grid.getOrNull(it) }
        val day = bar?.day
        s.copy(selectedIndex = if (day != null) sel else null, selected = day?.toUi(bar.kind, entries, time.zone))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressState())

    fun onAction(a: ProgressAction) {
        when (a) {
            is ProgressAction.TapCell -> {
                val bar = state.value.grid.getOrNull(a.index)
                if (bar == null || bar.kind == BarKind.FUTURE) return
                selected.update { if (it == a.index) null else a.index }
            }
            ProgressAction.ToggleReminder -> viewModelScope.launch { settings.update { it.copy(notifCheckin = !it.notifCheckin) } }
        }
    }
}
