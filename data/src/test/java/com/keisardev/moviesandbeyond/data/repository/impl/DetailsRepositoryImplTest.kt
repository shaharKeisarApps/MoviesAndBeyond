package com.keisardev.moviesandbeyond.data.repository.impl

import com.keisardev.moviesandbeyond.core.model.Result
import com.keisardev.moviesandbeyond.core.model.error.NetworkError
import com.keisardev.moviesandbeyond.core.network.model.content.NetworkContentResponse
import com.keisardev.moviesandbeyond.core.network.model.details.NetworkCredits
import com.keisardev.moviesandbeyond.core.network.model.details.NetworkMovieDetails
import com.keisardev.moviesandbeyond.core.network.model.details.people.NetworkPersonDetails
import com.keisardev.moviesandbeyond.core.network.model.details.tv.NetworkEpisodeDetails
import com.keisardev.moviesandbeyond.core.network.model.details.tv.NetworkTvDetails
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DetailsRepositoryImplTest {

    private lateinit var fakeTmdbApi: FakeTmdbApi
    private lateinit var repository: DetailsRepositoryImpl

    @Before
    fun setUp() {
        fakeTmdbApi = FakeTmdbApi()
        repository = DetailsRepositoryImpl(fakeTmdbApi)
    }

    // region getMovieDetails

    @Test
    fun `getMovieDetails success returns Result Success with mapped movie details`() = runTest {
        fakeTmdbApi.networkMovieDetails = testNetworkMovieDetails

        val result = repository.getMovieDetails(100)

        assertTrue(result is Result.Success)
        val movieDetails = (result as Result.Success).data
        assertEquals(100, movieDetails.id)
        assertEquals("Test Movie", movieDetails.title)
    }

    @Test
    fun `getMovieDetails with IOException returns Result Error`() = runTest {
        fakeTmdbApi.exception = IOException("No network")

        val result = repository.getMovieDetails(100)

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is IOException)
    }

    @Test
    fun `getMovieDetails with NetworkError returns Result Error with message`() = runTest {
        fakeTmdbApi.exception = NetworkError.NotFound()

        val result = repository.getMovieDetails(100)

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is NetworkError.NotFound)
        assertEquals("Not found (404)", result.message)
    }

    // endregion

    // region getTvShowDetails

    @Test
    fun `getTvShowDetails success returns Result Success with mapped tv details`() = runTest {
        fakeTmdbApi.networkTvDetails = testNetworkTvDetails

        val result = repository.getTvShowDetails(101)

        assertTrue(result is Result.Success)
        val tvDetails = (result as Result.Success).data
        assertEquals(101, tvDetails.id)
        assertEquals("Test Show", tvDetails.name)
    }

    @Test
    fun `getTvShowDetails with IOException returns Result Error`() = runTest {
        fakeTmdbApi.exception = IOException("Connection refused")

        val result = repository.getTvShowDetails(101)

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is IOException)
    }

    @Test
    fun `getTvShowDetails with NetworkError returns Result Error`() = runTest {
        fakeTmdbApi.exception = NetworkError.ServerError(500)

        val result = repository.getTvShowDetails(101)

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is NetworkError.ServerError)
    }

    // endregion

    // region getPersonDetails

    @Test
    fun `getPersonDetails success returns Result Success with mapped person details`() = runTest {
        fakeTmdbApi.networkPersonDetails = testNetworkPersonDetails

        val result = repository.getPersonDetails(102)

        assertTrue(result is Result.Success)
        val personDetails = (result as Result.Success).data
        assertEquals(102, personDetails.id)
        assertEquals("Test Person", personDetails.name)
    }

    @Test
    fun `getPersonDetails with NetworkError returns Result Error`() = runTest {
        fakeTmdbApi.exception = NetworkError.Unauthorized()

        val result = repository.getPersonDetails(102)

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is NetworkError.Unauthorized)
    }

    // endregion
}

// region Test fixtures

private val emptyContentResponse =
    NetworkContentResponse(page = 1, results = emptyList(), totalPages = 1, totalResults = 0)

private val testNetworkMovieDetails =
    NetworkMovieDetails(
        adult = false,
        backdropPath = "/backdrop.jpg",
        budget = 1000000,
        credits = NetworkCredits(cast = emptyList(), crew = emptyList()),
        genres = emptyList(),
        id = 100,
        originalLanguage = "en",
        overview = "A test movie overview",
        popularity = 10.0,
        posterPath = "/poster.jpg",
        productionCompanies = emptyList(),
        productionCountries = emptyList(),
        recommendations = emptyContentResponse,
        releaseDate = "2024-01-15",
        revenue = 5000000,
        runtime = 120,
        tagline = "Test tagline",
        title = "Test Movie",
        voteAverage = 7.5,
        voteCount = 100,
    )

private val testNetworkTvDetails =
    NetworkTvDetails(
        adult = false,
        backdropPath = "/backdrop.jpg",
        createdBy = emptyList(),
        credits = NetworkCredits(cast = emptyList(), crew = emptyList()),
        episodeRunTime = listOf(45),
        firstAirDate = "2024-01-15",
        genres = emptyList(),
        id = 101,
        inProduction = true,
        lastAirDate = "2024-06-15",
        lastEpisodeToAir =
            NetworkEpisodeDetails(
                airDate = "2024-06-15",
                episodeNumber = 10,
                id = 1000,
                name = "Finale",
                overview = "Episode overview",
                productionCode = "",
                runtime = 45,
                seasonNumber = 1,
                showId = 101,
                stillPath = null,
                voteAverage = 8.0,
                voteCount = 50,
            ),
        name = "Test Show",
        networks = emptyList(),
        nextEpisodeToAir = null,
        numberOfEpisodes = 10,
        numberOfSeasons = 1,
        originCountry = listOf("US"),
        originalLanguage = "en",
        overview = "A test show overview",
        posterPath = "/poster.jpg",
        productionCompanies = emptyList(),
        productionCountries = emptyList(),
        recommendations = emptyContentResponse,
        status = "Returning Series",
        tagline = "Test tagline",
        type = "Scripted",
        voteAverage = 8.0,
        voteCount = 200,
    )

private val testNetworkPersonDetails =
    NetworkPersonDetails(
        adult = false,
        alsoKnownAs = emptyList(),
        biography = "A test biography",
        birthday = "1990-05-20",
        deathday = null,
        gender = 2,
        id = 102,
        knownForDepartment = "Acting",
        name = "Test Person",
        placeOfBirth = "Los Angeles, CA",
        profilePath = "/profile.jpg",
    )

// endregion
