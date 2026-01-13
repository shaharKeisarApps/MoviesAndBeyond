package com.keisardev.moviesandbeyond.feature.details.content

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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

// Enhanced immersive hero dimensions
private val backdropExpandedHeight = Dimens.detailBackdropExpanded
private val collapsedHeight = Dimens.detailBackdropCollapsed
private val heightToCollapse = backdropExpandedHeight - collapsedHeight

@OptIn(FlowPreview::class)
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
 * Immersive backdrop section with parallax scrolling and gradient overlay. Creates a cinematic hero
 * effect for the details screen.
 */
@Composable
private fun BackdropImageSection(path: String, scrollValue: Float, modifier: Modifier = Modifier) {
    // Parallax offset - backdrop moves slower than scroll for depth effect
    val parallaxOffset = (1f - scrollValue) * 50f

    Box(modifier.fillMaxWidth()) {
        TmdbBackdropImage(
            imageUrl = path,
            modifier =
                Modifier.fillMaxSize().graphicsLayer {
                    translationY = parallaxOffset
                    alpha = scrollValue.coerceIn(0.3f, 1f)
                },
            contentScale = ContentScale.Crop)

        // Gradient overlay for better text readability
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

/** Enhanced info section with prominent title, color-coded rating badge, and meta info. */
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
            // Title - prominent headline style
            Text(
                text = name,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis)

            // Meta info row - year, runtime
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

            // Color-coded rating badge
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

            // Tagline - italic style
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

/** Top billed cast section with modern circular avatar cards. */
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

                    // View all button
                    Column(
                        modifier =
                            Modifier.width(Dimens.personCardWidth).noRippleClickable {
                                onSeeAllCastClick()
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
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

/** Modern cast item card with circular avatar and centered text. */
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
            // Circular avatar
            if (imagePath.isNotEmpty()) {
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

            // Character name
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
 * Library action buttons with delightful animations for favorites and watchlist. Features bounce
 * animation, color transitions, and haptic feedback when toggling items.
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
            // Favorite button with bounce animation and haptic feedback
            LibraryActionButton(
                name =
                    if (isFavorite) {
                        stringResource(id = R.string.remove_from_favorites)
                    } else {
                        stringResource(id = R.string.add_to_favorites)
                    },
                icon = Icons.Rounded.Favorite,
                isActive = isFavorite,
                activeIconTint = Color.Red,
                inactiveIconTint = MaterialTheme.colorScheme.onPrimary,
                onClick = onFavoriteClick,
                modifier = Modifier.fillMaxHeight().weight(1f))

            // Watchlist button with bounce animation and haptic feedback
            LibraryActionButton(
                name =
                    if (isAddedToWatchList) {
                        stringResource(id = R.string.remove_from_watchlist)
                    } else {
                        stringResource(id = R.string.add_to_watchlist)
                    },
                icon =
                    if (isAddedToWatchList) {
                        Icons.Rounded.Bookmark
                    } else {
                        Icons.Outlined.BookmarkBorder
                    },
                isActive = isAddedToWatchList,
                activeIconTint = MaterialTheme.colorScheme.primary,
                inactiveIconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                onClick = onWatchlistClick,
                modifier = Modifier.fillMaxHeight().weight(1f))
        }
}
