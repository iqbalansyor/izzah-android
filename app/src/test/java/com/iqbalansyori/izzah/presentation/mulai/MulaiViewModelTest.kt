package com.iqbalansyori.izzah.presentation.mulai

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.iqbalansyori.izzah.domain.logic.DayStat
import com.iqbalansyori.izzah.domain.logic.Journey
import com.iqbalansyori.izzah.domain.model.DayLog
import com.iqbalansyori.izzah.domain.model.Prayer
import com.iqbalansyori.izzah.domain.model.Trigger
import com.iqbalansyori.izzah.testing.FakeJourneyRepository
import com.iqbalansyori.izzah.testing.MainDispatcherExtension
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class MulaiViewModelTest {
    companion object {
        @JvmField
        @RegisterExtension
        val main = MainDispatcherExtension()
    }

    private val start = LocalDate(2026, 10, 1)
    private fun journey(slips: Set<Int>) = Journey(
        (0 until 14).map { i -> DayStat(start.plus(i, DateTimeUnit.DAY), i + 1, 3, Prayer.entries.take(3).toSet(), true, (i + 1) in slips, i == 13) },
        90,
    )

    private val journeys = FakeJourneyRepository(journey(emptySet()), DayLog(start))
    private fun vm() = MulaiViewModel(journeys)

    @Test
    fun `shows the kept days`() = runTest {
        vm().state.test { assertThat(expectMostRecentItem().kept).isEqualTo(14) }
    }

    @Test
    fun `steps and triggers toggle`() = runTest {
        val v = vm()
        v.state.test {
            expectMostRecentItem()
            v.onAction(MulaiAction.ToggleStep(1))
            assertThat(expectMostRecentItem().steps).isEqualTo(listOf(false, true, false))
            v.onAction(MulaiAction.ToggleTrigger(Trigger.BORED))
            v.onAction(MulaiAction.ToggleTrigger(Trigger.TIRED))
            v.onAction(MulaiAction.ToggleTrigger(Trigger.BORED))
            assertThat(expectMostRecentItem().triggers).isEqualTo(setOf(Trigger.TIRED))
        }
    }

    @Test
    fun `confirm logs a slip with the chosen triggers and emits Done`() = runTest {
        val v = vm()
        v.events.test {
            v.onAction(MulaiAction.ToggleTrigger(Trigger.SOCIAL))
            v.onAction(MulaiAction.Confirm)
            assertThat(awaitItem()).isEqualTo(MulaiEvent.Done)
        }
        assertThat(journeys.todayFlow.value.slip).isTrue()
        assertThat(journeys.todayFlow.value.triggers).isEqualTo(setOf(Trigger.SOCIAL))
    }

    @Test
    fun `the kept count shown stays the pre-slip value after confirming`() = runTest {
        val v = vm()
        v.state.test {
            assertThat(expectMostRecentItem().kept).isEqualTo(14)
            v.onAction(MulaiAction.Confirm)
            journeys.journeyFlow.value = journey(setOf(14))
            expectNoEvents()
            assertThat(v.state.value.kept).isEqualTo(14)
        }
    }

    @Test
    fun `confirming twice keeps a single slip day`() = runTest {
        val v = vm()
        v.onAction(MulaiAction.Confirm)
        v.onAction(MulaiAction.Confirm)
        assertThat(journeys.todayFlow.value.slip).isTrue()
    }
}
