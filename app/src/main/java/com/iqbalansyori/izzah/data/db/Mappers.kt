package com.iqbalansyori.izzah.data.db

import com.iqbalansyori.izzah.data.db.entities.DayLogEntity
import com.iqbalansyori.izzah.domain.model.DayLog
import com.iqbalansyori.izzah.domain.model.Prayer
import com.iqbalansyori.izzah.domain.model.Trigger
import kotlinx.datetime.LocalDate

fun Set<Prayer>.toMask(): Int = fold(0) { acc, p -> acc or (1 shl p.ordinal) }

fun Int.toPrayers(): Set<Prayer> = Prayer.entries.filter { this and (1 shl it.ordinal) != 0 }.toSet()

fun Set<Trigger>.toTriggerMask(): Int = fold(0) { acc, t -> acc or (1 shl t.ordinal) }

fun Int.toTriggers(): Set<Trigger> = Trigger.entries.filter { this and (1 shl it.ordinal) != 0 }.toSet()

fun DayLogEntity.toDomain() = DayLog(LocalDate.fromEpochDays(epochDay.toInt()), prayersMask.toPrayers(), slip, triggersMask.toTriggers())

fun DayLog.toEntity() = DayLogEntity(date.toEpochDays().toLong(), prayers.toMask(), slip, triggers.toTriggerMask())
