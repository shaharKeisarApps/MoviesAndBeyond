package com.keisardev.moviesandbeyond.feature.search

import com.keisardev.moviesandbeyond.core.model.SearchItem
import com.keisardev.moviesandbeyond.core.testing.MainDispatcherRule
import com.keisardev.moviesandbeyond.data.testdoubles.repository.TestSearchRepository
import com.keisardev.moviesandbeyond.data.testdoubles.repository.TestUserRepository
import com.keisardev.moviesandbeyond.data.testdoubles.testSearchResults
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private val userRepository = TestUserRepository()
    private val searchRepository = TestSearchRepository()
    private lateinit var viewModel: SearchViewModel

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        viewModel =
            SearchViewModel(userRepository = userRepository, searchRepository = searchRepository)
    }

    @Test
    fun `test initial state`() {
        assertEquals("", viewModel.searchQuery.value)

        assertNull(viewModel.errorMessage.value)

        assertEquals(emptyList<SearchItem>(), viewModel.searchSuggestions.value)
    }

    @Test
    fun `test search result when query entered`() = runTest {
        val searchQueryCollectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.searchQuery.collect() }
        val searchResultCollectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.searchSuggestions.collect() }

        viewModel.changeSearchQuery("test")
        advanceUntilIdle()

        assertEquals(testSearchResults, viewModel.searchSuggestions.value)

        searchQueryCollectJob.cancel()
        searchResultCollectJob.cancel()
    }

    @Test
    fun `test search result error`() = runTest {
        val searchQueryCollectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.searchQuery.collect() }
        val searchResultCollectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.searchSuggestions.collect() }

        searchRepository.generateError(true)
        viewModel.changeSearchQuery("test")
        advanceUntilIdle()

        assertEquals(emptyList<SearchItem>(), viewModel.searchSuggestions.value)
        // Error was mapped to a user-friendly message
        assertFalse(viewModel.errorMessage.value.isNullOrEmpty())

        searchQueryCollectJob.cancel()
        searchResultCollectJob.cancel()
    }

    @Test
    fun `test error message reset`() {
        viewModel.onErrorShown()

        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `test empty query produces no results`() = runTest {
        val searchResultCollectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.searchSuggestions.collect() }

        viewModel.changeSearchQuery("")
        advanceUntilIdle()

        assertEquals(emptyList<SearchItem>(), viewModel.searchSuggestions.value)

        searchResultCollectJob.cancel()
    }

    @Test
    fun `test error then retry returns success`() = runTest {
        val searchQueryCollectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.searchQuery.collect() }
        val searchResultCollectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.searchSuggestions.collect() }
        val errorCollectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.errorMessage.collect() }

        // First query fails
        searchRepository.generateError(true)
        viewModel.changeSearchQuery("test")
        advanceUntilIdle()

        assertFalse(viewModel.errorMessage.value.isNullOrEmpty())

        // Dismiss error, fix repo, retry
        viewModel.onErrorShown()
        searchRepository.generateError(false)

        // Trigger a new search by going back then searching again
        viewModel.onBack()
        viewModel.changeSearchQuery("test")
        advanceUntilIdle()

        assertEquals(testSearchResults, viewModel.searchSuggestions.value)
        assertNull(viewModel.errorMessage.value)

        searchQueryCollectJob.cancel()
        searchResultCollectJob.cancel()
        errorCollectJob.cancel()
    }
}
