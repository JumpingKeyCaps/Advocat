package com.lebaillyapp.advocat.prototype.shredder

import android.graphics.RuntimeShader
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.lebaillyapp.advocat.R

@Composable
fun ShredderScreen(modifier: Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val shaderCode = remember { context.resources.openRawResource(R.raw.shredder).bufferedReader().use { it.readText() } }
    val shader = remember(shaderCode) { RuntimeShader(shaderCode) }

    val animProgress = remember { Animatable(0f) }
    val infiniteTransition = rememberInfiniteTransition()
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 100f,
        animationSpec = infiniteRepeatable(tween(100000, easing = LinearEasing))
    )

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            shader.setFloatUniform("uResolution", size.width, size.height)
            shader.setFloatUniform("uProgress", animProgress.value)
            shader.setFloatUniform("uTime", time)
            drawRect(brush = ShaderBrush(shader))
        }

        Button(
            onClick = {
                scope.launch {
                    animProgress.snapTo(0f)
                    animProgress.animateTo(1f, tween(3500, easing = LinearEasing))
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter).padding(60.dp)
        ) {
            Text("BROYER")
        }
    }
}