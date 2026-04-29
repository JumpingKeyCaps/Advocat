package com.lebaillyapp.advocat.prototype.draggeur

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import java.util.UUID

/**
 * Manager responsable de la collection de documents et de leur profondeur (Z-Index).
 */
class PlaygroundState {
    val documents = mutableStateListOf<DocumentState>()
    private var topZIndex = 1f

    // État de la "Caméra" globale
    val globalOffset = Animatable(Offset.Zero, Offset.VectorConverter)
    val globalScale = Animatable(1f)

    fun addDocument(offset: Offset = Offset.Zero, rotation: Float = 0f) {
        val newDoc = DocumentState(initialOffset = offset, initialRotation = rotation).apply {
            zIndex = topZIndex++
        }
        documents.add(newDoc)
    }

    fun bringToFront(document: DocumentState) {
        if (document.zIndex < topZIndex - 0.1f) {
            document.zIndex = topZIndex++
        }
    }
}