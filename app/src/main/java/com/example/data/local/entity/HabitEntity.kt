package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String,
    val timeOfDay: String,
    val colorHex: Long,
    val iconName: String,
    val targetDaysMask: Int = 0b1111111,
    val reminderTime: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)
