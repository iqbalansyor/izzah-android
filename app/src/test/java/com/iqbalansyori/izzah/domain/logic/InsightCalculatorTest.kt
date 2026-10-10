package com.iqbalansyori.izzah.domain.logic

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlinx.datetime.LocalDateTime
import org.junit.jupiter.api.Test

class InsightCalculatorTest {
    private fun t(h: Int, m: Int = 0) = LocalDateTime(2026, 10, 1, h, m)

    @Test
    fun `needs five events`() = assertThat(InsightCalculator.busiestWindow(List(4) { t(22) })).isNull()

    @Test
    fun `picks busiest even 2h window`() =
        assertThat(InsightCalculator.busiestWindow(listOf(t(22), t(23, 40), t(22, 10), t(13), t(9), t(23)))).isEqualTo(InsightWindow(22, 0))

    @Test
    fun `tie goes to earliest`() =
        assertThat(InsightCalculator.busiestWindow(listOf(t(1), t(1), t(9), t(9), t(15)))).isEqualTo(InsightWindow(0, 2))
}
