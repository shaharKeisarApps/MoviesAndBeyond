package com.keisardev.moviesandbeyond.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// ============================================================================
// Top-Level Navigation Keys (for bottom navigation tabs)
// ============================================================================

/**
 * Sealed class representing top-level navigation destinations. These correspond to the bottom
 * navigation bar tabs.
 */
@Serializable
sealed class TopLevelRoute : NavKey {
    @Serializable data object Movies : TopLevelRoute()

    @Serializable data object TvShows : TopLevelRoute()

    @Serializable data object Search : TopLevelRoute()

    @Serializable data object You : TopLevelRoute()
}

// ============================================================================
// Onboarding Routes
// ============================================================================

@Serializable data object OnboardingRoute : NavKey

// ============================================================================
// Movies Feature Routes
// ============================================================================

@Serializable data object MoviesFeedRoute : NavKey

@Serializable data class MoviesItemsRoute(val category: String) : NavKey

// ============================================================================
// TV Shows Feature Routes
// ============================================================================

@Serializable data object TvShowsFeedRoute : NavKey

@Serializable data class TvShowsItemsRoute(val category: String) : NavKey

// ============================================================================
// Search Feature Routes
// ============================================================================

@Serializable data object SearchRoute : NavKey

// ============================================================================
// You / Profile Feature Routes
// ============================================================================

@Serializable data object YouRoute : NavKey

@Serializable data class LibraryItemsRoute(val type: String) : NavKey

// ============================================================================
// Details Feature Routes
// ============================================================================

/**
 * Route for the details screen.
 *
 * @param id The media ID in format "movie_{id}" or "tv_{id}" or "person_{id}"
 */
@Serializable data class DetailsRoute(val id: String) : NavKey

/**
 * Route for the credits screen. Now carries its own id argument since we've flattened the nested
 * navigation.
 *
 * @param id The media ID for which to show credits
 */
@Serializable data class CreditsRoute(val id: String) : NavKey

// ============================================================================
// Auth Feature Routes
// ============================================================================

@Serializable data object AuthRoute : NavKey
