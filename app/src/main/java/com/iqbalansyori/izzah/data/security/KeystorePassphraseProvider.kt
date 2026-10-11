package com.iqbalansyori.izzah.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class KeystorePassphraseProvider(private val context: Context) {
    // plain SharedPreferences, not DataStore: Room needs this synchronously before it opens
    private val prefs get() = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    suspend fun passphrase(): ByteArray = withContext(Dispatchers.IO) {
        val stored = prefs.getString(KEY_BLOB, null)
        if (stored != null) {
            decrypt(Base64.decode(stored, Base64.NO_WRAP))
        } else {
            val fresh = ByteArray(32).also(SecureRandom()::nextBytes)
            prefs.edit().putString(KEY_BLOB, Base64.encodeToString(encrypt(fresh), Base64.NO_WRAP)).commit()
            fresh
        }
    }

    suspend fun destroy() = withContext(Dispatchers.IO) {
        prefs.edit().clear().commit()
        KeyStore.getInstance(KEYSTORE).apply { load(null) }.deleteEntry(ALIAS)
    }

    private fun wrapKey(): SecretKey {
        val ks = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (ks.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val spec = KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE).apply { init(spec) }.generateKey()
    }

    private fun encrypt(plain: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORM).apply { init(Cipher.ENCRYPT_MODE, wrapKey()) }
        return cipher.iv + cipher.doFinal(plain)
    }

    private fun decrypt(blob: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORM).apply {
            init(Cipher.DECRYPT_MODE, wrapKey(), GCMParameterSpec(128, blob.copyOfRange(0, IV_BYTES)))
        }
        return cipher.doFinal(blob, IV_BYTES, blob.size - IV_BYTES)
    }

    private companion object {
        const val PREFS = "izzah_keys"
        const val KEY_BLOB = "db_passphrase"
        const val KEYSTORE = "AndroidKeyStore"
        const val ALIAS = "izzah_db_wrap"
        const val TRANSFORM = "AES/GCM/NoPadding"
        const val IV_BYTES = 12
    }
}
