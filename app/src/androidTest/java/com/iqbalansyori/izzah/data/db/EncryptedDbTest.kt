package com.iqbalansyori.izzah.data.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import com.iqbalansyori.izzah.data.db.entities.DayLogEntity
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EncryptedDbTest {
    @Test
    fun writesAndFileIsNotPlainSqlite() = runBlocking {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        System.loadLibrary("sqlcipher")
        ctx.deleteDatabase("izzah.db")
        val db = IzzahDatabase.build(ctx, ByteArray(32) { 7 })
        db.dayLog().upsert(DayLogEntity(20000, 3, false, 0))
        db.close()
        val header = ctx.getDatabasePath("izzah.db").readBytes().copyOf(16).decodeToString()
        assertThat(header.startsWith("SQLite format 3")).isFalse()
        val again = IzzahDatabase.build(ctx, ByteArray(32) { 7 })
        assertThat(again.dayLog().get(20000)?.prayersMask).isEqualTo(3)
        again.close()
    }
}
