package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class FocusTimerState {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED
}

/**
 * A simple focus-training timer component that asks users to stare at a specific
 * screen object for 30 seconds to improve concentration, with a progress bar
 * and completion message.
 */
@Composable
fun FocusTrainingTimer(
    modifier: Modifier = Modifier,
    totalSeconds: Int = 30,
    onSessionComplete: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null
) {
    var timerState by remember { mutableStateOf(FocusTimerState.IDLE) }
    var remainingMillis by remember { mutableLongStateOf(totalSeconds * 1000L) }

    val totalMillis = totalSeconds * 1000L

    // Coroutine countdown
    LaunchedEffect(timerState) {
        if (timerState == FocusTimerState.RUNNING) {
            val stepMillis = 50L
            while (remainingMillis > 0 && timerState == FocusTimerState.RUNNING) {
                delay(stepMillis)
                remainingMillis = (remainingMillis - stepMillis).coerceAtLeast(0L)
            }
            if (remainingMillis <= 0L && timerState == FocusTimerState.RUNNING) {
                timerState = FocusTimerState.COMPLETED
                onSessionComplete?.invoke()
            }
        }
    }

    val progress = (1f - (remainingMillis.toFloat() / totalMillis.toFloat())).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 80, easing = LinearEasing),
        label = "focus_progress"
    )

    val secondsLeft = ((remainingMillis + 999L) / 1000L).toInt()
    val secondsElapsed = (totalSeconds - secondsLeft).coerceAtLeast(0)

    // Gentle breathing pulse animation for the visual focus anchor
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "target_scale"
    )

    val ringGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.30f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_alpha"
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = when (timerState) {
                FocusTimerState.RUNNING -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                FocusTimerState.COMPLETED -> Color(0xFF10B981)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("focus_training_timer_component")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row with Title and Optional Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "30-Second Focus Anchor",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Visual fixation exercise to boost concentration",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (onClose != null) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_focus_timer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Instructions text based on current state
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when (timerState) {
                    FocusTimerState.RUNNING -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    FocusTimerState.COMPLETED -> Color(0xFF10B981).copy(alpha = 0.15f)
                    FocusTimerState.PAUSED -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                    FocusTimerState.IDLE -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when (timerState) {
                        FocusTimerState.IDLE ->
                            "Stare continuously at the central focal dot for 30 seconds without looking away. Minimize blinking and breathe steadily."
                        FocusTimerState.RUNNING ->
                            "Keep your eyes locked on the central dot. Resist all distractions and maintain unwavering gaze."
                        FocusTimerState.PAUSED ->
                            "Paused. Rest your eyes briefly, then resume when ready to hold your gaze."
                        FocusTimerState.COMPLETED ->
                            "🎯 Session Complete! You sustained visual fixation for the full 30 seconds."
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    textAlign = TextAlign.Center,
                    color = when (timerState) {
                        FocusTimerState.COMPLETED -> Color(0xFF059669)
                        FocusTimerState.RUNNING -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SPECIFIC SCREEN OBJECT TO STARE AT
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .testTag("focus_screen_object"),
                contentAlignment = Alignment.Center
            ) {
                if (timerState != FocusTimerState.COMPLETED) {
                    // Outer Radiant Ring
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .graphicsLayer {
                                if (timerState == FocusTimerState.RUNNING) {
                                    scaleX = pulseScale
                                    scaleY = pulseScale
                                }
                            }
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(
                                    alpha = if (timerState == FocusTimerState.RUNNING) ringGlowAlpha * 0.25f else 0.10f
                                )
                            )
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary.copy(
                                    alpha = if (timerState == FocusTimerState.RUNNING) ringGlowAlpha else 0.25f
                                ),
                                shape = CircleShape
                            )
                    )

                    // Middle Reticle Ring
                    Box(
                        modifier = Modifier
                            .size(136.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .border(
                                width = 1.5.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                shape = CircleShape
                            )
                    )

                    // Core High-Contrast Target Object with Focal Micro-Dot
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                        Color(0xFF0284C7)
                                    )
                                )
                            )
                            .border(
                                width = 3.dp,
                                color = Color.White,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Central Micro-Dot (The precise focal anchor point)
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(width = 1.dp, color = Color(0xFF0F172A), shape = CircleShape)
                        )
                    }

                    // Alignment Crosshairs for precise gaze lock
                    Box(
                        modifier = Modifier
                            .size(170.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Horizontal guide ticks
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(modifier = Modifier.size(8.dp, 2.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)))
                            Box(modifier = Modifier.size(8.dp, 2.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)))
                        }
                        // Vertical guide ticks
                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(modifier = Modifier.size(2.dp, 8.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)))
                            Box(modifier = Modifier.size(2.dp, 8.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)))
                        }
                    }
                } else {
                    // Completed State Visualization
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                            .border(width = 2.5.dp, color = Color(0xFF10B981), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Session Completed",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(72.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // TIMER STATUS & SECONDS BADGE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (timerState) {
                        FocusTimerState.IDLE -> "Target: ${totalSeconds}s"
                        FocusTimerState.RUNNING -> "Fixating... ${secondsElapsed}s / ${totalSeconds}s"
                        FocusTimerState.PAUSED -> "Paused at ${secondsElapsed}s"
                        FocusTimerState.COMPLETED -> "Completed: ${totalSeconds}s"
                    },
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = if (timerState == FocusTimerState.COMPLETED) {
                        "100%"
                    } else {
                        "${secondsLeft}s left"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (timerState == FocusTimerState.COMPLETED) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // PROGRESS BAR (Requested feature)
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .testTag("focus_progress_bar"),
                color = if (timerState == FocusTimerState.COMPLETED) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // COMPLETION MESSAGE (Requested feature)
            AnimatedVisibility(
                visible = timerState == FocusTimerState.COMPLETED,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("focus_completion_message")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Celebration,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Focus Session Completed!",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF059669)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Outstanding mental discipline! Holding unbroken gaze for 30 seconds engages frontal eye fields, calms the Default Mode Network (DMN), and expands executive cognitive control.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // CONTROLS (Start, Pause, Resume, Reset, Train Again)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (timerState) {
                    FocusTimerState.IDLE -> {
                        Button(
                            onClick = {
                                remainingMillis = totalMillis
                                timerState = FocusTimerState.RUNNING
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("start_focus_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Start Focus ($totalSeconds Seconds)",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    FocusTimerState.RUNNING -> {
                        OutlinedButton(
                            onClick = {
                                timerState = FocusTimerState.IDLE
                                remainingMillis = totalMillis
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("reset_focus_button")
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cancel")
                        }

                        Button(
                            onClick = { timerState = FocusTimerState.PAUSED },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("pause_focus_button")
                        ) {
                            Icon(imageVector = Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pause")
                        }
                    }

                    FocusTimerState.PAUSED -> {
                        OutlinedButton(
                            onClick = {
                                timerState = FocusTimerState.IDLE
                                remainingMillis = totalMillis
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("reset_focus_button")
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset")
                        }

                        Button(
                            onClick = { timerState = FocusTimerState.RUNNING },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("resume_focus_button")
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Resume")
                        }
                    }

                    FocusTimerState.COMPLETED -> {
                        Button(
                            onClick = {
                                remainingMillis = totalMillis
                                timerState = FocusTimerState.RUNNING
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("restart_focus_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Train Again (30s)",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}
