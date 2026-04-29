package com.lebaillyapp.advocat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.lebaillyapp.advocat.prototype.draggeur.Playground
import com.lebaillyapp.advocat.prototype.draggeur.PlaygroundLocked
import com.lebaillyapp.advocat.prototype.exploder.ExplodableScreen
import com.lebaillyapp.advocat.prototype.shredder.ShredderScreen
import com.lebaillyapp.advocat.ui.screen.ShaderLoveLayout
import com.lebaillyapp.advocat.ui.screen.ShaderizerScreenBox
import com.lebaillyapp.advocat.ui.theme.AdvocatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdvocatTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    //todo - Main intro - logo app + LCD + CRT shader + debug
                  //  IntroScreen(modifier = Modifier.padding(innerPadding))

                    //todo - Big picture - with LCD + CRT shader + debug
                 //   ShaderizerScreenBox(modifier = Modifier.padding(innerPadding))

                    //todo - Video content - with LCD + CRT shader + debug
                   // VideoScreenBox(modifier = Modifier.padding(innerPadding))

                    //todo - Card shredder effect prototype
                  //  ShredderScreen(modifier = Modifier.padding(innerPadding))

                    //todo - Draggeur prototype
                 //   Playground(modifier = Modifier.padding(innerPadding))

                 //   PlaygroundLocked(modifier = Modifier.padding(innerPadding))

                  //todo - Objection exploding prototype - natif without crt or lcd
                //  ExplodableScreen(modifier = Modifier.padding(innerPadding))


                    //todo - Generic shader layout content loader

                    ShaderLoveLayout(
                        activateCRT = true,
                        activateLCD = false,
                        content = {
                            //content to shaderizZ (CRT + LCD)
                        PlaygroundLocked(modifier = Modifier.padding(innerPadding))
                        }
                    )



                }
            }
        }
    }
}
