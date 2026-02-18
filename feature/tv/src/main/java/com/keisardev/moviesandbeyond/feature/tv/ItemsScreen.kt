package com.keisardev.moviesandbeyond.feature.tv

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.keisardev.moviesandbeyond.core.model.MediaType
import com.keisardev.moviesandbeyond.core.model.content.TvShowListCategory
import com.keisardev.moviesandbeyond.core.ui.LazyVerticalContentGrid
import com.keisardev.moviesandbeyond.core.ui.MediaItemCard
import com.keisardev.moviesandbeyond.core.ui.MediaSharedElementKey
import com.keisardev.moviesandbeyond.core.ui.MediaType as SharedMediaType
import com.keisardev.moviesandbeyond.core.ui.SharedElementOrigin
import com.keisardev.moviesandbeyond.core.ui.SharedElementType
import com.keisardev.moviesandbeyond.core.ui.TopAppBarWithBackButton
import com.keisardev.moviesandbeyond.core.ui.theme.Dimens
import com.keisardev.moviesandbeyond.core.ui.theme.PosterSize
import com.keisardev.moviesandbeyond.core.ui.theme.Spacing

/** Poster aspect ratio (2:3) for consistent grid item sizing */
private const val POSTER_ASPECT_RATIO = 2f / 3f

@Composable
fun ItemsRoute(
    categoryName: String,
    onItemClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: TvShowsViewModel
) {
    val category = enumValueOf<TvShowListCategory>(categoryName)
    val content by
        when (category) {
            TvShowListCategory.AIRING_TODAY ->
                viewModel.airingTodayTvShows.collectAsStateWithLifecycle()
            TvShowListCategory.POPULAR -> viewModel.popularTvShows.collectAsStateWithLifecycle()
            TvShowListCategory.TOP_RATED -> viewModel.topRatedTvShows.collectAsStateWithLifecycle()
            TvShowListCategory.ON_THE_AIR -> viewModel.onAirTvShows.collectAsStateWithLifecycle()
        }
    val categoryDisplayName =
        when (category) {
            TvShowListCategory.AIRING_TODAY -> stringResource(id = R.string.airing_today)
            TvShowListCategory.ON_THE_AIR -> stringResource(id = R.string.on_air)
            TvShowListCategory.TOP_RATED -> stringResource(id = R.string.top_rated)
            TvShowListCategory.POPULAR -> stringResource(id = R.string.popular)
        }
    ItemsScreen(
        content = content,
        categoryDisplayName = categoryDisplayName,
        appendItems = viewModel::appendItems,
        onItemClick = { onItemClick("$it,${MediaType.TV}") },
        onBackClick = onBackClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ItemsScreen(
    content: ContentUiState,
    categoryDisplayName: String,
    appendItems: (TvShowListCategory) -> Unit,
    onItemClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    // Stable callback to avoid recomposition
    val stableAppendItems = remember(content.category) { { appendItems(content.category) } }

    Scaffold(
        topBar = {
            TopAppBarWithBackButton(
                title = { Text(text = categoryDisplayName, fontWeight = FontWeight.SemiBold) },
                onBackClick = onBackClick)
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxWidth().padding(paddingValues)) {
            LazyVerticalContentGrid(
                pagingEnabled = true,
                itemsEmpty = content.items.isEmpty(),
                isLoading = content.isLoading,
                endReached = content.endReached,
                contentPadding =
                    PaddingValues(
                        horizontal = Dimens.gridContentPadding,
                        vertical = Spacing.screenPaddingVertical),
                minCellWidth = PosterSize.MEDIUM.width,
                itemSpacing = Dimens.gridItemSpacing,
                appendItems = stableAppendItems) {
                    items(items = content.items, key = { it.id }, contentType = { "media_item" }) {
                        item ->
                        // Use stable callbacks to prevent unnecessary recomposition
                        val stableItemClick =
                            remember(item.id) { { onItemClick("${item.id},${MediaType.TV}") } }
                        val sharedElementKey =
                            remember(item.id) {
                                MediaSharedElementKey(
                                    mediaId = item.id.toLong(),
                                    mediaType = SharedMediaType.TvShow,
                                    origin = SharedElementOrigin.TV_ITEMS,
                                    elementType = SharedElementType.Image)
                            }
                        // Use aspectRatio for equal sizing that adapts to grid cell width
                        MediaItemCard(
                            posterPath = item.imagePath,
                            sharedElementKey = sharedElementKey,
                            onItemClick = stableItemClick,
                            modifier = Modifier.fillMaxWidth().aspectRatio(POSTER_ASPECT_RATIO))
                    }
                    if (content.isLoading) {
                        item(contentType = "loading") {
                            Box(
                                modifier =
                                    Modifier.fillMaxWidth().aspectRatio(POSTER_ASPECT_RATIO)) {
                                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                                }
                        }
                    }
                }
        }
    }
}
