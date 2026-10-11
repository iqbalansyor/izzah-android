package com.iqbalansyori.izzah.data.backup

import androidx.room.withTransaction
import com.iqbalansyori.izzah.data.db.IzzahDatabase
import com.iqbalansyori.izzah.data.db.entities.DayLogEntity
import com.iqbalansyori.izzah.data.db.entities.JedaEntity
import com.iqbalansyori.izzah.data.db.entities.JournalEntity

class DbBackupStore(private val db: IzzahDatabase) : BackupStore {
    override suspend fun read() = BackupContent(
        days = db.dayLog().all().map { DayLogDto(it.epochDay, it.prayersMask, it.slip, it.triggersMask) },
        journal = db.journal().all().map { JournalDto(it.id, it.createdAtMs, it.epochDay, it.mood, it.note) },
        jeda = db.jeda().all().map { JedaDto(it.id, it.startedAtMs, it.completed) },
    )

    override suspend fun replace(c: BackupContent) {
        db.withTransaction {
            db.dayLog().deleteAll()
            db.journal().deleteAll()
            db.jeda().deleteAll()
            db.dayLog().insertAll(c.days.map { DayLogEntity(it.epochDay, it.prayersMask, it.slip, it.triggersMask) })
            db.journal().insertAll(c.journal.map { JournalEntity(it.id, it.createdAtMs, it.epochDay, it.mood, it.note) })
            db.jeda().insertAll(c.jeda.map { JedaEntity(it.id, it.startedAtMs, it.completed) })
        }
    }
}
