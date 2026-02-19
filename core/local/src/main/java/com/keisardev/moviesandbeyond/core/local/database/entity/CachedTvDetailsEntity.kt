package com.keisardev.moviesandbeyond.core.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for caching TV show details for offline-first support.
 *
 * Stores the full TV show details as JSON string to avoid complex nested entity relationships while
 * maintaining fast read/write operations.
 *
 * @param id The TMDB TV show ID
 * @param detailsJson JSON-serialized TvDetails object
 * @param fetchedAt Timestamp when this data was fetched (for freshness validation)
 */
@Entity(tableName = "cached_tv_details")
data class CachedTvDetailsEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "details_json") val detailsJson: String,
    @ColumnInfo(name = "fetched_at") val fetchedAt: Long = System.currentTimeMillis(),
)
