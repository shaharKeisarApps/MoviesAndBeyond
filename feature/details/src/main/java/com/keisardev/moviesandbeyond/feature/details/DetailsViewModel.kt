package com.keisardev.moviesandbeyond.feature.details

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keisardev.moviesandbeyond.core.model.MediaType
import com.keisardev.moviesandbeyond.core.model.NetworkResponse
import com.keisardev.moviesandbeyond.core.model.details.MovieDetails
import com.keisardev.moviesandbeyond.core.model.details.people.PersonDetails
import com.keisardev.moviesandbeyond.core.model.details.tv.TvDetails
import com.keisardev.moviesandbeyond.core.model.library.LibraryItem
import com.keisardev.moviesandbeyond.data.coroutines.stateInWhileSubscribed
import com.keisardev.moviesandbeyond.data.repository.AuthRepository
import com.keisardev.moviesandbeyond.data.repository.DetailsRepository
import com.keisardev.moviesandbeyond.data.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DetailsViewModel
@Inject
constructor(
    private val savedStateHandle: SavedStateHandle,
    private val detailsRepository: DetailsRepository,
    private val libraryRepository: LibraryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val idDetailsString =
        savedStateHandle.getStateFlow(key = idNavigationArgument, initialValue = "")

    /**
     * Set the details ID for Navigation 3. In Nav3, the ID comes from the route key instead of URL
     * path arguments.
     */
    fun setDetailsId(id: String) {
        if (savedStateHandle.get<String>(idNavigationArgument).isNullOrEmpty()) {
            savedStateHandle[idNavigationArgument] = id
        }
    }

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState = _uiState.asStateFlow()

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
                                val response = detailsRepository.getMovieDetails(id)
                                handleMovieDetailsResponse(response)
                            }

                            MediaType.TV -> {
                                val response = detailsRepository.getTvShowDetails(id)
                                handleTvDetailsResponse(response)
                            }

                            MediaType.PERSON -> {
                                val response = detailsRepository.getPersonDetails(id)
                                handlePeopleDetailsResponse(response)
                            }

                            else -> ContentDetailUiState.Empty
                        }
                    } ?: ContentDetailUiState.Empty
            }
            .stateInWhileSubscribed(
                scope = viewModelScope, initialValue = ContentDetailUiState.Loading)

    fun addOrRemoveFavorite(libraryItem: LibraryItem) {
        viewModelScope.launch {
            try {
                val isLoggedIn = authRepository.isLoggedIn.first()
                _uiState.update { it.copy(markedFavorite = !(it.markedFavorite)) }
                // Save locally for both guest and authenticated users
                libraryRepository.addOrRemoveFavorite(libraryItem, isLoggedIn)
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        markedFavorite = !(it.markedFavorite), errorMessage = "An error occurred")
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
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        savedInWatchlist = !(it.savedInWatchlist),
                        errorMessage = "An error occurred")
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
        response: NetworkResponse<MovieDetails>
    ): ContentDetailUiState {
        return when (response) {
            is NetworkResponse.Success -> {
                val data = response.data
                _uiState.update {
                    it.copy(
                        markedFavorite =
                            libraryRepository.itemInFavoritesExists(
                                mediaId = data.id, mediaType = MediaType.MOVIE),
                        savedInWatchlist =
                            libraryRepository.itemInWatchlistExists(
                                mediaId = data.id, mediaType = MediaType.MOVIE))
                }
                ContentDetailUiState.Movie(data = data)
            }

            is NetworkResponse.Error -> {
                _uiState.update { it.copy(errorMessage = response.errorMessage) }
                ContentDetailUiState.Empty
            }
        }
    }

    private suspend fun handleTvDetailsResponse(
        response: NetworkResponse<TvDetails>
    ): ContentDetailUiState {
        return when (response) {
            is NetworkResponse.Success -> {
                val data = response.data
                _uiState.update {
                    it.copy(
                        markedFavorite =
                            libraryRepository.itemInFavoritesExists(
                                mediaId = data.id, mediaType = MediaType.TV),
                        savedInWatchlist =
                            libraryRepository.itemInWatchlistExists(
                                mediaId = data.id, mediaType = MediaType.TV))
                }
                ContentDetailUiState.TV(data = data)
            }

            is NetworkResponse.Error -> {
                _uiState.update { it.copy(errorMessage = response.errorMessage) }
                ContentDetailUiState.Empty
            }
        }
    }

    private fun handlePeopleDetailsResponse(
        response: NetworkResponse<PersonDetails>
    ): ContentDetailUiState {
        return when (response) {
            is NetworkResponse.Success -> {
                ContentDetailUiState.Person(data = response.data)
            }

            is NetworkResponse.Error -> {
                _uiState.update { it.copy(errorMessage = response.errorMessage) }
                ContentDetailUiState.Empty
            }
        }
    }
}

@Immutable
data class DetailsUiState(
    val markedFavorite: Boolean = false,
    val savedInWatchlist: Boolean = false,
    val showSignInSheet: Boolean = false,
    val errorMessage: String? = null
)

@Immutable
sealed interface ContentDetailUiState {
    data object Loading : ContentDetailUiState

    data object Empty : ContentDetailUiState

    @Immutable data class Movie(val data: MovieDetails) : ContentDetailUiState

    @Immutable data class TV(val data: TvDetails) : ContentDetailUiState

    @Immutable data class Person(val data: PersonDetails) : ContentDetailUiState
}
