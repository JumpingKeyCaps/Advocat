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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.lebaillyapp.advocat.model.LcdSettings
import com.lebaillyapp.advocat.model.updateCrtUniforms
import com.lebaillyapp.advocat.model.updateLcdUniforms
import com.lebaillyapp.advocat.ui.component.CrtControlPanel
import com.lebaillyapp.advocat.ui.component.LayoutControlPanel
import com.lebaillyapp.advocat.ui.component.LcdControlPanel

@Composable
fun IntroScreen(modifier: Modifier = Modifier) {
    val resources = LocalResources.current

    // --- POLICES ---
    val minorFontFamily = FontFamily(Font(R.font.special_elite, FontWeight.Normal))
    val masterFontFamily = FontFamily(Font(R.font.bokor, FontWeight.Normal))

    // --- ÉTATS DES RÉGLAGES ---
    var crtSettings by remember { mutableStateOf(CrtSettings()) }
    var lcdSettings by remember { mutableStateOf(LcdSettings()) }
    var uiScale by remember { mutableFloatStateOf(0.67f) }

    var showCrtMenu by remember { mutableStateOf(false) }
    var showLcdMenu by remember { mutableStateOf(false) }
    var showLayoutMenu by remember { mutableStateOf(false) }

    // --- SETUP DES SHADERS ---
    val crtSource = remember { resources.openRawResource(R.raw.crt_lens).use { it.bufferedReader().readText() } }
    val lcdSource = remember { resources.openRawResource(R.raw.retroboy_shader_opti_ds).use { it.bufferedReader().readText() } }

    val crtShader = remember(crtSource) { RuntimeShader(crtSource) }
    val lcdShader = remember(lcdSource) { RuntimeShader(lcdSource) }

    val totalTime by rememberInfiniteTransition(label = "GlobalTime").animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(100000, easing = LinearEasing)),
        label = "TimeFloat"
    )

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {

        // --- COUCHE 1 : LE TUBE CRT (Parent) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    crtShader.setFloatUniform("size", size.width, size.height)
                    crtShader.setFloatUniform("time", totalTime)
                    crtShader.updateCrtUniforms(crtSettings)

                    renderEffect = RenderEffect.createRuntimeShaderEffect(
                        crtShader, "composable"
                    ).asComposeRenderEffect()
                }
        ) {
            // --- COUCHE 2 : LE RENDU LCD RETROBOY (Enfant) ---
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = uiScale
                        scaleY = uiScale
                        lcdShader.setFloatUniform("res", size.width, size.height)
                        lcdShader.setFloatUniform("imgRes", size.width, size.height)
                        lcdShader.updateLcdUniforms(lcdSettings)

                        renderEffect = RenderEffect.createRuntimeShaderEffect(
                            lcdShader, "inputFrame"
                        ).asComposeRenderEffect()
                    }
            ) {

                //BODY
                val (image, titre, sousTitre, spinner) = createRefs()
                val guideLigne35 = createGuidelineFromTop(0.35f)

                Image(
                    painter = painterResource(id = R.drawable.main_logo),
                    contentDescription = "logo app",
                    modifier = Modifier
                        .size(150.dp)
                        .constrainAs(image) {
                            top.linkTo(guideLigne35)
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

                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp) // Taille adaptée au style rétro
                        .constrainAs(spinner) {
                            top.linkTo(sousTitre.bottom, margin = 60.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        },
                    color = Color.White, // Sera transformé par la palette du shader LCD
                    strokeWidth = 3.dp   // Un trait un peu épais pour que les pixels "accrochent" bien
                )


            }
        }

        // --- INTERFACE DE CONTRÔLE (Hors Shaders) ---
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 45.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Bouton Menu CRT
            IconButton(
                onClick = {
                    showCrtMenu = !showCrtMenu
                    if (showCrtMenu) showLcdMenu = false
                },
                modifier = Modifier.size(18.dp).alpha(0.1f)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = "CRT", tint = Color.White)
            }

            // Bouton Menu LCD
            IconButton(
                onClick = {
                    showLcdMenu = !showLcdMenu
                    if (showLcdMenu) showCrtMenu = false
                },
                modifier = Modifier.size(18.dp).alpha(0.1f)
            ) {
                Icon(Icons.Default.AccountCircle, contentDescription = "LCD", tint = Color.White)
            }

            //inner scaling
            IconButton(
                onClick = {
                    showLayoutMenu = !showLayoutMenu
                    if (showLayoutMenu) { showCrtMenu = false; showLcdMenu = false }
                },
                modifier = Modifier.size(18.dp).alpha(0.1f)
            ) {
                Icon(Icons.Default.Face, contentDescription = "Layout", tint = Color.White)
            }

        }

        // Affichage des panneaux
        if (showCrtMenu) {
            CrtControlPanel(
                settings = crtSettings,
                onSettingsChange = { crtSettings = it },
                onClose = { showCrtMenu = false },
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

        if (showLcdMenu) {
            LcdControlPanel(
                settings = lcdSettings,
                onSettingsChange = { lcdSettings = it },
                onClose = { showLcdMenu = false },
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

        if (showLayoutMenu) {
            LayoutControlPanel(
                scale = uiScale,
                onScaleChange = { uiScale = it },
                onClose = { showLayoutMenu = false },
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}