package com.lebaillyapp.advocat.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lebaillyapp.advocat.model.LcdSettings

@Composable
fun LcdControlPanel(
    settings: LcdSettings,
    onSettingsChange: (LcdSettings) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedToneIndex by remember { mutableIntStateOf(0) }

    Card(
        modifier = modifier
            .padding(16.dp)
            .width(320.dp)
            .fillMaxHeight(0.85f),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1B0F).copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column {
            // Header (inchangé)
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("RETROBOY ENGINE", color = Color(0xFFD6E86A), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, null, tint = Color.White) }
            }

            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { SectionTitle("PALETTE (SÉLECTIONNE UN TON)") }

                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        settings.palette.forEachIndexed { index, color ->
                            Box(
                                modifier = Modifier
                                    .size(45.dp)
                                    .background(color, RoundedCornerShape(4.dp))
                                    .border(
                                        width = if (selectedToneIndex == index) 3.dp else 1.dp,
                                        color = if (selectedToneIndex == index) Color(0xFFD6E86A) else Color.Gray,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable { selectedToneIndex = index }
                            )
                        }
                    }
                }

                // --- RÉGLAGES FINS DE LA COULEUR SÉLECTIONNÉE ---
                item {
                    val activeColor = settings.palette[selectedToneIndex]
                    val currentHue = remember(activeColor) { getHue(activeColor) }
                    val currentSat = remember(activeColor) { getSaturation(activeColor) }
                    val currentVal = remember(activeColor) { getValue(activeColor) }

                    Column(modifier = Modifier.background(Color.Black.copy(0.2f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                        Text("Édition Ton $selectedToneIndex", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(bottom = 4.dp))

                        // TEINTE
                        SettingSlider(label = "Teinte", value = currentHue, range = 0f..360f) { h ->
                            updatePalette(settings, selectedToneIndex, Color.hsv(h, currentSat, currentVal), onSettingsChange)
                        }

                        // SATURATION (0 = Gris, 1 = Couleur vive)
                        SettingSlider(label = "Saturation", value = currentSat, range = 0f..1f) { s ->
                            updatePalette(settings, selectedToneIndex, Color.hsv(currentHue, s, currentVal), onSettingsChange)
                        }

                        // VALEUR (0 = Noir, 1 = Lumineux/Blanc)
                        SettingSlider(label = "Luminosité (Value)", value = currentVal, range = 0f..1f) { v ->
                            updatePalette(settings, selectedToneIndex, Color.hsv(currentHue, currentSat, v), onSettingsChange)
                        }
                    }
                }

                item { SectionTitle("PARAMÈTRES DE PIXELISATION") }
                item {
                    SettingSlider("Taille Pixel", settings.scaleFactor, 1f..14f) {
                        onSettingsChange(settings.copy(scaleFactor = it))
                    }
                }
                // ... (reste des sliders : Dithering, Grid, etc.)
                item {
                    SettingSlider("Dithering (Trame)", settings.ditheringStrength, 0f..1f) {
                        onSettingsChange(settings.copy(ditheringStrength = it))
                    }
                }
                item {
                    SettingSlider("Opacité Grille", settings.gridIntensity, 0f..1f) {
                        onSettingsChange(settings.copy(gridIntensity = it))
                    }
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

private fun getHue(color: Color): Float {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV((color.red * 255).toInt() shl 16 or ((color.green * 255).toInt() shl 8) or (color.blue * 255).toInt(), hsv)
    return hsv[0]
}

private fun getSaturation(color: Color): Float {
    val hsv = FloatArray(3)
    android.graphics.Color.RGBToHSV((color.red * 255).toInt(), (color.green * 255).toInt(), (color.blue * 255).toInt(), hsv)
    return hsv[1]
}

private fun getValue(color: Color): Float {
    val hsv = FloatArray(3)
    android.graphics.Color.RGBToHSV((color.red * 255).toInt(), (color.green * 255).toInt(), (color.blue * 255).toInt(), hsv)
    return hsv[2]
}

// Fonction utilitaire pour mettre à jour la liste proprement
private fun updatePalette(settings: LcdSettings, index: Int, newColor: Color, onSettingsChange: (LcdSettings) -> Unit) {
    val newList = settings.palette.toMutableList()
    newList[index] = newColor
    onSettingsChange(settings.copy(palette = newList))
}