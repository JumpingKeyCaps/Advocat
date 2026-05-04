package com.lebaillyapp.advocat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lebaillyapp.advocat.model.CrtSettings
import com.lebaillyapp.advocat.prototype.draggeur.Playground
import com.lebaillyapp.advocat.prototype.draggeur.PlaygroundLocked
import com.lebaillyapp.advocat.prototype.exploder.ExplodableScreen
import com.lebaillyapp.advocat.prototype.shredder.ShredderScreen
import com.lebaillyapp.advocat.ui.screen.IntroScreen
import com.lebaillyapp.advocat.ui.screen.ShaderLoveLayout
import com.lebaillyapp.advocat.ui.screen.ShaderizerScreenBox
import com.lebaillyapp.advocat.ui.screen.VideoScreenBox
import com.lebaillyapp.advocat.ui.theme.AdvocatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdvocatTheme {
                // On garde une trace de l'écran affiché (null = le menu)
                var currentScreen by remember { mutableStateOf<ProtoScreen?>(null) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    if (currentScreen == null) {
                        // --- LE MENU DE SÉLECTION ---
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {

                            ProtoScreen.entries.forEach { screen ->
                                Button(
                                    onClick = { currentScreen = screen },
                                    modifier = Modifier.fillMaxWidth(0.8f).padding(8.dp)
                                ) {
                                    Text(screen.label)
                                }
                            }
                        }
                    } else {

                        // Interception du bouton retour natif
                        BackHandler {
                            currentScreen = null
                        }


                        // --- L'ÉCRAN SÉLECTIONNÉ ---
                        Box(modifier = Modifier.fillMaxSize()) {
                            // On injecte le contenu selon le choix
                            when (currentScreen) {
                                ProtoScreen.Intro -> IntroScreen(modifier = Modifier.padding(innerPadding))
                                ProtoScreen.BigPicture -> ShaderizerScreenBox(modifier = Modifier.padding(innerPadding))
                                ProtoScreen.Video -> VideoScreenBox(modifier = Modifier.padding(innerPadding))
                                ProtoScreen.Shredder -> ShredderScreen(modifier = Modifier.padding(innerPadding))
                                ProtoScreen.Playground -> Playground(modifier = Modifier.padding(innerPadding))
                                ProtoScreen.Explosion -> ExplodableScreen(modifier = Modifier.padding(innerPadding))

                                ProtoScreen.PlaygroundLocked -> {
                                    ShaderLoveLayout(
                                        activateCRT = true,
                                        activateLCD = false,
                                        settingsCRT = CrtSettings(
                                            FISH_EYE_STRENGTH = 0.158f,
                                            SCREEN_ZOOM = 1.130f,
                                            VIGNETTE_INTENSITY = 0.119f,
                                            SCANLINE_OPACITY = 0.012f,
                                            TEXT_ANAGLYPH = 0.0f),
                                        content = {
                                            PlaygroundLocked(modifier = Modifier.padding(innerPadding)) }
                                    )
                                }
                                else -> {}
                            }


                        }
                    }
                }
            }
        }
    }
}


enum class ProtoScreen(val label: String) {
    Intro("Intro Logo"),
    BigPicture("Big Picture Shader"),
    Video("Video Content"),
    Shredder("Card Shredder"),
    Playground("Dragger Prototype"),
    PlaygroundLocked("Playground Locked"),
    Explosion("Objection Exploding")
}
