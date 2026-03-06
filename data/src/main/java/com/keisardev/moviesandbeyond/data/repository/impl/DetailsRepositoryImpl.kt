package com.keisardev.moviesandbeyond.data.repository.impl

import com.keisardev.moviesandbeyond.core.model.Result
import com.keisardev.moviesandbeyond.core.model.details.MovieDetails
import com.keisardev.moviesandbeyond.core.model.details.people.PersonDetails
import com.keisardev.moviesandbeyond.core.model.details.tv.TvDetails
import com.keisardev.moviesandbeyond.core.network.retrofit.TmdbApi
import com.keisardev.moviesandbeyond.data.repository.DetailsRepository
import java.io.IOException
import javax.inject.Inject
import retrofit2.HttpException

class DetailsRepositoryImpl @Inject constructor(private val tmdbApi: TmdbApi) : DetailsRepository {
    override suspend fun getMovieDetails(id: Int): Result<MovieDetails> {
        return try {
            Result.Success(tmdbApi.getMovieDetails(id).asModel())
        } catch (e: IOException) {
            Result.Error(e)
        } catch (e: HttpException) {
            Result.Error(e, e.message)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getTvShowDetails(id: Int): Result<TvDetails> {
        return try {
            Result.Success(tmdbApi.getTvShowDetails(id).asModel())
        } catch (e: IOException) {
            Result.Error(e)
        } catch (e: HttpException) {
            Result.Error(e, e.message)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getPersonDetails(id: Int): Result<PersonDetails> {
        return try {
            Result.Success(tmdbApi.getPersonDetails(id).asModel())
        } catch (e: IOException) {
            Result.Error(e)
        } catch (e: HttpException) {
            Result.Error(e, e.message)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
