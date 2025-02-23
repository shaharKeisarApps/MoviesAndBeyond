package com.keisardev.moviesandbeyond.data.repository.impl

import com.keisardev.moviesandbeyond.core.local.database.dao.AccountDetailsDao
import com.keisardev.moviesandbeyond.core.model.NetworkResponse
import com.keisardev.moviesandbeyond.core.model.content.ContentItem
import com.keisardev.moviesandbeyond.core.model.content.MovieListCategory
import com.keisardev.moviesandbeyond.core.model.content.TvShowListCategory
import com.keisardev.moviesandbeyond.core.network.model.content.NetworkContentItem
import com.keisardev.moviesandbeyond.core.network.retrofit.TmdbApi
import com.keisardev.moviesandbeyond.data.repository.ContentRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

internal class ContentRepositoryImpl @Inject constructor(
    private val tmdbApi: TmdbApi,
    private val accountDetailsDao: AccountDetailsDao
) : ContentRepository {
    override suspend fun getMovieItems(
        page: Int,
        category: MovieListCategory
    ): NetworkResponse<List<ContentItem>> {
        return try {
            val response = tmdbApi.getMovieLists(
                category = category.categoryName,
                page = page,
                region = accountDetailsDao.getRegionCode()
            )
            NetworkResponse.Success(response.results.map(
                NetworkContentItem::asModel))
        } catch (e: IOException) {
            return NetworkResponse.Error()
        } catch (e: HttpException) {
            return NetworkResponse.Error(e.message)
        }
    }

    override suspend fun getTvShowItems(
        page: Int,
        category: TvShowListCategory
    ): NetworkResponse<List<ContentItem>> {
        return try {
            val response = tmdbApi.getTvShowLists(
                category = category.categoryName,
                page = page
            )
            NetworkResponse.Success(response.results.map(NetworkContentItem::asModel))
        } catch (e: IOException) {
            return NetworkResponse.Error()
        } catch (e: HttpException) {
            return NetworkResponse.Error(e.message)
        }
    }
}