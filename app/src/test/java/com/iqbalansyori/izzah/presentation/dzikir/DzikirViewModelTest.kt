package com.iqbalansyori.izzah.presentation.dzikir

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DzikirViewModelTest {
    private val vm = DzikirViewModel()

    private fun taps(n: Int) = repeat(n) { vm.tap() }

    @Test
    fun `33 taps complete the first set with one haptic`() = runTest {
        vm.haptic.test {
            taps(33)
            assertThat(vm.state.value).isEqualTo(DzikirState(set = 1, count = 0))
            awaitItem()
            expectNoEvents()
        }
    }

    @Test
    fun `count advances within a set`() {
        taps(10)
        assertThat(vm.state.value).isEqualTo(DzikirState(0, 10))
        assertThat(vm.state.value.progress).isEqualTo(10 / 33f)
    }

    @Test
    fun `99 taps finish and further taps are ignored`() {
        taps(99)
        assertThat(vm.state.value.finished).isTrue()
        assertThat(vm.state.value.progress).isEqualTo(1f)
        vm.tap()
        assertThat(vm.state.value).isEqualTo(DzikirState(set = 3, count = 0))
    }

    @Test
    fun `reset returns to the start`() {
        taps(50)
        vm.reset()
        assertThat(vm.state.value).isEqualTo(DzikirState())
        assertThat(vm.state.value.finished).isFalse()
    }
}
