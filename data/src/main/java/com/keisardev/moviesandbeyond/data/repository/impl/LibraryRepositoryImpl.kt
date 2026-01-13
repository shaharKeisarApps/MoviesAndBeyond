package com.keisardev.moviesandbeyond.data.repository.impl

import com.keisardev.moviesandbeyond.core.local.database.dao.FavoriteContentDao
import com.keisardev.moviesandbeyond.core.local.database.dao.WatchlistContentDao
import com.keisardev.moviesandbeyond.core.local.database.entity.FavoriteContentEntity
import com.keisardev.moviesandbeyond.core.local.database.entity.WatchlistContentEntity
import com.keisardev.moviesandbeyond.core.local.database.entity.asFavoriteContentEntity
import com.keisardev.moviesandbeyond.core.local.database.entity.asWatchlistContentEntity
import com.keisardev.moviesandbeyond.core.model.MediaType
import com.keisardev.moviesandbeyond.core.model.library.LibraryItem
import com.keisardev.moviesandbeyond.core.model.library.LibraryItemType
import com.keisardev.moviesandbeyond.data.repository.LibraryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Local-only implementation of LibraryRepository. Manages favorites and watchlist entirely through
 * local Room database storage without TMDB API synchronization.
 */
internal class LibraryRepositoryImpl
@Inject
constructor(
    private val favoriteContentDao: FavoriteContentDao,
    private val watchlistContentDao: WatchlistContentDao,
) : LibraryRepository {
    override val favoriteMovies: Flow<List<LibraryItem>> =
        favoriteContentDao.getFavoriteMovies().map { it.map(FavoriteContentEntity::asLibraryItem) }

    override val favoriteTvShows: Flow<List<LibraryItem>> =
        favoriteContentDao.getFavoriteTvShows().map { it.map(FavoriteContentEntity::asLibraryItem) }

    override val moviesWatchlist: Flow<List<LibraryItem>> =
        watchlistContentDao.getMoviesWatchlist().map {
            it.map(WatchlistContentEntity::asLibraryItem)
        }

    override val tvShowsWatchlist: Flow<List<LibraryItem>> =
        watchlistContentDao.getTvShowsWatchlist().map {
            it.map(WatchlistContentEntity::asLibraryItem)
        }

    override suspend fun itemInFavoritesExists(mediaId: Int, mediaType: MediaType): Boolean {
        return favoriteContentDao.checkFavoriteItemExists(
            mediaId = mediaId, mediaType = mediaType.name.lowercase())
    }

    override suspend fun itemInWatchlistExists(mediaId: Int, mediaType: MediaType): Boolean {
        return watchlistContentDao.checkWatchlistItemExists(
            mediaId = mediaId, mediaType = mediaType.name.lowercase())
    }

    override suspend fun addOrRemoveFavorite(libraryItem: LibraryItem) {
        val itemExists =
            favoriteContentDao.checkFavoriteItemExists(
                mediaId = libraryItem.id, mediaType = libraryItem.mediaType)
        if (itemExists) {
            favoriteContentDao.deleteFavoriteItem(
                mediaId = libraryItem.id, mediaType = libraryItem.mediaType)
        } else {
            favoriteContentDao.insertFavoriteItem(libraryItem.asFavoriteContentEntity())
        }
    }

    override suspend fun addOrRemoveFromWatchlist(libraryItem: LibraryItem) {
        val itemExists =
            watchlistContentDao.checkWatchlistItemExists(
                mediaId = libraryItem.id, mediaType = libraryItem.mediaType)
        if (itemExists) {
            watchlistContentDao.deleteWatchlistItem(
                mediaId = libraryItem.id, mediaType = libraryItem.mediaType)
        } else {
            watchlistContentDao.insertWatchlistItem(libraryItem.asWatchlistContentEntity())
        }
    }

    // Local-only implementation - no network sync needed
    override suspend fun executeLibraryTask(
        id: Int,
        mediaType: MediaType,
        libraryItemType: LibraryItemType,
        itemExistsLocally: Boolean
    ): Boolean = true // Always succeeds since we're local-only

    // Local-only implementation - no network sync needed
    override suspend fun syncFavorites(): Boolean = true

    // Local-only implementation - no network sync needed
    override suspend fun syncWatchlist(): Boolean = true
}
