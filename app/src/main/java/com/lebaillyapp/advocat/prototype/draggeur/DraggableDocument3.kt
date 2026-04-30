package com.lebaillyapp.advocat.prototype.draggeur

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * # DraggableDocument3
 *
 * Composable permettant de rendre un document draggable, rotatif et scalable
 * sur la surface virtuelle du playground.
 *
 * ## Problème résolu vs DraggableDocument2
 *
 * Dans `DraggableDocument2`, deux `Box` imbriqués étaient utilisés :
 * - **Box extérieur** : portait le `pointerInput` et le drag
 * - **Box intérieur** : portait le `graphicsLayer` avec scale + rotation
 *
 * Cette architecture créait un **bug de hit-testing** : le `graphicsLayer` étant
 * une transformation purement GPU (draw pass), le système de layout de Compose
 * ne la prend jamais en compte pour calculer la zone de touch. Résultat : quand
 * la feuille était scalée à 1.4f, ses bords visuels dépassaient la zone de touch
 * réelle — les coins et bords de la feuille zoomée n'étaient plus interactifs.
 *
 * ## Solution adoptée : Box unique + compensation mathématique de rotation
 *
 * On fusionne tout sur un **seul Box** qui porte à la fois le `pointerInput` et
 * le `graphicsLayer` (scale + rotation). La zone de touch suit désormais
 * exactement le rendu visuel.
 *
 * **Contre-effet à compenser :** quand la rotation est appliquée sur le même
 * élément que le drag, les axes X/Y de l'espace local tournent avec lui. Un pan
 * vers la droite après 90° de rotation devient un pan vers le bas dans l'espace
 * monde. Pour corriger ça, on **contre-rotate le vecteur de pan** par l'angle
 * courant de la feuille avant de l'appliquer à l'offset :
 *
 * ```
 * val angleRad = Math.toRadians(state.rotation.value.toDouble())
 * correctedX  =  pan.x * cos(θ) - pan.y * sin(θ)
 * correctedY  = pan.x * sin(θ) + pan.y * cos(θ)
 * ```
 *
 * Cette transformation ramène le vecteur de pan dans l'espace monde quel que
 * soit l'angle de rotation de la feuille.
 *
 * ## Paramètres
 *
 * @param state          L'état physique et logique du document ([DocumentState]).
 * @param globalScale    Le facteur de zoom global de la caméra du playground,
 *                       utilisé pour corriger la magnitude du pan.
 * @param onPointerDown  Callback déclenché au premier contact, typiquement utilisé
 *                       pour appeler `bringToFront` sur le [PlaygroundState].
 * @param minSize        Scale minimal atteint quand la feuille est en haut de l'écran.
 *                       Défaut : `0.7f`.
 * @param maxSize        Scale maximal atteint quand la feuille est en bas de l'écran.
 *                       Défaut : `1.4f`.
 * @param content        Le contenu Composable à afficher dans la feuille.
 */
@Composable
fun DraggableDocument3(
    state: DocumentState,
    globalScale: Float,
    onPointerDown: () -> Unit,
    minSize: Float = 0.7f,
    maxSize: Float = 1.4f,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()

    // Taille du composable dans le layout (taille non-scalée, telle que déclarée au layout pass)
    var docSize by remember { mutableStateOf(IntSize.Zero) }

    // Taille du parent (la surface du playground) pour calculer les seuils d'autoscale
    var parentSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .zIndex(state.zIndex)

            // --- MESURE DE LA TAILLE LAYOUT ---
            // On capture la taille du composable et de son parent dans l'espace layout.
            // Ces valeurs sont utilisées pour le calcul de l'autoscale progressif selon Y.
            // Note : ces tailles sont NON-scalées — c'est intentionnel, car l'autoscale
            // est basé sur la position de l'offset, pas sur la taille visuelle.
            .onGloballyPositioned { coordinates ->
                docSize = coordinates.size
                parentSize = coordinates.parentLayoutCoordinates?.size ?: IntSize.Zero
            }

            // --- POSITIONNEMENT ---
            // L'offset est appliqué via Modifier.offset() AVANT le graphicsLayer.
            // Cela positionne le composable dans l'espace monde du playground.
            .offset {
                IntOffset(
                    x = state.offset.value.x.roundToInt(),
                    y = state.offset.value.y.roundToInt()
                )
            }

            // --- TRANSFORMATION VISUELLE ET HIT-TESTING ---
            // Scale ET rotation sur le même graphicsLayer que le pointerInput.
            // Désormais, la zone de touch suit le scale visuel car le graphicsLayer
            // est sur le composable racine lui-même.
            // TransformOrigin.Center garantit que le pivot de scale et de rotation
            // est toujours le centre géométrique de la feuille — comportement naturel
            // pour un document qu'on manipule à deux doigts.
            .graphicsLayer {
                scaleX = state.scale.value
                scaleY = state.scale.value
                rotationZ = state.rotation.value
                transformOrigin = TransformOrigin.Center
            }

            // --- GESTION DES GESTES ---
            // On utilise awaitEachGesture (low-level) plutôt que detectTransformGestures
            // pour garder un contrôle total sur le cycle de vie du geste et pouvoir
            // brancher la logique de magnétisme au relâchement plus tard.
            .pointerInput(globalScale) {
                awaitEachGesture {

                    // Attente du premier contact : on notifie le parent pour le z-ordering
                    awaitFirstDown()
                    onPointerDown()

                    // Boucle de traitement des events tant qu'au moins un doigt est posé
                    do {
                        val event = awaitPointerEvent()

                        val panChange      = event.calculatePan()
                        val rotationChange = event.calculateRotation()
                        val zoomChange     = event.calculateZoom()

                        if (panChange != Offset.Zero || rotationChange != 0f || zoomChange != 1f) {
                            scope.launch {

                                // ----------------------------------------------------------------
                                // 1. COMPENSATION MATHÉMATIQUE DE LA ROTATION SUR LE PAN
                                //
                                // Problème : rotation et drag sont sur le même Box. Les axes X/Y
                                // locaux de ce Box ont tourné avec la feuille. Un pan horizontal
                                // de l'utilisateur n'est donc plus horizontal dans l'espace monde.
                                //
                                // Solution : on contre-rotate le vecteur de pan par l'angle actuel
                                // de la feuille pour le ramener dans l'espace monde (référentiel
                                // du playground), puis on l'applique à l'offset.
                                //
                                // Formule de rotation inverse 2D :
                                //   correctedX =  pan.x * cos(θ) - pan.y * sin(θ)
                                //   correctedY =  pan.x * sin(θ) + pan.y * cos(θ)
                                // ----------------------------------------------------------------
                                val angleRad = Math.toRadians(state.rotation.value.toDouble())
                                val cosA = cos(angleRad).toFloat()
                                val sinA = sin(angleRad).toFloat()

                                val correctedPan = Offset(
                                    x = panChange.x * cosA - panChange.y * sinA,
                                    y = panChange.x * sinA + panChange.y * cosA
                                )

                                // Application de la translation corrigée,
                                // divisée par globalScale pour compenser le zoom caméra
                                state.offset.snapTo(
                                    state.offset.value + (correctedPan / globalScale)
                                )

                                // ----------------------------------------------------------------
                                // 2. ROTATION
                                //
                                // Le delta de rotation est appliqué directement , pas besoin
                                // de compensation ici car calculateRotation() retourne toujours
                                // un delta dans l'espace monde (angle entre les deux doigts).
                                // ----------------------------------------------------------------
                                state.rotation.snapTo(state.rotation.value + rotationChange)

                                // ----------------------------------------------------------------
                                // 3. AUTOSCALE PROGRESSIF SELON LA POSITION Y
                                //
                                // Plus la feuille monte vers le haut de l'écran (Y petit),
                                // plus elle rétrécit vers minSize : effet de perspective/profondeur.
                                // Plus elle descend (Y grand), plus elle grossit vers maxSize.
                                //
                                // Le scale manuel (pinch via zoomChange) est intentionnellement
                                // ignoré ici , on se base uniquement sur la position Y.
                                // Si un pinch est détecté, il ne surcharge pas l'autoscale.
                                //
                                // threshold : la hauteur Y à partir de laquelle le scale est maximal.
                                // progress  : ratio entre 0f (en haut) et 1f (au seuil ou en dessous).
                                // ----------------------------------------------------------------
                                val screenH = parentSize.height.toFloat()
                                if (screenH > 0f) {
                                    val threshold = screenH * 0.45f
                                    val progress = (state.offset.value.y / threshold).coerceIn(0f, 1f)
                                    val progressiveScale = minSize + (maxSize - minSize) * progress
                                    state.scale.snapTo(progressiveScale)
                                }
                            }

                            // On consomme tous les pointer events pour éviter
                            // qu'ils remontent aux composables parents
                            event.changes.forEach { it.consume() }
                        }

                    } while (event.changes.any { it.pressed })

                    // ----------------------------------------------------------------
                    // 4. RELÂCHEMENT — POINT D'ANCRAGE FUTUR DU MAGNÉTISME
                    //
                    // C'est ici que sera branchée la logique de magnétisme :
                    // - Calculer la bounding box réelle de la feuille après scale
                    // - Vérifier le ratio de surface hors écran sur chaque bord
                    // - Si ratio > 20% sur un bord → animateTo vers la position corrigée
                    //   avec une marge de 5dp
                    //
                    // L'inertie (fling) sera également branchée ici : on capturera
                    // la vélocité au moment du pointerUp et on lancera un animateTo
                    // avec une DecayAnimationSpec. Le magnétisme sera alors déclenché
                    // en callback de fin de cet animateTo.
                    // ----------------------------------------------------------------
                }
            }
    ) {
        content()
    }
}

