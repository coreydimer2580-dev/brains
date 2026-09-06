package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GameHeader
import com.example.ui.components.GameResultOverlay
import com.example.viewmodel.BrainViewModel
import com.example.viewmodel.MemoryCardItem
import com.example.viewmodel.MemoryGameMode

/**
 * A simple, intuitive, zero-confusion memory experience offering:
 * 1. Classic Card Match (Pair Matching): The universally understood, completely un-confusing
 *    visual memory challenge where players flip 2 cards to discover matching neuro-icons.
 * 2. Pattern Sequence (Guided Simon): Enhanced with step badges, "Replay Pattern" affordance,
 *    3 lives, and real-time step guidance so nobody is ever lost or confused.
 */
@Composable
fun MemoryGridGame(
    viewModel: BrainViewModel,
    modifier: Modifier = Modifier
) {
    val mode by viewModel.memoryMode.collectAsStateWithLifecycle()
    val isGameOver by viewModel.isMemoryGameOver.collectAsStateWithLifecycle()
    val isCardMatchWon by viewModel.isCardMatchWon.collectAsStateWithLifecycle()
    val lastResult by viewModel.lastGameResult.collectAsStateWithLifecycle()

    val memoryScore by viewModel.memoryScore.collectAsStateWithLifecycle()
    val cardMoves by viewModel.cardMoves.collectAsStateWithLifecycle()
    val cardTimeSec by viewModel.cardElapsedTimeSec.collectAsStateWithLifecycle()

    val currentScore = if (mode == MemoryGameMode.CARD_MATCH) {
        (1200 - cardMoves * 30 - cardTimeSec * 4).coerceAtLeast(0)
    } else {
        memoryScore
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("simple_memory_game_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Screen Header
            GameHeader(
                title = "Simple Memory",
                score = currentScore,
                subtitle = "Temporal Lobe & Working Memory",
                onExit = { viewModel.exitCurrentGame() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Game Mode Switcher: Card Pairs vs. Pattern Sequence
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    // Card Match Tab
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (mode == MemoryGameMode.CARD_MATCH) {
                            MaterialTheme.colorScheme.primary
                        } else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setMemoryMode(MemoryGameMode.CARD_MATCH) }
                            .testTag("mode_card_match")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Style,
                                contentDescription = null,
                                tint = if (mode == MemoryGameMode.CARD_MATCH) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Card Match",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (mode == MemoryGameMode.CARD_MATCH) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (mode == MemoryGameMode.CARD_MATCH) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Pattern Sequence Tab
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (mode == MemoryGameMode.PATTERN_SEQUENCE) {
                            MaterialTheme.colorScheme.primary
                        } else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setMemoryMode(MemoryGameMode.PATTERN_SEQUENCE) }
                            .testTag("mode_pattern_sequence")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridOn,
                                contentDescription = null,
                                tint = if (mode == MemoryGameMode.PATTERN_SEQUENCE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pattern Recall",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (mode == MemoryGameMode.PATTERN_SEQUENCE) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (mode == MemoryGameMode.PATTERN_SEQUENCE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Content Area based on Mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                when (mode) {
                    MemoryGameMode.CARD_MATCH -> {
                        CardMatchSection(viewModel = viewModel)
                    }
                    MemoryGameMode.PATTERN_SEQUENCE -> {
                        PatternSequenceSection(viewModel = viewModel)
                    }
                }
            }
        }

        // Result Dialog Overlay (When Game Ends or Won)
        val showResult = (mode == MemoryGameMode.PATTERN_SEQUENCE && isGameOver) ||
                (mode == MemoryGameMode.CARD_MATCH && isCardMatchWon)

        if (showResult && lastResult != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center
            ) {
                GameResultOverlay(
                    result = lastResult!!,
                    onPlayAgain = {
                        if (mode == MemoryGameMode.CARD_MATCH) {
                            viewModel.startCardMatchGame()
                        } else {
                            viewModel.startPatternSequenceGame()
                        }
                    },
                    onFinish = { viewModel.exitCurrentGame() }
                )
            }
        }
    }
}

/**
 * ----------------------------------------------------------------------
 * 1. CARD MATCH SECTION (Zero Confusion, Relaxed Pairs)
 * ----------------------------------------------------------------------
 */
@Composable
private fun CardMatchSection(
    viewModel: BrainViewModel,
    modifier: Modifier = Modifier
) {
    val cards by viewModel.cardList.collectAsStateWithLifecycle()
    val moves by viewModel.cardMoves.collectAsStateWithLifecycle()
    val matchedPairs by viewModel.cardMatchedPairs.collectAsStateWithLifecycle()
    val timeSec by viewModel.cardElapsedTimeSec.collectAsStateWithLifecycle()
    val isWon by viewModel.isCardMatchWon.collectAsStateWithLifecycle()

    val formattedTime = remember(timeSec) {
        val minutes = timeSec / 60
        val seconds = timeSec % 60
        String.format("%02d:%02d", minutes, seconds)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status Row: Pairs Found / Moves / Timer
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pairs
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pairs: $matchedPairs / 6",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF10B981)
                    )
                }

                // Moves
                Text(
                    text = "Moves: $moves",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Timer
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Simple Help / Instruction Bar
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isWon) "🎉 All 6 pairs found! Outstanding memory!" else "Tap any 2 cards to reveal and pair identical icons",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3x4 Grid of Cards (12 cards total, 6 pairs)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(cards, key = { it.id }) { card ->
                MemoryCardView(
                    card = card,
                    onCardClick = { viewModel.onCardTapped(card.id) }
                )
            }
        }

        // Action Row: Hint/Peek + Restart
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.peekCards() },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("peek_cards_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Peek Cards")
            }

            Button(
                onClick = { viewModel.startCardMatchGame() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("restart_card_match_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "New Game")
            }
        }
    }
}

/**
 * Visual presentation of an individual Memory Card with 3D flip animation.
 */
@Composable
private fun MemoryCardView(
    card: MemoryCardItem,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (card.isFaceUp || card.isMatched) 180f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "card_flip_${card.id}"
    )

    val isFrontVisible = rotation > 90f

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (card.isMatched) {
            Color(0xFF064E3B).copy(alpha = 0.35f)
        } else if (isFrontVisible) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        tonalElevation = if (card.isMatched) 1.dp else 3.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = if (card.isMatched) 2.dp else 1.dp,
            color = if (card.isMatched) Color(0xFF10B981) else card.color.copy(alpha = if (isFrontVisible) 0.5f else 0.15f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(enabled = !card.isFaceUp && !card.isMatched) { onCardClick() }
            .testTag("memory_card_${card.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isFrontVisible) {
                // Card Face Up Content (Reverse horizontal flip so text isn't mirrored)
                Column(
                    modifier = Modifier.graphicsLayer { rotationY = 180f },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = card.symbol,
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = card.title,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = card.color,
                        textAlign = TextAlign.Center
                    )

                    if (card.isMatched) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Matched",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            } else {
                // Card Face Down Content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Hidden card",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * ----------------------------------------------------------------------
 * 2. PATTERN SEQUENCE SECTION (Upgraded Simon: 3 Lives, Replay, Step Badges)
 * ----------------------------------------------------------------------
 */
@Composable
private fun PatternSequenceSection(
    viewModel: BrainViewModel,
    modifier: Modifier = Modifier
) {
    val level by viewModel.memoryLevel.collectAsStateWithLifecycle()
    val isShowingSequence by viewModel.isShowingSequence.collectAsStateWithLifecycle()
    val activeTile by viewModel.activeHighlightedTile.collectAsStateWithLifecycle()
    val activeStepNumber by viewModel.patternStepNumber.collectAsStateWithLifecycle()
    val userInput by viewModel.userMemoryInput.collectAsStateWithLifecycle()
    val targetSequence by viewModel.gridSequence.collectAsStateWithLifecycle()
    val lives by viewModel.memoryLives.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.patternFeedback.collectAsStateWithLifecycle()
    val mistakeTile by viewModel.lastTappedMistakeTile.collectAsStateWithLifecycle()
    val isGameOver by viewModel.isMemoryGameOver.collectAsStateWithLifecycle()

    // Distinct theme colors and icons for all 9 tiles so they are never identical blank boxes
    val tileMetadata = remember {
        listOf(
            Pair(Color(0xFF38BDF8), "1 🌟"),
            Pair(Color(0xFFA78BFA), "2 ⚡"),
            Pair(Color(0xFF34D399), "3 🍀"),
            Pair(Color(0xFFFBBF24), "4 ☀️"),
            Pair(Color(0xFFF87171), "5 💎"),
            Pair(Color(0xFFFB923C), "6 💡"),
            Pair(Color(0xFF60A5FA), "7 💧"),
            Pair(Color(0xFFF472B6), "8 ❤️"),
            Pair(Color(0xFF2DD4BF), "9 🛡️")
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status Row: Level & 3 Hearts/Lives
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Level
                Text(
                    text = "Level $level (${targetSequence.size} steps)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                // 3 Hearts/Lives
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Lives: ",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    for (i in 1..3) {
                        Icon(
                            imageVector = if (i <= lives) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Life $i",
                            tint = if (i <= lives) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Friendly Real-Time Feedback & Guidance Banner
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isShowingSequence) {
                Color(0xFFF59E0B).copy(alpha = 0.15f)
            } else {
                Color(0xFF10B981).copy(alpha = 0.15f)
            },
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (isShowingSequence) Color(0xFFF59E0B).copy(alpha = 0.4f) else Color(0xFF10B981).copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = feedbackMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isShowingSequence) Color(0xFFD97706) else Color(0xFF059669),
                    modifier = Modifier.weight(1f)
                )

                // Replay Button (Always accessible so users never feel lost)
                TextButton(
                    onClick = { viewModel.replayPattern() },
                    enabled = !isShowingSequence && !isGameOver,
                    modifier = Modifier.testTag("replay_pattern_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Replay")
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3x3 Memory Grid Matrix
        Box(
            modifier = Modifier
                .size(310.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
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
                            val meta = tileMetadata[index]
                            val isHighlighted = activeTile == index
                            val isUserTapped = userInput.lastOrNull() == index
                            val isMistake = mistakeTile == index

                            val tileBgColor by animateColorAsState(
                                targetValue = when {
                                    isHighlighted -> meta.first // Vibrant distinct color
                                    isMistake -> Color(0xFFEF4444) // Error flash
                                    isUserTapped -> meta.first.copy(alpha = 0.65f)
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                animationSpec = tween(durationMillis = 200),
                                label = "pattern_tile_bg_$index"
                            )

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = tileBgColor,
                                tonalElevation = if (isHighlighted) 8.dp else 2.dp,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isHighlighted) 2.dp else 1.dp,
                                    color = if (isHighlighted) Color.White else meta.first.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .testTag("pattern_tile_$index")
                                    .clickable(enabled = !isShowingSequence && !isGameOver) {
                                        viewModel.onMemoryTileTapped(index)
                                    }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    if (isHighlighted && activeStepNumber != null) {
                                        // Numbered Step Badge
                                        Surface(
                                            shape = CircleShape,
                                            color = Color.White,
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "#$activeStepNumber",
                                                    style = MaterialTheme.typography.labelLarge.copy(
                                                        fontWeight = FontWeight.Black
                                                    ),
                                                    color = Color.Black
                                                )
                                            }
                                        }
                                    } else {
                                        // Idle tile with identifier
                                        Text(
                                            text = meta.second,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = meta.first.copy(alpha = 0.75f)
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

        // Informative tip
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Tip: Chunk the pattern into pairs of numbers to expand hippocampal recall.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
