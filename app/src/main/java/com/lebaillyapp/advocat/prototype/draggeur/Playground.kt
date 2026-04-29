package com.lebaillyapp.advocat.prototype.draggeur

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lebaillyapp.advocat.R
import kotlinx.coroutines.launch

@Composable
fun Playground(modifier: Modifier = Modifier) {
    val playgroundState = remember { PlaygroundState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (playgroundState.documents.isEmpty()) {
            playgroundState.addDocument(offset = Offset(50f, 100f))
            playgroundState.addDocument(offset = Offset(50f, 100f), rotation = 15f)
            playgroundState.addDocument(offset = Offset(50f, 100f), rotation = -10f)
            playgroundState.addDocument(offset = Offset(50f, 100f), rotation = 5f)
            playgroundState.addDocument(offset = Offset(50f, 100f), rotation = -6f)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFEFEF))
            // GESTE 1 : Double tap pour Zoom/Dezoom
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        scope.launch {
                            val targetScale = if (playgroundState.globalScale.value > 1.0f) 1.0f else 1.5f//0.5f
                            playgroundState.globalScale.animateTo(targetScale)
                        }
                    }
                )
            }
            // GESTE 2 : Pan pour bouger le viewport
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, _, _ ->
                    scope.launch {
                        playgroundState.globalOffset.snapTo(playgroundState.globalOffset.value + pan)
                    }
                }
            }
    ) {
        // LE PLAN VIRTUEL
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = playgroundState.globalOffset.value.x
                    translationY = playgroundState.globalOffset.value.y
                    scaleX = playgroundState.globalScale.value
                    scaleY = playgroundState.globalScale.value
                }
        ) {
            playgroundState.documents.forEach { docState ->
                DraggableDocument(
                    state = docState,
                    globalScale = playgroundState.globalScale.value,
                    minSize = 0.5f,
                    maxSize = 1.0f,
                    onPointerDown = { playgroundState.bringToFront(docState) }
                ) {
                    Surface(
                        modifier = Modifier.size(width = 200.dp, height = 320.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = Color.White,
                        shadowElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Sample",
                                fontFamily = FontFamily(Font(R.font.special_elite, FontWeight.Normal)),
                                color = Color.Gray,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }
}