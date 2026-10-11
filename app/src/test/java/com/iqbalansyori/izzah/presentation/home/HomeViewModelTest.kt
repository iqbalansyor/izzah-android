package com.iqbalansyori.izzah.presentation.home

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.iqbalansyori.izzah.domain.logic.DayStat
import com.iqbalansyori.izzah.domain.logic.Journey
import com.iqbalansyori.izzah.domain.model.DayLog
import com.iqbalansyori.izzah.domain.model.JournalEntry
import com.iqbalansyori.izzah.domain.model.Prayer
import com.iqbalansyori.izzah.testing.FakeJournalRepository
import com.iqbalansyori.izzah.testing.FakeJourneyRepository
import com.iqbalansyori.izzah.testing.FakePrayerTimesRepository
import com.iqbalansyori.izzah.testing.FakeTimeSource
import com.iqbalansyori.izzah.testing.MainDispatcherExtension
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class HomeViewModelTest {
    companion object {
        @JvmField
        @RegisterExtension
        val main = MainDispatcherExtension()
    }

    private val start = LocalDate(2026, 10, 1)
    private val time = FakeTimeSource(Instant.parse("2026-10-14T05:00:00Z"), TimeZone.of("Asia/Jakarta"))

    private fun journey(days: Int, slips: Set<Int> = emptySet()) = Journey(
        (0 until days).map { i ->
            DayStat(start.plus(i, DateTimeUnit.DAY), i + 1, 3, Prayer.entries.take(3).toSet(), true, (i + 1) in slips, i == days - 1)
        },
        target = 90,
    )

    private val journeys = FakeJourneyRepository(journey(14, slips = setOf(5)))
    private val journal = FakeJournalRepository()
    private val prayers = FakePrayerTimesRepository(
        mapOf(Prayer.FAJR to LocalTime(4, 10), Prayer.DHUHR to LocalTime(11, 40), Prayer.ASR to LocalTime(15, 0), Prayer.MAGHRIB to LocalTime(17, 50), Prayer.ISHA to LocalTime(19, 0)),
    )
    private fun vm() = HomeViewModel(journeys, journal, prayers, time)

    @Test
    fun `state reflects the journey`() = runTest {
        vm().state.test {
            val s = expectMostRecentItem()
            assertThat(s.kept).isEqualTo(13)
            assertThat(s.bars).hasSize(30)
            assertThat(s.target).isEqualTo(90)
        }
    }

    @Test
    fun `tapping a bar opens the sheet and tapping again closes it`() = runTest {
        val v = vm()
        v.state.test {
            expectMostRecentItem()
            v.onAction(HomeAction.TapBar(13))
            val open = expectMostRecentItem()
            assertThat(open.selected).isEqualTo(13)
            assertThat(open.sheetDay?.number).isEqualTo(14)
            v.onAction(HomeAction.TapBar(13))
            val closed = expectMostRecentItem()
            assertThat(closed.selected).isNull()
            assertThat(closed.sheetDay).isNull()
        }
    }

    @Test
    fun `tapping a future bar does nothing`() = runTest {
        val v = vm()
        v.state.test {
            expectMostRecentItem()
            v.onAction(HomeAction.TapBar(20))
            expectNoEvents()
            assertThat(v.state.value.selected).isNull()
        }
    }

    @Test
    fun `sheet note comes from the newest journal entry of that day`() = runTest {
        val noon = Instant.parse("2026-10-14T05:00:00Z")
        journal.entriesFlow.value = listOf(
            JournalEntry(2, noon, null, "newer"),
            JournalEntry(1, Instant.parse("2026-10-14T02:00:00Z"), null, "older"),
        )
        val v = vm()
        v.state.test {
            expectMostRecentItem()
            v.onAction(HomeAction.TapBar(13))
            assertThat(expectMostRecentItem().sheetDay?.note).isEqualTo("newer")
        }
    }

    @Test
    fun `next prayer skips the ticked ones`() = runTest {
        journeys.todayFlow.value = DayLog(start, setOf(Prayer.FAJR, Prayer.DHUHR))
        vm().state.test {
            val s = expectMostRecentItem()
            assertThat(s.prayersToday).isEqualTo(setOf(Prayer.FAJR, Prayer.DHUHR))
            assertThat(s.nextPrayer).isEqualTo(Prayer.ASR to LocalTime(15, 0))
        }
    }

    @Test
    fun `all prayers done leaves no next prayer`() = runTest {
        journeys.todayFlow.value = DayLog(start, Prayer.entries.toSet())
        vm().state.test { assertThat(expectMostRecentItem().nextPrayer).isNull() }
    }

    @Test
    fun `morning before asr and evening after`() = runTest {
        vm().state.test { assertThat(expectMostRecentItem().morning).isEqualTo(true) } // 12:00 WIB
        time.advanceTo(Instant.parse("2026-10-14T09:00:00Z")) // 16:00 WIB
        vm().state.test { assertThat(expectMostRecentItem().morning).isEqualTo(false) }
    }

    @Test
    fun `kept grows when a new day arrives`() = runTest {
        vm().state.test {
            assertThat(expectMostRecentItem().kept).isEqualTo(13)
            journeys.journeyFlow.value = journey(15, slips = setOf(5))
            assertThat(expectMostRecentItem().kept).isEqualTo(14)
        }
    }
}
