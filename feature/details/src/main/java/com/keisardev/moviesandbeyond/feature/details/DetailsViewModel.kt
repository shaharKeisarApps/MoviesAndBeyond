package com.keisardev.moviesandbeyond.feature.details

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keisardev.moviesandbeyond.core.model.MediaType
import com.keisardev.moviesandbeyond.core.model.Result
import com.keisardev.moviesandbeyond.core.model.details.MovieDetails
import com.keisardev.moviesandbeyond.core.model.details.people.PersonDetails
import com.keisardev.moviesandbeyond.core.model.details.tv.TvDetails
import com.keisardev.moviesandbeyond.core.model.error.NetworkError
import com.keisardev.moviesandbeyond.core.model.library.LibraryItem
import com.keisardev.moviesandbeyond.core.ui.coroutines.stateInWhileSubscribed
import com.keisardev.moviesandbeyond.core.ui.toUserFriendlyMessage
import com.keisardev.moviesandbeyond.data.repository.AuthRepository
import com.keisardev.moviesandbeyond.data.repository.DetailsRepository
import com.keisardev.moviesandbeyond.data.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the movie/TV/person detail screen.
 *
 * Fetches full details from [DetailsRepository] based on a navigation argument encoded as
 * `"id,MEDIA_TYPE"`, and manages favorite/watchlist toggle actions via [LibraryRepository].
 */
internal const val ID_NAVIGATION_ARGUMENT = "id"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DetailsViewModel
@Inject
constructor(
    private val savedStateHandle: SavedStateHandle,
    private val detailsRepository: DetailsRepository,
    private val libraryRepository: LibraryRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val idDetailsString =
        savedStateHandle.getStateFlow(key = ID_NAVIGATION_ARGUMENT, initialValue = "")

    /**
     * Set the details ID for Navigation 3. In Nav3, the ID comes from the route key instead of URL
     * path arguments.
     */
    fun setDetailsId(id: String) {
        if (savedStateHandle.get<String>(ID_NAVIGATION_ARGUMENT).isNullOrEmpty()) {
            savedStateHandle[ID_NAVIGATION_ARGUMENT] = id
        }
    }

    private val _uiState = MutableStateFlow(DetailsUiState())

    /** UI state for library actions (favorite/watchlist toggles) and error messages. */
    val uiState = _uiState.asStateFlow()

    /** The detail content state: Loading, Empty, or a Movie/TV/Person result. */
    val contentDetailsUiState: StateFlow<ContentDetailUiState> =
        idDetailsString
            .mapLatest { detailsString ->
                detailsString
                    .takeIf { it.isNotEmpty() }
                    ?.let {
                        val idDetails = getIdAndMediaType(detailsString)

                        val id = idDetails.first
                        when (idDetails.second) {
                            MediaType.MOVIE -> {
                                val result = detailsRepository.getMovieDetails(id)
                                handleMovieDetailsResponse(result)
                            }

                            MediaType.TV -> {
                                val result = detailsRepository.getTvShowDetails(id)
                                handleTvDetailsResponse(result)
                            }

                            MediaType.PERSON -> {
                                val result = detailsRepository.getPersonDetails(id)
                                handlePeopleDetailsResponse(result)
                            }

                            else -> ContentDetailUiState.Empty
                        }
                    } ?: ContentDetailUiState.Empty
            }
            .stateInWhileSubscribed(
                scope = viewModelScope,
                initialValue = ContentDetailUiState.Loading,
            )

    fun addOrRemoveFavorite(libraryItem: LibraryItem) {
        viewModelScope.launch {
            try {
                val isLoggedIn = authRepository.isLoggedIn.first()
                _uiState.update { it.copy(markedFavorite = !(it.markedFavorite)) }
                // Save locally for both guest and authenticated users
                libraryRepository.addOrRemoveFavorite(libraryItem, isLoggedIn)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        markedFavorite = !(it.markedFavorite),
                        errorMessage = "Failed to update favorites. Please try again.",
                    )
                }
            }
        }
    }

    fun addOrRemoveFromWatchlist(libraryItem: LibraryItem) {
        viewModelScope.launch {
            try {
                val isLoggedIn = authRepository.isLoggedIn.first()
                _uiState.update { it.copy(savedInWatchlist = !(it.savedInWatchlist)) }
                // Save locally for both guest and authenticated users
                libraryRepository.addOrRemoveFromWatchlist(libraryItem, isLoggedIn)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        savedInWatchlist = !(it.savedInWatchlist),
                        errorMessage = "Failed to update watchlist. Please try again.",
                    )
                }
            }
        }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onHideBottomSheet() {
        _uiState.update { it.copy(showSignInSheet = false) }
    }

    private fun getIdAndMediaType(detailsString: String): Pair<Int, MediaType?> {
        val details = detailsString.split(",")
        val id = details.first().toInt()
        val mediaType = enumValueOf<MediaType>(details.last())
        return Pair(id, mediaType)
    }

    private suspend fun handleMovieDetailsResponse(
        result: Result<MovieDetails>
    ): ContentDetailUiState {
        return when (result) {
            is Result.Success -> {
                val data = result.data
                val (inFavorites, inWatchlist) =
                    coroutineScope {
                        val favoritesDeferred = async {
                            libraryRepository.itemInFavoritesExists(
                                mediaId = data.id,
                                mediaType = MediaType.MOVIE,
                            )
                        }
                        val watchlistDeferred = async {
                            libraryRepository.itemInWatchlistExists(
                                mediaId = data.id,
                                mediaType = MediaType.MOVIE,
                            )
                        }
                        Pair(favoritesDeferred.await(), watchlistDeferred.await())
                    }
                _uiState.update {
                    it.copy(markedFavorite = inFavorites, savedInWatchlist = inWatchlist)
                }
                ContentDetailUiState.Movie(data = data)
            }

            is Result.Error -> {
                _uiState.update {
                    it.copy(
                        errorMessage =
                            result.toUserFriendlyMessage(
                                fallback = "Failed to load details. Please try again.",
                                overrides =
                                    mapOf(
                                        NetworkError.Unauthorized::class.java to
                                            "Please sign in to view this content."
                                    ),
                            )
                    )
                }
                ContentDetailUiState.Empty
            }

            is Result.Loading -> ContentDetailUiState.Loading
        }
    }

    private suspend fun handleTvDetailsResponse(result: Result<TvDetails>): ContentDetailUiState {
        return when (result) {
            is Result.Success -> {
                val data = result.data
                val (inFavorites, inWatchlist) =
                    coroutineScope {
                        val favoritesDeferred = async {
                            libraryRepository.itemInFavoritesExists(
                                mediaId = data.id,
                                mediaType = MediaType.TV,
                            )
                        }
                        val watchlistDeferred = async {
                            libraryRepository.itemInWatchlistExists(
                                mediaId = data.id,
                                mediaType = MediaType.TV,
                            )
                        }
                        Pair(favoritesDeferred.await(), watchlistDeferred.await())
                    }
                _uiState.update {
                    it.copy(markedFavorite = inFavorites, savedInWatchlist = inWatchlist)
                }
                ContentDetailUiState.TV(data = data)
            }

            is Result.Error -> {
                _uiState.update {
                    it.copy(
                        errorMessage =
                            result.toUserFriendlyMessage(
                                fallback = "Failed to load details. Please try again.",
                                overrides =
                                    mapOf(
                                        NetworkError.Unauthorized::class.java to
                                            "Please sign in to view this content."
                                    ),
                            )
                    )
                }
                ContentDetailUiState.Empty
            }

            is Result.Loading -> ContentDetailUiState.Loading
        }
    }

    private fun handlePeopleDetailsResponse(result: Result<PersonDetails>): ContentDetailUiState {
        return when (result) {
            is Result.Success -> ContentDetailUiState.Person(data = result.data)

            is Result.Error -> {
                _uiState.update {
                    it.copy(
                        errorMessage =
                            result.toUserFriendlyMessage(
                                fallback = "Failed to load details. Please try again.",
                                overrides =
                                    mapOf(
                                        NetworkError.Unauthorized::class.java to
                                            "Please sign in to view this content."
                                    ),
                            )
                    )
                }
                ContentDetailUiState.Empty
            }

            is Result.Loading -> ContentDetailUiState.Loading
        }
    }
}

@Immutable
data class DetailsUiState(
    val markedFavorite: Boolean = false,
    val savedInWatchlist: Boolean = false,
    val showSignInSheet: Boolean = false,
    val errorMessage: String? = null,
)

/** Sealed state representing the loaded detail content for the detail screen. */
@Immutable
sealed interface ContentDetailUiState {
    data object Loading : ContentDetailUiState

    data object Empty : ContentDetailUiState

    @Immutable data class Movie(val data: MovieDetails) : ContentDetailUiState

    @Immutable data class TV(val data: TvDetails) : ContentDetailUiState

    @Immutable data class Person(val data: PersonDetails) : ContentDetailUiState
}
