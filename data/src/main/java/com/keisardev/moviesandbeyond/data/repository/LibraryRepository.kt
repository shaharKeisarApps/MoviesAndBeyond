package com.keisardev.moviesandbeyond.data.repository

import com.keisardev.moviesandbeyond.core.model.MediaType
import com.keisardev.moviesandbeyond.core.model.library.LibraryItem
import com.keisardev.moviesandbeyond.core.model.library.SyncStatus
import com.keisardev.moviesandbeyond.data.util.LibraryTaskSyncOperation
import com.keisardev.moviesandbeyond.data.util.UserLibrarySyncOperations
import kotlinx.coroutines.flow.Flow

/** Result of a sync operation between local and remote library. */
data class SyncResult(
    val pushed: Int,
    val pulled: Int,
    val conflicts: Int,
    val errors: List<String>,
)

interface LibraryRepository : UserLibrarySyncOperations, LibraryTaskSyncOperation {
    val favoriteMovies: Flow<List<LibraryItem>>
    val favoriteTvShows: Flow<List<LibraryItem>>
    val moviesWatchlist: Flow<List<LibraryItem>>
    val tvShowsWatchlist: Flow<List<LibraryItem>>

    suspend fun itemInFavoritesExists(mediaId: Int, mediaType: MediaType): Boolean

    suspend fun itemInWatchlistExists(mediaId: Int, mediaType: MediaType): Boolean

    /**
     * Adds or removes a favorite item. In guest mode (isAuthenticated=false), items are saved
     * locally only. In authenticated mode, items are saved locally and synced to TMDB.
     *
     * @param libraryItem The item to add or remove
     * @param isAuthenticated Whether the user is authenticated with TMDB
     */
    suspend fun addOrRemoveFavorite(libraryItem: LibraryItem, isAuthenticated: Boolean = true)

    /**
     * Adds or removes a watchlist item. In guest mode (isAuthenticated=false), items are saved
     * locally only. In authenticated mode, items are saved locally and synced to TMDB.
     *
     * @param libraryItem The item to add or remove
     * @param isAuthenticated Whether the user is authenticated with TMDB
     */
    suspend fun addOrRemoveFromWatchlist(libraryItem: LibraryItem, isAuthenticated: Boolean = true)

    /**
     * Syncs all local-only items with TMDB after user authenticates.
     * - Pushes LOCAL_ONLY items to TMDB
     * - Pulls remote items not in local
     * - Resolves conflicts (remote wins)
     *
     * @return SyncResult with counts of pushed, pulled, and conflicting items
     */
    suspend fun syncLocalItemsWithTmdb(): SyncResult

    /** Gets the sync status of a favorite item. */
    suspend fun getFavoriteSyncStatus(mediaId: Int, mediaType: MediaType): SyncStatus?

    /** Gets the sync status of a watchlist item. */
    suspend fun getWatchlistSyncStatus(mediaId: Int, mediaType: MediaType): SyncStatus?
}
