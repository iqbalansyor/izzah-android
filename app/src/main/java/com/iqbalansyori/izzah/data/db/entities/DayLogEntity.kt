package com.iqbalansyori.izzah.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_log")
data class DayLogEntity(
    @PrimaryKey val epochDay: Long,
    val prayersMask: Int,
    val slip: Boolean,
    val triggersMask: Int,
)
