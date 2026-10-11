package com.iqbalansyori.izzah.data.repository

import com.iqbalansyori.izzah.data.db.dao.JedaDao
import com.iqbalansyori.izzah.data.db.entities.JedaEntity
import com.iqbalansyori.izzah.domain.TimeSource
import com.iqbalansyori.izzah.domain.model.JedaEvent
import com.iqbalansyori.izzah.domain.repository.JedaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class JedaRepositoryImpl(private val dao: JedaDao, private val time: TimeSource) : JedaRepository {
    override fun observeEvents(): Flow<List<JedaEvent>> =
        dao.observeAll().map { rows -> rows.map { JedaEvent(it.id, Instant.fromEpochMilliseconds(it.startedAtMs), it.completed) } }

    override suspend fun start(): Long = dao.insert(JedaEntity(startedAtMs = time.now().toEpochMilliseconds(), completed = false))

    override suspend fun complete(id: Long) = dao.complete(id)
}
