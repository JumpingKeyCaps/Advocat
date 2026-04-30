package com.lebaillyapp.advocat.prototype.draggeur.draggableDocument

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.zIndex
import com.lebaillyapp.advocat.prototype.draggeur.DocumentState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DraggableDocument2(
    state: DocumentState,
    globalScale: Float,
    onPointerDown: () -> Unit,
    minSize: Float = 0.7f,
    maxSize: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    var docSize by remember { mutableStateOf(IntSize.Zero) }
    var parentSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .zIndex(state.zIndex)
            .onGloballyPositioned { coordinates ->
                docSize = coordinates.size
                parentSize = coordinates.parentLayoutCoordinates?.size ?: IntSize.Zero
            }
            .offset {
                IntOffset(state.offset.value.x.roundToInt(), state.offset.value.y.roundToInt())
            }
            .pointerInput(globalScale) {
                awaitEachGesture {
                    awaitFirstDown()
                    onPointerDown()

                    do {
                        val event = awaitPointerEvent()
                        // ON RECUPERE TOUT : Pan, Rotation, Zoom
                        val panChange = event.calculatePan()
                        val rotationChange = event.calculateRotation()
                        val zoomChange = event.calculateZoom()

                        if (panChange != Offset.Zero || rotationChange != 0f || zoomChange != 1f) {
                            scope.launch {
                                // 1. TRANSLATION & ROTATION
                                state.offset.snapTo(state.offset.value + (panChange / globalScale))
                                state.rotation.snapTo(state.rotation.value + rotationChange)

                                // 2. ZOOM MANUEL (Pinch)
                                if (zoomChange != 1f) {
                                    val nextScale = (state.scale.value * zoomChange).coerceIn(0.5f, 1.5f)
                                    state.scale.snapTo(nextScale)
                                }

                                // 3. SCALING PROGRESSIF (Auto-Scale selon Y)
                                val screenH = parentSize.height.toFloat()
                                if (screenH > 0) {
                                    val threshold = screenH * 0.45f // % de la hauteur de l'écran
                                    // On base le scale sur le Y du centre pour la transition
                                    // Plus le Y est petit (en haut), plus le scale tend vers minSize
                                    val progress = (state.offset.value.y / threshold).coerceIn(0f, 1f)
                                    val progressiveScale = minSize + (maxSize - minSize) * progress

                                    // SnapTo pour coller au doigt sans délai d'animation
                                    state.scale.snapTo(progressiveScale)
                                }
                            }
                            event.changes.forEach { it.consume() }
                        }
                    } while (event.changes.any { it.pressed })

                    // --- 4. RELACHEMENT : RECADRAGE MAGNÉTIQUE ---
                   //... a faire !
                }
            }
    ) {
        Box(modifier = Modifier.graphicsLayer {
            scaleX = state.scale.value
            scaleY = state.scale.value
            rotationZ = state.rotation.value
            transformOrigin = TransformOrigin.Center // Pivot central pour rotation naturelle
        }) {
            content()
        }
    }
}