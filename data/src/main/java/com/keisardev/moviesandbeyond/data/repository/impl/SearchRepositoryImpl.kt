package com.keisardev.moviesandbeyond.data.repository.impl

import com.keisardev.moviesandbeyond.core.model.NetworkResponse
import com.keisardev.moviesandbeyond.core.model.SearchItem
import com.keisardev.moviesandbeyond.core.network.model.search.asModel
import com.keisardev.moviesandbeyond.core.network.retrofit.TmdbApi
import com.keisardev.moviesandbeyond.data.repository.SearchRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

internal class SearchRepositoryImpl @Inject constructor(
    private val tmdbApi: TmdbApi
) : SearchRepository {

    override suspend fun getSearchSuggestions(
        query: String,
        includeAdult: Boolean
    ): NetworkResponse<List<SearchItem>> {
        return try {
            val result = tmdbApi.multiSearch(
                query = query,
                includeAdult = includeAdult
            ).results
                .map { suggestion -> suggestion.asModel() }
            NetworkResponse.Success(result)
        } catch (e: IOException) {
            NetworkResponse.Error()
        } catch (e: HttpException) {
            NetworkResponse.Error()
        }
    }
}