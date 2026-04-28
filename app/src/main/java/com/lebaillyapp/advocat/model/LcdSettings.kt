package com.lebaillyapp.advocat.model

import android.graphics.RuntimeShader
import androidx.compose.ui.graphics.Color

data class LcdSettings(
    val scaleFactor: Float = 1.511f,
    val ditheringStrength: Float = 0.171f,
    val gridIntensity: Float = 0.001f,
    val gridSize: Float = 2.0f,
    val palette: List<Color> = listOf(
        Color(0xFF492A18), Color(0xFF181817), Color(0xFF332612), Color(0xFFE0E0E0)
    )
)

// Extension pour mapper les uniforms du LCD
fun RuntimeShader.updateLcdUniforms(settings: LcdSettings) {
    setFloatUniform("scaleFactor", settings.scaleFactor)
    setFloatUniform("ditheringStrength", settings.ditheringStrength)
    setFloatUniform("gridIntensity", settings.gridIntensity)
    setFloatUniform("gridSize", settings.gridSize)
    settings.palette.forEachIndexed { i, color ->
        setFloatUniform("palette$i", color.red, color.green, color.blue, color.alpha)
    }
}