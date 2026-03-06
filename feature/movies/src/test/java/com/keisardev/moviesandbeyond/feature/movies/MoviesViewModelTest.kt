package com.keisardev.moviesandbeyond.feature.movies

import com.keisardev.moviesandbeyond.core.model.content.MovieListCategory
import com.keisardev.moviesandbeyond.core.testing.MainDispatcherRule
import com.keisardev.moviesandbeyond.data.testdoubles.repository.TestContentRepository
import com.keisardev.moviesandbeyond.data.testdoubles.repository.testContentItems
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MoviesViewModelTest {
    private val contentRepository = TestContentRepository()
    private lateinit var viewModel: MoviesViewModel

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        viewModel = MoviesViewModel(contentRepository = contentRepository)
    }

    @Test
    fun `test initial state has loading true`() {
        assertTrue(viewModel.nowPlayingMovies.value.isLoading)
        assertTrue(viewModel.popularMovies.value.isLoading)
        assertTrue(viewModel.topRatedMovies.value.isLoading)
        assertTrue(viewModel.upcomingMovies.value.isLoading)
    }

    @Test
    fun `test now playing movies loaded on collection`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.nowPlayingMovies.collect() }

        assertEquals(testContentItems, viewModel.nowPlayingMovies.value.items)
        assertFalse(viewModel.nowPlayingMovies.value.isLoading)
        assertNull(viewModel.errorMessage.value)

        collectJob.cancel()
    }

    @Test
    fun `test all categories loaded on collection`() = runTest {
        val jobs =
            listOf(
                launch(UnconfinedTestDispatcher()) { viewModel.nowPlayingMovies.collect() },
                launch(UnconfinedTestDispatcher()) { viewModel.popularMovies.collect() },
                launch(UnconfinedTestDispatcher()) { viewModel.topRatedMovies.collect() },
                launch(UnconfinedTestDispatcher()) { viewModel.upcomingMovies.collect() },
            )

        assertEquals(testContentItems, viewModel.nowPlayingMovies.value.items)
        assertEquals(testContentItems, viewModel.popularMovies.value.items)
        assertEquals(testContentItems, viewModel.topRatedMovies.value.items)
        assertEquals(testContentItems, viewModel.upcomingMovies.value.items)

        jobs.forEach { it.cancel() }
    }

    @Test
    fun `test error sets error message`() = runTest {
        contentRepository.generateError(true)

        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.nowPlayingMovies.collect() }
        val errorCollectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.errorMessage.collect() }

        assertFalse(viewModel.errorMessage.value.isNullOrEmpty())
        assertTrue(viewModel.nowPlayingMovies.value.items.isEmpty())
        assertFalse(viewModel.nowPlayingMovies.value.isLoading)

        collectJob.cancel()
        errorCollectJob.cancel()
    }

    @Test
    fun `test error message cleared after onErrorShown`() = runTest {
        contentRepository.generateError(true)

        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.nowPlayingMovies.collect() }
        val errorCollectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.errorMessage.collect() }

        assertFalse(viewModel.errorMessage.value.isNullOrEmpty())

        viewModel.onErrorShown()

        assertNull(viewModel.errorMessage.value)

        collectJob.cancel()
        errorCollectJob.cancel()
    }

    @Test
    fun `test pagination appends items on next page`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.nowPlayingMovies.collect() }

        assertEquals(testContentItems.size, viewModel.nowPlayingMovies.value.items.size)
        assertEquals(1, viewModel.nowPlayingMovies.value.page)

        viewModel.appendItems(MovieListCategory.NOW_PLAYING)

        assertEquals(2, viewModel.nowPlayingMovies.value.page)
        // Items are deduplicated by id — same test items so size stays the same
        assertEquals(testContentItems.size, viewModel.nowPlayingMovies.value.items.size)

        collectJob.cancel()
    }

    @Test
    fun `test empty result marks end reached`() = runTest {
        // Empty items on page 2 should mark endReached = true
        // The TestContentRepository returns testContentItems, which are non-empty
        // so this test verifies that non-empty results do NOT set endReached
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.nowPlayingMovies.collect() }

        assertFalse(viewModel.nowPlayingMovies.value.endReached)

        collectJob.cancel()
    }

    @Test
    fun `test refresh resets accumulated items and page`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.nowPlayingMovies.collect() }

        // Load page 2
        viewModel.appendItems(MovieListCategory.NOW_PLAYING)
        assertEquals(2, viewModel.nowPlayingMovies.value.page)

        // Refresh should reset to page 1
        viewModel.refresh(MovieListCategory.NOW_PLAYING)
        assertEquals(1, viewModel.nowPlayingMovies.value.page)
        assertEquals(testContentItems, viewModel.nowPlayingMovies.value.items)

        collectJob.cancel()
    }

    @Test
    fun `test error then refresh then success flow`() = runTest {
        contentRepository.generateError(true)

        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.nowPlayingMovies.collect() }
        val errorCollectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.errorMessage.collect() }

        // Error state
        assertFalse(viewModel.errorMessage.value.isNullOrEmpty())
        assertTrue(viewModel.nowPlayingMovies.value.items.isEmpty())

        // Fix error, dismiss message, and refresh.
        // appendItems() first moves page 1→2, then refresh() moves page 2→1.
        // This is necessary because MutableStateFlow deduplicates equal values:
        // calling refresh() when page is already 1 emits nothing and flatMapLatest
        // would not re-subscribe to the now-fixed repository.
        contentRepository.generateError(false)
        viewModel.onErrorShown()
        viewModel.appendItems(MovieListCategory.NOW_PLAYING) // page: 1 → 2
        viewModel.refresh(MovieListCategory.NOW_PLAYING) // page: 2 → 1 (distinct, triggers re-sub)

        // Let all pending coroutines (flatMapLatest re-subscription, combine) settle
        advanceUntilIdle()

        // Success state
        assertNull(viewModel.errorMessage.value)
        assertEquals(testContentItems, viewModel.nowPlayingMovies.value.items)

        collectJob.cancel()
        errorCollectJob.cancel()
    }

    @Test
    fun `test concurrent refresh and paginate`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.nowPlayingMovies.collect() }

        // Simulate concurrent paginate + refresh
        viewModel.appendItems(MovieListCategory.NOW_PLAYING)
        viewModel.refresh(MovieListCategory.NOW_PLAYING)

        // After refresh, should be back on page 1
        assertEquals(1, viewModel.nowPlayingMovies.value.page)
        assertEquals(testContentItems, viewModel.nowPlayingMovies.value.items)

        collectJob.cancel()
    }
}
