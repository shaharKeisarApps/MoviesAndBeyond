package com.keisardev.moviesandbeyond.feature.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.keisardev.moviesandbeyond.core.model.SearchItem
import com.keisardev.moviesandbeyond.core.ui.MediaSharedElementKey
import com.keisardev.moviesandbeyond.core.ui.MediaType as SharedMediaType
import com.keisardev.moviesandbeyond.core.ui.MoviesAndBeyondSearchBar
import com.keisardev.moviesandbeyond.core.ui.SharedElementOrigin
import com.keisardev.moviesandbeyond.core.ui.SharedElementType
import com.keisardev.moviesandbeyond.core.ui.theme.Spacing
import kotlinx.coroutines.launch

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
        onErrorShown = viewModel::onErrorShown)
}

@Composable
internal fun SearchScreen(
    searchQuery: String,
    errorMessage: String?,
    searchSuggestions: List<SearchItem>,
    onSearchQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onSearchResultClick: (String) -> Unit,
    onErrorShown: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    errorMessage?.let {
        scope.launch { snackbarHostState.showSnackbar(it) }
        onErrorShown()
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            MoviesAndBeyondSearchBar(
                value = searchQuery, onQueryChange = { onSearchQueryChange(it) })

            if (searchQuery.isNotEmpty()) {
                BackHandler {
                    onSearchQueryChange("")
                    onBack()
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = Spacing.screenPadding),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.itemSpacing),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    modifier = Modifier.fillMaxSize()) {
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
                                            elementType = SharedElementType.Image)
                                    },
                                onItemClick = {
                                    // Converting type to uppercase for [MediaType]
                                    onSearchResultClick("${item.id},${item.mediaType.uppercase()}")
                                })
                        }
                    }
            }
            SearchHistoryContent(history = listOf())
        }
    }
}

@Composable
private fun SearchHistoryContent(history: List<String>) {
    Box(Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(Spacing.md), modifier = Modifier.fillMaxSize()) {
            items(items = history) { Text(it) }
        }
    }
}
