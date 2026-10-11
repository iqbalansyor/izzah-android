package com.iqbalansyori.izzah.data.backup

import com.iqbalansyori.izzah.data.security.BackupCrypto
import com.iqbalansyori.izzah.data.security.BackupError
import com.iqbalansyori.izzah.data.security.PinHasher
import com.iqbalansyori.izzah.domain.model.Disguise
import com.iqbalansyori.izzah.domain.model.LockMode
import com.iqbalansyori.izzah.domain.model.Rawan
import com.iqbalansyori.izzah.domain.model.Settings
import com.iqbalansyori.izzah.domain.model.Slot
import com.iqbalansyori.izzah.domain.model.Why
import com.iqbalansyori.izzah.domain.repository.BackupRepository
import com.iqbalansyori.izzah.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class BackupRepositoryImpl(
    private val store: BackupStore,
    private val settings: SettingsRepository,
    private val iterations: Int = PinHasher.ITERATIONS,
) : BackupRepository {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    override suspend fun export(pin: String): ByteArray = withContext(Dispatchers.Default) {
        val s = settings.current()
        val salt = s.pinSalt
        val hash = s.pinHash
        if (salt == null || hash == null || !PinHasher.verify(pin, salt, hash, iterations)) throw BackupError.WrongPin
        val content = store.read()
        val payload = BackupPayload(
            settings = s.toDto(),
            days = content.days,
            journal = content.journal,
            jeda = content.jeda,
        )
        BackupCrypto.encrypt(json.encodeToString(BackupPayload.serializer(), payload).toByteArray(), pin, iterations)
    }

    // decrypt, parse and validate everything before touching any store, so a bad file can never leave half a restore behind
    override suspend fun restore(blob: ByteArray, pin: String): Result<Unit> = withContext(Dispatchers.Default) {
        val payload = try {
            val plain = BackupCrypto.decrypt(blob, pin, iterations)
            val parsed = try {
                json.decodeFromString(BackupPayload.serializer(), plain.decodeToString())
            } catch (e: SerializationException) {
                throw BackupError.NotABackup
            } catch (e: IllegalArgumentException) {
                throw BackupError.NotABackup
            }
            if (parsed.version != 1) throw BackupError.Unsupported
            if (!parsed.isSane()) throw BackupError.NotABackup
            parsed
        } catch (e: BackupError) {
            return@withContext Result.failure(e)
        }
        store.replace(BackupContent(payload.days, payload.journal, payload.jeda))
        val salt = PinHasher.newSalt()
        val hash = PinHasher.hash(pin, salt, iterations)
        settings.update { payload.settings.toSettings(salt, hash) }
        Result.success(Unit)
    }

    private fun BackupPayload.isSane() =
        settings.target in 1..3650 &&
            days.all { it.prayersMask in 0..31 && it.triggersMask in 0..63 } &&
            journal.all { it.mood == null || it.mood in 0..3 }

    private fun Settings.toDto() = SettingsDto(
        startEpochDay = startDate?.toEpochDays()?.toLong(),
        why = why.map { it.name },
        target = target,
        rawan = rawan.map { it.name },
        biometric = biometric,
        notifPrayer = notifPrayer,
        notifDzikir = notifDzikir,
        notifCheckin = notifCheckin,
        slots = slots.map { SlotBackupDto(it.id, it.startMinute, it.endMinute, it.days.map { d -> d.ordinal + 1 }, it.enabled) },
        lockedPackages = lockedPackages.toList(),
        lockMode = lockMode.name,
        disguise = disguise.name,
        hideInRecents = hideInRecents,
        location = location,
    )

    private fun SettingsDto.toSettings(salt: ByteArray, hash: ByteArray) = Settings(
        onboardingDone = true,
        startDate = startEpochDay?.let { LocalDate.fromEpochDays(it.toInt()) },
        why = why.mapNotNull { n -> Why.entries.firstOrNull { it.name == n } }.toSet(),
        target = target,
        rawan = rawan.mapNotNull { n -> Rawan.entries.firstOrNull { it.name == n } }.toSet(),
        pinHash = hash,
        pinSalt = salt,
        biometric = biometric,
        notifPrayer = notifPrayer,
        notifDzikir = notifDzikir,
        notifCheckin = notifCheckin,
        slots = slots.map { Slot(it.id, it.start, it.end, it.days.map { d -> DayOfWeek(d) }.toSet(), it.enabled) },
        lockedPackages = lockedPackages.toSet(),
        lockMode = LockMode.entries.firstOrNull { it.name == lockMode } ?: LockMode.PAUSE,
        disguise = Disguise.entries.firstOrNull { it.name == disguise } ?: Disguise.IZZAH,
        hideInRecents = hideInRecents,
        location = location,
    )
}
