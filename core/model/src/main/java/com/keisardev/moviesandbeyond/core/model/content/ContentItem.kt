package com.keisardev.moviesandbeyond.core.model.content

/**
 * Domain model for content items (movies/TV shows) in lists and grids.
 *
 * @param id TMDB content ID
 * @param imagePath Poster image path (without base URL)
 * @param name Display name (movie title or TV show name)
 * @param backdropPath Backdrop image path for hero/featured sections (optional)
 * @param rating Vote average from TMDB (0.0-10.0, optional)
 * @param releaseDate Release date string (optional)
 * @param overview Short description/synopsis (optional)
 */
@androidx.compose.runtime.Immutable
data class ContentItem(
    val id: Int,
    val imagePath: String,
    val name: String,
    val backdropPath: String? = null,
    val rating: Double? = null,
    val releaseDate: String? = null,
    val overview: String? = null
)
