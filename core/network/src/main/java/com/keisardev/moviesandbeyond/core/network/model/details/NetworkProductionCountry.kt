package com.keisardev.moviesandbeyond.core.network.model.details

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkProductionCountry(
    @SerialName("iso_3166_1") val iso31661: String,
    val name: String,
)
