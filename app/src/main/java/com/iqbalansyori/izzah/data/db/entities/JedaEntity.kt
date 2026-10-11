package com.iqbalansyori.izzah.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jeda_event")
data class JedaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAtMs: Long,
    val completed: Boolean,
)
