package com.iqbalansyori.izzah.presentation.progress

import com.iqbalansyori.izzah.domain.logic.Bar
import com.iqbalansyori.izzah.domain.logic.InsightWindow
import com.iqbalansyori.izzah.presentation.home.DayUi

data class ProgressState(
    val kept: Int = 0,
    val target: Int = 90,
    val dayCount: Int = 0,
    val slips: Int = 0,
    val percent: Int = 0,
    val grid: List<Bar> = emptyList(),
    val selectedIndex: Int? = null,
    val selected: DayUi? = null,
    val prayerPercent: Int = 0,
    val checkinDays: Int = 0,
    val jedaCompleted: Int = 0,
    val insight: InsightWindow? = null,
    val reminderOn: Boolean = false,
)
