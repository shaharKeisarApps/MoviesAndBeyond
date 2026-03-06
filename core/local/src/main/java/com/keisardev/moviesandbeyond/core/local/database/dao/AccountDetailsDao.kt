package com.keisardev.moviesandbeyond.core.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.keisardev.moviesandbeyond.core.local.database.entity.AccountDetailsEntity

/** Data Access Object for the locally cached TMDB account details (single-row table). */
@Dao
interface AccountDetailsDao {
    /** Returns the cached account details, or `null` if the user has never logged in. */
    @Query("SELECT * FROM account_details LIMIT 1")
    suspend fun getAccountDetails(): AccountDetailsEntity?

    /** Returns the ISO 3166-1 region code for content localization, or `null` if unavailable. */
    @Query("SELECT iso_3166_1 FROM account_details LIMIT 1") suspend fun getRegionCode(): String?

    /** Inserts or replaces the cached account details. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAccountDetails(accountDetails: AccountDetailsEntity)

    /** Deletes account details on logout. */
    @Query("DELETE FROM account_details WHERE id = :accountId")
    suspend fun deleteAccountDetails(accountId: Int)
}
