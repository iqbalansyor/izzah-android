package com.iqbalansyori.izzah.domain.logic

import com.iqbalansyori.izzah.domain.model.Rawan
import com.iqbalansyori.izzah.domain.model.Slot
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus

object SlotSchedule {
    val EVERY_DAY: Set<DayOfWeek> = DayOfWeek.entries.toSet()
    val WEEKDAYS: Set<DayOfWeek> = setOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
    )

    fun activeUntil(slot: Slot, now: LocalDateTime): LocalDateTime? {
        if (!slot.enabled) return null
        val m = now.hour * 60 + now.minute
        val endTime = LocalTime(slot.endMinute / 60, slot.endMinute % 60)
        if (slot.endMinute > slot.startMinute) {
            return if (now.dayOfWeek in slot.days && m >= slot.startMinute && m < slot.endMinute) {
                LocalDateTime(now.date, endTime)
            } else null
        }
        // crosses midnight: `days` names the day the slot starts on
        if (m >= slot.startMinute && now.dayOfWeek in slot.days) {
            return LocalDateTime(now.date.plus(1, DateTimeUnit.DAY), endTime)
        }
        val yesterday = now.date.minus(1, DateTimeUnit.DAY)
        if (m < slot.endMinute && yesterday.dayOfWeek in slot.days) return LocalDateTime(now.date, endTime)
        return null
    }

    fun activeUntil(slots: List<Slot>, now: LocalDateTime): Pair<Slot, LocalDateTime>? =
        slots.mapNotNull { s -> activeUntil(s, now)?.let { s to it } }.maxByOrNull { it.second }

    fun defaults(rawan: Set<Rawan>): List<Slot> = listOf(
        Slot(1, 22 * 60, 60, EVERY_DAY, enabled = Rawan.LATE_NIGHT in rawan || Rawan.DAWN in rawan),
        Slot(2, 13 * 60, 15 * 60, WEEKDAYS, enabled = false),
    )
}
