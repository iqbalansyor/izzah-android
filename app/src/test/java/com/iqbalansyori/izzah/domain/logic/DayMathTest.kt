package com.iqbalansyori.izzah.domain.logic

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.iqbalansyori.izzah.domain.model.DayLog
import com.iqbalansyori.izzah.domain.model.Prayer
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.junit.jupiter.api.Test

class DayMathTest {
    private val start = LocalDate(2026, 9, 1)
    private fun logs(vararg l: DayLog) = l.associateBy { it.date }

    @Test
    fun `first day, nothing logged, kept is 1`() {
        val j = DayMath.journey(start, start, emptyMap(), emptySet(), 90)
        assertThat(j.dayCount).isEqualTo(1)
        assertThat(j.kept).isEqualTo(1)
        assertThat(j.percent).isEqualTo(1)
    }

    @Test
    fun `slip on day one gives kept 0`() {
        val j = DayMath.journey(start, start, logs(DayLog(start, slip = true)), emptySet(), 7)
        assertThat(j.kept).isEqualTo(0)
        assertThat(j.slips).isEqualTo(1)
    }

    @Test
    fun `kept equals days minus slips and slips never reset`() {
        val today = LocalDate(2026, 10, 15)
        val slipDays = listOf(9, 21, 38).map { start.plus(it, DateTimeUnit.DAY) }
        val j = DayMath.journey(start, today, slipDays.associateWith { DayLog(it, slip = true) }, emptySet(), 90)
        assertThat(j.dayCount).isEqualTo(45)
        assertThat(j.kept).isEqualTo(42)
        assertThat(j.percent).isEqualTo(47)
    }

    @Test
    fun `clock before start is treated as day one`() {
        val j = DayMath.journey(start, start.minus(3, DateTimeUnit.DAY), emptyMap(), emptySet(), 30)
        assertThat(j.dayCount).isEqualTo(1)
    }

    @Test
    fun `percent caps at 100`() {
        val j = DayMath.journey(start, start.plus(20, DateTimeUnit.DAY), emptyMap(), emptySet(), 7)
        assertThat(j.percent).isEqualTo(100)
    }

    @Test
    fun `checkin from any prayer or a journal entry`() {
        val d2 = start.plus(1, DateTimeUnit.DAY)
        val j = DayMath.journey(start, d2, logs(DayLog(start, prayers = setOf(Prayer.FAJR))), setOf(d2), 90)
        assertThat(j.days.map { it.checkin }).containsExactly(true, true)
        assertThat(j.checkinDays).isEqualTo(2)
    }

    @Test
    fun `bar height formula matches prototype`() {
        val base = DayStat(start, 1, 0, emptySet(), false, false, false)
        assertThat(DayMath.barHeight(base.copy(slip = true, prayersDone = 5), 1f)).isEqualTo(18)
        assertThat(DayMath.barHeight(base.copy(prayersDone = 3, checkin = true), 1f)).isEqualTo(70)
        assertThat(DayMath.barHeight(base.copy(prayersDone = 5, checkin = true), 1f)).isEqualTo(92)
        assertThat(DayMath.barHeight(base.copy(prayersDone = 5, checkin = true), .55f)).isEqualTo(51)
    }

    @Test
    fun `home bars show last 14 days then futures to 30`() {
        val j = DayMath.journey(start, start.plus(44, DateTimeUnit.DAY), emptyMap(), emptySet(), 90)
        val bars = DayMath.homeBars(j)
        assertThat(bars.size).isEqualTo(30)
        assertThat(bars.count { it.kind != BarKind.FUTURE }).isEqualTo(14)
        assertThat(bars[13].kind).isEqualTo(BarKind.TODAY)
        assertThat(bars.last().heightDp).isEqualTo(40)
    }

    @Test
    fun `home bars on day 3 show 3 past bars`() {
        val bars = DayMath.homeBars(DayMath.journey(start, start.plus(2, DateTimeUnit.DAY), emptyMap(), emptySet(), 90))
        assertThat(bars.count { it.kind != BarKind.FUTURE }).isEqualTo(3)
        assertThat(bars.size).isEqualTo(30)
    }

    @Test
    fun `grid cell count rounds up to multiples of 15`() {
        assertThat(DayMath.gridCellCount(1, 7)).isEqualTo(15)
        assertThat(DayMath.gridCellCount(45, 40)).isEqualTo(45)
        assertThat(DayMath.gridCellCount(46, 40)).isEqualTo(60)
        assertThat(DayMath.gridCellCount(10, 90)).isEqualTo(90)
    }

    @Test
    fun `prayer percent`() {
        val d2 = start.plus(1, DateTimeUnit.DAY)
        val j = DayMath.journey(
            start, d2,
            logs(DayLog(start, prayers = Prayer.entries.toSet()), DayLog(d2, prayers = setOf(Prayer.FAJR))),
            emptySet(), 90,
        )
        assertThat(j.prayerPercent).isEqualTo(60)
    }
}
