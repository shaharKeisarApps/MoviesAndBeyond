package com.keisardev.moviesandbeyond.data.repository.impl

import com.keisardev.moviesandbeyond.core.network.ktor.TmdbApi
import com.keisardev.moviesandbeyond.core.network.model.auth.Avatar
import com.keisardev.moviesandbeyond.core.network.model.auth.DeleteSessionRequest
import com.keisardev.moviesandbeyond.core.network.model.auth.Gravatar
import com.keisardev.moviesandbeyond.core.network.model.auth.LoginRequest
import com.keisardev.moviesandbeyond.core.network.model.auth.LoginResponse
import com.keisardev.moviesandbeyond.core.network.model.auth.NetworkAccountDetails
import com.keisardev.moviesandbeyond.core.network.model.auth.RequestTokenResponse
import com.keisardev.moviesandbeyond.core.network.model.auth.SessionRequest
import com.keisardev.moviesandbeyond.core.network.model.auth.SessionResponse
import com.keisardev.moviesandbeyond.core.network.model.auth.Tmdb
import com.keisardev.moviesandbeyond.core.network.model.content.NetworkContentResponse
import com.keisardev.moviesandbeyond.core.network.model.details.NetworkMovieDetails
import com.keisardev.moviesandbeyond.core.network.model.details.people.NetworkPersonDetails
import com.keisardev.moviesandbeyond.core.network.model.details.tv.NetworkTvDetails
import com.keisardev.moviesandbeyond.core.network.model.library.FavoriteRequest
import com.keisardev.moviesandbeyond.core.network.model.library.WatchlistRequest
import com.keisardev.moviesandbeyond.core.network.model.search.SearchResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond

/**
 * A fake [TmdbApi] for unit testing that overrides all open methods to return configurable
 * responses or throw configurable exceptions. Uses a dummy [HttpClient] since all methods are
 * overridden and never delegate to the parent.
 */
class FakeTmdbApi : TmdbApi(HttpClient(MockEngine { respond("unused") })) {

    var exception: Exception? = null

    // Auth
    var requestTokenResponse =
        RequestTokenResponse(expiresAt = "2099-01-01", requestToken = "test_token")
    var loginResponse = LoginResponse(requestToken = "validated_token")
    var sessionResponse = SessionResponse(success = true, sessionId = "test_session_id")
    var networkAccountDetails =
        NetworkAccountDetails(
            avatar = Avatar(gravatar = Gravatar(hash = "hash"), tmdb = Tmdb(avatarPath = null)),
            id = 1,
            includeAdult = false,
            iso6391 = "en",
            iso31661 = "US",
            name = "Test User",
            username = "testuser",
        )
    var deleteSessionCalled = false

    // Details
    var networkMovieDetails: NetworkMovieDetails? = null
    var networkTvDetails: NetworkTvDetails? = null
    var networkPersonDetails: NetworkPersonDetails? = null

    // Search
    var searchResponse = SearchResponse(page = 1, results = emptyList(), totalPages = 1)
    var lastIncludeAdult = false

    // Library
    var libraryItemsResponse =
        NetworkContentResponse(page = 1, results = emptyList(), totalPages = 1, totalResults = 0)
    var addOrRemoveFavoriteCalled = false
    var lastFavoriteRequest: FavoriteRequest? = null
    var addOrRemoveFromWatchlistCalled = false
    var lastWatchlistRequest: WatchlistRequest? = null

    private fun throwIfConfigured() {
        exception?.let { throw it }
    }

    override suspend fun createRequestToken(): RequestTokenResponse {
        throwIfConfigured()
        return requestTokenResponse
    }

    override suspend fun validateWithLogin(loginRequest: LoginRequest): LoginResponse {
        throwIfConfigured()
        return loginResponse
    }

    override suspend fun createSession(sessionRequest: SessionRequest): SessionResponse {
        throwIfConfigured()
        return sessionResponse
    }

    override suspend fun getAccountDetails(sessionId: String): NetworkAccountDetails {
        throwIfConfigured()
        return networkAccountDetails
    }

    override suspend fun getAccountDetailsWithId(accountId: Int): NetworkAccountDetails {
        throwIfConfigured()
        return networkAccountDetails
    }

    override suspend fun deleteSession(deleteSessionRequest: DeleteSessionRequest) {
        throwIfConfigured()
        deleteSessionCalled = true
    }

    override suspend fun getMovieDetails(id: Int, appendToResponse: String): NetworkMovieDetails {
        throwIfConfigured()
        return networkMovieDetails ?: error("networkMovieDetails not set")
    }

    override suspend fun getTvShowDetails(id: Int, appendToResponse: String): NetworkTvDetails {
        throwIfConfigured()
        return networkTvDetails ?: error("networkTvDetails not set")
    }

    override suspend fun getPersonDetails(id: Int): NetworkPersonDetails {
        throwIfConfigured()
        return networkPersonDetails ?: error("networkPersonDetails not set")
    }

    override suspend fun multiSearch(
        page: Int,
        query: String,
        includeAdult: Boolean,
    ): SearchResponse {
        throwIfConfigured()
        lastIncludeAdult = includeAdult
        return searchResponse
    }

    override suspend fun getLibraryItems(
        accountId: Int,
        itemType: String,
        mediaType: String,
        page: Int,
    ): NetworkContentResponse {
        throwIfConfigured()
        return libraryItemsResponse
    }

    override suspend fun addOrRemoveFavorite(accountId: Int, favoriteRequest: FavoriteRequest) {
        throwIfConfigured()
        addOrRemoveFavoriteCalled = true
        lastFavoriteRequest = favoriteRequest
    }

    override suspend fun addOrRemoveFromWatchlist(
        accountId: Int,
        watchlistRequest: WatchlistRequest,
    ) {
        throwIfConfigured()
        addOrRemoveFromWatchlistCalled = true
        lastWatchlistRequest = watchlistRequest
    }
}
