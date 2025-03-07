package com.keisardev.moviesandbeyond

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keisardev.moviesandbeyond.MainActivityUiState.Loading
import com.keisardev.moviesandbeyond.MainActivityUiState.Success
import com.keisardev.moviesandbeyond.core.model.SelectedDarkMode
import com.keisardev.moviesandbeyond.data.repository.AuthRepository
import com.keisardev.moviesandbeyond.data.repository.UserRepository
import com.keisardev.moviesandbeyond.data.util.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val syncScheduler: SyncScheduler
) : ViewModel() {
    val uiState: StateFlow<MainActivityUiState> = userRepository.userData.map {
        Success(
            useDynamicColor = it.useDynamicColor,
            darkMode = it.darkMode,
            hideOnboarding = it.hideOnboarding
        )
    }.stateIn(
        scope = viewModelScope,
        initialValue = Loading,
        started = SharingStarted.WhileSubscribed(5000L),
    )

    init {
        executeLibrarySyncWork()
    }

    private fun executeLibrarySyncWork() {
        viewModelScope.launch {
            val loggedIn = authRepository.isLoggedIn.first()
            if (loggedIn) {
                syncScheduler.scheduleLibrarySyncWork()
            }
        }
    }
}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Success(
        val useDynamicColor: Boolean,
        val darkMode: SelectedDarkMode,
        val hideOnboarding: Boolean
    ) : MainActivityUiState
}