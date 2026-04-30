package com.lebaillyapp.advocat.prototype.draggeur.draggableDocument

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import com.lebaillyapp.advocat.prototype.draggeur.DocumentState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DraggableDocument(
    state: DocumentState,
    globalScale: Float,
    minSize: Float = 0.5f,
    maxSize: Float = 1.0f,
    onPointerDown: () -> Unit,
    content: @Composable () -> Unit,

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
            .pointerInput(globalScale) {
                detectTransformGestures(
                    onGesture = { _, pan, zoom, rotation ->
                        onPointerDown()
                        scope.launch {
                            // On divise par le scale pour que le mouvement soit 1:1 sous le doigt
                            // même quand le viewport est dézoomé
                            val correctedPan = pan * globalScale

                            state.offset.snapTo(state.offset.value + correctedPan)
                            state.rotation.snapTo(state.rotation.value + rotation)

                            val newScale = (state.scale.value * zoom).coerceIn(minSize, maxSize)
                            state.scale.snapTo(newScale)
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