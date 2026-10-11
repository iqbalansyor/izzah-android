package com.iqbalansyori.izzah.presentation.onboarding

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.iqbalansyori.izzah.data.security.PinHasher
import com.iqbalansyori.izzah.domain.model.Rawan
import com.iqbalansyori.izzah.domain.model.Why
import com.iqbalansyori.izzah.testing.FakeSettingsRepository
import com.iqbalansyori.izzah.testing.FakeTimeSource
import com.iqbalansyori.izzah.testing.MainDispatcherExtension
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class OnboardingViewModelTest {
    companion object {
        @JvmField
        @RegisterExtension
        val main = MainDispatcherExtension()
    }

    private val time = FakeTimeSource(Instant.parse("2026-10-13T05:00:00Z"), TimeZone.of("Asia/Jakarta"))
    private val settings = FakeSettingsRepository()
    private fun vm(biometric: Boolean = true) = OnboardingViewModel(settings, time) { biometric }

    private fun OnboardingViewModel.pin(code: String) = code.forEach { onAction(OnboardingAction.PinDigit(it)) }

    @Test
    fun `toggle why adds and removes`() {
        val v = vm()
        v.onAction(OnboardingAction.ToggleWhy(Why.FAMILY))
        assertThat(v.state.value.why).isEqualTo(setOf(Why.ALLAH, Why.FAMILY))
        v.onAction(OnboardingAction.ToggleWhy(Why.ALLAH))
        assertThat(v.state.value.why).isEqualTo(setOf(Why.FAMILY))
    }

    @Test
    fun `pick target`() {
        val v = vm()
        v.onAction(OnboardingAction.PickTarget(7))
        assertThat(v.state.value.target).isEqualTo(7)
    }

    @Test
    fun `pin create then matching confirm navigates`() = runTest {
        val v = vm()
        v.events.test {
            v.pin("1234")
            advanceTimeBy(301)
            assertThat(awaitItem()).isEqualTo(OnboardingEvent.PinCreated)
            assertThat(v.state.value.pinFirst).isEqualTo("1234")
            assertThat(v.state.value.pinEntry).isEqualTo("")
            v.pin("1234")
            assertThat(awaitItem()).isEqualTo(OnboardingEvent.PinConfirmed)
        }
    }

    @Test
    fun `pin mismatch resets both and flags`() = runTest {
        val v = vm()
        v.pin("1234")
        advanceTimeBy(301)
        v.pin("4321")
        assertThat(v.state.value.pinMismatch).isTrue()
        assertThat(v.state.value.pinFirst).isNull()
        assertThat(v.state.value.pinEntry).isEqualTo("")
        v.pin("1")
        assertThat(v.state.value.pinMismatch).isFalse()
    }

    @Test
    fun `delete removes the last digit`() {
        val v = vm()
        v.pin("12")
        v.onAction(OnboardingAction.PinDelete)
        assertThat(v.state.value.pinEntry).isEqualTo("1")
    }

    @Test
    fun `finish writes the full settings`() = runTest {
        val v = vm()
        v.onAction(OnboardingAction.PickTarget(40))
        v.onAction(OnboardingAction.ToggleRawan(Rawan.DAWN))
        v.pin("1234")
        advanceTimeBy(301)
        v.pin("1234")
        v.onAction(OnboardingAction.SetBiometric(true))
        v.onAction(OnboardingAction.SetNotif(checkin = false))
        v.events.test {
            skipItems(2)
            v.onAction(OnboardingAction.Finish)
            assertThat(awaitItem()).isEqualTo(OnboardingEvent.Finished)
        }
        val s = settings.current()
        assertThat(s.onboardingDone).isTrue()
        assertThat(s.startDate).isEqualTo(LocalDate(2026, 10, 13))
        assertThat(s.target).isEqualTo(40)
        assertThat(s.rawan).isEqualTo(setOf(Rawan.LATE_NIGHT, Rawan.DAWN))
        assertThat(s.biometric).isTrue()
        assertThat(s.notifCheckin).isFalse()
        assertThat(s.slots.first().enabled).isTrue()
        assertThat(s.pinSalt).isNotNull()
        assertThat(PinHasher.verify("1234", s.pinSalt!!, s.pinHash!!)).isTrue()
    }
}
