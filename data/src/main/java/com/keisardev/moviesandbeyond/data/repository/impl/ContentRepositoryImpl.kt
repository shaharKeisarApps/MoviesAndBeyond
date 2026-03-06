package com.keisardev.moviesandbeyond.data.repository.impl

import com.keisardev.moviesandbeyond.core.local.database.dao.CachedContentDao
import com.keisardev.moviesandbeyond.core.model.Result
import com.keisardev.moviesandbeyond.core.model.content.ContentItem
import com.keisardev.moviesandbeyond.core.model.content.MovieListCategory
import com.keisardev.moviesandbeyond.core.model.content.TvShowListCategory
import com.keisardev.moviesandbeyond.data.repository.ContentRepository
import com.keisardev.moviesandbeyond.data.store.MovieContentKey
import com.keisardev.moviesandbeyond.data.store.TvContentKey
import com.keisardev.moviesandbeyond.data.store.toResult
import com.keisardev.moviesandbeyond.data.store.toResultFlow
import javax.inject.Inject
import kotlin.math.pow
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse

private const val MAX_RETRIES = 3
private const val BASE_DELAY_MS = 1_000L
private const val JITTER_MS = 200L

internal class ContentRepositoryImpl
@Inject
constructor(
    private val cachedContentDao: CachedContentDao,
    private val movieContentStore: Store<MovieContentKey, List<ContentItem>>,
    private val tvContentStore: Store<TvContentKey, List<ContentItem>>,
) : ContentRepository {

    override fun observeMovieItems(
        category: MovieListCategory,
        page: Int,
        refresh: Boolean,
    ): Flow<Result<List<ContentItem>>> =
        movieContentStore
            .stream(
                StoreReadRequest.cached(key = MovieContentKey(category, page), refresh = refresh)
            )
            .toResultFlow()

    override fun observeTvShowItems(
        category: TvShowListCategory,
        page: Int,
        refresh: Boolean,
    ): Flow<Result<List<ContentItem>>> =
        tvContentStore
            .stream(StoreReadRequest.cached(key = TvContentKey(category, page), refresh = refresh))
            .toResultFlow()

    override suspend fun refreshMovieItems(
        category: MovieListCategory,
        page: Int,
    ): Result<List<ContentItem>> = fetchWithRetry {
        movieContentStore
            .stream(StoreReadRequest.fresh(MovieContentKey(category, page)))
            .filterIsInstance<StoreReadResponse.Data<List<ContentItem>>>()
            .first()
            .toResult()
    }

    override suspend fun refreshTvShowItems(
        category: TvShowListCategory,
        page: Int,
    ): Result<List<ContentItem>> = fetchWithRetry {
        tvContentStore
            .stream(StoreReadRequest.fresh(TvContentKey(category, page)))
            .filterIsInstance<StoreReadResponse.Data<List<ContentItem>>>()
            .first()
            .toResult()
    }

    override suspend fun clearCache() {
        cachedContentDao.deleteAll()
    }

    /**
     * Executes [block] with up to [MAX_RETRIES] retries using exponential back-off with jitter.
     *
     * Delays: 1 s, 2 s, 4 s (± up to [JITTER_MS] ms each). If every attempt returns [Result.Error],
     * the last error is returned.
     */
    private suspend fun <T> fetchWithRetry(block: suspend () -> Result<T>): Result<T> {
        var lastResult: Result<T> = Result.Error(RuntimeException("No attempts made"))
        repeat(MAX_RETRIES) { attempt ->
            val result = runCatching { block() }.getOrElse { Result.Error(it) }
            if (result is Result.Success) return result
            lastResult = result
            val backoff = BASE_DELAY_MS * 2.0.pow(attempt).toLong()
            val jitter = Random.nextLong(JITTER_MS)
            delay(backoff + jitter)
        }
        return lastResult
    }
}
