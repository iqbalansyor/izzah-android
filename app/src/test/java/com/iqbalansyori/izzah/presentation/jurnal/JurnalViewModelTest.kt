package com.iqbalansyori.izzah.presentation.jurnal

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.iqbalansyori.izzah.domain.model.JournalEntry
import com.iqbalansyori.izzah.domain.model.Mood
import com.iqbalansyori.izzah.testing.FakeJournalRepository
import com.iqbalansyori.izzah.testing.MainDispatcherExtension
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class JurnalViewModelTest {
    companion object {
        @JvmField
        @RegisterExtension
        val main = MainDispatcherExtension()
    }

    private val journal = FakeJournalRepository()
    private fun vm() = JurnalViewModel(journal)

    @Test
    fun `cannot save an empty form`() = runTest {
        val v = vm()
        v.state.test {
            assertThat(expectMostRecentItem().canSave).isFalse()
            v.onAction(JurnalAction.Save)
            expectNoEvents()
        }
        assertThat(journal.entriesFlow.value).isEqualTo(emptyList())
    }

    @Test
    fun `a blank note without a mood does not count`() = runTest {
        val v = vm()
        v.state.test {
            expectMostRecentItem()
            v.onAction(JurnalAction.Edit("   "))
            assertThat(expectMostRecentItem().canSave).isFalse()
        }
    }

    @Test
    fun `save with a mood stores the entry, clears the form and emits Saved`() = runTest {
        val v = vm()
        v.events.test {
            v.onAction(JurnalAction.PickMood(Mood.SHAKY))
            v.onAction(JurnalAction.Edit("  bored in the afternoon "))
            v.onAction(JurnalAction.Save)
            assertThat(awaitItem()).isEqualTo(JurnalEvent.Saved)
        }
        val saved = journal.entriesFlow.value.single()
        assertThat(saved.mood).isEqualTo(Mood.SHAKY)
        assertThat(saved.note).isEqualTo("bored in the afternoon")
        assertThat(v.state.value.mood).isEqualTo(null)
        assertThat(v.state.value.note).isEqualTo("")
    }

    @Test
    fun `tapping the same mood again clears it`() = runTest {
        val v = vm()
        v.state.test {
            expectMostRecentItem()
            v.onAction(JurnalAction.PickMood(Mood.OKAY))
            assertThat(expectMostRecentItem().canSave).isTrue()
            v.onAction(JurnalAction.PickMood(Mood.OKAY))
            assertThat(expectMostRecentItem().canSave).isFalse()
        }
    }

    @Test
    fun `entries are listed newest first`() = runTest {
        journal.entriesFlow.value = listOf(
            JournalEntry(1, Instant.fromEpochMilliseconds(1000), null, "old"),
            JournalEntry(3, Instant.fromEpochMilliseconds(3000), null, "new"),
            JournalEntry(2, Instant.fromEpochMilliseconds(2000), null, "mid"),
        )
        vm().state.test { assertThat(expectMostRecentItem().entries.map { it.note }).containsExactly("new", "mid", "old") }
    }
}
