package com.lebaillyapp.advocat.prototype.draggeur

import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.ui.geometry.Offset

/**
 * Objet de configuration pour les propriétés physiques globales.
 */
object PhysicsConstants {
    // Friction pour la translation (plus la valeur est haute, plus ça s'arrête vite)
    val OffsetDecay: DecayAnimationSpec<Offset> = exponentialDecay(
        frictionMultiplier = 1.5f,
        absVelocityThreshold = 0.5f
    )

    // Friction pour la rotation
    val RotationDecay: DecayAnimationSpec<Float> = exponentialDecay(
        frictionMultiplier = 2.0f,
        absVelocityThreshold = 0.1f
    )
}