package com.lebaillyapp.advocat.prototype.draggeur

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lebaillyapp.advocat.R
import com.lebaillyapp.advocat.prototype.draggeur.draggableDocument.DraggableDocument4
import com.lebaillyapp.advocat.prototype.draggeur.draggableDocument.DraggableDocument5
import com.lebaillyapp.advocat.prototype.draggeur.draggableDocument.DraggableDocument6

@Composable
fun PlaygroundLocked(modifier: Modifier = Modifier) {
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

    // LE PLAN VIRTUEL
    Box(
        modifier = Modifier
            .background(Color(0xFFEFEFEF))
            .fillMaxSize()
            .graphicsLayer {
                translationX = playgroundState.globalOffset.value.x
                translationY = playgroundState.globalOffset.value.y
                scaleX = playgroundState.globalScale.value
                scaleY = playgroundState.globalScale.value
            }
    ) {
        playgroundState.documents.forEach { docState ->
            DraggableDocument6(
                state = docState,
                globalScale = playgroundState.globalScale.value,
                onPointerDown = { playgroundState.bringToFront(docState) },
                minSize = 0.7f,
                maxSize = 1.4f
            ) {
                Surface(
                    modifier = Modifier.size(width = 200.dp, height = 320.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    //contenue de la sheet
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // En-tête "Officiel"
                        Text(
                            text = "Sample document",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))

                        Spacer(modifier = Modifier.height(12.dp))

                        // Corps du document (C'est ce texte dense qui va "baver" avec le flou)
                        repeat(8) { index ->
                            Text(
                                text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                                        "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                                fontSize = 7.sp,
                                lineHeight = 10.sp,
                                color = if (index % 5 == 0) Color.DarkGray else Color.Gray,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Petit tampon ou signature en bas
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.End)
                                .background(Color(0xFFFFEBEE), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("APPROVED", fontSize = 6.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}