package com.iqbalansyori.izzah.data.notify

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.iqbalansyori.izzah.domain.model.Prayer
import com.iqbalansyori.izzah.domain.model.Settings
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.junit.jupiter.api.Test

class ReminderPlannerTest {
    private val times = mapOf(
        Prayer.FAJR to LocalTime(4, 15),
        Prayer.DHUHR to LocalTime(11, 41),
        Prayer.ASR to LocalTime(14, 45),
        Prayer.MAGHRIB to LocalTime(17, 48),
        Prayer.ISHA to LocalTime(18, 58),
    )
    private val timesFor: (LocalDate) -> Map<Prayer, LocalTime>? = { times }
    private val all = Settings(notifPrayer = true, notifDzikir = true, notifCheckin = true)

    private fun at(h: Int, m: Int, d: Int = 13) = LocalDateTime(2026, 10, d, h, m)
    private fun List<PlannedReminder>.of(kind: ReminderKind, prayer: Prayer? = null) = single { it.kind == kind && it.prayer == prayer }

    @Test
    fun `early morning plans tomorrow's fajr, today's dhuhr and tonight's check-in`() {
        val plan = ReminderPlanner.next(at(5, 0), all, timesFor)
        assertThat(plan.of(ReminderKind.PRAYER, Prayer.FAJR).at).isEqualTo(at(4, 15, d = 14))
        assertThat(plan.of(ReminderKind.PRAYER, Prayer.DHUHR).at).isEqualTo(at(11, 41))
        assertThat(plan.of(ReminderKind.CHECKIN).at).isEqualTo(at(21, 30))
    }

    @Test
    fun `dzikir follows fajr and asr by 30 minutes`() {
        val plan = ReminderPlanner.next(at(5, 0), all, timesFor)
        assertThat(plan.of(ReminderKind.DZIKIR_MORNING).at).isEqualTo(at(4, 45, d = 14))
        assertThat(plan.of(ReminderKind.DZIKIR_EVENING).at).isEqualTo(at(15, 15))
    }

    @Test
    fun `after the check-in time it moves to tomorrow`() {
        val plan = ReminderPlanner.next(at(22, 0), all, timesFor)
        assertThat(plan.of(ReminderKind.CHECKIN).at).isEqualTo(at(21, 30, d = 14))
    }

    @Test
    fun `request codes follow the scheme`() {
        val plan = ReminderPlanner.next(at(5, 0), all, timesFor)
        assertThat(plan.of(ReminderKind.PRAYER, Prayer.ASR).requestCode).isEqualTo(102)
        assertThat(plan.of(ReminderKind.DZIKIR_MORNING).requestCode).isEqualTo(200)
        assertThat(plan.of(ReminderKind.DZIKIR_EVENING).requestCode).isEqualTo(201)
        assertThat(plan.of(ReminderKind.CHECKIN).requestCode).isEqualTo(300)
    }

    @Test
    fun `everything disabled plans nothing`() {
        val off = Settings(notifPrayer = false, notifDzikir = false, notifCheckin = false)
        assertThat(ReminderPlanner.next(at(5, 0), off, timesFor)).isEmpty()
    }

    @Test
    fun `without a location only the check-in remains`() {
        val plan = ReminderPlanner.next(at(5, 0), all) { null }
        assertThat(plan).hasSize(1)
        assertThat(plan.single().kind).isEqualTo(ReminderKind.CHECKIN)
    }

    @Test
    fun `each toggle controls its own reminders`() {
        val onlyDzikir = Settings(notifPrayer = false, notifDzikir = true, notifCheckin = false)
        val plan = ReminderPlanner.next(at(5, 0), onlyDzikir, timesFor)
        assertThat(plan.map { it.kind }.toSet()).isEqualTo(setOf(ReminderKind.DZIKIR_MORNING, ReminderKind.DZIKIR_EVENING))
    }
}
