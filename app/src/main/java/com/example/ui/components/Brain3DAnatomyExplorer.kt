package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BrainDataRepository
import com.example.model.BrainLobe
import kotlin.math.*

/**
 * 3D Point in coordinate space (origin at brain center)
 * +X = Right, -X = Left
 * +Y = Superior (Top), -Y = Inferior (Base)
 * +Z = Anterior (Front/Forehead), -Z = Posterior (Occipital/Back)
 */
data class Point3D(val x: Float, val y: Float, val z: Float) {
    fun rotate(yawRad: Float, pitchRad: Float): Point3D {
        // Yaw (rotation around Y-axis)
        val cosY = cos(yawRad)
        val sinY = sin(yawRad)
        val x1 = x * cosY + z * sinY
        val z1 = -x * sinY + z * cosY
        val y1 = y

        // Pitch (rotation around X-axis)
        val cosP = cos(pitchRad)
        val sinP = sin(pitchRad)
        val y2 = y1 * cosP - z1 * sinP
        val z2 = y1 * sinP + z1 * cosP
        val x2 = x1

        return Point3D(x2, y2, z2)
    }

    fun project(
        canvasWidth: Float,
        canvasHeight: Float,
        focalLength: Float = 340f,
        zoom: Float = 1.0f
    ): ProjectedPoint {
        val distance = focalLength - z
        val scale = (focalLength / max(40f, distance)) * zoom
        val screenX = (canvasWidth / 2f) + (x * scale)
        val screenY = (canvasHeight / 2f) - (y * scale) // Canvas Y is inverted
        return ProjectedPoint(screenX, screenY, z, scale)
    }
}

data class ProjectedPoint(
    val screenX: Float,
    val screenY: Float,
    val depthZ: Float,
    val scale: Float
)

data class AnatomicalRegion3D(
    val id: String,
    val name: String,
    val shortLabel: String,
    val description: String,
    val functionHighlight: String,
    val center: Point3D,
    val radius: Float,
    val accentColor: Color,
    val isDeepStructure: Boolean = false
)

enum class ViewpointPreset(val label: String, val yawDeg: Float, val pitchDeg: Float) {
    ISOMETRIC_3D("3D Isometric", 42f, 18f),
    LATERAL("Sagittal Lateral", 85f, 5f),
    FRONTAL("Coronal Frontal", 0f, 10f),
    SUPERIOR("Superior Top", 0f, 75f)
}

/**
 * 3D-inspired interactive brain anatomy explorer component using Compose Canvas.
 * Allows users to rotate in 3D, tap on brain regions (Frontal Lobe, Hippocampus, etc.)
 * to see descriptive labels and real-time highlighted anatomical pathways.
 */
@Composable
fun Brain3DAnatomyExplorer(
    selectedLobe: BrainLobe,
    onLobeSelected: (BrainLobe) -> Unit,
    modifier: Modifier = Modifier,
    initialViewpoint: ViewpointPreset = ViewpointPreset.ISOMETRIC_3D
) {
    // Rotation state (degrees)
    var yawDeg by remember { mutableFloatStateOf(initialViewpoint.yawDeg) }
    var pitchDeg by remember { mutableFloatStateOf(initialViewpoint.pitchDeg) }
    var isAutoRotating by remember { mutableStateOf(false) }
    var zoomScale by remember { mutableFloatStateOf(1.05f) }

    // Pulse animation for selected region and neural synapses
    val infiniteTransition = rememberInfiniteTransition(label = "3d_brain_pulse")
    val orbitYaw by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_yaw"
    )

    val currentYaw = if (isAutoRotating) (yawDeg + orbitYaw) % 360f else yawDeg

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val waveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_alpha"
    )

    // Anatomical regions in 3D coordinate space
    val regions3D = remember {
        listOf(
            AnatomicalRegion3D(
                id = "FRONTAL",
                name = "Frontal Lobe",
                shortLabel = "Frontal (PFC)",
                description = "Executive decision-making, working memory, impulse inhibition, and voluntary motor planning.",
                functionHighlight = "Executive Function & Focus",
                center = Point3D(0f, 32f, 52f),
                radius = 48f,
                accentColor = Color(0xFF38BDF8) // Cyan / Blue
            ),
            AnatomicalRegion3D(
                id = "PARIETAL",
                name = "Parietal Lobe",
                shortLabel = "Parietal",
                description = "Somatosensory integration, 3D spatial navigation, body proprioception, and numerical computation.",
                functionHighlight = "Sensory & Spatial Logic",
                center = Point3D(0f, 55f, -12f),
                radius = 44f,
                accentColor = Color(0xFF10B981) // Emerald Green
            ),
            AnatomicalRegion3D(
                id = "OCCIPITAL",
                name = "Occipital Lobe",
                shortLabel = "Occipital",
                description = "Primary visual cortex (V1); processes optical retinotopy, orientation, motion, and color perception.",
                functionHighlight = "Visual Scene Processing",
                center = Point3D(0f, 16f, -68f),
                radius = 38f,
                accentColor = Color(0xFFF59E0B) // Amber
            ),
            AnatomicalRegion3D(
                id = "TEMPORAL",
                name = "Temporal Lobe",
                shortLabel = "Temporal",
                description = "Auditory cortex, speech comprehension (Wernicke's area), and memory consolidation via hippocampus.",
                functionHighlight = "Auditory & Language Recall",
                center = Point3D(-46f, -8f, 10f),
                radius = 42f,
                accentColor = Color(0xFFEC4899) // Pink
            ),
            AnatomicalRegion3D(
                id = "LIMBIC",
                name = "Hippocampus & Limbic Arch",
                shortLabel = "Hippocampus",
                description = "Deep medial temporal seahorse-shaped arch responsible for consolidating long-term memory and adult neurogenesis.",
                functionHighlight = "Memory Consolidation & Neurogenesis",
                center = Point3D(0f, 2f, 2f),
                radius = 32f,
                accentColor = Color(0xFFA855F7), // Purple
                isDeepStructure = true
            ),
            AnatomicalRegion3D(
                id = "CEREBELLUM",
                name = "Cerebellum",
                shortLabel = "Cerebellum",
                description = "Houses over 50% of brain neurons; coordinates fine motor control, balance, and sub-second timing.",
                functionHighlight = "Motor Timing & Precision",
                center = Point3D(0f, -44f, -48f),
                radius = 38f,
                accentColor = Color(0xFFF97316) // Orange
            )
        )
    }

    // Find currently selected 3D region
    val selected3D = regions3D.find { it.id == selectedLobe.id } ?: regions3D.first()

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = selectedLobe.accentColor.copy(alpha = 0.35f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("brain_3d_anatomy_explorer")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. Top Control Bar: Viewpoint Presets and 3D Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ViewInAr,
                        contentDescription = "3D Mode",
                        tint = selectedLobe.accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "3D Interactive Canvas",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Auto-Orbit Toggle Button
                    IconButton(
                        onClick = { isAutoRotating = !isAutoRotating },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_3d_orbit")
                    ) {
                        Icon(
                            imageVector = if (isAutoRotating) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = if (isAutoRotating) "Pause Orbit" else "Auto Rotate 3D",
                            tint = if (isAutoRotating) selectedLobe.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Reset / Preset Menu or Quick Reset
                    IconButton(
                        onClick = {
                            yawDeg = ViewpointPreset.ISOMETRIC_3D.yawDeg
                            pitchDeg = ViewpointPreset.ISOMETRIC_3D.pitchDeg
                            zoomScale = 1.05f
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_3d_reset")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset 3D View",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Quick Viewpoint Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ViewpointPreset.values().forEach { preset ->
                    val isCurrent = abs(yawDeg - preset.yawDeg) < 8f && abs(pitchDeg - preset.pitchDeg) < 8f
                    FilterChip(
                        selected = isCurrent,
                        onClick = {
                            isAutoRotating = false
                            yawDeg = preset.yawDeg
                            pitchDeg = preset.pitchDeg
                        },
                        label = {
                            Text(
                                text = preset.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = selectedLobe.accentColor.copy(alpha = 0.2f),
                            selectedLabelColor = selectedLobe.accentColor
                        ),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("btn_preset_${preset.name.lowercase()}")
                    )
                }
            }

            // 2. MAIN 3D COMPOSE CANVAS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(Color(0xFF0B1120)) // Deep atmospheric dark canvas for glowing 3D neural hologram
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            isAutoRotating = false
                            // Drag left/right controls Yaw (horizontal rotation)
                            yawDeg = (yawDeg + dragAmount.x * 0.45f) % 360f
                            // Drag up/down controls Pitch (vertical tilt)
                            pitchDeg = (pitchDeg - dragAmount.y * 0.45f).coerceIn(-75f, 85f)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { tapOffset ->
                            val canvasW = size.width.toFloat()
                            val canvasH = size.height.toFloat()

                            val yawRad = Math.toRadians(currentYaw.toDouble()).toFloat()
                            val pitchRad = Math.toRadians(pitchDeg.toDouble()).toFloat()

                            // Check proximity to projected 3D region anchors
                            var closestRegion: AnatomicalRegion3D? = null
                            var minDistance = Float.MAX_VALUE

                            regions3D.forEach { region ->
                                val rotatedPt = region.center.rotate(yawRad, pitchRad)
                                val projected = rotatedPt.project(canvasW, canvasH, zoom = zoomScale)

                                val dist = hypot(tapOffset.x - projected.screenX, tapOffset.y - projected.screenY)
                                val touchThreshold = 55f * projected.scale

                                if (dist < touchThreshold && dist < minDistance) {
                                    minDistance = dist
                                    closestRegion = region
                                }
                            }

                            closestRegion?.let { matched ->
                                val matchedLobe = BrainDataRepository.brainLobes.find { it.id == matched.id }
                                if (matchedLobe != null) {
                                    onLobeSelected(matchedLobe)
                                }
                            }
                        }
                    }
                    .testTag("brain_3d_canvas")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val yawRad = Math.toRadians(currentYaw.toDouble()).toFloat()
                    val pitchRad = Math.toRadians(pitchDeg.toDouble()).toFloat()

                    // Background holographic grid lines for 3D depth reference
                    drawHolographicBackdrop(w, h)

                    // 1. Project 3D Gyri, Sulci, and Hemisphere rib lines
                    drawVolumetricCerebralMesh(w, h, yawRad, pitchRad, zoomScale, selectedLobe.accentColor)

                    // 2. Project Hippocampus & Limbic 3D Arch (deep internal structure)
                    drawHippocampusArch3D(w, h, yawRad, pitchRad, zoomScale, selectedLobe.id == "LIMBIC", pulseScale)

                    // 3. Project 3D Anatomical Region Nodes and Callout Labels
                    drawAnatomicalRegions3D(
                        w = w,
                        h = h,
                        yawRad = yawRad,
                        pitchRad = pitchRad,
                        zoom = zoomScale,
                        regions = regions3D,
                        selectedId = selectedLobe.id,
                        pulseScale = pulseScale,
                        waveAlpha = waveAlpha
                    )
                }

                // Interactive Hint Overlay
                Text(
                    text = "Drag to rotate in 3D • Tap region or label to inspect",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 12.dp, bottom = 8.dp)
                )

                // Current Orientation Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.55f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Yaw: ${currentYaw.toInt()}° | Pitch: ${pitchDeg.toInt()}°",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // 3. DESCRIPTIVE LABEL & CLINICAL DETAIL CARD (Updated dynamically on tap)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .testTag("brain_3d_descriptive_label_card")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(selectedLobe.accentColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selected3D.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("selected_region_title")
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = selectedLobe.accentColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = selected3D.functionHighlight,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = selectedLobe.accentColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = selectedLobe.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("selected_region_description")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Key functions pill row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        selectedLobe.functions.take(2).forEach { fn ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = selectedLobe.accentColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = fn,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
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
}

/**
 * Draws background holographic guide grid on the Canvas
 */
private fun DrawScope.drawHolographicBackdrop(w: Float, h: Float) {
    val gridColor = Color(0xFF1E293B).copy(alpha = 0.45f)
    val step = 40.dp.toPx()

    var x = 0f
    while (x <= w) {
        drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 0.8f)
        x += step
    }

    var y = 0f
    while (y <= h) {
        drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 0.8f)
        y += step
    }

    // Subtle radial glow centered in the hologram canvas
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF0284C7).copy(alpha = 0.12f), Color.Transparent),
            center = Offset(w / 2f, h / 2f),
            radius = w * 0.45f
        ),
        radius = w * 0.45f,
        center = Offset(w / 2f, h / 2f)
    )
}

/**
 * Draws 3D volumetric cerebral ribs, latitude contours, and the longitudinal fissure
 * which accurately rotate and exhibit depth perspective in Compose Canvas.
 */
private fun DrawScope.drawVolumetricCerebralMesh(
    w: Float,
    h: Float,
    yawRad: Float,
    pitchRad: Float,
    zoom: Float,
    activeColor: Color
) {
    // 1. Longitudinal Fissure (Separating Left and Right Cerebral Hemispheres)
    val fissurePoints = listOf(
        Point3D(0f, 65f, -20f),
        Point3D(0f, 55f, 20f),
        Point3D(0f, 40f, 55f),
        Point3D(0f, 15f, 65f),
        Point3D(0f, -5f, 60f)
    )
    val projFissure = fissurePoints.map { it.rotate(yawRad, pitchRad).project(w, h, zoom = zoom) }
    for (i in 0 until projFissure.size - 1) {
        val p1 = projFissure[i]
        val p2 = projFissure[i + 1]
        drawLine(
            color = Color(0xFF38BDF8).copy(alpha = 0.45f),
            start = Offset(p1.screenX, p1.screenY),
            end = Offset(p2.screenX, p2.screenY),
            strokeWidth = 2.5f * ((p1.scale + p2.scale) / 2f),
            cap = StrokeCap.Round
        )
    }

    // 2. Left and Right Hemisphere Rib Contours (Latitude & Longitude 3D arcs)
    val latitudeAngles = listOf(-35f, -15f, 10f, 30f, 50f)
    latitudeAngles.forEach { latDeg ->
        val latRad = Math.toRadians(latDeg.toDouble()).toFloat()
        val r = 70f * cos(latRad)
        val y = 70f * sin(latRad)

        val circlePoints = mutableListOf<Point3D>()
        for (a in 0..360 step 30) {
            val aRad = Math.toRadians(a.toDouble()).toFloat()
            // Brain is slightly elongated anterior-posteriorly (Z-axis scale = 1.18) and narrower laterally (X-axis scale = 0.88)
            val px = r * cos(aRad) * 0.90f
            val pz = r * sin(aRad) * 1.15f
            circlePoints.add(Point3D(px, y, pz))
        }

        val projected = circlePoints.map { it.rotate(yawRad, pitchRad).project(w, h, zoom = zoom) }

        for (i in 0 until projected.size - 1) {
            val pt1 = projected[i]
            val pt2 = projected[i + 1]

            // Depth cues: points in front (depthZ > 0) are brighter and thicker
            val avgZ = (pt1.depthZ + pt2.depthZ) / 2f
            val alpha = (0.15f + (avgZ / 120f).coerceIn(-0.1f, 0.45f)).coerceIn(0.08f, 0.55f)
            val strokeW = (1.5f * ((pt1.scale + pt2.scale) / 2f)).coerceIn(0.8f, 3.0f)

            drawLine(
                color = Color(0xFF64748B).copy(alpha = alpha),
                start = Offset(pt1.screenX, pt1.screenY),
                end = Offset(pt2.screenX, pt2.screenY),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
        }
    }

    // 3. Central Sulcus 3D curves (Separating Frontal and Parietal Lobes on both hemispheres)
    listOf(-1f, 1f).forEach { side ->
        val sulcus3D = listOf(
            Point3D(side * 5f, 62f, 0f),
            Point3D(side * 28f, 52f, 8f),
            Point3D(side * 48f, 32f, 15f),
            Point3D(side * 55f, 10f, 18f)
        )
        val projSulcus = sulcus3D.map { it.rotate(yawRad, pitchRad).project(w, h, zoom = zoom) }
        for (i in 0 until projSulcus.size - 1) {
            val p1 = projSulcus[i]
            val p2 = projSulcus[i + 1]
            drawLine(
                color = Color(0xFF38BDF8).copy(alpha = 0.70f),
                start = Offset(p1.screenX, p1.screenY),
                end = Offset(p2.screenX, p2.screenY),
                strokeWidth = 2.8f,
                cap = StrokeCap.Round
            )
        }
    }

    // 4. Lateral Sylvian Fissure 3D curve (Separating Temporal from Frontal/Parietal)
    listOf(-1f, 1f).forEach { side ->
        val sylvian3D = listOf(
            Point3D(side * 22f, 12f, 38f),
            Point3D(side * 52f, 8f, 18f),
            Point3D(side * 56f, 15f, -15f),
            Point3D(side * 42f, 25f, -38f)
        )
        val projSylvian = sylvian3D.map { it.rotate(yawRad, pitchRad).project(w, h, zoom = zoom) }
        for (i in 0 until projSylvian.size - 1) {
            val p1 = projSylvian[i]
            val p2 = projSylvian[i + 1]
            drawLine(
                color = Color(0xFFEC4899).copy(alpha = 0.65f),
                start = Offset(p1.screenX, p1.screenY),
                end = Offset(p2.screenX, p2.screenY),
                strokeWidth = 2.4f,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Renders the Hippocampus & Limbic Arch in 3D:
 * Distinctive seahorse-curved paired structures inside the temporal lobes.
 */
private fun DrawScope.drawHippocampusArch3D(
    w: Float,
    h: Float,
    yawRad: Float,
    pitchRad: Float,
    zoom: Float,
    isSelected: Boolean,
    pulseScale: Float
) {
    val hippoColor = Color(0xFFA855F7) // Radiant purple / violet

    listOf(-1f, 1f).forEach { side ->
        // Hippocampal C-shaped arch coordinates in 3D
        val archPoints = listOf(
            Point3D(side * 14f, -4f, 24f), // Head / Amygdala junction
            Point3D(side * 24f, -2f, 14f), // Body anterior
            Point3D(side * 28f, 2f, -8f),  // Body middle
            Point3D(side * 22f, 12f, -24f), // Tail posterior
            Point3D(side * 12f, 18f, -28f)  // Fornix crus ascend
        )

        val projected = archPoints.map { it.rotate(yawRad, pitchRad).project(w, h, zoom = zoom) }

        // Draw glowing background tube for the hippocampus
        for (i in 0 until projected.size - 1) {
            val p1 = projected[i]
            val p2 = projected[i + 1]
            val glowWidth = if (isSelected) 14f * pulseScale else 8f

            drawLine(
                color = hippoColor.copy(alpha = if (isSelected) 0.55f else 0.28f),
                start = Offset(p1.screenX, p1.screenY),
                end = Offset(p2.screenX, p2.screenY),
                strokeWidth = glowWidth * ((p1.scale + p2.scale) / 2f),
                cap = StrokeCap.Round
            )

            drawLine(
                color = Color.White.copy(alpha = if (isSelected) 0.95f else 0.70f),
                start = Offset(p1.screenX, p1.screenY),
                end = Offset(p2.screenX, p2.screenY),
                strokeWidth = 3f * ((p1.scale + p2.scale) / 2f),
                cap = StrokeCap.Round
            )
        }

        // Distinctive dentate gyrus neural beads along the hippocampus
        projected.forEach { pt ->
            drawCircle(
                color = hippoColor,
                radius = (if (isSelected) 6f * pulseScale else 4.5f) * pt.scale,
                center = Offset(pt.screenX, pt.screenY)
            )
            drawCircle(
                color = Color.White,
                radius = 2.5f * pt.scale,
                center = Offset(pt.screenX, pt.screenY)
            )
        }
    }
}

/**
 * Draws 3D anatomical regions sorted by depth (painter's algorithm),
 * with leader lines, glowing halos, and floating descriptive labels.
 */
private fun DrawScope.drawAnatomicalRegions3D(
    w: Float,
    h: Float,
    yawRad: Float,
    pitchRad: Float,
    zoom: Float,
    regions: List<AnatomicalRegion3D>,
    selectedId: String,
    pulseScale: Float,
    waveAlpha: Float
) {
    // 1. Project all region centers and sort by depth Z (back to front)
    val projectedRegions = regions.map { region ->
        val rotated = region.center.rotate(yawRad, pitchRad)
        val projected = rotated.project(w, h, zoom = zoom)
        Triple(region, rotated, projected)
    }.sortedBy { it.third.depthZ }

    projectedRegions.forEach { (region, rotated, proj) ->
        val isSelected = region.id == selectedId
        val baseAlpha = (0.45f + (proj.depthZ / 100f).coerceIn(-0.25f, 0.55f)).coerceIn(0.2f, 1f)
        val nodeRadius = (if (isSelected) 18f * pulseScale else 12f) * proj.scale

        // Draw radial neural halo
        if (isSelected) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        region.accentColor.copy(alpha = 0.65f),
                        region.accentColor.copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = Offset(proj.screenX, proj.screenY),
                    radius = 48f * pulseScale * proj.scale
                ),
                radius = 48f * pulseScale * proj.scale,
                center = Offset(proj.screenX, proj.screenY)
            )
        }

        // Primary 3D Region Node
        drawCircle(
            color = region.accentColor.copy(alpha = baseAlpha),
            radius = nodeRadius,
            center = Offset(proj.screenX, proj.screenY)
        )

        // Inner glowing core
        drawCircle(
            color = Color.White.copy(alpha = if (isSelected) 1f else 0.85f),
            radius = (if (isSelected) 6.5f else 4f) * proj.scale,
            center = Offset(proj.screenX, proj.screenY)
        )

        // Pulsing selection ring
        if (isSelected) {
            drawCircle(
                color = Color.White.copy(alpha = waveAlpha),
                radius = 24f * pulseScale * proj.scale,
                center = Offset(proj.screenX, proj.screenY),
                style = Stroke(width = 2.5f)
            )
        }

        // Leader Line and Floating Callout Tag for the Selected Region (or prominent front-facing regions)
        if (isSelected || proj.depthZ > 15f) {
            val labelOffset = when (region.id) {
                "FRONTAL" -> Offset(45f, -30f)
                "PARIETAL" -> Offset(45f, -35f)
                "OCCIPITAL" -> Offset(-50f, -30f)
                "TEMPORAL" -> Offset(-55f, 25f)
                "LIMBIC" -> Offset(50f, 20f)
                "CEREBELLUM" -> Offset(-45f, 35f)
                else -> Offset(40f, -25f)
            }

            val targetX = (proj.screenX + labelOffset.x).coerceIn(20f, w - 80f)
            val targetY = (proj.screenY + labelOffset.y).coerceIn(20f, h - 20f)

            // Draw connecting leader line with elbow
            val elbowX = proj.screenX + (labelOffset.x * 0.4f)
            val elbowY = proj.screenY + (labelOffset.y * 0.4f)

            val leaderColor = if (isSelected) region.accentColor else Color.White.copy(alpha = 0.4f)
            drawLine(
                color = leaderColor,
                start = Offset(proj.screenX, proj.screenY),
                end = Offset(elbowX, elbowY),
                strokeWidth = if (isSelected) 1.8f else 1.0f
            )
            drawLine(
                color = leaderColor,
                start = Offset(elbowX, elbowY),
                end = Offset(targetX, targetY),
                strokeWidth = if (isSelected) 1.8f else 1.0f
            )

            // Draw text badge at target position
            val tagText = region.shortLabel
            val bgWidth = (tagText.length * 8.5f + 16f) * proj.scale
            val bgHeight = 22f * proj.scale

            drawRoundRect(
                color = if (isSelected) region.accentColor.copy(alpha = 0.90f) else Color(0xFF1E293B).copy(alpha = 0.75f),
                topLeft = Offset(targetX - 4f, targetY - bgHeight / 2f),
                size = Size(bgWidth, bgHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )

            if (isSelected) {
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(targetX - 4f, targetY - bgHeight / 2f),
                    size = Size(bgWidth, bgHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                    style = Stroke(width = 1.2f)
                )
            }
        }
    }
}
