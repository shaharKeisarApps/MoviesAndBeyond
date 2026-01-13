package com.keisardev.moviesandbeyond.data.store

import com.keisardev.moviesandbeyond.core.local.database.entity.CachedContentEntity
import com.keisardev.moviesandbeyond.core.model.content.ContentItem

/** Maps a [CachedContentEntity] to a [ContentItem] domain model. */
fun CachedContentEntity.toContentItem(): ContentItem =
    ContentItem(
        id = contentId,
        imagePath = imagePath,
        name = name,
        backdropPath = backdropPath,
        rating = rating,
        releaseDate = releaseDate,
        overview = overview)

/**
 * Maps a [ContentItem] domain model to a [CachedContentEntity] for database storage.
 *
 * @param category The category string for this content (e.g., "movie_popular", "tv_airing_today")
 * @param page The page number this content was fetched from
 * @param position The position within the page for maintaining order
 */
fun ContentItem.toCachedEntity(category: String, page: Int, position: Int): CachedContentEntity =
    CachedContentEntity(
        contentId = id,
        category = category,
        page = page,
        position = position,
        imagePath = imagePath,
        name = name,
        backdropPath = backdropPath,
        rating = rating,
        releaseDate = releaseDate,
        overview = overview,
        fetchedAt = System.currentTimeMillis())

/**
 * Maps a list of [ContentItem]s to [CachedContentEntity]s with proper positioning.
 *
 * @param category The category string for this content
 * @param page The page number this content was fetched from
 */
fun List<ContentItem>.toCachedEntities(category: String, page: Int): List<CachedContentEntity> =
    mapIndexed { index, item ->
        item.toCachedEntity(category, page, index)
    }
