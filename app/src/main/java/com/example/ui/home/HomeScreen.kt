package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.HabitWithStatus
import com.example.domain.model.TimeOfDay
import com.example.ui.components.AddHabitBottomSheet
import com.example.ui.components.HabitCard
import com.example.ui.components.HabitProgressHeader
import com.example.ui.components.StatsOverview
import com.example.ui.theme.StreakFire

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    // Expand/Collapse Extended FAB based on list scroll
    val isFabExpanded by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = "HabitFlow",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    if (uiState.stats.bestOverallStreak > 0) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = StreakFire.copy(alpha = 0.15f),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocalFireDepartment,
                                    contentDescription = "Récord",
                                    tint = StreakFire,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${uiState.stats.bestOverallStreak}d",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = StreakFire
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = uiState.selectedTab == HomeTab.TODAY,
                    onClick = { viewModel.onSelectTab(HomeTab.TODAY) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.selectedTab == HomeTab.TODAY) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                            contentDescription = "Hoy"
                        )
                    },
                    label = { Text("Hoy") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    )
                )

                NavigationBarItem(
                    selected = uiState.selectedTab == HomeTab.STATS,
                    onClick = { viewModel.onSelectTab(HomeTab.STATS) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.selectedTab == HomeTab.STATS) Icons.Filled.AutoGraph else Icons.Outlined.AutoGraph,
                            contentDescription = "Estadísticas"
                        )
                    },
                    label = { Text("Estadísticas") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    )
                )

                NavigationBarItem(
                    selected = uiState.selectedTab == HomeTab.ALL_HABITS,
                    onClick = { viewModel.onSelectTab(HomeTab.ALL_HABITS) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.selectedTab == HomeTab.ALL_HABITS) Icons.Filled.Tune else Icons.Outlined.Tune,
                            contentDescription = "Gestionar"
                        )
                    },
                    label = { Text("Gestionar") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddHabit() },
                expanded = isFabExpanded,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Crear hábito"
                    )
                },
                text = { Text("Nuevo Hábito") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .testTag("add_habit_fab")
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (uiState.selectedTab) {
                HomeTab.TODAY -> {
                    TodayTabContent(
                        uiState = uiState,
                        listState = listState,
                        onToggle = { viewModel.onToggleHabit(it) },
                        onEdit = { viewModel.openEditHabit(it) },
                        onDateSelected = { viewModel.onSelectDate(it) },
                        onFilterSelected = { viewModel.onSelectTimeOfDayFilter(it) }
                    )
                }

                HomeTab.STATS -> {
                    StatsOverview(
                        stats = uiState.stats,
                        habits = uiState.habits
                    )
                }

                HomeTab.ALL_HABITS -> {
                    AllHabitsTabContent(
                        habits = uiState.habits,
                        onEdit = { viewModel.openEditHabit(it) },
                        onAdd = { viewModel.openAddHabit() }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    // Add / Edit Habit Modal Bottom Sheet
    if (uiState.isAddEditSheetOpen) {
        AddHabitBottomSheet(
            habitToEdit = uiState.habitToEdit,
            onDismiss = { viewModel.dismissSheet() },
            onSave = { viewModel.saveHabit(it) },
            onDelete = { viewModel.deleteHabit(it) }
        )
    }
}

@Composable
fun TodayTabContent(
    uiState: HomeUiState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onToggle: (Long) -> Unit,
    onEdit: (com.example.domain.model.Habit) -> Unit,
    onDateSelected: (String) -> Unit,
    onFilterSelected: (TimeOfDay?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Progress Header & Date Picker Strip
        item {
            HabitProgressHeader(
                stats = uiState.stats,
                selectedDateIso = uiState.selectedDateIso,
                onDateSelected = onDateSelected
            )
        }

        // Moment of Day Filter Chips Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = uiState.selectedTimeOfDayFilter == null,
                    onClick = { onFilterSelected(null) },
                    label = { Text("Todos los momentos") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                listOf(TimeOfDay.MORNING, TimeOfDay.AFTERNOON, TimeOfDay.NIGHT).forEach { tod ->
                    FilterChip(
                        selected = uiState.selectedTimeOfDayFilter == tod,
                        onClick = {
                            if (uiState.selectedTimeOfDayFilter == tod) {
                                onFilterSelected(null)
                            } else {
                                onFilterSelected(tod)
                            }
                        },
                        label = { Text(tod.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        // Habit Cards List
        if (uiState.habits.isEmpty()) {
            item {
                EmptyHabitsState()
            }
        } else {
            // Group by Time of Day if no single filter is selected
            if (uiState.selectedTimeOfDayFilter == null) {
                val morningHabits = uiState.habits.filter { it.habit.timeOfDay == TimeOfDay.MORNING }
                val afternoonHabits = uiState.habits.filter { it.habit.timeOfDay == TimeOfDay.AFTERNOON }
                val nightHabits = uiState.habits.filter { it.habit.timeOfDay == TimeOfDay.NIGHT }
                val anytimeHabits = uiState.habits.filter { it.habit.timeOfDay == TimeOfDay.ANYTIME }

                if (morningHabits.isNotEmpty()) {
                    item {
                        TimeSectionHeader("🌅 Mañana", morningHabits)
                    }
                    items(morningHabits, key = { it.habit.id }) { habitWithStatus ->
                        HabitCard(
                            habitWithStatus = habitWithStatus,
                            onToggle = { onToggle(habitWithStatus.habit.id) },
                            onEdit = { onEdit(habitWithStatus.habit) }
                        )
                    }
                }

                if (afternoonHabits.isNotEmpty()) {
                    item {
                        TimeSectionHeader("☀️ Tarde", afternoonHabits)
                    }
                    items(afternoonHabits, key = { it.habit.id }) { habitWithStatus ->
                        HabitCard(
                            habitWithStatus = habitWithStatus,
                            onToggle = { onToggle(habitWithStatus.habit.id) },
                            onEdit = { onEdit(habitWithStatus.habit) }
                        )
                    }
                }

                if (nightHabits.isNotEmpty()) {
                    item {
                        TimeSectionHeader("🌙 Noche", nightHabits)
                    }
                    items(nightHabits, key = { it.habit.id }) { habitWithStatus ->
                        HabitCard(
                            habitWithStatus = habitWithStatus,
                            onToggle = { onToggle(habitWithStatus.habit.id) },
                            onEdit = { onEdit(habitWithStatus.habit) }
                        )
                    }
                }

                if (anytimeHabits.isNotEmpty()) {
                    item {
                        TimeSectionHeader("✨ En cualquier momento", anytimeHabits)
                    }
                    items(anytimeHabits, key = { it.habit.id }) { habitWithStatus ->
                        HabitCard(
                            habitWithStatus = habitWithStatus,
                            onToggle = { onToggle(habitWithStatus.habit.id) },
                            onEdit = { onEdit(habitWithStatus.habit) }
                        )
                    }
                }
            } else {
                items(uiState.habits, key = { it.habit.id }) { habitWithStatus ->
                    HabitCard(
                        habitWithStatus = habitWithStatus,
                        onToggle = { onToggle(habitWithStatus.habit.id) },
                        onEdit = { onEdit(habitWithStatus.habit) }
                    )
                }
            }
        }
    }
}

@Composable
fun TimeSectionHeader(title: String, habits: List<HabitWithStatus>) {
    val completedCount = habits.count { it.isCompletedToday }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = "$completedCount/${habits.size}",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AllHabitsTabContent(
    habits: List<HabitWithStatus>,
    onEdit: (com.example.domain.model.Habit) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Todos tus Hábitos (${habits.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Toca cualquier hábito para modificar su recordatorio, días programados o eliminarlo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(habits, key = { it.habit.id }) { habitWithStatus ->
            HabitCard(
                habitWithStatus = habitWithStatus,
                onToggle = { onEdit(habitWithStatus.habit) },
                onEdit = { onEdit(habitWithStatus.habit) }
            )
        }
    }
}

@Composable
fun EmptyHabitsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Sin hábitos para este momento",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Crea un nuevo hábito tocando el botón '+' abajo para iniciar tu progreso.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
