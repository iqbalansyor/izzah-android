package com.iqbalansyori.izzah

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class SanityTest {
    @Test
    fun platformRuns() = assertThat(1 + 1).isEqualTo(2)
}
