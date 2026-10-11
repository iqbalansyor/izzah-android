package com.iqbalansyori.izzah.data.settings

import java.util.Base64
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.Preferences.Key
import com.iqbalansyori.izzah.domain.model.GeoPoint
import com.iqbalansyori.izzah.domain.model.Settings
import com.iqbalansyori.izzah.domain.model.Slot
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
private data class SlotDto(val id: Int, val start: Int, val end: Int, val days: List<Int>, val enabled: Boolean)

object SettingsMapper {
    private val json = Json { ignoreUnknownKeys = true }
    private val defaults = Settings()

    fun read(p: Preferences): Settings = Settings(
        onboardingDone = p[SettingsKeys.onboardingDone] ?: defaults.onboardingDone,
        startDate = p[SettingsKeys.startDay]?.let { LocalDate.fromEpochDays(it.toInt()) },
        why = p.enumSet(SettingsKeys.why),
        target = p[SettingsKeys.target] ?: defaults.target,
        rawan = p.enumSet(SettingsKeys.rawan),
        pinHash = p[SettingsKeys.pinHash]?.let(::decode),
        pinSalt = p[SettingsKeys.pinSalt]?.let(::decode),
        biometric = p[SettingsKeys.biometric] ?: defaults.biometric,
        pinFailCount = p[SettingsKeys.pinFailCount] ?: defaults.pinFailCount,
        pinLockedUntil = p[SettingsKeys.pinLockedUntil]?.let(Instant::fromEpochMilliseconds),
        notifPrayer = p[SettingsKeys.notifPrayer] ?: defaults.notifPrayer,
        notifDzikir = p[SettingsKeys.notifDzikir] ?: defaults.notifDzikir,
        notifCheckin = p[SettingsKeys.notifCheckin] ?: defaults.notifCheckin,
        slots = p[SettingsKeys.slots]?.let(::decodeSlots).orEmpty(),
        lockedPackages = p[SettingsKeys.lockedPackages]?.split(',')?.filter { it.isNotEmpty() }?.toSet().orEmpty(),
        lockMode = p[SettingsKeys.lockMode]?.let { n -> enumValues<com.iqbalansyori.izzah.domain.model.LockMode>().firstOrNull { it.name == n } }
            ?: defaults.lockMode,
        disguise = p[SettingsKeys.disguise]?.let { n -> enumValues<com.iqbalansyori.izzah.domain.model.Disguise>().firstOrNull { it.name == n } }
            ?: defaults.disguise,
        hideInRecents = p[SettingsKeys.hideInRecents] ?: defaults.hideInRecents,
        location = p[SettingsKeys.location]?.let { runCatching { json.decodeFromString(GeoPoint.serializer(), it) }.getOrNull() },
    )

    fun write(p: MutablePreferences, s: Settings) {
        p[SettingsKeys.onboardingDone] = s.onboardingDone
        p.putOrRemove(SettingsKeys.startDay, s.startDate?.toEpochDays()?.toLong())
        p[SettingsKeys.why] = s.why.joinToString(",") { it.name }
        p[SettingsKeys.target] = s.target
        p[SettingsKeys.rawan] = s.rawan.joinToString(",") { it.name }
        p.putOrRemove(SettingsKeys.pinHash, s.pinHash?.let(::encode))
        p.putOrRemove(SettingsKeys.pinSalt, s.pinSalt?.let(::encode))
        p[SettingsKeys.biometric] = s.biometric
        p[SettingsKeys.pinFailCount] = s.pinFailCount
        p.putOrRemove(SettingsKeys.pinLockedUntil, s.pinLockedUntil?.toEpochMilliseconds())
        p[SettingsKeys.notifPrayer] = s.notifPrayer
        p[SettingsKeys.notifDzikir] = s.notifDzikir
        p[SettingsKeys.notifCheckin] = s.notifCheckin
        p[SettingsKeys.slots] = json.encodeToString(ListSerializer(SlotDto.serializer()), s.slots.map(::toDto))
        p[SettingsKeys.lockedPackages] = s.lockedPackages.joinToString(",")
        p[SettingsKeys.lockMode] = s.lockMode.name
        p[SettingsKeys.disguise] = s.disguise.name
        p[SettingsKeys.hideInRecents] = s.hideInRecents
        p.putOrRemove(SettingsKeys.location, s.location?.let { json.encodeToString(GeoPoint.serializer(), it) })
    }

    private fun <T> MutablePreferences.putOrRemove(key: Key<T>, value: T?) {
        if (value == null) remove(key) else set(key, value)
    }

    private inline fun <reified E : Enum<E>> Preferences.enumSet(key: Key<String>): Set<E> =
        get(key)?.split(',')?.mapNotNull { n -> enumValues<E>().firstOrNull { it.name == n } }?.toSet().orEmpty()

    private fun encode(b: ByteArray) = Base64.getEncoder().encodeToString(b)
    private fun decode(s: String): ByteArray = Base64.getDecoder().decode(s)

    private fun toDto(s: Slot) = SlotDto(s.id, s.startMinute, s.endMinute, s.days.map { it.isoDayNumber }, s.enabled)

    private fun decodeSlots(raw: String): List<Slot> =
        runCatching { json.decodeFromString(ListSerializer(SlotDto.serializer()), raw) }.getOrDefault(emptyList())
            .map { Slot(it.id, it.start, it.end, it.days.map { d -> DayOfWeek(d) }.toSet(), it.enabled) }
}
