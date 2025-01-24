package com.keisardev.moviesandbeyond.data.repository

import com.keisardev.moviesandbeyond.core.model.MediaType
import com.keisardev.moviesandbeyond.core.model.library.LibraryItem
import com.keisardev.moviesandbeyond.data.util.LibraryTaskSyncOperation
import com.keisardev.moviesandbeyond.data.util.UserLibrarySyncOperations
import kotlinx.coroutines.flow.Flow

interface LibraryRepository: UserLibrarySyncOperations, LibraryTaskSyncOperation {
    val favoriteMovies: Flow<List<LibraryItem>>
    val favoriteTvShows: Flow<List<LibraryItem>>
    val moviesWatchlist: Flow<List<LibraryItem>>
    val tvShowsWatchlist: Flow<List<LibraryItem>>

    suspend fun itemInFavoritesExists(
        mediaId: Int,
        mediaType: MediaType
    ): Boolean

    suspend fun itemInWatchlistExists(
        mediaId: Int,
        mediaType: MediaType
    ): Boolean

    suspend fun addOrRemoveFavorite(libraryItem: LibraryItem)

    suspend fun addOrRemoveFromWatchlist(libraryItem: LibraryItem)
}