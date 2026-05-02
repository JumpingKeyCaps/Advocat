package com.lebaillyapp.advocat.prototype.draggeur

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lebaillyapp.advocat.R
import com.lebaillyapp.advocat.prototype.draggeur.draggableDocument.DraggableDocument4
import com.lebaillyapp.advocat.prototype.draggeur.draggableDocument.DraggableDocument5
import com.lebaillyapp.advocat.prototype.draggeur.draggableDocument.DraggableDocument6
import com.lebaillyapp.advocat.prototype.draggeur.draggableDocument.DraggableDocument7
import com.lebaillyapp.advocat.prototype.draggeur.draggableDocument.DraggableDocument8

@Composable
fun PlaygroundLocked(modifier: Modifier = Modifier) {
    val playgroundState = remember { PlaygroundState() }
    val scope = rememberCoroutineScope()




    // Liste simple des ressources générées
    val evidenceImages = remember {
        listOf(
            R.drawable.vh_1,
            R.drawable.vh_2,
            R.drawable.vh_3,
            R.drawable.vh_4,
            R.drawable.vh_5,
            R.drawable.vh_6,
            R.drawable.vh_7,
            R.drawable.vh_8
        )
    }





    LaunchedEffect(Unit) {
        if (playgroundState.documents.isEmpty()) {
            playgroundState.addDocument(offset = Offset(350f, 1000f))
            playgroundState.addDocument(offset = Offset(350f, 1000f), rotation = 3f)
            playgroundState.addDocument(offset = Offset(350f, 1000f), rotation = -4f)
            playgroundState.addDocument(offset = Offset(350f, 1000f), rotation = 1f)
            playgroundState.addDocument(offset = Offset(350f, 1000f), rotation = -3f)
            playgroundState.addDocument(offset = Offset(350f, 1000f), rotation = 2f)
            playgroundState.addDocument(offset = Offset(350f, 1000f), rotation = 4f)
            playgroundState.addDocument(offset = Offset(350f, 1000f), rotation = -5f)

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
        playgroundState.documents.forEachIndexed { index, docState ->
            DraggableDocument8(
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
                    Image(
                        painter = painterResource(id = evidenceImages[index % evidenceImages.size]),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}