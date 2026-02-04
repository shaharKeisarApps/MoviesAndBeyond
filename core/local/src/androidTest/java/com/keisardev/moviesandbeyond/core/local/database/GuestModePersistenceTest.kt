package com.keisardev.moviesandbeyond.core.local.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.keisardev.moviesandbeyond.core.local.database.dao.FavoriteContentDao
import com.keisardev.moviesandbeyond.core.local.database.dao.WatchlistContentDao
import com.keisardev.moviesandbeyond.core.local.database.entity.FavoriteContentEntity
import com.keisardev.moviesandbeyond.core.local.database.entity.WatchlistContentEntity
import com.keisardev.moviesandbeyond.core.model.library.SyncStatus
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for guest mode data persistence bug fix.
 *
 * Verifies that LOCAL_ONLY items persist across logout while SYNCED/PENDING_PUSH items are deleted.
 *
 * This test would have caught the original bug where logout was deleting ALL items including guest
 * mode data.
 *
 * Run with: ./gradlew :core:local:connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class GuestModePersistenceTest {
    private lateinit var database: MoviesAndBeyondDatabase
    private lateinit var favoriteDao: FavoriteContentDao
    private lateinit var watchlistDao: WatchlistContentDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room.inMemoryDatabaseBuilder(context, MoviesAndBeyondDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        favoriteDao = database.favoriteContentDao()
        watchlistDao = database.watchlistContentDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun logout_preservesGuestModeFavorites() = runTest {
        // GIVEN: Guest user adds a favorite (LOCAL_ONLY)
        val guestFavorite =
            FavoriteContentEntity(
                id = 1,
                mediaId = 100,
                mediaType = "movie",
                name = "Guest Movie",
                imagePath = "/guest.jpg",
                syncStatus = SyncStatus.LOCAL_ONLY // Guest mode data
                )

        favoriteDao.upsertFavoriteItems(listOf(guestFavorite))

        // AND: User then logs in and adds a synced favorite
        val syncedFavorite =
            FavoriteContentEntity(
                id = 2,
                mediaId = 200,
                mediaType = "movie",
                name = "TMDB Movie",
                imagePath = "/tmdb.jpg",
                syncStatus = SyncStatus.SYNCED // TMDB synced data
                )

        favoriteDao.upsertFavoriteItems(listOf(syncedFavorite))

        // Verify both exist
        val allFavorites = favoriteDao.getAllFavoriteContent().first()
        assertEquals(2, allFavorites.size)

        // WHEN: User logs out (should only delete synced items)
        favoriteDao.deleteSyncedFavoriteItems()

        // THEN: Guest favorite should remain, TMDB favorite should be deleted
        val remainingFavorites = favoriteDao.getAllFavoriteContent().first()
        assertEquals(1, remainingFavorites.size)
        assertEquals("Guest Movie", remainingFavorites[0].name)
        assertEquals(SyncStatus.LOCAL_ONLY, remainingFavorites[0].syncStatus)
    }

    @Test
    fun logout_preservesGuestModeWatchlist() = runTest {
        // GIVEN: Guest user adds to watchlist (LOCAL_ONLY)
        val guestWatchlist =
            WatchlistContentEntity(
                id = 1,
                mediaId = 100,
                mediaType = "tv",
                name = "Guest Show",
                imagePath = "/guestshow.jpg",
                syncStatus = SyncStatus.LOCAL_ONLY)

        watchlistDao.upsertWatchlistItems(listOf(guestWatchlist))

        // AND: User logs in and syncs TMDB watchlist
        val syncedWatchlist =
            WatchlistContentEntity(
                id = 2,
                mediaId = 200,
                mediaType = "tv",
                name = "TMDB Show",
                imagePath = "/tmdbshow.jpg",
                syncStatus = SyncStatus.SYNCED)

        watchlistDao.upsertWatchlistItems(listOf(syncedWatchlist))

        // WHEN: User logs out
        watchlistDao.deleteSyncedWatchlistItems()

        // THEN: Guest watchlist item should remain
        val remainingWatchlist = watchlistDao.getAllWatchlistContent().first()
        assertEquals(1, remainingWatchlist.size)
        assertEquals("Guest Show", remainingWatchlist[0].name)
        assertEquals(SyncStatus.LOCAL_ONLY, remainingWatchlist[0].syncStatus)
    }

    @Test
    fun logout_deletesPendingPushItems() = runTest {
        // GIVEN: User has items pending sync to TMDB
        val localOnlyItem =
            FavoriteContentEntity(
                id = 1,
                mediaId = 100,
                mediaType = "movie",
                name = "Local Only",
                imagePath = "/local.jpg",
                syncStatus = SyncStatus.LOCAL_ONLY)

        val pendingPushItem =
            FavoriteContentEntity(
                id = 2,
                mediaId = 200,
                mediaType = "movie",
                name = "Pending Push",
                imagePath = "/pending.jpg",
                syncStatus = SyncStatus.PENDING_PUSH)

        val syncedItem =
            FavoriteContentEntity(
                id = 3,
                mediaId = 300,
                mediaType = "movie",
                name = "Synced",
                imagePath = "/synced.jpg",
                syncStatus = SyncStatus.SYNCED)

        favoriteDao.upsertFavoriteItems(listOf(localOnlyItem, pendingPushItem, syncedItem))

        // WHEN: User logs out
        favoriteDao.deleteSyncedFavoriteItems()

        // THEN: Only LOCAL_ONLY items should remain
        val remainingItems = favoriteDao.getAllFavoriteContent().first()
        assertEquals(1, remainingItems.size)
        assertEquals("Local Only", remainingItems[0].name)
        assertEquals(SyncStatus.LOCAL_ONLY, remainingItems[0].syncStatus)
    }

    @Test
    fun guestModeFlow_endToEnd() = runTest {
        // SCENARIO: Complete guest mode flow
        // Step 1: Guest adds favorites
        val guestFavorites =
            listOf(
                FavoriteContentEntity(
                    id = 1,
                    mediaId = 100,
                    mediaType = "movie",
                    name = "Guest Movie 1",
                    imagePath = "/g1.jpg",
                    syncStatus = SyncStatus.LOCAL_ONLY),
                FavoriteContentEntity(
                    id = 2,
                    mediaId = 101,
                    mediaType = "movie",
                    name = "Guest Movie 2",
                    imagePath = "/g2.jpg",
                    syncStatus = SyncStatus.LOCAL_ONLY))

        favoriteDao.upsertFavoriteItems(guestFavorites)

        // Verify guest favorites exist
        var favorites = favoriteDao.getAllFavoriteContent().first()
        assertEquals(2, favorites.size)

        // Step 2: User logs in (guest data should remain)
        // Simulate TMDB sync adding new items
        val tmdbFavorite =
            FavoriteContentEntity(
                id = 3,
                mediaId = 200,
                mediaType = "movie",
                name = "TMDB Movie",
                imagePath = "/tmdb.jpg",
                syncStatus = SyncStatus.SYNCED)

        favoriteDao.upsertFavoriteItems(listOf(tmdbFavorite))

        // Verify all items exist
        favorites = favoriteDao.getAllFavoriteContent().first()
        assertEquals(3, favorites.size)

        // Step 3: User logs out
        favoriteDao.deleteSyncedFavoriteItems()

        // Verify guest data persists after logout
        favorites = favoriteDao.getAllFavoriteContent().first()
        assertEquals(2, favorites.size)
        assertTrue(favorites.all { it.syncStatus == SyncStatus.LOCAL_ONLY })
        assertTrue(favorites.any { it.name == "Guest Movie 1" })
        assertTrue(favorites.any { it.name == "Guest Movie 2" })
    }

    @Test
    fun multipleLogoutCycles_preservesGuestData() = runTest {
        // GIVEN: Guest adds a favorite
        val guestFavorite =
            FavoriteContentEntity(
                id = 1,
                mediaId = 100,
                mediaType = "movie",
                name = "Persistent Guest Favorite",
                imagePath = "/persist.jpg",
                syncStatus = SyncStatus.LOCAL_ONLY)

        favoriteDao.upsertFavoriteItems(listOf(guestFavorite))

        // WHEN: User logs in and out multiple times
        repeat(3) { cycle ->
            // Add synced item
            val syncedItem =
                FavoriteContentEntity(
                    id = 100 + cycle,
                    mediaId = 200 + cycle,
                    mediaType = "movie",
                    name = "TMDB Cycle $cycle",
                    imagePath = "/cycle$cycle.jpg",
                    syncStatus = SyncStatus.SYNCED)

            favoriteDao.upsertFavoriteItems(listOf(syncedItem))

            // Logout (delete synced items)
            favoriteDao.deleteSyncedFavoriteItems()
        }

        // THEN: Guest favorite should still exist after multiple cycles
        val remainingItems = favoriteDao.getAllFavoriteContent().first()
        assertEquals(1, remainingItems.size)
        assertEquals("Persistent Guest Favorite", remainingItems[0].name)
        assertEquals(SyncStatus.LOCAL_ONLY, remainingItems[0].syncStatus)
    }
}
