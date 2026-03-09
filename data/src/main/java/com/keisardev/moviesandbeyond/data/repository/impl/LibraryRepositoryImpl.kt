package com.keisardev.moviesandbeyond.data.repository.impl

import android.util.Log
import com.keisardev.moviesandbeyond.core.local.database.dao.AccountDetailsDao
import com.keisardev.moviesandbeyond.core.local.database.dao.FavoriteContentDao
import com.keisardev.moviesandbeyond.core.local.database.dao.WatchlistContentDao
import com.keisardev.moviesandbeyond.core.local.database.entity.FavoriteContentEntity
import com.keisardev.moviesandbeyond.core.local.database.entity.WatchlistContentEntity
import com.keisardev.moviesandbeyond.core.local.database.entity.asFavoriteContentEntity
import com.keisardev.moviesandbeyond.core.local.database.entity.asWatchlistContentEntity
import com.keisardev.moviesandbeyond.core.model.MediaType
import com.keisardev.moviesandbeyond.core.model.error.NetworkError
import com.keisardev.moviesandbeyond.core.model.library.LibraryItem
import com.keisardev.moviesandbeyond.core.model.library.LibraryItemType
import com.keisardev.moviesandbeyond.core.model.library.LibraryTask
import com.keisardev.moviesandbeyond.core.model.library.SyncStatus
import com.keisardev.moviesandbeyond.core.network.ktor.TmdbApi
import com.keisardev.moviesandbeyond.core.network.model.content.NetworkContentItem
import com.keisardev.moviesandbeyond.core.network.model.library.FavoriteRequest
import com.keisardev.moviesandbeyond.core.network.model.library.WatchlistRequest
import com.keisardev.moviesandbeyond.data.repository.LibraryRepository
import com.keisardev.moviesandbeyond.data.repository.SyncResult
import com.keisardev.moviesandbeyond.data.util.SyncScheduler
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

internal class LibraryRepositoryImpl
@Inject
constructor(
    private val tmdbApi: TmdbApi,
    private val favoriteContentDao: FavoriteContentDao,
    private val watchlistContentDao: WatchlistContentDao,
    private val accountDetailsDao: AccountDetailsDao,
    private val syncScheduler: SyncScheduler,
) : LibraryRepository {
    override val favoriteMovies: Flow<List<LibraryItem>> =
        favoriteContentDao.getFavoriteMovies().map { entities ->
            entities.map(FavoriteContentEntity::asLibraryItem)
        }

    override val favoriteTvShows: Flow<List<LibraryItem>> =
        favoriteContentDao.getFavoriteTvShows().map { entities ->
            entities.map(FavoriteContentEntity::asLibraryItem)
        }

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
            mediaId = mediaId,
            mediaType = mediaType.name.lowercase(),
        )
    }

    override suspend fun itemInWatchlistExists(mediaId: Int, mediaType: MediaType): Boolean {
        return watchlistContentDao.checkWatchlistItemExists(
            mediaId = mediaId,
            mediaType = mediaType.name.lowercase(),
        )
    }

    override suspend fun addOrRemoveFavorite(libraryItem: LibraryItem, isAuthenticated: Boolean) {
        addOrRemoveCollectionItem(
            libraryItem = libraryItem,
            isAuthenticated = isAuthenticated,
            checkExists = { id, type ->
                favoriteContentDao.checkFavoriteItemExists(mediaId = id, mediaType = type)
            },
            markForDeletion = { id, type -> favoriteContentDao.markForDeletion(id, type) },
            deleteItem = { id, type -> favoriteContentDao.deleteFavoriteItem(id, type) },
            insertItem = { item, status ->
                favoriteContentDao.insertFavoriteItem(item.asFavoriteContentEntity(status))
            },
            createTask = { id, type, exists -> LibraryTask.favoriteItemTask(id, type, exists) },
        )
    }

    override suspend fun addOrRemoveFromWatchlist(
        libraryItem: LibraryItem,
        isAuthenticated: Boolean,
    ) {
        addOrRemoveCollectionItem(
            libraryItem = libraryItem,
            isAuthenticated = isAuthenticated,
            checkExists = { id, type ->
                watchlistContentDao.checkWatchlistItemExists(mediaId = id, mediaType = type)
            },
            markForDeletion = { id, type -> watchlistContentDao.markForDeletion(id, type) },
            deleteItem = { id, type -> watchlistContentDao.deleteWatchlistItem(id, type) },
            insertItem = { item, status ->
                watchlistContentDao.insertWatchlistItem(item.asWatchlistContentEntity(status))
            },
            createTask = { id, type, exists -> LibraryTask.watchlistItemTask(id, type, exists) },
        )
    }

    @Suppress("LongMethod") // Sync logic is cohesive and reads better as a single method
    override suspend fun syncLocalItemsWithTmdb(): SyncResult {
        val accountId =
            accountDetailsDao.getAccountDetails()?.id
                ?: return SyncResult(
                    pushed = 0,
                    pulled = 0,
                    conflicts = 0,
                    errors = listOf("Not authenticated"),
                )

        var pushed = 0
        val errors = mutableListOf<String>()

        // Push pending favorites to TMDB
        for (item in favoriteContentDao.getPendingSyncItems()) {
            try {
                tmdbApi.addOrRemoveFavorite(
                    accountId,
                    FavoriteRequest(
                        mediaType = item.mediaType,
                        mediaId = item.mediaId,
                        favorite = true,
                    ),
                )
                favoriteContentDao.updateSyncStatus(item.mediaId, item.mediaType, SyncStatus.SYNCED)
                pushed++
            } catch (e: Exception) {
                Log.w(TAG, "Failed to push favorite ${item.mediaId} to TMDB", e)
                errors.add("Failed to sync favorite: ${item.name}")
            }
        }

        // Push pending watchlist to TMDB
        for (item in watchlistContentDao.getPendingSyncItems()) {
            try {
                tmdbApi.addOrRemoveFromWatchlist(
                    accountId,
                    WatchlistRequest(
                        mediaType = item.mediaType,
                        mediaId = item.mediaId,
                        watchlist = true,
                    ),
                )
                watchlistContentDao.updateSyncStatus(
                    item.mediaId,
                    item.mediaType,
                    SyncStatus.SYNCED,
                )
                pushed++
            } catch (e: Exception) {
                Log.w(TAG, "Failed to push watchlist ${item.mediaId} to TMDB", e)
                errors.add("Failed to sync watchlist: ${item.name}")
            }
        }

        // Handle pending deletes for favorites
        for (item in favoriteContentDao.getPendingDeleteItems()) {
            try {
                tmdbApi.addOrRemoveFavorite(
                    accountId,
                    FavoriteRequest(
                        mediaType = item.mediaType,
                        mediaId = item.mediaId,
                        favorite = false,
                    ),
                )
                favoriteContentDao.deleteFavoriteItem(item.mediaId, item.mediaType)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to delete favorite ${item.mediaId} from TMDB", e)
                // Keep marked for deletion, will retry later
            }
        }

        // Handle pending deletes for watchlist
        for (item in watchlistContentDao.getPendingDeleteItems()) {
            try {
                tmdbApi.addOrRemoveFromWatchlist(
                    accountId,
                    WatchlistRequest(
                        mediaType = item.mediaType,
                        mediaId = item.mediaId,
                        watchlist = false,
                    ),
                )
                watchlistContentDao.deleteWatchlistItem(item.mediaId, item.mediaType)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to delete watchlist ${item.mediaId} from TMDB", e)
                // Keep marked for deletion, will retry later
            }
        }

        // Run full sync to pull any remote items
        syncFavorites()
        syncWatchlist()

        return SyncResult(pushed = pushed, pulled = 0, conflicts = 0, errors = errors)
    }

    override suspend fun getFavoriteSyncStatus(mediaId: Int, mediaType: MediaType): SyncStatus? {
        return favoriteContentDao.getFavoriteItem(mediaId, mediaType.name.lowercase())?.syncStatus
    }

    override suspend fun getWatchlistSyncStatus(mediaId: Int, mediaType: MediaType): SyncStatus? {
        return watchlistContentDao.getWatchlistItem(mediaId, mediaType.name.lowercase())?.syncStatus
    }

    companion object {
        private const val TAG = "LibraryRepository"
    }

    override suspend fun executeLibraryTask(
        id: Int,
        mediaType: MediaType,
        libraryItemType: LibraryItemType,
        itemExistsLocally: Boolean,
    ): Boolean {
        val accountId = accountDetailsDao.getAccountDetails()?.id ?: return false
        return try {
            when (libraryItemType) {
                LibraryItemType.FAVORITE -> {
                    val favoriteRequest =
                        FavoriteRequest(
                            mediaType = mediaType.name.lowercase(),
                            mediaId = id,
                            favorite = itemExistsLocally,
                        )
                    tmdbApi.addOrRemoveFavorite(accountId, favoriteRequest)
                    // Update sync status after successful API call
                    if (itemExistsLocally) {
                        favoriteContentDao.updateSyncStatus(
                            id,
                            mediaType.name.lowercase(),
                            SyncStatus.SYNCED,
                        )
                    }
                }

                LibraryItemType.WATCHLIST -> {
                    val watchlistRequest =
                        WatchlistRequest(
                            mediaType = mediaType.name.lowercase(),
                            mediaId = id,
                            watchlist = itemExistsLocally,
                        )
                    tmdbApi.addOrRemoveFromWatchlist(accountId, watchlistRequest)
                    // Update sync status after successful API call
                    if (itemExistsLocally) {
                        watchlistContentDao.updateSyncStatus(
                            id,
                            mediaType.name.lowercase(),
                            SyncStatus.SYNCED,
                        )
                    }
                }
            }
            true
        } catch (e: IOException) {
            false
        } catch (e: NetworkError) {
            false
        }
    }

    /**
     * This function syncs favorites from server by inserting items into database and removes items
     * which are stale (i.e. not present on server) and for which no work is scheduled.
     */
    override suspend fun syncFavorites(): Boolean {
        val accountId = accountDetailsDao.getAccountDetails()?.id ?: return false
        val itemTypeString = LibraryItemType.FAVORITE.name.lowercase()
        return syncCollection(
            accountId = accountId,
            itemTypeString = itemTypeString,
            itemType = LibraryItemType.FAVORITE,
            fetchLocalByMediaType = { mediaType ->
                when (mediaType) {
                    MediaType.MOVIE ->
                        favoriteContentDao.getFavoriteMovies().first().map { entity ->
                            LocalItem(entity.mediaId, entity.mediaType, entity.syncStatus)
                        }
                    MediaType.TV ->
                        favoriteContentDao.getFavoriteTvShows().first().map { entity ->
                            LocalItem(entity.mediaId, entity.mediaType, entity.syncStatus)
                        }
                    else -> emptyList()
                }
            },
            fetchLocalItem = { mediaId, mediaTypeString ->
                favoriteContentDao.getFavoriteItem(mediaId, mediaTypeString)?.asLibraryItem()
            },
            updateLocalSource = { libraryItems, staleItems ->
                favoriteContentDao.syncFavoriteItems(
                    upsertItems =
                        libraryItems.map { it.asFavoriteContentEntity(SyncStatus.SYNCED) },
                    deleteItems = staleItems,
                )
            },
        )
    }

    /**
     * This function syncs watchlist from server by inserting items into database and removes items
     * which are stale (i.e. not present on server) and for which no work is scheduled.
     */
    override suspend fun syncWatchlist(): Boolean {
        val accountId = accountDetailsDao.getAccountDetails()?.id ?: return false
        val itemTypeString = LibraryItemType.WATCHLIST.name.lowercase()
        return syncCollection(
            accountId = accountId,
            itemTypeString = itemTypeString,
            itemType = LibraryItemType.WATCHLIST,
            fetchLocalByMediaType = { mediaType ->
                when (mediaType) {
                    MediaType.MOVIE ->
                        watchlistContentDao.getMoviesWatchlist().first().map { entity ->
                            LocalItem(entity.mediaId, entity.mediaType, entity.syncStatus)
                        }
                    MediaType.TV ->
                        watchlistContentDao.getTvShowsWatchlist().first().map { entity ->
                            LocalItem(entity.mediaId, entity.mediaType, entity.syncStatus)
                        }
                    else -> emptyList()
                }
            },
            fetchLocalItem = { mediaId, mediaTypeString ->
                watchlistContentDao.getWatchlistItem(mediaId, mediaTypeString)?.asLibraryItem()
            },
            updateLocalSource = { libraryItems, staleItems ->
                watchlistContentDao.syncWatchlistItems(
                    upsertItems =
                        libraryItems.map { it.asWatchlistContentEntity(SyncStatus.SYNCED) },
                    deleteItems = staleItems,
                )
            },
        )
    }

    // region Private helpers

    /**
     * Shared logic for adding or removing an item from a collection (favorites or watchlist). The
     * caller supplies DAO-specific lambdas; this function handles the branching on existence and
     * authentication state.
     */
    private suspend fun addOrRemoveCollectionItem(
        libraryItem: LibraryItem,
        isAuthenticated: Boolean,
        checkExists: suspend (mediaId: Int, mediaType: String) -> Boolean,
        markForDeletion: suspend (mediaId: Int, mediaType: String) -> Unit,
        deleteItem: suspend (mediaId: Int, mediaType: String) -> Unit,
        insertItem: suspend (item: LibraryItem, status: SyncStatus) -> Unit,
        createTask: (mediaId: Int, mediaType: MediaType, itemExists: Boolean) -> LibraryTask,
    ) {
        val itemExists = checkExists(libraryItem.id, libraryItem.mediaType)
        if (itemExists) {
            if (isAuthenticated) {
                // Mark for deletion and schedule sync
                markForDeletion(libraryItem.id, libraryItem.mediaType)
                syncScheduler.scheduleLibraryTaskWork(
                    createTask(
                        libraryItem.id,
                        enumValueOf<MediaType>(libraryItem.mediaType.uppercase()),
                        false,
                    )
                )
            } else {
                // Guest mode: delete locally immediately
                deleteItem(libraryItem.id, libraryItem.mediaType)
            }
        } else {
            val syncStatus = if (isAuthenticated) SyncStatus.PENDING_PUSH else SyncStatus.LOCAL_ONLY
            insertItem(libraryItem, syncStatus)
            if (isAuthenticated) {
                syncScheduler.scheduleLibraryTaskWork(
                    createTask(
                        libraryItem.id,
                        enumValueOf<MediaType>(libraryItem.mediaType.uppercase()),
                        true,
                    )
                )
            }
        }
    }

    /**
     * Shared sync logic for a collection (favorites or watchlist). Delegates to
     * [syncFromLocalAndNetwork] with DAO-specific lambdas supplied by the caller.
     *
     * [fetchLocalByMediaType] returns a list of [LocalItem] — a lightweight, entity-agnostic
     * projection of the fields needed for staleness detection.
     */
    private suspend fun syncCollection(
        accountId: Int,
        itemTypeString: String,
        itemType: LibraryItemType,
        fetchLocalByMediaType: suspend (MediaType) -> List<LocalItem>,
        fetchLocalItem: suspend (mediaId: Int, mediaTypeString: String) -> LibraryItem?,
        updateLocalSource: suspend (List<LibraryItem>, List<Pair<Int, String>>) -> Unit,
    ): Boolean {
        return syncFromLocalAndNetwork(
            fetchFromNetwork = { mediaTypeString ->
                val networkResults = mutableListOf<NetworkContentItem>()
                var page = 1
                do {
                    val result =
                        tmdbApi
                            .getLibraryItems(
                                accountId = accountId,
                                itemType = itemTypeString,
                                mediaType = mediaTypeString,
                                page = page++,
                            )
                            .results
                    networkResults.addAll(result)
                } while (result.isNotEmpty())
                networkResults
            },
            fetchStaleItemsFromLocalSource = { mediaType, networkResultsPair ->
                fetchLocalByMediaType(mediaType)
                    .filter {
                        // Only consider SYNCED items as stale (not LOCAL_ONLY or PENDING_PUSH)
                        it.syncStatus == SyncStatus.SYNCED &&
                            Pair(it.mediaId, it.mediaType) !in networkResultsPair &&
                            syncScheduler.isWorkNotScheduled(
                                mediaId = it.mediaId,
                                mediaType = mediaType,
                                itemType = itemType,
                            )
                    }
                    .map { Pair(it.mediaId, it.mediaType) }
            },
            fetchFromLocalSource = { mediaType, mediaTypeString, networkResults ->
                networkResults
                    .filter {
                        syncScheduler.isWorkNotScheduled(
                            mediaId = it.id,
                            mediaType = mediaType,
                            itemType = itemType,
                        )
                    }
                    .map {
                        val contentItem = it.asModel()
                        val existing = fetchLocalItem(contentItem.id, mediaTypeString)
                        existing?.copy(imagePath = contentItem.imagePath, name = contentItem.name)
                            ?: LibraryItem(
                                id = contentItem.id,
                                mediaType = mediaTypeString,
                                imagePath = contentItem.imagePath,
                                name = contentItem.name,
                            )
                    }
            },
            updateLocalSource = updateLocalSource,
        )
    }

    /**
     * Entity-agnostic projection used by [syncCollection] to detect stale items without coupling to
     * a specific entity type.
     */
    private data class LocalItem(
        val mediaId: Int,
        val mediaType: String,
        val syncStatus: SyncStatus,
    )

    // endregion
}
