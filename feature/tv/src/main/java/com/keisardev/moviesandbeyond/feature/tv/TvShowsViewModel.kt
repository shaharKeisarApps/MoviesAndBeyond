package com.keisardev.moviesandbeyond.feature.tv

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keisardev.moviesandbeyond.core.model.content.ContentItem
import com.keisardev.moviesandbeyond.core.model.content.TvShowListCategory
import com.keisardev.moviesandbeyond.data.coroutines.stateInWhileSubscribed
import com.keisardev.moviesandbeyond.data.repository.ContentRepository
import com.keisardev.moviesandbeyond.data.store.errorMessageOrNull
import com.keisardev.moviesandbeyond.data.store.isFromCache
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.mobilenativefoundation.store.store5.StoreReadResponse

/**
 * ViewModel for the TV Shows feed screen using Store5 for offline-first caching and
 * WhileSubscribedOrRetained for optimal Flow consumption during configuration changes.
 *
 * Data loading pattern follows best practices from:
 * - https://proandroiddev.com/loading-initial-data-in-launchedeffect-vs-viewmodel-f1747c20ce62
 * - Uses Flow + stateIn instead of LaunchedEffect or init block API calls
 * - WhileSubscribedOrRetained prevents re-fetch on configuration changes
 * - Store5 provides offline-first caching with automatic cache/network coordination
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TvShowsViewModel @Inject constructor(private val contentRepository: ContentRepository) :
    ViewModel() {

    // Page tracking for each category - initialized to 1, triggers first page load when subscribed
    private val _airingTodayPage = MutableStateFlow(1)
    private val _onAirPage = MutableStateFlow(1)
    private val _popularPage = MutableStateFlow(1)
    private val _topRatedPage = MutableStateFlow(1)

    // Accumulated items for each category (for pagination)
    private val _airingTodayAccumulated = MutableStateFlow<List<ContentItem>>(emptyList())
    private val _onAirAccumulated = MutableStateFlow<List<ContentItem>>(emptyList())
    private val _popularAccumulated = MutableStateFlow<List<ContentItem>>(emptyList())
    private val _topRatedAccumulated = MutableStateFlow<List<ContentItem>>(emptyList())

    // Error message state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // Airing Today TV Shows with offline-first support
    val airingTodayTvShows: StateFlow<ContentUiState> =
        createContentFlow(
                category = TvShowListCategory.AIRING_TODAY,
                pageFlow = _airingTodayPage,
                accumulatedFlow = _airingTodayAccumulated)
            .stateInWhileSubscribed(
                scope = viewModelScope,
                initialValue = ContentUiState(TvShowListCategory.AIRING_TODAY))

    // On Air TV Shows with offline-first support
    val onAirTvShows: StateFlow<ContentUiState> =
        createContentFlow(
                category = TvShowListCategory.ON_THE_AIR,
                pageFlow = _onAirPage,
                accumulatedFlow = _onAirAccumulated)
            .stateInWhileSubscribed(
                scope = viewModelScope,
                initialValue = ContentUiState(TvShowListCategory.ON_THE_AIR))

    // Popular TV Shows with offline-first support
    val popularTvShows: StateFlow<ContentUiState> =
        createContentFlow(
                category = TvShowListCategory.POPULAR,
                pageFlow = _popularPage,
                accumulatedFlow = _popularAccumulated)
            .stateInWhileSubscribed(
                scope = viewModelScope, initialValue = ContentUiState(TvShowListCategory.POPULAR))

    // Top Rated TV Shows with offline-first support
    val topRatedTvShows: StateFlow<ContentUiState> =
        createContentFlow(
                category = TvShowListCategory.TOP_RATED,
                pageFlow = _topRatedPage,
                accumulatedFlow = _topRatedAccumulated)
            .stateInWhileSubscribed(
                scope = viewModelScope, initialValue = ContentUiState(TvShowListCategory.TOP_RATED))

    /**
     * Creates a Flow that observes content from Store5 and combines it with accumulated items for
     * pagination support.
     */
    private fun createContentFlow(
        category: TvShowListCategory,
        pageFlow: MutableStateFlow<Int>,
        accumulatedFlow: MutableStateFlow<List<ContentItem>>
    ) =
        pageFlow
            .flatMapLatest { page ->
                contentRepository.observeTvShowItems(category = category, page = page).map {
                    response ->
                    handleStoreResponse(response, category, page, accumulatedFlow)
                }
            }
            .combine(accumulatedFlow) { currentState, accumulated ->
                currentState.copy(items = accumulated)
            }

    /**
     * Handles StoreReadResponse and updates accumulated items. Returns a ContentUiState based on
     * the response.
     */
    private fun handleStoreResponse(
        response: StoreReadResponse<List<ContentItem>>,
        category: TvShowListCategory,
        page: Int,
        accumulatedFlow: MutableStateFlow<List<ContentItem>>
    ): ContentUiState {
        return when (response) {
            is StoreReadResponse.Initial,
            is StoreReadResponse.Loading -> {
                ContentUiState(
                    items = accumulatedFlow.value,
                    isLoading = true,
                    endReached = false,
                    page = page,
                    category = category,
                    isFromCache = false)
            }
            is StoreReadResponse.Data -> {
                val newItems = response.value.orEmpty()
                // Accumulate items for pagination
                if (newItems.isNotEmpty()) {
                    accumulatedFlow.update { current ->
                        if (page == 1) newItems else (current + newItems).distinctBy { it.id }
                    }
                }
                ContentUiState(
                    items = accumulatedFlow.value,
                    isLoading = false,
                    endReached = newItems.isEmpty(),
                    page = page,
                    category = category,
                    isFromCache = response.isFromCache)
            }
            is StoreReadResponse.Error -> {
                _errorMessage.update { response.errorMessageOrNull() }
                ContentUiState(
                    items = accumulatedFlow.value,
                    isLoading = false,
                    endReached = false,
                    page = page,
                    category = category,
                    isFromCache = false)
            }
            is StoreReadResponse.NoNewData -> {
                // Keep current state, data hasn't changed
                ContentUiState(
                    items = accumulatedFlow.value,
                    isLoading = false,
                    endReached = false,
                    page = page,
                    category = category,
                    isFromCache = true)
            }
        }
    }

    /** Loads the next page of items for the specified category. */
    fun appendItems(category: TvShowListCategory) {
        when (category) {
            TvShowListCategory.AIRING_TODAY -> _airingTodayPage.update { it + 1 }
            TvShowListCategory.ON_THE_AIR -> _onAirPage.update { it + 1 }
            TvShowListCategory.POPULAR -> _popularPage.update { it + 1 }
            TvShowListCategory.TOP_RATED -> _topRatedPage.update { it + 1 }
        }
    }

    /** Forces a refresh of items for the specified category. */
    fun refresh(category: TvShowListCategory) {
        viewModelScope.launch {
            try {
                // Reset accumulated items and page
                when (category) {
                    TvShowListCategory.AIRING_TODAY -> {
                        _airingTodayAccumulated.value = emptyList()
                        _airingTodayPage.value = 1
                    }
                    TvShowListCategory.ON_THE_AIR -> {
                        _onAirAccumulated.value = emptyList()
                        _onAirPage.value = 1
                    }
                    TvShowListCategory.POPULAR -> {
                        _popularAccumulated.value = emptyList()
                        _popularPage.value = 1
                    }
                    TvShowListCategory.TOP_RATED -> {
                        _topRatedAccumulated.value = emptyList()
                        _topRatedPage.value = 1
                    }
                }
            } catch (e: Exception) {
                _errorMessage.update { e.message ?: "Refresh failed" }
            }
        }
    }

    fun onErrorShown() {
        _errorMessage.update { null }
    }
}

/**
 * UI state for TV show content lists with offline-first support.
 *
 * @param items The list of content items to display
 * @param isLoading Whether data is currently being fetched
 * @param endReached Whether all pages have been loaded
 * @param page The current page number
 * @param category The TV show list category
 * @param isFromCache Whether the current data is from cache (stale data indicator)
 */
@Immutable
data class ContentUiState(
    val items: List<ContentItem>,
    val isLoading: Boolean,
    val endReached: Boolean,
    val page: Int,
    val category: TvShowListCategory,
    val isFromCache: Boolean = false
) {
    constructor(
        category: TvShowListCategory
    ) : this(
        items = emptyList(),
        isLoading = true, // Start with loading state
        endReached = false,
        page = 1,
        category = category,
        isFromCache = false)
}
