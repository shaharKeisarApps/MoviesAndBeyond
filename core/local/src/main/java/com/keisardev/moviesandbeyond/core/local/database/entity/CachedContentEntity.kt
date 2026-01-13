package com.keisardev.moviesandbeyond.core.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 * Entity for caching movie and TV show content items for offline-first support.
 *
 * Uses composite primary key of [contentId] and [category] since the same content can appear in
 * multiple categories (e.g., a movie could be both "popular" and "top_rated").
 *
 * @param contentId The TMDB content ID (movie or TV show)
 * @param category Category identifier (e.g., "movie_now_playing", "tv_airing_today")
 * @param page The page number this content was fetched from (for pagination)
 * @param position Position within the page for maintaining order
 * @param imagePath Poster image path
 * @param name Display name (movie title or TV show name)
 * @param backdropPath Optional backdrop image path
 * @param rating Optional vote average from TMDB
 * @param releaseDate Optional release date string
 * @param overview Optional description/synopsis
 * @param fetchedAt Timestamp when this data was fetched (for freshness validation)
 */
@Entity(
    tableName = "cached_content",
    primaryKeys = ["content_id", "category"],
    indices = [Index(value = ["category"]), Index(value = ["fetched_at"])])
data class CachedContentEntity(
    @ColumnInfo(name = "content_id") val contentId: Int,
    val category: String,
    val page: Int,
    val position: Int,
    @ColumnInfo(name = "image_path") val imagePath: String,
    val name: String,
    @ColumnInfo(name = "backdrop_path") val backdropPath: String?,
    val rating: Double?,
    @ColumnInfo(name = "release_date") val releaseDate: String?,
    val overview: String?,
    @ColumnInfo(name = "fetched_at") val fetchedAt: Long = System.currentTimeMillis()
)
