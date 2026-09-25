package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.HabitLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitLogDao {

    @Query("SELECT * FROM habit_logs WHERE dateIso = :dateIso")
    fun getLogsForDate(dateIso: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY dateIso DESC")
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs ORDER BY dateIso DESC")
    fun getAllLogs(): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND dateIso >= :startDateIso ORDER BY dateIso ASC")
    fun getLogsForHabitFrom(habitId: Long, startDateIso: String): Flow<List<HabitLogEntity>>

    @Query("SELECT COUNT(*) FROM habit_logs WHERE habitId = :habitId AND dateIso = :dateIso")
    suspend fun getCompletionCount(habitId: Long, dateIso: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLogEntity): Long

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND dateIso = :dateIso")
    suspend fun deleteLog(habitId: Long, dateIso: String)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteAllLogsForHabit(habitId: Long)
}
