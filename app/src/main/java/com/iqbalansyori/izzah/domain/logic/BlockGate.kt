package com.iqbalansyori.izzah.domain.logic

import com.iqbalansyori.izzah.domain.model.Slot
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime

object BlockGate {
    fun shouldBlock(
        pkg: String,
        now: LocalDateTime,
        nowInstant: Instant,
        locked: Set<String>,
        slots: List<Slot>,
        passes: Map<String, Instant>,
    ): Boolean {
        if (pkg !in locked) return false
        if (passes[pkg]?.let { it > nowInstant } == true) return false
        return SlotSchedule.activeUntil(slots, now) != null
    }
}
