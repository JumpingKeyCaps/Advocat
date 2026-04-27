package com.lebaillyapp.advocat.ui.screen

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.lebaillyapp.advocat.R
import com.lebaillyapp.advocat.model.CrtSettings
import androidx.compose.ui.platform.LocalResources
import com.lebaillyapp.advocat.model.updateCrtUniforms
import com.lebaillyapp.advocat.ui.component.CrtControlPanel

@Composable
fun IntroScreen(modifier: Modifier) {

    val minorFontFamily = FontFamily(Font(R.font.special_elite, FontWeight.Normal))
    val masterFontFamily = FontFamily(Font(R.font.bokor, FontWeight.Normal))

    val resources = LocalResources.current

    // --- SETUP SHADER GLOBAL (CRT) ---
    val shaderSource = remember {
        resources.openRawResource(R.raw.crt_lens).use { it.bufferedReader().readText() }
    }
    val runtimeShader = remember(shaderSource) { RuntimeShader(shaderSource) }
    val totalTime by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(100000, easing = LinearEasing))
    )
    // États
    var settings by remember { mutableStateOf(CrtSettings()) }
    var showSettings by remember { mutableStateOf(false) }


    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {

        // --- CONTAINER PARENT (Le "Tube" Cathodique) ---
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .graphicsLayer {
                    // On applique le CRT ici : il affectera TOUT le contenu de la Box
                    runtimeShader.setFloatUniform("size", size.width, size.height)
                    runtimeShader.setFloatUniform("time", totalTime)
                    runtimeShader.updateCrtUniforms(settings)

                    renderEffect = RenderEffect.createRuntimeShaderEffect(
                        runtimeShader, "composable"
                    ).asComposeRenderEffect()
                }
        ) {
            ConstraintLayout(
                modifier = Modifier.fillMaxSize()
            ) {
                val (image, titre, sousTitre) = createRefs()
                // Création d'une ligne de guidage horizontale à 40% du haut
                val guideLigne68 = createGuidelineFromTop(0.40f)
                Image(
                    painter = painterResource(id = R.drawable.main_logo),
                    contentDescription = "logo app",
                    modifier = Modifier
                        .size(150.dp)
                        .constrainAs(image) {
                            // On accroche le haut de l'image sur notre ligne de guidage
                            top.linkTo(guideLigne68)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                )
                Text(
                    text = "Advocat!",
                    fontFamily = masterFontFamily,
                    color = Color.White,
                    fontSize = 44.sp,
                    modifier = Modifier.constrainAs(titre) {
                        top.linkTo(image.bottom, margin = (-25).dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                )
                Text(
                    text = "Free by dinner, or your money back.",
                    fontFamily = minorFontFamily,
                    color = Color.White,
                    fontSize = 9.sp,
                    modifier = Modifier.constrainAs(sousTitre) {
                        top.linkTo(titre.bottom, margin = (-15).dp)
                        start.linkTo(titre.start)
                        end.linkTo(titre.end)
                    }
                )
            }
        }

        IconButton(
            onClick = { showSettings = !showSettings },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(45.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
        }

        // COUCHE 3 : Le Panneau (si ouvert)
        if (showSettings) {
            CrtControlPanel(
                settings = settings,
                onSettingsChange = { settings = it },
                onClose = { showSettings = false }, // On ferme le panel ici
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }



}