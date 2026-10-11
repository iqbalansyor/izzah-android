package com.iqbalansyori.izzah.data.repository

import com.iqbalansyori.izzah.data.db.dao.JournalDao
import com.iqbalansyori.izzah.data.db.entities.JournalEntity
import com.iqbalansyori.izzah.domain.TimeSource
import com.iqbalansyori.izzah.domain.model.JournalEntry
import com.iqbalansyori.izzah.domain.model.Mood
import com.iqbalansyori.izzah.domain.repository.JournalDayStore
import com.iqbalansyori.izzah.domain.repository.JournalRepository
import com.iqbalansyori.izzah.domain.today
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

class JournalRepositoryImpl(private val dao: JournalDao, private val time: TimeSource) : JournalRepository, JournalDayStore {
    override fun observeEntries(): Flow<List<JournalEntry>> = dao.observeAll().map { rows ->
        rows.map { JournalEntry(it.id, Instant.fromEpochMilliseconds(it.createdAtMs), it.mood?.let(Mood.entries::getOrNull), it.note) }
    }

    override fun observeDays(): Flow<Set<LocalDate>> =
        dao.observeDays().map { days -> days.map { LocalDate.fromEpochDays(it.toInt()) }.toSet() }

    override suspend fun add(mood: Mood?, note: String) {
        dao.insert(JournalEntity(createdAtMs = time.now().toEpochMilliseconds(), epochDay = time.today().toEpochDays().toLong(), mood = mood?.ordinal, note = note))
    }
}
