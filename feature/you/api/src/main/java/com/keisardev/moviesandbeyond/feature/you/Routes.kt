package com.keisardev.moviesandbeyond.feature.you

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object YouRoute : NavKey

@Serializable data class LibraryItemsRoute(val type: String) : NavKey
