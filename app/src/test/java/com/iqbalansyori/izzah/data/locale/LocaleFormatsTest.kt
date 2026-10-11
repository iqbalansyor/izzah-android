package com.iqbalansyori.izzah.data.locale

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.jupiter.api.Test
import java.util.Locale

class LocaleFormatsTest {
    private val id = LocaleFormats { Locale.forLanguageTag("in") }
    private val en = LocaleFormats { Locale.ENGLISH }
    private fun norm(s: String) = s.replace(' ', ' ').replace(' ', ' ')

    @Test
    fun time() {
        assertThat(id.time(LocalTime(4, 12))).isEqualTo("04.12")
        assertThat(norm(en.time(LocalTime(21, 30)))).isEqualTo("9:30 PM")
    }

    @Test
    fun weekday() {
        assertThat(id.weekday(LocalDate(2026, 10, 13))).isEqualTo("Selasa")
        assertThat(en.weekday(LocalDate(2026, 10, 13))).isEqualTo("Tuesday")
    }

    @Test
    fun clock() = assertThat(id.clock(LocalTime(9, 5, 7))).isEqualTo("09:05:07")

    @Test
    fun longDate() {
        assertThat(id.longDate(LocalDate(2026, 10, 13))).isEqualTo("Selasa, 13 Oktober")
        assertThat(en.longDate(LocalDate(2026, 10, 13))).isEqualTo("Tuesday, October 13")
    }

    @Test
    fun hijri() {
        assertThat(HijriFormatter.toHijri(LocalDate(2026, 10, 13))).isEqualTo(HijriDate(2, 5, 1448))
        assertThat(HijriFormatter.toHijri(LocalDate(2026, 3, 20))).isEqualTo(HijriDate(1, 10, 1447))
    }
}
