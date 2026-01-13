package com.keisardev.moviesandbeyond.data.repository

import com.keisardev.moviesandbeyond.core.model.NetworkResponse
import com.keisardev.moviesandbeyond.core.model.content.ContentItem
import com.keisardev.moviesandbeyond.core.model.content.MovieListCategory
import com.keisardev.moviesandbeyond.core.model.content.TvShowListCategory
import kotlinx.coroutines.flow.Flow
import org.mobilenativefoundation.store.store5.StoreReadResponse

interface ContentRepository {
    // ==================== Legacy API (Deprecated) ====================
    // These methods are kept for backward compatibility during migration.
    // New code should use the Store5-based methods below.

    @Deprecated("Use observeMovieItems instead for offline-first support")
    suspend fun getMovieItems(
        page: Int,
        category: MovieListCategory
    ): NetworkResponse<List<ContentItem>>

    @Deprecated("Use observeTvShowItems instead for offline-first support")
    suspend fun getTvShowItems(
        page: Int,
        category: TvShowListCategory
    ): NetworkResponse<List<ContentItem>>

    // ==================== Store5 API (Offline-First) ====================

    /**
     * Observes movie items for a specific category and page with offline-first caching.
     *
     * Flow emissions:
     * - [StoreReadResponse.Loading] while fetching
     * - [StoreReadResponse.Data] with cached data (if available)
     * - [StoreReadResponse.Data] with fresh data (from network)
     * - [StoreReadResponse.Error] if both cache and network fail
     *
     * @param category The movie list category
     * @param page The page number (1-indexed)
     * @param refresh Whether to force a network refresh
     */
    fun observeMovieItems(
        category: MovieListCategory,
        page: Int,
        refresh: Boolean = true
    ): Flow<StoreReadResponse<List<ContentItem>>>

    /**
     * Observes TV show items for a specific category and page with offline-first caching.
     *
     * @param category The TV show list category
     * @param page The page number (1-indexed)
     * @param refresh Whether to force a network refresh
     */
    fun observeTvShowItems(
        category: TvShowListCategory,
        page: Int,
        refresh: Boolean = true
    ): Flow<StoreReadResponse<List<ContentItem>>>

    /**
     * Forces a refresh of movie items from the network.
     *
     * @param category The movie list category
     * @param page The page number (1-indexed)
     * @return The fresh list of content items
     */
    suspend fun refreshMovieItems(category: MovieListCategory, page: Int): List<ContentItem>

    /**
     * Forces a refresh of TV show items from the network.
     *
     * @param category The TV show list category
     * @param page The page number (1-indexed)
     * @return The fresh list of content items
     */
    suspend fun refreshTvShowItems(category: TvShowListCategory, page: Int): List<ContentItem>

    /** Clears all cached content data. */
    suspend fun clearCache()
}
