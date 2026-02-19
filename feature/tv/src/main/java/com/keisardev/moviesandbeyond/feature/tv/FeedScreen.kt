package com.keisardev.moviesandbeyond.feature.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.keisardev.moviesandbeyond.core.model.MediaType
import com.keisardev.moviesandbeyond.core.model.content.ContentItem
import com.keisardev.moviesandbeyond.core.model.content.TvShowListCategory
import com.keisardev.moviesandbeyond.core.ui.ContentSectionHeader
import com.keisardev.moviesandbeyond.core.ui.HeroCarouselItem
import com.keisardev.moviesandbeyond.core.ui.MediaHeroCarousel
import com.keisardev.moviesandbeyond.core.ui.MediaPosterCarousel
import com.keisardev.moviesandbeyond.core.ui.ShimmerPosterCarousel
import com.keisardev.moviesandbeyond.core.ui.theme.PosterSize
import com.keisardev.moviesandbeyond.core.ui.theme.Spacing

@Composable
fun FeedRoute(
    navigateToDetails: (String) -> Unit,
    navigateToItems: (String) -> Unit,
    viewModel: TvShowsViewModel,
) {
    val airingTodayTvShows by viewModel.airingTodayTvShows.collectAsStateWithLifecycle()
    val onAirTvShows by viewModel.onAirTvShows.collectAsStateWithLifecycle()
    val topRatedTvShows by viewModel.topRatedTvShows.collectAsStateWithLifecycle()
    val popularTvShows by viewModel.popularTvShows.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    FeedScreen(
        airingTodayTvShows = airingTodayTvShows,
        onAirTvShows = onAirTvShows,
        topRatedTvShows = topRatedTvShows,
        popularTvShows = popularTvShows,
        errorMessage = errorMessage,
        onItemClick = navigateToDetails,
        onSeeAllClick = navigateToItems,
        onErrorShown = viewModel::onErrorShown,
    )
}

@Suppress("LongMethod")
@Composable
internal fun FeedScreen(
    airingTodayTvShows: ContentUiState,
    onAirTvShows: ContentUiState,
    topRatedTvShows: ContentUiState,
    popularTvShows: ContentUiState,
    errorMessage: String?,
    onItemClick: (String) -> Unit,
    onSeeAllClick: (String) -> Unit,
    onErrorShown: () -> Unit,
) {
    val snackbarState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarState.showSnackbar(message)
            onErrorShown()
        }
    }

    val onCarouselItemClick =
        remember(onItemClick) { { itemId: Int -> onItemClick("$itemId,${MediaType.TV}") } }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = Spacing.feedBottomPadding),
            modifier = Modifier.fillMaxWidth().padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionSpacing),
        ) {
            // Hero carousel with popular TV shows
            item(key = "hero") {
                if (popularTvShows.items.isNotEmpty()) {
                    val heroItems =
                        remember(popularTvShows.items) {
                            popularTvShows.items.take(5).map { item ->
                                HeroCarouselItem(
                                    id = item.id,
                                    title = item.name,
                                    posterPath = item.imagePath,
                                    backdropPath = item.backdropPath,
                                    rating = item.rating,
                                    releaseYear = item.releaseDate?.take(4),
                                    overview = item.overview,
                                )
                            }
                        }
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        ContentSectionHeader(
                            sectionName = stringResource(id = R.string.popular),
                            onSeeAllClick = null,
                            modifier = Modifier.padding(horizontal = Spacing.screenPadding),
                        )
                        MediaHeroCarousel(items = heroItems, onItemClick = onCarouselItemClick)
                    }
                }
            }

            // Airing Today section - Large poster carousel
            item(key = "airing_today") {
                CarouselSection(
                    items = airingTodayTvShows.items,
                    isLoading = airingTodayTvShows.items.isEmpty() && airingTodayTvShows.isLoading,
                    sectionName = stringResource(id = R.string.airing_today),
                    posterSize = PosterSize.LARGE,
                    onItemClick = onCarouselItemClick,
                    onSeeAllClick =
                        remember(onSeeAllClick) {
                            { onSeeAllClick(TvShowListCategory.AIRING_TODAY.name) }
                        },
                )
            }

            // On Air section - Medium poster carousel
            item(key = "on_air") {
                CarouselSection(
                    items = onAirTvShows.items,
                    isLoading = onAirTvShows.items.isEmpty() && onAirTvShows.isLoading,
                    sectionName = stringResource(id = R.string.on_air),
                    posterSize = PosterSize.MEDIUM,
                    onItemClick = onCarouselItemClick,
                    onSeeAllClick =
                        remember(onSeeAllClick) {
                            { onSeeAllClick(TvShowListCategory.ON_THE_AIR.name) }
                        },
                )
            }

            // Top Rated section - Medium poster carousel with ratings
            item(key = "top_rated") {
                CarouselSection(
                    items = topRatedTvShows.items,
                    isLoading = topRatedTvShows.items.isEmpty() && topRatedTvShows.isLoading,
                    sectionName = stringResource(id = R.string.top_rated),
                    posterSize = PosterSize.MEDIUM,
                    showRatings = true,
                    onItemClick = onCarouselItemClick,
                    onSeeAllClick =
                        remember(onSeeAllClick) {
                            { onSeeAllClick(TvShowListCategory.TOP_RATED.name) }
                        },
                )
            }
        }
    }
}

@Composable
private fun CarouselSection(
    items: List<ContentItem>,
    isLoading: Boolean,
    sectionName: String,
    onItemClick: (Int) -> Unit,
    onSeeAllClick: () -> Unit,
    posterSize: PosterSize = PosterSize.MEDIUM,
    showRatings: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.headerSpacing)) {
        ContentSectionHeader(
            sectionName = sectionName,
            onSeeAllClick = onSeeAllClick,
            modifier = Modifier.padding(horizontal = Spacing.screenPadding),
        )

        if (items.isEmpty() && isLoading) {
            ShimmerPosterCarousel(posterSize = posterSize)
        } else {
            MediaPosterCarousel(
                items = items,
                onItemClick = onItemClick,
                posterSize = posterSize,
                showRatings = showRatings,
            )
        }
    }
}

// region Previews

private val previewItems =
    listOf(
        ContentItem(1, "/poster1.jpg", "Breaking Bad", "/backdrop1.jpg", 9.5, "2008-01-20"),
        ContentItem(2, "/poster2.jpg", "Game of Thrones", "/backdrop2.jpg", 8.4, "2011-04-17"),
        ContentItem(3, "/poster3.jpg", "The Last of Us", "/backdrop3.jpg", 8.8, "2023-01-15"),
        ContentItem(4, "/poster4.jpg", "Stranger Things", "/backdrop4.jpg", 8.7, "2016-07-15"),
        ContentItem(5, "/poster5.jpg", "The Mandalorian", "/backdrop5.jpg", 8.5, "2019-11-12"),
    )

private fun previewContentUiState(category: TvShowListCategory) =
    ContentUiState(
        items = previewItems,
        isLoading = false,
        endReached = false,
        page = 1,
        category = category,
    )

@Preview(showBackground = true)
@Composable
private fun TvFeedScreenPreview() {
    FeedScreen(
        airingTodayTvShows = previewContentUiState(TvShowListCategory.AIRING_TODAY),
        onAirTvShows = previewContentUiState(TvShowListCategory.ON_THE_AIR),
        topRatedTvShows = previewContentUiState(TvShowListCategory.TOP_RATED),
        popularTvShows = previewContentUiState(TvShowListCategory.POPULAR),
        errorMessage = null,
        onItemClick = {},
        onSeeAllClick = {},
        onErrorShown = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun TvFeedScreenLoadingPreview() {
    FeedScreen(
        airingTodayTvShows = ContentUiState(TvShowListCategory.AIRING_TODAY),
        onAirTvShows = ContentUiState(TvShowListCategory.ON_THE_AIR),
        topRatedTvShows = ContentUiState(TvShowListCategory.TOP_RATED),
        popularTvShows = ContentUiState(TvShowListCategory.POPULAR),
        errorMessage = null,
        onItemClick = {},
        onSeeAllClick = {},
        onErrorShown = {},
    )
}

// endregion
