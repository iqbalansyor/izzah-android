package com.iqbalansyori.izzah.presentation.settings

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.iqbalansyori.izzah.domain.model.Settings
import com.iqbalansyori.izzah.testing.FakeLocaleSetter
import com.iqbalansyori.izzah.testing.FakeSettingsRepository
import com.iqbalansyori.izzah.testing.MainDispatcherExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class SettingsViewModelTest {
    companion object {
        @JvmField
        @RegisterExtension
        val main = MainDispatcherExtension()
    }

    private val settings = FakeSettingsRepository(Settings(onboardingDone = true))
    private val locale = FakeLocaleSetter()
    private fun vm(biometricAvailable: Boolean = true) = SettingsViewModel(settings, locale, "1.0", { biometricAvailable })

    @Test
    fun `state mirrors settings and exposes version and availability`() = runTest {
        vm().state.test {
            val s = expectMostRecentItem()
            assertThat(s.versionName).isEqualTo("1.0")
            assertThat(s.biometricAvailable).isTrue()
            assertThat(s.target).isEqualTo(90)
        }
    }

    @Test
    fun `biometric can only be enabled when available`() = runTest {
        vm(biometricAvailable = false).onAction(SettingsAction.SetBiometric(true))
        assertThat(settings.current().biometric).isFalse()
        vm().onAction(SettingsAction.SetBiometric(true))
        assertThat(settings.current().biometric).isTrue()
    }

    @Test
    fun `hide in recents and target persist`() = runTest {
        val v = vm()
        v.onAction(SettingsAction.SetHideInRecents(true))
        v.onAction(SettingsAction.SetTarget(40))
        assertThat(settings.current().hideInRecents).isTrue()
        assertThat(settings.current().target).isEqualTo(40)
    }

    @Test
    fun `notification toggles change only what is given`() = runTest {
        vm().onAction(SettingsAction.SetNotif(dzikir = false))
        val s = settings.current()
        assertThat(s.notifDzikir).isFalse()
        assertThat(s.notifPrayer).isTrue()
        assertThat(s.notifCheckin).isTrue()
    }

    @Test
    fun `language goes through the locale setter`() = runTest {
        val v = vm()
        v.onAction(SettingsAction.SetLanguage("in"))
        v.onAction(SettingsAction.SetLanguage(null))
        assertThat(locale.calls).containsExactly("in", null)
    }

    @Test
    fun `the language tag in state follows the setter`() = runTest {
        locale.tag = "en"
        vm().state.test { assertThat(expectMostRecentItem().languageTag).isEqualTo("en") }
    }
}
