package com.keisardev.moviesandbeyond.feature.details

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Route for the details screen.
 *
 * @param id The media ID in format "movie_{id}" or "tv_{id}" or "person_{id}"
 */
@Serializable data class DetailsRoute(val id: String) : NavKey

/**
 * Route for the credits screen.
 *
 * @param id The media ID for which to show credits
 */
@Serializable data class CreditsRoute(val id: String) : NavKey
