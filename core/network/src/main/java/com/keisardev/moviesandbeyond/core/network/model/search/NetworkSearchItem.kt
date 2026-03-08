package com.keisardev.moviesandbeyond.core.network.model.search

import com.keisardev.moviesandbeyond.core.model.SearchItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkSearchItem(
    val id: Int,
    val name: String? = null,
    val title: String? = null,
    @SerialName("media_type") val mediaType: String,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("profile_path") val profilePath: String? = null,
)

fun NetworkSearchItem.asModel() =
    SearchItem(
        id = id,
        name = title ?: name ?: "",
        mediaType = mediaType,
        imagePath = posterPath ?: profilePath ?: "",
    )
