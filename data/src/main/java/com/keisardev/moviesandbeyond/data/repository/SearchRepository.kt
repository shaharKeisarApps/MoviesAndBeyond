package com.keisardev.moviesandbeyond.data.repository

import com.keisardev.moviesandbeyond.core.model.Result
import com.keisardev.moviesandbeyond.core.model.SearchItem

/**
 * Repository for multi-search across movies, TV shows, and people on TMDB.
 *
 * Provides search-as-you-type suggestions used by the search screen.
 */
interface SearchRepository {
    /**
     * Returns search suggestions matching the given query.
     *
     * @param query The user's search text
     * @param includeAdult Whether to include adult content in results
     * @return A list of [SearchItem] matching the query, or an error
     */
    suspend fun getSearchSuggestions(query: String, includeAdult: Boolean): Result<List<SearchItem>>
}
