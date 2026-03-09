package com.keisardev.moviesandbeyond

import com.keisardev.moviesandbeyond.core.model.MediaType
import com.keisardev.moviesandbeyond.core.model.SelectedDarkMode
import com.keisardev.moviesandbeyond.core.model.library.LibraryItemType
import com.keisardev.moviesandbeyond.core.testing.MainDispatcherRule
import com.keisardev.moviesandbeyond.data.testdoubles.repository.TestAuthRepository
import com.keisardev.moviesandbeyond.data.testdoubles.repository.TestUserRepository
import com.keisardev.moviesandbeyond.data.util.SyncScheduler
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
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
class MainActivityViewModelTest {
    private lateinit var userRepository: TestUserRepository
    private lateinit var authRepository: TestAuthRepository
    private lateinit var syncScheduler: FakeSyncScheduler

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        userRepository = TestUserRepository()
        authRepository = TestAuthRepository()
        syncScheduler = FakeSyncScheduler()
    }

    private fun createViewModel(): MainActivityViewModel =
        MainActivityViewModel(
            userRepository = userRepository,
            authRepository = authRepository,
            syncScheduler = syncScheduler,
        )

    @Test
    fun `initial uiState is Loading`() {
        val viewModel = createViewModel()
        assertEquals(MainActivityUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `uiState transitions to Success when userData emits`() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        assertTrue(viewModel.uiState.value is MainActivityUiState.Success)

        collectJob.cancel()
    }

    @Test
    fun `Success state contains correct useDynamicColor from preferences`() = runTest {
        userRepository.setDynamicColorPreference(true)
        val viewModel = createViewModel()
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        val state = viewModel.uiState.value as MainActivityUiState.Success
        assertTrue(state.useDynamicColor)

        collectJob.cancel()
    }

    @Test
    fun `Success state contains correct darkMode from preferences`() = runTest {
        userRepository.setDarkModePreference(SelectedDarkMode.DARK)
        val viewModel = createViewModel()
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        val state = viewModel.uiState.value as MainActivityUiState.Success
        assertEquals(SelectedDarkMode.DARK, state.darkMode)

        collectJob.cancel()
    }

    @Test
    fun `Success state contains correct hideOnboarding from preferences`() = runTest {
        userRepository.setHideOnboarding(true)
        val viewModel = createViewModel()
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        val state = viewModel.uiState.value as MainActivityUiState.Success
        assertTrue(state.hideOnboarding)

        collectJob.cancel()
    }

    @Test
    fun `Success state contains correct customColorArgb from preferences`() = runTest {
        val customColor = 0xFF00FF00L
        userRepository.setCustomColorArgb(customColor)
        val viewModel = createViewModel()
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        val state = viewModel.uiState.value as MainActivityUiState.Success
        assertEquals(customColor, state.customColorArgb)

        collectJob.cancel()
    }

    @Test
    fun `executeLibrarySyncWork calls syncScheduler when logged in`() = runTest {
        authRepository.setAuthStatus(true)
        createViewModel()

        assertTrue(syncScheduler.scheduleLibrarySyncWorkCalled)
    }

    @Test
    fun `executeLibrarySyncWork does not call syncScheduler when logged out`() = runTest {
        authRepository.setAuthStatus(false)
        createViewModel()

        assertFalse(syncScheduler.scheduleLibrarySyncWorkCalled)
    }
}

/** Fake [SyncScheduler] that records calls for verification. */
private class FakeSyncScheduler : SyncScheduler {
    var scheduleLibrarySyncWorkCalled = false
        private set

    override fun scheduleLibrarySyncWork() {
        scheduleLibrarySyncWorkCalled = true
    }

    override fun scheduleLibraryTaskWork(
        libraryTask: com.keisardev.moviesandbeyond.core.model.library.LibraryTask
    ) {
        // no-op for tests
    }

    override fun isWorkNotScheduled(
        mediaId: Int,
        mediaType: MediaType,
        itemType: LibraryItemType,
    ): Boolean = true
}
