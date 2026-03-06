package com.keisardev.moviesandbeyond.data.testdoubles.repository

import com.keisardev.moviesandbeyond.core.model.Result
import com.keisardev.moviesandbeyond.core.model.details.MovieDetails
import com.keisardev.moviesandbeyond.core.model.details.people.PersonDetails
import com.keisardev.moviesandbeyond.core.model.details.tv.TvDetails
import com.keisardev.moviesandbeyond.data.repository.DetailsRepository
import com.keisardev.moviesandbeyond.data.testdoubles.testMovieDetail
import com.keisardev.moviesandbeyond.data.testdoubles.testPersonDetails
import com.keisardev.moviesandbeyond.data.testdoubles.testTvShowDetails

class TestDetailsRepository : DetailsRepository {
    private var generateError = false

    override suspend fun getMovieDetails(id: Int): Result<MovieDetails> {
        return if (!generateError) {
            Result.Success(data = testMovieDetail)
        } else {
            Result.Error(RuntimeException("Movie details fetch failed"))
        }
    }

    override suspend fun getTvShowDetails(id: Int): Result<TvDetails> {
        return if (!generateError) {
            Result.Success(data = testTvShowDetails)
        } else {
            Result.Error(RuntimeException("TV show details fetch failed"))
        }
    }

    override suspend fun getPersonDetails(id: Int): Result<PersonDetails> {
        return if (!generateError) {
            Result.Success(data = testPersonDetails)
        } else {
            Result.Error(RuntimeException("Person details fetch failed"))
        }
    }

    fun generateError(value: Boolean) {
        generateError = value
    }
}
