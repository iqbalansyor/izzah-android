package com.iqbalansyori.izzah.data

import com.iqbalansyori.izzah.domain.TimeSource
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

class SystemTimeSource : TimeSource {
    override fun now(): Instant = Clock.System.now()
    override val zone: TimeZone get() = TimeZone.currentSystemDefault()
}
