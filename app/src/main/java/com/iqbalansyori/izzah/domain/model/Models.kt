package com.iqbalansyori.izzah.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

data class DayLog(
    val date: LocalDate,
    val prayers: Set<Prayer> = emptySet(),
    val slip: Boolean = false,
    val triggers: Set<Trigger> = emptySet(),
)

data class JournalEntry(val id: Long = 0, val createdAt: Instant, val mood: Mood?, val note: String)

data class JedaEvent(val id: Long = 0, val startedAt: Instant, val completed: Boolean)

data class Slot(val id: Int, val startMinute: Int, val endMinute: Int, val days: Set<DayOfWeek>, val enabled: Boolean)
