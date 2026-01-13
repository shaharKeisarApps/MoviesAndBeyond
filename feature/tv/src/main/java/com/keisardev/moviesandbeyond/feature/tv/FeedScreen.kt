package com.keisardev.moviesandbeyond.feature.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.keisardev.moviesandbeyond.core.model.MediaType
import com.keisardev.moviesandbeyond.core.model.content.ContentItem
import com.keisardev.moviesandbeyond.core.model.content.TvShowListCategory
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
import kotlinx.coroutines.launch

@Composable
fun FeedRoute(
    navigateToDetails: (String) -> Unit,
    navigateToItems: (String) -> Unit,
    viewModel: TvShowsViewModel
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
        appendItems = viewModel::appendItems,
        onItemClick = navigateToDetails,
        onSeeAllClick = navigateToItems,
        onErrorShown = viewModel::onErrorShown)
}

@Suppress("LongMethod")
@Composable
internal fun FeedScreen(
    airingTodayTvShows: ContentUiState,
    onAirTvShows: ContentUiState,
    topRatedTvShows: ContentUiState,
    popularTvShows: ContentUiState,
    errorMessage: String?,
    appendItems: (TvShowListCategory) -> Unit,
    onItemClick: (String) -> Unit,
    onSeeAllClick: (String) -> Unit,
    onErrorShown: () -> Unit
) {
    val snackbarState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    errorMessage?.let {
        scope.launch { snackbarState.showSnackbar(it) }
        onErrorShown()
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarState) }) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = Spacing.feedBottomPadding),
            modifier = Modifier.fillMaxWidth().padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionSpacing)) {
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
                                        overview = item.overview)
                                }
                            }
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            ContentSectionHeader(
                                sectionName = stringResource(id = R.string.popular),
                                onSeeAllClick = null,
                                modifier = Modifier.padding(horizontal = Spacing.screenPadding))
                            MediaHeroCarousel(
                                items = heroItems,
                                onItemClick = { itemId -> onItemClick("$itemId,${MediaType.TV}") })
                        }
                    }
                }

                // Airing Today section - Large cards for prominence
                item(key = "airing_today") {
                    ContentSection(
                        content = airingTodayTvShows,
                        sectionName = stringResource(id = R.string.airing_today),
                        posterSize = PosterSize.LARGE,
                        appendItems = appendItems,
                        onItemClick = onItemClick,
                        onSeeAllClick = onSeeAllClick)
                }

                // On Air section - Medium cards
                item(key = "on_air") {
                    ContentSection(
                        content = onAirTvShows,
                        sectionName = stringResource(id = R.string.on_air),
                        posterSize = PosterSize.MEDIUM,
                        appendItems = appendItems,
                        onItemClick = onItemClick,
                        onSeeAllClick = onSeeAllClick)
                }

                // Top Rated section - Medium cards with ratings
                item(key = "top_rated") {
                    ContentSection(
                        content = topRatedTvShows,
                        sectionName = stringResource(id = R.string.top_rated),
                        posterSize = PosterSize.MEDIUM,
                        showRatings = true,
                        appendItems = appendItems,
                        onItemClick = onItemClick,
                        onSeeAllClick = onSeeAllClick)
                }

                // Popular section - Small cards
                item(key = "popular") {
                    ContentSection(
                        content = popularTvShows,
                        sectionName = stringResource(id = R.string.popular),
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
    appendItems: (TvShowListCategory) -> Unit,
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
                    remember(item.id) { { onItemClick("${item.id},${MediaType.TV}") } }
                val sharedElementKey =
                    remember(item.id) {
                        MediaSharedElementKey(
                            mediaId = item.id.toLong(),
                            mediaType = SharedMediaType.TvShow,
                            origin = SharedElementOrigin.TV_FEED,
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
        ContentItem(1, "/poster1.jpg", "Breaking Bad", "/backdrop1.jpg", 9.5, "2008-01-20"),
        ContentItem(2, "/poster2.jpg", "Game of Thrones", "/backdrop2.jpg", 8.4, "2011-04-17"),
        ContentItem(3, "/poster3.jpg", "The Last of Us", "/backdrop3.jpg", 8.8, "2023-01-15"),
        ContentItem(4, "/poster4.jpg", "Stranger Things", "/backdrop4.jpg", 8.7, "2016-07-15"),
        ContentItem(5, "/poster5.jpg", "The Mandalorian", "/backdrop5.jpg", 8.5, "2019-11-12"))

private fun previewContentUiState(category: TvShowListCategory) =
    ContentUiState(
        items = previewItems, isLoading = false, endReached = false, page = 1, category = category)

@Preview(showBackground = true)
@Composable
private fun TvFeedScreenPreview() {
    FeedScreen(
        airingTodayTvShows = previewContentUiState(TvShowListCategory.AIRING_TODAY),
        onAirTvShows = previewContentUiState(TvShowListCategory.ON_THE_AIR),
        topRatedTvShows = previewContentUiState(TvShowListCategory.TOP_RATED),
        popularTvShows = previewContentUiState(TvShowListCategory.POPULAR),
        errorMessage = null,
        appendItems = {},
        onItemClick = {},
        onSeeAllClick = {},
        onErrorShown = {})
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
        appendItems = {},
        onItemClick = {},
        onSeeAllClick = {},
        onErrorShown = {})
}

// endregion
