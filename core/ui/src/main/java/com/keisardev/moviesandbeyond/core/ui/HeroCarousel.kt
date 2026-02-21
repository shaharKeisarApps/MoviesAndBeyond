@file:Suppress("MatchingDeclarationName")

package com.keisardev.moviesandbeyond.core.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.keisardev.moviesandbeyond.core.model.content.ContentItem
import com.keisardev.moviesandbeyond.core.ui.loading.shimmerBrush
import com.keisardev.moviesandbeyond.core.ui.theme.Dimens
import com.keisardev.moviesandbeyond.core.ui.theme.PosterSize
import com.keisardev.moviesandbeyond.core.ui.theme.RatingBadgeSize
import com.keisardev.moviesandbeyond.core.ui.theme.Spacing

private val HeroGradientBrush =
    Brush.verticalGradient(
        colors = listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.8f)),
        startY = 0f,
        endY = Float.POSITIVE_INFINITY,
    )

private val OverlayTextColor = Color.White.copy(alpha = 0.8f)
private val OverlaySubtleTextColor = Color.White.copy(alpha = 0.7f)

/**
 * Data class for hero carousel items. Abstraction layer to avoid direct dependency on domain model.
 */
data class HeroCarouselItem(
    val id: Int,
    val title: String,
    val posterPath: String,
    val backdropPath: String? = null,
    val rating: Double? = null,
    val releaseYear: String? = null,
    val overview: String? = null,
)

/**
 * Hero carousel for featured content at top of feed screens. Uses Material 3
 * HorizontalMultiBrowseCarousel for a modern multi-item browsing experience.
 *
 * @param items Featured carousel items (should have backdrop images)
 * @param onItemClick Callback when a carousel item is clicked with item id
 * @param modifier Modifier for the carousel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaHeroCarousel(
    items: List<HeroCarouselItem>,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return

    val displayItems = remember(items) { items.take(5) }
    val carouselState = rememberCarouselState { displayItems.size }

    HorizontalMultiBrowseCarousel(
        state = carouselState,
        modifier = modifier.fillMaxWidth().height(Dimens.heroMaxHeight),
        preferredItemWidth = 300.dp,
        itemSpacing = Spacing.sm,
        contentPadding = PaddingValues(horizontal = Spacing.screenPadding),
    ) { index ->
        val item = displayItems[index]
        val itemId = item.id
        val onClick = remember(itemId) { { onItemClick(itemId) } }
        HeroCarouselItemContent(
            item = item,
            onItemClick = onClick,
            modifier = Modifier.maskClip(MaterialTheme.shapes.extraLarge),
        )
    }
}

/** Single hero carousel item content with backdrop, gradient overlay, and content info. */
@Suppress("LongMethod")
@Composable
private fun HeroCarouselItemContent(
    item: HeroCarouselItem,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by
        animateFloatAsState(
            targetValue = if (isPressed) 0.98f else 1f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            label = "hero_scale",
        )

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .pointerInput(onItemClick) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onTap = { onItemClick() },
                    )
                }
    ) {
        // Backdrop image
        val backdropUrl = item.backdropPath ?: item.posterPath
        TmdbBackdropImage(
            imageUrl = backdropUrl,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // Gradient overlay
        Box(modifier = Modifier.fillMaxSize().background(HeroGradientBrush))

        // Content overlay
        Column(
            modifier =
                Modifier.align(Alignment.BottomStart)
                    .padding(horizontal = Spacing.screenPadding, vertical = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            // Rating badge
            item.rating?.let { rating ->
                if (rating > 0) {
                    RatingBadge(rating = rating, size = RatingBadgeSize.MEDIUM)
                }
            }

            // Title
            Text(
                text = item.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            // Release date and overview
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                item.releaseYear?.let { year ->
                    Text(
                        text = year,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OverlayTextColor,
                    )
                }

                item.rating?.let { rating ->
                    if (rating > 0) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = RatingColors.Star,
                        )
                        Text(
                            text =
                                String.format(LocalLocale.current.platformLocale, "%.1f", rating),
                            style = MaterialTheme.typography.bodyMedium,
                            color = OverlayTextColor,
                        )
                    }
                }
            }

            // Overview excerpt
            item.overview?.let { overview ->
                if (overview.isNotBlank()) {
                    Spacer(modifier = Modifier.height(Spacing.xxs))
                    Text(
                        text = overview,
                        style = MaterialTheme.typography.bodySmall,
                        color = OverlaySubtleTextColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/**
 * Poster-style carousel for feed content sections. Uses Material 3 HorizontalMultiBrowseCarousel
 * with poster-card items for a consistent browsing experience.
 *
 * Shows up to 15 items; users tap "See All" for the full list.
 *
 * @param items Content items to display in the carousel
 * @param onItemClick Callback with item ID string when a poster is tapped
 * @param posterSize Size variant for poster cards
 * @param modifier Modifier for the carousel
 * @param showRatings Whether to show rating badges on cards
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPosterCarousel(
    items: List<ContentItem>,
    onItemClick: (Int) -> Unit,
    posterSize: PosterSize,
    modifier: Modifier = Modifier,
    showRatings: Boolean = false,
) {
    if (items.isEmpty()) return

    val displayItems = remember(items) { items.take(15) }
    val carouselState = rememberCarouselState { displayItems.size }

    HorizontalMultiBrowseCarousel(
        state = carouselState,
        modifier = modifier.fillMaxWidth().height(posterSize.height),
        preferredItemWidth = posterSize.width,
        itemSpacing = Spacing.itemSpacing,
        contentPadding = PaddingValues(horizontal = Spacing.screenPadding),
    ) { index ->
        val item = displayItems[index]
        val itemId = item.id
        val onClick = remember(itemId) { { onItemClick(itemId) } }
        PosterCarouselItem(
            posterPath = item.imagePath,
            rating = if (showRatings) item.rating else null,
            onItemClick = onClick,
            modifier = Modifier.maskClip(MaterialTheme.shapes.large),
        )
    }
}

/** Single poster item inside the carousel. Renders poster image with optional rating badge. */
@Composable
private fun PosterCarouselItem(
    posterPath: String,
    rating: Double?,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by
        animateFloatAsState(
            targetValue = if (isPressed) 0.96f else 1f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            label = "poster_carousel_scale",
        )

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .pointerInput(onItemClick) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onTap = { onItemClick() },
                    )
                }
    ) {
        TmdbListImage(imageUrl = posterPath)

        rating?.let {
            if (it > 0) {
                CompactRatingBadge(
                    rating = it,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                )
            }
        }
    }
}

/** Shimmer loading state for poster carousel. Shows animated shimmer cards matching real layout. */
@Composable
fun ShimmerPosterCarousel(posterSize: PosterSize, modifier: Modifier = Modifier) {
    val brush = shimmerBrush()

    LazyRow(
        contentPadding = PaddingValues(horizontal = Spacing.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(Spacing.itemSpacing),
        userScrollEnabled = false,
        modifier = modifier.fillMaxWidth(),
    ) {
        items(5) {
            Box(
                modifier =
                    Modifier.size(width = posterSize.width, height = posterSize.height)
                        .clip(MaterialTheme.shapes.large)
                        .background(brush)
            )
        }
    }
}

/**
 * Shimmer loading state for hero carousel. Shows animated shimmer card matching hero dimensions.
 */
@Composable
fun ShimmerHeroCarousel(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(Dimens.heroMaxHeight)
                .padding(horizontal = Spacing.screenPadding)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(brush)
    )
}

// region Previews

private val previewItems =
    listOf(
        HeroCarouselItem(
            id = 1,
            title = "Dune: Part Two",
            posterPath = "/poster1.jpg",
            backdropPath = "/backdrop1.jpg",
            rating = 8.5,
            releaseYear = "2024",
            overview = "Paul Atreides unites with Chani and the Fremen while seeking revenge.",
        ),
        HeroCarouselItem(
            id = 2,
            title = "Oppenheimer",
            posterPath = "/poster2.jpg",
            backdropPath = "/backdrop2.jpg",
            rating = 8.3,
            releaseYear = "2023",
            overview = "The story of American scientist J. Robert Oppenheimer.",
        ),
        HeroCarouselItem(
            id = 3,
            title = "The Batman",
            posterPath = "/poster3.jpg",
            backdropPath = "/backdrop3.jpg",
            rating = 7.8,
            releaseYear = "2022",
            overview = "Batman investigates when a serial killer targets Gotham's elite.",
        ),
        HeroCarouselItem(
            id = 4,
            title = "Avatar: The Way of Water",
            posterPath = "/poster4.jpg",
            backdropPath = "/backdrop4.jpg",
            rating = 7.6,
            releaseYear = "2022",
            overview = "Jake Sully lives with his newfound family on Pandora.",
        ),
        HeroCarouselItem(
            id = 5,
            title = "Top Gun: Maverick",
            posterPath = "/poster5.jpg",
            backdropPath = "/backdrop5.jpg",
            rating = 8.2,
            releaseYear = "2022",
            overview = "Maverick is still pushing the envelope as a top naval aviator.",
        ),
    )

@Preview(showBackground = true)
@Composable
private fun MediaHeroCarouselPreview() {
    MaterialTheme { MediaHeroCarousel(items = previewItems, onItemClick = {}) }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MediaHeroCarouselDarkPreview() {
    MaterialTheme { MediaHeroCarousel(items = previewItems, onItemClick = {}) }
}

@Preview(showBackground = true)
@Composable
private fun ShimmerHeroCarouselPreview() {
    MaterialTheme { ShimmerHeroCarousel() }
}

// endregion
