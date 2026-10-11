package com.iqbalansyori.izzah.presentation.sholat

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.iqbalansyori.izzah.domain.logic.Journey
import com.iqbalansyori.izzah.domain.model.DayLog
import com.iqbalansyori.izzah.domain.model.GeoPoint
import com.iqbalansyori.izzah.domain.model.Prayer
import com.iqbalansyori.izzah.domain.model.Settings
import com.iqbalansyori.izzah.testing.FakeJourneyRepository
import com.iqbalansyori.izzah.testing.FakePrayerTimesRepository
import com.iqbalansyori.izzah.testing.FakeSettingsRepository
import com.iqbalansyori.izzah.testing.FakeTimeSource
import com.iqbalansyori.izzah.testing.MainDispatcherExtension
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class SholatViewModelTest {
    companion object {
        @JvmField
        @RegisterExtension
        val main = MainDispatcherExtension()
    }

    private val time = FakeTimeSource(Instant.parse("2026-10-14T05:00:00Z"), TimeZone.of("Asia/Jakarta"))
    private val today = LocalDate(2026, 10, 14)
    private val journeys = FakeJourneyRepository(Journey(emptyList(), 90), DayLog(today))
    private val times = mapOf(Prayer.FAJR to LocalTime(4, 10), Prayer.DHUHR to LocalTime(11, 40))
    private val prayers = FakePrayerTimesRepository(times)
    private val settings = FakeSettingsRepository(Settings(location = GeoPoint(-6.2, 106.8, "Jakarta")))
    private val cities = listOf(GeoPoint(-6.2, 106.8, "Jakarta"), GeoPoint(-7.25, 112.75, "Surabaya"))
    private fun vm() = SholatViewModel(journeys, prayers, settings, cities, time)

    @Test
    fun `state shows today, times and city`() = runTest {
        vm().state.test {
            val s = expectMostRecentItem()
            assertThat(s.today).isEqualTo(today)
            assertThat(s.times).isEqualTo(times)
            assertThat(s.city).isEqualTo("Jakarta")
            assertThat(s.cities).isEqualTo(cities)
        }
    }

    @Test
    fun `toggle ticks the prayer for today`() = runTest {
        val v = vm()
        v.state.test {
            expectMostRecentItem()
            v.onAction(SholatAction.Toggle(Prayer.ASR))
            assertThat(expectMostRecentItem().done).isEqualTo(setOf(Prayer.ASR))
            v.onAction(SholatAction.Toggle(Prayer.ASR))
            assertThat(expectMostRecentItem().done).isEqualTo(emptySet())
        }
    }

    @Test
    fun `choosing a city sets the location and closes the picker`() = runTest {
        val v = vm()
        v.state.test {
            expectMostRecentItem()
            v.onAction(SholatAction.PickCity)
            assertThat(expectMostRecentItem().showCityPicker).isTrue()
            v.onAction(SholatAction.CityChosen(cities[1]))
            assertThat(expectMostRecentItem().showCityPicker).isFalse()
        }
        assertThat(prayers.lastLocation).isEqualTo(cities[1])
    }

    @Test
    fun `a found location is saved`() = runTest {
        val v = vm()
        val here = GeoPoint(1.0, 2.0, "Here")
        v.onAction(SholatAction.LocationFound(here))
        assertThat(prayers.lastLocation).isEqualTo(here)
    }

    @Test
    fun `dismiss closes the picker`() = runTest {
        val v = vm()
        v.state.test {
            expectMostRecentItem()
            v.onAction(SholatAction.PickCity)
            expectMostRecentItem()
            v.onAction(SholatAction.DismissPicker)
            assertThat(expectMostRecentItem().showCityPicker).isFalse()
        }
    }
}
