package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WorkoutEntity
import com.example.viewmodel.DayActivity
import com.example.viewmodel.ProgressMetric
import com.example.viewmodel.TopicMastery
import com.example.viewmodel.TopicMasteryStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.*

/**
 * Interactive Performance Trend Line/Area Chart with smooth bezier curves and touch scrubber.
 */
@Composable
fun PerformanceTrendChart(
    workouts: List<WorkoutEntity>,
    metric: ProgressMetric,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    onPointSelected: (WorkoutEntity?) -> Unit = {}
) {
    if (workouts.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No workout data recorded for this timeframe.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var selectedIndex by remember(workouts) { mutableStateOf<Int?>(null) }
    val selectedWorkout = selectedIndex?.let { if (it in workouts.indices) workouts[it] else null }

    LaunchedEffect(selectedWorkout) {
        onPointSelected(selectedWorkout)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        // Top summary header for selected or latest point
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (metric == ProgressMetric.SCORE) "Cognitive Score Trajectory" else "Accuracy Progression",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (selectedWorkout != null) {
                        val fmt = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
                        "${formatGameTitle(selectedWorkout.gameType)} • ${fmt.format(Date(selectedWorkout.timestamp))}"
                    } else {
                        "Tap or drag across data points to inspect"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selectedWorkout != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (selectedWorkout != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = if (metric == ProgressMetric.SCORE) "${selectedWorkout.score} pts" else "${(selectedWorkout.accuracy * 100).roundToInt()}% acc",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Canvas Area
        val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        val surfaceColor = MaterialTheme.colorScheme.surface

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .testTag("performance_trend_canvas")
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(workouts) {
                        detectTapGestures(
                            onPress = { offset ->
                                val stepX = (size.width - 60.dp.toPx()) / (workouts.size - 1).coerceAtLeast(1)
                                val padStart = 30.dp.toPx()
                                val nearestIndex = ((offset.x - padStart + (stepX / 2)) / stepX).toInt().coerceIn(0, workouts.size - 1)
                                selectedIndex = nearestIndex
                            }
                        )
                    }
            ) {
                val padStart = 30.dp.toPx()
                val padEnd = 30.dp.toPx()
                val padTop = 15.dp.toPx()
                val padBottom = 25.dp.toPx()

                val chartW = size.width - padStart - padEnd
                val chartH = size.height - padTop - padBottom

                // Calculate Value Bounds
                val values = workouts.map {
                    if (metric == ProgressMetric.SCORE) it.score.toFloat()
                    else it.accuracy * 100f
                }

                val minVal = if (metric == ProgressMetric.ACCURACY) 0f else (values.minOrNull() ?: 0f).coerceAtLeast(0f)
                val rawMax = values.maxOrNull() ?: 100f
                val maxVal = if (metric == ProgressMetric.ACCURACY) 100f else (ceil(rawMax / 50f) * 50f).coerceAtLeast(100f)
                val valRange = (maxVal - minVal).coerceAtLeast(1f)

                // 1. Draw horizontal gridlines (3 lines)
                val gridSteps = 3
                for (i in 0..gridSteps) {
                    val yNorm = i / gridSteps.toFloat()
                    val yPos = padTop + chartH * (1f - yNorm)
                    drawLine(
                        color = gridColor,
                        start = Offset(padStart, yPos),
                        end = Offset(size.width - padEnd, yPos),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }

                if (workouts.size == 1) {
                    // Single point display
                    val cx = padStart + chartW / 2f
                    val cy = padTop + chartH * (1f - (values[0] - minVal) / valRange)
                    drawCircle(color = lineColor, radius = 6.dp.toPx(), center = Offset(cx, cy))
                    return@Canvas
                }

                // 2. Compute Points
                val points = values.mapIndexed { idx, v ->
                    val x = padStart + (idx.toFloat() / (workouts.size - 1)) * chartW
                    val normY = ((v - minVal) / valRange).coerceIn(0f, 1f)
                    val y = padTop + chartH * (1f - normY)
                    Offset(x, y)
                }

                // 3. Build Smooth Bezier Path
                val curvePath = Path()
                val fillPath = Path()

                curvePath.moveTo(points[0].x, points[0].y)
                fillPath.moveTo(points[0].x, padTop + chartH)
                fillPath.lineTo(points[0].x, points[0].y)

                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val cp1X = p0.x + (p1.x - p0.x) / 2f
                    val cp1Y = p0.y
                    val cp2X = p0.x + (p1.x - p0.x) / 2f
                    val cp2Y = p1.y

                    curvePath.cubicTo(cp1X, cp1Y, cp2X, cp2Y, p1.x, p1.y)
                    fillPath.cubicTo(cp1X, cp1Y, cp2X, cp2Y, p1.x, p1.y)
                }

                fillPath.lineTo(points.last().x, padTop + chartH)
                fillPath.close()

                // Draw Gradient Area Under Curve
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(lineColor.copy(alpha = 0.35f), lineColor.copy(alpha = 0.02f)),
                        startY = padTop,
                        endY = padTop + chartH
                    ),
                    style = Fill
                )

                // Draw Stroke Curve
                drawPath(
                    path = curvePath,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Draw Data Points
                points.forEachIndexed { idx, pt ->
                    val isSelected = (idx == selectedIndex)
                    if (isSelected) {
                        // Guide line
                        drawLine(
                            color = lineColor.copy(alpha = 0.5f),
                            start = Offset(pt.x, padTop),
                            end = Offset(pt.x, padTop + chartH),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                        // Glow circle
                        drawCircle(color = lineColor.copy(alpha = 0.3f), radius = 12.dp.toPx(), center = pt)
                        drawCircle(color = surfaceColor, radius = 7.dp.toPx(), center = pt)
                        drawCircle(color = lineColor, radius = 5.dp.toPx(), center = pt)
                    } else {
                        drawCircle(color = surfaceColor, radius = 4.dp.toPx(), center = pt)
                        drawCircle(color = lineColor, radius = 3.dp.toPx(), center = pt)
                    }
                }
            }
        }

        // Bottom Date Range Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val dateFmt = SimpleDateFormat("MMM d", Locale.getDefault())
            Text(
                text = dateFmt.format(Date(workouts.first().timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${workouts.size} Sessions Recorded",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = dateFmt.format(Date(workouts.last().timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Multi-Axis Cognitive Domain Radar / Spider Web Chart.
 */
@Composable
fun CognitiveMasteryRadarChart(
    masteries: List<TopicMastery>,
    modifier: Modifier = Modifier
) {
    if (masteries.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Cognitive Domain Radar",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Multi-axis balance across brain lobes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Current Mastery",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val primaryColor = MaterialTheme.colorScheme.primary
        val gridLineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

        Box(
            modifier = Modifier
                .size(240.dp)
                .testTag("radar_chart_box"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxRadius = (min(size.width, size.height) / 2f) - 24.dp.toPx()
                val n = masteries.size
                val angleStep = (2 * PI / n).toFloat()

                // 1. Draw Concentric Web Rings (25%, 50%, 75%, 100%)
                val ringLevels = listOf(0.25f, 0.50f, 0.75f, 1.0f)
                ringLevels.forEach { level ->
                    val ringRadius = maxRadius * level
                    val ringPath = Path()
                    for (i in 0 until n) {
                        val angle = (i * angleStep) - (PI / 2).toFloat()
                        val x = center.x + ringRadius * cos(angle)
                        val y = center.y + ringRadius * sin(angle)
                        if (i == 0) ringPath.moveTo(x, y) else ringPath.lineTo(x, y)
                    }
                    ringPath.close()
                    drawPath(
                        path = ringPath,
                        color = gridLineColor,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // 2. Draw Spokes from Center
                for (i in 0 until n) {
                    val angle = (i * angleStep) - (PI / 2).toFloat()
                    val spokeX = center.x + maxRadius * cos(angle)
                    val spokeY = center.y + maxRadius * sin(angle)
                    drawLine(
                        color = gridLineColor,
                        start = center,
                        end = Offset(spokeX, spokeY),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // 3. Draw Benchmark guideline polygon (e.g. 70% proficiency line)
                val benchmarkRadius = maxRadius * 0.70f
                val benchmarkPath = Path()
                for (i in 0 until n) {
                    val angle = (i * angleStep) - (PI / 2).toFloat()
                    val bx = center.x + benchmarkRadius * cos(angle)
                    val by = center.y + benchmarkRadius * sin(angle)
                    if (i == 0) benchmarkPath.moveTo(bx, by) else benchmarkPath.lineTo(bx, by)
                }
                benchmarkPath.close()
                drawPath(
                    path = benchmarkPath,
                    color = primaryColor.copy(alpha = 0.25f),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                )

                // 4. Draw User Mastery Polygon
                val masteryPath = Path()
                val masteryPoints = mutableListOf<Offset>()

                for (i in 0 until n) {
                    val masteryRatio = (masteries[i].masteryScore / 100f).coerceIn(0.15f, 1.0f)
                    val ptRadius = maxRadius * masteryRatio
                    val angle = (i * angleStep) - (PI / 2).toFloat()
                    val mx = center.x + ptRadius * cos(angle)
                    val my = center.y + ptRadius * sin(angle)
                    val pt = Offset(mx, my)
                    masteryPoints.add(pt)
                    if (i == 0) masteryPath.moveTo(mx, my) else masteryPath.lineTo(mx, my)
                }
                masteryPath.close()

                // Translucent fill
                drawPath(
                    path = masteryPath,
                    color = primaryColor.copy(alpha = 0.28f),
                    style = Fill
                )

                // Border stroke
                drawPath(
                    path = masteryPath,
                    color = primaryColor,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Vertex nodes
                masteryPoints.forEachIndexed { idx, pt ->
                    val domainColor = masteries[idx].color
                    drawCircle(color = Color.White, radius = 5.dp.toPx(), center = pt)
                    drawCircle(color = domainColor, radius = 3.5.dp.toPx(), center = pt)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mastery Legend Badges Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            masteries.forEach { item ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(item.color)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${item.masteryScore}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = item.color
                    )
                    Text(
                        text = item.title.split(" ").firstOrNull() ?: item.title,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 7-Day Weekly Training Activity Frequency Bar Chart.
 */
@Composable
fun WeeklyActivityBarChart(
    activity: List<DayActivity>,
    modifier: Modifier = Modifier
) {
    if (activity.isEmpty()) return

    val maxCount = activity.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Weekly Activity Cadence",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Workout consistency over the last 7 days",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val totalWeekly = activity.sumOf { it.count }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "$totalWeekly sessions",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            activity.forEach { day ->
                val ratio = (day.count.toFloat() / maxCount).coerceIn(0.08f, 1f)
                val isToday = day == activity.last()

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    if (day.count > 0) {
                        Text(
                            text = "${day.count}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = if (isToday) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .fillMaxHeight(fraction = ratio * 0.75f)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                            .background(
                                if (day.count > 0) {
                                    Brush.verticalGradient(
                                        listOf(primaryColor, primaryColor.copy(alpha = 0.6f))
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        listOf(surfaceVariant.copy(alpha = 0.7f), surfaceVariant.copy(alpha = 0.4f))
                                    )
                                }
                            )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = day.dayLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        ),
                        color = if (isToday) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Topic Mastery Distribution Donut Progress Ring.
 */
@Composable
fun MasteryDistributionDonut(
    masteries: List<TopicMastery>,
    overallMastery: Int,
    modifier: Modifier = Modifier
) {
    val masteredCount = masteries.count { it.status == TopicMasteryStatus.MASTERED }
    val proficientCount = masteries.count { it.status == TopicMasteryStatus.PROFICIENT }
    val developingCount = masteries.count { it.status == TopicMasteryStatus.DEVELOPING }
    val needsFocusCount = masteries.count { it.status == TopicMasteryStatus.NEEDS_FOCUS }
    val total = masteries.size.coerceAtLeast(1)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Donut ring
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeW = 10.dp.toPx()
                val radius = (min(size.width, size.height) - strokeW) / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                // Draw segments
                val colors = listOf(
                    Color(0xFF10B981) to masteredCount,
                    Color(0xFF0EA5E9) to proficientCount,
                    Color(0xFF8B5CF6) to developingCount,
                    Color(0xFFF59E0B) to needsFocusCount
                )

                var startAngle = -90f
                colors.forEach { (color, count) ->
                    if (count > 0) {
                        val sweep = (count.toFloat() / total) * 360f
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweep - 3f, // small gap between arcs
                            useCenter = false,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round),
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2)
                        )
                        startAngle += sweep
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$overallMastery%",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "INDEX",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(20.dp))

        // Legend Breakdown
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StatusLegendRow(label = "Mastered (80%+)", count = masteredCount, color = Color(0xFF10B981))
            StatusLegendRow(label = "Proficient (60-79%)", count = proficientCount, color = Color(0xFF0EA5E9))
            StatusLegendRow(label = "Developing (35-59%)", count = developingCount, color = Color(0xFF8B5CF6))
            StatusLegendRow(label = "Needs Focus (<35%)", count = needsFocusCount, color = Color(0xFFF59E0B))
        }
    }
}

@Composable
private fun StatusLegendRow(
    label: String,
    count: Int,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

private fun formatGameTitle(gameType: String): String {
    return when (gameType) {
        "MEMORY_GRID" -> "Working Memory"
        "STROOP_SPEED" -> "Executive Inhibition"
        "SYNAPSE_MATH" -> "Numerical Processing"
        "NEURO_QUIZ" -> "Neuroscience Quiz"
        "FOCUS_TRAINING" -> "Visual Focus"
        else -> gameType.replace("_", " ")
    }
}
