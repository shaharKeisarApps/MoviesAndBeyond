package com.keisardev.moviesandbeyond.data.testdoubles.repository

import com.keisardev.moviesandbeyond.core.model.Result
import com.keisardev.moviesandbeyond.core.model.SearchItem
import com.keisardev.moviesandbeyond.data.repository.SearchRepository
import com.keisardev.moviesandbeyond.data.testdoubles.testSearchResults

class TestSearchRepository : SearchRepository {
    private var generateError = false

    override suspend fun getSearchSuggestions(
        query: String,
        includeAdult: Boolean,
    ): Result<List<SearchItem>> {
        if (generateError) return Result.Error(RuntimeException("Search failed"))
        return Result.Success(testSearchResults)
    }

    fun generateError(value: Boolean) {
        generateError = value
    }
}
