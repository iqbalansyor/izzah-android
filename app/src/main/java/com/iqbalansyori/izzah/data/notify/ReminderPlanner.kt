package com.iqbalansyori.izzah.data.notify

import com.iqbalansyori.izzah.domain.logic.ReminderDefaults
import com.iqbalansyori.izzah.domain.model.Prayer
import com.iqbalansyori.izzah.domain.model.Settings
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus

enum class ReminderKind { PRAYER, DZIKIR_MORNING, DZIKIR_EVENING, CHECKIN }

data class PlannedReminder(val kind: ReminderKind, val prayer: Prayer?, val at: LocalDateTime, val requestCode: Int)

object ReminderPlanner {
    private const val DZIKIR_DELAY_MINUTES = 30

    fun next(now: LocalDateTime, s: Settings, timesFor: (LocalDate) -> Map<Prayer, LocalTime>?): List<PlannedReminder> {
        val plan = mutableListOf<PlannedReminder>()

        // the next occurrence of a daily clock time: today if it is still ahead, else tomorrow
        fun nextAt(timeOf: (LocalDate) -> LocalTime?): LocalDateTime? {
            val today = now.date
            val tomorrow = today.plus(1, DateTimeUnit.DAY)
            val first = timeOf(today)?.let { LocalDateTime(today, it) }
            if (first != null && first > now) return first
            return timeOf(tomorrow)?.let { LocalDateTime(tomorrow, it) }
        }

        fun prayerTime(p: Prayer, plusMinutes: Int = 0): (LocalDate) -> LocalTime? = { d ->
            timesFor(d)?.get(p)?.let { t ->
                val total = t.hour * 60 + t.minute + plusMinutes
                LocalTime(total / 60 % 24, total % 60)
            }
        }

        if (s.notifPrayer) {
            Prayer.entries.forEach { p ->
                nextAt(prayerTime(p))?.let { plan += PlannedReminder(ReminderKind.PRAYER, p, it, 100 + p.ordinal) }
            }
        }
        if (s.notifDzikir) {
            nextAt(prayerTime(Prayer.FAJR, DZIKIR_DELAY_MINUTES))?.let { plan += PlannedReminder(ReminderKind.DZIKIR_MORNING, null, it, 200) }
            nextAt(prayerTime(Prayer.ASR, DZIKIR_DELAY_MINUTES))?.let { plan += PlannedReminder(ReminderKind.DZIKIR_EVENING, null, it, 201) }
        }
        if (s.notifCheckin) {
            val at = LocalTime(ReminderDefaults.CHECKIN_MINUTE / 60, ReminderDefaults.CHECKIN_MINUTE % 60)
            nextAt { at }?.let { plan += PlannedReminder(ReminderKind.CHECKIN, null, it, 300) }
        }
        return plan
    }
}
