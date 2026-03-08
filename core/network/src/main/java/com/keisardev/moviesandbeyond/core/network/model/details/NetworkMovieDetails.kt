package com.keisardev.moviesandbeyond.core.network.model.details

import com.keisardev.moviesandbeyond.core.model.details.MovieDetails
import com.keisardev.moviesandbeyond.core.network.model.content.NetworkContentItem
import com.keisardev.moviesandbeyond.core.network.model.content.NetworkContentResponse
import com.keisardev.moviesandbeyond.core.network.util.formatDate
import java.util.Locale
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkMovieDetails(
    val adult: Boolean,
    @SerialName("backdrop_path") val backdropPath: String?,
    //    val belongs_to_collection: Any,
    val budget: Int,
    val credits: NetworkCredits,
    val genres: List<NetworkGenre>?,
    val id: Int,
    @SerialName("original_language") val originalLanguage: String,
    val overview: String,
    val popularity: Double,
    @SerialName("poster_path") val posterPath: String?,
    @SerialName("production_companies") val productionCompanies: List<NetworkProductionCompany>,
    @SerialName("production_countries") val productionCountries: List<NetworkProductionCountry>,
    val recommendations: NetworkContentResponse,
    @SerialName("release_date") val releaseDate: String,
    val revenue: Int,
    val runtime: Int,
    val tagline: String,
    val title: String,
    @SerialName("vote_average") val voteAverage: Double,
    @SerialName("vote_count") val voteCount: Int,
) {
    fun asModel() =
        MovieDetails(
            adult = adult,
            backdropPath = backdropPath ?: "",
            budget = "%,d".format(budget),
            credits = credits.asModel(),
            genres = genres?.map { it.name } ?: emptyList(),
            id = id,
            originalLanguage = Locale(originalLanguage).displayLanguage,
            overview = overview,
            posterPath = posterPath ?: "",
            productionCompanies = productionCompanies.joinToString(separator = ", ") { it.name },
            productionCountries = productionCountries.joinToString(separator = ", ") { it.name },
            rating = voteAverage / 2,
            recommendations = recommendations.results.map(NetworkContentItem::asModel),
            releaseDate = formatDate(releaseDate),
            releaseYear = releaseDate.split("-").first().toInt(),
            revenue = "%,d".format(revenue),
            runtime = getFormattedRuntime(),
            tagline = tagline,
            title = title,
            voteCount = voteCount,
        )

    private fun getFormattedRuntime(): String {
        val hours = runtime.div(60)
        val minutes = runtime.mod(60)
        return if (minutes < 1) {
            "${hours}h"
        } else {
            "${hours}h ${minutes}m"
        }
    }
}
