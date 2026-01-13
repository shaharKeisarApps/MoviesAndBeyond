package com.keisardev.moviesandbeyond.core.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.keisardev.moviesandbeyond.core.local.database.entity.CachedTvDetailsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for cached TV show details. Used by Store5 SourceOfTruth for offline-first
 * caching.
 */
@Dao
interface CachedTvDetailsDao {

    /** Observe cached TV show details by ID. Used as SourceOfTruth reader. */
    @Query("SELECT * FROM cached_tv_details WHERE id = :id")
    fun observeById(id: Int): Flow<CachedTvDetailsEntity?>

    /** Get cached TV show details by ID (non-reactive). */
    @Query("SELECT * FROM cached_tv_details WHERE id = :id")
    suspend fun getById(id: Int): CachedTvDetailsEntity?

    /** Get the fetch timestamp for cached TV show details. Used for freshness validation. */
    @Query("SELECT fetched_at FROM cached_tv_details WHERE id = :id")
    suspend fun getFetchTime(id: Int): Long?

    /** Insert or replace cached TV show details. Used as SourceOfTruth writer. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CachedTvDetailsEntity)

    /** Delete cached TV show details by ID. Used as SourceOfTruth delete. */
    @Query("DELETE FROM cached_tv_details WHERE id = :id") suspend fun deleteById(id: Int)

    /** Delete all cached TV show details. Used for cache clearing. */
    @Query("DELETE FROM cached_tv_details") suspend fun deleteAll()

    /** Delete stale TV show details older than the specified timestamp. Used for cache cleanup. */
    @Query("DELETE FROM cached_tv_details WHERE fetched_at < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}
