package com.iqbalansyori.izzah.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.iqbalansyori.izzah.data.db.entities.JournalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entry ORDER BY createdAtMs DESC")
    fun observeAll(): Flow<List<JournalEntity>>

    @Query("SELECT DISTINCT epochDay FROM journal_entry")
    fun observeDays(): Flow<List<Long>>

    @Insert
    suspend fun insert(e: JournalEntity): Long

    @Query("SELECT * FROM journal_entry")
    suspend fun all(): List<JournalEntity>

    @Insert
    suspend fun insertAll(e: List<JournalEntity>)
}
