package com.keisardev.moviesandbeyond.data.repository

import com.keisardev.moviesandbeyond.core.model.Result
import com.keisardev.moviesandbeyond.core.model.details.MovieDetails
import com.keisardev.moviesandbeyond.core.model.details.people.PersonDetails
import com.keisardev.moviesandbeyond.core.model.details.tv.TvDetails

/**
 * Repository for fetching detailed information about movies, TV shows, and people from TMDB.
 *
 * Each method returns a [Result] wrapping the full detail model including cast, crew, and related
 * metadata.
 */
interface DetailsRepository {
    /**
     * Fetches full movie details including cast, crew, and recommendations.
     *
     * @param id The TMDB movie ID
     */
    suspend fun getMovieDetails(id: Int): Result<MovieDetails>

    /**
     * Fetches full TV show details including seasons, cast, and recommendations.
     *
     * @param id The TMDB TV show ID
     */
    suspend fun getTvShowDetails(id: Int): Result<TvDetails>

    /**
     * Fetches full person details including filmography and biography.
     *
     * @param id The TMDB person ID
     */
    suspend fun getPersonDetails(id: Int): Result<PersonDetails>
}
