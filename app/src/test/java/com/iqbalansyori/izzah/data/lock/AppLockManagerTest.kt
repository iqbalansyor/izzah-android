package com.iqbalansyori.izzah.data.lock

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.iqbalansyori.izzah.testing.FakeTimeSource
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

class AppLockManagerTest {
    private val time = FakeTimeSource(Instant.parse("2026-10-13T00:00:00Z"), TimeZone.UTC)

    @Test
    fun `locks after 30s in background`() {
        val m = AppLockManager(time)
        m.unlock(); m.onBackground(); time.advanceBy(31.seconds); m.onForeground(hasPin = true)
        assertThat(m.locked.value).isTrue()
    }

    @Test
    fun `stays unlocked within grace`() {
        val m = AppLockManager(time)
        m.unlock(); m.onBackground(); time.advanceBy(10.seconds); m.onForeground(true)
        assertThat(m.locked.value).isFalse()
    }

    @Test
    fun `no pin never locks`() {
        val m = AppLockManager(time)
        m.unlock(); m.onBackground(); time.advanceBy(1.hours); m.onForeground(false)
        assertThat(m.locked.value).isFalse()
    }

    @Test
    fun `starts locked`() = assertThat(AppLockManager(time).locked.value).isTrue()
}
