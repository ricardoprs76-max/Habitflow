package com.example.domain.usecase

import com.example.domain.model.Habit
import com.example.domain.model.HabitSummaryStats
import com.example.domain.model.HabitWithStatus
import com.example.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow

class GetHabitsWithStatusUseCase(private val repository: HabitRepository) {
    operator fun invoke(dateIso: String): Flow<List<HabitWithStatus>> {
        return repository.getHabitsWithStatus(dateIso)
    }
}

class ToggleHabitCompletionUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(habitId: Long, dateIso: String): Boolean {
        return repository.toggleHabitCompletion(habitId, dateIso)
    }
}

class CreateHabitUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(habit: Habit): Long {
        require(habit.title.isNotBlank()) { "El título del hábito no puede estar vacío" }
        return repository.saveHabit(habit)
    }
}

class UpdateHabitUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(habit: Habit) {
        require(habit.title.isNotBlank()) { "El título del hábito no puede estar vacío" }
        repository.updateHabit(habit)
    }
}

class DeleteHabitUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(habitId: Long) {
        repository.deleteHabit(habitId)
    }
}

class GetHabitStatsUseCase(private val repository: HabitRepository) {
    operator fun invoke(dateIso: String): Flow<HabitSummaryStats> {
        return repository.getSummaryStats(dateIso)
    }
}

class HabitUseCases(
    val getHabitsWithStatus: GetHabitsWithStatusUseCase,
    val toggleHabitCompletion: ToggleHabitCompletionUseCase,
    val createHabit: CreateHabitUseCase,
    val updateHabit: UpdateHabitUseCase,
    val deleteHabit: DeleteHabitUseCase,
    val getHabitStats: GetHabitStatsUseCase
)
