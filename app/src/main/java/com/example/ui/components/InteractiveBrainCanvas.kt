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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
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
        initialValue = 0.88f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val synapseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "synapse_alpha"
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = selectedLobe.accentColor.copy(alpha = 0.3f)
        ),
        modifier = modifier.testTag("brain_explorer_canvas")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val canvasW = size.width
                        val canvasH = size.height
                        val normX = tapOffset.x / canvasW
                        val normY = tapOffset.y / canvasH

                        // 1. Proximity check to lobe node centers
                        var closestLobe: BrainLobe? = null
                        var minDistance = Float.MAX_VALUE

                        BrainDataRepository.brainLobes.forEach { lobe ->
                            val lobeX = lobe.relativePositionX * canvasW
                            val lobeY = lobe.relativePositionY * canvasH
                            val dist = hypot(tapOffset.x - lobeX, tapOffset.y - lobeY)
                            if (dist < minDistance && dist < 140f) {
                                minDistance = dist
                                closestLobe = lobe
                            }
                        }

                        // 2. If not directly near a node center, check anatomical region bounds
                        val selected = closestLobe ?: run {
                            when {
                                normY > 0.68f && normX > 0.55f ->
                                    BrainDataRepository.brainLobes.find { it.id == "CEREBELLUM" }
                                normX in 0.38f..0.60f && normY in 0.38f..0.58f ->
                                    BrainDataRepository.brainLobes.find { it.id == "LIMBIC" }
                                normX < 0.45f && normY < 0.60f ->
                                    BrainDataRepository.brainLobes.find { it.id == "FRONTAL" }
                                normX in 0.45f..0.76f && normY < 0.50f ->
                                    BrainDataRepository.brainLobes.find { it.id == "PARIETAL" }
                                normX > 0.74f && normY < 0.68f ->
                                    BrainDataRepository.brainLobes.find { it.id == "OCCIPITAL" }
                                normX in 0.22f..0.65f && normY in 0.52f..0.76f ->
                                    BrainDataRepository.brainLobes.find { it.id == "TEMPORAL" }
                                else -> null
                            }
                        }

                        selected?.let { onLobeSelected(it) }
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            // 1. Draw distinct anatomical lobe region patches with color highlights
            drawLobeRegionAreas(w, h, selectedLobe, pulseScale)

            // 2. Draw anatomical fissures and sulci lines
            drawGyriFolds(w, h)

            // 3. Draw outer cerebral cortex glowing contour
            drawCerebralContour(w, h, selectedLobe.accentColor)

            // 4. Draw neural interconnecting pathways (synaptic network lines)
            drawNeuralPathways(w, h, synapseAlpha)

            // 5. Draw lobe node centers with glowing halos and labels
            BrainDataRepository.brainLobes.forEach { lobe ->
                val isSelected = lobe.id == selectedLobe.id
                val centerX = lobe.relativePositionX * w
                val centerY = lobe.relativePositionY * h

                // Outer Halo if selected
                if (isSelected) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                lobe.accentColor.copy(alpha = 0.60f),
                                lobe.accentColor.copy(alpha = 0.20f),
                                Color.Transparent
                            ),
                            center = Offset(centerX, centerY),
                            radius = 70f * pulseScale
                        ),
                        radius = 70f * pulseScale,
                        center = Offset(centerX, centerY)
                    )
                }

                // Node background
                drawCircle(
                    color = if (isSelected) lobe.accentColor else lobe.accentColor.copy(alpha = 0.75f),
                    radius = if (isSelected) 22f else 16f,
                    center = Offset(centerX, centerY)
                )

                // Inner spark
                drawCircle(
                    color = Color.White,
                    radius = if (isSelected) 8f else 5f,
                    center = Offset(centerX, centerY)
                )

                // Selected ring outline
                if (isSelected) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.9f),
                        radius = 30f * pulseScale,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 3.5f)
                    )
                }
            }
        }
    }
}

/**
 * Draws filled anatomical zones for each lobe region so each area is visually identifiable and selectable.
 */
private fun DrawScope.drawLobeRegionAreas(w: Float, h: Float, selectedLobe: BrainLobe, pulseScale: Float) {
    val lobes = BrainDataRepository.brainLobes

    lobes.forEach { lobe ->
        val isSelected = lobe.id == selectedLobe.id
        val regionPath = getLobeRegionPath(lobe.id, w, h) ?: return@forEach

        // Fill region area with distinctive lobe color
        val fillAlpha = if (isSelected) 0.38f else 0.12f
        drawPath(
            path = regionPath,
            brush = Brush.radialGradient(
                colors = listOf(
                    lobe.accentColor.copy(alpha = fillAlpha * (if (isSelected) 1.15f else 1.0f)),
                    lobe.accentColor.copy(alpha = fillAlpha * 0.5f),
                    Color.Transparent
                ),
                center = Offset(lobe.relativePositionX * w, lobe.relativePositionY * h),
                radius = w * 0.35f
            )
        )

        // If selected, draw active region perimeter outline
        if (isSelected) {
            drawPath(
                path = regionPath,
                color = lobe.accentColor.copy(alpha = 0.75f),
                style = Stroke(
                    width = 3f * pulseScale,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.cornerPathEffect(18f)
                )
            )
        }
    }
}

private fun getLobeRegionPath(lobeId: String, w: Float, h: Float): Path? {
    return when (lobeId) {
        "FRONTAL" -> Path().apply {
            moveTo(w * 0.14f, h * 0.42f)
            cubicTo(w * 0.10f, h * 0.28f, w * 0.22f, h * 0.15f, w * 0.40f, h * 0.14f)
            cubicTo(w * 0.48f, h * 0.14f, w * 0.50f, h * 0.22f, w * 0.48f, h * 0.35f)
            cubicTo(w * 0.46f, h * 0.46f, w * 0.40f, h * 0.53f, w * 0.24f, h * 0.55f)
            cubicTo(w * 0.18f, h * 0.52f, w * 0.12f, h * 0.48f, w * 0.14f, h * 0.42f)
            close()
        }
        "PARIETAL" -> Path().apply {
            moveTo(w * 0.48f, h * 0.14f)
            cubicTo(w * 0.58f, h * 0.13f, w * 0.74f, h * 0.16f, w * 0.84f, h * 0.28f)
            cubicTo(w * 0.82f, h * 0.38f, w * 0.75f, h * 0.44f, w * 0.70f, h * 0.48f)
            cubicTo(w * 0.55f, h * 0.52f, w * 0.46f, h * 0.46f, w * 0.48f, h * 0.35f)
            close()
        }
        "OCCIPITAL" -> Path().apply {
            moveTo(w * 0.84f, h * 0.28f)
            cubicTo(w * 0.94f, h * 0.38f, w * 0.96f, h * 0.52f, w * 0.88f, h * 0.64f)
            cubicTo(w * 0.80f, h * 0.66f, w * 0.75f, h * 0.58f, w * 0.72f, h * 0.48f)
            close()
        }
        "TEMPORAL" -> Path().apply {
            moveTo(w * 0.24f, h * 0.55f)
            cubicTo(w * 0.40f, h * 0.52f, w * 0.60f, h * 0.50f, w * 0.72f, h * 0.52f)
            cubicTo(w * 0.70f, h * 0.68f, w * 0.55f, h * 0.76f, w * 0.40f, h * 0.74f)
            cubicTo(w * 0.28f, h * 0.72f, w * 0.22f, h * 0.65f, w * 0.24f, h * 0.55f)
            close()
        }
        "CEREBELLUM" -> Path().apply {
            moveTo(w * 0.56f, h * 0.74f)
            cubicTo(w * 0.70f, h * 0.70f, w * 0.84f, h * 0.68f, w * 0.86f, h * 0.78f)
            cubicTo(w * 0.84f, h * 0.88f, w * 0.70f, h * 0.92f, w * 0.60f, h * 0.90f)
            cubicTo(w * 0.54f, h * 0.84f, w * 0.52f, h * 0.78f, w * 0.56f, h * 0.74f)
            close()
        }
        "LIMBIC" -> Path().apply {
            addOval(
                Rect(
                    center = Offset(w * 0.50f, h * 0.46f),
                    radius = w * 0.12f
                )
            )
        }
        else -> null
    }
}

private fun DrawScope.drawCerebralContour(w: Float, h: Float, activeColor: Color) {
    val contourPath = Path().apply {
        moveTo(w * 0.14f, h * 0.42f)
        cubicTo(w * 0.10f, h * 0.28f, w * 0.22f, h * 0.15f, w * 0.40f, h * 0.14f)
        cubicTo(w * 0.58f, h * 0.13f, w * 0.78f, h * 0.18f, w * 0.88f, h * 0.32f)
        cubicTo(w * 0.96f, h * 0.42f, w * 0.94f, h * 0.60f, w * 0.84f, h * 0.68f)
        cubicTo(w * 0.82f, h * 0.72f, w * 0.84f, h * 0.86f, w * 0.72f, h * 0.90f)
        cubicTo(w * 0.62f, h * 0.92f, w * 0.55f, h * 0.82f, w * 0.52f, h * 0.74f)
        cubicTo(w * 0.44f, h * 0.76f, w * 0.30f, h * 0.75f, w * 0.24f, h * 0.62f)
        cubicTo(w * 0.18f, h * 0.55f, w * 0.12f, h * 0.52f, w * 0.14f, h * 0.42f)
        close()
    }

    // Glowing border outline
    drawPath(
        path = contourPath,
        color = activeColor.copy(alpha = 0.55f),
        style = Stroke(
            width = 3.5f,
            pathEffect = PathEffect.cornerPathEffect(16f)
        )
    )
}

private fun DrawScope.drawGyriFolds(w: Float, h: Float) {
    val foldColor = Color(0xFF38BDF8).copy(alpha = 0.35f)
    val stroke = Stroke(width = 2.2f, cap = StrokeCap.Round)

    // Central sulcus (separating Frontal and Parietal)
    val centralSulcus = Path().apply {
        moveTo(w * 0.50f, h * 0.14f)
        cubicTo(w * 0.48f, h * 0.28f, w * 0.50f, h * 0.38f, w * 0.44f, h * 0.52f)
    }
    drawPath(centralSulcus, foldColor, style = stroke)

    // Lateral fissure (Sylvian fissure - separating Temporal from Frontal/Parietal)
    val sylvianFissure = Path().apply {
        moveTo(w * 0.24f, h * 0.55f)
        cubicTo(w * 0.40f, h * 0.52f, w * 0.58f, h * 0.53f, w * 0.72f, h * 0.48f)
    }
    drawPath(sylvianFissure, foldColor, style = stroke)

    // Parieto-occipital sulcus
    val parietoOccipital = Path().apply {
        moveTo(w * 0.76f, h * 0.22f)
        cubicTo(w * 0.74f, h * 0.34f, w * 0.78f, h * 0.42f, w * 0.80f, h * 0.50f)
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
                    lobes[i].accentColor.copy(alpha = 0.50f * alpha),
                    lobes[j].accentColor.copy(alpha = 0.50f * alpha)
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

