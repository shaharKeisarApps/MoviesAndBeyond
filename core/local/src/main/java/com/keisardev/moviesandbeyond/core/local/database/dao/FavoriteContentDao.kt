package com.keisardev.moviesandbeyond.core.local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.keisardev.moviesandbeyond.core.local.database.entity.FavoriteContentEntity
import com.keisardev.moviesandbeyond.core.model.library.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteContentDao {

    @Query("SELECT * FROM favorite_content WHERE media_type = 'movie' ORDER BY id DESC")
    fun getFavoriteMovies(): Flow<List<FavoriteContentEntity>>

    @Query("SELECT * FROM favorite_content WHERE media_type = 'tv' ORDER BY id DESC")
    fun getFavoriteTvShows(): Flow<List<FavoriteContentEntity>>

    @Query("SELECT * FROM favorite_content WHERE media_id = :mediaId AND media_type = :mediaType")
    suspend fun getFavoriteItem(mediaId: Int, mediaType: String): FavoriteContentEntity?

    @Upsert
    suspend fun insertFavoriteItem(favoriteContentEntity: FavoriteContentEntity)

    @Query(
        "SELECT EXISTS(SELECT 1 FROM favorite_content WHERE media_id = :mediaId AND media_type = :mediaType)")
    suspend fun checkFavoriteItemExists(mediaId: Int, mediaType: String): Boolean

    @Query("DELETE FROM favorite_content WHERE media_id = :mediaId AND media_type = :mediaType ")
    suspend fun deleteFavoriteItem(mediaId: Int, mediaType: String)

    @Query("DELETE FROM favorite_content") suspend fun deleteAllFavoriteItems()

    @Upsert suspend fun upsertFavoriteItems(items: List<FavoriteContentEntity>)

    @Transaction
    suspend fun syncFavoriteItems(
        upsertItems: List<FavoriteContentEntity>,
        deleteItems: List<Pair<Int, String>>
    ) {
        upsertFavoriteItems(upsertItems)
        deleteItems.forEach { deleteFavoriteItem(mediaId = it.first, mediaType = it.second) }
    }

    // Sync status queries for offline-first support

    @Query("SELECT * FROM favorite_content WHERE sync_status = :status")
    suspend fun getItemsBySyncStatus(status: SyncStatus): List<FavoriteContentEntity>

    @Query("SELECT * FROM favorite_content WHERE sync_status IN ('LOCAL_ONLY', 'PENDING_PUSH')")
    suspend fun getPendingSyncItems(): List<FavoriteContentEntity>

    @Query("SELECT * FROM favorite_content WHERE sync_status = 'PENDING_DELETE'")
    suspend fun getPendingDeleteItems(): List<FavoriteContentEntity>

    @Query(
        "UPDATE favorite_content SET sync_status = :status WHERE media_id = :mediaId AND media_type = :mediaType")
    suspend fun updateSyncStatus(mediaId: Int, mediaType: String, status: SyncStatus)

    @Query("UPDATE favorite_content SET sync_status = 'SYNCED' WHERE media_id IN (:mediaIds)")
    suspend fun markAsSynced(mediaIds: List<Int>)

    @Query(
        "UPDATE favorite_content SET sync_status = 'PENDING_DELETE' " +
            "WHERE media_id = :mediaId AND media_type = :mediaType")
    suspend fun markForDeletion(mediaId: Int, mediaType: String)
}
