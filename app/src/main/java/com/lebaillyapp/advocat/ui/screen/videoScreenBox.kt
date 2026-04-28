package com.lebaillyapp.advocat.ui.screen

import androidx.compose.runtime.Composable
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.net.Uri
import android.view.TextureView
import androidx.annotation.OptIn
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.lebaillyapp.advocat.R
import com.lebaillyapp.advocat.model.CrtSettings
import com.lebaillyapp.advocat.model.LcdSettings
import com.lebaillyapp.advocat.model.updateCrtUniforms
import com.lebaillyapp.advocat.model.updateLcdUniforms
import com.lebaillyapp.advocat.ui.component.CrtControlPanel
import com.lebaillyapp.advocat.ui.component.LayoutControlPanel
import com.lebaillyapp.advocat.ui.component.LcdControlPanel
import androidx.core.net.toUri

@OptIn(UnstableApi::class)
@Composable
fun VideoScreenBox(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val resources = LocalResources.current

    // --- POLICES ---
    val minorFontFamily = FontFamily(Font(R.font.special_elite, FontWeight.Normal))
    val masterFontFamily = FontFamily(Font(R.font.bokor, FontWeight.Normal))

    // --- SETUP VIDÉO (ExoPlayer) ---
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val videoUri = "android.resource://${context.packageName}/${R.raw.demovideo_5}".toUri()
            setMediaItem(MediaItem.fromUri(videoUri))
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
            prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

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
                val (video, image, titre, sousTitre, spinner) = createRefs()

                // VIDÉO EN ARRIÈRE-PLAN
                AndroidView(
                    factory = { ctx ->
                        val textureView = TextureView(ctx)
                        exoPlayer.setVideoTextureView(textureView)

                        // On retourne directement la TextureView au lieu d'un PlayerView
                        // car tu n'as pas besoin de l'interface Media3 (play/pause/etc.)
                        textureView
                    },
                    modifier = Modifier
                        .constrainAs(video) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                        .fillMaxSize()
                )




            }
        }

        // --- INTERFACE DE CONTRÔLE (Translucide en bas) ---
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 45.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            IconButton(
                onClick = {
                    showCrtMenu = !showCrtMenu
                    if (showCrtMenu) { showLcdMenu = false; showLayoutMenu = false }
                },
                modifier = Modifier.size(24.dp).alpha(0.2f)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = "CRT", tint = Color.White)
            }

            IconButton(
                onClick = {
                    showLcdMenu = !showLcdMenu
                    if (showLcdMenu) { showCrtMenu = false; showLayoutMenu = false }
                },
                modifier = Modifier.size(24.dp).alpha(0.2f)
            ) {
                Icon(Icons.Default.AccountCircle, contentDescription = "LCD", tint = Color.White)
            }

            IconButton(
                onClick = {
                    showLayoutMenu = !showLayoutMenu
                    if (showLayoutMenu) { showCrtMenu = false; showLcdMenu = false }
                },
                modifier = Modifier.size(24.dp).alpha(0.2f)
            ) {
                Icon(Icons.Default.Face, contentDescription = "Layout", tint = Color.White)
            }
        }

        // --- AFFICHAGE DES PANNEAUX ---
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