package com.keisardev.moviesandbeyond.feature.you.library_items

import androidx.lifecycle.SavedStateHandle
import com.keisardev.moviesandbeyond.core.model.library.LibraryItemType
import com.keisardev.moviesandbeyond.core.testing.MainDispatcherRule
import com.keisardev.moviesandbeyond.data.testdoubles.repository.TestAuthRepository
import com.keisardev.moviesandbeyond.data.testdoubles.repository.TestLibraryRepository
import com.keisardev.moviesandbeyond.feature.you.libraryItemTypeNavigationArgument
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/**
 * Integration test for Navigation 3 parameter passing to LibraryItemsViewModel.
 *
 * This test reproduces the bug where:
 * 1. User navigates from YouScreen to LibraryItemsScreen with type="FAVORITE"
 * 2. Navigation 3 creates LibraryItemsRoute(type="FAVORITE")
 * 3. The type parameter must be extracted and passed to LibraryItemsRoute composable
 * 4. LibraryItemsRoute calls viewModel.setLibraryItemType(type)
 * 5. ViewModel should emit the correct items based on type
 *
 * If the type is not passed (or is null/empty), the ViewModel emits empty lists.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LibraryItemsNavigationIntegrationTest {
    private val libraryRepository = TestLibraryRepository()
    private val authRepository = TestAuthRepository()

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `navigation with FAVORITE type parameter - shows favorite items not empty list`() =
        runTest {
            // Simulate Navigation 3 flow:
            // 1. User clicks "Favorites" in YouScreen
            // 2. Navigation creates LibraryItemsRoute(type="FAVORITE")
            // 3. NavDisplay calls entry<LibraryItemsRoute> { key -> ... }
            // 4. We extract key.type and pass to LibraryItemsScreen(libraryItemType = key.type)

            val viewModel = createViewModelWithEmptyState()

            // This simulates what LibraryItemsRoute composable does when receiving the parameter
            viewModel.setLibraryItemType(LibraryItemType.FAVORITE.name)

            val libraryItemTypeCollectJob =
                launch(UnconfinedTestDispatcher()) { viewModel.libraryItemType.collect() }
            val moviesCollectJob =
                launch(UnconfinedTestDispatcher()) { viewModel.movieItems.collect() }
            val tvShowsCollectJob =
                launch(UnconfinedTestDispatcher()) { viewModel.tvItems.collect() }

            // Verify type is set correctly
            assertNotNull("Library item type should not be null", viewModel.libraryItemType.value)
            assertEquals(LibraryItemType.FAVORITE, viewModel.libraryItemType.value)

            // Verify items are loaded (not empty)
            assertEquals(
                "Should show favorite movies, not empty list", 1, viewModel.movieItems.value.size)
            assertEquals(
                "Should show favorite TV shows, not empty list", 1, viewModel.tvItems.value.size)

            libraryItemTypeCollectJob.cancel()
            moviesCollectJob.cancel()
            tvShowsCollectJob.cancel()
        }

    @Test
    fun `navigation with WATCHLIST type parameter - shows watchlist items not empty list`() =
        runTest {
            // Simulate Navigation 3 flow for Watchlist
            val viewModel = createViewModelWithEmptyState()

            // This simulates what LibraryItemsRoute composable does
            viewModel.setLibraryItemType(LibraryItemType.WATCHLIST.name)

            val libraryItemTypeCollectJob =
                launch(UnconfinedTestDispatcher()) { viewModel.libraryItemType.collect() }
            val moviesCollectJob =
                launch(UnconfinedTestDispatcher()) { viewModel.movieItems.collect() }
            val tvShowsCollectJob =
                launch(UnconfinedTestDispatcher()) { viewModel.tvItems.collect() }

            // Verify type is set correctly
            assertNotNull("Library item type should not be null", viewModel.libraryItemType.value)
            assertEquals(LibraryItemType.WATCHLIST, viewModel.libraryItemType.value)

            // Verify items are loaded (not empty)
            assertEquals(
                "Should show watchlist movies, not empty list", 1, viewModel.movieItems.value.size)
            assertEquals(
                "Should show watchlist TV shows, not empty list", 1, viewModel.tvItems.value.size)

            libraryItemTypeCollectJob.cancel()
            moviesCollectJob.cancel()
            tvShowsCollectJob.cancel()
        }

    @Test
    fun `BUG SCENARIO - missing type parameter results in empty lists`() = runTest {
        // This test reproduces the bug that was happening:
        // 1. Navigation 3 entry didn't extract key.type
        // 2. LibraryItemsRoute was called with libraryItemType = null
        // 3. setLibraryItemType was never called
        // 4. ViewModel emitted empty lists

        val viewModel = createViewModelWithEmptyState()

        // Bug: setLibraryItemType is NOT called (type parameter was missing)
        // This is what was happening before the fix

        val libraryItemTypeCollectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.libraryItemType.collect() }
        val moviesCollectJob = launch(UnconfinedTestDispatcher()) { viewModel.movieItems.collect() }
        val tvShowsCollectJob = launch(UnconfinedTestDispatcher()) { viewModel.tvItems.collect() }

        // Verify type is null (bug symptom)
        assertEquals(
            "Without setLibraryItemType call, type should be null",
            null,
            viewModel.libraryItemType.value)

        // Verify empty lists (bug symptom)
        assertEquals(
            "Bug: Movies list is empty because type is null", 0, viewModel.movieItems.value.size)
        assertEquals(
            "Bug: TV shows list is empty because type is null", 0, viewModel.tvItems.value.size)

        libraryItemTypeCollectJob.cancel()
        moviesCollectJob.cancel()
        tvShowsCollectJob.cancel()
    }

    /**
     * Create ViewModel with empty SavedStateHandle, simulating Navigation 3 scenario where the type
     * must be set via setLibraryItemType() function.
     */
    private fun createViewModelWithEmptyState() =
        LibraryItemsViewModel(
            savedStateHandle = SavedStateHandle(), // Empty - no type set yet
            libraryRepository = libraryRepository,
            authRepository = authRepository)

    /** Create ViewModel with pre-populated type, simulating traditional navigation argument. */
    private fun createViewModelWithType(type: String) =
        LibraryItemsViewModel(
            savedStateHandle = SavedStateHandle(mapOf(libraryItemTypeNavigationArgument to type)),
            libraryRepository = libraryRepository,
            authRepository = authRepository)
}
