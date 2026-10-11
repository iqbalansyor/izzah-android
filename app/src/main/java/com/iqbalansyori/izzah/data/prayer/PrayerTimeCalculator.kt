package com.iqbalansyori.izzah.data.prayer

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import com.iqbalansyori.izzah.domain.model.GeoPoint
import com.iqbalansyori.izzah.domain.model.Prayer
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Date

object PrayerTimeCalculator {
    // Kemenag: Fajr 20°, Isha 18°, +2 min ihtiyat on every prayer
    private fun params() = CalculationMethod.OTHER.parameters.apply {
        fajrAngle = 20.0
        ishaAngle = 18.0
        madhab = Madhab.SHAFI
        adjustments.fajr = 2
        adjustments.dhuhr = 2
        adjustments.asr = 2
        adjustments.maghrib = 2
        adjustments.isha = 2
    }

    fun times(date: LocalDate, at: GeoPoint, zone: TimeZone): Map<Prayer, LocalTime> {
        val pt = PrayerTimes(Coordinates(at.lat, at.lng), DateComponents(date.year, date.monthNumber, date.dayOfMonth), params())
        fun conv(d: Date): LocalTime =
            Instant.fromEpochMilliseconds(d.time).toLocalDateTime(zone).time.let { LocalTime(it.hour, it.minute) }
        return mapOf(
            Prayer.FAJR to conv(pt.fajr),
            Prayer.DHUHR to conv(pt.dhuhr),
            Prayer.ASR to conv(pt.asr),
            Prayer.MAGHRIB to conv(pt.maghrib),
            Prayer.ISHA to conv(pt.isha),
        )
    }
}
