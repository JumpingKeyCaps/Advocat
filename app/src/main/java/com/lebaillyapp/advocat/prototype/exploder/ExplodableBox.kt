package com.lebaillyapp.advocat.prototype.exploder

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.launch

@Composable
fun ExplodableBox(
    modifier: Modifier = Modifier,
    shaderResId: Int,
    globalTimer: Int = 3000,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val resources = LocalResources.current
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var contentMeasuredSize by remember { mutableStateOf(IntSize.Zero) }

    val shaderSource = remember(shaderResId) {
        resources.openRawResource(shaderResId).use { it.bufferedReader().readText() }
    }
    val runtimeShader = remember(shaderSource) { RuntimeShader(shaderSource) }

    var explosionCenter by remember { mutableStateOf(Offset.Zero) }

    // Notre Master Progress : 0 (fixe) -> 1 (fin de l'explosion)
    val progress = remember { Animatable(0f) }

    // On utilise un état simple pour savoir si on affiche l'original ou le shader actif
    var isExploding by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        explosionCenter = offset
                        val released = try { tryAwaitRelease(); true } catch (c: Exception) { false }

                        if (released) {
                            scope.launch {
                                // 1. On active le mode explosion dans le shader
                                isExploding = true

                                // 2. On lance le chrono de 0 à 1 (Linear car le shader gère l'easing)
                                progress.snapTo(0f)
                                progress.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(globalTimer, easing = LinearEasing)
                                )

                                // 3. Une fois fini, on reset
                                isExploding = false
                                progress.snapTo(0f)
                            }
                        }
                    }
                )
            }
            .graphicsLayer {
                if (containerSize.width > 0 && containerSize.height > 0) {
                    runtimeShader.setFloatUniform("resolution", containerSize.width.toFloat(), containerSize.height.toFloat())
                    runtimeShader.setFloatUniform("explosionCenter", explosionCenter.x, explosionCenter.y)
                    runtimeShader.setFloatUniform("explosionProgress", progress.value)

                    // On envoie 2.0 si ça explose, 0.0 sinon (pour coller à l' interactionPhase < 1.5)
                    runtimeShader.setFloatUniform("interactionPhase", if (isExploding) 2f else 0f)

                    runtimeShader.setFloatUniform("contentSize",
                        contentMeasuredSize.width.toFloat(),
                        contentMeasuredSize.height.toFloat()
                    )

                    renderEffect = RenderEffect.createRuntimeShaderEffect(runtimeShader, "composable")
                        .asComposeRenderEffect()
                }
            },
        contentAlignment = Alignment.Center

    ) {
        Box(modifier = Modifier.onSizeChanged { contentMeasuredSize = it }) {
            content()
        }
    }
}