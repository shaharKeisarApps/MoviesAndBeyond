package com.keisardev.moviesandbeyond.data.repository.impl

import com.keisardev.moviesandbeyond.core.local.database.entity.AccountDetailsEntity
import com.keisardev.moviesandbeyond.core.local.database.entity.FavoriteContentEntity
import com.keisardev.moviesandbeyond.core.local.database.entity.WatchlistContentEntity
import com.keisardev.moviesandbeyond.core.model.MediaType
import com.keisardev.moviesandbeyond.core.model.library.LibraryItem
import com.keisardev.moviesandbeyond.core.model.library.LibraryItemType
import com.keisardev.moviesandbeyond.core.model.library.SyncStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryRepositoryImplTest {

    private lateinit var fakeTmdbApi: FakeTmdbApi
    private lateinit var fakeFavoriteContentDao: FakeFavoriteContentDao
    private lateinit var fakeWatchlistContentDao: FakeWatchlistContentDao
    private lateinit var fakeAccountDetailsDao: FakeAccountDetailsDao
    private lateinit var fakeSyncScheduler: FakeSyncScheduler
    private lateinit var repository: LibraryRepositoryImpl

    @Before
    fun setUp() {
        fakeTmdbApi = FakeTmdbApi()
        fakeFavoriteContentDao = FakeFavoriteContentDao()
        fakeWatchlistContentDao = FakeWatchlistContentDao()
        fakeAccountDetailsDao = FakeAccountDetailsDao()
        fakeSyncScheduler = FakeSyncScheduler()
        repository =
            LibraryRepositoryImpl(
                tmdbApi = fakeTmdbApi,
                favoriteContentDao = fakeFavoriteContentDao,
                watchlistContentDao = fakeWatchlistContentDao,
                accountDetailsDao = fakeAccountDetailsDao,
                syncScheduler = fakeSyncScheduler,
            )
    }

    // region favoriteMovies flow

    @Test
    fun `favoriteMovies emits empty list initially`() = runTest {
        val movies = repository.favoriteMovies.first()

        assertTrue(movies.isEmpty())
    }

    @Test
    fun `favoriteMovies emits items after adding a movie favorite`() = runTest {
        val item =
            LibraryItem(id = 1, imagePath = "/poster.jpg", name = "Movie", mediaType = "movie")

        repository.addOrRemoveFavorite(item, isAuthenticated = false)

        val movies = repository.favoriteMovies.first()
        assertEquals(1, movies.size)
        assertEquals("Movie", movies[0].name)
    }

    @Test
    fun `favoriteMovies does not include tv shows`() = runTest {
        val movie =
            LibraryItem(id = 1, imagePath = "/poster.jpg", name = "Movie", mediaType = "movie")
        val tvShow =
            LibraryItem(id = 2, imagePath = "/poster.jpg", name = "TV Show", mediaType = "tv")

        repository.addOrRemoveFavorite(movie, isAuthenticated = false)
        repository.addOrRemoveFavorite(tvShow, isAuthenticated = false)

        val movies = repository.favoriteMovies.first()
        assertEquals(1, movies.size)
        assertEquals("Movie", movies[0].name)
    }

    // endregion

    // region favoriteTvShows flow

    @Test
    fun `favoriteTvShows emits tv shows only`() = runTest {
        val movie =
            LibraryItem(id = 1, imagePath = "/poster.jpg", name = "Movie", mediaType = "movie")
        val tvShow =
            LibraryItem(id = 2, imagePath = "/poster.jpg", name = "TV Show", mediaType = "tv")

        repository.addOrRemoveFavorite(movie, isAuthenticated = false)
        repository.addOrRemoveFavorite(tvShow, isAuthenticated = false)

        val tvShows = repository.favoriteTvShows.first()
        assertEquals(1, tvShows.size)
        assertEquals("TV Show", tvShows[0].name)
    }

    // endregion

    // region moviesWatchlist flow

    @Test
    fun `moviesWatchlist emits items after adding to watchlist`() = runTest {
        val item =
            LibraryItem(id = 1, imagePath = "/poster.jpg", name = "Movie", mediaType = "movie")

        repository.addOrRemoveFromWatchlist(item, isAuthenticated = false)

        val movies = repository.moviesWatchlist.first()
        assertEquals(1, movies.size)
        assertEquals("Movie", movies[0].name)
    }

    // endregion

    // region itemInFavoritesExists

    @Test
    fun `itemInFavoritesExists returns false when item not present`() = runTest {
        val exists = repository.itemInFavoritesExists(mediaId = 1, mediaType = MediaType.MOVIE)

        assertFalse(exists)
    }

    @Test
    fun `itemInFavoritesExists returns true when item is present`() = runTest {
        val item =
            LibraryItem(id = 1, imagePath = "/poster.jpg", name = "Movie", mediaType = "movie")
        repository.addOrRemoveFavorite(item, isAuthenticated = false)

        val exists = repository.itemInFavoritesExists(mediaId = 1, mediaType = MediaType.MOVIE)

        assertTrue(exists)
    }

    // endregion

    // region itemInWatchlistExists

    @Test
    fun `itemInWatchlistExists returns false when item not present`() = runTest {
        val exists = repository.itemInWatchlistExists(mediaId = 1, mediaType = MediaType.MOVIE)

        assertFalse(exists)
    }

    @Test
    fun `itemInWatchlistExists returns true when item is present`() = runTest {
        val item =
            LibraryItem(id = 1, imagePath = "/poster.jpg", name = "Movie", mediaType = "movie")
        repository.addOrRemoveFromWatchlist(item, isAuthenticated = false)

        val exists = repository.itemInWatchlistExists(mediaId = 1, mediaType = MediaType.MOVIE)

        assertTrue(exists)
    }

    // endregion

    // region addOrRemoveFavorite - guest mode

    @Test
    fun `addOrRemoveFavorite in guest mode adds item with LOCAL_ONLY status`() = runTest {
        val item =
            LibraryItem(id = 1, imagePath = "/poster.jpg", name = "Movie", mediaType = "movie")

        repository.addOrRemoveFavorite(item, isAuthenticated = false)

        val status = repository.getFavoriteSyncStatus(mediaId = 1, mediaType = MediaType.MOVIE)
        assertEquals(SyncStatus.LOCAL_ONLY, status)
    }

    @Test
    fun `addOrRemoveFavorite in guest mode removes existing item`() = runTest {
        val item =
            LibraryItem(id = 1, imagePath = "/poster.jpg", name = "Movie", mediaType = "movie")

        // Add then remove
        repository.addOrRemoveFavorite(item, isAuthenticated = false)
        assertTrue(repository.itemInFavoritesExists(mediaId = 1, mediaType = MediaType.MOVIE))

        repository.addOrRemoveFavorite(item, isAuthenticated = false)
        assertFalse(repository.itemInFavoritesExists(mediaId = 1, mediaType = MediaType.MOVIE))
    }

    @Test
    fun `addOrRemoveFavorite in guest mode does not schedule sync`() = runTest {
        val item =
            LibraryItem(id = 1, imagePath = "/poster.jpg", name = "Movie", mediaType = "movie")

        repository.addOrRemoveFavorite(item, isAuthenticated = false)

        assertTrue(fakeSyncScheduler.scheduledTasks.isEmpty())
    }

    // endregion

    // region addOrRemoveFavorite - authenticated mode

    @Test
    fun `addOrRemoveFavorite authenticated adds item with PENDING_PUSH status`() = runTest {
        val item =
            LibraryItem(id = 1, imagePath = "/poster.jpg", name = "Movie", mediaType = "movie")

        repository.addOrRemoveFavorite(item, isAuthenticated = true)

        val status = repository.getFavoriteSyncStatus(mediaId = 1, mediaType = MediaType.MOVIE)
        assertEquals(SyncStatus.PENDING_PUSH, status)
    }

    @Test
    fun `addOrRemoveFavorite authenticated schedules sync task`() = runTest {
        val item =
            LibraryItem(id = 1, imagePath = "/poster.jpg", name = "Movie", mediaType = "movie")

        repository.addOrRemoveFavorite(item, isAuthenticated = true)

        assertEquals(1, fakeSyncScheduler.scheduledTasks.size)
        val task = fakeSyncScheduler.scheduledTasks[0]
        assertEquals(1, task.mediaId)
        assertEquals(MediaType.MOVIE, task.mediaType)
        assertEquals(LibraryItemType.FAVORITE, task.itemType)
        assertTrue(task.itemExistLocally)
    }

    @Test
    fun `addOrRemoveFavorite authenticated removes existing by marking for deletion`() = runTest {
        val item =
            LibraryItem(id = 1, imagePath = "/poster.jpg", name = "Movie", mediaType = "movie")

        repository.addOrRemoveFavorite(item, isAuthenticated = true)
        // Item exists now; remove it
        repository.addOrRemoveFavorite(item, isAuthenticated = true)

        // Should be marked PENDING_DELETE, so not visible in favorites
        assertFalse(repository.itemInFavoritesExists(mediaId = 1, mediaType = MediaType.MOVIE))
        assertEquals(2, fakeSyncScheduler.scheduledTasks.size)
    }

    // endregion

    // region addOrRemoveFromWatchlist - guest mode

    @Test
    fun `addOrRemoveFromWatchlist in guest mode adds with LOCAL_ONLY status`() = runTest {
        val item =
            LibraryItem(id = 1, imagePath = "/poster.jpg", name = "Movie", mediaType = "movie")

        repository.addOrRemoveFromWatchlist(item, isAuthenticated = false)

        val status = repository.getWatchlistSyncStatus(mediaId = 1, mediaType = MediaType.MOVIE)
        assertEquals(SyncStatus.LOCAL_ONLY, status)
    }

    @Test
    fun `addOrRemoveFromWatchlist in guest mode toggles item`() = runTest {
        val item =
            LibraryItem(id = 1, imagePath = "/poster.jpg", name = "Movie", mediaType = "movie")

        repository.addOrRemoveFromWatchlist(item, isAuthenticated = false)
        assertTrue(repository.itemInWatchlistExists(mediaId = 1, mediaType = MediaType.MOVIE))

        repository.addOrRemoveFromWatchlist(item, isAuthenticated = false)
        assertFalse(repository.itemInWatchlistExists(mediaId = 1, mediaType = MediaType.MOVIE))
    }

    // endregion

    // region addOrRemoveFromWatchlist - authenticated mode

    @Test
    fun `addOrRemoveFromWatchlist authenticated schedules sync task`() = runTest {
        val item =
            LibraryItem(id = 1, imagePath = "/poster.jpg", name = "TV Show", mediaType = "tv")

        repository.addOrRemoveFromWatchlist(item, isAuthenticated = true)

        assertEquals(1, fakeSyncScheduler.scheduledTasks.size)
        val task = fakeSyncScheduler.scheduledTasks[0]
        assertEquals(1, task.mediaId)
        assertEquals(MediaType.TV, task.mediaType)
        assertEquals(LibraryItemType.WATCHLIST, task.itemType)
    }

    // endregion

    // region executeLibraryTask

    @Test
    fun `executeLibraryTask favorite success returns true`() = runTest {
        fakeAccountDetailsDao.accountDetails =
            AccountDetailsEntity(
                id = 42,
                gravatarHash = "hash",
                includeAdult = false,
                iso6391 = "en",
                iso31661 = "US",
                name = "Test",
                tmdbAvatarPath = null,
                username = "testuser",
            )
        // Pre-insert item so sync status update can find it
        fakeFavoriteContentDao.insertFavoriteItem(
            FavoriteContentEntity(
                mediaId = 1,
                mediaType = "movie",
                imagePath = "/poster.jpg",
                name = "Movie",
                syncStatus = SyncStatus.PENDING_PUSH,
            )
        )

        val result =
            repository.executeLibraryTask(
                id = 1,
                mediaType = MediaType.MOVIE,
                libraryItemType = LibraryItemType.FAVORITE,
                itemExistsLocally = true,
            )

        assertTrue(result)
        assertTrue(fakeTmdbApi.addOrRemoveFavoriteCalled)
    }

    @Test
    fun `executeLibraryTask watchlist success returns true`() = runTest {
        fakeAccountDetailsDao.accountDetails =
            AccountDetailsEntity(
                id = 42,
                gravatarHash = "hash",
                includeAdult = false,
                iso6391 = "en",
                iso31661 = "US",
                name = "Test",
                tmdbAvatarPath = null,
                username = "testuser",
            )
        fakeWatchlistContentDao.insertWatchlistItem(
            WatchlistContentEntity(
                mediaId = 1,
                mediaType = "movie",
                imagePath = "/poster.jpg",
                name = "Movie",
                syncStatus = SyncStatus.PENDING_PUSH,
            )
        )

        val result =
            repository.executeLibraryTask(
                id = 1,
                mediaType = MediaType.MOVIE,
                libraryItemType = LibraryItemType.WATCHLIST,
                itemExistsLocally = true,
            )

        assertTrue(result)
        assertTrue(fakeTmdbApi.addOrRemoveFromWatchlistCalled)
    }

    @Test
    fun `executeLibraryTask returns false when no account details`() = runTest {
        val result =
            repository.executeLibraryTask(
                id = 1,
                mediaType = MediaType.MOVIE,
                libraryItemType = LibraryItemType.FAVORITE,
                itemExistsLocally = true,
            )

        assertFalse(result)
    }

    @Test
    fun `executeLibraryTask returns false on IOException`() = runTest {
        fakeAccountDetailsDao.accountDetails =
            AccountDetailsEntity(
                id = 42,
                gravatarHash = "hash",
                includeAdult = false,
                iso6391 = "en",
                iso31661 = "US",
                name = "Test",
                tmdbAvatarPath = null,
                username = "testuser",
            )
        fakeTmdbApi.exception = java.io.IOException("Network error")

        val result =
            repository.executeLibraryTask(
                id = 1,
                mediaType = MediaType.MOVIE,
                libraryItemType = LibraryItemType.FAVORITE,
                itemExistsLocally = true,
            )

        assertFalse(result)
    }

    // endregion

    // region getFavoriteSyncStatus / getWatchlistSyncStatus

    @Test
    fun `getFavoriteSyncStatus returns null when item not found`() = runTest {
        val status = repository.getFavoriteSyncStatus(mediaId = 999, mediaType = MediaType.MOVIE)

        assertNull(status)
    }

    @Test
    fun `getWatchlistSyncStatus returns null when item not found`() = runTest {
        val status = repository.getWatchlistSyncStatus(mediaId = 999, mediaType = MediaType.MOVIE)

        assertNull(status)
    }

    @Test
    fun `getWatchlistSyncStatus returns correct status`() = runTest {
        val item =
            LibraryItem(id = 1, imagePath = "/poster.jpg", name = "Movie", mediaType = "movie")
        repository.addOrRemoveFromWatchlist(item, isAuthenticated = true)

        val status = repository.getWatchlistSyncStatus(mediaId = 1, mediaType = MediaType.MOVIE)

        assertEquals(SyncStatus.PENDING_PUSH, status)
    }

    // endregion

    // region syncLocalItemsWithTmdb

    @Test
    fun `syncLocalItemsWithTmdb returns error when not authenticated`() = runTest {
        val result = repository.syncLocalItemsWithTmdb()

        assertEquals(0, result.pushed)
        assertEquals(listOf("Not authenticated"), result.errors)
    }

    @Test
    fun `syncLocalItemsWithTmdb pushes pending items`() = runTest {
        fakeAccountDetailsDao.accountDetails =
            AccountDetailsEntity(
                id = 42,
                gravatarHash = "hash",
                includeAdult = false,
                iso6391 = "en",
                iso31661 = "US",
                name = "Test",
                tmdbAvatarPath = null,
                username = "testuser",
            )
        fakeFavoriteContentDao.insertFavoriteItem(
            FavoriteContentEntity(
                mediaId = 1,
                mediaType = "movie",
                imagePath = "/poster.jpg",
                name = "Movie",
                syncStatus = SyncStatus.PENDING_PUSH,
            )
        )

        val result = repository.syncLocalItemsWithTmdb()

        assertEquals(1, result.pushed)
        assertTrue(result.errors.isEmpty())
    }

    // endregion
}
