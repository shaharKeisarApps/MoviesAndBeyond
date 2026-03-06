package com.keisardev.moviesandbeyond.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keisardev.moviesandbeyond.core.model.Result
import com.keisardev.moviesandbeyond.core.model.SearchItem
import com.keisardev.moviesandbeyond.core.network.error.NetworkError
import com.keisardev.moviesandbeyond.data.coroutines.stateInWhileSubscribed
import com.keisardev.moviesandbeyond.data.repository.SearchRepository
import com.keisardev.moviesandbeyond.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

/**
 * ViewModel for the multi-search screen.
 *
 * Provides debounced search-as-you-type suggestions by observing [searchQuery] and querying
 * [SearchRepository]. Respects the user's adult-content preference from [UserRepository].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel
@Inject
constructor(
    private val userRepository: UserRepository,
    private val searchRepository: SearchRepository,
) : ViewModel() {
    private var includeAdult: Boolean = false

    init {
        getIncludeAdult()
    }

    private val _searchQuery = MutableStateFlow("")

    /** The current search query text entered by the user. */
    val searchQuery = _searchQuery.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)

    /** One-shot error message to display in a snackbar; cleared after shown. */
    val errorMessage = _errorMessage.asStateFlow()

    /** Search suggestions that update reactively as the user types (200ms debounce). */
    val searchSuggestions: StateFlow<List<SearchItem>> =
        _searchQuery
            .filter { it.isNotEmpty() }
            .mapLatest { query ->
                delay(200)
                val result =
                    searchRepository.getSearchSuggestions(
                        query = query,
                        includeAdult = includeAdult,
                    )
                when (result) {
                    is Result.Success -> result.data

                    is Result.Error -> {
                        _errorMessage.update { toUserFriendlyMessage(result) }
                        emptyList()
                    }

                    is Result.Loading -> emptyList()
                }
            }
            .stateInWhileSubscribed(scope = viewModelScope, initialValue = emptyList())

    fun changeSearchQuery(query: String) {
        _searchQuery.update { query }
    }

    fun onBack() {
        _searchQuery.update { "" }
    }

    fun onErrorShown() {
        _errorMessage.update { null }
    }

    private fun toUserFriendlyMessage(error: Result.Error): String =
        when (val ex = error.exception) {
            is NetworkError.RateLimited -> "Too many requests. Please wait and try again."
            is NetworkError.ConnectionError -> "No internet connection. Please check your network."
            is NetworkError.ServerError -> "Server error (${ex.code}). Please try again later."
            else -> error.message ?: "Search failed. Please try again."
        }

    private fun getIncludeAdult() {
        userRepository.userData
            .map { it.includeAdultResults }
            .distinctUntilChanged()
            .onEach { includeAdult = it }
            .launchIn(viewModelScope)
    }
}
