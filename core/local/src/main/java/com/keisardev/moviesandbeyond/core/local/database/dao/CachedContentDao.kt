package com.keisardev.moviesandbeyond.core.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.keisardev.moviesandbeyond.core.local.database.entity.CachedContentEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for cached content (movies and TV shows). Used by Store5 SourceOfTruth for
 * offline-first caching.
 */
@Dao
interface CachedContentDao {

    /**
     * Observe all cached content for a specific category, ordered by page and position. Used as
     * SourceOfTruth reader.
     */
    @Query(
        """
        SELECT * FROM cached_content
        WHERE category = :category
        ORDER BY page ASC, position ASC
        """
    )
    fun observeByCategory(category: String): Flow<List<CachedContentEntity>>

    /** Observe cached content for a specific category and page. Used for paginated data loading. */
    @Query(
        """
        SELECT * FROM cached_content
        WHERE category = :category AND page = :page
        ORDER BY position ASC
        """
    )
    fun observeByCategoryAndPage(category: String, page: Int): Flow<List<CachedContentEntity>>

    /** Get the latest fetch timestamp for a category. Used for freshness validation. */
    @Query("SELECT MAX(fetched_at) FROM cached_content WHERE category = :category")
    suspend fun getLatestFetchTime(category: String): Long?

    /** Insert or replace cached content items. Used as SourceOfTruth writer. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CachedContentEntity>)

    /** Delete all cached content for a specific category. Used when refreshing a full category. */
    @Query("DELETE FROM cached_content WHERE category = :category")
    suspend fun deleteByCategory(category: String)

    /**
     * Delete cached content for a specific category and page. Used when refreshing a single page.
     */
    @Query("DELETE FROM cached_content WHERE category = :category AND page = :page")
    suspend fun deleteByCategoryAndPage(category: String, page: Int)

    /** Delete all cached content. Used for cache clearing. */
    @Query("DELETE FROM cached_content") suspend fun deleteAll()

    /** Delete stale content older than the specified timestamp. Used for cache cleanup. */
    @Query("DELETE FROM cached_content WHERE fetched_at < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}
