package com.iqbalansyori.izzah.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.iqbalansyori.izzah.data.db.dao.DayLogDao
import com.iqbalansyori.izzah.data.db.dao.JedaDao
import com.iqbalansyori.izzah.data.db.dao.JournalDao
import com.iqbalansyori.izzah.data.db.entities.DayLogEntity
import com.iqbalansyori.izzah.data.db.entities.JedaEntity
import com.iqbalansyori.izzah.data.db.entities.JournalEntity
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(entities = [DayLogEntity::class, JournalEntity::class, JedaEntity::class], version = 1, exportSchema = false)
abstract class IzzahDatabase : RoomDatabase() {
    abstract fun dayLog(): DayLogDao
    abstract fun journal(): JournalDao
    abstract fun jeda(): JedaDao

    companion object {
        fun build(context: Context, passphrase: ByteArray, inMemory: Boolean = false): IzzahDatabase {
            val factory = SupportOpenHelperFactory(passphrase)
            val builder = if (inMemory) {
                Room.inMemoryDatabaseBuilder(context, IzzahDatabase::class.java)
            } else {
                Room.databaseBuilder(context, IzzahDatabase::class.java, "izzah.db")
            }
            return builder.openHelperFactory(factory).build()
        }
    }
}
