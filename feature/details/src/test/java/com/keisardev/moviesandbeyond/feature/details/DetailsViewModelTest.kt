package com.keisardev.moviesandbeyond.feature.details

import androidx.lifecycle.SavedStateHandle
import com.keisardev.moviesandbeyond.core.model.MediaType
import com.keisardev.moviesandbeyond.core.model.NetworkResponse
import com.keisardev.moviesandbeyond.core.testing.MainDispatcherRule
import com.keisardev.moviesandbeyond.data.testdoubles.repository.TestDetailsRepository
import com.keisardev.moviesandbeyond.data.testdoubles.repository.TestLibraryRepository
import com.keisardev.moviesandbeyond.data.testdoubles.testLibraryItems
import com.keisardev.moviesandbeyond.data.testdoubles.testMovieDetail
import com.keisardev.moviesandbeyond.data.testdoubles.testPersonDetails
import com.keisardev.moviesandbeyond.data.testdoubles.testTvShowDetails
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {
    private val detailsRepository = TestDetailsRepository()
    private val libraryRepository = TestLibraryRepository()
    private lateinit var viewModel: DetailsViewModel

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        viewModel = createViewModel()
    }

    @Test
    fun `test initial state`() = runTest { assertEquals(DetailsUiState(), viewModel.uiState.value) }

    @Test
    fun `test empty content state`() = runTest {
        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.contentDetailsUiState.collect() }

        assertEquals(ContentDetailUiState.Empty, viewModel.contentDetailsUiState.value)

        collectJob.cancel()
    }

    @Test
    fun `test movie details content state`() = runTest {
        viewModel = createViewModel(navigationArgument = "100,${MediaType.MOVIE}")

        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.contentDetailsUiState.collect() }

        assertEquals(
            ContentDetailUiState.Movie(data = testMovieDetail),
            viewModel.contentDetailsUiState.value)

        collectJob.cancel()
    }

    @Test
    fun `test tv show details content state`() = runTest {
        viewModel = createViewModel(navigationArgument = "101,${MediaType.TV}")

        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.contentDetailsUiState.collect() }

        assertEquals(
            ContentDetailUiState.TV(data = testTvShowDetails),
            viewModel.contentDetailsUiState.value)

        collectJob.cancel()
    }

    @Test
    fun `test person details content state`() = runTest {
        viewModel = createViewModel(navigationArgument = "102,${MediaType.PERSON}")

        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.contentDetailsUiState.collect() }

        assertEquals(
            ContentDetailUiState.Person(data = testPersonDetails),
            viewModel.contentDetailsUiState.value)

        collectJob.cancel()
    }

    @Test
    fun `test error in movie details content state`() = runTest {
        detailsRepository.generateError(true)
        val errorResponse = detailsRepository.getMovieDetails(0) as NetworkResponse.Error
        viewModel = createViewModel(navigationArgument = "100,${MediaType.MOVIE}")

        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.contentDetailsUiState.collect() }

        assertEquals(ContentDetailUiState.Empty, viewModel.contentDetailsUiState.value)

        assertEquals(errorResponse.errorMessage, viewModel.uiState.value.errorMessage)

        collectJob.cancel()
    }

    @Test
    fun `test error in tv show details content state`() = runTest {
        detailsRepository.generateError(true)
        val errorResponse = detailsRepository.getTvShowDetails(0) as NetworkResponse.Error
        viewModel = createViewModel(navigationArgument = "101,${MediaType.TV}")

        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.contentDetailsUiState.collect() }

        assertEquals(ContentDetailUiState.Empty, viewModel.contentDetailsUiState.value)

        assertEquals(errorResponse.errorMessage, viewModel.uiState.value.errorMessage)

        collectJob.cancel()
    }

    @Test
    fun `test error in person details content state`() = runTest {
        detailsRepository.generateError(true)
        val errorResponse = detailsRepository.getPersonDetails(0) as NetworkResponse.Error
        viewModel = createViewModel(navigationArgument = "102,${MediaType.PERSON}")

        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.contentDetailsUiState.collect() }

        assertEquals(ContentDetailUiState.Empty, viewModel.contentDetailsUiState.value)

        assertEquals(errorResponse.errorMessage, viewModel.uiState.value.errorMessage)

        collectJob.cancel()
    }

    @Test
    fun `test favorite toggles locally without login`() = runTest {
        val libraryItem = testLibraryItems[0].copy(id = 0)

        // Test that favorite can be toggled without login (local-only storage)
        viewModel.addOrRemoveFavorite(libraryItem)
        assertTrue(viewModel.uiState.value.markedFavorite)

        // Toggle back
        viewModel.addOrRemoveFavorite(libraryItem)
        assertFalse(viewModel.uiState.value.markedFavorite)
    }

    @Test
    fun `test watchlist toggles locally without login`() = runTest {
        val libraryItem = testLibraryItems[0].copy(id = 0)

        // Test that watchlist can be toggled without login (local-only storage)
        viewModel.addOrRemoveFromWatchlist(libraryItem)
        assertTrue(viewModel.uiState.value.savedInWatchlist)

        // Toggle back
        viewModel.addOrRemoveFromWatchlist(libraryItem)
        assertFalse(viewModel.uiState.value.savedInWatchlist)
    }

    @Test
    fun `test error message reset`() {
        viewModel.onErrorShown()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `test bottom sheet reset`() {
        viewModel.onHideBottomSheet()

        assertFalse(viewModel.uiState.value.showSignInSheet)
    }

    private fun createViewModel(navigationArgument: String = "") =
        DetailsViewModel(
            savedStateHandle = SavedStateHandle(mapOf(idNavigationArgument to navigationArgument)),
            detailsRepository = detailsRepository,
            libraryRepository = libraryRepository)
}
