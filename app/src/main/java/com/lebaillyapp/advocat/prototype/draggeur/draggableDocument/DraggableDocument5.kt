package com.lebaillyapp.advocat.prototype.draggeur.draggableDocument

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.lebaillyapp.advocat.prototype.draggeur.DocumentState
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin



@Composable
fun DraggableDocument5(
    state: DocumentState,
    globalScale: Float,
    onPointerDown: () -> Unit,
    minSize: Float = 0.7f,
    maxSize: Float = 1.4f,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    var docSize by remember { mutableStateOf(IntSize.Zero) }
    var parentSize by remember { mutableStateOf(IntSize.Zero) }

    // --- ÉTATS PHYSIQUES ---
    val lift = remember { Animatable(0f) }
    val tilt = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val motionBlurIntensity = remember { Animatable(0f) }

    // --- Configuration ---
    val maxMagneticOverflowMargin = 0.05f
    val recoverMagneticSpeed = 3000
    val maxLateralTilt = 15f
    val maxVerticalTilt = 5f
    val tiltNervosity = 50f
    val ratioLiftScaling = 0.05f
    val minElevationLift = 2f
    val maxElevationLift = 5f

    Box(
        modifier = Modifier
            .zIndex(state.zIndex)
            .onGloballyPositioned { coordinates ->
                docSize = coordinates.size
                parentSize = coordinates.parentLayoutCoordinates?.size ?: IntSize.Zero
            }
            .offset {
                IntOffset(
                    x = state.offset.value.x.roundToInt(),
                    y = state.offset.value.y.roundToInt()
                )
            }
            .graphicsLayer {
                val liftScale = 1f + (lift.value * ratioLiftScaling)
                scaleX = state.scale.value * liftScale
                scaleY = state.scale.value * liftScale

                rotationY = tilt.value.x * maxLateralTilt
                rotationX = -tilt.value.y * maxVerticalTilt
                rotationZ = state.rotation.value

                shadowElevation = (minElevationLift.dp.toPx() + (lift.value * maxElevationLift.dp.toPx()))
                shape = RoundedCornerShape(4.dp)
                clip = true

                // Pivot fixe au centre pour la stabilité
                transformOrigin = TransformOrigin.Center

                // MOTION BLUR
                if (motionBlurIntensity.value > 0.1f) {
                    renderEffect = android.graphics.RenderEffect.createBlurEffect(
                        motionBlurIntensity.value * 2f,
                        motionBlurIntensity.value * 1f,
                        android.graphics.Shader.TileMode.DECAL
                    ).asComposeRenderEffect()
                }
            }
            .pointerInput(globalScale) {
                awaitEachGesture {
                    awaitFirstDown()
                    onPointerDown()

                    scope.launch {
                        state.offset.stop()
                        // Petit rebond à la saisie
                        lift.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
                    }

                    do {
                        val event = awaitPointerEvent()
                        val panChange = event.calculatePan()
                        val rotationChange = event.calculateRotation()

                        if (panChange != Offset.Zero || rotationChange != 0f) {
                            scope.launch {
                                // TILT
                                val tiltTargetX = (panChange.x / tiltNervosity).coerceIn(-1f, 1f)
                                val tiltTargetY = (panChange.y / tiltNervosity).coerceIn(-1f, 1f)

                                // BLUR basé sur la vitesse
                                val speed = panChange.getDistance()
                                launch { motionBlurIntensity.animateTo((speed / 20f).coerceAtMost(4f)) }

                                launch {
                                    tilt.animateTo(
                                        Offset(tiltTargetX, tiltTargetY),
                                        spring(stiffness = Spring.StiffnessMedium)
                                    )
                                }

                                // COMPENSATION ROTATION
                                val angleRad = Math.toRadians(state.rotation.value.toDouble())
                                val cosA = cos(angleRad).toFloat()
                                val sinA = sin(angleRad).toFloat()
                                val correctedPan = Offset(
                                    x = panChange.x * cosA - panChange.y * sinA,
                                    y = panChange.x * sinA + panChange.y * cosA
                                )

                                state.offset.snapTo(state.offset.value + (correctedPan / globalScale))
                                state.rotation.snapTo(state.rotation.value + rotationChange)

                                // AUTOSCALE
                                val screenH = parentSize.height.toFloat()
                                if (screenH > 0f) {
                                    val threshold = screenH * 0.45f
                                    val progress = (state.offset.value.y / threshold).coerceIn(0f, 1f)
                                    val progressiveScale = minSize + (maxSize - minSize) * progress
                                    state.scale.snapTo(progressiveScale)
                                }
                            }
                            event.changes.forEach { it.consume() }
                        } else {
                            scope.launch {
                                tilt.animateTo(Offset.Zero, spring(stiffness = Spring.StiffnessLow))
                                motionBlurIntensity.animateTo(0f)
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    // --- RELÂCHEMENT ---
                    scope.launch {
                        // Atterrissage avec rebond (Springy-Z)
                        launch { lift.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
                        launch { tilt.animateTo(Offset.Zero, spring(stiffness = Spring.StiffnessLow)) }
                        launch { motionBlurIntensity.animateTo(0f) }

                        val scale = state.scale.value
                        val currentOffset = state.offset.value
                        val scaledWidth = docSize.width * scale
                        val scaledHeight = docSize.height * scale
                        val centerX = currentOffset.x + (docSize.width / 2f)
                        val centerY = currentOffset.y + (docSize.height / 2f)

                        val leafLeft = centerX - (scaledWidth / 2f)
                        val leafRight = centerX + (scaledWidth / 2f)
                        val leafTop = centerY - (scaledHeight / 2f)
                        val leafBottom = centerY + (scaledHeight / 2f)

                        val maxOutX = scaledWidth * maxMagneticOverflowMargin
                        val maxOutY = scaledHeight * maxMagneticOverflowMargin
                        var correctionX = 0f
                        var correctionY = 0f

                        if (leafLeft < -maxOutX) correctionX = -maxOutX - leafLeft
                        else if (leafRight > parentSize.width + maxOutX) correctionX = (parentSize.width + maxOutX) - leafRight

                        if (leafTop < -maxOutY) correctionY = -maxOutY - leafTop
                        else if (leafBottom > parentSize.height + maxOutY) correctionY = (parentSize.height + maxOutY) - leafBottom

                        if (correctionX != 0f || correctionY != 0f) {
                            state.offset.animateTo(
                                targetValue = Offset(currentOffset.x + correctionX, currentOffset.y + correctionY),
                                animationSpec = tween(durationMillis = recoverMagneticSpeed, easing = LinearOutSlowInEasing)
                            )
                        }
                    }
                }
            }
    ) {
        content()
    }
}