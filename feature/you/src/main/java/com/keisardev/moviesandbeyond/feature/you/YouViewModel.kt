package com.keisardev.moviesandbeyond.feature.you

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keisardev.moviesandbeyond.core.model.Result
import com.keisardev.moviesandbeyond.core.model.SeedColor
import com.keisardev.moviesandbeyond.core.model.SelectedDarkMode
import com.keisardev.moviesandbeyond.core.model.user.AccountDetails
import com.keisardev.moviesandbeyond.core.ui.coroutines.stateInWhileSubscribed
import com.keisardev.moviesandbeyond.data.repository.AuthRepository
import com.keisardev.moviesandbeyond.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val LIBRARY_ITEM_TYPE_NAVIGATION_ARGUMENT = "type"

/**
 * ViewModel for the "You" profile and settings screen.
 *
 * Manages TMDB account details, user preferences (theme, dark mode, content filters), library item
 * counts, and login/logout flows. Triggers immediate library sync after authentication changes.
 */
@HiltViewModel
class YouViewModel
@Inject
constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val libraryRepository: com.keisardev.moviesandbeyond.data.repository.LibraryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(YouUiState())

    /** Profile screen UI state (account details, loading indicators, errors). */
    val uiState = _uiState.asStateFlow()

    /** Emits the current authentication state; triggers account detail fetch on login. */
    val isLoggedIn =
        authRepository.isLoggedIn
            .onEach { isLoggedIn -> if (isLoggedIn) getAccountDetails() }
            .stateInWhileSubscribed(scope = viewModelScope, initialValue = null)

    /** Current user preferences for theme, content filters, and local-only mode. */
    val userSettings: StateFlow<UserSettings?> =
        userRepository.userData
            .map {
                UserSettings(
                    useDynamicColor = it.useDynamicColor,
                    includeAdultResults = it.includeAdultResults,
                    darkMode = it.darkMode,
                    useLocalOnly = it.useLocalOnly,
                    customColorArgb = it.customColorArgb,
                )
            }
            .stateInWhileSubscribed(scope = viewModelScope, initialValue = null)

    /** Combined count of favorites and watchlist items across movies and TV shows. */
    val libraryItemCounts: StateFlow<LibraryItemCounts> =
        combine(
                libraryRepository.favoriteMovies,
                libraryRepository.favoriteTvShows,
                libraryRepository.moviesWatchlist,
                libraryRepository.tvShowsWatchlist,
            ) { favoriteMovies, favoriteTvShows, moviesWatchlist, tvShowsWatchlist ->
                LibraryItemCounts(
                    favoritesCount = favoriteMovies.size + favoriteTvShows.size,
                    watchlistCount = moviesWatchlist.size + tvShowsWatchlist.size,
                )
            }
            .stateInWhileSubscribed(scope = viewModelScope, initialValue = LibraryItemCounts(0, 0))

    fun setDynamicColorPreference(useDynamicColor: Boolean) {
        viewModelScope.launch { userRepository.setDynamicColorPreference(useDynamicColor) }
    }

    fun setAdultResultPreference(includeAdultResults: Boolean) {
        viewModelScope.launch { userRepository.setAdultResultPreference(includeAdultResults) }
    }

    fun setDarkModePreference(selectedDarkMode: SelectedDarkMode) {
        viewModelScope.launch { userRepository.setDarkModePreference(selectedDarkMode) }
    }

    fun setSeedColorPreference(seedColor: SeedColor) {
        viewModelScope.launch { userRepository.setSeedColorPreference(seedColor) }
    }

    fun toggleUseLocalOnly(useLocalOnly: Boolean) {
        viewModelScope.launch { userRepository.setUseLocalOnly(useLocalOnly) }
    }

    fun setCustomColorArgb(colorArgb: Long) {
        viewModelScope.launch { userRepository.setCustomColorArgb(colorArgb) }
    }

    fun getAccountDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val accountDetails = userRepository.getAccountDetails()
            _uiState.update { it.copy(isLoading = false, accountDetails = accountDetails) }

            // Trigger immediate sync of favorites and watchlist after login
            // This ensures TMDB data appears without waiting for WorkManager
            launch {
                libraryRepository.syncFavorites()
                libraryRepository.syncWatchlist()
            }
        }
    }

    fun logOut() {
        viewModelScope.launch {
            val accountId = _uiState.value.accountDetails?.id ?: return@launch
            _uiState.update { it.copy(isLoggingOut = true) }

            val result = authRepository.logout(accountId = accountId)
            when (result) {
                is Result.Success -> {}

                is Result.Error -> {
                    _uiState.update {
                        it.copy(errorMessage = result.message ?: "Logout failed. Please try again.")
                    }
                }

                is Result.Loading -> {}
            }

            _uiState.update { it.copy(isLoggingOut = false) }
        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            val accountId = _uiState.value.accountDetails?.id ?: return@launch
            _uiState.update { it.copy(isRefreshing = true) }

            val result = userRepository.updateAccountDetails(accountId = accountId)
            when (result) {
                is Result.Success -> {
                    // Sync favorites and watchlist after refreshing account details
                    // This ensures TMDB data is pulled when user refreshes
                    launch {
                        libraryRepository.syncFavorites()
                        libraryRepository.syncWatchlist()
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            errorMessage = result.message ?: "Refresh failed. Please try again."
                        )
                    }
                }

                is Result.Loading -> {}
            }

            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

@Immutable
data class YouUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoggingOut: Boolean = false,
    val accountDetails: AccountDetails? = null,
    val errorMessage: String? = null,
)

@Immutable
data class UserSettings(
    val useDynamicColor: Boolean,
    val includeAdultResults: Boolean,
    val darkMode: SelectedDarkMode,
    val useLocalOnly: Boolean,
    val customColorArgb: Long,
)

@Immutable data class LibraryItemCounts(val favoritesCount: Int, val watchlistCount: Int)
