package com.iqbalansyori.izzah.presentation.home

import com.iqbalansyori.izzah.domain.logic.BarKind
import com.iqbalansyori.izzah.domain.model.Prayer
import kotlinx.datetime.LocalDate

// note == null means the screen picks a day_note_* fallback
data class DayUi(
    val number: Int,
    val date: LocalDate,
    val status: BarKind,
    val prayers: Set<Prayer>,
    val checkin: Boolean,
    val note: String?,
)
