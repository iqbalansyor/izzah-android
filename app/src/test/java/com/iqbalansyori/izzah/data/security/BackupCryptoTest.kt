package com.iqbalansyori.izzah.data.security

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import org.junit.jupiter.api.Test

class BackupCryptoTest {
    private val data = "{\"x\":1}".toByteArray()

    @Test
    fun roundtrip() =
        assertThat(BackupCrypto.decrypt(BackupCrypto.encrypt(data, "1234", 1000), "1234", 1000).decodeToString()).isEqualTo("{\"x\":1}")

    @Test
    fun `wrong pin`() {
        assertFailure { BackupCrypto.decrypt(BackupCrypto.encrypt(data, "1234", 1000), "0000", 1000) }.isEqualTo(BackupError.WrongPin)
    }

    @Test
    fun `not a backup`() {
        assertFailure { BackupCrypto.decrypt("hello world, not ours".toByteArray(), "1234", 1000) }.isEqualTo(BackupError.NotABackup)
    }

    @Test
    fun `truncated file`() {
        val b = BackupCrypto.encrypt(data, "1234", 1000)
        assertFailure { BackupCrypto.decrypt(b.copyOf(20), "1234", 1000) }.isEqualTo(BackupError.NotABackup)
    }

    @Test
    fun `two encryptions differ`() =
        assertThat(BackupCrypto.encrypt(data, "1234", 1000).contentEquals(BackupCrypto.encrypt(data, "1234", 1000))).isFalse()
}
