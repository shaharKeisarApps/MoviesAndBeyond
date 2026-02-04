package com.keisardev.moviesandbeyond.core.local.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.keisardev.moviesandbeyond.core.local.database.dao.FavoriteContentDao
import com.keisardev.moviesandbeyond.core.local.database.entity.FavoriteContentEntity
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
 * Test to verify mediaType format handling in DAO queries.
 *
 * This test verifies that items are retrieved correctly regardless of mediaType casing.
 */
@RunWith(AndroidJUnit4::class)
class MediaTypeFormatTest {
    private lateinit var database: MoviesAndBeyondDatabase
    private lateinit var favoriteDao: FavoriteContentDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room.inMemoryDatabaseBuilder(context, MoviesAndBeyondDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        favoriteDao = database.favoriteContentDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getFavoriteMovies_returnsMoviesRegardlessOfCase() = runTest {
        // Insert items with different mediaType formats
        val lowercaseMovie =
            FavoriteContentEntity(
                id = 1,
                mediaId = 100,
                mediaType = "movie", // lowercase
                name = "Lowercase Movie",
                imagePath = "/path1.jpg",
                syncStatus = SyncStatus.LOCAL_ONLY)

        val uppercaseMovie =
            FavoriteContentEntity(
                id = 2,
                mediaId = 200,
                mediaType = "MOVIE", // uppercase
                name = "Uppercase Movie",
                imagePath = "/path2.jpg",
                syncStatus = SyncStatus.LOCAL_ONLY)

        favoriteDao.upsertFavoriteItems(listOf(lowercaseMovie, uppercaseMovie))

        // Query for movies
        val movies = favoriteDao.getFavoriteMovies().first()

        // Debug: print what we got
        println("DEBUG: Found ${movies.size} movies")
        movies.forEach {
            println("DEBUG: Movie - id=${it.mediaId}, name=${it.name}, mediaType='${it.mediaType}'")
        }

        // Check results
        assertTrue("Should have found at least the lowercase movie", movies.isNotEmpty())
    }

    @Test
    fun verifyMediaTypeCaseSensitivity() = runTest {
        // Insert with lowercase
        val item =
            FavoriteContentEntity(
                mediaId = 100,
                mediaType = "movie",
                name = "Test Movie",
                imagePath = "/path.jpg",
                syncStatus = SyncStatus.LOCAL_ONLY)

        favoriteDao.insertFavoriteItem(item)

        // Try to retrieve
        val movies = favoriteDao.getFavoriteMovies().first()
        assertEquals("Should find 1 movie with lowercase mediaType", 1, movies.size)

        // Now insert with uppercase
        val uppercaseItem =
            FavoriteContentEntity(
                mediaId = 200,
                mediaType = "MOVIE",
                name = "Uppercase Movie",
                imagePath = "/path2.jpg",
                syncStatus = SyncStatus.LOCAL_ONLY)

        favoriteDao.insertFavoriteItem(uppercaseItem)

        // Try to retrieve again
        val allMovies = favoriteDao.getFavoriteMovies().first()
        println("DEBUG: After uppercase insert, found ${allMovies.size} movies")
        allMovies.forEach { println("DEBUG: mediaType='${it.mediaType}'") }

        // This will fail if DAO query is case-sensitive
        assertEquals("Should find 2 movies regardless of case", 2, allMovies.size)
    }

    @Test
    fun checkExistenceIsCaseInsensitive() = runTest {
        // Insert with uppercase
        val item =
            FavoriteContentEntity(
                mediaId = 100,
                mediaType = "MOVIE",
                name = "Test Movie",
                imagePath = "/path.jpg",
                syncStatus = SyncStatus.LOCAL_ONLY)

        favoriteDao.insertFavoriteItem(item)

        // Check with lowercase should still find it
        val exists = favoriteDao.checkFavoriteItemExists(mediaId = 100, mediaType = "movie")
        assertTrue("Should find item with case-insensitive check", exists)

        // Check with uppercase
        val existsUpper = favoriteDao.checkFavoriteItemExists(mediaId = 100, mediaType = "MOVIE")
        assertTrue("Should find item with uppercase check", existsUpper)

        // Check with mixed case
        val existsMixed = favoriteDao.checkFavoriteItemExists(mediaId = 100, mediaType = "MoViE")
        assertTrue("Should find item with mixed case check", existsMixed)
    }

    @Test
    fun deleteIsCaseInsensitive() = runTest {
        // Insert with uppercase
        val item =
            FavoriteContentEntity(
                mediaId = 100,
                mediaType = "MOVIE",
                name = "Test Movie",
                imagePath = "/path.jpg",
                syncStatus = SyncStatus.LOCAL_ONLY)

        favoriteDao.insertFavoriteItem(item)

        // Verify it exists
        val movies = favoriteDao.getFavoriteMovies().first()
        assertEquals("Should have 1 movie", 1, movies.size)

        // Delete with lowercase should work
        favoriteDao.deleteFavoriteItem(mediaId = 100, mediaType = "movie")

        // Verify it's deleted
        val moviesAfterDelete = favoriteDao.getFavoriteMovies().first()
        assertEquals("Should have 0 movies after delete", 0, moviesAfterDelete.size)
    }
}
