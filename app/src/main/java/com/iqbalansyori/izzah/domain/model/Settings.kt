package com.iqbalansyori.izzah.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

data class Settings(
    val onboardingDone: Boolean = false,
    val startDate: LocalDate? = null,
    val why: Set<Why> = emptySet(),
    val target: Int = 90,
    val rawan: Set<Rawan> = emptySet(),
    val pinHash: ByteArray? = null,
    val pinSalt: ByteArray? = null,
    val biometric: Boolean = false,
    val pinFailCount: Int = 0,
    val pinLockedUntil: Instant? = null,
    val notifPrayer: Boolean = true,
    val notifDzikir: Boolean = true,
    val notifCheckin: Boolean = true,
    val slots: List<Slot> = emptyList(),
    val lockedPackages: Set<String> = emptySet(),
    val lockMode: LockMode = LockMode.PAUSE,
    val disguise: Disguise = Disguise.IZZAH,
    val hideInRecents: Boolean = false,
    val location: GeoPoint? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Settings) return false
        return onboardingDone == other.onboardingDone && startDate == other.startDate && why == other.why &&
            target == other.target && rawan == other.rawan && pinHash.contentEq(other.pinHash) &&
            pinSalt.contentEq(other.pinSalt) && biometric == other.biometric && pinFailCount == other.pinFailCount &&
            pinLockedUntil == other.pinLockedUntil && notifPrayer == other.notifPrayer &&
            notifDzikir == other.notifDzikir && notifCheckin == other.notifCheckin && slots == other.slots &&
            lockedPackages == other.lockedPackages && lockMode == other.lockMode && disguise == other.disguise &&
            hideInRecents == other.hideInRecents && location == other.location
    }

    override fun hashCode(): Int {
        var h = onboardingDone.hashCode()
        h = 31 * h + (startDate?.hashCode() ?: 0)
        h = 31 * h + why.hashCode()
        h = 31 * h + target
        h = 31 * h + rawan.hashCode()
        h = 31 * h + (pinHash?.contentHashCode() ?: 0)
        h = 31 * h + (pinSalt?.contentHashCode() ?: 0)
        h = 31 * h + biometric.hashCode()
        h = 31 * h + pinFailCount
        h = 31 * h + (pinLockedUntil?.hashCode() ?: 0)
        h = 31 * h + notifPrayer.hashCode()
        h = 31 * h + notifDzikir.hashCode()
        h = 31 * h + notifCheckin.hashCode()
        h = 31 * h + slots.hashCode()
        h = 31 * h + lockedPackages.hashCode()
        h = 31 * h + lockMode.hashCode()
        h = 31 * h + disguise.hashCode()
        h = 31 * h + hideInRecents.hashCode()
        h = 31 * h + (location?.hashCode() ?: 0)
        return h
    }

    private fun ByteArray?.contentEq(o: ByteArray?) = if (this == null || o == null) this == o else contentEquals(o)
}
