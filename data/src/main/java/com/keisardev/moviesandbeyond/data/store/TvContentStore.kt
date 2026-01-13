package com.keisardev.moviesandbeyond.data.store

import com.keisardev.moviesandbeyond.core.local.database.dao.CachedContentDao
import com.keisardev.moviesandbeyond.core.model.content.ContentItem
import com.keisardev.moviesandbeyond.core.network.model.content.NetworkContentItem
import com.keisardev.moviesandbeyond.core.network.retrofit.TmdbApi
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MemoryPolicy
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder

/** Maximum number of items to keep in memory cache. */
private const val MEMORY_CACHE_SIZE = 100L

/**
 * Factory for creating Store5 instances for TV show content. Each store instance handles caching
 * for TV show list data with offline-first support.
 */
@Singleton
class TvContentStoreFactory
@Inject
constructor(private val api: TmdbApi, private val cachedContentDao: CachedContentDao) {
    /**
     * Creates a Store for TV show content with:
     * - Network fetcher from TMDB API
     * - Room database as source of truth
     * - Memory cache with configurable size and expiration
     */
    fun create(): Store<TvContentKey, List<ContentItem>> =
        StoreBuilder.from(
                fetcher =
                    Fetcher.of { key: TvContentKey ->
                        api.getTvShowLists(category = key.category.categoryName, page = key.page)
                            .results
                            .map(NetworkContentItem::asModel)
                    },
                sourceOfTruth =
                    SourceOfTruth.of(
                        reader = { key ->
                            cachedContentDao
                                .observeByCategoryAndPage(key.toCategoryString(), key.page)
                                .map { entities ->
                                    entities.map { it.toContentItem() }.takeIf { it.isNotEmpty() }
                                }
                        },
                        writer = { key, items ->
                            // Delete old entries for this page before inserting new ones
                            cachedContentDao.deleteByCategoryAndPage(
                                key.toCategoryString(), key.page)
                            cachedContentDao.insertAll(
                                items.toCachedEntities(key.toCategoryString(), key.page))
                        },
                        delete = { key ->
                            cachedContentDao.deleteByCategoryAndPage(
                                key.toCategoryString(), key.page)
                        },
                        deleteAll = { cachedContentDao.deleteAll() }))
            .cachePolicy(
                MemoryPolicy.builder<TvContentKey, List<ContentItem>>()
                    .setMaxSize(MEMORY_CACHE_SIZE)
                    .build())
            .build()
}
