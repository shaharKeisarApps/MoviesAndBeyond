package com.keisardev.moviesandbeyond.data.repository.impl

import com.keisardev.moviesandbeyond.core.local.database.dao.AccountDetailsDao
import com.keisardev.moviesandbeyond.core.local.database.dao.CachedContentDao
import com.keisardev.moviesandbeyond.core.local.database.dao.FavoriteContentDao
import com.keisardev.moviesandbeyond.core.local.database.dao.WatchlistContentDao
import com.keisardev.moviesandbeyond.core.local.database.entity.AccountDetailsEntity
import com.keisardev.moviesandbeyond.core.local.database.entity.CachedContentEntity
import com.keisardev.moviesandbeyond.core.local.database.entity.FavoriteContentEntity
import com.keisardev.moviesandbeyond.core.local.database.entity.WatchlistContentEntity
import com.keisardev.moviesandbeyond.core.model.library.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

// region FakeAccountDetailsDao

class FakeAccountDetailsDao : AccountDetailsDao {
    var accountDetails: AccountDetailsEntity? = null

    override suspend fun getAccountDetails(): AccountDetailsEntity? = accountDetails

    override suspend fun getRegionCode(): String? = accountDetails?.iso31661

    override suspend fun addAccountDetails(accountDetails: AccountDetailsEntity) {
        this.accountDetails = accountDetails
    }

    override suspend fun deleteAccountDetails(accountId: Int) {
        if (accountDetails?.id == accountId) accountDetails = null
    }
}

// endregion

// region FakeFavoriteContentDao

class FakeFavoriteContentDao : FavoriteContentDao {
    private val items = MutableStateFlow<List<FavoriteContentEntity>>(emptyList())

    override fun getFavoriteMovies(): Flow<List<FavoriteContentEntity>> =
        items.map { list ->
            list.filter {
                it.mediaType.lowercase() == "movie" && it.syncStatus != SyncStatus.PENDING_DELETE
            }
        }

    override fun getFavoriteTvShows(): Flow<List<FavoriteContentEntity>> =
        items.map { list ->
            list.filter {
                it.mediaType.lowercase() == "tv" && it.syncStatus != SyncStatus.PENDING_DELETE
            }
        }

    override fun getAllFavoriteContent(): Flow<List<FavoriteContentEntity>> =
        items.map { list -> list.filter { it.syncStatus != SyncStatus.PENDING_DELETE } }

    override suspend fun getFavoriteItem(mediaId: Int, mediaType: String): FavoriteContentEntity? =
        items.value.find {
            it.mediaId == mediaId &&
                it.mediaType.equals(mediaType, ignoreCase = true) &&
                it.syncStatus != SyncStatus.PENDING_DELETE
        }

    override suspend fun insertFavoriteItem(favoriteContentEntity: FavoriteContentEntity) {
        items.update { current ->
            val filtered =
                current.filter {
                    !(it.mediaId == favoriteContentEntity.mediaId &&
                        it.mediaType.equals(favoriteContentEntity.mediaType, ignoreCase = true))
                }
            filtered + favoriteContentEntity
        }
    }

    override suspend fun checkFavoriteItemExists(mediaId: Int, mediaType: String): Boolean =
        items.value.any {
            it.mediaId == mediaId &&
                it.mediaType.equals(mediaType, ignoreCase = true) &&
                it.syncStatus != SyncStatus.PENDING_DELETE
        }

    override suspend fun deleteFavoriteItem(mediaId: Int, mediaType: String) {
        items.update { current ->
            current.filter {
                !(it.mediaId == mediaId && it.mediaType.equals(mediaType, ignoreCase = true))
            }
        }
    }

    override suspend fun deleteAllFavoriteItems() {
        items.value = emptyList()
    }

    override suspend fun deleteSyncedFavoriteItems() {
        items.update { current ->
            current.filter {
                it.syncStatus != SyncStatus.SYNCED && it.syncStatus != SyncStatus.PENDING_PUSH
            }
        }
    }

    override suspend fun upsertFavoriteItems(items: List<FavoriteContentEntity>) {
        items.forEach { insertFavoriteItem(it) }
    }

    override suspend fun syncFavoriteItems(
        upsertItems: List<FavoriteContentEntity>,
        deleteItems: List<Pair<Int, String>>,
    ) {
        upsertFavoriteItems(upsertItems)
        deleteItems.forEach { deleteFavoriteItem(mediaId = it.first, mediaType = it.second) }
    }

    override suspend fun getItemsBySyncStatus(status: SyncStatus): List<FavoriteContentEntity> =
        items.value.filter { it.syncStatus == status }

    override suspend fun getPendingSyncItems(): List<FavoriteContentEntity> =
        items.value.filter {
            it.syncStatus == SyncStatus.LOCAL_ONLY || it.syncStatus == SyncStatus.PENDING_PUSH
        }

    override suspend fun getPendingDeleteItems(): List<FavoriteContentEntity> =
        items.value.filter { it.syncStatus == SyncStatus.PENDING_DELETE }

    override suspend fun updateSyncStatus(mediaId: Int, mediaType: String, status: SyncStatus) {
        items.update { current ->
            current.map {
                if (it.mediaId == mediaId && it.mediaType.equals(mediaType, ignoreCase = true)) {
                    it.copy(syncStatus = status)
                } else {
                    it
                }
            }
        }
    }

    override suspend fun markAsSynced(mediaIds: List<Int>) {
        items.update { current ->
            current.map {
                if (it.mediaId in mediaIds) it.copy(syncStatus = SyncStatus.SYNCED) else it
            }
        }
    }

    override suspend fun markForDeletion(mediaId: Int, mediaType: String) {
        updateSyncStatus(mediaId, mediaType, SyncStatus.PENDING_DELETE)
    }
}

// endregion

// region FakeWatchlistContentDao

class FakeWatchlistContentDao : WatchlistContentDao {
    private val items = MutableStateFlow<List<WatchlistContentEntity>>(emptyList())

    override fun getMoviesWatchlist(): Flow<List<WatchlistContentEntity>> =
        items.map { list ->
            list.filter {
                it.mediaType.lowercase() == "movie" && it.syncStatus != SyncStatus.PENDING_DELETE
            }
        }

    override fun getTvShowsWatchlist(): Flow<List<WatchlistContentEntity>> =
        items.map { list ->
            list.filter {
                it.mediaType.lowercase() == "tv" && it.syncStatus != SyncStatus.PENDING_DELETE
            }
        }

    override fun getAllWatchlistContent(): Flow<List<WatchlistContentEntity>> =
        items.map { list -> list.filter { it.syncStatus != SyncStatus.PENDING_DELETE } }

    override suspend fun getWatchlistItem(
        mediaId: Int,
        mediaType: String,
    ): WatchlistContentEntity? =
        items.value.find {
            it.mediaId == mediaId &&
                it.mediaType.equals(mediaType, ignoreCase = true) &&
                it.syncStatus != SyncStatus.PENDING_DELETE
        }

    override suspend fun insertWatchlistItem(watchlistContentEntity: WatchlistContentEntity) {
        items.update { current ->
            val filtered =
                current.filter {
                    !(it.mediaId == watchlistContentEntity.mediaId &&
                        it.mediaType.equals(watchlistContentEntity.mediaType, ignoreCase = true))
                }
            filtered + watchlistContentEntity
        }
    }

    override suspend fun checkWatchlistItemExists(mediaId: Int, mediaType: String): Boolean =
        items.value.any {
            it.mediaId == mediaId &&
                it.mediaType.equals(mediaType, ignoreCase = true) &&
                it.syncStatus != SyncStatus.PENDING_DELETE
        }

    override suspend fun deleteWatchlistItem(mediaId: Int, mediaType: String) {
        items.update { current ->
            current.filter {
                !(it.mediaId == mediaId && it.mediaType.equals(mediaType, ignoreCase = true))
            }
        }
    }

    override suspend fun deleteAllWatchlistItems() {
        items.value = emptyList()
    }

    override suspend fun deleteSyncedWatchlistItems() {
        items.update { current ->
            current.filter {
                it.syncStatus != SyncStatus.SYNCED && it.syncStatus != SyncStatus.PENDING_PUSH
            }
        }
    }

    override suspend fun upsertWatchlistItems(items: List<WatchlistContentEntity>) {
        items.forEach { insertWatchlistItem(it) }
    }

    override suspend fun syncWatchlistItems(
        upsertItems: List<WatchlistContentEntity>,
        deleteItems: List<Pair<Int, String>>,
    ) {
        upsertWatchlistItems(upsertItems)
        deleteItems.forEach { deleteWatchlistItem(mediaId = it.first, mediaType = it.second) }
    }

    override suspend fun getItemsBySyncStatus(status: SyncStatus): List<WatchlistContentEntity> =
        items.value.filter { it.syncStatus == status }

    override suspend fun getPendingSyncItems(): List<WatchlistContentEntity> =
        items.value.filter {
            it.syncStatus == SyncStatus.LOCAL_ONLY || it.syncStatus == SyncStatus.PENDING_PUSH
        }

    override suspend fun getPendingDeleteItems(): List<WatchlistContentEntity> =
        items.value.filter { it.syncStatus == SyncStatus.PENDING_DELETE }

    override suspend fun updateSyncStatus(mediaId: Int, mediaType: String, status: SyncStatus) {
        items.update { current ->
            current.map {
                if (it.mediaId == mediaId && it.mediaType.equals(mediaType, ignoreCase = true)) {
                    it.copy(syncStatus = status)
                } else {
                    it
                }
            }
        }
    }

    override suspend fun markAsSynced(mediaIds: List<Int>) {
        items.update { current ->
            current.map {
                if (it.mediaId in mediaIds) it.copy(syncStatus = SyncStatus.SYNCED) else it
            }
        }
    }

    override suspend fun markForDeletion(mediaId: Int, mediaType: String) {
        updateSyncStatus(mediaId, mediaType, SyncStatus.PENDING_DELETE)
    }
}

// endregion

// region FakeCachedContentDao

class FakeCachedContentDao : CachedContentDao {
    private val items = MutableStateFlow<List<CachedContentEntity>>(emptyList())
    var deleteAllCalled = false

    override fun observeByCategory(category: String): Flow<List<CachedContentEntity>> =
        items.map { list -> list.filter { it.category == category } }

    override fun observeByCategoryAndPage(
        category: String,
        page: Int,
    ): Flow<List<CachedContentEntity>> =
        items.map { list -> list.filter { it.category == category && it.page == page } }

    override suspend fun getLatestFetchTime(category: String): Long? =
        items.value.filter { it.category == category }.maxOfOrNull { it.fetchedAt }

    override suspend fun insertAll(items: List<CachedContentEntity>) {
        this.items.update { current -> current + items }
    }

    override suspend fun deleteByCategory(category: String) {
        items.update { current -> current.filter { it.category != category } }
    }

    override suspend fun deleteByCategoryAndPage(category: String, page: Int) {
        items.update { current -> current.filter { !(it.category == category && it.page == page) } }
    }

    override suspend fun deleteAll() {
        deleteAllCalled = true
        items.value = emptyList()
    }

    override suspend fun deleteOlderThan(timestamp: Long) {
        items.update { current -> current.filter { it.fetchedAt >= timestamp } }
    }
}

// endregion
