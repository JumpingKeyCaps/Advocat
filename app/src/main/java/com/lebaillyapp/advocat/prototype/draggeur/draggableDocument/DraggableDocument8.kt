package com.lebaillyapp.advocat.prototype.draggeur.draggableDocument

import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.exponentialDecay
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
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.lebaillyapp.advocat.prototype.draggeur.DocumentState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
fun DraggableDocument8(
    state: DocumentState,
    globalScale: Float,
    onPointerDown: () -> Unit,
    minSize: Float = 0.7f,
    maxSize: Float = 1.4f,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val view = androidx.compose.ui.platform.LocalView.current
    val density = LocalDensity.current

    var docSize by remember { mutableStateOf(IntSize.Zero) }
    var parentSize by remember { mutableStateOf(IntSize.Zero) }

    val lift = remember { Animatable(0f) }
    val tilt = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val motionBlurIntensity = remember { Animatable(0f) }

    val flingJob = remember { mutableStateOf<Job?>(null) }

    // Configuration
    val maxMagneticOverflowMargin = 0.05f
    val recoverMagneticSpeed = 3000
    val maxLateralTilt = 15f
    val maxVerticalTilt = 5f
    val tiltNervosity = 50f
    val ratioLiftScaling = 0.05f
    val flingFriction = 1.5f

    val flingRotationMaxDelta = 6f

    // Cache ombre ambiante
    val liftStep = (lift.value / 0.05f).roundToInt() * 0.05f
    val shadowBlurPx = with(density) { 2.dp.toPx() + (liftStep * 12.dp.toPx()) }
    val cachedShadowRenderEffect = remember(liftStep) {
        RenderEffect.createBlurEffect(shadowBlurPx, shadowBlurPx, Shader.TileMode.DECAL)
            .asComposeRenderEffect()
    }

    // Cache motion blur feuille
    val blurStep = (motionBlurIntensity.value / 0.5f).roundToInt() * 0.5f
    val cachedMotionBlurRenderEffect = remember(blurStep) {
        if (blurStep > 0.1f) {
            RenderEffect.createBlurEffect(
                blurStep * 2f,
                blurStep * 1f,
                Shader.TileMode.DECAL
            ).asComposeRenderEffect()
        } else {
            null
        }
    }

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
                    renderEffect = cachedShadowRenderEffect
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

                    val angleRad = Math.toRadians(state.rotation.value.toDouble())
                    val cosA = cos(angleRad).toFloat()
                    val sinA = sin(angleRad).toFloat()

                    val compensatedTiltX = tilt.value.x * cosA + tilt.value.y * sinA
                    val compensatedTiltY = -tilt.value.x * sinA + tilt.value.y * cosA

                    rotationY = compensatedTiltX * maxLateralTilt
                    rotationX = -compensatedTiltY * maxVerticalTilt
                    rotationZ = state.rotation.value

                    shadowElevation = (2.dp.toPx() + (lift.value * 5.dp.toPx()))
                    shape = RoundedCornerShape(4.dp)
                    clip = true
                    transformOrigin = TransformOrigin.Center

                    renderEffect = cachedMotionBlurRenderEffect
                }
                .pointerInput(globalScale) {
                    awaitEachGesture {
                        awaitFirstDown()
                        onPointerDown()

                        view.performHapticFeedback(android.view.HapticFeedbackConstants.GESTURE_START)

                        val velocityTracker = VelocityTracker()

                        scope.launch {
                            flingJob.value?.cancel()
                            state.offset.stop()
                            lift.animateTo(
                                1f,
                                spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }

                        do {
                            val event = awaitPointerEvent()
                            val panChange = event.calculatePan()
                            val rotationChange = event.calculateRotation()

                            event.changes.firstOrNull()?.let { change: PointerInputChange ->
                                velocityTracker.addPointerInputChange(change)
                            }

                            if (panChange != Offset.Zero || rotationChange != 0f) {
                                scope.launch {
                                    val speed = panChange.getDistance()
                                    launch { motionBlurIntensity.animateTo((speed / 20f).coerceAtMost(4f)) }

                                    launch {
                                        tilt.animateTo(
                                            Offset(
                                                (panChange.x / tiltNervosity).coerceIn(-1f, 1f),
                                                (panChange.y / tiltNervosity).coerceIn(-1f, 1f)
                                            ),
                                            spring(stiffness = Spring.StiffnessMedium)
                                        )
                                    }

                                    val angleRad = Math.toRadians(state.rotation.value.toDouble())
                                    val cosA = cos(angleRad).toFloat()
                                    val sinA = sin(angleRad).toFloat()
                                    val correctedPan = Offset(
                                        x = panChange.x * cosA - panChange.y * sinA,
                                        y = panChange.x * sinA + panChange.y * cosA
                                    )

                                    state.offset.snapTo(state.offset.value + (correctedPan / globalScale))
                                    state.rotation.snapTo(state.rotation.value + rotationChange)

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

                        val rawVelocity = velocityTracker.calculateVelocity()
                        val angleRadAtRelease = Math.toRadians(state.rotation.value.toDouble())
                        val cosR = cos(angleRadAtRelease).toFloat()
                        val sinR = sin(angleRadAtRelease).toFloat()
                        val velocityX = (rawVelocity.x * cosR - rawVelocity.y * sinR) / globalScale
                        val velocityY = (rawVelocity.x * sinR + rawVelocity.y * cosR) / globalScale

                        // ---------------------------------------------------------
                        // V8 : micro rotation Z aléatoire au lâcher
                        // Signe aléatoire (horaire/antihoraire), amplitude dans
                        // [0, flingRotationMaxDelta]. Calculé une seule fois au
                        // lâcher — pas de recalcul pendant le fling.
                        // On ne déclenche la rotation que si le fling a une vitesse
                        // minimale — évite la rotation sur un simple tap/drag lent.
                        // ---------------------------------------------------------
                        val flingSpeed = sqrt(velocityX * velocityX + velocityY * velocityY)
                        val rotationDelta = if (flingSpeed > 200f) {
                            val sign = if (Random.nextBoolean()) 1f else -1f
                            sign * Random.nextFloat() * flingRotationMaxDelta
                        } else {
                            0f
                        }

                        flingJob.value = scope.launch {
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.GESTURE_END)

                            launch {
                                lift.animateTo(
                                    0f,
                                    spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                            launch { tilt.animateTo(Offset.Zero, spring(stiffness = Spring.StiffnessLow)) }

                            // -------------------------------------------------
                            // V8 : micro rotation Z pendant le fling
                            // Animée en parallèle via spring, conservée à la fin.
                            // Lancée uniquement si flingSpeed dépasse le seuil.
                            // -------------------------------------------------
                            if (rotationDelta != 0f) {
                                launch {
                                    state.rotation.animateTo(
                                        state.rotation.value + rotationDelta,
                                        spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                }
                            }

                            val flingAnimX = Animatable(state.offset.value.x)
                            val flingAnimY = Animatable(state.offset.value.y)
                            val decaySpec = exponentialDecay<Float>(frictionMultiplier = flingFriction)

                            val jobX = launch {
                                flingAnimX.animateDecay(
                                    initialVelocity = velocityX,
                                    animationSpec = decaySpec
                                )
                            }

                            val jobY = launch {
                                flingAnimY.animateDecay(
                                    initialVelocity = velocityY,
                                    animationSpec = decaySpec
                                )
                            }

                            val jobSync = launch {
                                while (jobX.isActive || jobY.isActive) {
                                    withFrameNanos { }
                                    val newOffset = Offset(flingAnimX.value, flingAnimY.value)
                                    state.offset.snapTo(newOffset)

                                    // Autoscale pendant le fling
                                    val screenH = parentSize.height.toFloat()
                                    if (screenH > 0f) {
                                        val threshold = screenH * 0.45f
                                        val progress = (newOffset.y / threshold).coerceIn(0f, 1f)
                                        state.scale.snapTo(minSize + (maxSize - minSize) * progress)
                                    }

                                    // -----------------------------------------
                                    // V8 : motion blur lié à la vitesse du fling
                                    // Norme du vecteur vélocité courant, normalisée
                                    // sur la même échelle que pendant le drag.
                                    // snapTo direct — pas d'animateTo pour suivre
                                    // fidèlement la décélération frame par frame.
                                    // -----------------------------------------
                                    val currentSpeed = sqrt(
                                        flingAnimX.velocity * flingAnimX.velocity +
                                                flingAnimY.velocity * flingAnimY.velocity
                                    )
                                    motionBlurIntensity.snapTo(
                                        (currentSpeed / 280f).coerceAtMost(1f)
                                    )
                                }
                                // Blur à zéro proprement à la fin du fling
                                motionBlurIntensity.snapTo(0f)
                            }

                            jobX.join()
                            jobY.join()
                            jobSync.join()

                            // Magnétisme — déclenché après fin complète du fling
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
                            var cX = 0f; var cY = 0f
                            if (leafLeft < -maxOutX) cX = -maxOutX - leafLeft
                            else if (leafRight > parentSize.width + maxOutX) cX = (parentSize.width + maxOutX) - leafRight
                            if (leafTop < -maxOutY) cY = -maxOutY - leafTop
                            else if (leafBottom > parentSize.height + maxOutY) cY = (parentSize.height + maxOutY) - leafBottom

                            if (cX != 0f || cY != 0f) {
                                state.offset.animateTo(
                                    Offset(currentOffset.x + cX, currentOffset.y + cY),
                                    tween(recoverMagneticSpeed, easing = LinearOutSlowInEasing)
                                )
                            }
                        }
                    }
                }
        ) {
            content()
        }
    }
}