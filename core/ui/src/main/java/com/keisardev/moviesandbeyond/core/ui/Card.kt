package com.keisardev.moviesandbeyond.core.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.keisardev.moviesandbeyond.core.ui.theme.Dimens
import com.keisardev.moviesandbeyond.core.ui.theme.PosterSize
import com.keisardev.moviesandbeyond.core.ui.theme.RatingBadgeSize
import com.keisardev.moviesandbeyond.core.ui.theme.Spacing
import java.util.Locale

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
 * @param size Poster size variant (SMALL, MEDIUM, LARGE)
 * @param rating Optional rating to display as badge overlay
 * @param sharedElementKey Optional shared element key for transitions
 * @param onItemClick Callback when the card is clicked
 */
@Suppress("LongMethod", "LongParameterList")
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MediaItemCard(
    posterPath: String,
    modifier: Modifier = Modifier,
    size: PosterSize = PosterSize.MEDIUM,
    rating: Double? = null,
    sharedElementKey: MediaSharedElementKey? = null,
    onItemClick: () -> Unit = {}
) {
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
            targetValue = if (isPressed) 2.dp else 4.dp,
            animationSpec = spring(),
            label = "card_elevation")

    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    val useSharedElements =
        sharedElementKey != null && sharedTransitionScope != null && animatedVisibilityScope != null

    val cardModifier =
        modifier
            .size(width = size.width, height = size.height)
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

    val cardShape = MaterialTheme.shapes.medium

    if (useSharedElements && sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            ElevatedCard(
                shape = cardShape,
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation),
                modifier =
                    cardModifier.sharedBounds(
                        sharedContentState =
                            rememberSharedContentState(
                                key =
                                    sharedElementKey!!.copy(elementType = SharedElementType.Card)),
                        animatedVisibilityScope = animatedVisibilityScope)) {
                    Box {
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

                        // Rating badge overlay
                        rating?.let {
                            if (it > 0) {
                                CompactRatingBadge(
                                    rating = it,
                                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp))
                            }
                        }
                    }
                }
        }
    } else {
        ElevatedCard(
            shape = cardShape,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation),
            modifier = cardModifier) {
                Box {
                    TmdbListImage(imageUrl = posterPath)

                    rating?.let {
                        if (it > 0) {
                            CompactRatingBadge(
                                rating = it,
                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp))
                        }
                    }
                }
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
    size: PosterSize = PosterSize.MEDIUM,
    rating: Double? = null,
    onItemClick: () -> Unit = {}
) {
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
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier =
            modifier
                .size(width = size.width, height = size.height)
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
            Box {
                TmdbListImage(imageUrl = posterPath)

                rating?.let {
                    if (it > 0) {
                        CompactRatingBadge(
                            rating = it, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp))
                    }
                }
            }
        }
}

/**
 * Wide backdrop card for trending/featured sections. Uses 16:9 backdrop images with gradient
 * overlay and info.
 *
 * @param backdropPath The TMDB backdrop image path
 * @param title Movie/TV show title
 * @param modifier Modifier for the card
 * @param year Release year (optional)
 * @param rating Rating score (optional)
 * @param genres List of genre names (optional)
 * @param onItemClick Callback when card is clicked
 */
@Suppress("LongParameterList", "LongMethod")
@Composable
fun MediaBackdropCard(
    backdropPath: String,
    title: String,
    modifier: Modifier = Modifier,
    year: String? = null,
    rating: Double? = null,
    genres: List<String>? = null,
    onItemClick: () -> Unit = {}
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by
        animateFloatAsState(
            targetValue = if (isPressed) 0.98f else 1f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium),
            label = "backdrop_scale")

    Card(
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier =
            modifier
                .fillMaxWidth()
                .height(Dimens.backdropCardHeight)
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
            Box(modifier = Modifier.fillMaxSize()) {
                // Backdrop image
                TmdbBackdropImage(
                    imageUrl = backdropPath,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop)

                // Gradient overlay
                Box(
                    modifier =
                        Modifier.fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors =
                                        listOf(
                                            Color.Black.copy(alpha = 0.8f),
                                            Color.Black.copy(alpha = 0.4f),
                                            Color.Transparent),
                                    startX = 0f,
                                    endX = 800f)))

                // Content overlay
                Column(
                    modifier =
                        Modifier.fillMaxSize()
                            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xxs, Alignment.Bottom)) {
                        // Title
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis)

                        // Year and rating row
                        if (year != null || rating != null) {
                            val metaText = buildString {
                                year?.let { append(it) }
                                if (year != null && rating != null) append(" • ")
                                rating?.let {
                                    append(String.format(Locale.getDefault(), "%.1f", it))
                                }
                            }
                            Text(
                                text = metaText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f))
                        }

                        // Genre chips (limited to 2-3)
                        genres?.take(3)?.let { limitedGenres ->
                            if (limitedGenres.isNotEmpty()) {
                                GenreChipRow(genres = limitedGenres)
                            }
                        }
                    }

                // Rating badge in top right
                rating?.let {
                    if (it > 0) {
                        RatingBadge(
                            rating = it,
                            size = RatingBadgeSize.SMALL,
                            modifier = Modifier.align(Alignment.TopEnd).padding(Spacing.xs))
                    }
                }
            }
        }
}

/**
 * Person card for cast/crew display. Shows circular avatar image with name and role below.
 *
 * @param imagePath TMDB profile image path
 * @param name Person's name
 * @param modifier Modifier for the card
 * @param role Character name or role (optional)
 * @param onItemClick Callback when card is clicked
 */
@Suppress("LongMethod")
@Composable
fun PersonCard(
    imagePath: String?,
    name: String,
    modifier: Modifier = Modifier,
    role: String? = null,
    onItemClick: () -> Unit = {}
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by
        animateFloatAsState(
            targetValue = if (isPressed) 0.95f else 1f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium),
            label = "person_scale")

    Column(
        modifier =
            modifier
                .width(Dimens.personCardWidth)
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
                },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
            // Circular avatar
            if (imagePath != null) {
                TmdbProfileImage(
                    imageUrl = imagePath,
                    modifier = Modifier.size(Dimens.personAvatarSize).clip(CircleShape))
            } else {
                // Placeholder for missing image
                Box(
                    modifier =
                        Modifier.size(Dimens.personAvatarSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center) {
                        Text(
                            text = name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
            }

            // Name
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center)

            // Role
            role?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center)
            }
        }
}

// region Previews

@Preview(showBackground = true)
@Composable
private fun MediaItemCardPreview() {
    MaterialTheme { MediaItemCard(posterPath = "/poster.jpg", size = PosterSize.MEDIUM) }
}

@Preview(showBackground = true)
@Composable
private fun MediaItemCardWithRatingPreview() {
    MaterialTheme {
        MediaItemCard(posterPath = "/poster.jpg", size = PosterSize.MEDIUM, rating = 8.5)
    }
}

@Preview(showBackground = true)
@Composable
private fun SimpleMediaItemCardPreview() {
    MaterialTheme { SimpleMediaItemCard(posterPath = "/poster.jpg", size = PosterSize.SMALL) }
}

@Preview(showBackground = true)
@Composable
private fun MediaBackdropCardPreview() {
    MaterialTheme {
        MediaBackdropCard(
            backdropPath = "/backdrop.jpg",
            title = "The Dark Knight",
            year = "2008",
            rating = 9.0,
            genres = listOf("Action", "Crime", "Drama"))
    }
}

@Preview(showBackground = true)
@Composable
private fun PersonCardPreview() {
    MaterialTheme {
        PersonCard(imagePath = "/profile.jpg", name = "Christian Bale", role = "Bruce Wayne")
    }
}

@Preview(showBackground = true)
@Composable
private fun PersonCardNoImagePreview() {
    MaterialTheme { PersonCard(imagePath = null, name = "Christian Bale", role = "Bruce Wayne") }
}

// endregion
