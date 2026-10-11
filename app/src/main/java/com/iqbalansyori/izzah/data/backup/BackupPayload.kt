package com.iqbalansyori.izzah.data.backup

import com.iqbalansyori.izzah.domain.model.GeoPoint
import kotlinx.serialization.Serializable

// pinHash, pinSalt, pinFailCount and pinLockedUntil are deliberately absent: a backup never carries PIN material
@Serializable
data class SettingsDto(
    val startEpochDay: Long? = null,
    val why: List<String> = emptyList(),
    val target: Int = 90,
    val rawan: List<String> = emptyList(),
    val biometric: Boolean = false,
    val notifPrayer: Boolean = true,
    val notifDzikir: Boolean = true,
    val notifCheckin: Boolean = true,
    val slots: List<SlotBackupDto> = emptyList(),
    val lockedPackages: List<String> = emptyList(),
    val lockMode: String = "PAUSE",
    val disguise: String = "IZZAH",
    val hideInRecents: Boolean = false,
    val location: GeoPoint? = null,
)

@Serializable
data class SlotBackupDto(val id: Int, val start: Int, val end: Int, val days: List<Int>, val enabled: Boolean)

@Serializable
data class DayLogDto(val epochDay: Long, val prayersMask: Int, val slip: Boolean, val triggersMask: Int)

@Serializable
data class JournalDto(val id: Long, val createdAtMs: Long, val epochDay: Long, val mood: Int?, val note: String)

@Serializable
data class JedaDto(val id: Long, val startedAtMs: Long, val completed: Boolean)

@Serializable
data class BackupPayload(
    val version: Int = 1,
    val settings: SettingsDto,
    val days: List<DayLogDto>,
    val journal: List<JournalDto>,
    val jeda: List<JedaDto>,
)

data class BackupContent(val days: List<DayLogDto>, val journal: List<JournalDto>, val jeda: List<JedaDto>)

interface BackupStore {
    suspend fun read(): BackupContent
    suspend fun replace(c: BackupContent)
}
