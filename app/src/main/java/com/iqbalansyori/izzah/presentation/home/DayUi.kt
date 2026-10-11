package com.iqbalansyori.izzah.presentation.home

import com.iqbalansyori.izzah.domain.logic.BarKind
import com.iqbalansyori.izzah.domain.logic.DayStat
import com.iqbalansyori.izzah.domain.model.JournalEntry
import com.iqbalansyori.izzah.domain.model.Prayer
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// note == null means the screen picks a day_note_* fallback
data class DayUi(
    val number: Int,
    val date: LocalDate,
    val status: BarKind,
    val prayers: Set<Prayer>,
    val checkin: Boolean,
    val note: String?,
)

fun List<JournalEntry>.noteFor(date: LocalDate, zone: TimeZone): String? =
    filter { it.createdAt.toLocalDateTime(zone).date == date }.maxByOrNull { it.createdAt }?.note

fun DayStat.toUi(kind: BarKind, entries: List<JournalEntry>, zone: TimeZone) =
    DayUi(number, date, kind, prayers, checkin, entries.noteFor(date, zone))
