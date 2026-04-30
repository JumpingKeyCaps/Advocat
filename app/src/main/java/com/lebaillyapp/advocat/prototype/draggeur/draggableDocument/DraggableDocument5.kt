package com.lebaillyapp.advocat.prototype.draggeur.draggableDocument

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
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
    val view = androidx.compose.ui.platform.LocalView.current
    var docSize by remember { mutableStateOf(IntSize.Zero) }
    var parentSize by remember { mutableStateOf(IntSize.Zero) }

    val lift = remember { Animatable(0f) }
    val tilt = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val motionBlurIntensity = remember { Animatable(0f) }

    // Configuration
    val maxMagneticOverflowMargin = 0.05f
    val recoverMagneticSpeed = 3000
    val maxLateralTilt = 15f
    val maxVerticalTilt = 5f
    val tiltNervosity = 50f
    val ratioLiftScaling = 0.05f

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
    ) {
        // --- 1. OCCLUSION AMBIANTE ---
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    val shadowScale = 1f + (lift.value * 0.1f)
                    scaleX = state.scale.value * shadowScale
                    scaleY = state.scale.value * shadowScale
                    rotationZ = state.rotation.value
                    alpha = (0.2f - (lift.value * 0.12f)).coerceAtLeast(0f)

                    renderEffect = android.graphics.RenderEffect.createBlurEffect(
                        (2.dp.toPx() + (lift.value * 12.dp.toPx())),
                        (2.dp.toPx() + (lift.value * 12.dp.toPx())),
                        android.graphics.Shader.TileMode.DECAL
                    ).asComposeRenderEffect()
                }
                .background(Color.Black, RoundedCornerShape(4.dp))
        )

        // --- 2. LA FEUILLE ---
        Box(
            modifier = Modifier
                .graphicsLayer {
                    val liftScale = 1f + (lift.value * ratioLiftScaling)
                    scaleX = state.scale.value * liftScale
                    scaleY = state.scale.value * liftScale

                    // --- COMPENSATION DU TILT ---
                    // On calcule l'angle inverse pour que le tilt reste aligné sur l'écran
                    val angleRad = Math.toRadians(state.rotation.value.toDouble())
                    val cosA = cos(angleRad).toFloat()
                    val sinA = sin(angleRad).toFloat()

                    // Rotation inverse du vecteur de Tilt
                    val compensatedTiltX = tilt.value.x * cosA + tilt.value.y * sinA
                    val compensatedTiltY = -tilt.value.x * sinA + tilt.value.y * cosA

                    rotationY = compensatedTiltX * maxLateralTilt
                    rotationX = -compensatedTiltY * maxVerticalTilt

                    rotationZ = state.rotation.value

                    shadowElevation = (2.dp.toPx() + (lift.value * 5.dp.toPx()))
                    shape = RoundedCornerShape(4.dp)
                    clip = true
                    transformOrigin = TransformOrigin.Center

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

                        view.performHapticFeedback(android.view.HapticFeedbackConstants.GESTURE_START)

                        scope.launch {
                            state.offset.stop()
                            lift.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
                        }

                        do {
                            val event = awaitPointerEvent()
                            val panChange = event.calculatePan()
                            val rotationChange = event.calculateRotation()

                            if (panChange != Offset.Zero || rotationChange != 0f) {
                                scope.launch {
                                    val speed = panChange.getDistance()
                                    launch { motionBlurIntensity.animateTo((speed / 20f).coerceAtMost(4f)) }

                                    // On stocke le tilt brut (basé sur le mouvement écran)
                                    launch {
                                        tilt.animateTo(
                                            Offset(
                                                (panChange.x / tiltNervosity).coerceIn(-1f, 1f),
                                                (panChange.y / tiltNervosity).coerceIn(-1f, 1f)
                                            ),
                                            spring(stiffness = Spring.StiffnessMedium)
                                        )
                                    }

                                    // MOUVEMENT (Déjà compensé)
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
                                        state.scale.snapTo(minSize + (maxSize - minSize) * progress)
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

                        scope.launch {
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.GESTURE_END)
                            launch { lift.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
                            launch { tilt.animateTo(Offset.Zero, spring(stiffness = Spring.StiffnessLow)) }
                            launch { motionBlurIntensity.animateTo(0f) }

                            // Logique de magnétisme simplifiée pour le test direct
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
                            var cX = 0f ; var cY = 0f
                            if (leafLeft < -maxOutX) cX = -maxOutX - leafLeft
                            else if (leafRight > parentSize.width + maxOutX) cX = (parentSize.width + maxOutX) - leafRight
                            if (leafTop < -maxOutY) cY = -maxOutY - leafTop
                            else if (leafBottom > parentSize.height + maxOutY) cY = (parentSize.height + maxOutY) - leafBottom

                            if (cX != 0f || cY != 0f) {
                                state.offset.animateTo(Offset(currentOffset.x + cX, currentOffset.y + cY), tween(recoverMagneticSpeed, easing = LinearOutSlowInEasing))
                            }
                        }
                    }
                }
        ) {
            content()
        }
    }
}