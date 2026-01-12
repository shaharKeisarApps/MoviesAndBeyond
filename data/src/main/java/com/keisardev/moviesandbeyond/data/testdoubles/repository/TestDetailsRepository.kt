package com.keisardev.moviesandbeyond.data.testdoubles.repository

import com.keisardev.moviesandbeyond.core.model.NetworkResponse
import com.keisardev.moviesandbeyond.core.model.details.MovieDetails
import com.keisardev.moviesandbeyond.core.model.details.people.PersonDetails
import com.keisardev.moviesandbeyond.core.model.details.tv.TvDetails
import com.keisardev.moviesandbeyond.data.repository.DetailsRepository
import com.keisardev.moviesandbeyond.data.testdoubles.testMovieDetail
import com.keisardev.moviesandbeyond.data.testdoubles.testPersonDetails
import com.keisardev.moviesandbeyond.data.testdoubles.testTvShowDetails

class TestDetailsRepository : DetailsRepository {
    private var generateError = false

    override suspend fun getMovieDetails(id: Int): NetworkResponse<MovieDetails> {
        return if (!generateError) {
            NetworkResponse.Success(data = testMovieDetail)
        } else {
            NetworkResponse.Error()
        }
    }

    override suspend fun getTvShowDetails(id: Int): NetworkResponse<TvDetails> {
        return if (!generateError) {
            NetworkResponse.Success(data = testTvShowDetails)
        } else {
            NetworkResponse.Error()
        }
    }

    override suspend fun getPersonDetails(id: Int): NetworkResponse<PersonDetails> {
        return if (!generateError) {
            NetworkResponse.Success(data = testPersonDetails)
        } else {
            NetworkResponse.Error()
        }
    }

    fun generateError(value: Boolean) {
        generateError = value
    }
}
