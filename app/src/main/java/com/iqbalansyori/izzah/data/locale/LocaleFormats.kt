package com.iqbalansyori.izzah.data.locale

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

class LocaleFormats(private val locale: () -> Locale) {
    // legacy "in" and modern "id" both mean Indonesian; normalise so CLDR data is always found
    private fun loc(): Locale = locale().let { if (it.language == "in") Locale.forLanguageTag("id") else it }
    private fun isIndonesian() = loc().language == "id"

    fun time(t: LocalTime): String =
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(loc()).format(t.toJavaLocalTime())

    fun clock(t: LocalTime): String = "%02d:%02d:%02d".format(t.hour, t.minute, t.second)

    fun weekday(d: LocalDate): String = d.toJavaLocalDate().dayOfWeek.getDisplayName(TextStyle.FULL, loc())

    fun longDate(d: LocalDate): String {
        val pattern = if (isIndonesian()) "EEEE, d MMMM" else "EEEE, MMMM d"
        return DateTimeFormatter.ofPattern(pattern, loc()).format(d.toJavaLocalDate())
    }

    fun minuteOfDay(m: Int): String = time(LocalTime(m / 60 % 24, m % 60))
}
