package com.keisardev.moviesandbeyond.core.network.model.details.tv

import com.keisardev.moviesandbeyond.core.model.details.tv.EpisodeDetails
import com.keisardev.moviesandbeyond.core.network.util.formatDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkEpisodeDetails(
    @SerialName("air_date") val airDate: String,
    @SerialName("episode_number") val episodeNumber: Int,
    val id: Int,
    val name: String,
    val overview: String,
    @SerialName("production_code") val productionCode: String,
    val runtime: Int?,
    @SerialName("season_number") val seasonNumber: Int,
    @SerialName("show_id") val showId: Int,
    @SerialName("still_path") val stillPath: String?,
    @SerialName("vote_average") val voteAverage: Double,
    @SerialName("vote_count") val voteCount: Int,
) {
    fun asModel() =
        EpisodeDetails(
            airDate = formatDate(airDate),
            episodeNumber = episodeNumber,
            id = id,
            name = name,
            overview = overview,
            productionCode = productionCode,
            runtime = runtime,
            seasonNumber = seasonNumber,
            showId = showId,
            stillPath = stillPath ?: "",
            voteAverage = voteAverage,
            voteCount = voteCount,
        )
}
