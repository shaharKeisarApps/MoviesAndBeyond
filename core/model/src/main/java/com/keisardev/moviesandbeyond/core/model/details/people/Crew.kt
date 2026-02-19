package com.keisardev.moviesandbeyond.core.model.details.people

@androidx.compose.runtime.Immutable
data class Crew(
    val creditId: String,
    val department: String,
    val id: Int,
    val job: String,
    val name: String,
    val profilePath: String,
)
