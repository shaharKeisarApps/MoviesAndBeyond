package com.keisardev.moviesandbeyond.data.repository

import com.keisardev.moviesandbeyond.core.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository for TMDB account authentication.
 *
 * Manages user login/logout flows and exposes the current authentication state as a reactive
 * [Flow].
 */
interface AuthRepository {
    /** Emits `true` when the user has an active TMDB session, `false` otherwise. */
    val isLoggedIn: Flow<Boolean>

    /**
     * Authenticates the user with TMDB credentials.
     *
     * @param username TMDB account username
     * @param password TMDB account password
     * @return [Result.Success] on successful login, [Result.Error] on failure
     */
    suspend fun login(username: String, password: String): Result<Unit>

    /**
     * Logs the user out and invalidates the current TMDB session.
     *
     * @param accountId The TMDB account ID to log out
     * @return [Result.Success] on successful logout, [Result.Error] on failure
     */
    suspend fun logout(accountId: Int): Result<Unit>
}
