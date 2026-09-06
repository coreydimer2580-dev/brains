package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.model.BrainDataRepository
import com.example.model.BrainLobe
import kotlin.math.hypot

@Composable
fun InteractiveBrainCanvas(
    selectedLobe: BrainLobe,
    onLobeSelected: (BrainLobe) -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulse animation for selected lobe and synapses
    val infiniteTransition = rememberInfiniteTransition(label = "brain_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val synapseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "synapse_alpha"
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val canvasW = size.width
                        val canvasH = size.height
                        // Check which lobe was tapped closest
                        var closestLobe: BrainLobe? = null
                        var minDistance = Float.MAX_VALUE

                        BrainDataRepository.brainLobes.forEach { lobe ->
                            val lobeX = lobe.relativePositionX * canvasW
                            val lobeY = lobe.relativePositionY * canvasH
                            val dist = hypot(tapOffset.x - lobeX, tapOffset.y - lobeY)
                            if (dist < minDistance && dist < 120f) { // Within tap radius
                                minDistance = dist
                                closestLobe = lobe
                            }
                        }
                        closestLobe?.let { onLobeSelected(it) }
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            // 1. Draw outer cerebral cortex contour
            drawCerebralContour(w, h)

            // 2. Draw neural interconnecting pathways (synaptic network lines)
            drawNeuralPathways(w, h, synapseAlpha)

            // 3. Draw lobe node centers with glowing halos
            BrainDataRepository.brainLobes.forEach { lobe ->
                val isSelected = lobe.id == selectedLobe.id
                val centerX = lobe.relativePositionX * w
                val centerY = lobe.relativePositionY * h

                // Outer Halo if selected
                if (isSelected) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                lobe.accentColor.copy(alpha = 0.55f),
                                lobe.accentColor.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            center = Offset(centerX, centerY),
                            radius = 65f * pulseScale
                        ),
                        radius = 65f * pulseScale,
                        center = Offset(centerX, centerY)
                    )
                }

                // Node background
                drawCircle(
                    color = if (isSelected) lobe.accentColor else lobe.accentColor.copy(alpha = 0.65f),
                    radius = if (isSelected) 24f else 18f,
                    center = Offset(centerX, centerY)
                )

                // Inner core spark
                drawCircle(
                    color = Color.White,
                    radius = if (isSelected) 8f else 5f,
                    center = Offset(centerX, centerY)
                )

                // Selected ring outline
                if (isSelected) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = 32f * pulseScale,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 3f)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawCerebralContour(w: Float, h: Float) {
    val contourPath = Path().apply {
        // Approximate lateral profile of human cerebrum
        moveTo(w * 0.14f, h * 0.42f)
        // Frontal pole curve
        cubicTo(w * 0.10f, h * 0.28f, w * 0.22f, h * 0.15f, w * 0.40f, h * 0.14f)
        // Superior parietal contour
        cubicTo(w * 0.58f, h * 0.13f, w * 0.78f, h * 0.18f, w * 0.88f, h * 0.32f)
        // Occipital pole
        cubicTo(w * 0.96f, h * 0.42f, w * 0.94f, h * 0.60f, w * 0.84f, h * 0.68f)
        // Cerebellar recess
        cubicTo(w * 0.82f, h * 0.72f, w * 0.84f, h * 0.86f, w * 0.72f, h * 0.90f)
        // Brainstem base
        cubicTo(w * 0.62f, h * 0.92f, w * 0.55f, h * 0.82f, w * 0.52f, h * 0.74f)
        // Temporal pole curve
        cubicTo(w * 0.44f, h * 0.76f, w * 0.30f, h * 0.75f, w * 0.24f, h * 0.62f)
        // Return to frontal base
        cubicTo(w * 0.18f, h * 0.55f, w * 0.12f, h * 0.52f, w * 0.14f, h * 0.42f)
        close()
    }

    // Subtle gradient fill inside brain silhouette
    drawPath(
        path = contourPath,
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF38BDF8).copy(alpha = 0.12f),
                Color(0xFF818CF8).copy(alpha = 0.18f),
                Color(0xFFA855F7).copy(alpha = 0.12f)
            ),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )
    )

    // Glowing border outline
    drawPath(
        path = contourPath,
        color = Color(0xFF38BDF8).copy(alpha = 0.45f),
        style = Stroke(
            width = 3.5f,
            pathEffect = PathEffect.cornerPathEffect(16f)
        )
    )

    // Inner gyri grooves
    drawGyriFolds(w, h)
}

private fun DrawScope.drawGyriFolds(w: Float, h: Float) {
    val foldColor = Color(0xFF38BDF8).copy(alpha = 0.22f)
    val stroke = Stroke(width = 2f, cap = StrokeCap.Round)

    // Central sulcus approximation
    val centralSulcus = Path().apply {
        moveTo(w * 0.50f, h * 0.14f)
        cubicTo(w * 0.48f, h * 0.28f, w * 0.52f, h * 0.40f, w * 0.44f, h * 0.55f)
    }
    drawPath(centralSulcus, foldColor, style = stroke)

    // Lateral fissure (Sylvian fissure)
    val sylvianFissure = Path().apply {
        moveTo(w * 0.24f, h * 0.55f)
        cubicTo(w * 0.40f, h * 0.52f, w * 0.58f, h * 0.53f, w * 0.72f, h * 0.46f)
    }
    drawPath(sylvianFissure, foldColor, style = stroke)

    // Parieto-occipital sulcus
    val parietoOccipital = Path().apply {
        moveTo(w * 0.76f, h * 0.24f)
        cubicTo(w * 0.72f, h * 0.36f, w * 0.78f, h * 0.44f, w * 0.82f, h * 0.52f)
    }
    drawPath(parietoOccipital, foldColor, style = stroke)
}

private fun DrawScope.drawNeuralPathways(w: Float, h: Float, alpha: Float) {
    val lobes = BrainDataRepository.brainLobes
    val connections = listOf(
        Pair(0, 1), // Frontal -> Parietal
        Pair(0, 3), // Frontal -> Temporal
        Pair(0, 5), // Frontal -> Limbic
        Pair(1, 2), // Parietal -> Occipital
        Pair(1, 5), // Parietal -> Limbic
        Pair(2, 3), // Occipital -> Temporal
        Pair(3, 5), // Temporal -> Limbic
        Pair(4, 5), // Cerebellum -> Limbic/Brainstem
        Pair(4, 2)  // Cerebellum -> Occipital
    )

    connections.forEach { (i, j) ->
        val p1 = Offset(lobes[i].relativePositionX * w, lobes[i].relativePositionY * h)
        val p2 = Offset(lobes[j].relativePositionX * w, lobes[j].relativePositionY * h)

        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    lobes[i].accentColor.copy(alpha = 0.45f * alpha),
                    lobes[j].accentColor.copy(alpha = 0.45f * alpha)
                ),
                start = p1,
                end = p2
            ),
            start = p1,
            end = p2,
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
    }
}
