package com.keisardev.moviesandbeyond.feature.movies

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object MoviesFeedRoute : NavKey

@Serializable data class MoviesItemsRoute(val category: String) : NavKey
