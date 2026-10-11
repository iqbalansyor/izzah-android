package com.iqbalansyori.izzah.presentation.home

import com.iqbalansyori.izzah.domain.logic.Bar
import com.iqbalansyori.izzah.domain.model.Prayer
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class HomeState(
    val today: LocalDate? = null,
    val kept: Int = 0,
    val target: Int = 90,
    val bars: List<Bar> = emptyList(),
    val selected: Int? = null,
    val prayersToday: Set<Prayer> = emptySet(),
    // the time is null when no location is set: the card then shows the name only
    val nextPrayer: Pair<Prayer, LocalTime?>? = null,
    val morning: Boolean = true,
    val sheetDay: DayUi? = null,
)
