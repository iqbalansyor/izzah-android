package com.iqbalansyori.izzah.presentation.protect

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.iqbalansyori.izzah.domain.logic.SlotSchedule
import com.iqbalansyori.izzah.domain.model.LockMode
import com.iqbalansyori.izzah.domain.model.Rawan
import com.iqbalansyori.izzah.domain.model.Settings
import com.iqbalansyori.izzah.testing.FakeInstalledApps
import com.iqbalansyori.izzah.testing.FakeSettingsRepository
import com.iqbalansyori.izzah.testing.MainDispatcherExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class ProtectViewModelTest {
    companion object {
        @JvmField
        @RegisterExtension
        val main = MainDispatcherExtension()
    }

    private val settings = FakeSettingsRepository(
        Settings(onboardingDone = true, slots = SlotSchedule.defaults(setOf(Rawan.LATE_NIGHT)), lockedPackages = setOf("com.a", "com.b")),
    )
    private val apps = FakeInstalledApps(mapOf("com.a" to "Alpha", "com.b" to "Beta", "com.iqbalansyori.izzah" to "Izzah"))
    private fun vm(keepRunning: Boolean = false) = ProtectViewModel(settings, apps) { keepRunning }

    @Test
    fun `state shows slots, locked apps with labels and the mode`() = runTest {
        vm().state.test {
            val s = expectMostRecentItem()
            assertThat(s.slots.map { it.id }).containsExactly(1, 2)
            assertThat(s.locked.map { it.label }).containsExactly("Alpha", "Beta")
            assertThat(s.mode).isEqualTo(LockMode.PAUSE)
        }
    }

    @Test
    fun `toggle slot flips enabled in settings`() = runTest {
        val before = settings.current().slots.first { it.id == 2 }.enabled
        vm().onAction(ProtectAction.ToggleSlot(2))
        assertThat(settings.current().slots.first { it.id == 2 }.enabled).isEqualTo(!before)
    }

    @Test
    fun `save slot replaces by id`() = runTest {
        val slot = settings.current().slots.first { it.id == 1 }.copy(startMinute = 23 * 60, endMinute = 2 * 60)
        vm().onAction(ProtectAction.SaveSlot(slot))
        assertThat(settings.current().slots.first { it.id == 1 }.startMinute).isEqualTo(23 * 60)
        assertThat(settings.current().slots.size).isEqualTo(2)
    }

    @Test
    fun `edit opens and dismiss closes the editor`() = runTest {
        val v = vm()
        v.state.test {
            expectMostRecentItem()
            v.onAction(ProtectAction.EditSlot(1))
            assertThat(expectMostRecentItem().editing?.id).isEqualTo(1)
            v.onAction(ProtectAction.DismissEdit)
            assertThat(expectMostRecentItem().editing).isNull()
        }
    }

    @Test
    fun `set apps never includes izzah itself`() = runTest {
        vm().onAction(ProtectAction.SetApps(setOf("com.a", "com.iqbalansyori.izzah", "com.c")))
        assertThat(settings.current().lockedPackages).isEqualTo(setOf("com.a", "com.c"))
    }

    @Test
    fun `remove app and set mode persist`() = runTest {
        val v = vm()
        v.onAction(ProtectAction.RemoveApp("com.a"))
        v.onAction(ProtectAction.SetMode(LockMode.FULL))
        assertThat(settings.current().lockedPackages).isEqualTo(setOf("com.b"))
        assertThat(settings.current().lockMode).isEqualTo(LockMode.FULL)
    }

    @Test
    fun `permissions and keep-running flags flow into state`() = runTest {
        val v = vm(keepRunning = true)
        v.state.test {
            assertThat(expectMostRecentItem().showKeepRunning).isTrue()
            v.onAction(ProtectAction.PermissionsChanged(usage = false, overlay = true))
            val s = expectMostRecentItem()
            assertThat(s.usageGranted).isFalse()
            assertThat(s.overlayGranted).isTrue()
        }
    }
}
