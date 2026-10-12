package com.iqbalansyori.izzah.domain.repository

import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class SuggestedPackagesTest {
    @Test
    fun `has no duplicates`() {
        assertThat(SUGGESTED_PACKAGES.size).isEqualTo(SUGGESTED_PACKAGES.toSet().size)
    }

    @Test
    fun `covers the common social and video apps and their lite versions`() {
        assertThat(SUGGESTED_PACKAGES).containsAll(
            "com.instagram.android", "com.instagram.lite", "com.google.android.youtube",
            "com.facebook.katana", "com.facebook.lite", "com.zhiliaoapp.musically", "com.zhiliaoapp.musically.go",
        )
    }
}
