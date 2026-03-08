package com.keisardev.moviesandbeyond.core.network.model.details.tv

import com.keisardev.moviesandbeyond.core.model.details.tv.TvDetails
import com.keisardev.moviesandbeyond.core.network.model.content.NetworkContentItem
import com.keisardev.moviesandbeyond.core.network.model.content.NetworkContentResponse
import com.keisardev.moviesandbeyond.core.network.model.details.NetworkCredits
import com.keisardev.moviesandbeyond.core.network.model.details.NetworkGenre
import com.keisardev.moviesandbeyond.core.network.model.details.NetworkProductionCompany
import com.keisardev.moviesandbeyond.core.network.model.details.NetworkProductionCountry
import com.keisardev.moviesandbeyond.core.network.util.formatDate
import java.util.Locale
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkTvDetails(
    val adult: Boolean,
    @SerialName("backdrop_path") val backdropPath: String?,
    @SerialName("created_by") val createdBy: List<NetworkCreatedBy>,
    val credits: NetworkCredits,
    @SerialName("episode_run_time") val episodeRunTime: List<Int>,
    @SerialName("first_air_date") val firstAirDate: String,
    val genres: List<NetworkGenre>?,
    val id: Int,
    @SerialName("in_production") val inProduction: Boolean,
    @SerialName("last_air_date") val lastAirDate: String,
    @SerialName("last_episode_to_air") val lastEpisodeToAir: NetworkEpisodeDetails,
    val name: String,
    val networks: List<NetworkBroadcastNetwork>,
    @SerialName("next_episode_to_air") val nextEpisodeToAir: NetworkEpisodeDetails?,
    @SerialName("number_of_episodes") val numberOfEpisodes: Int,
    @SerialName("number_of_seasons") val numberOfSeasons: Int,
    @SerialName("origin_country") val originCountry: List<String>,
    @SerialName("original_language") val originalLanguage: String,
    val overview: String,
    @SerialName("poster_path") val posterPath: String?,
    @SerialName("production_companies") val productionCompanies: List<NetworkProductionCompany>,
    @SerialName("production_countries") val productionCountries: List<NetworkProductionCountry>,
    val recommendations: NetworkContentResponse,
    //    val seasons: List<Season>,
    val status: String,
    val tagline: String,
    val type: String,
    @SerialName("vote_average") val voteAverage: Double,
    @SerialName("vote_count") val voteCount: Int,
) {
    fun asModel() =
        TvDetails(
            adult = adult,
            backdropPath = backdropPath ?: "",
            createdBy = createdBy.map(NetworkCreatedBy::asModel),
            credits = credits.asModel(),
            episodeRunTime = getFormattedRuntime(),
            firstAirDate = formatDate(firstAirDate),
            genres = genres?.map { it.name } ?: emptyList(),
            id = id,
            inProduction = if (inProduction) "Yes" else "No",
            lastAirDate = formatDate(lastAirDate),
            lastEpisodeToAir = lastEpisodeToAir.asModel(),
            name = name,
            networks = networks.joinToString(separator = ", ") { it.name },
            nextEpisodeToAir = nextEpisodeToAir?.asModel(),
            numberOfEpisodes = numberOfEpisodes,
            numberOfSeasons = numberOfSeasons,
            originCountry = originCountry,
            originalLanguage = Locale(originalLanguage).displayLanguage,
            overview = overview,
            posterPath = posterPath ?: "",
            productionCompanies = productionCompanies.joinToString(separator = ", ") { it.name },
            productionCountries = productionCountries.joinToString(separator = ", ") { it.name },
            recommendations = recommendations.results.map(NetworkContentItem::asModel),
            releaseYear = firstAirDate.split("-").first().toInt(),
            status = status,
            tagline = tagline,
            type = type,
            rating = voteAverage / 2,
            voteCount = voteCount,
        )

    private fun getFormattedRuntime(): String {
        if (episodeRunTime.isEmpty()) return ""

        val hours = episodeRunTime.first().div(60)
        val minutes = episodeRunTime.first().mod(60)
        return if (hours < 1) {
            "${minutes}m"
        } else if (minutes < 1) {
            "${hours}h"
        } else {
            "${hours}h ${minutes}m"
        }
    }
}
