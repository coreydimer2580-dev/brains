package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.pow
import kotlin.math.sqrt

data class BrainRegion(
    val id: String,
    val name: String,
    val function: String,
    val color: Color,
    val relativeX: Float,
    val relativeY: Float,
    val relativeRadius: Float
)

val brainRegions = listOf(
    BrainRegion("frontal", "Frontal Lobe", "Executive functions, thinking, planning, organizing, problem-solving, emotions, and behavioral control.", Color(0xFFEF5350), 0.35f, 0.45f, 0.25f),
    BrainRegion("parietal", "Parietal Lobe", "Perception, making sense of the world, arithmetic, spelling.", Color(0xFF42A5F5), 0.6f, 0.35f, 0.22f),
    BrainRegion("occipital", "Occipital Lobe", "Vision and visual processing.", Color(0xFF66BB6A), 0.75f, 0.55f, 0.18f),
    BrainRegion("temporal", "Temporal Lobe", "Memory, understanding, language.", Color(0xFFFFA726), 0.5f, 0.6f, 0.20f),
    BrainRegion("cerebellum", "Cerebellum", "Balance, coordination, and posture.", Color(0xFFAB47BC), 0.65f, 0.75f, 0.15f),
    BrainRegion("stem", "Brain Stem", "Breathing, heart rate, temperature.", Color(0xFF78909C), 0.55f, 0.85f, 0.12f)
)

@Composable
fun BrainMapScreen(modifier: Modifier = Modifier) {
    var selectedRegion by remember { mutableStateOf(brainRegions.first()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Interactive Brain Map",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Text(
            text = "Tap an abstract 3D region to explore its cognitive functions.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        // 3D Canvas Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.65f)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            val minDim = minOf(canvasWidth, canvasHeight)

                            val clicked = brainRegions.minByOrNull { region ->
                                val centerX = region.relativeX * canvasWidth
                                val centerY = region.relativeY * canvasHeight
                                val radius = region.relativeRadius * minDim
                                val dist = sqrt((offset.x - centerX).pow(2) + (offset.y - centerY).pow(2))
                                if (dist <= radius) dist else Float.MAX_VALUE
                            }

                            clicked?.let {
                                val centerX = it.relativeX * canvasWidth
                                val centerY = it.relativeY * canvasHeight
                                val radius = it.relativeRadius * minDim
                                val dist = sqrt((offset.x - centerX).pow(2) + (offset.y - centerY).pow(2))
                                if (dist <= radius) {
                                    selectedRegion = it
                                }
                            }
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val minDim = minOf(canvasWidth, canvasHeight)

                // Draw abstract background shadow for the brain
                drawCircle(
                    color = Color.Black.copy(alpha = 0.1f),
                    radius = minDim * 0.45f,
                    center = Offset(canvasWidth * 0.55f, canvasHeight * 0.60f)
                )

                brainRegions.forEach { region ->
                    val centerX = region.relativeX * canvasWidth
                    val centerY = region.relativeY * canvasHeight
                    val baseRadius = region.relativeRadius * minDim
                    
                    val isSelected = selectedRegion == region
                    val targetRadius = if (isSelected) baseRadius * 1.15f else baseRadius

                    val gradient = Brush.radialGradient(
                        colors = listOf(
                            region.color,
                            region.color.copy(alpha = 0.8f),
                            region.color.copy(alpha = 0.4f)
                        ),
                        center = Offset(centerX - targetRadius * 0.3f, centerY - targetRadius * 0.3f),
                        radius = targetRadius * 1.2f
                    )

                    // Outer glow if selected
                    if (isSelected) {
                        drawCircle(
                            color = region.color.copy(alpha = 0.3f),
                            radius = targetRadius * 1.25f,
                            center = Offset(centerX, centerY)
                        )
                    }

                    // Main 3D sphere
                    drawCircle(
                        brush = gradient,
                        radius = targetRadius,
                        center = Offset(centerX, centerY)
                    )

                    // 3D Highlight reflection
                    drawCircle(
                        color = Color.White.copy(alpha = 0.3f),
                        radius = targetRadius * 0.25f,
                        center = Offset(centerX - targetRadius * 0.4f, centerY - targetRadius * 0.4f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info Card
        Crossfade(
            targetState = selectedRegion,
            animationSpec = tween(400),
            label = "Region Info",
            modifier = Modifier.weight(0.35f)
        ) { region ->
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = region.color.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(region.color, RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = region.name,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = region.function,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
