package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

data class EvolutionNode(
    val id: Int,
    val text: String,
    val angle: Float,
    val distanceRatio: Float,
    val color: Color
)

@Composable
fun EvolutionBuilderScreen(modifier: Modifier = Modifier) {
    var taskText by remember { mutableStateOf("") }
    var nodes by remember { mutableStateOf(listOf<EvolutionNode>()) }
    var nodeIdCounter by remember { mutableStateOf(0) }

    // Animation for slow rotation of the entire network
    val infiniteTransition = rememberInfiniteTransition(label = "network_rotation")
    val globalRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Pulse animation for the central node
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

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
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SelfImprovement,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Evolution Builder",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Text(
            text = "Visualize your internal growth. Add tasks focusing on respect, humility, and positive evolution.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        // Visual Builder Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (nodes.isEmpty()) {
                Text(
                    text = "The network is waiting to grow...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxRadius = minOf(size.width, size.height) / 2f * 0.8f
                
                val currentPulse = pulseAlpha // read before drawing

                // Draw central "Core" node
                drawCircle(
                    color = Color(0xFF6200EE).copy(alpha = currentPulse),
                    radius = 30f,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF6200EE),
                    radius = 15f,
                    center = center
                )

                // Draw connecting lines and satellite nodes
                nodes.forEach { node ->
                    // Calculate position with global rotation
                    val currentAngle = (node.angle + globalRotation) * (Math.PI / 180.0)
                    val distance = maxRadius * node.distanceRatio
                    
                    val nodeX = center.x + (Math.cos(currentAngle) * distance).toFloat()
                    val nodeY = center.y + (Math.sin(currentAngle) * distance).toFloat()
                    val nodeCenter = Offset(nodeX, nodeY)

                    // Draw connection line to core
                    drawLine(
                        color = node.color.copy(alpha = 0.4f),
                        start = center,
                        end = nodeCenter,
                        strokeWidth = 3f
                    )

                    // Draw satellite node
                    drawCircle(
                        color = node.color,
                        radius = 20f,
                        center = nodeCenter
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = 8f,
                        center = nodeCenter
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input Area
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().weight(0.4f)
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                OutlinedTextField(
                    value = taskText,
                    onValueChange = { taskText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Enter a growth task (e.g. 'Practice listening')") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (taskText.isNotBlank()) {
                            val colors = listOf(Color(0xFF42A5F5), Color(0xFF66BB6A), Color(0xFFFFA726), Color(0xFFAB47BC), Color(0xFFEF5350))
                            val newNode = EvolutionNode(
                                id = nodeIdCounter++,
                                text = taskText,
                                angle = (0..360).random().toFloat(),
                                distanceRatio = 0.4f + Math.random().toFloat() * 0.6f,
                                color = colors.random()
                            )
                            nodes = nodes + newNode
                            taskText = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = taskText.isNotBlank()
                ) {
                    Icon(Icons.Default.Build, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Construct Node")
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // List of active nodes
                Box(modifier = Modifier.weight(1f)) {
                    androidx.compose.foundation.lazy.LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(nodes) { node ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(12.dp).background(node.color, RoundedCornerShape(6.dp)))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = node.text,
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
}
