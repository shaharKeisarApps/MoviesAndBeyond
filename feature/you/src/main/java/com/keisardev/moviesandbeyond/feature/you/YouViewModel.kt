package com.keisardev.moviesandbeyond.feature.you

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keisardev.moviesandbeyond.core.model.NetworkResponse
import com.keisardev.moviesandbeyond.core.model.SeedColor
import com.keisardev.moviesandbeyond.core.model.SelectedDarkMode
import com.keisardev.moviesandbeyond.core.model.user.AccountDetails
import com.keisardev.moviesandbeyond.data.coroutines.stateInWhileSubscribed
import com.keisardev.moviesandbeyond.data.repository.AuthRepository
import com.keisardev.moviesandbeyond.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class YouViewModel
@Inject
constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val libraryRepository: com.keisardev.moviesandbeyond.data.repository.LibraryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(YouUiState())
    val uiState = _uiState.asStateFlow()

    val isLoggedIn =
        authRepository.isLoggedIn
            .onEach { isLoggedIn -> if (isLoggedIn) getAccountDetails() }
            .stateInWhileSubscribed(scope = viewModelScope, initialValue = null)

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

    val libraryItemCounts: StateFlow<LibraryItemCounts> =
        kotlinx.coroutines.flow
            .combine(
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
            try {
                val accountDetails = userRepository.getAccountDetails()
                _uiState.update { it.copy(isLoading = false, accountDetails = accountDetails) }

                // Trigger immediate sync of favorites and watchlist after login
                // This ensures TMDB data appears without waiting for WorkManager
                launch {
                    try {
                        libraryRepository.syncFavorites()
                        libraryRepository.syncWatchlist()
                    } catch (e: Exception) {
                        // Silent failure - WorkManager will retry later
                    }
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Failed to load account details.")
                }
            }
        }
    }

    fun logOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }

            val response = authRepository.logout(accountId = _uiState.value.accountDetails!!.id)
            when (response) {
                is NetworkResponse.Success -> {}

                is NetworkResponse.Error -> {
                    val errorMessage = response.errorMessage
                    _uiState.update { it.copy(errorMessage = errorMessage) }
                }
            }

            _uiState.update { it.copy(isLoggingOut = false) }
        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            val response =
                userRepository.updateAccountDetails(accountId = _uiState.value.accountDetails!!.id)
            when (response) {
                is NetworkResponse.Success -> {
                    // Sync favorites and watchlist after refreshing account details
                    // This ensures TMDB data is pulled when user refreshes
                    launch {
                        try {
                            libraryRepository.syncFavorites()
                            libraryRepository.syncWatchlist()
                        } catch (e: Exception) {
                            // Silent failure - sync will retry later via WorkManager
                        }
                    }
                }

                is NetworkResponse.Error -> {
                    val errorMessage = response.errorMessage
                    _uiState.update { it.copy(errorMessage = errorMessage) }
                }
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
