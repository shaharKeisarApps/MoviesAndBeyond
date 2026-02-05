package com.keisardev.moviesandbeyond.feature.details.content

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.keisardev.moviesandbeyond.core.model.MediaType
import com.keisardev.moviesandbeyond.core.model.content.ContentItem
import com.keisardev.moviesandbeyond.core.model.details.people.Cast
import com.keisardev.moviesandbeyond.core.ui.ContentSectionHeader
import com.keisardev.moviesandbeyond.core.ui.GenreChipRow
import com.keisardev.moviesandbeyond.core.ui.LazyRowContentSection
import com.keisardev.moviesandbeyond.core.ui.LibraryActionButton
import com.keisardev.moviesandbeyond.core.ui.RatingBadge
import com.keisardev.moviesandbeyond.core.ui.SimpleMediaItemCard
import com.keisardev.moviesandbeyond.core.ui.TmdbBackdropImage
import com.keisardev.moviesandbeyond.core.ui.TmdbProfileImage
import com.keisardev.moviesandbeyond.core.ui.noRippleClickable
import com.keisardev.moviesandbeyond.core.ui.theme.Dimens
import com.keisardev.moviesandbeyond.core.ui.theme.RatingBadgeSize
import com.keisardev.moviesandbeyond.core.ui.theme.Spacing
import com.keisardev.moviesandbeyond.feature.details.OverviewSection
import com.keisardev.moviesandbeyond.feature.details.R
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

// M3 Premium Hero Dimensions - Cinematic immersive experience
private val backdropExpandedHeight = Dimens.detailBackdropExpanded
private val collapsedHeight = Dimens.detailBackdropCollapsed
private val heightToCollapse = backdropExpandedHeight - collapsedHeight

@OptIn(FlowPreview::class, ExperimentalSharedTransitionApi::class)
@Composable
internal fun MediaDetailsContent(
    backdropPath: String,
    voteCount: Int,
    name: String,
    rating: Double,
    releaseYear: Int,
    runtime: String,
    tagline: String,
    genres: List<String>,
    overview: String,
    cast: List<Cast>,
    recommendations: List<ContentItem>,
    isFavorite: Boolean,
    isAddedToWatchList: Boolean,
    onFavoriteClick: () -> Unit,
    onWatchlistClick: () -> Unit,
    onSeeAllCastClick: () -> Unit,
    onCastClick: (String) -> Unit,
    onRecommendationClick: (String) -> Unit,
    onBackdropCollapse: (Boolean) -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val heightToCollapsePx = with(LocalDensity.current) { heightToCollapse.toPx() }

    // persist collapse offset between different Details screen
    var savedCollapseOffsetPx by rememberSaveable { mutableFloatStateOf(0f) }

    val nestedScrollConnection =
        remember(heightToCollapsePx) { ExitOnlyCollapseNestedConnection(heightToCollapsePx) }

    LaunchedEffect(Unit) {
        // set value of savedCollapseOffsetPx when returning from different Details screen
        nestedScrollConnection.collapseOffsetPx = savedCollapseOffsetPx

        // whenever backdrop collapses or expands, save collapse offset
        snapshotFlow { nestedScrollConnection.collapseOffsetPx }
            .debounce(500L)
            .collect { offset -> savedCollapseOffsetPx = offset }
    }

    val backdropHeight =
        with(LocalDensity.current) {
            (backdropExpandedHeight.toPx() + nestedScrollConnection.collapseOffsetPx).toDp()
        }
    val isBackdropCollapsed by
        remember(backdropHeight) { derivedStateOf { backdropHeight == collapsedHeight } }
    LaunchedEffect(isBackdropCollapsed) { onBackdropCollapse(isBackdropCollapsed) }

    val scrollValue = 1 - ((backdropExpandedHeight - backdropHeight) / heightToCollapse)

    Column(modifier = Modifier.fillMaxWidth().nestedScroll(nestedScrollConnection)) {
        BackdropImageSection(
            path = backdropPath,
            scrollValue = scrollValue,
            modifier = Modifier.height(backdropHeight))
        LazyColumn(
            contentPadding =
                PaddingValues(horizontal = Spacing.screenPadding, vertical = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionSpacing),
            modifier = Modifier.fillMaxWidth()) {
                item {
                    InfoSection(
                        voteCount = voteCount,
                        name = name,
                        rating = rating,
                        releaseYear = releaseYear,
                        runtime = runtime,
                        tagline = tagline)
                }

                item { GenreSection(genres) }

                item {
                    LibraryActions(
                        isFavorite = isFavorite,
                        isAddedToWatchList = isAddedToWatchList,
                        onFavoriteClick = onFavoriteClick,
                        onWatchlistClick = onWatchlistClick)
                }

                item {
                    TopBilledCast(
                        cast = cast,
                        onCastClick = onCastClick,
                        onSeeAllCastClick = onSeeAllCastClick)
                }

                item { OverviewSection(overview) }

                item { content() }

                item {
                    Recommendations(
                        recommendations = recommendations,
                        onRecommendationClick = onRecommendationClick)
                }
            }
    }
}

private class ExitOnlyCollapseNestedConnection(private val heightToCollapse: Float) :
    NestedScrollConnection {
    var collapseOffsetPx by mutableFloatStateOf(0f)

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val delta = available.y

        // if scrolling down, don't consume anything
        if (delta > 0f) return Offset.Zero

        val previousOffset = collapseOffsetPx
        val newOffset = collapseOffsetPx + delta
        collapseOffsetPx = newOffset.coerceIn(-heightToCollapse, 0f)
        return if (previousOffset != collapseOffsetPx) {
            // We are in the middle of top app bar collapse
            available
        } else {
            Offset.Zero
        }
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        // change height of top app bar when scrolling all the way down and
        // child has finished scrolling
        if (consumed.y >= 0f && available.y > 0f) {
            val prevOffset = collapseOffsetPx
            val newOffset = collapseOffsetPx + available.y
            collapseOffsetPx = newOffset.coerceIn(-heightToCollapse, 0f)
            return Offset(x = 0f, y = (collapseOffsetPx - prevOffset))
        }

        return Offset.Zero
    }
}

@Composable
internal fun DetailItem(fieldName: String, value: String) {
    val text = buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(fieldName) }
        append(value)
    }
    Text(text)
}

/**
 * Premium M3 immersive hero section with parallax scrolling, gradient overlays, and status bar
 * scrim. Creates a cinematic experience for media detail screens following Material Design 3
 * guidelines.
 *
 * Features:
 * - Edge-to-edge rendering with proper window insets handling
 * - Parallax effect for depth perception (moves slower than scroll)
 * - Adaptive gradient overlay for content legibility
 * - Status bar scrim that fades out as backdrop collapses
 * - Smooth Material motion with emphasized easing
 *
 * Note: Shared element transitions are intentionally NOT applied during scrolling to avoid visual
 * glitches, as parallax effects conflict with shared element animations.
 *
 * @param path The TMDB backdrop image URL
 * @param scrollValue Normalized scroll progress (0f = expanded, 1f = collapsed)
 * @param modifier Modifier for the container
 */
@Composable
private fun BackdropImageSection(path: String, scrollValue: Float, modifier: Modifier = Modifier) {
    // M3 Parallax Effect - backdrop translates slower than scroll for depth
    // Uses 50dp max offset for subtle but noticeable depth effect
    val parallaxOffset = (1f - scrollValue) * 50f

    // Status bar scrim opacity - fades out as backdrop collapses
    // Ensures status bar icons remain legible against dynamic image content
    val statusBarScrimAlpha = (1f - scrollValue).coerceIn(0f, 0.5f)

    Box(
        modifier
            .fillMaxWidth()
            // Apply status bar insets to the entire backdrop so it extends under status bar
            .windowInsetsPadding(WindowInsets.statusBars)) {
            // Hero backdrop image with parallax and fade effects
            TmdbBackdropImage(
                imageUrl = path,
                modifier =
                    Modifier.fillMaxSize().graphicsLayer {
                        // Parallax translation for depth
                        translationY = parallaxOffset
                        // Fade out as it collapses (min 30% opacity for smooth transition)
                        alpha = scrollValue.coerceIn(0.3f, 1f)
                    },
                contentScale = ContentScale.Crop)

            // Status bar scrim - dark overlay at top for icon legibility
            // Fades out as backdrop collapses and title appears in app bar
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(48.dp) // Status bar typical height
                        .background(
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        Color.Black.copy(alpha = statusBarScrimAlpha),
                                        Color.Transparent))))

            // Content gradient overlay - ensures text readability over dynamic images
            // Uses M3 color roles for seamless transition to background
            Box(
                modifier =
                    Modifier.fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        Color.Transparent,
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                                        MaterialTheme.colorScheme.background),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY)))
        }
}

/**
 * Premium M3 info section with enhanced visual hierarchy and Material motion.
 *
 * Features:
 * - Prominent headline typography with proper weight (M3 Display/Headline scale)
 * - Color-coded rating badge using M3 semantic colors
 * - Meta information with proper contrast using onSurfaceVariant
 * - Tagline with italic emphasis for quotes
 * - Centered alignment for balanced composition
 * - Proper spacing using M3 spacing tokens
 *
 * Follows M3 guidelines for emphasis hierarchy: Title > Rating > Meta > Tagline
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InfoSection(
    voteCount: Int,
    name: String,
    rating: Double,
    releaseYear: Int,
    tagline: String,
    runtime: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        modifier = Modifier.fillMaxWidth()) {
            // Title - M3 Display style for maximum prominence and visual hierarchy
            Text(
                text = name,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface)

            // Meta info row - year, runtime with M3 onSurfaceVariant for secondary emphasis
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically) {
                    if (releaseYear > 0) {
                        Text(
                            text = "$releaseYear",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (runtime.isNotEmpty() && releaseYear > 0) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (runtime.isNotEmpty()) {
                        Text(
                            text = runtime,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

            // Color-coded rating badge with vote count - uses M3 RatingBadge component
            if (rating > 0) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically) {
                        RatingBadge(rating = rating, size = RatingBadgeSize.MEDIUM)
                        if (voteCount > 0) {
                            Text(
                                text = "($voteCount votes)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
            }

            // Tagline - italic style with quotes for distinction
            // Uses onSurfaceVariant for subtle emphasis per M3 guidelines
            if (tagline.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.xxs))
                Text(
                    text = "\"$tagline\"",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
}

/** Genre section using styled chips with flow layout. */
@Composable
private fun GenreSection(genres: List<String>) {
    if (genres.isNotEmpty()) {
        GenreChipRow(genres = genres)
    }
}

/**
 * Premium M3 cast section with circular avatars and proper visual hierarchy.
 *
 * Features:
 * - Horizontal scrolling row with circular profile images
 * - M3 surface variant for placeholder avatars
 * - Primary color accent for "View All" action
 * - Proper spacing using M3 tokens
 * - Avatar initials for missing profile images
 * - Centered text alignment for names and roles
 *
 * Follows M3 guidelines for person representation and list patterns.
 */
@Composable
private fun TopBilledCast(
    cast: List<Cast>,
    onCastClick: (String) -> Unit,
    onSeeAllCastClick: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        modifier = Modifier.fillMaxWidth()) {
            ContentSectionHeader(
                sectionName = stringResource(id = R.string.top_billed_cast),
                onSeeAllClick = onSeeAllCastClick)
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                modifier =
                    Modifier.horizontalScroll(rememberScrollState())
                        .padding(bottom = Spacing.xxs)) {
                    cast.forEach {
                        CastItemCard(
                            id = it.id,
                            imagePath = it.profilePath,
                            name = it.name,
                            characterName = it.character,
                            onItemClick = onCastClick)
                    }

                    // View all button - M3 tonal surface with primary accent
                    Column(
                        modifier =
                            Modifier.width(Dimens.personCardWidth).noRippleClickable {
                                onSeeAllCastClick()
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                            // Circular container with surface variant background
                            Box(
                                modifier =
                                    Modifier.size(Dimens.personAvatarSize)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "→",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            Spacer(modifier = Modifier.height(Spacing.xxs))
                            // Primary color for action emphasis
                            Text(
                                text = stringResource(id = R.string.view_all),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center)
                        }
                }
        }
}

@Composable
private fun Recommendations(
    recommendations: List<ContentItem>,
    onRecommendationClick: (String) -> Unit
) {
    LazyRowContentSection(
        pagingEnabled = false,
        sectionHeaderContent = {
            Text(
                text = stringResource(id = R.string.recommendations),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold)
        },
        rowContent = {
            if (recommendations.isEmpty()) {
                item {
                    Box(Modifier.fillMaxSize()) {
                        Text(
                            text = stringResource(id = R.string.not_available),
                            modifier = Modifier.align(Alignment.Center))
                    }
                }
            } else {
                items(items = recommendations, key = { it.id }) { item ->
                    // Use SimpleMediaItemCard for recommendations (no shared element transitions
                    // since these navigate to a new detail screen, not back to a list)
                    SimpleMediaItemCard(
                        posterPath = item.imagePath,
                        onItemClick = { onRecommendationClick("${item.id}") })
                }
            }
        },
        modifier = Modifier.padding(bottom = 4.dp))
}

/**
 * Premium M3 cast item card with circular avatar and proper text hierarchy.
 *
 * Features:
 * - Circular profile image following M3 avatar patterns
 * - Fallback initial letter avatar with surface variant background
 * - Two-line text layout: Actor name (medium weight) + Character name (variant)
 * - Proper text truncation with ellipsis
 * - Centered alignment for balanced composition
 * - M3 typography scale: labelMedium for name, labelSmall for role
 *
 * @param id Cast member ID for navigation
 * @param imagePath TMDB profile image URL
 * @param name Actor/actress name (primary text)
 * @param characterName Character name (secondary text)
 * @param onItemClick Navigation callback with person ID
 * @param modifier Optional modifier for customization
 */
@Composable
private fun CastItemCard(
    id: Int,
    imagePath: String,
    name: String,
    characterName: String,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier.width(Dimens.personCardWidth).noRippleClickable {
                onItemClick("$id,${MediaType.PERSON}")
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
            // Circular avatar - M3 standard size for person representation
            if (imagePath.isNotEmpty()) {
                TmdbProfileImage(
                    imageUrl = imagePath,
                    modifier = Modifier.size(Dimens.personAvatarSize).clip(CircleShape))
            } else {
                // M3 fallback avatar with initial letter
                // Uses surface variant for subtle contrast
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

            // Actor name - Primary text with medium weight for emphasis
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center)

            // Character name - Secondary text with variant color for hierarchy
            Text(
                text = characterName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center)
        }
}

/**
 * Premium M3 library action buttons with semantic colors and proper states.
 *
 * Features:
 * - Filled primary button for favorite action (high emphasis)
 * - Tonal variant button for watchlist action (medium emphasis)
 * - State-based icon and color changes
 * - Proper M3 container colors and tonal elevation
 * - Equal weight distribution for balanced layout
 * - Semantic color usage: Red for active favorite, Primary for actions
 *
 * Follows M3 button guidelines for action hierarchy and emphasis levels.
 */
@Composable
private fun LibraryActions(
    isFavorite: Boolean,
    isAddedToWatchList: Boolean,
    onFavoriteClick: () -> Unit,
    onWatchlistClick: () -> Unit
) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .height(IntrinsicSize.Max)
                .padding(top = Spacing.sm, bottom = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            // Favorite button - Filled button (high emphasis action)
            // Uses semantic red color when favorited for clear visual feedback
            LibraryActionButton(
                name =
                    if (isFavorite) {
                        stringResource(id = R.string.remove_from_favorites)
                    } else {
                        stringResource(id = R.string.add_to_favorites)
                    },
                icon = Icons.Rounded.Favorite,
                iconTint =
                    if (isFavorite) {
                        Color.Red // Semantic color for favorited state
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    },
                onClick = onFavoriteClick,
                modifier = Modifier.fillMaxHeight().weight(1f))

            // Watchlist button - Tonal button (medium emphasis action)
            // Uses surface variant container for subtle distinction from favorite
            LibraryActionButton(
                name =
                    if (isAddedToWatchList) {
                        stringResource(id = R.string.remove_from_watchlist)
                    } else {
                        stringResource(id = R.string.add_to_watchlist)
                    },
                icon =
                    if (isAddedToWatchList) {
                        Icons.Rounded.Bookmark // Filled icon when in watchlist
                    } else {
                        Icons.Outlined.BookmarkBorder // Outlined icon when not in watchlist
                    },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                onClick = onWatchlistClick,
                modifier = Modifier.fillMaxHeight().weight(1f))
        }
}
