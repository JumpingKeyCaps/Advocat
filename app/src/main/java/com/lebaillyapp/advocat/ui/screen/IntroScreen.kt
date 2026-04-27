package com.lebaillyapp.advocat.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.lebaillyapp.advocat.R

@Composable
fun IntroScreen(modifier: Modifier) {

    val minorFontFamily = FontFamily(Font(R.font.special_elite, FontWeight.Normal))
    val masterFontFamily = FontFamily(Font(R.font.bokor, FontWeight.Normal))

    ConstraintLayout(
        modifier = modifier.fillMaxSize()
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
            fontSize = 9.sp,
            modifier = Modifier.constrainAs(sousTitre) {
                top.linkTo(titre.bottom, margin = (-15).dp)
                start.linkTo(titre.start)
                end.linkTo(titre.end)
            }
        )
    }
}