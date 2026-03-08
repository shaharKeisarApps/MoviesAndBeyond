package com.keisardev.moviesandbeyond.data.repository.impl

import com.keisardev.moviesandbeyond.core.local.database.dao.AccountDetailsDao
import com.keisardev.moviesandbeyond.core.local.database.dao.FavoriteContentDao
import com.keisardev.moviesandbeyond.core.local.database.dao.WatchlistContentDao
import com.keisardev.moviesandbeyond.core.local.datastore.UserPreferencesDataStore
import com.keisardev.moviesandbeyond.core.local.session.SessionManager
import com.keisardev.moviesandbeyond.core.model.Result
import com.keisardev.moviesandbeyond.core.network.error.NetworkError
import com.keisardev.moviesandbeyond.core.network.ktor.TmdbApi
import com.keisardev.moviesandbeyond.core.network.model.auth.DeleteSessionRequest
import com.keisardev.moviesandbeyond.core.network.model.auth.LoginRequest
import com.keisardev.moviesandbeyond.core.network.model.auth.SessionRequest
import com.keisardev.moviesandbeyond.data.model.asEntity
import com.keisardev.moviesandbeyond.data.repository.AuthRepository
import com.keisardev.moviesandbeyond.data.util.SyncScheduler
import java.io.IOException
import javax.inject.Inject

internal class AuthRepositoryImpl
@Inject
constructor(
    private val tmdbApi: TmdbApi,
    private val favoriteContentDao: FavoriteContentDao,
    private val watchlistContentDao: WatchlistContentDao,
    private val accountDetailsDao: AccountDetailsDao,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val sessionManager: SessionManager,
    private val syncScheduler: SyncScheduler,
) : AuthRepository {
    override val isLoggedIn = sessionManager.isLoggedIn

    override suspend fun login(username: String, password: String): Result<Unit> {
        return try {
            val response = tmdbApi.createRequestToken()
            val loginRequest =
                LoginRequest(
                    username = username,
                    password = password,
                    requestToken = response.requestToken,
                )
            val loginResponse = tmdbApi.validateWithLogin(loginRequest)

            val sessionRequest = SessionRequest(loginResponse.requestToken)
            val sessionResponse = tmdbApi.createSession(sessionRequest)

            val accountDetails = tmdbApi.getAccountDetails(sessionResponse.sessionId).asEntity()

            sessionManager.storeSessionId(sessionResponse.sessionId)
            accountDetailsDao.addAccountDetails(accountDetails)
            userPreferencesDataStore.setAdultResultPreference(accountDetails.includeAdult)

            syncScheduler.scheduleLibrarySyncWork()

            Result.Success(Unit)
        } catch (e: IOException) {
            Result.Error(e)
        } catch (e: NetworkError) {
            Result.Error(e, e.message)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun logout(accountId: Int): Result<Unit> {
        return try {
            val sessionId = sessionManager.getSessionId()!!
            val deleteSessionRequest = DeleteSessionRequest(sessionId)

            tmdbApi.deleteSession(deleteSessionRequest)
            sessionManager.deleteSessionId()
            accountDetailsDao.deleteAccountDetails(accountId)

            // Only delete TMDB-synced items, preserve LOCAL_ONLY items for guest mode
            favoriteContentDao.deleteSyncedFavoriteItems()
            watchlistContentDao.deleteSyncedWatchlistItems()

            Result.Success(Unit)
        } catch (e: IOException) {
            Result.Error(e)
        } catch (e: NetworkError) {
            Result.Error(e, e.message)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
