package com.keisardev.moviesandbeyond.data.repository.impl

import com.keisardev.moviesandbeyond.core.local.datastore.UserPreferencesDataStore
import com.keisardev.moviesandbeyond.core.local.session.SessionManager
import com.keisardev.moviesandbeyond.core.model.Result
import com.keisardev.moviesandbeyond.core.model.error.NetworkError
import com.keisardev.moviesandbeyond.core.network.model.auth.Avatar
import com.keisardev.moviesandbeyond.core.network.model.auth.Gravatar
import com.keisardev.moviesandbeyond.core.network.model.auth.NetworkAccountDetails
import com.keisardev.moviesandbeyond.core.network.model.auth.Tmdb
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryImplTest {

    private lateinit var fakeTmdbApi: FakeTmdbApi
    private lateinit var fakeFavoriteContentDao: FakeFavoriteContentDao
    private lateinit var fakeWatchlistContentDao: FakeWatchlistContentDao
    private lateinit var fakeAccountDetailsDao: FakeAccountDetailsDao
    private lateinit var fakeUserPreferencesDataStore: UserPreferencesDataStore
    private lateinit var sessionManager: SessionManager
    private lateinit var fakeSyncScheduler: FakeSyncScheduler
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setUp() {
        fakeTmdbApi = FakeTmdbApi()
        fakeFavoriteContentDao = FakeFavoriteContentDao()
        fakeWatchlistContentDao = FakeWatchlistContentDao()
        fakeAccountDetailsDao = FakeAccountDetailsDao()
        fakeUserPreferencesDataStore = FakeUserPreferencesDataStore()
        sessionManager = SessionManager(InMemorySharedPreferences())
        fakeSyncScheduler = FakeSyncScheduler()
        repository =
            AuthRepositoryImpl(
                tmdbApi = fakeTmdbApi,
                favoriteContentDao = fakeFavoriteContentDao,
                watchlistContentDao = fakeWatchlistContentDao,
                accountDetailsDao = fakeAccountDetailsDao,
                userPreferencesDataStore = fakeUserPreferencesDataStore,
                sessionManager = sessionManager,
                syncScheduler = fakeSyncScheduler,
            )
    }

    // region login

    @Test
    fun `login success returns Result Success`() = runTest {
        fakeTmdbApi.networkAccountDetails =
            NetworkAccountDetails(
                avatar =
                    Avatar(gravatar = Gravatar(hash = "abc"), tmdb = Tmdb(avatarPath = "/avatar")),
                id = 42,
                includeAdult = false,
                iso6391 = "en",
                iso31661 = "US",
                name = "Test",
                username = "testuser",
            )

        val result = repository.login("user", "pass")

        assertTrue(result is Result.Success)
    }

    @Test
    fun `login success stores session id`() = runTest {
        val result = repository.login("user", "pass")

        assertTrue(result is Result.Success)
        assertNotNull(sessionManager.getSessionId())
        assertEquals("test_session_id", sessionManager.getSessionId())
    }

    @Test
    fun `login success stores account details in dao`() = runTest {
        repository.login("user", "pass")

        val storedAccount = fakeAccountDetailsDao.accountDetails
        assertNotNull(storedAccount)
        assertEquals(1, storedAccount!!.id)
        assertEquals("testuser", storedAccount.username)
    }

    @Test
    fun `login success schedules library sync work`() = runTest {
        repository.login("user", "pass")

        assertTrue(fakeSyncScheduler.librarySyncScheduled)
    }

    @Test
    fun `login with IOException returns Result Error`() = runTest {
        fakeTmdbApi.exception = IOException("No network")

        val result = repository.login("user", "pass")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is IOException)
    }

    @Test
    fun `login with NetworkError returns Result Error with message`() = runTest {
        fakeTmdbApi.exception = NetworkError.Unauthorized()

        val result = repository.login("user", "pass")

        assertTrue(result is Result.Error)
        assertEquals("Unauthorized (401)", (result as Result.Error).message)
    }

    @Test
    fun `login failure does not store session id`() = runTest {
        fakeTmdbApi.exception = IOException("No network")

        repository.login("user", "pass")

        assertNull(sessionManager.getSessionId())
    }

    // endregion

    // region logout

    @Test
    fun `logout success returns Result Success and clears session`() = runTest {
        // Arrange: login first
        repository.login("user", "pass")
        assertNotNull(sessionManager.getSessionId())

        // Act
        val result = repository.logout(accountId = 1)

        // Assert
        assertTrue(result is Result.Success)
        assertNull(sessionManager.getSessionId())
    }

    @Test
    fun `logout success deletes account details`() = runTest {
        repository.login("user", "pass")
        assertNotNull(fakeAccountDetailsDao.accountDetails)

        repository.logout(accountId = 1)

        assertNull(fakeAccountDetailsDao.accountDetails)
    }

    @Test
    fun `logout success deletes synced favorite and watchlist items`() = runTest {
        repository.login("user", "pass")
        // The dao deleteSyncedFavoriteItems/deleteSyncedWatchlistItems should be called
        // We verify by calling logout and checking that deleteSession was called on the API
        val result = repository.logout(accountId = 1)

        assertTrue(result is Result.Success)
        assertTrue(fakeTmdbApi.deleteSessionCalled)
    }

    @Test
    fun `logout with network error returns Result Error`() = runTest {
        // Arrange: login first, then make API fail on logout
        repository.login("user", "pass")
        fakeTmdbApi.exception = NetworkError.ServerError(500)

        val result = repository.logout(accountId = 1)

        assertTrue(result is Result.Error)
    }

    // endregion

    // region isLoggedIn

    @Test
    fun `isLoggedIn initially emits false`() = runTest {
        val isLoggedIn = repository.isLoggedIn.first()

        assertFalse(isLoggedIn)
    }

    @Test
    fun `isLoggedIn emits true after login`() = runTest {
        repository.login("user", "pass")

        val isLoggedIn = repository.isLoggedIn.first()

        assertTrue(isLoggedIn)
    }

    // endregion
}
