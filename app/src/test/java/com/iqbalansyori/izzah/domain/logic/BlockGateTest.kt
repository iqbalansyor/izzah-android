package com.iqbalansyori.izzah.domain.logic

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.iqbalansyori.izzah.domain.model.Slot
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class BlockGateTest {
    private val now = LocalDateTime(2026, 10, 12, 23, 0)
    private val inst = Instant.parse("2026-10-12T16:00:00Z")
    private val slots = listOf(Slot(1, 22 * 60, 60, SlotSchedule.EVERY_DAY, true))

    @Test
    fun `blocks locked app inside slot`() = assertThat(BlockGate.shouldBlock("x", now, inst, setOf("x"), slots, emptyMap())).isTrue()

    @Test
    fun `ignores unlocked app`() = assertThat(BlockGate.shouldBlock("y", now, inst, setOf("x"), slots, emptyMap())).isFalse()

    @Test
    fun `valid pass lets through`() = assertThat(BlockGate.shouldBlock("x", now, inst, setOf("x"), slots, mapOf("x" to inst.plus(1.minutes)))).isFalse()

    @Test
    fun `expired pass blocks`() = assertThat(BlockGate.shouldBlock("x", now, inst, setOf("x"), slots, mapOf("x" to inst.minus(1.seconds)))).isTrue()

    @Test
    fun `outside slot never blocks`() =
        assertThat(BlockGate.shouldBlock("x", LocalDateTime(2026, 10, 12, 12, 0), inst, setOf("x"), slots, emptyMap())).isFalse()
}
