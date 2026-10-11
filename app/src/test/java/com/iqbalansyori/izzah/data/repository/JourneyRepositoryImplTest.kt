package com.iqbalansyori.izzah.data.repository

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.iqbalansyori.izzah.domain.model.DayLog
import com.iqbalansyori.izzah.domain.model.Prayer
import com.iqbalansyori.izzah.domain.model.Settings
import com.iqbalansyori.izzah.domain.model.Trigger
import com.iqbalansyori.izzah.testing.FakeDayLogStore
import com.iqbalansyori.izzah.testing.FakeJournalDayStore
import com.iqbalansyori.izzah.testing.FakeSettingsRepository
import com.iqbalansyori.izzah.testing.FakeTimeSource
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.hours

class JourneyRepositoryImplTest {
    private val time = FakeTimeSource(Instant.parse("2026-10-13T05:00:00Z"), TimeZone.of("Asia/Jakarta"))
    private val settings = FakeSettingsRepository(Settings(onboardingDone = true, startDate = LocalDate(2026, 10, 1), target = 30))
    private val store = FakeDayLogStore()
    private val repo = JourneyRepositoryImpl(store, FakeJournalDayStore(), settings, time)

    @Test
    fun `toggle prayer upserts today`() = runTest {
        repo.togglePrayer(Prayer.FAJR)
        repo.togglePrayer(Prayer.ASR)
        repo.togglePrayer(Prayer.FAJR)
        assertThat(store.get(LocalDate(2026, 10, 13))?.prayers).isEqualTo(setOf(Prayer.ASR))
    }

    @Test
    fun `log slip keeps prayers and adds triggers`() = runTest {
        repo.togglePrayer(Prayer.FAJR)
        repo.logSlip(setOf(Trigger.BORED))
        assertThat(store.get(LocalDate(2026, 10, 13)))
            .isEqualTo(DayLog(LocalDate(2026, 10, 13), setOf(Prayer.FAJR), true, setOf(Trigger.BORED)))
    }

    @Test
    fun `logging a slip twice keeps a single slip day`() = runTest {
        repo.logSlip(setOf(Trigger.BORED))
        repo.logSlip(setOf(Trigger.TIRED))
        assertThat(store.get(LocalDate(2026, 10, 13))?.slip).isEqualTo(true)
        assertThat(repo.observeJourney().let { var s = 0; it.test { s = awaitItem().slips; cancelAndIgnoreRemainingEvents() }; s }).isEqualTo(1)
    }

    @Test
    fun `journey re-emits after midnight`() = runTest {
        repo.observeJourney().test {
            assertThat(awaitItem().dayCount).isEqualTo(13)
            time.advanceTo(Instant.parse("2026-10-13T17:00:01Z"))
            advanceTimeBy(13.hours)
            assertThat(awaitItem().dayCount).isEqualTo(14)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
