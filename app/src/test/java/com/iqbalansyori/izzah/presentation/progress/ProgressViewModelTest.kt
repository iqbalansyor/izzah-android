package com.iqbalansyori.izzah.presentation.progress

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.iqbalansyori.izzah.domain.logic.DayStat
import com.iqbalansyori.izzah.domain.logic.InsightWindow
import com.iqbalansyori.izzah.domain.logic.Journey
import com.iqbalansyori.izzah.domain.model.JedaEvent
import com.iqbalansyori.izzah.domain.model.Prayer
import com.iqbalansyori.izzah.domain.model.Settings
import com.iqbalansyori.izzah.testing.FakeJedaRepository
import com.iqbalansyori.izzah.testing.FakeJournalRepository
import com.iqbalansyori.izzah.testing.FakeJourneyRepository
import com.iqbalansyori.izzah.testing.FakeSettingsRepository
import com.iqbalansyori.izzah.testing.FakeTimeSource
import com.iqbalansyori.izzah.testing.MainDispatcherExtension
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class ProgressViewModelTest {
    companion object {
        @JvmField
        @RegisterExtension
        val main = MainDispatcherExtension()
    }

    private val zone = TimeZone.of("Asia/Jakarta")
    private val time = FakeTimeSource(Instant.parse("2026-10-14T05:00:00Z"), zone)
    private val start = LocalDate(2026, 10, 1)

    private val journeys = FakeJourneyRepository(
        Journey(
            (0 until 14).map { i ->
                DayStat(start.plus(i, DateTimeUnit.DAY), i + 1, 3, Prayer.entries.take(3).toSet(), i % 2 == 0, i + 1 == 5, i == 13)
            },
            target = 30,
        ),
    )
    private val jeda = FakeJedaRepository()
    private val settings = FakeSettingsRepository(Settings(notifCheckin = false))
    private fun vm() = ProgressViewModel(journeys, jeda, FakeJournalRepository(), settings, time)

    private fun events(count: Int, completed: Boolean = true) = (0 until count).map { i ->
        val at = LocalDateTime(start.plus(i, DateTimeUnit.DAY), kotlinx.datetime.LocalTime(22 + i % 2, 10 + i)).toInstant(zone)
        JedaEvent(i.toLong(), at, completed)
    }

    @Test
    fun `stats come from the journey`() = runTest {
        vm().state.test {
            val s = expectMostRecentItem()
            assertThat(s.kept).isEqualTo(13)
            assertThat(s.slips).isEqualTo(1)
            assertThat(s.dayCount).isEqualTo(14)
            assertThat(s.percent).isEqualTo(43)
            assertThat(s.grid.size).isEqualTo(30)
            assertThat(s.checkinDays).isEqualTo(7)
        }
    }

    @Test
    fun `insight is null with fewer than five completed pauses`() = runTest {
        jeda.eventsFlow.value = events(4) + events(10, completed = false)
        vm().state.test { assertThat(expectMostRecentItem().insight).isNull() }
    }

    @Test
    fun `insight finds the busiest window`() = runTest {
        jeda.eventsFlow.value = events(11)
        vm().state.test {
            val s = expectMostRecentItem()
            assertThat(s.insight).isEqualTo(InsightWindow(22, 0))
            assertThat(s.jedaCompleted).isEqualTo(11)
        }
    }

    @Test
    fun `toggle reminder flips the setting`() = runTest {
        val v = vm()
        v.state.test {
            assertThat(expectMostRecentItem().reminderOn).isFalse()
            v.onAction(ProgressAction.ToggleReminder)
            assertThat(expectMostRecentItem().reminderOn).isTrue()
            assertThat(settings.current().notifCheckin).isTrue()
        }
    }

    @Test
    fun `tapping cells selects, deselects and ignores the future`() = runTest {
        val v = vm()
        v.state.test {
            expectMostRecentItem()
            v.onAction(ProgressAction.TapCell(4))
            assertThat(expectMostRecentItem().selected?.number).isEqualTo(5)
            v.onAction(ProgressAction.TapCell(4))
            assertThat(expectMostRecentItem().selected).isNull()
            v.onAction(ProgressAction.TapCell(20))
            expectNoEvents()
        }
    }
}
