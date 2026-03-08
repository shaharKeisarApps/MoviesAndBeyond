package com.keisardev.moviesandbeyond.core.network.model.details.people

import com.keisardev.moviesandbeyond.core.model.details.people.Cast
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkCast(
    val character: String?,
    val id: Int,
    val name: String,
    @SerialName("profile_path") val profilePath: String?,
) {
    fun asModel() =
        Cast(character = character ?: "", id = id, name = name, profilePath = profilePath ?: "")
}
