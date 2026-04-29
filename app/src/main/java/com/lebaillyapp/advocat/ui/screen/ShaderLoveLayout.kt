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
import androidx.compose.ui.layout.ContentScale
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
import com.lebaillyapp.advocat.prototype.exploder.ExplodableScreen
import com.lebaillyapp.advocat.ui.component.CrtControlPanel
import com.lebaillyapp.advocat.ui.component.LcdControlPanel

@Composable
fun ShaderLoveLayout(
    modifier: Modifier = Modifier,
    activateCRT: Boolean = true,
    activateLCD: Boolean = true,
    content: @Composable () -> Unit
) {
    val resources = LocalResources.current

    // --- POLICES ---
    val minorFontFamily = FontFamily(Font(R.font.special_elite, FontWeight.Normal))
    val masterFontFamily = FontFamily(Font(R.font.bokor, FontWeight.Normal))

    // --- ÉTATS DES RÉGLAGES ---
    var crtSettings by remember { mutableStateOf(CrtSettings()) }
    var lcdSettings by remember { mutableStateOf(LcdSettings()) }

    var showCrtMenu by remember { mutableStateOf(false) }
    var showLcdMenu by remember { mutableStateOf(false) }

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
                    if(activateCRT){
                        crtShader.setFloatUniform("size", size.width, size.height)
                        crtShader.setFloatUniform("time", totalTime)
                        crtShader.updateCrtUniforms(crtSettings)

                        renderEffect = RenderEffect.createRuntimeShaderEffect(
                            crtShader, "composable"
                        ).asComposeRenderEffect()
                    }
                }
        ) {
            // --- COUCHE 2 : LE RENDU LCD RETROBOY (Enfant) ---
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        if(activateLCD){
                            lcdShader.setFloatUniform("res", size.width, size.height)
                            lcdShader.setFloatUniform("imgRes", size.width, size.height)
                            lcdShader.updateLcdUniforms(lcdSettings)

                            renderEffect = RenderEffect.createRuntimeShaderEffect(
                                lcdShader, "inputFrame"
                            ).asComposeRenderEffect()
                        }
                    }
            ) {
                val (bodytag) = createRefs()
                val guideLigne35 = createGuidelineFromTop(0.0f)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .constrainAs(bodytag) {
                            top.linkTo(guideLigne35)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                ){
                    content()
                }

            }
        }

        // --- INTERFACE DE CONTRÔLE debug  (Hors Shaders) ---
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 45.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Bouton Menu CRT
            if(activateCRT){
                IconButton(
                    onClick = {
                        showCrtMenu = !showCrtMenu
                        if (showCrtMenu) showLcdMenu = false
                    },
                    modifier = Modifier.background(if (showCrtMenu) Color.Cyan.copy(0.4f) else Color.White.copy(0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "CRT", tint = Color.White)
                }
            }

            if(activateLCD){
                // Bouton Menu LCD
                IconButton(
                    onClick = {
                        showLcdMenu = !showLcdMenu
                        if (showLcdMenu) showCrtMenu = false
                    },
                    modifier = Modifier.background(if (showLcdMenu) Color(0xFFD6E86A).copy(0.4f) else Color.White.copy(0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = "LCD", tint = Color.White)
                }
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
    }
}