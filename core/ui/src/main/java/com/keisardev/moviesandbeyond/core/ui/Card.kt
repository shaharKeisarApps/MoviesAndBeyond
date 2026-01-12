package com.keisardev.moviesandbeyond.core.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * Card component for displaying movie/TV show posters in lists. Uses [TmdbListImage] which is
 * optimized for scrolling performance.
 *
 * Supports shared element transitions when [sharedElementKey] is provided and shared element scopes
 * are available. Falls back gracefully when scopes are not available.
 *
 * Features expressive press feedback animation with scale and elevation changes.
 *
 * @param posterPath The TMDB image path for the poster
 * @param modifier Modifier for the card
 * @param sharedElementKey Optional shared element key for transitions. When provided along with
 *   available scopes, enables shared element transitions.
 * @param onItemClick Callback when the card is clicked
 */
@Suppress("LongMethod") // Compose UI component with animation state - naturally longer
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MediaItemCard(
    posterPath: String,
    modifier: Modifier = Modifier,
    sharedElementKey: MediaSharedElementKey? = null,
    onItemClick: () -> Unit = {}
) {
    // Expressive press feedback state
    var isPressed by remember { mutableStateOf(false) }

    val scale by
        animateFloatAsState(
            targetValue = if (isPressed) 0.96f else 1f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium),
            label = "card_scale")

    val elevation by
        animateDpAsState(
            targetValue = if (isPressed) 4.dp else 10.dp,
            animationSpec = spring(),
            label = "card_elevation")

    // Get shared element scopes from CompositionLocals
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    // Determine if shared elements should be used
    val useSharedElements =
        sharedElementKey != null && sharedTransitionScope != null && animatedVisibilityScope != null

    val cardModifier =
        modifier
            .size(width = 120.dp, height = 160.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onItemClick() })
            }

    if (useSharedElements && sharedTransitionScope != null && animatedVisibilityScope != null) {
        // Use shared element transitions
        with(sharedTransitionScope) {
            ElevatedCard(
                shape = RoundedCornerShape(6.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation),
                modifier =
                    cardModifier.sharedBounds(
                        sharedContentState =
                            rememberSharedContentState(
                                key =
                                    sharedElementKey!!.copy(elementType = SharedElementType.Card)),
                        animatedVisibilityScope = animatedVisibilityScope)) {
                    TmdbListImage(
                        imageUrl = posterPath,
                        modifier =
                            Modifier.sharedElement(
                                sharedContentState =
                                    rememberSharedContentState(
                                        key =
                                            sharedElementKey.copy(
                                                elementType = SharedElementType.Image)),
                                animatedVisibilityScope = animatedVisibilityScope))
                }
        }
    } else {
        // Fallback without shared elements
        ElevatedCard(
            shape = RoundedCornerShape(6.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation),
            modifier = cardModifier) {
                TmdbListImage(imageUrl = posterPath)
            }
    }
}

/**
 * Simple media item card without shared element support. For use in places where shared elements
 * are not needed (e.g., recommendations section, person profile images).
 *
 * Features expressive press feedback animation with scale for consistent UX.
 */
@Composable
fun SimpleMediaItemCard(
    posterPath: String,
    modifier: Modifier = Modifier,
    onItemClick: () -> Unit = {}
) {
    // Expressive press feedback state
    var isPressed by remember { mutableStateOf(false) }

    val scale by
        animateFloatAsState(
            targetValue = if (isPressed) 0.96f else 1f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium),
            label = "simple_card_scale")

    Card(
        shape = RoundedCornerShape(6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        modifier =
            modifier
                .size(width = 120.dp, height = 160.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onTap = { onItemClick() })
                }) {
            TmdbListImage(imageUrl = posterPath)
        }
}
