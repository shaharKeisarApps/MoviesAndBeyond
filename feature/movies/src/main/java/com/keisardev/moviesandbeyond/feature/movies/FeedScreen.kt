package com.keisardev.moviesandbeyond.feature.movies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.keisardev.moviesandbeyond.core.model.MediaType
import com.keisardev.moviesandbeyond.core.model.content.ContentItem
import com.keisardev.moviesandbeyond.core.model.content.MovieListCategory
import com.keisardev.moviesandbeyond.core.ui.ContentSectionHeader
import com.keisardev.moviesandbeyond.core.ui.HeroCarouselItem
import com.keisardev.moviesandbeyond.core.ui.LazyRowContentSection
import com.keisardev.moviesandbeyond.core.ui.MediaHeroCarousel
import com.keisardev.moviesandbeyond.core.ui.MediaItemCard
import com.keisardev.moviesandbeyond.core.ui.MediaSharedElementKey
import com.keisardev.moviesandbeyond.core.ui.MediaType as SharedMediaType
import com.keisardev.moviesandbeyond.core.ui.SharedElementOrigin
import com.keisardev.moviesandbeyond.core.ui.SharedElementType
import com.keisardev.moviesandbeyond.core.ui.theme.Dimens
import com.keisardev.moviesandbeyond.core.ui.theme.PosterSize
import com.keisardev.moviesandbeyond.core.ui.theme.Spacing

@Composable
fun FeedRoute(
    navigateToDetails: (String) -> Unit,
    navigateToItems: (String) -> Unit,
    viewModel: MoviesViewModel,
    modifier: Modifier = Modifier
) {
    val nowPlayingMovies by viewModel.nowPlayingMovies.collectAsStateWithLifecycle()
    val popularMovies by viewModel.popularMovies.collectAsStateWithLifecycle()
    val topRatedMovies by viewModel.topRatedMovies.collectAsStateWithLifecycle()
    val upcomingMovies by viewModel.upcomingMovies.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    FeedScreen(
        nowPlayingMovies = nowPlayingMovies,
        popularMovies = popularMovies,
        topRatedMovies = topRatedMovies,
        upcomingMovies = upcomingMovies,
        errorMessage = errorMessage,
        appendItems = viewModel::appendItems,
        onItemClick = navigateToDetails,
        onSeeAllClick = navigateToItems,
        onErrorShown = viewModel::onErrorShown,
        modifier = modifier)
}

@Suppress("LongMethod")
@Composable
internal fun FeedScreen(
    nowPlayingMovies: ContentUiState,
    popularMovies: ContentUiState,
    topRatedMovies: ContentUiState,
    upcomingMovies: ContentUiState,
    errorMessage: String?,
    appendItems: (MovieListCategory) -> Unit,
    onItemClick: (String) -> Unit,
    onSeeAllClick: (String) -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarState.showSnackbar(message)
            onErrorShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = Spacing.feedBottomPadding),
            modifier = modifier.fillMaxWidth().padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionSpacing)) {
                // Hero carousel with popular movies
                item(key = "hero") {
                    if (popularMovies.items.isNotEmpty()) {
                        val heroItems =
                            remember(popularMovies.items) {
                                popularMovies.items.take(5).map { item ->
                                    HeroCarouselItem(
                                        id = item.id,
                                        title = item.name,
                                        posterPath = item.imagePath,
                                        backdropPath = item.backdropPath,
                                        rating = item.rating,
                                        releaseYear = item.releaseDate?.take(4),
                                        overview = item.overview)
                                }
                            }
                        val onHeroItemClick =
                            remember(onItemClick) {
                                { itemId: Int -> onItemClick("$itemId,${MediaType.MOVIE}") }
                            }
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            ContentSectionHeader(
                                sectionName = stringResource(id = R.string.popular),
                                onSeeAllClick = null,
                                modifier = Modifier.padding(horizontal = Spacing.screenPadding))
                            MediaHeroCarousel(items = heroItems, onItemClick = onHeroItemClick)
                        }
                    }
                }

                // Now Playing section - Large cards for prominence
                item(key = "now_playing") {
                    ContentSection(
                        content = nowPlayingMovies,
                        sectionName = stringResource(id = R.string.now_playing),
                        posterSize = PosterSize.LARGE,
                        appendItems = appendItems,
                        onItemClick = onItemClick,
                        onSeeAllClick = onSeeAllClick)
                }

                // Top Rated section - Medium cards with ratings
                item(key = "top_rated") {
                    ContentSection(
                        content = topRatedMovies,
                        sectionName = stringResource(id = R.string.top_rated),
                        posterSize = PosterSize.MEDIUM,
                        showRatings = true,
                        appendItems = appendItems,
                        onItemClick = onItemClick,
                        onSeeAllClick = onSeeAllClick)
                }

                // Upcoming section - Small cards
                item(key = "upcoming") {
                    ContentSection(
                        content = upcomingMovies,
                        sectionName = stringResource(id = R.string.upcoming),
                        posterSize = PosterSize.SMALL,
                        appendItems = appendItems,
                        onItemClick = onItemClick,
                        onSeeAllClick = onSeeAllClick)
                }
            }
    }
}

@Composable
private fun ContentSection(
    content: ContentUiState,
    sectionName: String,
    appendItems: (MovieListCategory) -> Unit,
    onItemClick: (String) -> Unit,
    onSeeAllClick: (String) -> Unit,
    posterSize: PosterSize = PosterSize.MEDIUM,
    showRatings: Boolean = false
) {
    // STABLE: Only recreated when category changes
    val stableAppendItems = remember(content.category) { { appendItems(content.category) } }
    val stableSeeAllClick = remember(content.category) { { onSeeAllClick(content.category.name) } }

    LazyRowContentSection(
        pagingEnabled = true,
        isLoading = content.isLoading,
        endReached = content.endReached,
        itemsEmpty = content.items.isEmpty(),
        rowContentPadding = PaddingValues(horizontal = Spacing.screenPadding),
        appendItems = stableAppendItems,
        sectionHeaderContent = {
            ContentSectionHeader(
                sectionName = sectionName,
                onSeeAllClick = stableSeeAllClick,
                modifier = Modifier.padding(horizontal = Spacing.screenPadding))
        },
        rowContent = {
            items(items = content.items, key = { it.id }, contentType = { "media_item" }) { item ->
                val stableItemClick =
                    remember(item.id) { { onItemClick("${item.id},${MediaType.MOVIE}") } }
                val sharedElementKey =
                    remember(item.id) {
                        MediaSharedElementKey(
                            mediaId = item.id.toLong(),
                            mediaType = SharedMediaType.Movie,
                            origin = SharedElementOrigin.MOVIES_FEED,
                            elementType = SharedElementType.Image)
                    }
                MediaItemCard(
                    posterPath = item.imagePath,
                    size = posterSize,
                    rating = if (showRatings) item.rating else null,
                    sharedElementKey = sharedElementKey,
                    onItemClick = stableItemClick)
            }

            if (content.isLoading) {
                item(contentType = "loading") {
                    Box(modifier = Modifier.fillMaxHeight().width(Dimens.loadingIndicatorWidth)) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }
            }
        },
        modifier = Modifier.height(posterSize.height))
}

// region Previews

private val previewItems =
    listOf(
        ContentItem(1, "/poster1.jpg", "Dune: Part Two", "/backdrop1.jpg", 8.5, "2024-03-01"),
        ContentItem(2, "/poster2.jpg", "Oppenheimer", "/backdrop2.jpg", 8.3, "2023-07-21"),
        ContentItem(3, "/poster3.jpg", "The Batman", "/backdrop3.jpg", 7.8, "2022-03-04"),
        ContentItem(4, "/poster4.jpg", "Avatar: The Way of Water", "/backdrop4.jpg", 7.6, "2022"),
        ContentItem(5, "/poster5.jpg", "Top Gun: Maverick", "/backdrop5.jpg", 8.2, "2022-05-27"))

private fun previewContentUiState(category: MovieListCategory) =
    ContentUiState(
        items = previewItems, isLoading = false, endReached = false, page = 1, category = category)

@Preview(showBackground = true)
@Composable
private fun FeedScreenPreview() {
    FeedScreen(
        nowPlayingMovies = previewContentUiState(MovieListCategory.NOW_PLAYING),
        popularMovies = previewContentUiState(MovieListCategory.POPULAR),
        topRatedMovies = previewContentUiState(MovieListCategory.TOP_RATED),
        upcomingMovies = previewContentUiState(MovieListCategory.UPCOMING),
        errorMessage = null,
        appendItems = {},
        onItemClick = {},
        onSeeAllClick = {},
        onErrorShown = {})
}

@Preview(showBackground = true)
@Composable
private fun FeedScreenLoadingPreview() {
    FeedScreen(
        nowPlayingMovies = ContentUiState(MovieListCategory.NOW_PLAYING),
        popularMovies = ContentUiState(MovieListCategory.POPULAR),
        topRatedMovies = ContentUiState(MovieListCategory.TOP_RATED),
        upcomingMovies = ContentUiState(MovieListCategory.UPCOMING),
        errorMessage = null,
        appendItems = {},
        onItemClick = {},
        onSeeAllClick = {},
        onErrorShown = {})
}

// endregion
