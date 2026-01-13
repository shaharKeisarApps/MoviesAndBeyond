package com.keisardev.moviesandbeyond.data.repository.impl

import com.keisardev.moviesandbeyond.core.local.database.dao.AccountDetailsDao
import com.keisardev.moviesandbeyond.core.local.database.dao.CachedContentDao
import com.keisardev.moviesandbeyond.core.model.NetworkResponse
import com.keisardev.moviesandbeyond.core.model.content.ContentItem
import com.keisardev.moviesandbeyond.core.model.content.MovieListCategory
import com.keisardev.moviesandbeyond.core.model.content.TvShowListCategory
import com.keisardev.moviesandbeyond.core.network.model.content.NetworkContentItem
import com.keisardev.moviesandbeyond.core.network.retrofit.TmdbApi
import com.keisardev.moviesandbeyond.data.repository.ContentRepository
import com.keisardev.moviesandbeyond.data.store.MovieContentKey
import com.keisardev.moviesandbeyond.data.store.TvContentKey
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import retrofit2.HttpException

internal class ContentRepositoryImpl
@Inject
constructor(
    private val tmdbApi: TmdbApi,
    private val accountDetailsDao: AccountDetailsDao,
    private val cachedContentDao: CachedContentDao,
    private val movieContentStore: Store<MovieContentKey, List<ContentItem>>,
    private val tvContentStore: Store<TvContentKey, List<ContentItem>>
) : ContentRepository {

    // ==================== Legacy API (Kept for backward compatibility) ====================

    @Deprecated("Use observeMovieItems instead for offline-first support")
    override suspend fun getMovieItems(
        page: Int,
        category: MovieListCategory
    ): NetworkResponse<List<ContentItem>> {
        return try {
            val response =
                tmdbApi.getMovieLists(
                    category = category.categoryName,
                    page = page,
                    region = accountDetailsDao.getRegionCode())
            NetworkResponse.Success(response.results.map(NetworkContentItem::asModel))
        } catch (e: IOException) {
            return NetworkResponse.Error()
        } catch (e: HttpException) {
            return NetworkResponse.Error(e.message)
        }
    }

    @Deprecated("Use observeTvShowItems instead for offline-first support")
    override suspend fun getTvShowItems(
        page: Int,
        category: TvShowListCategory
    ): NetworkResponse<List<ContentItem>> {
        return try {
            val response = tmdbApi.getTvShowLists(category = category.categoryName, page = page)
            NetworkResponse.Success(response.results.map(NetworkContentItem::asModel))
        } catch (e: IOException) {
            return NetworkResponse.Error()
        } catch (e: HttpException) {
            return NetworkResponse.Error(e.message)
        }
    }

    // ==================== Store5 API (Offline-First) ====================

    override fun observeMovieItems(
        category: MovieListCategory,
        page: Int,
        refresh: Boolean
    ): Flow<StoreReadResponse<List<ContentItem>>> =
        movieContentStore.stream(
            StoreReadRequest.cached(key = MovieContentKey(category, page), refresh = refresh))

    override fun observeTvShowItems(
        category: TvShowListCategory,
        page: Int,
        refresh: Boolean
    ): Flow<StoreReadResponse<List<ContentItem>>> =
        tvContentStore.stream(
            StoreReadRequest.cached(key = TvContentKey(category, page), refresh = refresh))

    override suspend fun refreshMovieItems(
        category: MovieListCategory,
        page: Int
    ): List<ContentItem> =
        movieContentStore
            .stream(StoreReadRequest.fresh(MovieContentKey(category, page)))
            .filterIsInstance<StoreReadResponse.Data<List<ContentItem>>>()
            .first()
            .value

    override suspend fun refreshTvShowItems(
        category: TvShowListCategory,
        page: Int
    ): List<ContentItem> =
        tvContentStore
            .stream(StoreReadRequest.fresh(TvContentKey(category, page)))
            .filterIsInstance<StoreReadResponse.Data<List<ContentItem>>>()
            .first()
            .value

    override suspend fun clearCache() {
        cachedContentDao.deleteAll()
    }
}
