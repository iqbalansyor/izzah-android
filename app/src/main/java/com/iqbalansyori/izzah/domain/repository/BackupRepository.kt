package com.iqbalansyori.izzah.domain.repository

interface BackupRepository {
    // pin = the current PIN, re-entered by the user and checked against the stored hash first
    suspend fun export(pin: String): ByteArray

    // on failure nothing is modified; the exception is a BackupError
    suspend fun restore(blob: ByteArray, pin: String): Result<Unit>
}
