package com.keisardev.moviesandbeyond.data.repository.impl

import com.keisardev.moviesandbeyond.core.model.Result
import com.keisardev.moviesandbeyond.core.model.SearchItem
import com.keisardev.moviesandbeyond.core.network.model.search.asModel
import com.keisardev.moviesandbeyond.core.network.retrofit.TmdbApi
import com.keisardev.moviesandbeyond.data.repository.SearchRepository
import java.io.IOException
import javax.inject.Inject
import retrofit2.HttpException

internal class SearchRepositoryImpl @Inject constructor(private val tmdbApi: TmdbApi) :
    SearchRepository {

    override suspend fun getSearchSuggestions(
        query: String,
        includeAdult: Boolean,
    ): Result<List<SearchItem>> {
        return try {
            val result =
                tmdbApi.multiSearch(query = query, includeAdult = includeAdult).results.map {
                    suggestion ->
                    suggestion.asModel()
                }
            Result.Success(result)
        } catch (e: IOException) {
            Result.Error(e)
        } catch (e: HttpException) {
            Result.Error(e, e.message)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
