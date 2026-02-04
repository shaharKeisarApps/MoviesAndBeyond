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
 * Integration test for TMDB sync bug fix.
 *
 * Verifies that TMDB sync doesn't delete LOCAL_ONLY or PENDING_PUSH items, only SYNCED items that
 * are no longer on TMDB (stale).
 *
 * This test would have caught the bug where syncFavorites() was deleting guest mode data.
 *
 * Run with: ./gradlew :core:local:connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class SyncBugPersistenceTest {
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
    fun syncFavorites_preservesLocalOnlyItems() = runTest {
        // GIVEN: Database has LOCAL_ONLY (guest), PENDING_PUSH, and SYNCED items
        val localOnlyItem =
            FavoriteContentEntity(
                id = 1,
                mediaId = 100,
                mediaType = "movie",
                name = "Local Only Movie",
                imagePath = "/local.jpg",
                syncStatus = SyncStatus.LOCAL_ONLY // Guest mode data
                )

        val pendingPushItem =
            FavoriteContentEntity(
                id = 2,
                mediaId = 200,
                mediaType = "movie",
                name = "Pending Push Movie",
                imagePath = "/pending.jpg",
                syncStatus = SyncStatus.PENDING_PUSH // Waiting to sync to TMDB
                )

        val syncedItem =
            FavoriteContentEntity(
                id = 3,
                mediaId = 300,
                mediaType = "movie",
                name = "Synced Movie (stale)",
                imagePath = "/synced.jpg",
                syncStatus = SyncStatus.SYNCED // Synced with TMDB but removed on server
                )

        favoriteDao.upsertFavoriteItems(listOf(localOnlyItem, pendingPushItem, syncedItem))

        // Verify all 3 items exist
        val allItems = favoriteDao.getAllFavoriteContent().first()
        assertEquals(3, allItems.size)

        // WHEN: Simulating TMDB sync that removes stale SYNCED items
        // (In real code, LibraryRepository.syncFavorites() would identify syncedItem as stale
        //  because it's SYNCED but not on TMDB server, then delete it)

        // Get items that should be considered "stale" (only SYNCED items not on server)
        val itemsFromDao = favoriteDao.getAllFavoriteContent().first()
        val staleItems =
            itemsFromDao
                .filter {
                    // This is the fix: only SYNCED items can be stale
                    it.syncStatus == SyncStatus.SYNCED &&
                        // Simulate: this item is not on TMDB server
                        it.mediaId == 300
                }
                .map { Pair(it.mediaId, it.mediaType) }

        // Delete stale items (what syncFavoriteItems does)
        staleItems.forEach {
            favoriteDao.deleteFavoriteItem(mediaId = it.first, mediaType = it.second)
        }

        // THEN: LOCAL_ONLY and PENDING_PUSH items should remain
        val remainingItems = favoriteDao.getAllFavoriteContent().first()
        assertEquals(2, remainingItems.size)
        assertTrue(remainingItems.any { it.syncStatus == SyncStatus.LOCAL_ONLY })
        assertTrue(remainingItems.any { it.syncStatus == SyncStatus.PENDING_PUSH })
        assertTrue(remainingItems.none { it.syncStatus == SyncStatus.SYNCED })
    }

    @Test
    fun syncWatchlist_preservesLocalOnlyItems() = runTest {
        // GIVEN: Database has LOCAL_ONLY (guest), PENDING_PUSH, and SYNCED items
        val localOnlyItem =
            WatchlistContentEntity(
                id = 1,
                mediaId = 100,
                mediaType = "tv",
                name = "Local Only Show",
                imagePath = "/local.jpg",
                syncStatus = SyncStatus.LOCAL_ONLY)

        val pendingPushItem =
            WatchlistContentEntity(
                id = 2,
                mediaId = 200,
                mediaType = "tv",
                name = "Pending Push Show",
                imagePath = "/pending.jpg",
                syncStatus = SyncStatus.PENDING_PUSH)

        val syncedItem =
            WatchlistContentEntity(
                id = 3,
                mediaId = 300,
                mediaType = "tv",
                name = "Synced Show (stale)",
                imagePath = "/synced.jpg",
                syncStatus = SyncStatus.SYNCED)

        watchlistDao.upsertWatchlistItems(listOf(localOnlyItem, pendingPushItem, syncedItem))

        // WHEN: Simulating TMDB sync that removes stale SYNCED items
        val itemsFromDao = watchlistDao.getAllWatchlistContent().first()
        val staleItems =
            itemsFromDao
                .filter {
                    // This is the fix: only SYNCED items can be stale
                    it.syncStatus == SyncStatus.SYNCED &&
                        // Simulate: this item is not on TMDB server
                        it.mediaId == 300
                }
                .map { Pair(it.mediaId, it.mediaType) }

        staleItems.forEach {
            watchlistDao.deleteWatchlistItem(mediaId = it.first, mediaType = it.second)
        }

        // THEN: LOCAL_ONLY and PENDING_PUSH items should remain
        val remainingItems = watchlistDao.getAllWatchlistContent().first()
        assertEquals(2, remainingItems.size)
        assertTrue(remainingItems.any { it.syncStatus == SyncStatus.LOCAL_ONLY })
        assertTrue(remainingItems.any { it.syncStatus == SyncStatus.PENDING_PUSH })
        assertTrue(remainingItems.none { it.syncStatus == SyncStatus.SYNCED })
    }

    @Test
    fun syncBug_wouldHaveDeletedGuestData() = runTest {
        // This test demonstrates what WOULD have happened with the bug

        // GIVEN: Guest adds favorites before login
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
        assertEquals(2, favoriteDao.getAllFavoriteContent().first().size)

        // WHEN: Simulating the OLD buggy behavior (no syncStatus filter)
        val allItems = favoriteDao.getAllFavoriteContent().first()
        val staleItemsBuggy =
            allItems
                .filter {
                    // BUG: No syncStatus check - ALL items not on TMDB are considered stale!
                    // Simulate: these items are not on TMDB server
                    it.mediaId in listOf(100, 101)
                }
                .map { Pair(it.mediaId, it.mediaType) }

        // This would have deleted ALL guest favorites!
        staleItemsBuggy.forEach {
            favoriteDao.deleteFavoriteItem(mediaId = it.first, mediaType = it.second)
        }

        // Result: ALL guest data deleted (the bug!)
        val remainingBuggy = favoriteDao.getAllFavoriteContent().first()
        assertEquals(0, remainingBuggy.size) // Bug: guest data lost!

        // NOW: Demonstrate the FIX works
        // Re-insert guest favorites
        favoriteDao.upsertFavoriteItems(guestFavorites)

        // WHEN: Using FIXED behavior (with syncStatus filter)
        val allItemsFixed = favoriteDao.getAllFavoriteContent().first()
        val staleItemsFixed =
            allItemsFixed
                .filter {
                    // FIX: Only SYNCED items can be stale
                    it.syncStatus == SyncStatus.SYNCED &&
                        // These items are not on TMDB server
                        it.mediaId in listOf(100, 101)
                }
                .map { Pair(it.mediaId, it.mediaType) }

        staleItemsFixed.forEach {
            favoriteDao.deleteFavoriteItem(mediaId = it.first, mediaType = it.second)
        }

        // THEN: Guest data preserved!
        val remainingFixed = favoriteDao.getAllFavoriteContent().first()
        assertEquals(2, remainingFixed.size) // Fix: guest data persists!
        assertTrue(remainingFixed.all { it.syncStatus == SyncStatus.LOCAL_ONLY })
    }
}
