package com.keisardev.moviesandbeyond.feature.you.library_items

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keisardev.moviesandbeyond.core.model.library.LibraryItem
import com.keisardev.moviesandbeyond.core.model.library.LibraryItemType
import com.keisardev.moviesandbeyond.core.ui.coroutines.stateInWhileSubscribed
import com.keisardev.moviesandbeyond.data.repository.AuthRepository
import com.keisardev.moviesandbeyond.data.repository.LibraryRepository
import com.keisardev.moviesandbeyond.feature.you.LIBRARY_ITEM_TYPE_NAVIGATION_ARGUMENT
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the library items list screen (Favorites or Watchlist).
 *
 * Displays movie and TV show items for the selected [LibraryItemType] and supports swipe-to-delete
 * with automatic TMDB sync when authenticated.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LibraryItemsViewModel
@Inject
constructor(
    private val savedStateHandle: SavedStateHandle,
    private val libraryRepository: LibraryRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)

    /** One-shot error message to display in a snackbar; cleared after shown. */
    val errorMessage = _errorMessage.asStateFlow()

    /**
     * Set the library item type for Navigation 3. In Nav3, the type comes from the route key
     * instead of URL path arguments.
     */
    fun setLibraryItemType(type: String) {
        if (savedStateHandle.get<String>(LIBRARY_ITEM_TYPE_NAVIGATION_ARGUMENT).isNullOrEmpty()) {
            savedStateHandle[LIBRARY_ITEM_TYPE_NAVIGATION_ARGUMENT] = type
        }
    }

    private val libraryItemTypeString =
        savedStateHandle.getStateFlow(
            key = LIBRARY_ITEM_TYPE_NAVIGATION_ARGUMENT,
            initialValue = "",
        )

    /** The current library type (FAVORITE or WATCHLIST) derived from the navigation argument. */
    val libraryItemType: StateFlow<LibraryItemType?> =
        libraryItemTypeString
            .map { if (it.isEmpty()) null else enumValueOf<LibraryItemType>(it) }
            .stateInWhileSubscribed(scope = viewModelScope, initialValue = null)

    /** Reactive list of movie items for the current library type. */
    val movieItems: StateFlow<ImmutableList<LibraryItem>> =
        libraryItemType
            .flatMapLatest { itemType ->
                itemType?.let {
                    when (it) {
                        LibraryItemType.FAVORITE -> libraryRepository.favoriteMovies
                        LibraryItemType.WATCHLIST -> libraryRepository.moviesWatchlist
                    }
                } ?: flow { emit(emptyList()) }
            }
            .map { it.toImmutableList() }
            .stateInWhileSubscribed(scope = viewModelScope, initialValue = persistentListOf())

    /** Reactive list of TV show items for the current library type. */
    val tvItems: StateFlow<ImmutableList<LibraryItem>> =
        libraryItemType
            .flatMapLatest { itemType ->
                itemType?.let {
                    when (it) {
                        LibraryItemType.FAVORITE -> libraryRepository.favoriteTvShows
                        LibraryItemType.WATCHLIST -> libraryRepository.tvShowsWatchlist
                    }
                } ?: flow { emit(emptyList()) }
            }
            .map { it.toImmutableList() }
            .stateInWhileSubscribed(scope = viewModelScope, initialValue = persistentListOf())

    fun deleteItem(libraryItem: LibraryItem) {
        viewModelScope.launch {
            try {
                val isAuthenticated = authRepository.isLoggedIn.first()
                when (libraryItemType.value) {
                    LibraryItemType.FAVORITE -> {
                        libraryRepository.addOrRemoveFavorite(libraryItem, isAuthenticated)
                    }
                    LibraryItemType.WATCHLIST -> {
                        libraryRepository.addOrRemoveFromWatchlist(libraryItem, isAuthenticated)
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                _errorMessage.update { "Failed to remove item. Please try again." }
            }
        }
    }

    fun onErrorShown() {
        _errorMessage.update { null }
    }
}
