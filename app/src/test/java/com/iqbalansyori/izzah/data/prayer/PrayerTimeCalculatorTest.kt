package com.iqbalansyori.izzah.data.prayer

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isTrue
import com.iqbalansyori.izzah.domain.model.GeoPoint
import com.iqbalansyori.izzah.domain.model.Prayer
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.junit.jupiter.api.Test
import java.io.File

class PrayerTimeCalculatorTest {
    private val jakarta = GeoPoint(-6.2088, 106.8456, "Jakarta")
    private val t = PrayerTimeCalculator.times(LocalDate(2026, 10, 13), jakarta, TimeZone.of("Asia/Jakarta"))

    private fun between(p: Prayer, from: LocalTime, to: LocalTime) = assertThat(t.getValue(p) in from..to).isTrue()

    @Test
    fun ordered() = assertThat(Prayer.entries.map { t.getValue(it) }.zipWithNext().all { (a, b) -> a < b }).isTrue()

    @Test
    fun `jakarta october ranges`() {
        between(Prayer.FAJR, LocalTime(4, 10), LocalTime(4, 25))
        between(Prayer.DHUHR, LocalTime(11, 35), LocalTime(11, 48))
        between(Prayer.ASR, LocalTime(14, 45), LocalTime(15, 10))
        between(Prayer.MAGHRIB, LocalTime(17, 42), LocalTime(17, 55))
        between(Prayer.ISHA, LocalTime(18, 50), LocalTime(19, 5))
    }

    @Test
    fun `catalog parses`() =
        assertThat(CityCatalog("""[{"label":"Jakarta","lat":-6.2,"lng":106.8}]""").cities.single().label).isEqualTo("Jakarta")

    @Test
    fun `bundled catalog has the provincial capitals`() {
        val cities = CityCatalog(File("src/main/assets/cities.json").readText()).cities
        assertThat(cities.size).isGreaterThan(37)
        assertThat(cities.any { it.label == "Jakarta" }).isTrue()
    }
}
