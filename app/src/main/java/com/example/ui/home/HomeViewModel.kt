package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Habit
import com.example.domain.model.TimeOfDay
import com.example.domain.usecase.HabitUseCases
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val useCases: HabitUseCases
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val todayIso = dateFormat.format(Date())

    private val _selectedDate = MutableStateFlow(todayIso)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedTab = MutableStateFlow(HomeTab.TODAY)
    val selectedTab: StateFlow<HomeTab> = _selectedTab.asStateFlow()

    private val _selectedTimeOfDayFilter = MutableStateFlow<TimeOfDay?>(null)
    val selectedTimeOfDayFilter: StateFlow<TimeOfDay?> = _selectedTimeOfDayFilter.asStateFlow()

    private val _isAddEditSheetOpen = MutableStateFlow(false)
    val isAddEditSheetOpen: StateFlow<Boolean> = _isAddEditSheetOpen.asStateFlow()

    private val _habitToEdit = MutableStateFlow<Habit?>(null)
    val habitToEdit: StateFlow<Habit?> = _habitToEdit.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Reactive streams driven by selected date
    private val habitsFlow = _selectedDate.flatMapLatest { dateIso ->
        useCases.getHabitsWithStatus(dateIso)
    }

    private val statsFlow = _selectedDate.flatMapLatest { dateIso ->
        useCases.getHabitStats(dateIso)
    }

    private data class SheetStateHolder(
        val isOpen: Boolean,
        val habitToEdit: Habit?,
        val message: String?
    )

    private val sheetFlow = combine(
        _isAddEditSheetOpen,
        _habitToEdit,
        _userMessage
    ) { open, habit, msg ->
        SheetStateHolder(open, habit, msg)
    }

    private data class FiltersHolder(
        val date: String,
        val tab: HomeTab,
        val timeOfDay: TimeOfDay?
    )

    private val filtersFlow = combine(
        _selectedDate,
        _selectedTab,
        _selectedTimeOfDayFilter
    ) { date, tab, tod ->
        FiltersHolder(date, tab, tod)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        filtersFlow,
        habitsFlow,
        statsFlow,
        sheetFlow
    ) { filters, habits, stats, sheet ->
        val filteredHabits = if (filters.timeOfDay != null) {
            habits.filter { it.habit.timeOfDay == filters.timeOfDay }
        } else {
            habits
        }

        HomeUiState(
            selectedDateIso = filters.date,
            selectedTab = filters.tab,
            selectedTimeOfDayFilter = filters.timeOfDay,
            habits = filteredHabits,
            stats = stats,
            isLoading = false,
            isAddEditSheetOpen = sheet.isOpen,
            habitToEdit = sheet.habitToEdit,
            userMessage = sheet.message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(selectedDateIso = todayIso, isLoading = true)
    )

    fun onSelectDate(dateIso: String) {
        _selectedDate.value = dateIso
    }

    fun onSelectTab(tab: HomeTab) {
        _selectedTab.value = tab
    }

    fun onSelectTimeOfDayFilter(filter: TimeOfDay?) {
        _selectedTimeOfDayFilter.value = filter
    }

    fun onToggleHabit(habitId: Long) {
        viewModelScope.launch {
            try {
                val nowCompleted = useCases.toggleHabitCompletion(habitId, _selectedDate.value)
                if (nowCompleted) {
                    _userMessage.value = "¡Hábito completado! Gran progreso."
                }
            } catch (e: Exception) {
                _userMessage.value = "Error al actualizar hábito: ${e.message}"
            }
        }
    }

    fun openAddHabit() {
        _habitToEdit.value = null
        _isAddEditSheetOpen.value = true
    }

    fun openEditHabit(habit: Habit) {
        _habitToEdit.value = habit
        _isAddEditSheetOpen.value = true
    }

    fun dismissSheet() {
        _isAddEditSheetOpen.value = false
        _habitToEdit.value = null
    }

    fun saveHabit(habit: Habit) {
        viewModelScope.launch {
            try {
                if (habit.id == 0L) {
                    useCases.createHabit(habit)
                    _userMessage.value = "Hábito creado con éxito"
                } else {
                    useCases.updateHabit(habit)
                    _userMessage.value = "Hábito actualizado"
                }
                dismissSheet()
            } catch (e: Exception) {
                _userMessage.value = "Error al guardar: ${e.message}"
            }
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            try {
                useCases.deleteHabit(habitId)
                _userMessage.value = "Hábito eliminado"
                dismissSheet()
            } catch (e: Exception) {
                _userMessage.value = "Error al eliminar: ${e.message}"
            }
        }
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    companion object {
        fun provideFactory(useCases: HabitUseCases): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                        return HomeViewModel(useCases) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}
