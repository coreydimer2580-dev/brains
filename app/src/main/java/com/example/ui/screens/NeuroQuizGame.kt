package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.BrainDataRepository
import com.example.ui.components.GameHeader
import com.example.ui.components.GameResultOverlay
import com.example.viewmodel.BrainViewModel

@Composable
fun NeuroQuizGame(
    viewModel: BrainViewModel,
    modifier: Modifier = Modifier
) {
    val qIndex by viewModel.quizQuestionIndex.collectAsStateWithLifecycle()
    val score by viewModel.quizScore.collectAsStateWithLifecycle()
    val selectedOption by viewModel.quizSelectedOption.collectAsStateWithLifecycle()
    val isAnswered by viewModel.quizIsAnswered.collectAsStateWithLifecycle()
    val isGameOver by viewModel.isQuizGameOver.collectAsStateWithLifecycle()
    val lastResult by viewModel.lastGameResult.collectAsStateWithLifecycle()

    val totalQuestions = BrainDataRepository.quizQuestions.size
    val currentQ = BrainDataRepository.quizQuestions.getOrNull(qIndex)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            GameHeader(
                title = "Neuro Quiz",
                score = score,
                subtitle = "Cognitive Science & Anatomy",
                onExit = { viewModel.exitCurrentGame() }
            )

            // Progress Bar
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
                        text = "Question ${qIndex + 1} of $totalQuestions",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Target: ${currentQ?.targetLobe ?: "Brain"}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (qIndex + 1).toFloat() / totalQuestions },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            currentQ?.let { question ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Question Card
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = question.question,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 26.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // 4 Options
                    items(question.options.size) { idx ->
                        val optionText = question.options[idx]
                        val isChosen = selectedOption == idx
                        val isCorrect = idx == question.correctIndex

                        val containerColor = when {
                            !isAnswered -> MaterialTheme.colorScheme.surface
                            isCorrect -> Color(0xFF10B981).copy(alpha = 0.15f)
                            isChosen -> Color(0xFFEF4444).copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        }

                        val borderColor = when {
                            !isAnswered && isChosen -> MaterialTheme.colorScheme.primary
                            isAnswered && isCorrect -> Color(0xFF10B981)
                            isAnswered && isChosen -> Color(0xFFEF4444)
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        }

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = containerColor,
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, borderColor, RoundedCornerShape(18.dp))
                                .testTag("quiz_option_$idx")
                                .clickable(enabled = !isAnswered) {
                                    viewModel.answerQuizQuestion(idx)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            color = borderColor.copy(alpha = 0.25f),
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isAnswered && isCorrect) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Correct",
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else if (isAnswered && isChosen) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Incorrect",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Text(
                                            text = ('A' + idx).toString(),
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Text(
                                    text = optionText,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Explanation Box
                    if (isAnswered) {
                        item {
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn()
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(18.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.School,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Scientific Explanation",
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = question.explanation,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Continue Action Button
            if (isAnswered) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Button(
                            onClick = { viewModel.advanceQuiz() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("quiz_next_button")
                        ) {
                            Text(
                                text = if (qIndex + 1 < totalQuestions) "Next Question" else "Complete Quiz",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null
                            )
                        }
                    }
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
                    onPlayAgain = { viewModel.startQuizGame() },
                    onFinish = { viewModel.exitCurrentGame() }
                )
            }
        }
    }
}
