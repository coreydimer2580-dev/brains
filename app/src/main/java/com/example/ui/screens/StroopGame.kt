package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GameHeader
import com.example.ui.components.GameResultOverlay
import com.example.viewmodel.BrainViewModel

@Composable
fun StroopGame(
    viewModel: BrainViewModel,
    modifier: Modifier = Modifier
) {
    val round by viewModel.stroopRound.collectAsStateWithLifecycle()
    val score by viewModel.stroopScore.collectAsStateWithLifecycle()
    val challenge by viewModel.stroopChallenge.collectAsStateWithLifecycle()
    val timeLeft by viewModel.stroopTimeLeft.collectAsStateWithLifecycle()
    val isGameOver by viewModel.isStroopGameOver.collectAsStateWithLifecycle()
    val lastResult by viewModel.lastGameResult.collectAsStateWithLifecycle()

    val animatedProgress by animateFloatAsState(
        targetValue = timeLeft,
        label = "stroop_timer_progress"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GameHeader(
                title = "Stroop Focus",
                score = score,
                subtitle = "Prefrontal Inhibitory Control",
                onExit = { viewModel.exitCurrentGame() }
            )

            // Progress bar & Round
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Round $round / 12",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tap the INK COLOR!",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (animatedProgress > 0.35f) MaterialTheme.colorScheme.primary else Color(0xFFEF4444),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.weight(0.3f))

            // Central Stimulus Card
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(180.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    challenge?.let { ch ->
                        Text(
                            text = ch.wordText,
                            color = ch.textColor,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.4f))

            // 4 Option Buttons (Colors to pick from)
            challenge?.let { ch ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (i in 0..1) {
                            val option = ch.options.getOrNull(i)
                            if (option != null) {
                                ColorChoiceButton(
                                    colorName = option.first,
                                    color = option.second,
                                    onClick = { viewModel.onStroopOptionSelected(option.second) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (i in 2..3) {
                            val option = ch.options.getOrNull(i)
                            if (option != null) {
                                ColorChoiceButton(
                                    colorName = option.first,
                                    color = option.second,
                                    onClick = { viewModel.onStroopOptionSelected(option.second) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.3f))

            // Neuroscience Tip
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "The Stroop task forces your Anterior Cingulate Cortex to suppress the automatic impulse to read words instead of identifying colors.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Result Dialog Overlay
        if (isGameOver && lastResult != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                GameResultOverlay(
                    result = lastResult!!,
                    onPlayAgain = { viewModel.startStroopGame() },
                    onFinish = { viewModel.exitCurrentGame() }
                )
            }
        }
    }
}

@Composable
private fun ColorChoiceButton(
    colorName: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier
            .height(64.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("stroop_option_$colorName")
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = colorName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
