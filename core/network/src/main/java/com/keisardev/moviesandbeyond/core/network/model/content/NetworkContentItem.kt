package com.keisardev.moviesandbeyond.core.network.model.content

import com.keisardev.moviesandbeyond.core.model.content.ContentItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkContentItem(
    val id: Int,
    val name: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    val title: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("vote_average") val voteAverage: Double? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("first_air_date") val firstAirDate: String? = null,
    val overview: String? = null,
) {
    fun asModel() =
        ContentItem(
            id = id,
            imagePath = posterPath ?: "",
            name = name ?: title ?: "",
            backdropPath = backdropPath,
            rating = voteAverage,
            releaseDate = releaseDate ?: firstAirDate,
            overview = overview,
        )
}
