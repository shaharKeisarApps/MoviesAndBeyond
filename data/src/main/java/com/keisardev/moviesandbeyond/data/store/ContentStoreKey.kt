package com.keisardev.moviesandbeyond.data.store

import com.keisardev.moviesandbeyond.core.model.content.MovieListCategory
import com.keisardev.moviesandbeyond.core.model.content.TvShowListCategory

/**
 * Key for movie content Store. Combines category and page for unique identification.
 *
 * @param category The movie list category (now_playing, popular, top_rated, upcoming)
 * @param page The page number for pagination (1-indexed)
 */
data class MovieContentKey(val category: MovieListCategory, val page: Int) {
    /** Returns the category string used for database storage. */
    fun toCategoryString(): String = "movie_${category.categoryName}"
}

/**
 * Key for TV show content Store. Combines category and page for unique identification.
 *
 * @param category The TV show list category (airing_today, popular, top_rated, on_the_air)
 * @param page The page number for pagination (1-indexed)
 */
data class TvContentKey(val category: TvShowListCategory, val page: Int) {
    /** Returns the category string used for database storage. */
    fun toCategoryString(): String = "tv_${category.categoryName}"
}
