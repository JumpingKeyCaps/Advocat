package com.lebaillyapp.advocat.prototype.draggeur

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DraggableDocument2(
    state: DocumentState,
    globalScale: Float,
    onPointerDown: () -> Unit,
    minSize: Float = 0.7f,
    maxSize: Float = 1.4f,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .zIndex(state.zIndex)
            .offset {
                IntOffset(
                    state.offset.value.x.roundToInt(),
                    state.offset.value.y.roundToInt()
                )
            }
            .onGloballyPositioned { coordinates ->
                val yInWindow = coordinates.positionInWindow().y
                val screenHeight = coordinates.parentLayoutCoordinates?.size?.height ?: 2000
                val threshold = screenHeight / 3f

                //L'Hystérésis
                val targetScale = when {
                    yInWindow < threshold - 20 -> 0.7f
                    yInWindow > threshold + 20 -> 1.2f
                    else -> state.scale.targetValue
                }

                // CRITIQUE : On ne lance l'animation que si la cible est différente
                // et que l'animation n'est pas déjà en train de viser cette valeur.
                if (state.scale.targetValue != targetScale) {
                    scope.launch {
                        state.scale.animateTo(
                            targetValue = targetScale,
                            // On peut ajouter un tween pour que ce soit plus doux
                            animationSpec = androidx.compose.animation.core.tween(durationMillis = 300)
                        )
                    }
                }
            }
            .pointerInput(globalScale) {
                detectTransformGestures(
                    onGesture = { _, pan, zoom, rotation ->
                        onPointerDown()
                        scope.launch {
                            val correctedPan = pan / globalScale
                            state.offset.snapTo(state.offset.value + correctedPan)
                            state.rotation.snapTo(state.rotation.value + rotation)

                            // On désactive le zoom manuel ici pour laisser
                            // la position gérer le scale, ou on combine les deux.
                        }
                    }
                )
            }
    ) {
        Box(modifier = Modifier.graphicsLayer {
            rotationZ = state.rotation.value
            scaleX = state.scale.value
            scaleY = state.scale.value
        }) {
            content()
        }
    }
}