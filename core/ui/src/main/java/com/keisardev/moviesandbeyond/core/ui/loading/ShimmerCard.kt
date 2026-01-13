package com.keisardev.moviesandbeyond.core.ui.loading

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.keisardev.moviesandbeyond.core.ui.theme.Dimens
import com.keisardev.moviesandbeyond.core.ui.theme.PosterSize
import com.keisardev.moviesandbeyond.core.ui.theme.Spacing

/**
 * Creates an animated shimmer brush effect for loading placeholders. Uses graphicsLayer for optimal
 * 60fps performance.
 */
@Composable
fun shimmerBrush(): Brush {
    val shimmerColors =
        listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.surfaceVariant)

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = 1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart),
            label = "shimmerTranslate")

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim))
}

/**
 * Single shimmer card placeholder that matches the dimensions of MediaItemCard. Use as a loading
 * state replacement for individual content cards.
 *
 * @param modifier Modifier to apply to the card
 * @param size Poster size variant (SMALL, MEDIUM, LARGE)
 */
@Composable
fun ShimmerCard(modifier: Modifier = Modifier, size: PosterSize = PosterSize.MEDIUM) {
    val brush = shimmerBrush()

    Box(
        modifier =
            modifier
                .size(width = size.width, height = size.height)
                .graphicsLayer { clip = true }
                .clip(RoundedCornerShape(12.dp))
                .background(brush))
}

/**
 * Single shimmer card with custom dimensions.
 *
 * @param modifier Modifier to apply to the card
 * @param width Card width
 * @param height Card height
 */
@Composable
fun ShimmerCard(
    modifier: Modifier = Modifier,
    width: Dp = Dimens.cardWidth,
    height: Dp = Dimens.cardHeight
) {
    val brush = shimmerBrush()

    Box(
        modifier =
            modifier
                .width(width)
                .height(height)
                .graphicsLayer { clip = true }
                .clip(RoundedCornerShape(12.dp))
                .background(brush))
}

/**
 * Row of shimmer cards for horizontal lists. Shows multiple shimmer placeholders with proper
 * spacing.
 *
 * @param cardCount Number of shimmer cards to display (default: 4)
 * @param size Poster size variant for each card
 * @param contentPadding Padding around the row content
 * @param modifier Modifier to apply to the row
 */
@Composable
fun ShimmerRow(
    cardCount: Int = 4,
    size: PosterSize = PosterSize.MEDIUM,
    contentPadding: PaddingValues = PaddingValues(horizontal = Spacing.screenPadding),
    modifier: Modifier = Modifier
) {
    LazyRow(
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(Spacing.itemSpacing),
        userScrollEnabled = false,
        modifier = modifier) {
            items(cardCount) { ShimmerCard(size = size) }
        }
}

/**
 * Full-width shimmer card for grid loading states.
 *
 * @param modifier Modifier to apply to the card
 */
@Composable
fun ShimmerGridCard(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(Dimens.cardHeight)
                .graphicsLayer { clip = true }
                .clip(RoundedCornerShape(12.dp))
                .background(brush))
}

/**
 * Shimmer card sized for lazy row content sections. Fills the available height of the parent row.
 *
 * @param modifier Modifier to apply to the card
 * @param size Poster size variant
 */
@Composable
fun ShimmerRowCard(modifier: Modifier = Modifier, size: PosterSize = PosterSize.MEDIUM) {
    val brush = shimmerBrush()

    Box(
        modifier =
            modifier
                .width(size.width)
                .fillMaxHeight()
                .graphicsLayer { clip = true }
                .clip(RoundedCornerShape(12.dp))
                .background(brush))
}

/**
 * Shimmer backdrop card for loading states in trending/featured sections.
 *
 * @param modifier Modifier to apply to the card
 */
@Composable
fun ShimmerBackdropCard(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(Dimens.backdropCardHeight)
                .graphicsLayer { clip = true }
                .clip(RoundedCornerShape(16.dp))
                .background(brush))
}

/**
 * Shimmer person card for cast/crew loading states.
 *
 * @param modifier Modifier to apply to the card
 */
@Composable
fun ShimmerPersonCard(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()

    Box(
        modifier =
            modifier
                .size(Dimens.personAvatarSize)
                .graphicsLayer { clip = true }
                .clip(RoundedCornerShape(50))
                .background(brush))
}

// region Previews

@Preview(showBackground = true)
@Composable
private fun ShimmerCardPreview() {
    MaterialTheme { ShimmerCard(size = PosterSize.MEDIUM) }
}

@Preview(showBackground = true)
@Composable
private fun ShimmerRowPreview() {
    MaterialTheme { ShimmerRow(cardCount = 3, size = PosterSize.SMALL) }
}

@Preview(showBackground = true)
@Composable
private fun ShimmerBackdropCardPreview() {
    MaterialTheme { ShimmerBackdropCard() }
}

@Preview(showBackground = true)
@Composable
private fun ShimmerPersonCardPreview() {
    MaterialTheme { ShimmerPersonCard() }
}

// endregion
