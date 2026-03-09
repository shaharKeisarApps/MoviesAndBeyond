package com.keisardev.moviesandbeyond.core.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Sealed class representing top-level navigation destinations. These correspond to the bottom
 * navigation bar tabs. These are pure tab identifiers and never appear in the back stack.
 */
@Serializable
sealed class TopLevelRoute : NavKey {
    @Serializable data object Movies : TopLevelRoute()

    @Serializable data object TvShows : TopLevelRoute()

    @Serializable data object Search : TopLevelRoute()

    @Serializable data object You : TopLevelRoute()
}

@Serializable data object OnboardingRoute : NavKey
