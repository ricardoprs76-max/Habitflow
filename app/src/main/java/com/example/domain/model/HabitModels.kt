package com.example.domain.model

enum class TimeOfDay(val displayName: String) {
    MORNING("Mañana"),
    AFTERNOON("Tarde"),
    NIGHT("Noche"),
    ANYTIME("Cualquier momento")
}

enum class HabitCategory(val displayName: String) {
    HEALTH("Salud & Bienestar"),
    FITNESS("Ejercicio & Fitness"),
    PRODUCTIVITY("Productividad"),
    MINDFULNESS("Mindfulness & Calma"),
    LEARNING("Aprendizaje"),
    ROUTINE("Rutina Diaria")
}

data class Habit(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: HabitCategory = HabitCategory.ROUTINE,
    val timeOfDay: TimeOfDay = TimeOfDay.MORNING,
    val colorHex: Long = 0xFF10B981,
    val iconName: String = "check_circle",
    val targetDaysMask: Int = 0b1111111, // 7 days of week bitmask (Mon-Sun)
    val reminderTime: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
) {
    fun isScheduledForDayOfWeek(dayOfWeekMon1Sun7: Int): Boolean {
        val shift = dayOfWeekMon1Sun7 - 1
        if (shift in 0..6) {
            return (targetDaysMask and (1 shl shift)) != 0
        }
        return true
    }
}

data class HabitLog(
    val id: Long = 0,
    val habitId: Long,
    val dateIso: String, // "YYYY-MM-DD"
    val completedAt: Long = System.currentTimeMillis()
)

data class HabitWithStatus(
    val habit: Habit,
    val isCompletedToday: Boolean,
    val currentStreak: Int,
    val bestStreak: Int,
    val completionRatePercentage: Int,
    val last7DaysStatus: List<Boolean> = emptyList()
)

data class HabitSummaryStats(
    val totalHabits: Int,
    val completedToday: Int,
    val completionPercentageToday: Int,
    val activeStreaksCount: Int,
    val bestOverallStreak: Int
)
