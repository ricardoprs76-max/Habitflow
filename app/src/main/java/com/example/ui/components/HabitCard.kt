package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.HabitWithStatus
import com.example.ui.theme.StreakFire

@Composable
fun HabitCard(
    habitWithStatus: HabitWithStatus,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habit = habitWithStatus.habit
    val isCompleted = habitWithStatus.isCompletedToday
    val habitColor = Color(habit.colorHex)
    val haptic = LocalHapticFeedback.current

    // Animated Card elevation and border based on completed state
    val cardAlpha by animateFloatAsState(
        targetValue = if (isCompleted) 0.88f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "card_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("habit_card_${habit.id}")
            .clip(RoundedCornerShape(20.dp))
            .clickable { onToggle() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCompleted) 1.dp else 3.dp
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isCompleted) habitColor.copy(alpha = 0.35f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            ),
            width = if (isCompleted) 1.5.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Category Tag & Time of Day badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = habitColor.copy(alpha = 0.18f),
                        contentColor = habitColor
                    ) {
                        Text(
                            text = habit.category.displayName,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = habit.timeOfDay.displayName,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Edit Button
                IconButton(
                    onClick = { onEdit() },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("edit_habit_button_${habit.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar hábito",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Row: Icon, Title & Animated Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leading Icon Container
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(habitColor.copy(alpha = if (isCompleted) 0.15f else 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = HabitIcons.getIcon(habit.iconName),
                        contentDescription = null,
                        tint = habitColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Title and Description
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (isCompleted) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )

                    if (habit.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = habit.description,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (habit.reminderTime != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = habit.reminderTime,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Animated Checkbox / Toggle Button with Ripple and Haptic Feedback
                AnimatedHabitCheckbox(
                    checked = isCompleted,
                    accentColor = habitColor,
                    testTag = "habit_toggle_${habit.id}",
                    onToggle = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggle()
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer: Streak Indicator & Last 7 Days Mini Dot Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Streak Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = "Racha",
                        tint = if (habitWithStatus.currentStreak > 0) StreakFire else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (habitWithStatus.currentStreak > 0) {
                            "${habitWithStatus.currentStreak} días de racha"
                        } else {
                            "Empieza tu racha hoy"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (habitWithStatus.currentStreak > 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (habitWithStatus.currentStreak > 0) {
                            StreakFire
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                // Mini 7 Days Dots
                if (habitWithStatus.last7DaysStatus.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        habitWithStatus.last7DaysStatus.takeLast(7).forEach { isDone ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isDone) habitColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedHabitCheckbox(
    checked: Boolean,
    accentColor: Color,
    testTag: String,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (checked) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "checkbox_scale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (checked) accentColor else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "checkbox_bg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (checked) accentColor else MaterialTheme.colorScheme.outline,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "checkbox_border"
    )

    Box(
        modifier = modifier
            .size(46.dp)
            .testTag(testTag)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 24.dp)
            ) { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(2.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = checked,
                enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessHigh)),
                exit = fadeOut()
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completado",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
