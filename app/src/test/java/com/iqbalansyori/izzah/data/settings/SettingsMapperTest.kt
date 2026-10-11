package com.iqbalansyori.izzah.data.settings

import androidx.datastore.preferences.core.mutablePreferencesOf
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.iqbalansyori.izzah.domain.logic.SlotSchedule
import com.iqbalansyori.izzah.domain.model.Disguise
import com.iqbalansyori.izzah.domain.model.GeoPoint
import com.iqbalansyori.izzah.domain.model.LockMode
import com.iqbalansyori.izzah.domain.model.Rawan
import com.iqbalansyori.izzah.domain.model.Settings
import com.iqbalansyori.izzah.domain.model.Why
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Test

class SettingsMapperTest {
    private val full = Settings(
        onboardingDone = true,
        startDate = LocalDate(2026, 10, 1),
        why = setOf(Why.ALLAH, Why.PEACE),
        target = 40,
        rawan = setOf(Rawan.DAWN),
        pinHash = byteArrayOf(1, 2, 3),
        pinSalt = byteArrayOf(9, 8),
        biometric = true,
        pinFailCount = 2,
        pinLockedUntil = Instant.parse("2026-10-13T05:00:30Z"),
        notifPrayer = false,
        notifDzikir = false,
        notifCheckin = true,
        slots = SlotSchedule.defaults(setOf(Rawan.DAWN)),
        lockedPackages = setOf("com.a", "com.b"),
        lockMode = LockMode.FULL,
        disguise = Disguise.NOTES,
        hideInRecents = true,
        location = GeoPoint(-6.2, 106.8, "Jakarta"),
    )

    @Test
    fun `full settings roundtrip through preferences`() {
        val prefs = mutablePreferencesOf()
        SettingsMapper.write(prefs, full)
        assertThat(SettingsMapper.read(prefs)).isEqualTo(full)
    }

    @Test
    fun `empty preferences read as defaults`() = assertThat(SettingsMapper.read(mutablePreferencesOf())).isEqualTo(Settings())

    @Test
    fun `unknown enum names fall back instead of crashing`() {
        val prefs = mutablePreferencesOf()
        SettingsMapper.write(prefs, full)
        prefs[SettingsKeys.lockMode] = "SOMETHING_NEW"
        prefs[SettingsKeys.why] = "ALLAH,GONE"
        val read = SettingsMapper.read(prefs)
        assertThat(read.lockMode).isEqualTo(LockMode.PAUSE)
        assertThat(read.why).isEqualTo(setOf(Why.ALLAH))
    }
}
