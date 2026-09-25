package com.example

import com.example.domain.model.Habit
import com.example.domain.model.HabitCategory
import com.example.domain.model.HabitLog
import com.example.domain.model.HabitSummaryStats
import com.example.domain.model.HabitWithStatus
import com.example.domain.model.TimeOfDay
import com.example.domain.repository.HabitRepository
import com.example.domain.usecase.CreateHabitUseCase
import com.example.domain.usecase.GetHabitsWithStatusUseCase
import com.example.domain.usecase.ToggleHabitCompletionUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitFlowDomainTest {

    private class FakeHabitRepository : HabitRepository {
        val habits = mutableListOf<Habit>()
        val logs = mutableListOf<HabitLog>()

        override fun getActiveHabits(): Flow<List<Habit>> = flowOf(habits)
        override fun getAllHabits(): Flow<List<Habit>> = flowOf(habits)
        override fun getHabitById(id: Long): Flow<Habit?> = flowOf(habits.find { it.id == id })

        override suspend fun saveHabit(habit: Habit): Long {
            val id = if (habit.id == 0L) (habits.size + 1).toLong() else habit.id
            val newHabit = habit.copy(id = id)
            habits.removeAll { it.id == id }
            habits.add(newHabit)
            return id
        }

        override suspend fun updateHabit(habit: Habit) {
            habits.removeAll { it.id == habit.id }
            habits.add(habit)
        }

        override suspend fun deleteHabit(id: Long) {
            habits.removeAll { it.id == id }
            logs.removeAll { it.habitId == id }
        }

        override suspend fun toggleHabitCompletion(habitId: Long, dateIso: String): Boolean {
            val existing = logs.find { it.habitId == habitId && it.dateIso == dateIso }
            return if (existing != null) {
                logs.remove(existing)
                false
            } else {
                logs.add(HabitLog(id = logs.size + 1L, habitId = habitId, dateIso = dateIso))
                true
            }
        }

        override fun getHabitsWithStatus(dateIso: String): Flow<List<HabitWithStatus>> {
            val list = habits.map { h ->
                val isDone = logs.any { it.habitId == h.id && it.dateIso == dateIso }
                HabitWithStatus(
                    habit = h,
                    isCompletedToday = isDone,
                    currentStreak = if (isDone) 1 else 0,
                    bestStreak = 1,
                    completionRatePercentage = if (isDone) 100 else 0
                )
            }
            return flowOf(list)
        }

        override fun getSummaryStats(dateIso: String): Flow<HabitSummaryStats> {
            val total = habits.size
            val done = habits.count { h -> logs.any { it.habitId == h.id && it.dateIso == dateIso } }
            return flowOf(
                HabitSummaryStats(
                    totalHabits = total,
                    completedToday = done,
                    completionPercentageToday = if (total > 0) (done * 100) / total else 0,
                    activeStreaksCount = done,
                    bestOverallStreak = 1
                )
            )
        }

        override fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>> =
            flowOf(logs.filter { it.habitId == habitId })
    }

    @Test
    fun createHabit_persistsSuccessfully() = runTest {
        val repo = FakeHabitRepository()
        val createUseCase = CreateHabitUseCase(repo)

        val habit = Habit(
            title = "Leer 15 páginas",
            category = HabitCategory.LEARNING,
            timeOfDay = TimeOfDay.NIGHT
        )

        val id = createUseCase(habit)
        assertEquals(1L, id)
        assertEquals(1, repo.habits.size)
        assertEquals("Leer 15 páginas", repo.habits[0].title)
    }

    @Test
    fun toggleHabit_marksAndUnmarksCompletion() = runTest {
        val repo = FakeHabitRepository()
        val createUseCase = CreateHabitUseCase(repo)
        val toggleUseCase = ToggleHabitCompletionUseCase(repo)

        val id = createUseCase(Habit(title = "Caminata 30 min"))
        val dateIso = "2026-09-20"

        val firstToggle = toggleUseCase(id, dateIso)
        assertTrue(firstToggle)
        assertEquals(1, repo.logs.size)

        val secondToggle = toggleUseCase(id, dateIso)
        assertEquals(false, secondToggle)
        assertEquals(0, repo.logs.size)
    }
}
