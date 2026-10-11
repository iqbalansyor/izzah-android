package com.iqbalansyori.izzah.data.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PinHasher {
    const val ITERATIONS = 120_000
    private val random = SecureRandom()

    fun newSalt(): ByteArray = ByteArray(16).also(random::nextBytes)

    fun hash(pin: String, salt: ByteArray, iterations: Int = ITERATIONS): ByteArray =
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(PBEKeySpec(pin.toCharArray(), salt, iterations, 256)).encoded

    fun verify(pin: String, salt: ByteArray, expected: ByteArray, iterations: Int = ITERATIONS): Boolean =
        MessageDigest.isEqual(hash(pin, salt, iterations), expected)
}
