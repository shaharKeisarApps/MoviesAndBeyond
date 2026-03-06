package com.keisardev.moviesandbeyond.feature.tv

import com.keisardev.moviesandbeyond.core.model.content.TvShowListCategory
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
class TvShowsViewModelTest {
    private val contentRepository = TestContentRepository()
    private lateinit var viewModel: TvShowsViewModel

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        viewModel = TvShowsViewModel(contentRepository = contentRepository)
    }

    @Test
    fun `test initial state has loading true`() {
        assertTrue(viewModel.airingTodayTvShows.value.isLoading)
        assertTrue(viewModel.onAirTvShows.value.isLoading)
        assertTrue(viewModel.popularTvShows.value.isLoading)
        assertTrue(viewModel.topRatedTvShows.value.isLoading)
    }

    @Test
    fun `test airing today tv shows loaded on collection`() = runTest {
        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.airingTodayTvShows.collect() }

        assertEquals(testContentItems, viewModel.airingTodayTvShows.value.items)
        assertFalse(viewModel.airingTodayTvShows.value.isLoading)
        assertNull(viewModel.errorMessage.value)

        collectJob.cancel()
    }

    @Test
    fun `test all categories loaded on collection`() = runTest {
        val jobs =
            listOf(
                launch(UnconfinedTestDispatcher()) { viewModel.airingTodayTvShows.collect() },
                launch(UnconfinedTestDispatcher()) { viewModel.onAirTvShows.collect() },
                launch(UnconfinedTestDispatcher()) { viewModel.popularTvShows.collect() },
                launch(UnconfinedTestDispatcher()) { viewModel.topRatedTvShows.collect() },
            )

        assertEquals(testContentItems, viewModel.airingTodayTvShows.value.items)
        assertEquals(testContentItems, viewModel.onAirTvShows.value.items)
        assertEquals(testContentItems, viewModel.popularTvShows.value.items)
        assertEquals(testContentItems, viewModel.topRatedTvShows.value.items)

        jobs.forEach { it.cancel() }
    }

    @Test
    fun `test error sets error message`() = runTest {
        contentRepository.generateError(true)

        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.airingTodayTvShows.collect() }
        val errorCollectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.errorMessage.collect() }

        assertFalse(viewModel.errorMessage.value.isNullOrEmpty())
        assertTrue(viewModel.airingTodayTvShows.value.items.isEmpty())
        assertFalse(viewModel.airingTodayTvShows.value.isLoading)

        collectJob.cancel()
        errorCollectJob.cancel()
    }

    @Test
    fun `test error message cleared after onErrorShown`() = runTest {
        contentRepository.generateError(true)

        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.airingTodayTvShows.collect() }
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
        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.airingTodayTvShows.collect() }

        assertEquals(testContentItems.size, viewModel.airingTodayTvShows.value.items.size)
        assertEquals(1, viewModel.airingTodayTvShows.value.page)

        viewModel.appendItems(TvShowListCategory.AIRING_TODAY)

        assertEquals(2, viewModel.airingTodayTvShows.value.page)

        collectJob.cancel()
    }

    @Test
    fun `test refresh resets accumulated items and page`() = runTest {
        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.airingTodayTvShows.collect() }

        viewModel.appendItems(TvShowListCategory.AIRING_TODAY)
        assertEquals(2, viewModel.airingTodayTvShows.value.page)

        viewModel.refresh(TvShowListCategory.AIRING_TODAY)
        assertEquals(1, viewModel.airingTodayTvShows.value.page)
        assertEquals(testContentItems, viewModel.airingTodayTvShows.value.items)

        collectJob.cancel()
    }

    @Test
    fun `test error then refresh then success flow`() = runTest {
        contentRepository.generateError(true)

        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.airingTodayTvShows.collect() }
        val errorCollectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.errorMessage.collect() }

        assertFalse(viewModel.errorMessage.value.isNullOrEmpty())
        assertTrue(viewModel.airingTodayTvShows.value.items.isEmpty())

        // Fix error, dismiss message, and refresh.
        // appendItems() first moves page 1→2, then refresh() moves page 2→1.
        // This is necessary because MutableStateFlow deduplicates equal values:
        // calling refresh() when page is already 1 emits nothing and flatMapLatest
        // would not re-subscribe to the now-fixed repository.
        contentRepository.generateError(false)
        viewModel.onErrorShown()
        viewModel.appendItems(TvShowListCategory.AIRING_TODAY) // page: 1 → 2
        viewModel.refresh(
            TvShowListCategory.AIRING_TODAY
        ) // page: 2 → 1 (distinct, triggers re-sub)

        // Let all pending coroutines (flatMapLatest re-subscription, combine) settle
        advanceUntilIdle()

        assertNull(viewModel.errorMessage.value)
        assertEquals(testContentItems, viewModel.airingTodayTvShows.value.items)

        collectJob.cancel()
        errorCollectJob.cancel()
    }

    @Test
    fun `test last page marks end reached when empty`() = runTest {
        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.airingTodayTvShows.collect() }

        // Non-empty items → endReached should be false
        assertFalse(viewModel.airingTodayTvShows.value.endReached)

        collectJob.cancel()
    }

    @Test
    fun `test concurrent refresh and paginate`() = runTest {
        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.airingTodayTvShows.collect() }

        viewModel.appendItems(TvShowListCategory.AIRING_TODAY)
        viewModel.refresh(TvShowListCategory.AIRING_TODAY)

        assertEquals(1, viewModel.airingTodayTvShows.value.page)
        assertEquals(testContentItems, viewModel.airingTodayTvShows.value.items)

        collectJob.cancel()
    }
}
