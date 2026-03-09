package com.keisardev.moviesandbeyond.core.network.ktor

import com.keisardev.moviesandbeyond.core.network.model.auth.DeleteSessionRequest
import com.keisardev.moviesandbeyond.core.network.model.auth.LoginRequest
import com.keisardev.moviesandbeyond.core.network.model.auth.LoginResponse
import com.keisardev.moviesandbeyond.core.network.model.auth.NetworkAccountDetails
import com.keisardev.moviesandbeyond.core.network.model.auth.RequestTokenResponse
import com.keisardev.moviesandbeyond.core.network.model.auth.SessionRequest
import com.keisardev.moviesandbeyond.core.network.model.auth.SessionResponse
import com.keisardev.moviesandbeyond.core.network.model.content.NetworkContentResponse
import com.keisardev.moviesandbeyond.core.network.model.details.NetworkMovieDetails
import com.keisardev.moviesandbeyond.core.network.model.details.people.NetworkPersonDetails
import com.keisardev.moviesandbeyond.core.network.model.details.tv.NetworkTvDetails
import com.keisardev.moviesandbeyond.core.network.model.library.FavoriteRequest
import com.keisardev.moviesandbeyond.core.network.model.library.WatchlistRequest
import com.keisardev.moviesandbeyond.core.network.model.search.SearchResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

open class TmdbApi(private val client: HttpClient) {
    open suspend fun getMovieLists(
        category: String,
        language: String = "en-US",
        page: Int,
        region: String? = null,
    ): NetworkContentResponse =
        client
            .get("movie/$category") {
                parameter("language", language)
                parameter("page", page)
                region?.let { parameter("region", it) }
            }
            .body()

    open suspend fun getTvShowLists(
        category: String,
        language: String = "en-US",
        page: Int,
    ): NetworkContentResponse =
        client
            .get("tv/$category") {
                parameter("language", language)
                parameter("page", page)
            }
            .body()

    open suspend fun multiSearch(
        page: Int = 1,
        query: String,
        includeAdult: Boolean,
    ): SearchResponse =
        client
            .get("search/multi") {
                parameter("page", page)
                parameter("query", query)
                parameter("include_adult", includeAdult)
            }
            .body()

    open suspend fun getMovieDetails(
        id: Int,
        appendToResponse: String = "recommendations,credits",
    ): NetworkMovieDetails =
        client.get("movie/$id") { parameter("append_to_response", appendToResponse) }.body()

    open suspend fun getTvShowDetails(
        id: Int,
        appendToResponse: String = "recommendations,credits",
    ): NetworkTvDetails =
        client.get("tv/$id") { parameter("append_to_response", appendToResponse) }.body()

    open suspend fun getPersonDetails(id: Int): NetworkPersonDetails =
        client.get("person/$id").body()

    open suspend fun getLibraryItems(
        accountId: Int,
        itemType: String,
        mediaType: String,
        page: Int,
    ): NetworkContentResponse =
        client.get("account/$accountId/$itemType/$mediaType") { parameter("page", page) }.body()

    open suspend fun addOrRemoveFavorite(accountId: Int, favoriteRequest: FavoriteRequest) {
        client.post("account/$accountId/favorite") {
            contentType(ContentType.Application.Json)
            setBody(favoriteRequest)
        }
    }

    open suspend fun addOrRemoveFromWatchlist(accountId: Int, watchlistRequest: WatchlistRequest) {
        client.post("account/$accountId/watchlist") {
            contentType(ContentType.Application.Json)
            setBody(watchlistRequest)
        }
    }

    open suspend fun createRequestToken(): RequestTokenResponse =
        client.get("authentication/token/new").body()

    open suspend fun validateWithLogin(loginRequest: LoginRequest): LoginResponse =
        client
            .post("authentication/token/validate_with_login") {
                contentType(ContentType.Application.Json)
                setBody(loginRequest)
            }
            .body()

    open suspend fun createSession(sessionRequest: SessionRequest): SessionResponse =
        client
            .post("authentication/session/new") {
                contentType(ContentType.Application.Json)
                setBody(sessionRequest)
            }
            .body()

    open suspend fun getAccountDetails(sessionId: String): NetworkAccountDetails =
        client.get("account") { parameter("session_id", sessionId) }.body()

    open suspend fun getAccountDetailsWithId(accountId: Int): NetworkAccountDetails =
        client.get("account/$accountId").body()

    open suspend fun deleteSession(deleteSessionRequest: DeleteSessionRequest) {
        client.delete("authentication/session") {
            contentType(ContentType.Application.Json)
            setBody(deleteSessionRequest)
        }
    }
}
