package com.keisardev.moviesandbeyond.core.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for caching movie details for offline-first support.
 *
 * Stores the full movie details as JSON string to avoid complex nested entity relationships while
 * maintaining fast read/write operations.
 *
 * @param id The TMDB movie ID
 * @param detailsJson JSON-serialized MovieDetails object
 * @param fetchedAt Timestamp when this data was fetched (for freshness validation)
 */
@Entity(tableName = "cached_movie_details")
data class CachedMovieDetailsEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "details_json") val detailsJson: String,
    @ColumnInfo(name = "fetched_at") val fetchedAt: Long = System.currentTimeMillis(),
)
