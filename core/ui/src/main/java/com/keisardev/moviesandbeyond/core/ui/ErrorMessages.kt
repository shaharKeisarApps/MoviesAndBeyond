package com.keisardev.moviesandbeyond.core.ui

import com.keisardev.moviesandbeyond.core.model.Result
import com.keisardev.moviesandbeyond.core.model.error.NetworkError

/**
 * Maps a [Result.Error] to a user-friendly message string.
 *
 * Common network errors are mapped to shared messages. Supply [fallback] for a context-specific
 * default when the error type is not recognized.
 *
 * Supply optional [overrides] to customize messages for specific error types (e.g. a different
 * message for [NetworkError.Unauthorized] on a login screen vs. a feed screen).
 */
fun Result.Error.toUserFriendlyMessage(
    fallback: String = "Something went wrong. Please try again.",
    overrides: Map<Class<out Throwable>, String> = emptyMap(),
): String {
    val overrideMessage = overrides[exception::class.java]
    if (overrideMessage != null) return overrideMessage

    return when (val ex = exception) {
        is NetworkError.NotFound -> "This content could not be found."
        is NetworkError.Unauthorized -> "Session expired. Please sign in again."
        is NetworkError.RateLimited -> "Too many requests. Please wait and try again."
        is NetworkError.ConnectionError -> "No internet connection. Please check your network."
        is NetworkError.ServerError -> "Server error (${ex.code}). Please try again later."
        else -> message ?: fallback
    }
}
