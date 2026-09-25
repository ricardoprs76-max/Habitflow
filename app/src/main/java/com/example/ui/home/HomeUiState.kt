package com.example.ui.home

import com.example.domain.model.Habit
import com.example.domain.model.HabitSummaryStats
import com.example.domain.model.HabitWithStatus
import com.example.domain.model.TimeOfDay

enum class HomeTab(val title: String) {
    TODAY("Hoy"),
    STATS("Estadísticas"),
    ALL_HABITS("Gestionar")
}

data class HomeUiState(
    val selectedDateIso: String = "",
    val selectedTab: HomeTab = HomeTab.TODAY,
    val selectedTimeOfDayFilter: TimeOfDay? = null,
    val habits: List<HabitWithStatus> = emptyList(),
    val stats: HabitSummaryStats = HabitSummaryStats(0, 0, 0, 0, 0),
    val isLoading: Boolean = false,
    val isAddEditSheetOpen: Boolean = false,
    val habitToEdit: Habit? = null,
    val userMessage: String? = null
)
