package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GameHeader
import com.example.ui.components.GameResultOverlay
import com.example.viewmodel.BrainViewModel

@Composable
fun MemoryGridGame(
    viewModel: BrainViewModel,
    modifier: Modifier = Modifier
) {
    val level by viewModel.memoryLevel.collectAsStateWithLifecycle()
    val score by viewModel.memoryScore.collectAsStateWithLifecycle()
    val isShowingSequence by viewModel.isShowingSequence.collectAsStateWithLifecycle()
    val activeTile by viewModel.activeHighlightedTile.collectAsStateWithLifecycle()
    val userInput by viewModel.userMemoryInput.collectAsStateWithLifecycle()
    val targetSequence by viewModel.gridSequence.collectAsStateWithLifecycle()
    val isGameOver by viewModel.isMemoryGameOver.collectAsStateWithLifecycle()
    val lastResult by viewModel.lastGameResult.collectAsStateWithLifecycle()

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
                title = "Memory Grid",
                score = score,
                subtitle = "Temporal & Frontal Lobe Working Memory",
                onExit = { viewModel.exitCurrentGame() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Level and status indicator
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Level $level",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Divider(
                        modifier = Modifier
                            .height(16.dp)
                            .width(1.dp)
                    )
                    Text(
                        text = if (isShowingSequence) "Memorize Pattern..." else "Your Turn! (${userInput.size}/${targetSequence.size})",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = if (isShowingSequence) Color(0xFFF59E0B) else Color(0xFF10B981)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 3x3 Memory Grid Matrix
            Box(
                modifier = Modifier
                    .size(310.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (row in 0..2) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (col in 0..2) {
                                val index = row * 3 + col
                                val isHighlighted = activeTile == index
                                val isUserRecentlyTapped = userInput.lastOrNull() == index

                                val tileColor by animateColorAsState(
                                    targetValue = when {
                                        isHighlighted -> Color(0xFF38BDF8) // Glowing Cyan
                                        isUserRecentlyTapped -> Color(0xFFA78BFA) // Violet user tap
                                        else -> MaterialTheme.colorScheme.surface
                                    },
                                    animationSpec = tween(durationMillis = 180),
                                    label = "tile_color_$index"
                                )

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = tileColor,
                                    tonalElevation = if (isHighlighted) 8.dp else 2.dp,
                                    shadowElevation = if (isHighlighted) 6.dp else 1.dp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .testTag("memory_tile_$index")
                                        .clickable(enabled = !isShowingSequence && !isGameOver) {
                                            viewModel.onMemoryTileTapped(index)
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (isHighlighted) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.White)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Neuroscience Tip Bar
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Working memory span in typical adults is 4-7 items. Practice stimulates synaptic connectivity in the hippocampus.",
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
                    onPlayAgain = { viewModel.startMemoryGridGame() },
                    onFinish = { viewModel.exitCurrentGame() }
                )
            }
        }
    }
}
