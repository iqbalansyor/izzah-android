package com.iqbalansyori.izzah.domain.logic

import com.iqbalansyori.izzah.domain.model.DayLog
import com.iqbalansyori.izzah.domain.model.Prayer
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlin.math.ceil
import kotlin.math.roundToInt

data class DayStat(
    val date: LocalDate,
    val number: Int,
    val prayersDone: Int,
    val prayers: Set<Prayer>,
    val checkin: Boolean,
    val slip: Boolean,
    val isToday: Boolean,
)

enum class BarKind { KEPT, SLIP, TODAY, FUTURE }

data class Bar(val heightDp: Int, val kind: BarKind, val day: DayStat?)

data class Journey(val days: List<DayStat>, val target: Int) {
    val dayCount get() = days.size
    val slips get() = days.count { it.slip }
    val kept get() = dayCount - slips
    val percent get() = minOf(100, (kept * 100.0 / target).roundToInt())
    val prayerPercent get() = if (days.isEmpty()) 0 else (days.sumOf { it.prayersDone } * 100.0 / (days.size * 5)).roundToInt()
    val checkinDays get() = days.count { it.checkin }
}

object DayMath {
    private const val HOME_PAST = 14
    private const val HOME_TOTAL = 30

    fun journey(
        start: LocalDate,
        today: LocalDate,
        logs: Map<LocalDate, DayLog>,
        journalDates: Set<LocalDate>,
        target: Int,
    ): Journey {
        val end = if (today < start) start else today
        val count = start.daysUntil(end) + 1
        val days = (0 until count).map { i ->
            val date = start.plus(i, DateTimeUnit.DAY)
            val log = logs[date]
            val prayers = log?.prayers.orEmpty()
            DayStat(
                date = date,
                number = i + 1,
                prayersDone = prayers.size,
                prayers = prayers,
                checkin = prayers.isNotEmpty() || date in journalDates,
                slip = log?.slip == true,
                isToday = date == end,
            )
        }
        return Journey(days, target)
    }

    fun barHeight(d: DayStat, scale: Float): Int =
        ((if (d.slip) 18 else 22 + d.prayersDone * 11 + if (d.checkin) 15 else 0) * scale).roundToInt()

    private fun kind(d: DayStat) = when {
        d.slip -> BarKind.SLIP
        d.isToday -> BarKind.TODAY
        else -> BarKind.KEPT
    }

    fun homeBars(j: Journey): List<Bar> {
        val past = j.days.takeLast(HOME_PAST).map { Bar(barHeight(it, 1f), kind(it), it) }
        return past + List(HOME_TOTAL - past.size) { Bar(40, BarKind.FUTURE, null) }
    }

    fun gridCellCount(dayCount: Int, target: Int): Int = ceil(maxOf(dayCount, target) / 15.0).toInt() * 15

    fun gridBars(j: Journey): List<Bar> {
        val past = j.days.map { Bar(barHeight(it, .55f), kind(it), it) }
        return past + List(gridCellCount(j.dayCount, j.target) - past.size) { Bar(22, BarKind.FUTURE, null) }
    }
}
