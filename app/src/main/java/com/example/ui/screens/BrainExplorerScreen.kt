package com.example.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.BrainDataRepository
import com.example.model.BrainLobe
import com.example.ui.components.Brain3DAnatomyExplorer
import com.example.ui.components.InteractiveBrainCanvas
import com.example.viewmodel.ActiveGame
import com.example.viewmodel.BrainViewModel
import java.util.Locale

/**
 * Interactive Brain Explorer screen allowing users to tap on visual brain lobes
 * to reveal short educational descriptions, functional circuits, and clinical insights.
 */
@Composable
fun BrainExplorerScreen(
    viewModel: BrainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedLobe by viewModel.selectedLobe.collectAsStateWithLifecycle()

    BrainExplorer(
        selectedLobe = selectedLobe,
        onLobeSelected = { viewModel.selectLobe(it) },
        onTrainLobe = { lobe ->
            when (lobe.id) {
                "FRONTAL" -> viewModel.openGame(ActiveGame.STROOP_SPEED)
                "PARIETAL" -> viewModel.openGame(ActiveGame.SYNAPSE_MATH)
                "TEMPORAL" -> viewModel.openGame(ActiveGame.MEMORY_GRID)
                else -> viewModel.openGame(ActiveGame.NEURO_QUIZ)
            }
        },
        modifier = modifier.testTag("brain_explorer_screen")
    )
}

/**
 * Self-contained BrainExplorer composable for standalone use or integration into any layout.
 */
@Composable
fun BrainExplorer(
    selectedLobe: BrainLobe,
    onLobeSelected: (BrainLobe) -> Unit,
    modifier: Modifier = Modifier,
    onTrainLobe: ((BrainLobe) -> Unit)? = null
) {
    val context = LocalContext.current
    val lobes = BrainDataRepository.brainLobes
    val currentIndex = lobes.indexOfFirst { it.id == selectedLobe.id }.coerceAtLeast(0)

    var is3DMode by remember { mutableStateOf(true) }

    // TTS engine to read educational descriptions aloud
    var isTtsSpeaking by remember { mutableStateOf(false) }
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Configured
            }
        }
        tts.language = Locale.US
        ttsEngine = tts

        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun speakDescription(text: String) {
        ttsEngine?.let { tts ->
            if (tts.isSpeaking) {
                tts.stop()
                isTtsSpeaking = false
            } else {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "brain_desc_utterance")
                isTtsSpeaking = true
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Header Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_brain_hero),
                    contentDescription = "Brain Explorer neural map",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Atmospheric gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                )

                // Header Texts
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = "INTERACTIVE BRAIN EXPLORER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "Cortical Atlas",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Tap on any brain lobe to reveal its role in thought and memory",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // View Mode Switcher: 3D Hologram Canvas vs 2D Atlas Map
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (is3DMode) "3D Anatomy Explorer" else "2D Cortical Map",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(modifier = Modifier.padding(3.dp)) {
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = if (is3DMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(9.dp))
                                .clickable { is3DMode = true }
                                .testTag("btn_switch_3d_mode")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ViewInAr,
                                    contentDescription = null,
                                    tint = if (is3DMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "3D Canvas",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (is3DMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = if (!is3DMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(9.dp))
                                .clickable { is3DMode = false }
                                .testTag("btn_switch_2d_mode")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    tint = if (!is3DMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "2D Map",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (!is3DMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Interactive Brain Canvas (3D Inspired or 2D)
        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (is3DMode) {
                    Brain3DAnatomyExplorer(
                        selectedLobe = selectedLobe,
                        onLobeSelected = onLobeSelected
                    )
                } else {
                    InteractiveBrainCanvas(
                        selectedLobe = selectedLobe,
                        onLobeSelected = onLobeSelected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Region ${currentIndex + 1} of ${lobes.size}: ${selectedLobe.name}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = selectedLobe.accentColor
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                val prevIdx = if (currentIndex > 0) currentIndex - 1 else lobes.size - 1
                                onLobeSelected(lobes[prevIdx])
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("prev_lobe_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous brain region",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val nextIdx = (currentIndex + 1) % lobes.size
                                onLobeSelected(lobes[nextIdx])
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("next_lobe_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next brain region",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Horizontal Lobe Selector Chips
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lobes) { lobe ->
                    val isSelected = lobe.id == selectedLobe.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { onLobeSelected(lobe) },
                        label = {
                            Text(
                                text = lobe.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(lobe.accentColor)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = lobe.accentColor.copy(alpha = 0.22f),
                            selectedLabelColor = lobe.accentColor
                        ),
                        modifier = Modifier.testTag("lobe_chip_${lobe.id}")
                    )
                }
            }
        }

        // 4. Revealed Educational Description & Deep Dive Card
        item {
            AnimatedContent(
                targetState = selectedLobe,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                },
                label = "educational_description_transition",
                modifier = Modifier.padding(horizontal = 20.dp)
            ) { lobe ->
                EducationalDescriptionCard(
                    lobe = lobe,
                    onSpeak = { speakDescription("${lobe.name}. ${lobe.description}") },
                    isSpeaking = isTtsSpeaking,
                    onTrainClicked = { onTrainLobe?.invoke(lobe) }
                )
            }
        }
    }
}

/**
 * Displays the revealed educational description and structured cognitive functions of the selected region.
 */
@Composable
private fun EducationalDescriptionCard(
    lobe: BrainLobe,
    onSpeak: () -> Unit,
    isSpeaking: Boolean,
    onTrainClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableIntStateOf(0) }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("educational_description_card")
            .border(
                width = 1.dp,
                color = lobe.accentColor.copy(alpha = 0.35f),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row: Lobe Title + Latin Name + Speak Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(lobe.accentColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = lobe.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "${lobe.latinName} • ${lobe.subtitle}",
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Read Aloud / TTS button
                IconButton(
                    onClick = onSpeak,
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            if (isSpeaking) lobe.accentColor else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                        .testTag("read_description_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Read description aloud",
                        tint = if (isSpeaking) Color.White else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // REVEALED SHORT EDUCATIONAL DESCRIPTION (The core of user request)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = lobe.accentColor.copy(alpha = 0.10f),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = lobe.accentColor.copy(alpha = 0.25f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = lobe.accentColor,
                        modifier = Modifier
                            .size(22.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = lobe.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Sub-tabs: Functions / Neurotransmitters / Clinical
            TabRow(
                selectedTabIndex = selectedSection,
                containerColor = Color.Transparent,
                divider = {},
                indicator = {}
            ) {
                listOf("Functions", "Insight", "Chemistry").forEachIndexed { index, label ->
                    val isSelected = selectedSection == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedSection = index },
                        text = {
                            Text(
                                text = label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) lobe.accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedSection) {
                0 -> {
                    // Cognitive Functions list
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        lobe.functions.forEach { func ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = lobe.accentColor,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(top = 3.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = func,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Neuroscience Clinical Insight
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Clinical Insight",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFF59E0B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = lobe.clinicalInsight,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                2 -> {
                    // Active Neurotransmitters
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Key Neurochemical Messengers:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            lobe.keyNeurotransmitters.forEach { nt ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = nt,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action: Train in Brain Gym
            Button(
                onClick = onTrainClicked,
                colors = ButtonDefaults.buttonColors(containerColor = lobe.accentColor),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("train_region_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Train This Region in Brain Gym",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
