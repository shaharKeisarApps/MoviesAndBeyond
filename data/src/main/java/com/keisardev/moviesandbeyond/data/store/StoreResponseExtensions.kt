package com.keisardev.moviesandbeyond.data.store

import com.keisardev.moviesandbeyond.core.model.NetworkResponse
import com.keisardev.moviesandbeyond.core.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreReadResponseOrigin

/**
 * Converts a Store5 [StoreReadResponse] stream to a [Result] stream.
 *
 * Mapping:
 * - [StoreReadResponse.Loading] → [Result.Loading]
 * - [StoreReadResponse.Data] → [Result.Success]
 * - [StoreReadResponse.Error] → [Result.Error]
 * - [StoreReadResponse.NoNewData] and [StoreReadResponse.Initial] are filtered out
 */
fun <T> Flow<StoreReadResponse<T>>.toResultFlow(): Flow<Result<T>> =
    this.map { response -> response.toResult() }.catch { e -> emit(Result.Error(e)) }

/** Converts a single [StoreReadResponse] to a [Result]. */
fun <T> StoreReadResponse<T>.toResult(): Result<T> =
    when (this) {
        is StoreReadResponse.Loading -> Result.Loading
        is StoreReadResponse.Data -> Result.Success(value)
        is StoreReadResponse.Error.Exception -> Result.Error(error, error.message)
        is StoreReadResponse.Error.Message -> Result.Error(RuntimeException(message), message)
        is StoreReadResponse.Error.Custom<*> -> Result.Error(RuntimeException("Custom error"))
        is StoreReadResponse.NoNewData -> Result.Loading
        is StoreReadResponse.Initial -> Result.Loading
    }

// ==================== Legacy helpers (kept for backward compatibility) ====================

/**
 * Converts a Store5 [StoreReadResponse] stream to a [NetworkResponse] stream. Filters out Loading
 * and NoNewData states, converting only Data and Error states.
 */
fun <T> Flow<StoreReadResponse<T>>.toNetworkResponseFlow(): Flow<NetworkResponse<T>> =
    this.map { response -> response.toNetworkResponse() }
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
