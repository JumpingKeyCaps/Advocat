package com.lebaillyapp.advocat.prototype.draggeur

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import java.util.UUID

/**
 * Représente l'état physique et logique d'un document sur le playground.
 */
class DocumentState(
    val id: UUID = UUID.randomUUID(),
    initialOffset: Offset = Offset.Zero,
    initialRotation: Float = 0f
) {
    // Position x, y avec support pour les animations d'inertie
    val offset = Animatable(initialOffset, Offset.VectorConverter)

    // Rotation en degrés avec support pour l'inertie angulaire
    val rotation = Animatable(initialRotation)

    // Échelle pour les effets de feedback (ex: grossissement au toucher)
    val scale = Animatable(1f)

    // État d'interaction pour savoir si le document est actuellement saisi
    var isDragging by mutableStateOf(false)

    // Z-Index géré par le manager pour l'empilement
    var zIndex by mutableStateOf(0f)
}