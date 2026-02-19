package com.keisardev.moviesandbeyond.feature.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.keisardev.moviesandbeyond.core.model.SearchItem
import com.keisardev.moviesandbeyond.core.ui.MediaSharedElementKey
import com.keisardev.moviesandbeyond.core.ui.MediaType as SharedMediaType
import com.keisardev.moviesandbeyond.core.ui.MoviesAndBeyondSearchBar
import com.keisardev.moviesandbeyond.core.ui.SharedElementOrigin
import com.keisardev.moviesandbeyond.core.ui.SharedElementType
import com.keisardev.moviesandbeyond.core.ui.theme.Dimens
import com.keisardev.moviesandbeyond.core.ui.theme.Spacing

@Composable
fun SearchRoute(navigateToDetail: (String) -> Unit, viewModel: SearchViewModel = hiltViewModel()) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchSuggestions by viewModel.searchSuggestions.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    SearchScreen(
        searchQuery = searchQuery,
        errorMessage = errorMessage,
        searchSuggestions = searchSuggestions,
        onSearchQueryChange = viewModel::changeSearchQuery,
        onBack = viewModel::onBack,
        onSearchResultClick = navigateToDetail,
        onErrorShown = viewModel::onErrorShown,
    )
}

@Composable
internal fun SearchScreen(
    searchQuery: String,
    errorMessage: String?,
    searchSuggestions: List<SearchItem>,
    onSearchQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onSearchResultClick: (String) -> Unit,
    onErrorShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onErrorShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            MoviesAndBeyondSearchBar(
                value = searchQuery,
                onQueryChange = { onSearchQueryChange(it) },
            )

            if (searchQuery.isNotEmpty()) {
                BackHandler {
                    onSearchQueryChange("")
                    onBack()
                }

                if (searchSuggestions.isEmpty()) {
                    // Empty search results state
                    SearchEmptyState(query = searchQuery)
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(Dimens.searchGridColumns),
                        contentPadding =
                            PaddingValues(
                                horizontal = Spacing.screenPadding,
                                vertical = Spacing.sm,
                            ),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.itemSpacing),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(items = searchSuggestions, key = { it.id }) { item ->
                            // Convert string media type to SharedMediaType for shared elements
                            val sharedMediaType =
                                when (item.mediaType.lowercase()) {
                                    "movie" -> SharedMediaType.Movie
                                    "tv" -> SharedMediaType.TvShow
                                    "person" -> SharedMediaType.Person
                                    else -> null
                                }
                            SearchSuggestionItem(
                                name = item.name,
                                imagePath = item.imagePath,
                                sharedElementKey =
                                    sharedMediaType?.let {
                                        MediaSharedElementKey(
                                            mediaId = item.id.toLong(),
                                            mediaType = it,
                                            origin = SharedElementOrigin.SEARCH,
                                            elementType = SharedElementType.Image,
                                        )
                                    },
                                onItemClick = {
                                    // Converting type to uppercase for [MediaType]
                                    onSearchResultClick("${item.id},${item.mediaType.uppercase()}")
                                },
                            )
                        }
                    }
                }
            } else {
                // Initial empty state - encourage user to search
                SearchInitialState()
            }
        }
    }
}

/** Initial empty state when no search query is entered. */
@Composable
private fun SearchInitialState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                modifier = Modifier.size(Dimens.iconSizeLarge),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = stringResource(id = R.string.search_hint),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(id = R.string.search_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Empty state when search returns no results. */
@Composable
private fun SearchEmptyState(query: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            modifier = Modifier.padding(horizontal = Spacing.screenPadding),
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                modifier = Modifier.size(Dimens.iconSizeLarge),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = stringResource(id = R.string.no_results_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(id = R.string.no_results_description, query),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
