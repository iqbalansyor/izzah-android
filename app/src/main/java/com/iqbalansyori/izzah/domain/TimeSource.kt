package com.iqbalansyori.izzah.domain

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.seconds

interface TimeSource {
    fun now(): Instant
    val zone: TimeZone
}

fun TimeSource.today(): LocalDate = now().toLocalDateTime(zone).date

fun TimeSource.localNow(): LocalDateTime = now().toLocalDateTime(zone)

fun TimeSource.todayFlow(): Flow<LocalDate> = flow {
    while (true) {
        val now = localNow()
        emit(now.date)
        val next = LocalDateTime(now.date.plus(1, DateTimeUnit.DAY), LocalTime(0, 0)).toInstant(zone)
        delay((next - now()).coerceAtLeast(1.seconds))
    }
}.distinctUntilChanged()
