package com.keisardev.moviesandbeyond.core.network.model.content

import com.keisardev.moviesandbeyond.core.model.content.ContentItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkContentItem(
    val id: Int,
    val name: String?,
    @Json(name = "poster_path") val posterPath: String?,
    val title: String?,
    @Json(name = "backdrop_path") val backdropPath: String?,
    @Json(name = "vote_average") val voteAverage: Double?,
    @Json(name = "release_date") val releaseDate: String?,
    @Json(name = "first_air_date") val firstAirDate: String?,
    val overview: String?,
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
