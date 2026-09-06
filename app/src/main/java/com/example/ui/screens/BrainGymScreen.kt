package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.SectionHeader
import com.example.viewmodel.ActiveGame
import com.example.viewmodel.BrainViewModel

@Composable
fun BrainGymScreen(
    viewModel: BrainViewModel,
    modifier: Modifier = Modifier
) {
    val activeGame by viewModel.activeGame.collectAsStateWithLifecycle()
    val workouts by viewModel.workoutHistory.collectAsStateWithLifecycle()

    // If a game is active, render that game full-screen
    when (activeGame) {
        ActiveGame.MEMORY_GRID -> {
            MemoryGridGame(viewModel = viewModel, modifier = modifier)
            return
        }
        ActiveGame.STROOP_SPEED -> {
            StroopGame(viewModel = viewModel, modifier = modifier)
            return
        }
        ActiveGame.SYNAPSE_MATH -> {
            SynapseMathGame(viewModel = viewModel, modifier = modifier)
            return
        }
        ActiveGame.NEURO_QUIZ -> {
            NeuroQuizGame(viewModel = viewModel, modifier = modifier)
            return
        }
        ActiveGame.NONE -> {
            // Render workout menu
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = "Brain Gym",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Targeted cognitive workouts calibrated to activate specific brain lobes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Daily workout recommendation banner
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Recommended Circuit",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Complete 3 workouts daily to stimulate BDNF and consolidate neural plasticity",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Cognitive Disciplines",
                subtitle = "Select an exercise to train today"
            )
        }

        // 1. Memory Grid
        item {
            val memoryWorkouts = workouts.filter { it.gameType == "MEMORY_GRID" }
            val topScore = memoryWorkouts.maxOfOrNull { it.score } ?: 0
            GymExerciseCard(
                title = "Memory Grid",
                subtitle = "Temporal & Frontal Lobe",
                description = "Memorize and reproduce spatial tile sequences with progressive length.",
                targetAttribute = "Working Memory",
                icon = Icons.Default.GridOn,
                accentColor = Color(0xFF38BDF8),
                topScore = topScore,
                onClick = { viewModel.openGame(ActiveGame.MEMORY_GRID) },
                testTag = "gym_card_memory_grid"
            )
        }

        // 2. Stroop Speed Focus
        item {
            val stroopWorkouts = workouts.filter { it.gameType == "STROOP_SPEED" }
            val topScore = stroopWorkouts.maxOfOrNull { it.score } ?: 0
            GymExerciseCard(
                title = "Stroop Focus",
                subtitle = "Prefrontal Cortex & Executive Control",
                description = "Overcome cognitive conflict: identify ink colors while ignoring conflicting words.",
                targetAttribute = "Inhibitory Speed",
                icon = Icons.Default.Speed,
                accentColor = Color(0xFFEF4444),
                topScore = topScore,
                onClick = { viewModel.openGame(ActiveGame.STROOP_SPEED) },
                testTag = "gym_card_stroop_speed"
            )
        }

        // 3. Synapse Math
        item {
            val mathWorkouts = workouts.filter { it.gameType == "SYNAPSE_MATH" }
            val topScore = mathWorkouts.maxOfOrNull { it.score } ?: 0
            GymExerciseCard(
                title = "Synapse Math",
                subtitle = "Parietal Lobe (Intraparietal Sulcus)",
                description = "Fast-fire mental arithmetic under pressure testing mental number line fluency.",
                targetAttribute = "Numerical Agility",
                icon = Icons.Default.Calculate,
                accentColor = Color(0xFF10B981),
                topScore = topScore,
                onClick = { viewModel.openGame(ActiveGame.SYNAPSE_MATH) },
                testTag = "gym_card_synapse_math"
            )
        }

        // 4. Neuro Quiz
        item {
            val quizWorkouts = workouts.filter { it.gameType == "NEURO_QUIZ" }
            val topScore = quizWorkouts.maxOfOrNull { it.score } ?: 0
            GymExerciseCard(
                title = "Neuro Quiz",
                subtitle = "All Lobes & Hippocampus",
                description = "Test and solidify your understanding of neuroscience, neurotransmitters, and anatomy.",
                targetAttribute = "Science Knowledge",
                icon = Icons.Default.School,
                accentColor = Color(0xFF8B5CF6),
                topScore = topScore,
                onClick = { viewModel.openGame(ActiveGame.NEURO_QUIZ) },
                testTag = "gym_card_neuro_quiz"
            )
        }
    }
}

@Composable
private fun GymExerciseCard(
    title: String,
    subtitle: String,
    description: String,
    targetAttribute: String,
    icon: ImageVector,
    accentColor: Color,
    topScore: Int,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.25f),
                shape = RoundedCornerShape(22.dp)
            )
            .testTag(testTag)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(accentColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accentColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = targetAttribute,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (topScore > 0) "Best: $topScore pts" else "Not yet played",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = accentColor.copy(alpha = 0.2f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(text = "Start", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
