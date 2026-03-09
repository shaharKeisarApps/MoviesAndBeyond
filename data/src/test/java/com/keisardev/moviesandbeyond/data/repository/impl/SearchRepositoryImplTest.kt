package com.keisardev.moviesandbeyond.data.repository.impl

import com.keisardev.moviesandbeyond.core.model.Result
import com.keisardev.moviesandbeyond.core.model.error.NetworkError
import com.keisardev.moviesandbeyond.core.network.model.search.NetworkSearchItem
import com.keisardev.moviesandbeyond.core.network.model.search.SearchResponse
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchRepositoryImplTest {

    private lateinit var fakeTmdbApi: FakeTmdbApi
    private lateinit var repository: SearchRepositoryImpl

    @Before
    fun setUp() {
        fakeTmdbApi = FakeTmdbApi()
        repository = SearchRepositoryImpl(fakeTmdbApi)
    }

    @Test
    fun `getSearchSuggestions success returns mapped search items`() = runTest {
        fakeTmdbApi.searchResponse =
            SearchResponse(
                page = 1,
                results =
                    listOf(
                        NetworkSearchItem(
                            id = 1,
                            title = "Inception",
                            mediaType = "movie",
                            posterPath = "/inception.jpg",
                        ),
                        NetworkSearchItem(
                            id = 2,
                            name = "Breaking Bad",
                            mediaType = "tv",
                            posterPath = "/bb.jpg",
                        ),
                    ),
                totalPages = 1,
            )

        val result = repository.getSearchSuggestions(query = "test", includeAdult = false)

        assertTrue(result is Result.Success)
        val items = (result as Result.Success).data
        assertEquals(2, items.size)
        assertEquals("Inception", items[0].name)
        assertEquals("Breaking Bad", items[1].name)
    }

    @Test
    fun `getSearchSuggestions with empty results returns empty list`() = runTest {
        fakeTmdbApi.searchResponse = SearchResponse(page = 1, results = emptyList(), totalPages = 1)

        val result = repository.getSearchSuggestions(query = "nonexistent", includeAdult = false)

        assertTrue(result is Result.Success)
        assertEquals(0, (result as Result.Success).data.size)
    }

    @Test
    fun `getSearchSuggestions with IOException returns Result Error`() = runTest {
        fakeTmdbApi.exception = IOException("Network unavailable")

        val result = repository.getSearchSuggestions(query = "test", includeAdult = false)

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is IOException)
    }

    @Test
    fun `getSearchSuggestions with NetworkError returns Result Error with message`() = runTest {
        fakeTmdbApi.exception = NetworkError.RateLimited()

        val result = repository.getSearchSuggestions(query = "test", includeAdult = false)

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is NetworkError.RateLimited)
        assertEquals("Rate limited (429)", result.message)
    }

    @Test
    fun `getSearchSuggestions forwards includeAdult parameter to api`() = runTest {
        fakeTmdbApi.searchResponse = SearchResponse(page = 1, results = emptyList(), totalPages = 1)

        repository.getSearchSuggestions(query = "test", includeAdult = true)

        assertTrue(fakeTmdbApi.lastIncludeAdult)
    }
}
