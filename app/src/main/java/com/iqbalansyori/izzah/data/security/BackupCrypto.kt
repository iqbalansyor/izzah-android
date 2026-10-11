package com.iqbalansyori.izzah.data.security

import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

sealed class BackupError : Exception() {
    data object NotABackup : BackupError()
    data object WrongPin : BackupError()
}

object BackupCrypto {
    private val MAGIC = "IZZ1".toByteArray()
    private const val SALT = 16
    private const val IV = 12
    private const val TAG_BYTES = 16

    fun encrypt(plain: ByteArray, pin: String, iterations: Int = PinHasher.ITERATIONS): ByteArray {
        val salt = PinHasher.newSalt()
        val iv = ByteArray(IV).also(SecureRandom()::nextBytes)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.ENCRYPT_MODE, SecretKeySpec(PinHasher.hash(pin, salt, iterations), "AES"), GCMParameterSpec(128, iv))
        }
        return MAGIC + salt + iv + cipher.doFinal(plain)
    }

    fun decrypt(blob: ByteArray, pin: String, iterations: Int = PinHasher.ITERATIONS): ByteArray {
        val head = MAGIC.size + SALT + IV
        if (blob.size < head + TAG_BYTES || !blob.copyOfRange(0, MAGIC.size).contentEquals(MAGIC)) throw BackupError.NotABackup
        val salt = blob.copyOfRange(MAGIC.size, MAGIC.size + SALT)
        val iv = blob.copyOfRange(MAGIC.size + SALT, head)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, SecretKeySpec(PinHasher.hash(pin, salt, iterations), "AES"), GCMParameterSpec(128, iv))
        }
        return try {
            cipher.doFinal(blob, head, blob.size - head)
        } catch (e: AEADBadTagException) {
            throw BackupError.WrongPin
        }
    }
}
