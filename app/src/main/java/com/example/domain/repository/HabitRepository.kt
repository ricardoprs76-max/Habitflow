package com.example.domain.repository

import com.example.domain.model.Habit
import com.example.domain.model.HabitLog
import com.example.domain.model.HabitSummaryStats
import com.example.domain.model.HabitWithStatus
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getActiveHabits(): Flow<List<Habit>>
    fun getAllHabits(): Flow<List<Habit>>
    fun getHabitById(id: Long): Flow<Habit?>
    suspend fun saveHabit(habit: Habit): Long
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(id: Long)
    suspend fun toggleHabitCompletion(habitId: Long, dateIso: String): Boolean
    fun getHabitsWithStatus(dateIso: String): Flow<List<HabitWithStatus>>
    fun getSummaryStats(dateIso: String): Flow<HabitSummaryStats>
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>>
}
