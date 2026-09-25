package com.example.data.repository

import com.example.data.local.dao.HabitDao
import com.example.data.local.dao.HabitLogDao
import com.example.data.local.entity.HabitLogEntity
import com.example.data.mapper.HabitMapper
import com.example.domain.model.Habit
import com.example.domain.model.HabitLog
import com.example.domain.model.HabitSummaryStats
import com.example.domain.model.HabitWithStatus
import com.example.domain.repository.HabitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HabitRepositoryImpl(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao
) : HabitRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun getActiveHabits(): Flow<List<Habit>> {
        return habitDao.getActiveHabits().map { list ->
            list.map { HabitMapper.toDomain(it) }
        }
    }

    override fun getAllHabits(): Flow<List<Habit>> {
        return habitDao.getAllHabits().map { list ->
            list.map { HabitMapper.toDomain(it) }
        }
    }

    override fun getHabitById(id: Long): Flow<Habit?> {
        return habitDao.observeHabitById(id).map { entity ->
            entity?.let { HabitMapper.toDomain(it) }
        }
    }

    override suspend fun saveHabit(habit: Habit): Long = withContext(Dispatchers.IO) {
        val entity = HabitMapper.toEntity(habit)
        habitDao.insertHabit(entity)
    }

    override suspend fun updateHabit(habit: Habit) = withContext(Dispatchers.IO) {
        val entity = HabitMapper.toEntity(habit)
        habitDao.updateHabit(entity)
    }

    override suspend fun deleteHabit(id: Long) = withContext(Dispatchers.IO) {
        habitDao.deleteHabitById(id)
    }

    override suspend fun toggleHabitCompletion(habitId: Long, dateIso: String): Boolean = withContext(Dispatchers.IO) {
        val count = habitLogDao.getCompletionCount(habitId, dateIso)
        if (count > 0) {
            habitLogDao.deleteLog(habitId, dateIso)
            false
        } else {
            habitLogDao.insertLog(
                HabitLogEntity(
                    habitId = habitId,
                    dateIso = dateIso,
                    completedAt = System.currentTimeMillis()
                )
            )
            true
        }
    }

    override fun getHabitsWithStatus(dateIso: String): Flow<List<HabitWithStatus>> {
        return combine(habitDao.getActiveHabits(), habitLogDao.getAllLogs()) { habitEntities, logEntities ->
            val habits = habitEntities.map { HabitMapper.toDomain(it) }
            val logsByHabit = logEntities.groupBy { it.habitId }

            habits.map { habit ->
                val habitLogs = logsByHabit[habit.id] ?: emptyList()
                val logDatesSet = habitLogs.map { it.dateIso }.toSet()

                val isCompletedToday = logDatesSet.contains(dateIso)
                val currentStreak = calculateCurrentStreak(logDatesSet, dateIso)
                val bestStreak = calculateBestStreak(logDatesSet)
                val last7Days = calculateLast7Days(logDatesSet, dateIso)

                val completionRate = if (last7Days.isNotEmpty()) {
                    val completedCount = last7Days.count { it }
                    ((completedCount.toFloat() / last7Days.size.toFloat()) * 100).toInt()
                } else {
                    0
                }

                HabitWithStatus(
                    habit = habit,
                    isCompletedToday = isCompletedToday,
                    currentStreak = currentStreak,
                    bestStreak = maxOf(bestStreak, currentStreak),
                    completionRatePercentage = completionRate,
                    last7DaysStatus = last7Days
                )
            }
        }
    }

    override fun getSummaryStats(dateIso: String): Flow<HabitSummaryStats> {
        return getHabitsWithStatus(dateIso).map { list ->
            val total = list.size
            val completed = list.count { it.isCompletedToday }
            val percent = if (total > 0) ((completed.toFloat() / total) * 100).toInt() else 0
            val activeStreaks = list.count { it.currentStreak > 0 }
            val bestOverall = list.maxOfOrNull { it.bestStreak } ?: 0

            HabitSummaryStats(
                totalHabits = total,
                completedToday = completed,
                completionPercentageToday = percent,
                activeStreaksCount = activeStreaks,
                bestOverallStreak = bestOverall
            )
        }
    }

    override fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>> {
        return habitLogDao.getLogsForHabit(habitId).map { entities ->
            entities.map { HabitMapper.toDomain(it) }
        }
    }

    private fun calculateCurrentStreak(completedDates: Set<String>, referenceDateIso: String): Int {
        val calendar = Calendar.getInstance()
        try {
            val refDate = dateFormat.parse(referenceDateIso) ?: Date()
            calendar.time = refDate
        } catch (_: Exception) {
            calendar.time = Date()
        }

        var streak = 0
        val isTodayDone = completedDates.contains(referenceDateIso)

        if (isTodayDone) {
            streak++
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            // Check if streak was alive until yesterday
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayIso = dateFormat.format(calendar.time)
            if (!completedDates.contains(yesterdayIso)) {
                return 0
            }
        }

        while (true) {
            val dateStr = dateFormat.format(calendar.time)
            if (completedDates.contains(dateStr)) {
                streak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    private fun calculateBestStreak(completedDates: Set<String>): Int {
        if (completedDates.isEmpty()) return 0
        val sortedDates = completedDates.mapNotNull {
            try {
                dateFormat.parse(it)
            } catch (_: Exception) {
                null
            }
        }.sorted()

        if (sortedDates.isEmpty()) return 0

        var maxStreak = 1
        var currentStreak = 1

        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()

        for (i in 1 until sortedDates.size) {
            cal1.time = sortedDates[i - 1]
            cal2.time = sortedDates[i]

            cal1.add(Calendar.DAY_OF_YEAR, 1)
            if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
            ) {
                currentStreak++
                if (currentStreak > maxStreak) {
                    maxStreak = currentStreak
                }
            } else {
                currentStreak = 1
            }
        }

        return maxStreak
    }

    private fun calculateLast7Days(completedDates: Set<String>, referenceDateIso: String): List<Boolean> {
        val calendar = Calendar.getInstance()
        try {
            val refDate = dateFormat.parse(referenceDateIso) ?: Date()
            calendar.time = refDate
        } catch (_: Exception) {
            calendar.time = Date()
        }

        val result = mutableListOf<Boolean>()
        // From 6 days ago up to reference date (7 days total)
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        for (i in 0 until 7) {
            val dateStr = dateFormat.format(calendar.time)
            result.add(completedDates.contains(dateStr))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return result
    }
}
