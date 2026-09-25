package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector

object HabitIcons {
    val availableIcons: List<Pair<String, ImageVector>> = listOf(
        "water_drop" to Icons.Filled.WaterDrop,
        "fitness_center" to Icons.Filled.FitnessCenter,
        "self_improvement" to Icons.Filled.SelfImprovement,
        "menu_book" to Icons.Filled.MenuBook,
        "task_alt" to Icons.Filled.TaskAlt,
        "directions_run" to Icons.Filled.DirectionsRun,
        "directions_walk" to Icons.Filled.DirectionsWalk,
        "bedtime" to Icons.Filled.Bedtime,
        "psychology" to Icons.Filled.Psychology,
        "code" to Icons.Filled.Code,
        "favorite" to Icons.Filled.Favorite,
        "star" to Icons.Filled.Star,
        "local_fire_department" to Icons.Filled.LocalFireDepartment,
        "music_note" to Icons.Filled.MusicNote,
        "check_circle" to Icons.Filled.CheckCircle
    )

    fun getIcon(name: String): ImageVector {
        return availableIcons.find { it.first == name }?.second ?: Icons.Filled.CheckCircle
    }
}
