package com.keisardev.moviesandbeyond.core.network.model.details

import com.keisardev.moviesandbeyond.core.model.details.people.Credits
import com.keisardev.moviesandbeyond.core.network.model.details.people.NetworkCast
import com.keisardev.moviesandbeyond.core.network.model.details.people.NetworkCrew
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkCredits(val cast: List<NetworkCast>, val crew: List<NetworkCrew>) {
    fun asModel() =
        Credits(cast = cast.map(NetworkCast::asModel), crew = crew.map(NetworkCrew::asModel))
}
