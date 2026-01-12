package com.keisardev.moviesandbeyond.feature.movies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.keisardev.moviesandbeyond.core.model.MediaType
import com.keisardev.moviesandbeyond.core.model.content.MovieListCategory
import com.keisardev.moviesandbeyond.core.ui.ContentSectionHeader
import com.keisardev.moviesandbeyond.core.ui.LazyRowContentSection
import com.keisardev.moviesandbeyond.core.ui.MediaItemCard
import com.keisardev.moviesandbeyond.core.ui.MediaSharedElementKey
import com.keisardev.moviesandbeyond.core.ui.MediaType as SharedMediaType
import com.keisardev.moviesandbeyond.core.ui.SharedElementOrigin
import com.keisardev.moviesandbeyond.core.ui.SharedElementType
import com.keisardev.moviesandbeyond.core.ui.theme.Dimens
import com.keisardev.moviesandbeyond.core.ui.theme.Spacing
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()

    errorMessage?.let {
        scope.launch { snackbarState.showSnackbar(it) }
        onErrorShown()
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarState) }) { paddingValues ->
        LazyColumn(
            contentPadding =
                PaddingValues(top = Spacing.feedTopPadding, bottom = Spacing.feedBottomPadding),
            modifier = modifier.fillMaxWidth().padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionSpacing)) {
                item {
                    ContentSection(
                        content = nowPlayingMovies,
                        sectionName = stringResource(id = R.string.now_playing),
                        appendItems = appendItems,
                        onItemClick = onItemClick,
                        onSeeAllClick = onSeeAllClick)
                }
                item {
                    ContentSection(
                        content = popularMovies,
                        sectionName = stringResource(id = R.string.popular),
                        appendItems = appendItems,
                        onItemClick = onItemClick,
                        onSeeAllClick = onSeeAllClick)
                }
                item {
                    ContentSection(
                        content = topRatedMovies,
                        sectionName = stringResource(id = R.string.top_rated),
                        appendItems = appendItems,
                        onItemClick = onItemClick,
                        onSeeAllClick = onSeeAllClick)
                }
                item {
                    ContentSection(
                        content = upcomingMovies,
                        sectionName = stringResource(id = R.string.upcoming),
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
    onSeeAllClick: (String) -> Unit
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
            items(
                items = content.items,
                key = { it.id },
                contentType = { "media_item" } // Enables Compose slot reuse optimization
                ) { item ->
                    // STABLE: Remembered per item - prevents lambda recreation
                    val stableItemClick =
                        remember(item.id) { { onItemClick("${item.id},${MediaType.MOVIE}") } }
                    // Shared element key for smooth transitions to detail screen
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
                        sharedElementKey = sharedElementKey,
                        onItemClick = stableItemClick)
                }

            if (content.isLoading) {
                item(contentType = "loading") { // Different contentType for loading indicator
                    Box(modifier = Modifier.fillMaxHeight().width(Dimens.loadingIndicatorWidth)) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }
            }
        },
        modifier = Modifier.height(Dimens.cardHeight))
}
