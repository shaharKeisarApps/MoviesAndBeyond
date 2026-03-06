package com.keisardev.moviesandbeyond.data.repository

import com.keisardev.moviesandbeyond.core.model.Result
import com.keisardev.moviesandbeyond.core.model.content.ContentItem
import com.keisardev.moviesandbeyond.core.model.content.MovieListCategory
import com.keisardev.moviesandbeyond.core.model.content.TvShowListCategory
import kotlinx.coroutines.flow.Flow

/**
 * Repository for browsing movie and TV show listings from TMDB.
 *
 * Provides offline-first content observation via Store5 caching. Consumers observe [Flow]s that
 * emit cached data immediately and then fresh data from the network.
 */
interface ContentRepository {
    /**
     * Observes movie items for a specific category and page with offline-first caching.
     *
     * Flow emissions:
     * - [Result.Loading] while fetching
     * - [Result.Success] with cached data (if available)
     * - [Result.Success] with fresh data (from network)
     * - [Result.Error] if both cache and network fail
     *
     * @param category The movie list category
     * @param page The page number (1-indexed)
     * @param refresh Whether to force a network refresh
     */
    fun observeMovieItems(
        category: MovieListCategory,
        page: Int,
        refresh: Boolean = true,
    ): Flow<Result<List<ContentItem>>>

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
        refresh: Boolean = true,
    ): Flow<Result<List<ContentItem>>>

    /**
     * Forces a refresh of movie items from the network.
     *
     * @param category The movie list category
     * @param page The page number (1-indexed)
     * @return [Result] with fresh content items, or [Result.Error] on failure
     */
    suspend fun refreshMovieItems(category: MovieListCategory, page: Int): Result<List<ContentItem>>

    /**
     * Forces a refresh of TV show items from the network.
     *
     * @param category The TV show list category
     * @param page The page number (1-indexed)
     * @return [Result] with fresh content items, or [Result.Error] on failure
     */
    suspend fun refreshTvShowItems(
        category: TvShowListCategory,
        page: Int,
    ): Result<List<ContentItem>>

    /** Clears all cached content data. */
    suspend fun clearCache()
}
