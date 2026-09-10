package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.data.WorkoutEntity
import com.example.ui.components.*
import com.example.viewmodel.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProgressScreen(
    viewModel: UserProgressViewModel,
    brainViewModel: BrainViewModel? = null,
    modifier: Modifier = Modifier
) {
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    val filteredWorkouts by viewModel.filteredWorkouts.collectAsStateWithLifecycle()
    val timeframeFilter by viewModel.timeframeFilter.collectAsStateWithLifecycle()
    val topicFilter by viewModel.topicFilter.collectAsStateWithLifecycle()
    val metricType by viewModel.metricType.collectAsStateWithLifecycle()
    val topicMasteries by viewModel.topicMasteries.collectAsStateWithLifecycle()
    val overallMastery by viewModel.overallMasteryScore.collectAsStateWithLifecycle()
    val recommendations by viewModel.recommendations.collectAsStateWithLifecycle()
    val weeklyActivity by viewModel.weeklyActivity.collectAsStateWithLifecycle()
    val totalWorkouts by viewModel.totalWorkoutsCount.collectAsStateWithLifecycle()
    val averageAccuracy by viewModel.averageAccuracyPercent.collectAsStateWithLifecycle()
    val highestScore by viewModel.highestScore.collectAsStateWithLifecycle()
    val streakDays by viewModel.learningStreakDays.collectAsStateWithLifecycle()
    val aiRecommendation by viewModel.aiRecommendation.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("user_progress_screen"),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Header & Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Progress & Analytics",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Performance trends, topic mastery & personalized focus",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.testTag("btn_add_progress_score")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = "Log Session",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.testTag("btn_clear_progress_history")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Reset History",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Summary Metric Highlight Card
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        RoundedCornerShape(24.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "OVERALL COGNITIVE MASTERY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$overallMastery%",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 36.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Circular mastery gauge
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                .border(2.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 Stat Indicators Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ProgressSummaryItem(
                            value = "$streakDays d",
                            label = "Streak",
                            icon = Icons.Default.LocalFireDepartment,
                            tint = Color(0xFFF97316)
                        )
                        ProgressSummaryItem(
                            value = "$totalWorkouts",
                            label = "Sessions",
                            icon = Icons.Default.FitnessCenter,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        ProgressSummaryItem(
                            value = "$averageAccuracy%",
                            label = "Avg Acc",
                            icon = Icons.Default.Verified,
                            tint = Color(0xFF10B981)
                        )
                        ProgressSummaryItem(
                            value = "$highestScore",
                            label = "Top Score",
                            icon = Icons.Default.EmojiEvents,
                            tint = Color(0xFFF59E0B)
                        )
                    }
                }
            }
        }

        // ==========================================
        // SECTION 1: PERFORMANCE OVER TIME & CHARTS
        // ==========================================
        item {
            SectionHeader(
                title = "Performance Over Time",
                subtitle = "Interactive trajectory across training days and sessions"
            )
        }

        // Filter Controls: Timeframe & Metric Toggle
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Timeframe Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TimeframeFilter.values().forEach { filter ->
                            FilterChip(
                                selected = (timeframeFilter == filter),
                                onClick = { viewModel.setTimeframeFilter(filter) },
                                label = { Text(filter.label, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("filter_${filter.name.lowercase()}")
                            )
                        }
                    }

                    // Metric Toggle (Score vs Accuracy)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (metricType == ProgressMetric.SCORE) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { viewModel.setMetricType(ProgressMetric.SCORE) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Score",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (metricType == ProgressMetric.SCORE) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (metricType == ProgressMetric.ACCURACY) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { viewModel.setMetricType(ProgressMetric.ACCURACY) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Accuracy",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (metricType == ProgressMetric.ACCURACY) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Topic Filter Horizontal Scroll
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = (topicFilter == null),
                            onClick = { viewModel.setTopicFilter(null) },
                            label = { Text("All Disciplines", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                    items(topicMasteries) { tm ->
                        FilterChip(
                            selected = (topicFilter == tm.gameType),
                            onClick = { viewModel.setTopicFilter(tm.gameType) },
                            label = { Text(tm.title, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(tm.color)
                                )
                            }
                        )
                    }
                }
            }
        }

        // Performance Trend Chart Card
        item {
            PerformanceTrendChart(
                workouts = filteredWorkouts,
                metric = metricType,
                modifier = Modifier.testTag("performance_trend_chart_card")
            )
        }

        // ==========================================
        // SECTION 2: TOPIC MASTERY & RADAR VISUALS
        // ==========================================
        item {
            SectionHeader(
                title = "Topics Mastered & Cognitive Balance",
                subtitle = "Proficiency across neuro-anatomical lobes and skills"
            )
        }

        // Radar Chart
        item {
            CognitiveMasteryRadarChart(
                masteries = topicMasteries,
                modifier = Modifier.testTag("radar_chart_section")
            )
        }

        // Donut Breakdown
        item {
            MasteryDistributionDonut(
                masteries = topicMasteries,
                overallMastery = overallMastery
            )
        }

        // Detailed Topic Mastery Cards
        item {
            Text(
                text = "Detailed Topic Breakdown",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(topicMasteries) { topic ->
            TopicMasteryCard(
                topic = topic,
                onTrainTopic = {
                    brainViewModel?.openGame(topic.activeGame)
                }
            )
        }

        // ==========================================
        // SECTION 3: RECOMMENDATIONS FOR NEXT FOCUS
        // ==========================================
        item {
            SectionHeader(
                title = "Recommended Focus Areas",
                subtitle = "Targeted drills to eliminate cognitive blindspots & level up"
            )
        }

        // AI Personalized Recommendation Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                ),
                modifier = Modifier.fillMaxWidth().testTag("ai_coach_recommendation_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini AI Focus Coach",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        TextButton(
                            onClick = { viewModel.fetchAiRecommendations() },
                            enabled = !isAiLoading
                        ) {
                            Text(if (isAiLoading) "Analyzing..." else "Refresh")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (isAiLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))
                    } else {
                        Text(
                            text = aiRecommendation ?: "Tap 'Refresh' to have Gemini analyze your topic masteries and recommend your optimal training schedule for the week.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        // Algorithmic Topic Recommendation Cards
        items(recommendations) { rec ->
            RecommendationCard(
                recommendation = rec,
                onStartExercise = {
                    brainViewModel?.openGame(rec.activeGame)
                }
            )
        }

        // ==========================================
        // SECTION 4: WEEKLY ACTIVITY CADENCE
        // ==========================================
        item {
            SectionHeader(
                title = "Weekly Training Cadence",
                subtitle = "Consistency over the past 7 days"
            )
        }

        item {
            WeeklyActivityBarChart(activity = weeklyActivity)
        }

        // ==========================================
        // SECTION 5: RECENT SESSION HISTORY LOGS
        // ==========================================
        item {
            SectionHeader(
                title = "Recent Training History",
                subtitle = "Latest chronological sessions from local Room database"
            )
        }

        if (workouts.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Sessions Recorded Yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Play games in the Brain Gym or tap '+' above to add your first session.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(onClick = { viewModel.seedSampleData() }) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Load Sample Calibration Data")
                        }
                    }
                }
            }
        } else {
            items(workouts.take(8)) { session ->
                WorkoutHistoryCardItem(session = session)
            }
        }
    }

    // Dialog: Record New Score / Manual Training Entry
    if (showAddDialog) {
        RecordScoreDialog(
            onDismiss = { showAddDialog = false },
            onSave = { gameType, score, accuracy ->
                viewModel.addManualWorkout(gameType, score, accuracy)
                showAddDialog = false
            }
        )
    }

    // Dialog: Reset / Clear History
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Reset Progress Data?") },
            text = { Text("This will clear all recorded workouts and reset all topic masteries to zero.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllWorkouts()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProgressSummaryItem(
    value: String,
    label: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TopicMasteryCard(
    topic: TopicMastery,
    onTrainTopic: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.5.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                RoundedCornerShape(18.dp)
            )
            .testTag("topic_card_${topic.id.lowercase()}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with status chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(topic.color)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = topic.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = topic.brainRegion,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = topic.status.badgeColor.copy(alpha = 0.16f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, topic.status.badgeColor.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (topic.status == TopicMasteryStatus.MASTERED) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = topic.status.badgeColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = topic.status.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = topic.status.badgeColor
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar & Percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mastery Level",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${topic.masteryScore}%",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = topic.color
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { topic.masteryScore / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = topic.color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stats row & Train action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text(
                            text = "${topic.sessionsCount}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Sessions",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column {
                        Text(
                            text = "${topic.avgAccuracy}%",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Accuracy",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column {
                        Text(
                            text = "${topic.bestScore}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Peak Score",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onTrainTopic,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("btn_train_${topic.id.lowercase()}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Train", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
private fun RecommendationCard(
    recommendation: TopicRecommendation,
    onStartExercise: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.5.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                recommendation.priority.color.copy(alpha = 0.35f),
                RoundedCornerShape(18.dp)
            )
            .testTag("recommendation_card_${recommendation.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = recommendation.priority.color.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = recommendation.priority.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = recommendation.priority.color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = recommendation.estimatedBoost,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Focus: ${recommendation.topicTitle}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = recommendation.reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = recommendation.actionDescription,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onStartExercise,
                modifier = Modifier.fillMaxWidth().testTag("btn_rec_action_${recommendation.id}"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Workout Now",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun WorkoutHistoryCardItem(
    session: WorkoutEntity,
    modifier: Modifier = Modifier
) {
    val (title, icon, color) = when (session.gameType) {
        "MEMORY_GRID" -> Triple("Working Memory", Icons.Default.Style, Color(0xFF38BDF8))
        "STROOP_SPEED" -> Triple("Executive Inhibition", Icons.Default.Speed, Color(0xFFEF4444))
        "SYNAPSE_MATH" -> Triple("Synapse Math", Icons.Default.Calculate, Color(0xFF10B981))
        "FOCUS_TRAINING" -> Triple("Visual Focus", Icons.Default.Visibility, Color(0xFFF59E0B))
        else -> Triple("Neuro Quiz", Icons.Default.School, Color(0xFF8B5CF6))
    }

    val dateFormat = remember { SimpleDateFormat("MMM d • h:mm a", Locale.getDefault()) }
    val formattedDate = remember(session.timestamp) { dateFormat.format(Date(session.timestamp)) }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "$formattedDate • ${(session.accuracy * 100).toInt()}% acc",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${session.score} pts",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun RecordScoreDialog(
    onDismiss: () -> Unit,
    onSave: (String, Int, Float) -> Unit
) {
    var selectedType by remember { mutableStateOf("MEMORY_GRID") }
    var scoreInput by remember { mutableStateOf("450") }
    var accuracySlider by remember { mutableFloatStateOf(0.85f) }

    val categories = listOf(
        "MEMORY_GRID" to "Working Memory",
        "STROOP_SPEED" to "Executive Inhibition",
        "SYNAPSE_MATH" to "Numerical Processing",
        "NEURO_QUIZ" to "Neuroscience Quiz",
        "FOCUS_TRAINING" to "Visual Focus"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Cognitive Session")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Select Training Topic",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { (id, label) ->
                        FilterChip(
                            selected = (selectedType == id),
                            onClick = { selectedType = id },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = scoreInput,
                    onValueChange = { scoreInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Training Score (Pts)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Accuracy", style = MaterialTheme.typography.labelMedium)
                        Text("${(accuracySlider * 100).roundToInt()}%", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Slider(
                        value = accuracySlider,
                        onValueChange = { accuracySlider = it },
                        valueRange = 0.20f..1.0f
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedScore = scoreInput.toIntOrNull() ?: 300
                    onSave(selectedType, parsedScore, accuracySlider)
                }
            ) {
                Text("Save Session")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
