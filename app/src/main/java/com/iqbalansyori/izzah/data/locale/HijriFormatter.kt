package com.iqbalansyori.izzah.data.locale

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

data class HijriDate(val day: Int, val month: Int, val year: Int)

object HijriFormatter {
    fun toHijri(d: LocalDate): HijriDate {
        val h = HijrahDate.from(d.toJavaLocalDate())
        return HijriDate(h.get(ChronoField.DAY_OF_MONTH), h.get(ChronoField.MONTH_OF_YEAR), h.get(ChronoField.YEAR))
    }
}
