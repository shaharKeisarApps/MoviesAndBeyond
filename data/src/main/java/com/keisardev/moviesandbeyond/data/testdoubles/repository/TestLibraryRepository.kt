package com.keisardev.moviesandbeyond.data.testdoubles.repository

import com.keisardev.moviesandbeyond.core.model.MediaType
import com.keisardev.moviesandbeyond.core.model.library.LibraryItem
import com.keisardev.moviesandbeyond.core.model.library.LibraryItemType
import com.keisardev.moviesandbeyond.core.model.library.SyncStatus
import com.keisardev.moviesandbeyond.data.repository.LibraryRepository
import com.keisardev.moviesandbeyond.data.repository.SyncResult
import com.keisardev.moviesandbeyond.data.testdoubles.movieMediaType
import com.keisardev.moviesandbeyond.data.testdoubles.testLibraryItems
import com.keisardev.moviesandbeyond.data.testdoubles.tvMediaType
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TestLibraryRepository : LibraryRepository {
    private var generateError = false

    private val movieLibrary = testLibraryItems.filter { it.mediaType == movieMediaType }
    private val tvLibrary = testLibraryItems.filter { it.mediaType == tvMediaType }

    private val _movies = MutableStateFlow(movieLibrary)
    private val _tvShows = MutableStateFlow(tvLibrary)

    // Track sync status for test items
    private val syncStatusMap = mutableMapOf<Pair<Int, String>, SyncStatus>()

    // Track sync method calls for testing
    var syncFavoritesCallCount = 0
        private set

    var syncWatchlistCallCount = 0
        private set

    override val favoriteMovies: Flow<List<LibraryItem>> = _movies.asStateFlow()

    override val favoriteTvShows: Flow<List<LibraryItem>> = _tvShows.asStateFlow()

    override val moviesWatchlist: Flow<List<LibraryItem>> = _movies.asStateFlow()

    override val tvShowsWatchlist: Flow<List<LibraryItem>> = _tvShows.asStateFlow()

    override suspend fun itemInFavoritesExists(mediaId: Int, mediaType: MediaType): Boolean {
        return testLibraryItems.find { it.id == mediaId } != null
    }

    override suspend fun itemInWatchlistExists(mediaId: Int, mediaType: MediaType): Boolean {
        return testLibraryItems.find { it.id == mediaId } != null
    }

    override suspend fun addOrRemoveFavorite(libraryItem: LibraryItem, isAuthenticated: Boolean) {
        return if (generateError) {
            throw IOException()
        } else {
            // For testing add
            if (libraryItem.id == 0) return

            // Track sync status
            val key = Pair(libraryItem.id, libraryItem.mediaType)
            syncStatusMap[key] = if (isAuthenticated) SyncStatus.SYNCED else SyncStatus.LOCAL_ONLY

            // For testing delete
            // Since delete button is present on items list, list needs to be updated
            when (enumValueOf<MediaType>(libraryItem.mediaType.uppercase())) {
                MediaType.MOVIE -> _movies.update { it - libraryItem }

                MediaType.TV -> _tvShows.update { it - libraryItem }

                else -> {}
            }
        }
    }

    override suspend fun addOrRemoveFromWatchlist(
        libraryItem: LibraryItem,
        isAuthenticated: Boolean
    ) {
        return if (generateError) {
            throw IOException()
        } else {
            // For testing add
            if (libraryItem.id == 0) return

            // Track sync status
            val key = Pair(libraryItem.id, libraryItem.mediaType)
            syncStatusMap[key] = if (isAuthenticated) SyncStatus.SYNCED else SyncStatus.LOCAL_ONLY

            // For testing delete
            // Since delete button is present on items list, list needs to be updated
            when (enumValueOf<MediaType>(libraryItem.mediaType.uppercase())) {
                MediaType.MOVIE -> _movies.update { it - libraryItem }

                MediaType.TV -> _tvShows.update { it - libraryItem }

                else -> {}
            }
        }
    }

    override suspend fun syncLocalItemsWithTmdb(): SyncResult {
        // Mark all LOCAL_ONLY items as SYNCED
        syncStatusMap.entries
            .filter { it.value == SyncStatus.LOCAL_ONLY }
            .forEach { syncStatusMap[it.key] = SyncStatus.SYNCED }
        return SyncResult(pushed = 0, pulled = 0, conflicts = 0, errors = emptyList())
    }

    override suspend fun getFavoriteSyncStatus(mediaId: Int, mediaType: MediaType): SyncStatus? {
        return syncStatusMap[Pair(mediaId, mediaType.name.lowercase())]
    }

    override suspend fun getWatchlistSyncStatus(mediaId: Int, mediaType: MediaType): SyncStatus? {
        return syncStatusMap[Pair(mediaId, mediaType.name.lowercase())]
    }

    override suspend fun executeLibraryTask(
        id: Int,
        mediaType: MediaType,
        libraryItemType: LibraryItemType,
        itemExistsLocally: Boolean
    ): Boolean = true

    override suspend fun syncFavorites(): Boolean {
        syncFavoritesCallCount++
        return true
    }

    override suspend fun syncWatchlist(): Boolean {
        syncWatchlistCallCount++
        return true
    }

    fun generateError(value: Boolean) {
        generateError = value
    }

    fun resetSyncCallCounts() {
        syncFavoritesCallCount = 0
        syncWatchlistCallCount = 0
    }
}
