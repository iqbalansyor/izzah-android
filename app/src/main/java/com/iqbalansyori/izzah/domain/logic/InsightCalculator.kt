package com.iqbalansyori.izzah.domain.logic

import kotlinx.datetime.LocalDateTime

data class InsightWindow(val startHour: Int, val endHour: Int)

object InsightCalculator {
    const val MIN_EVENTS = 5

    fun busiestWindow(starts: List<LocalDateTime>): InsightWindow? {
        if (starts.size < MIN_EVENTS) return null
        val best = starts.groupingBy { it.hour / 2 * 2 }.eachCount().entries
            .sortedWith(compareByDescending<Map.Entry<Int, Int>> { it.value }.thenBy { it.key })
            .first().key
        return InsightWindow(best, (best + 2) % 24)
    }
}
