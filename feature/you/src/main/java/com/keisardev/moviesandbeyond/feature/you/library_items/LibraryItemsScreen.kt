package com.keisardev.moviesandbeyond.feature.you.library_items

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.keisardev.moviesandbeyond.core.model.library.LibraryItem
import com.keisardev.moviesandbeyond.core.model.library.LibraryItemType
import com.keisardev.moviesandbeyond.core.ui.LazyVerticalContentGrid
import com.keisardev.moviesandbeyond.core.ui.MediaItemCard
import com.keisardev.moviesandbeyond.core.ui.MediaSharedElementKey
import com.keisardev.moviesandbeyond.core.ui.MediaType as SharedMediaType
import com.keisardev.moviesandbeyond.core.ui.SharedElementOrigin
import com.keisardev.moviesandbeyond.core.ui.SharedElementType
import com.keisardev.moviesandbeyond.core.ui.TopAppBarWithBackButton
import com.keisardev.moviesandbeyond.core.ui.theme.Dimens
import com.keisardev.moviesandbeyond.core.ui.theme.Spacing
import com.keisardev.moviesandbeyond.feature.you.R
import kotlinx.coroutines.launch

@Composable
fun LibraryItemsRoute(
    onBackClick: () -> Unit,
    navigateToDetails: (String) -> Unit,
    viewModel: LibraryItemsViewModel = hiltViewModel()
) {
    val movieItems by viewModel.movieItems.collectAsStateWithLifecycle()
    val tvItems by viewModel.tvItems.collectAsStateWithLifecycle()
    val libraryItemType by viewModel.libraryItemType.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    LibraryItemsScreen(
        movieItems = movieItems,
        tvItems = tvItems,
        libraryItemType = libraryItemType,
        errorMessage = errorMessage,
        onDeleteItem = viewModel::deleteItem,
        onBackClick = onBackClick,
        onItemClick = navigateToDetails,
        onErrorShown = viewModel::onErrorShown)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun LibraryItemsScreen(
    movieItems: List<LibraryItem>,
    tvItems: List<LibraryItem>,
    libraryItemType: LibraryItemType?,
    errorMessage: String?,
    onDeleteItem: (LibraryItem) -> Unit,
    onItemClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onErrorShown: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    errorMessage?.let {
        scope.launch { snackbarHostState.showSnackbar(it) }
        onErrorShown()
    }

    val libraryItemTitle =
        when (libraryItemType) {
            LibraryItemType.FAVORITE -> stringResource(id = R.string.favorites)
            LibraryItemType.WATCHLIST -> stringResource(id = R.string.watchlist)
            else -> null
        }

    Scaffold(
        topBar = {
            TopAppBarWithBackButton(
                title = { libraryItemTitle?.let { Text(text = it) } }, onBackClick = onBackClick)
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                val libraryMediaTabs = LibraryMediaType.entries
                val pagerState = rememberPagerState(pageCount = { libraryMediaTabs.size })

                val selectedTabIndex by
                    remember(pagerState.currentPage) { mutableIntStateOf(pagerState.currentPage) }

                TabRow(selectedTabIndex = selectedTabIndex, modifier = Modifier.fillMaxWidth()) {
                    libraryMediaTabs.forEachIndexed { index, mediaTypeTab ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            text = { Text(text = stringResource(id = mediaTypeTab.displayName)) })
                    }
                }

                Spacer(Modifier.height(Spacing.xs))

                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    Column(Modifier.fillMaxSize()) {
                        when (libraryMediaTabs[page]) {
                            LibraryMediaType.MOVIE -> {
                                LibraryContent(
                                    content = movieItems,
                                    libraryItemType = libraryItemType,
                                    onItemClick = onItemClick,
                                    onDeleteClick = onDeleteItem)
                            }

                            LibraryMediaType.TV -> {
                                LibraryContent(
                                    content = tvItems,
                                    libraryItemType = libraryItemType,
                                    onItemClick = onItemClick,
                                    onDeleteClick = onDeleteItem)
                            }
                        }
                    }
                }
            }
        }
}

@Composable
private fun LibraryContent(
    content: List<LibraryItem>,
    libraryItemType: LibraryItemType?,
    onItemClick: (String) -> Unit,
    onDeleteClick: (LibraryItem) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        if (content.isEmpty()) {
            // Empty state with icon and message
            Column(
                modifier = Modifier.align(Alignment.Center).padding(Spacing.screenPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Icon(
                        imageVector =
                            if (libraryItemType == LibraryItemType.FAVORITE) {
                                Icons.Rounded.Favorite
                            } else {
                                Icons.Rounded.Bookmark
                            },
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.iconSizeLarge),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = stringResource(id = R.string.no_items),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = stringResource(id = R.string.no_items_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center)
                }
        } else {
            LazyVerticalContentGrid(
                pagingEnabled = false,
                contentPadding =
                    PaddingValues(horizontal = Spacing.screenPadding, vertical = Spacing.sm)) {
                    items(items = content, key = { it.id }) { item ->
                        // Convert string media type to SharedMediaType for shared elements
                        val sharedMediaType =
                            when (item.mediaType.lowercase()) {
                                "movie" -> SharedMediaType.Movie
                                "tv" -> SharedMediaType.TvShow
                                else -> null
                            }
                        LibraryItemCard(
                            posterPath = item.imagePath,
                            sharedElementKey =
                                sharedMediaType?.let {
                                    MediaSharedElementKey(
                                        mediaId = item.id.toLong(),
                                        mediaType = it,
                                        origin = SharedElementOrigin.LIBRARY,
                                        elementType = SharedElementType.Image)
                                },
                            onItemClick = {
                                onItemClick("${item.id},${item.mediaType.uppercase()}")
                            },
                            onDeleteClick = { onDeleteClick(item) })
                    }
                }
        }
    }
}

/** Library item card with delete button overlay. */
@Composable
private fun LibraryItemCard(
    posterPath: String,
    sharedElementKey: MediaSharedElementKey? = null,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Box {
        MediaItemCard(
            posterPath = posterPath, sharedElementKey = sharedElementKey, onItemClick = onItemClick)

        // Delete button with modern styling
        IconButton(
            onClick = onDeleteClick,
            colors =
                IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                    contentColor = MaterialTheme.colorScheme.onErrorContainer),
            modifier =
                Modifier.align(Alignment.TopEnd)
                    .padding(Spacing.xxs)
                    .size(32.dp)
                    .clip(CircleShape)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.delete),
                    modifier = Modifier.size(Dimens.iconSizeSmall))
            }
    }
}
