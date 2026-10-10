package com.iqbalansyori.izzah.domain.logic

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.iqbalansyori.izzah.domain.model.Rawan
import com.iqbalansyori.izzah.domain.model.Slot
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import org.junit.jupiter.api.Test

class SlotScheduleTest {
    private val night = Slot(1, 22 * 60, 60, setOf(DayOfWeek.MONDAY), true)
    private fun at(d: Int, h: Int, m: Int = 0) = LocalDateTime(2026, 10, d, h, m)

    @Test
    fun `active Monday 23h`() = assertThat(SlotSchedule.activeUntil(night, at(12, 23))).isEqualTo(at(13, 1))

    @Test
    fun `active Tuesday 00_30 via Monday start`() = assertThat(SlotSchedule.activeUntil(night, at(13, 0, 30))).isEqualTo(at(13, 1))

    @Test
    fun `inactive exactly at end`() = assertThat(SlotSchedule.activeUntil(night, at(13, 1))).isNull()

    @Test
    fun `inactive Monday 00_30 when Sunday not selected`() = assertThat(SlotSchedule.activeUntil(night, at(12, 0, 30))).isNull()

    @Test
    fun `inactive Monday 21_59`() = assertThat(SlotSchedule.activeUntil(night, at(12, 21, 59))).isNull()

    @Test
    fun `disabled slot never active`() = assertThat(SlotSchedule.activeUntil(night.copy(enabled = false), at(12, 23))).isNull()

    @Test
    fun `same-day slot`() {
        val day = Slot(2, 13 * 60, 15 * 60, SlotSchedule.WEEKDAYS, true)
        assertThat(SlotSchedule.activeUntil(day, at(14, 13))).isEqualTo(at(14, 15))
        assertThat(SlotSchedule.activeUntil(day, at(17, 14))).isNull()
    }

    @Test
    fun `defaults enable night slot only for night rawan`() {
        assertThat(SlotSchedule.defaults(setOf(Rawan.LATE_NIGHT)).map { it.enabled }).containsExactly(true, false)
        assertThat(SlotSchedule.defaults(setOf(Rawan.WEEKEND)).map { it.enabled }).containsExactly(false, false)
        assertThat(SlotSchedule.defaults(emptySet())[0].startMinute).isEqualTo(22 * 60)
    }
}
