package com.keisardev.moviesandbeyond.data.store

import com.keisardev.moviesandbeyond.core.model.NetworkResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreReadResponseOrigin

/**
 * Converts a Store5 [StoreReadResponse] stream to a [NetworkResponse] stream. Filters out Loading
 * and NoNewData states, converting only Data and Error states.
 */
fun <T> Flow<StoreReadResponse<T>>.toNetworkResponseFlow(): Flow<NetworkResponse<T>> =
    this.filterNot { it is StoreReadResponse.Loading || it is StoreReadResponse.NoNewData }
        .map { response -> response.toNetworkResponse() }
        .catch { e -> emit(NetworkResponse.Error(e.message ?: "Unknown error")) }

/** Converts a single [StoreReadResponse] to a [NetworkResponse]. */
fun <T> StoreReadResponse<T>.toNetworkResponse(): NetworkResponse<T> =
    when (this) {
        is StoreReadResponse.Data -> NetworkResponse.Success(value)
        is StoreReadResponse.Error.Exception -> NetworkResponse.Error(error.message)
        is StoreReadResponse.Error.Message -> NetworkResponse.Error(message)
        is StoreReadResponse.Error.Custom<*> -> NetworkResponse.Error("Custom error")
        is StoreReadResponse.Loading -> NetworkResponse.Error("Loading")
        is StoreReadResponse.NoNewData -> NetworkResponse.Error("No new data")
        is StoreReadResponse.Initial -> NetworkResponse.Error("Initial state")
    }

/** Returns true if this response contains data (either from cache or network). */
val <T> StoreReadResponse<T>.hasData: Boolean
    get() = this is StoreReadResponse.Data

/** Returns the data if available, null otherwise. */
fun <T> StoreReadResponse<T>.dataOrNull(): T? = (this as? StoreReadResponse.Data)?.value

/** Returns true if this response is from cache. */
val <T> StoreReadResponse<T>.isFromCache: Boolean
    get() =
        this is StoreReadResponse.Data &&
            (origin is StoreReadResponseOrigin.Cache ||
                origin is StoreReadResponseOrigin.SourceOfTruth)

/** Returns true if this response is from network. */
val <T> StoreReadResponse<T>.isFromNetwork: Boolean
    get() = this is StoreReadResponse.Data && origin is StoreReadResponseOrigin.Fetcher

/** Returns true if this response is a loading state. */
val <T> StoreReadResponse<T>.isLoading: Boolean
    get() = this is StoreReadResponse.Loading

/** Returns true if this response is an error state. */
val <T> StoreReadResponse<T>.isError: Boolean
    get() = this is StoreReadResponse.Error

/** Returns the error message if this is an error response, null otherwise. */
fun <T> StoreReadResponse<T>.errorMessageOrNull(): String? =
    when (this) {
        is StoreReadResponse.Error.Exception -> error.message
        is StoreReadResponse.Error.Message -> message
        else -> null
    }
