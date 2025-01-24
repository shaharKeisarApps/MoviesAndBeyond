package com.keisardev.moviesandbeyond.core.network.model.details.tv

import com.keisardev.moviesandbeyond.core.model.details.tv.CreatedBy
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkCreatedBy(
    val id: Int,
    val name: String
) {
    fun asModel() = CreatedBy(
        id = id,
        name = name
    )
}
