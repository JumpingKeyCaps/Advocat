package com.lebaillyapp.advocat.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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

@Composable
fun LayoutControlPanel(
    scale: Float,
    onScaleChange: (Float) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(16.dp)
            .width(280.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A101A).copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("LAYOUT & SCALE", color = Color.Magenta, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Global UI Scale: ${String.format("%.2f", scale)}", color = Color.LightGray, fontSize = 12.sp)
            Slider(
                value = scale,
                onValueChange = onScaleChange,
                valueRange = 0.1f..2f, // On peut descendre très bas pour faire un effet "écran dans l'écran"
                colors = SliderDefaults.colors(thumbColor = Color.Magenta, activeTrackColor = Color.Magenta)
            )

            Text(
                "Note: Réduis le scale pour mieux voir l'effet de courbure du CRT sur les bords.",
                color = Color.Gray,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}