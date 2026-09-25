package com.example.di

import android.content.Context
import com.example.data.local.HabitDatabase
import com.example.data.repository.HabitRepositoryImpl
import com.example.domain.repository.HabitRepository
import com.example.domain.usecase.CreateHabitUseCase
import com.example.domain.usecase.DeleteHabitUseCase
import com.example.domain.usecase.GetHabitStatsUseCase
import com.example.domain.usecase.GetHabitsWithStatusUseCase
import com.example.domain.usecase.HabitUseCases
import com.example.domain.usecase.ToggleHabitCompletionUseCase
import com.example.domain.usecase.UpdateHabitUseCase

interface AppContainer {
    val habitRepository: HabitRepository
    val habitUseCases: HabitUseCases
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database: HabitDatabase by lazy {
        HabitDatabase.getInstance(context)
    }

    override val habitRepository: HabitRepository by lazy {
        HabitRepositoryImpl(
            habitDao = database.habitDao(),
            habitLogDao = database.habitLogDao()
        )
    }

    override val habitUseCases: HabitUseCases by lazy {
        HabitUseCases(
            getHabitsWithStatus = GetHabitsWithStatusUseCase(habitRepository),
            toggleHabitCompletion = ToggleHabitCompletionUseCase(habitRepository),
            createHabit = CreateHabitUseCase(habitRepository),
            updateHabit = UpdateHabitUseCase(habitRepository),
            deleteHabit = DeleteHabitUseCase(habitRepository),
            getHabitStats = GetHabitStatsUseCase(habitRepository)
        )
    }
}
