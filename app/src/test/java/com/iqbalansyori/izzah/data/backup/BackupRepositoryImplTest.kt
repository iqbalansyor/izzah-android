package com.iqbalansyori.izzah.data.backup

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.iqbalansyori.izzah.data.security.BackupCrypto
import com.iqbalansyori.izzah.data.security.BackupError
import com.iqbalansyori.izzah.data.security.PinHasher
import com.iqbalansyori.izzah.domain.model.Disguise
import com.iqbalansyori.izzah.domain.model.GeoPoint
import com.iqbalansyori.izzah.domain.model.LockMode
import com.iqbalansyori.izzah.domain.model.Rawan
import com.iqbalansyori.izzah.domain.model.Settings
import com.iqbalansyori.izzah.domain.model.Why
import com.iqbalansyori.izzah.testing.FakeSettingsRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

class BackupRepositoryImplTest {
    private val iterations = 1000
    private val salt = PinHasher.newSalt()

    private fun settings(pin: String = "1234") = Settings(
        onboardingDone = true,
        startDate = LocalDate(2026, 8, 28),
        why = setOf(Why.ALLAH, Why.FAMILY),
        target = 40,
        rawan = setOf(Rawan.DAWN),
        pinSalt = salt,
        pinHash = PinHasher.hash(pin, salt, iterations),
        pinFailCount = 3,
        notifCheckin = false,
        lockedPackages = setOf("com.a"),
        lockMode = LockMode.FULL,
        disguise = Disguise.NOTES,
        hideInRecents = true,
        location = GeoPoint(-6.2, 106.8, "Jakarta"),
    )

    private val content = BackupContent(
        days = listOf(DayLogDto(20000, 5, false, 0), DayLogDto(20001, 1, true, 3)),
        journal = listOf(JournalDto(1, 1_700_000_000_000, 20000, 1, "note")),
        jeda = listOf(JedaDto(1, 1_700_000_100_000, true)),
    )

    private val sourceStore = FakeBackupStore(content)
    private val sourceSettings = FakeSettingsRepository(settings())
    private val source = BackupRepositoryImpl(sourceStore, sourceSettings, iterations)

    @Test
    fun `export then restore on a fresh install reproduces the data`() = runTest {
        val blob = source.export("1234")
        val freshStore = FakeBackupStore()
        val freshSettings = FakeSettingsRepository()
        val result = BackupRepositoryImpl(freshStore, freshSettings, iterations).restore(blob, "1234")
        assertThat(result.isSuccess).isTrue()
        assertThat(freshStore.content).isEqualTo(content)
        val s = freshSettings.current()
        assertThat(s.onboardingDone).isTrue()
        assertThat(s.target).isEqualTo(40)
        assertThat(s.why).isEqualTo(setOf(Why.ALLAH, Why.FAMILY))
        assertThat(s.disguise).isEqualTo(Disguise.NOTES)
        assertThat(s.location).isEqualTo(GeoPoint(-6.2, 106.8, "Jakarta"))
        assertThat(PinHasher.verify("1234", s.pinSalt!!, s.pinHash!!, iterations)).isTrue()
        assertThat(s.pinFailCount).isEqualTo(0)
        assertThat(s.pinLockedUntil).isNull()
    }

    @Test
    fun `the backup does not carry pin material`() = runTest {
        val plain = BackupCrypto.decrypt(source.export("1234"), "1234", iterations).decodeToString()
        assertThat(plain.contains("pinHash")).isEqualTo(false)
        assertThat(plain.contains("pinSalt")).isEqualTo(false)
    }

    @Test
    fun `export refuses a wrong current pin`() = runTest {
        assertThat(runCatching { source.export("9999") }.exceptionOrNull()).isEqualTo(BackupError.WrongPin)
    }

    @Test
    fun `restore with a wrong pin fails and leaves existing data untouched`() = runTest {
        val blob = source.export("1234")
        val existing = FakeBackupStore(BackupContent(listOf(DayLogDto(1, 1, false, 0)), emptyList(), emptyList()))
        val existingSettings = FakeSettingsRepository(Settings(target = 7))
        val result = BackupRepositoryImpl(existing, existingSettings, iterations).restore(blob, "0000")
        assertThat(result.exceptionOrNull()).isEqualTo(BackupError.WrongPin)
        assertThat(existing.content.days).isEqualTo(listOf(DayLogDto(1, 1, false, 0)))
        assertThat(existingSettings.current().target).isEqualTo(7)
    }

    @Test
    fun `random bytes are not a backup and change nothing`() = runTest {
        val existing = FakeBackupStore(content)
        val result = BackupRepositoryImpl(existing, FakeSettingsRepository(), iterations).restore("definitely not a backup file".toByteArray(), "1234")
        assertThat(result.exceptionOrNull()).isEqualTo(BackupError.NotABackup)
        assertThat(existing.content).isEqualTo(content)
    }

    @Test
    fun `a newer backup version is refused and changes nothing`() = runTest {
        val payload = BackupPayload(version = 2, settings = SettingsDto(), days = emptyList(), journal = emptyList(), jeda = emptyList())
        val blob = BackupCrypto.encrypt(Json.encodeToString(payload).toByteArray(), "1234", iterations)
        val existing = FakeBackupStore(content)
        val result = BackupRepositoryImpl(existing, FakeSettingsRepository(), iterations).restore(blob, "1234")
        assertThat(result.exceptionOrNull()).isEqualTo(BackupError.Unsupported)
        assertThat(existing.content).isEqualTo(content)
    }

    @Test
    fun `valid encryption but garbage json is not a backup`() = runTest {
        val blob = BackupCrypto.encrypt("{not json".toByteArray(), "1234", iterations)
        val existing = FakeBackupStore(content)
        val result = BackupRepositoryImpl(existing, FakeSettingsRepository(), iterations).restore(blob, "1234")
        assertThat(result.exceptionOrNull()).isEqualTo(BackupError.NotABackup)
        assertThat(existing.content).isEqualTo(content)
    }
}

class FakeBackupStore(var content: BackupContent = BackupContent(emptyList(), emptyList(), emptyList())) : BackupStore {
    override suspend fun read(): BackupContent = content
    override suspend fun replace(c: BackupContent) {
        content = c
    }
}
