package com.keisardev.moviesandbeyond.core.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.keisardev.moviesandbeyond.core.local.database.entity.CachedMovieDetailsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for cached movie details. Used by Store5 SourceOfTruth for offline-first
 * caching.
 */
@Dao
interface CachedMovieDetailsDao {

    /** Observe cached movie details by ID. Used as SourceOfTruth reader. */
    @Query("SELECT * FROM cached_movie_details WHERE id = :id")
    fun observeById(id: Int): Flow<CachedMovieDetailsEntity?>

    /** Get cached movie details by ID (non-reactive). */
    @Query("SELECT * FROM cached_movie_details WHERE id = :id")
    suspend fun getById(id: Int): CachedMovieDetailsEntity?

    /** Get the fetch timestamp for cached movie details. Used for freshness validation. */
    @Query("SELECT fetched_at FROM cached_movie_details WHERE id = :id")
    suspend fun getFetchTime(id: Int): Long?

    /** Insert or replace cached movie details. Used as SourceOfTruth writer. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CachedMovieDetailsEntity)

    /** Delete cached movie details by ID. Used as SourceOfTruth delete. */
    @Query("DELETE FROM cached_movie_details WHERE id = :id") suspend fun deleteById(id: Int)

    /** Delete all cached movie details. Used for cache clearing. */
    @Query("DELETE FROM cached_movie_details") suspend fun deleteAll()

    /** Delete stale movie details older than the specified timestamp. Used for cache cleanup. */
    @Query("DELETE FROM cached_movie_details WHERE fetched_at < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}
