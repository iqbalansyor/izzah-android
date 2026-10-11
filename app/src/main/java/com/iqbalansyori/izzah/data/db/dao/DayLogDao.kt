package com.iqbalansyori.izzah.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.iqbalansyori.izzah.data.db.entities.DayLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DayLogDao {
    @Query("SELECT * FROM day_log WHERE epochDay BETWEEN :from AND :to")
    fun observeRange(from: Long, to: Long): Flow<List<DayLogEntity>>

    @Query("SELECT * FROM day_log")
    fun observeAll(): Flow<List<DayLogEntity>>

    @Query("SELECT * FROM day_log WHERE epochDay = :d")
    suspend fun get(d: Long): DayLogEntity?

    @Upsert
    suspend fun upsert(e: DayLogEntity)

    @Query("SELECT * FROM day_log")
    suspend fun all(): List<DayLogEntity>

    @Insert
    suspend fun insertAll(e: List<DayLogEntity>)
}
