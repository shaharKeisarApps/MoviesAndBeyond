package com.keisardev.moviesandbeyond.core.local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.keisardev.moviesandbeyond.core.local.database.entity.WatchlistContentEntity
import com.keisardev.moviesandbeyond.core.model.library.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistContentDao {

    @Query(
        "SELECT * FROM watchlist_content " +
            "WHERE LOWER(media_type) = 'movie' AND sync_status != 'PENDING_DELETE' " +
            "ORDER BY id DESC")
    fun getMoviesWatchlist(): Flow<List<WatchlistContentEntity>>

    @Query(
        "SELECT * FROM watchlist_content " +
            "WHERE LOWER(media_type) = 'tv' AND sync_status != 'PENDING_DELETE' " +
            "ORDER BY id DESC")
    fun getTvShowsWatchlist(): Flow<List<WatchlistContentEntity>>

    @Query("SELECT * FROM watchlist_content WHERE sync_status != 'PENDING_DELETE' ORDER BY id DESC")
    fun getAllWatchlistContent(): Flow<List<WatchlistContentEntity>>

    @Query(
        "SELECT * FROM watchlist_content " +
            "WHERE media_id = :mediaId AND LOWER(media_type) = LOWER(:mediaType) " +
            "AND sync_status != 'PENDING_DELETE'")
    suspend fun getWatchlistItem(mediaId: Int, mediaType: String): WatchlistContentEntity?

    @Upsert suspend fun insertWatchlistItem(watchlistContentEntity: WatchlistContentEntity)

    @Query(
        "SELECT EXISTS(SELECT 1 FROM watchlist_content " +
            "WHERE media_id = :mediaId AND LOWER(media_type) = LOWER(:mediaType) " +
            "AND sync_status != 'PENDING_DELETE')")
    suspend fun checkWatchlistItemExists(mediaId: Int, mediaType: String): Boolean

    @Query(
        "DELETE FROM watchlist_content WHERE media_id = :mediaId AND LOWER(media_type) = LOWER(:mediaType)")
    suspend fun deleteWatchlistItem(mediaId: Int, mediaType: String)

    @Query("DELETE FROM watchlist_content") suspend fun deleteAllWatchlistItems()

    @Query("DELETE FROM watchlist_content WHERE sync_status IN ('SYNCED', 'PENDING_PUSH')")
    suspend fun deleteSyncedWatchlistItems()

    @Upsert suspend fun upsertWatchlistItems(items: List<WatchlistContentEntity>)

    @Transaction
    suspend fun syncWatchlistItems(
        upsertItems: List<WatchlistContentEntity>,
        deleteItems: List<Pair<Int, String>>
    ) {
        upsertWatchlistItems(upsertItems)
        deleteItems.forEach { deleteWatchlistItem(mediaId = it.first, mediaType = it.second) }
    }

    // Sync status queries for offline-first support

    @Query("SELECT * FROM watchlist_content WHERE sync_status = :status")
    suspend fun getItemsBySyncStatus(status: SyncStatus): List<WatchlistContentEntity>

    @Query("SELECT * FROM watchlist_content WHERE sync_status IN ('LOCAL_ONLY', 'PENDING_PUSH')")
    suspend fun getPendingSyncItems(): List<WatchlistContentEntity>

    @Query("SELECT * FROM watchlist_content WHERE sync_status = 'PENDING_DELETE'")
    suspend fun getPendingDeleteItems(): List<WatchlistContentEntity>

    @Query(
        "UPDATE watchlist_content SET sync_status = :status " +
            "WHERE media_id = :mediaId AND LOWER(media_type) = LOWER(:mediaType)")
    suspend fun updateSyncStatus(mediaId: Int, mediaType: String, status: SyncStatus)

    @Query("UPDATE watchlist_content SET sync_status = 'SYNCED' WHERE media_id IN (:mediaIds)")
    suspend fun markAsSynced(mediaIds: List<Int>)

    @Query(
        "UPDATE watchlist_content SET sync_status = 'PENDING_DELETE' " +
            "WHERE media_id = :mediaId AND LOWER(media_type) = LOWER(:mediaType)")
    suspend fun markForDeletion(mediaId: Int, mediaType: String)
}
