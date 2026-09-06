package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.WorkoutEntity
import com.example.ui.components.SectionHeader
import com.example.viewmodel.BrainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatsScreen(
    viewModel: BrainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val workouts by viewModel.workoutHistory.collectAsStateWithLifecycle()
    val bq = viewModel.calculateBrainQuotient(workouts)
    val streak = viewModel.calculateStreak(workouts)
    val brainInsight by viewModel.brainInsight.collectAsStateWithLifecycle()
    val isFetchingInsight by viewModel.isFetchingInsight.collectAsStateWithLifecycle()

    var showClearDialog by remember { mutableStateOf(false) }
    var showDownloadDialog by remember { mutableStateOf(false) }
    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (brainInsight == null) {
            viewModel.fetchPersonalizedInsight()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Brain Analytics",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Real-time cognitive metrics & workout export",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            isCopied = false
                            showDownloadDialog = true
                        },
                        modifier = Modifier.testTag("download_analytics_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download Brain Analytics",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (workouts.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearDialog = true },
                            modifier = Modifier.testTag("clear_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear History",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // BQ & Streak Overview Card
        item {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(26.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "BRAIN QUOTIENT (BQ)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // BQ Big Dial Number
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                            .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$bq",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 38.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "INDEX",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val tierTitle = when {
                        bq >= 130 -> "Superior Synaptic Plasticity"
                        bq >= 115 -> "High Cognitive Agility"
                        bq >= 100 -> "Active Neural Baseline"
                        else -> "Developing Potential"
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = tierTitle,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatSummaryItem(
                            value = "$streak days",
                            label = "Daily Streak",
                            icon = Icons.Default.LocalFireDepartment,
                            tint = Color(0xFFF97316)
                        )
                        StatSummaryItem(
                            value = "${workouts.size}",
                            label = "Workouts Done",
                            icon = Icons.Default.FitnessCenter,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        val avgAcc = if (workouts.isNotEmpty()) {
                            (workouts.map { it.accuracy }.average() * 100).toInt()
                        } else 0
                        StatSummaryItem(
                            value = "$avgAcc%",
                            label = "Avg Accuracy",
                            icon = Icons.Default.Verified,
                            tint = Color(0xFF10B981)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Download Brain Analytics Report Action Button
                    Button(
                        onClick = {
                            isCopied = false
                            showDownloadDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_download_analytics_report"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Download Brain Analytics Report",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // Live AI Insights Section
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth().testTag("ai_insights_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
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
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Live AI Insights",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        
                        TextButton(
                            onClick = { viewModel.fetchPersonalizedInsight() },
                            enabled = !isFetchingInsight
                        ) {
                            Text(if (isFetchingInsight) "Analyzing..." else "Refresh")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (isFetchingInsight) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        )
                    } else {
                        Text(
                            text = brainInsight ?: "Tap 'Refresh' to get personalized cognitive insights powered by Gemini AI.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Domain Breakdown
        item {
            SectionHeader(
                title = "Cognitive Domain Ratings",
                subtitle = "Proficiency estimated from your training games"
            )
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val memoryCount = workouts.count { it.gameType == "MEMORY_GRID" }
                DomainProgressRow(
                    domain = "Working Memory",
                    lobe = "Temporal / Hippocampus",
                    level = (memoryCount * 12).coerceIn(15, 100),
                    color = Color(0xFF38BDF8)
                )

                val stroopCount = workouts.count { it.gameType == "STROOP_SPEED" }
                DomainProgressRow(
                    domain = "Executive Inhibition",
                    lobe = "Prefrontal Cortex",
                    level = (stroopCount * 12).coerceIn(15, 100),
                    color = Color(0xFFEF4444)
                )

                val mathCount = workouts.count { it.gameType == "SYNAPSE_MATH" }
                DomainProgressRow(
                    domain = "Numerical Processing",
                    lobe = "Parietal Lobe",
                    level = (mathCount * 12).coerceIn(15, 100),
                    color = Color(0xFF10B981)
                )

                val quizCount = workouts.count { it.gameType == "NEURO_QUIZ" }
                DomainProgressRow(
                    domain = "Neuroscience Literacy",
                    lobe = "Cerebral Cortex",
                    level = (quizCount * 12).coerceIn(15, 100),
                    color = Color(0xFF8B5CF6)
                )
            }
        }

        // Recent Workouts List
        item {
            SectionHeader(
                title = "Workout History",
                subtitle = if (workouts.isEmpty()) "No workouts recorded yet" else "Last ${workouts.take(15).size} sessions"
            )
        }

        if (workouts.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Workouts Yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Jump into the Brain Gym to play Memory Grid or Stroop Speed and track your score history!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(workouts.take(15)) { session ->
                WorkoutHistoryCard(session = session)
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Workout History?") },
            text = { Text("This will permanently remove your recorded workout sessions and reset your stats.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearStatsHistory()
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

    if (showDownloadDialog) {
        val reportText = remember(workouts, bq, streak) {
            generateAnalyticsReport(bq, streak, workouts)
        }

        AlertDialog(
            onDismissRequest = { showDownloadDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Brain Analytics Export")
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                ) {
                    Text(
                        text = "Export your cognitive profile, BQ metrics, and workout logs:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        LazyColumn(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            item {
                                Text(
                                    text = reportText,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    if (isCopied) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ Report copied to clipboard!",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        shareAnalyticsReport(context, reportText)
                        showDownloadDialog = false
                    },
                    modifier = Modifier.testTag("btn_share_report_action")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download / Share")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(reportText))
                        isCopied = true
                    },
                    modifier = Modifier.testTag("btn_copy_report_clipboard")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isCopied) "Copied" else "Copy Text")
                }
            }
        )
    }
}

/**
 * Generates formatted plain-text Brain Analytics Report for exporting or saving
 */
fun generateAnalyticsReport(bq: Int, streak: Int, workouts: List<WorkoutEntity>): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val totalWorkouts = workouts.size
    val totalScore = workouts.sumOf { it.score }
    val focusSessions = workouts.filter { it.gameType == "FOCUS_TRAINING" }
    val focusSeconds = focusSessions.sumOf { if (it.reactionTimeMs > 0) (it.reactionTimeMs / 1000).toInt() else 30 }

    return buildString {
        appendLine("==========================================")
        appendLine("           BRAIN ANALYTICS REPORT         ")
        appendLine("==========================================")
        appendLine("Generated: ${dateFormat.format(Date())}")
        appendLine()
        appendLine("OVERALL COGNITIVE METRICS:")
        appendLine("• Brain Quotient (BQ): $bq")
        appendLine("• Current Daily Streak: $streak days")
        appendLine("• Total Completed Workouts: $totalWorkouts")
        appendLine("• Cumulative Training Score: $totalScore pts")
        appendLine("• Visual Focus Time: $focusSeconds sec (${focusSessions.size} sessions)")
        appendLine()
        appendLine("BREAKDOWN BY TRAINING DISCIPLINE:")
        if (workouts.isEmpty()) {
            appendLine("  (No training sessions recorded yet)")
        } else {
            val grouped = workouts.groupBy { it.gameType }
            grouped.forEach { (type, list) ->
                val avgAcc = (list.map { it.accuracy }.average() * 100).toInt()
                val maxScore = list.maxOf { it.score }
                appendLine("• $type: ${list.size} sessions | Top Score: $maxScore | Avg Acc: $avgAcc%")
            }
        }
        appendLine()
        appendLine("RECENT SESSIONS:")
        if (workouts.isEmpty()) {
            appendLine("  (None)")
        } else {
            workouts.take(8).forEach { session ->
                appendLine("- ${dateFormat.format(Date(session.timestamp))}: ${session.gameType} (${session.score} pts, ${(session.accuracy * 100).toInt()}% acc)")
            }
        }
        appendLine("==========================================")
        appendLine("Exported from Brain Learning Platform")
    }
}

/**
 * Dispatches Android Intent to download, save, or share the generated report
 */
fun shareAnalyticsReport(context: Context, report: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, report)
        putExtra(Intent.EXTRA_SUBJECT, "Brain Analytics Summary Report")
        type = "text/plain"
    }
    val chooser = Intent.createChooser(sendIntent, "Download / Share Brain Analytics")
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
}

@Composable
private fun StatSummaryItem(
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
            modifier = Modifier.size(22.dp)
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
private fun DomainProgressRow(
    domain: String,
    lobe: String,
    level: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = domain,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = lobe,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "$level%",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { level / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun WorkoutHistoryCard(
    session: WorkoutEntity,
    modifier: Modifier = Modifier
) {
    val (title, icon, color) = when (session.gameType) {
        "MEMORY_GRID" -> Triple("Simple Memory", Icons.Default.Style, Color(0xFF38BDF8))
        "STROOP_SPEED" -> Triple("Stroop Focus", Icons.Default.Speed, Color(0xFFEF4444))
        "SYNAPSE_MATH" -> Triple("Synapse Math", Icons.Default.Calculate, Color(0xFF10B981))
        "FOCUS_TRAINING" -> Triple("Focus Anchor", Icons.Default.Visibility, Color(0xFF0EA5E9))
        else -> Triple("Neuro Quiz", Icons.Default.School, Color(0xFF8B5CF6))
    }

    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
    val formattedDate = remember(session.timestamp) { dateFormat.format(Date(session.timestamp)) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
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
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

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
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
