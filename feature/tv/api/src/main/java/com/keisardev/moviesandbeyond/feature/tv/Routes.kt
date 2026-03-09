package com.keisardev.moviesandbeyond.feature.tv

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object TvShowsFeedRoute : NavKey

@Serializable data class TvShowsItemsRoute(val category: String) : NavKey
