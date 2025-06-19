package com.keisardev.moviesandbeyond.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface NavKey

@Serializable
object OnboardingKey : NavKey

@Serializable
object AuthKey : NavKey

@Serializable
object MoviesKey : NavKey // Main entry for movies feature, shows movie feed

@Serializable
data class MoviesItemsKey(val category: String) : NavKey // For a specific category list of movies

@Serializable
object TvShowsKey : NavKey // Main entry for TV shows feature, shows TV feed

@Serializable
data class TvItemsKey(val category: String) : NavKey // For a specific category list of TV shows

@Serializable
object SearchKey : NavKey

@Serializable
object YouKey : NavKey // Main entry for You/Profile feature

@Serializable
data class LibraryItemsKey(val type: String) : NavKey // e.g., "favorites" or "watchlist"

@Serializable
data class DetailsKey(val itemId: String, val itemType: String) : NavKey

@Serializable
data class CreditsKey(val itemId: String, val itemType: String) : NavKey
