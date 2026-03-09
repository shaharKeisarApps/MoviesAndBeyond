package com.keisardev.moviesandbeyond.feature.auth

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keisardev.moviesandbeyond.core.model.Result
import com.keisardev.moviesandbeyond.core.model.error.NetworkError
import com.keisardev.moviesandbeyond.core.ui.toUserFriendlyMessage
import com.keisardev.moviesandbeyond.data.repository.AuthRepository
import com.keisardev.moviesandbeyond.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the TMDB authentication screen.
 *
 * Manages login form state, communicates with [AuthRepository] for credential validation, and
 * handles first-launch onboarding dismissal.
 */
@HiltViewModel
class AuthViewModel
@Inject
constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())

    /** The current authentication UI state (form fields, loading, errors). */
    val uiState = _uiState.asStateFlow()

    /**
     * Whether to skip the onboarding screen; null while loading.
     *
     * Uses [SharingStarted.Eagerly] so the value is always current when [logIn] reads it,
     * regardless of whether the UI is actively subscribed to this flow.
     */
    val hideOnboarding: StateFlow<Boolean?> =
        userRepository.userData
            .map { it.hideOnboarding }
            .stateIn(scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = null)

    fun logIn() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result =
                authRepository.login(
                    username = _uiState.value.username,
                    password = _uiState.value.password,
                )
            when (result) {
                is Result.Success -> {
                    if (hideOnboarding.value == false) {
                        /**
                         * User has opened app for first time. This will recompose the NavHost and
                         * user will be automatically navigated from AuthScreen.
                         */
                        setHideOnboarding()
                    } else {
                        _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                    }
                }

                is Result.Error -> {
                    val message =
                        result.toUserFriendlyMessage(
                            fallback = "Login failed. Please try again.",
                            overrides =
                                mapOf(
                                    NetworkError.Unauthorized::class.java to
                                        "Invalid username or password.",
                                    NetworkError.RateLimited::class.java to
                                        "Too many login attempts. Please wait and try again.",
                                ),
                        )
                    _uiState.update { it.copy(isLoading = false, errorMessage = message) }
                }

                is Result.Loading -> Unit
            }
        }
    }

    fun setHideOnboarding() {
        viewModelScope.launch { userRepository.setHideOnboarding(true) }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onUsernameChange(username: String) {
        _uiState.update { it.copy(username = username) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }
}

@Immutable
data class AuthUiState(
    val username: String = "",
    val password: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
)
