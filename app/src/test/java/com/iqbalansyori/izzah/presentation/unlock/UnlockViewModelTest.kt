package com.iqbalansyori.izzah.presentation.unlock

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.iqbalansyori.izzah.data.lock.AppLockManager
import com.iqbalansyori.izzah.data.security.PinHasher
import com.iqbalansyori.izzah.domain.model.Settings
import com.iqbalansyori.izzah.testing.FakeSettingsRepository
import com.iqbalansyori.izzah.testing.FakeTimeSource
import com.iqbalansyori.izzah.testing.MainDispatcherExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.time.Duration.Companion.seconds

class UnlockViewModelTest {
    companion object {
        @JvmField
        @RegisterExtension
        val main = MainDispatcherExtension()
    }

    private val time = FakeTimeSource(Instant.parse("2026-10-13T00:00:00Z"), TimeZone.UTC)
    private val salt = PinHasher.newSalt()
    private fun settingsWith(biometric: Boolean = false, fails: Int = 0) = FakeSettingsRepository(
        Settings(onboardingDone = true, pinSalt = salt, pinHash = PinHasher.hash("1234", salt, 1000), biometric = biometric, pinFailCount = fails),
    )

    private val settings = settingsWith()
    private val lock = AppLockManager(time)
    private val vm by lazy { UnlockViewModel(settings, lock, time, 1000, Dispatchers.Main) }

    private fun UnlockViewModel.pin(code: String) = code.forEach { onAction(UnlockAction.Digit(it)) }

    @Test
    fun `correct pin unlocks and resets fails`() = runTest {
        val s = settingsWith(fails = 3)
        val v = UnlockViewModel(s, lock, time, 1000, Dispatchers.Main)
        v.events.test {
            v.pin("1234")
            assertThat(awaitItem()).isEqualTo(UnlockEvent.Unlocked)
        }
        assertThat(s.current().pinFailCount).isEqualTo(0)
        assertThat(lock.locked.value).isFalse()
    }

    @Test
    fun `wrong pin flags and clears the entry`() = runTest {
        vm.pin("0000")
        assertThat(vm.state.value.wrong).isTrue()
        assertThat(vm.state.value.entry).isEqualTo("")
        assertThat(settings.current().pinFailCount).isEqualTo(1)
    }

    @Test
    fun `five wrong pins start a 30s cooldown persisted in settings`() = runTest {
        repeat(5) { vm.pin("0000") }
        assertThat(settings.current().pinLockedUntil).isEqualTo(time.now() + 30.seconds)
        assertThat(settings.current().pinFailCount).isEqualTo(0)
        val vm2 = UnlockViewModel(settings, lock, time, 1000, Dispatchers.Main) // simulates process death
        vm2.pin("1234")
        assertThat(lock.locked.value).isTrue() // ignored during cooldown
        time.advanceBy(31.seconds)
        vm2.onAction(UnlockAction.Tick)
        vm2.pin("1234")
        assertThat(lock.locked.value).isFalse()
    }

    @Test
    fun `cooldown seconds are exposed and count down`() = runTest {
        repeat(5) { vm.pin("0000") }
        assertThat(vm.state.value.cooldownSeconds).isEqualTo(30)
        time.advanceBy(10.seconds)
        vm.onAction(UnlockAction.Tick)
        assertThat(vm.state.value.cooldownSeconds).isEqualTo(20)
    }

    @Test
    fun `biometric enabled prompts on start`() = runTest {
        val v = UnlockViewModel(settingsWith(biometric = true), lock, time, 1000, Dispatchers.Main)
        v.events.test { assertThat(awaitItem()).isEqualTo(UnlockEvent.PromptBiometric) }
    }

    @Test
    fun `biometric success unlocks`() = runTest {
        vm.events.test {
            vm.onAction(UnlockAction.BiometricSucceeded)
            assertThat(awaitItem()).isEqualTo(UnlockEvent.Unlocked)
        }
    }

    @Test
    fun `forgot sheet toggles`() {
        vm.onAction(UnlockAction.ForgotTapped)
        assertThat(vm.state.value.showForgot).isTrue()
        vm.onAction(UnlockAction.ForgotDismissed)
        assertThat(vm.state.value.showForgot).isFalse()
    }
}
