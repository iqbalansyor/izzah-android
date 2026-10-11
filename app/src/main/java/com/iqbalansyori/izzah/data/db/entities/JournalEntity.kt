package com.iqbalansyori.izzah.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entry")
data class JournalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAtMs: Long,
    val epochDay: Long,
    val mood: Int?,
    val note: String,
)
