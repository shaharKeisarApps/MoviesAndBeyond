package com.keisardev.moviesandbeyond.core.network.model.details.people

import com.keisardev.moviesandbeyond.core.model.details.people.Crew
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkCrew(
    @SerialName("credit_id") val creditId: String,
    val department: String?,
    val id: Int,
    val job: String?,
    val name: String,
    @SerialName("profile_path") val profilePath: String?,
) {
    fun asModel() =
        Crew(
            creditId = creditId,
            department = department ?: "",
            id = id,
            job = job ?: "",
            name = name,
            profilePath = profilePath ?: "",
        )
}
