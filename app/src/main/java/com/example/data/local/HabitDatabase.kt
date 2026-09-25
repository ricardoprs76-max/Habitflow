package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.HabitDao
import com.example.data.local.dao.HabitLogDao
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Database(
    entities = [HabitEntity::class, HabitLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HabitDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao

    companion object {
        @Volatile
        private var INSTANCE: HabitDatabase? = null

        fun getInstance(context: Context): HabitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HabitDatabase::class.java,
                    "habit_flow.db"
                )
                    .addCallback(HabitDatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class HabitDatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Prepopulate starter habits on a background coroutine
            CoroutineScope(Dispatchers.IO).launch {
                val instance = INSTANCE ?: return@launch
                val habitDao = instance.habitDao()
                val habitLogDao = instance.habitLogDao()

                val now = System.currentTimeMillis()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val today = dateFormat.format(Date(now))

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = now
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                val yesterday = dateFormat.format(calendar.time)

                calendar.add(Calendar.DAY_OF_YEAR, -1)
                val twoDaysAgo = dateFormat.format(calendar.time)

                val habit1Id = habitDao.insertHabit(
                    HabitEntity(
                        title = "Beber 2 Litros de Agua",
                        description = "Mantener una hidratación óptima durante todo el día",
                        category = "HEALTH",
                        timeOfDay = "MORNING",
                        colorHex = 0xFF06B6D4, // Cyan
                        iconName = "water_drop",
                        targetDaysMask = 0b1111111,
                        reminderTime = "08:00",
                        createdAt = now - 86400000L * 7
                    )
                )

                val habit2Id = habitDao.insertHabit(
                    HabitEntity(
                        title = "Meditación y Respiración",
                        description = "10 minutos de respiración diafragmática para calmar la mente",
                        category = "MINDFULNESS",
                        timeOfDay = "MORNING",
                        colorHex = 0xFF8B5CF6, // Violet
                        iconName = "self_improvement",
                        targetDaysMask = 0b1111111,
                        reminderTime = "07:30",
                        createdAt = now - 86400000L * 5
                    )
                )

                val habit3Id = habitDao.insertHabit(
                    HabitEntity(
                        title = "Lectura de Crecimiento",
                        description = "Leer al menos 20 páginas de libros técnicos o desarrollo",
                        category = "LEARNING",
                        timeOfDay = "AFTERNOON",
                        colorHex = 0xFFF59E0B, // Amber
                        iconName = "menu_book",
                        targetDaysMask = 0b1111111,
                        reminderTime = "16:00",
                        createdAt = now - 86400000L * 4
                    )
                )

                val habit4Id = habitDao.insertHabit(
                    HabitEntity(
                        title = "Entrenamiento Funcional / Gym",
                        description = "45 minutos de fuerza o cardio ligero",
                        category = "FITNESS",
                        timeOfDay = "AFTERNOON",
                        colorHex = 0xFF10B981, // Emerald
                        iconName = "fitness_center",
                        targetDaysMask = 0b0111110, // Mon-Fri
                        reminderTime = "18:30",
                        createdAt = now - 86400000L * 6
                    )
                )

                val habit5Id = habitDao.insertHabit(
                    HabitEntity(
                        title = "Planificar el Día Siguiente",
                        description = "Organizar las 3 tareas clave antes de desconectar pantallas",
                        category = "PRODUCTIVITY",
                        timeOfDay = "NIGHT",
                        colorHex = 0xFF6366F1, // Indigo
                        iconName = "task_alt",
                        targetDaysMask = 0b1111111,
                        reminderTime = "22:00",
                        createdAt = now - 86400000L * 10
                    )
                )

                // Add starter streak logs for demo satisfaction
                habitLogDao.insertLog(HabitLogEntity(habitId = habit1Id, dateIso = twoDaysAgo))
                habitLogDao.insertLog(HabitLogEntity(habitId = habit1Id, dateIso = yesterday))
                habitLogDao.insertLog(HabitLogEntity(habitId = habit1Id, dateIso = today))

                habitLogDao.insertLog(HabitLogEntity(habitId = habit2Id, dateIso = yesterday))
                habitLogDao.insertLog(HabitLogEntity(habitId = habit2Id, dateIso = today))

                habitLogDao.insertLog(HabitLogEntity(habitId = habit4Id, dateIso = yesterday))
            }
        }
    }
}
