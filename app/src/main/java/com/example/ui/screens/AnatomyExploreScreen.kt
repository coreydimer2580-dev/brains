package com.example.ui.screens

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.BrainDataRepository
import com.example.model.BrainLobe
import com.example.model.Neurotransmitter
import com.example.ui.components.InteractiveBrainCanvas
import com.example.ui.components.SectionHeader
import com.example.viewmodel.ActiveGame
import com.example.viewmodel.BrainViewModel

@Composable
fun AnatomyExploreScreen(
    viewModel: BrainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedLobe by viewModel.selectedLobe.collectAsStateWithLifecycle()
    var selectedNeurotransmitter by remember { mutableStateOf<Neurotransmitter?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero Banner with Generated Image
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_brain_hero),
                    contentDescription = "Neural pathways brain illustration",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient overlay
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

                // Title & Tagline
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "NEUROSCIENCE ATLAS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "Interactive Human Brain",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Tap lobes on the cortex to inspect functional circuits",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Interactive Brain Canvas
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                InteractiveBrainCanvas(
                    selectedLobe = selectedLobe,
                    onLobeSelected = { viewModel.selectLobe(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )
            }
        }

        // Horizontal Lobe Selector Chips
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(BrainDataRepository.brainLobes) { lobe ->
                    val isSelected = lobe.id == selectedLobe.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectLobe(lobe) },
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
                            selectedContainerColor = lobe.accentColor.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) lobe.accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("lobe_chip_${lobe.id}")
                    )
                }
            }
        }

        // Lobe Deep Dive Card
        item {
            LobeDetailCard(
                lobe = selectedLobe,
                onTrainClicked = {
                    when (selectedLobe.id) {
                        "FRONTAL" -> viewModel.openGame(ActiveGame.STROOP_SPEED)
                        "PARIETAL" -> viewModel.openGame(ActiveGame.SYNAPSE_MATH)
                        "TEMPORAL" -> viewModel.openGame(ActiveGame.MEMORY_GRID)
                        else -> viewModel.openGame(ActiveGame.NEURO_QUIZ)
                    }
                },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        // Neurotransmitter Carousel Section
        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionHeader(
                    title = "Key Neurotransmitters",
                    subtitle = "Chemical messengers governing cognition, drive, and calmness"
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(BrainDataRepository.neurotransmitters) { nt ->
                        NeurotransmitterCard(
                            neurotransmitter = nt,
                            onClick = { selectedNeurotransmitter = nt }
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet for Neurotransmitter details
    if (selectedNeurotransmitter != null) {
        val nt = selectedNeurotransmitter!!
        AlertDialog(
            onDismissRequest = { selectedNeurotransmitter = null },
            confirmButton = {
                TextButton(onClick = { selectedNeurotransmitter = null }) {
                    Text("Close")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(nt.color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = nt.formulaSymbol,
                            color = nt.color,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = nt.name, fontWeight = FontWeight.Bold)
                        Text(
                            text = nt.role,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = nt.description,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Natural Optimizers:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    nt.naturalBoosters.forEach { booster ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(nt.color)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = booster, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun LobeDetailCard(
    lobe: BrainLobe,
    onTrainClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = lobe.accentColor.copy(alpha = 0.35f),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lobe.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${lobe.latinName} • ${lobe.subtitle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(lobe.accentColor)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Revealed Educational Description
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = lobe.accentColor.copy(alpha = 0.10f),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = lobe.accentColor.copy(alpha = 0.25f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = lobe.description,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Functions Section
            Text(
                text = "PRIMARY COGNITIVE FUNCTIONS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = lobe.accentColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            lobe.functions.forEach { func ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = lobe.accentColor,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = func,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Clinical Insight Box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = lobe.clinicalInsight,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action: Train in Brain Gym
            Button(
                onClick = onTrainClicked,
                colors = ButtonDefaults.buttonColors(containerColor = lobe.accentColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("train_lobe_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Train This Region in Brain Gym")
            }
        }
    }
}

@Composable
private fun NeurotransmitterCard(
    neurotransmitter: Neurotransmitter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier
            .width(180.dp)
            .border(
                width = 1.dp,
                color = neurotransmitter.color.copy(alpha = 0.25f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(neurotransmitter.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = neurotransmitter.formulaSymbol,
                    color = neurotransmitter.color,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Text(
                text = neurotransmitter.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = neurotransmitter.role,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}
