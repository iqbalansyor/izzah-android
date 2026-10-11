package com.iqbalansyori.izzah.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.iqbalansyori.izzah.data.db.entities.JedaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JedaDao {
    @Insert
    suspend fun insert(e: JedaEntity): Long

    @Query("UPDATE jeda_event SET completed = 1 WHERE id = :id")
    suspend fun complete(id: Long)

    @Query("SELECT * FROM jeda_event")
    fun observeAll(): Flow<List<JedaEntity>>

    @Query("SELECT * FROM jeda_event")
    suspend fun all(): List<JedaEntity>

    @Insert
    suspend fun insertAll(e: List<JedaEntity>)
}
