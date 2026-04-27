package com.lebaillyapp.advocat.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lebaillyapp.advocat.model.CrtSettings

@Composable
fun CrtControlPanel(
    settings: CrtSettings,
    onSettingsChange: (CrtSettings) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(16.dp)
            .width(320.dp)
            .fillMaxHeight(0.8f), // On limite la hauteur pour ne pas manger tout l'écran
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A).copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Column {
            // Header avec bouton fermer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("CONSOLE CRT v1.0", color = Color.Cyan, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                }
            }

            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { SectionTitle("PHYSIQUE DU TUBE") }
                item {
                    SettingSlider("Fisheye", settings.FISH_EYE_STRENGTH, 0f..2f) {
                        onSettingsChange(settings.copy(FISH_EYE_STRENGTH = it))
                    }
                }
                item {
                    SettingSlider("Zoom", settings.SCREEN_ZOOM, 1f..2.5f) {
                        onSettingsChange(settings.copy(SCREEN_ZOOM = it))
                    }
                }
                item {
                    SettingSlider("Vignette", settings.VIGNETTE_INTENSITY, 0f..2f) {
                        onSettingsChange(settings.copy(VIGNETTE_INTENSITY = it))
                    }
                }
                item {
                    SettingSlider("Scanlines Opacity", settings.SCANLINE_OPACITY, 0f..0.5f) {
                        onSettingsChange(settings.copy(SCANLINE_OPACITY = it))
                    }
                }
                item {
                    SettingSlider("Grid Opacity", settings.GRID_OPACITY, 0f..0.5f) {
                        onSettingsChange(settings.copy(GRID_OPACITY = it))
                    }
                }

                item { SectionTitle("SIGNAL & GLITCH") }
                item {
                    SettingSlider("Signal Shift", settings.SIGNAL_SHIFT, 0f..50f) {
                        onSettingsChange(settings.copy(SIGNAL_SHIFT = it))
                    }
                }
                item {
                    SettingSlider("Jitter Chance", settings.JITTER_CHANCE, 0f..0.1f) {
                        onSettingsChange(settings.copy(JITTER_CHANCE = it))
                    }
                }
                item {
                    SettingSlider("Jitter Intensity", settings.JITTER_INTENSITY, 0f..100f) {
                        onSettingsChange(settings.copy(JITTER_INTENSITY = it))
                    }
                }
                item {
                    SettingSlider("Global Jitter Chance", settings.GLOBAL_JITTER_CHANCE, 0f..0.05f) {
                        onSettingsChange(settings.copy(GLOBAL_JITTER_CHANCE = it))
                    }
                }
                item {
                    SettingSlider("Anaglyph Text", settings.TEXT_ANAGLYPH, 0f..20f) {
                        onSettingsChange(settings.copy(TEXT_ANAGLYPH = it))
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Color.Cyan.copy(alpha = 0.7f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun SettingSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White, fontSize = 11.sp)
            Text(String.format("%.3f", value), color = Color.Cyan, fontSize = 11.sp)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = Color.Cyan,
                activeTrackColor = Color.Cyan,
                inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
            )
        )
    }
}