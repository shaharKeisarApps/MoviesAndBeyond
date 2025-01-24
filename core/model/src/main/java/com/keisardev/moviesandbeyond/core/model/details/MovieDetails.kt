package com.keisardev.moviesandbeyond.core.model.details

import com.keisardev.moviesandbeyond.core.model.MediaType
import com.keisardev.moviesandbeyond.core.model.content.ContentItem
import com.keisardev.moviesandbeyond.core.model.details.people.Credits
import com.keisardev.moviesandbeyond.core.model.library.LibraryItem

data class MovieDetails(
    val adult: Boolean,
    val backdropPath: String,
//    val belongs_to_collection: Any,
    val budget: String,
    val credits: Credits,
    val genres: List<String>,
    val id: Int,
    val originalLanguage: String,
    val overview: String,
    val posterPath: String,
    val productionCompanies: String,
    val productionCountries: String,
    val rating: Double,
    val recommendations: List<ContentItem>,
    val releaseDate: String,
    val releaseYear: Int,
    val revenue: String,
    val runtime: String,
    val tagline: String,
    val title: String,
    val voteCount: Int
) {
    fun asLibraryItem() = LibraryItem(
        id = id,
        imagePath = posterPath,
        name = title,
        mediaType = MediaType.MOVIE.name.lowercase()
    )
}
