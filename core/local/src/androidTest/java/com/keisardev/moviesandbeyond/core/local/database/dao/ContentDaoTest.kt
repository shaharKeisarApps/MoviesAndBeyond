package com.keisardev.moviesandbeyond.core.local.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.keisardev.moviesandbeyond.core.local.database.MoviesAndBeyondDatabase
import com.keisardev.moviesandbeyond.core.local.database.entity.FavoriteContentEntity
import com.keisardev.moviesandbeyond.core.local.database.entity.WatchlistContentEntity
import com.keisardev.moviesandbeyond.core.model.library.SyncStatus
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class ContentDaoTest {
    private lateinit var db: MoviesAndBeyondDatabase
    private lateinit var favoriteContentDao: FavoriteContentDao
    private lateinit var watchlistContentDao: WatchlistContentDao

    @Before
    fun setUp() {
        val testContext = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(testContext, MoviesAndBeyondDatabase::class.java).build()
        favoriteContentDao = db.favoriteContentDao()
        watchlistContentDao = db.watchlistContentDao()
    }

    @Test
    fun favoriteContentDao_sameId_differentMediaType_inserted_separately() = runTest {
        val favoriteItems =
            listOf(
                FavoriteContentEntity(
                    id = 1, mediaId = 1, mediaType = "movie", name = "", imagePath = ""),
                FavoriteContentEntity(
                    id = 2, mediaId = 1, mediaType = "tv", name = "", imagePath = ""))
        favoriteContentDao.upsertFavoriteItems(favoriteItems)

        assertEquals(listOf(favoriteItems[0]), favoriteContentDao.getFavoriteMovies().first())

        assertEquals(listOf(favoriteItems[1]), favoriteContentDao.getFavoriteTvShows().first())
    }

    @Test
    fun watchlistContentDao_sameId_differentMediaType_inserted_separately() = runTest {
        val watchlistItems =
            listOf(
                WatchlistContentEntity(
                    id = 1, mediaId = 1, mediaType = "movie", name = "", imagePath = ""),
                WatchlistContentEntity(
                    id = 2, mediaId = 1, mediaType = "tv", name = "", imagePath = ""))
        watchlistContentDao.upsertWatchlistItems(watchlistItems)

        assertEquals(listOf(watchlistItems[0]), watchlistContentDao.getMoviesWatchlist().first())

        assertEquals(listOf(watchlistItems[1]), watchlistContentDao.getTvShowsWatchlist().first())
    }

    @Test
    fun favoriteContentDao_pendingDeleteItems_excludedFromQuery() = runTest {
        // Insert favorite items
        val favoriteItems =
            listOf(
                FavoriteContentEntity(
                    id = 1,
                    mediaId = 100,
                    mediaType = "movie",
                    name = "Movie 1",
                    imagePath = "",
                    syncStatus = SyncStatus.SYNCED),
                FavoriteContentEntity(
                    id = 2,
                    mediaId = 200,
                    mediaType = "movie",
                    name = "Movie 2",
                    imagePath = "",
                    syncStatus = SyncStatus.SYNCED))
        favoriteContentDao.upsertFavoriteItems(favoriteItems)

        // Verify both items are returned
        assertEquals(2, favoriteContentDao.getFavoriteMovies().first().size)

        // Mark one item for deletion
        favoriteContentDao.markForDeletion(mediaId = 100, mediaType = "movie")

        // Verify only non-deleted item is returned
        val result = favoriteContentDao.getFavoriteMovies().first()
        assertEquals(1, result.size)
        assertEquals(200, result[0].mediaId)
    }

    @Test
    fun watchlistContentDao_pendingDeleteItems_excludedFromQuery() = runTest {
        // Insert watchlist items
        val watchlistItems =
            listOf(
                WatchlistContentEntity(
                    id = 1,
                    mediaId = 100,
                    mediaType = "tv",
                    name = "TV Show 1",
                    imagePath = "",
                    syncStatus = SyncStatus.SYNCED),
                WatchlistContentEntity(
                    id = 2,
                    mediaId = 200,
                    mediaType = "tv",
                    name = "TV Show 2",
                    imagePath = "",
                    syncStatus = SyncStatus.SYNCED))
        watchlistContentDao.upsertWatchlistItems(watchlistItems)

        // Verify both items are returned
        assertEquals(2, watchlistContentDao.getTvShowsWatchlist().first().size)

        // Mark one item for deletion
        watchlistContentDao.markForDeletion(mediaId = 100, mediaType = "tv")

        // Verify only non-deleted item is returned
        val result = watchlistContentDao.getTvShowsWatchlist().first()
        assertEquals(1, result.size)
        assertEquals(200, result[0].mediaId)
    }

    @Test
    fun favoriteContentDao_localOnlyItems_includedInQuery() = runTest {
        // Insert favorites with different sync statuses
        val favoriteItems =
            listOf(
                FavoriteContentEntity(
                    id = 1,
                    mediaId = 100,
                    mediaType = "movie",
                    name = "Guest Favorite",
                    imagePath = "",
                    syncStatus = SyncStatus.LOCAL_ONLY),
                FavoriteContentEntity(
                    id = 2,
                    mediaId = 200,
                    mediaType = "movie",
                    name = "Synced Favorite",
                    imagePath = "",
                    syncStatus = SyncStatus.SYNCED),
                FavoriteContentEntity(
                    id = 3,
                    mediaId = 300,
                    mediaType = "movie",
                    name = "Pending Favorite",
                    imagePath = "",
                    syncStatus = SyncStatus.PENDING_PUSH))
        favoriteContentDao.upsertFavoriteItems(favoriteItems)

        // Verify all non-deleted items are returned
        val result = favoriteContentDao.getFavoriteMovies().first()
        assertEquals(3, result.size)

        // Verify LOCAL_ONLY item is included
        val localOnlyItem = result.find { it.mediaId == 100 }
        assertEquals("Guest Favorite", localOnlyItem?.name)
        assertEquals(SyncStatus.LOCAL_ONLY, localOnlyItem?.syncStatus)
    }

    @Test
    fun watchlistContentDao_localOnlyItems_includedInQuery() = runTest {
        // Insert watchlist with different sync statuses
        val watchlistItems =
            listOf(
                WatchlistContentEntity(
                    id = 1,
                    mediaId = 100,
                    mediaType = "tv",
                    name = "Guest Watchlist",
                    imagePath = "",
                    syncStatus = SyncStatus.LOCAL_ONLY),
                WatchlistContentEntity(
                    id = 2,
                    mediaId = 200,
                    mediaType = "tv",
                    name = "Synced Watchlist",
                    imagePath = "",
                    syncStatus = SyncStatus.SYNCED),
                WatchlistContentEntity(
                    id = 3,
                    mediaId = 300,
                    mediaType = "tv",
                    name = "Pending Watchlist",
                    imagePath = "",
                    syncStatus = SyncStatus.PENDING_PUSH))
        watchlistContentDao.upsertWatchlistItems(watchlistItems)

        // Verify all non-deleted items are returned
        val result = watchlistContentDao.getTvShowsWatchlist().first()
        assertEquals(3, result.size)

        // Verify LOCAL_ONLY item is included
        val localOnlyItem = result.find { it.mediaId == 100 }
        assertEquals("Guest Watchlist", localOnlyItem?.name)
        assertEquals(SyncStatus.LOCAL_ONLY, localOnlyItem?.syncStatus)
    }

    @Test
    fun favoriteContentDao_onlyPendingDeleteExcluded_allOtherStatusesIncluded() = runTest {
        // Insert favorites with ALL sync statuses
        val favoriteItems =
            listOf(
                FavoriteContentEntity(
                    id = 1,
                    mediaId = 100,
                    mediaType = "movie",
                    name = "Local Only",
                    imagePath = "",
                    syncStatus = SyncStatus.LOCAL_ONLY),
                FavoriteContentEntity(
                    id = 2,
                    mediaId = 200,
                    mediaType = "movie",
                    name = "Synced",
                    imagePath = "",
                    syncStatus = SyncStatus.SYNCED),
                FavoriteContentEntity(
                    id = 3,
                    mediaId = 300,
                    mediaType = "movie",
                    name = "Pending Push",
                    imagePath = "",
                    syncStatus = SyncStatus.PENDING_PUSH),
                FavoriteContentEntity(
                    id = 4,
                    mediaId = 400,
                    mediaType = "movie",
                    name = "Pending Delete",
                    imagePath = "",
                    syncStatus = SyncStatus.PENDING_DELETE))
        favoriteContentDao.upsertFavoriteItems(favoriteItems)

        // Verify only PENDING_DELETE is excluded (3 items returned, not 4)
        val result = favoriteContentDao.getFavoriteMovies().first()
        assertEquals(3, result.size)

        // Verify PENDING_DELETE is excluded
        assertEquals(false, result.any { it.mediaId == 400 })

        // Verify all others are included
        assertEquals(true, result.any { it.syncStatus == SyncStatus.LOCAL_ONLY })
        assertEquals(true, result.any { it.syncStatus == SyncStatus.SYNCED })
        assertEquals(true, result.any { it.syncStatus == SyncStatus.PENDING_PUSH })
    }

    @Test
    fun favoriteContentDao_deleteSyncedItems_preservesLocalOnlyItems() = runTest {
        // Insert favorites with different sync statuses
        val favoriteItems =
            listOf(
                FavoriteContentEntity(
                    id = 1,
                    mediaId = 100,
                    mediaType = "movie",
                    name = "Guest Favorite (LOCAL_ONLY)",
                    imagePath = "",
                    syncStatus = SyncStatus.LOCAL_ONLY),
                FavoriteContentEntity(
                    id = 2,
                    mediaId = 200,
                    mediaType = "movie",
                    name = "Synced Favorite",
                    imagePath = "",
                    syncStatus = SyncStatus.SYNCED),
                FavoriteContentEntity(
                    id = 3,
                    mediaId = 300,
                    mediaType = "movie",
                    name = "Pending Favorite",
                    imagePath = "",
                    syncStatus = SyncStatus.PENDING_PUSH),
                FavoriteContentEntity(
                    id = 4,
                    mediaId = 400,
                    mediaType = "movie",
                    name = "Pending Delete",
                    imagePath = "",
                    syncStatus = SyncStatus.PENDING_DELETE))
        favoriteContentDao.upsertFavoriteItems(favoriteItems)

        // Delete synced items (simulating logout)
        favoriteContentDao.deleteSyncedFavoriteItems()

        // Verify only LOCAL_ONLY and PENDING_DELETE remain
        val allItems = favoriteContentDao.getFavoriteMovies().first()
        assertEquals(1, allItems.size)
        assertEquals(100, allItems[0].mediaId)
        assertEquals(SyncStatus.LOCAL_ONLY, allItems[0].syncStatus)
        assertEquals("Guest Favorite (LOCAL_ONLY)", allItems[0].name)
    }

    @Test
    fun watchlistContentDao_deleteSyncedItems_preservesLocalOnlyItems() = runTest {
        // Insert watchlist with different sync statuses
        val watchlistItems =
            listOf(
                WatchlistContentEntity(
                    id = 1,
                    mediaId = 100,
                    mediaType = "tv",
                    name = "Guest Watchlist (LOCAL_ONLY)",
                    imagePath = "",
                    syncStatus = SyncStatus.LOCAL_ONLY),
                WatchlistContentEntity(
                    id = 2,
                    mediaId = 200,
                    mediaType = "tv",
                    name = "Synced Watchlist",
                    imagePath = "",
                    syncStatus = SyncStatus.SYNCED),
                WatchlistContentEntity(
                    id = 3,
                    mediaId = 300,
                    mediaType = "tv",
                    name = "Pending Watchlist",
                    imagePath = "",
                    syncStatus = SyncStatus.PENDING_PUSH),
                WatchlistContentEntity(
                    id = 4,
                    mediaId = 400,
                    mediaType = "tv",
                    name = "Pending Delete",
                    imagePath = "",
                    syncStatus = SyncStatus.PENDING_DELETE))
        watchlistContentDao.upsertWatchlistItems(watchlistItems)

        // Delete synced items (simulating logout)
        watchlistContentDao.deleteSyncedWatchlistItems()

        // Verify only LOCAL_ONLY and PENDING_DELETE remain
        val allItems = watchlistContentDao.getTvShowsWatchlist().first()
        assertEquals(1, allItems.size)
        assertEquals(100, allItems[0].mediaId)
        assertEquals(SyncStatus.LOCAL_ONLY, allItems[0].syncStatus)
        assertEquals("Guest Watchlist (LOCAL_ONLY)", allItems[0].name)
    }

    @After
    fun tearDown() {
        db.close()
    }
}
