package com.iqbalansyori.izzah.presentation.jeda

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.iqbalansyori.izzah.domain.logic.DayStat
import com.iqbalansyori.izzah.domain.logic.Journey
import com.iqbalansyori.izzah.domain.model.Prayer
import com.iqbalansyori.izzah.testing.FakeJedaRepository
import com.iqbalansyori.izzah.testing.FakeJourneyRepository
import com.iqbalansyori.izzah.testing.MainDispatcherExtension
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class JedaViewModelTest {
    companion object {
        @JvmField
        @RegisterExtension
        val main = MainDispatcherExtension()
    }

    private val start = LocalDate(2026, 10, 1)
    private val journeys = FakeJourneyRepository(
        Journey((0 until 10).map { i -> DayStat(start.plus(i, DateTimeUnit.DAY), i + 1, 3, Prayer.entries.take(3).toSet(), true, false, i == 9) }, 90),
    )
    private val jeda = FakeJedaRepository()

    private fun vmTest(block: suspend TestScope.(JedaViewModel) -> Unit) = runTest {
        val vm = JedaViewModel(jeda, journeys)
        try {
            block(vm)
        } finally {
            vm.clear()
        }
    }

    @Test
    fun `starts an event and shows the kept days`() = vmTest { vm ->
        assertThat(jeda.eventsFlow.value).hasSize(1)
        assertThat(vm.state.value.kept).isEqualTo(10)
        assertThat(vm.state.value.secondsLeft).isEqualTo(60)
    }

    @Test
    fun `breathing follows the 4 second cycle`() = vmTest { vm ->
        assertThat(vm.state.value.inhale).isFalse()
        advanceTimeBy(61)
        assertThat(vm.state.value.inhale).isTrue()
        advanceTimeBy(4000)
        assertThat(vm.state.value.inhale).isFalse()
        assertThat(vm.state.value.breaths).isEqualTo(1)
        advanceTimeBy(4000)
        assertThat(vm.state.value.inhale).isTrue()
        assertThat(vm.state.value.breaths).isEqualTo(2)
    }

    @Test
    fun `countdown reaches zero and completes the event once`() = vmTest { vm ->
        advanceTimeBy(60_001)
        assertThat(vm.state.value.secondsLeft).isEqualTo(0)
        assertThat(jeda.eventsFlow.value.count { it.completed }).isEqualTo(1)
    }

    @Test
    fun `calmer twice completes once`() = vmTest { vm ->
        vm.calmer()
        vm.calmer()
        assertThat(jeda.eventsFlow.value).hasSize(1)
        assertThat(jeda.eventsFlow.value.single().completed).isTrue()
        advanceTimeBy(60_001)
        assertThat(jeda.eventsFlow.value).hasSize(1)
    }
}
