package com.iqbalansyori.izzah.data.db

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.iqbalansyori.izzah.domain.model.DayLog
import com.iqbalansyori.izzah.domain.model.Prayer
import com.iqbalansyori.izzah.domain.model.Trigger
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Test

class MappersTest {
    @Test
    fun `prayer mask roundtrip`() {
        val all = Prayer.entries.toSet()
        assertThat(all.toMask().toPrayers()).isEqualTo(all)
    }

    @Test
    fun `empty mask`() = assertThat(0.toPrayers()).isEmpty()

    @Test
    fun `day log roundtrip`() {
        val d = DayLog(LocalDate(2026, 10, 1), setOf(Prayer.ASR), true, setOf(Trigger.BORED))
        assertThat(d.toEntity().toDomain()).isEqualTo(d)
    }
}
