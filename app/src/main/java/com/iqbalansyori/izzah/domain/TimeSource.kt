package com.iqbalansyori.izzah.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

interface TimeSource {
    fun now(): Instant
    val zone: TimeZone
}

fun TimeSource.today(): LocalDate = now().toLocalDateTime(zone).date

fun TimeSource.localNow(): LocalDateTime = now().toLocalDateTime(zone)
