package com.example.data.mapper

import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.domain.model.Habit
import com.example.domain.model.HabitCategory
import com.example.domain.model.HabitLog
import com.example.domain.model.TimeOfDay

object HabitMapper {

    fun toDomain(entity: HabitEntity): Habit {
        return Habit(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            category = runCatching { HabitCategory.valueOf(entity.category) }.getOrDefault(HabitCategory.ROUTINE),
            timeOfDay = runCatching { TimeOfDay.valueOf(entity.timeOfDay) }.getOrDefault(TimeOfDay.MORNING),
            colorHex = entity.colorHex,
            iconName = entity.iconName,
            targetDaysMask = entity.targetDaysMask,
            reminderTime = entity.reminderTime,
            createdAt = entity.createdAt,
            isArchived = entity.isArchived
        )
    }

    fun toEntity(domain: Habit): HabitEntity {
        return HabitEntity(
            id = domain.id,
            title = domain.title,
            description = domain.description,
            category = domain.category.name,
            timeOfDay = domain.timeOfDay.name,
            colorHex = domain.colorHex,
            iconName = domain.iconName,
            targetDaysMask = domain.targetDaysMask,
            reminderTime = domain.reminderTime,
            createdAt = domain.createdAt,
            isArchived = domain.isArchived
        )
    }

    fun toDomain(logEntity: HabitLogEntity): HabitLog {
        return HabitLog(
            id = logEntity.id,
            habitId = logEntity.habitId,
            dateIso = logEntity.dateIso,
            completedAt = logEntity.completedAt
        )
    }

    fun toEntity(domainLog: HabitLog): HabitLogEntity {
        return HabitLogEntity(
            id = domainLog.id,
            habitId = domainLog.habitId,
            dateIso = domainLog.dateIso,
            completedAt = domainLog.completedAt
        )
    }
}
