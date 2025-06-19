package com.keisardev.moviesandbeyond.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object NavigationKeys { // Changed to an object to hold nested keys

    @Serializable
    object OnboardingKey

    @Serializable
    object AuthKey

    @Serializable
    object MoviesKey // Main entry for movies feature, shows movie feed

    @Serializable
    data class MoviesItemsKey(val category: String) // For a specific category list of movies

    @Serializable
    object TvShowsKey // Main entry for TV shows feature, shows TV feed

    @Serializable
    data class TvItemsKey(val category: String) // For a specific category list of TV shows

    @Serializable
    object SearchKey

    @Serializable
    object YouKey // Main entry for You/Profile feature

    @Serializable
    data class LibraryItemsKey(val type: String) // e.g., "favorites" or "watchlist"

    @Serializable
    data class DetailsKey(val itemId: String, val itemType: String)

    @Serializable
    data class CreditsKey(val itemId: String, val itemType: String)
}
