package com.iqbalansyori.izzah.data.repository

import com.iqbalansyori.izzah.data.db.dao.DayLogDao
import com.iqbalansyori.izzah.data.db.toDomain
import com.iqbalansyori.izzah.data.db.toEntity
import com.iqbalansyori.izzah.domain.model.DayLog
import com.iqbalansyori.izzah.domain.repository.DayLogStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class DayLogStoreImpl(private val dao: DayLogDao) : DayLogStore {
    override fun observeAll(): Flow<List<DayLog>> = dao.observeAll().map { rows -> rows.map { it.toDomain() } }
    override suspend fun get(d: LocalDate): DayLog? = dao.get(d.toEpochDays().toLong())?.toDomain()
    override suspend fun upsert(d: DayLog) = dao.upsert(d.toEntity())
}
