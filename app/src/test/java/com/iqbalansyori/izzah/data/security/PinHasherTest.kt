package com.iqbalansyori.izzah.data.security

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

class PinHasherTest {
    @Test
    fun `verify roundtrip and reject`() {
        val s = PinHasher.newSalt()
        val h = PinHasher.hash("1234", s, 1000)
        assertThat(PinHasher.verify("1234", s, h, 1000)).isTrue()
        assertThat(PinHasher.verify("1235", s, h, 1000)).isFalse()
        assertThat(s.size).isEqualTo(16)
        assertThat(h.size).isEqualTo(32)
    }
}
