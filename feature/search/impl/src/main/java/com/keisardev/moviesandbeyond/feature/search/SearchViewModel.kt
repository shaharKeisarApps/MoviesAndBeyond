package com.keisardev.moviesandbeyond.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keisardev.moviesandbeyond.core.model.Result
import com.keisardev.moviesandbeyond.core.model.SearchItem
import com.keisardev.moviesandbeyond.core.ui.coroutines.stateInWhileSubscribed
import com.keisardev.moviesandbeyond.core.ui.toUserFriendlyMessage
import com.keisardev.moviesandbeyond.data.repository.SearchRepository
import com.keisardev.moviesandbeyond.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
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
    private val _searchQuery = MutableStateFlow("")

    /** The current search query text entered by the user. */
    val searchQuery = _searchQuery.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)

    /** One-shot error message to display in a snackbar; cleared after shown. */
    val errorMessage = _errorMessage.asStateFlow()

    private val includeAdult =
        userRepository.userData.map { it.includeAdultResults }.distinctUntilChanged()

    /** Search suggestions that update reactively as the user types (200ms debounce). */
    val searchSuggestions: StateFlow<ImmutableList<SearchItem>> =
        combine(_searchQuery, includeAdult) { query, adult -> query to adult }
            .mapLatest { (query, adult) ->
                if (query.isEmpty()) return@mapLatest persistentListOf()
                delay(200)
                val result =
                    searchRepository.getSearchSuggestions(query = query, includeAdult = adult)
                when (result) {
                    is Result.Success -> result.data.toImmutableList()

                    is Result.Error -> {
                        _errorMessage.update {
                            result.toUserFriendlyMessage(
                                fallback = "Search failed. Please try again."
                            )
                        }
                        persistentListOf()
                    }

                    is Result.Loading -> persistentListOf()
                }
            }
            .stateInWhileSubscribed(scope = viewModelScope, initialValue = persistentListOf())

    fun changeSearchQuery(query: String) {
        _searchQuery.update { query }
    }

    fun onBack() {
        _searchQuery.update { "" }
    }

    fun onErrorShown() {
        _errorMessage.update { null }
    }
}
