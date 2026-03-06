package com.keisardev.moviesandbeyond.core.model

/**
 * A sealed wrapper for operation results that carries one of three states: [Success], [Error], or
 * [Loading].
 *
 * Use this as the standard return type for repository operations that may fail or be in-progress,
 * replacing ad-hoc nullable returns or unchecked exceptions.
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()

    data class Error(val exception: Throwable, val message: String? = exception.message) :
        Result<Nothing>()

    data object Loading : Result<Nothing>()
}

/**
 * Transforms the data inside [Result.Success] using [transform], leaving other states unchanged.
 */
inline fun <T, R> Result<T>.mapSuccess(transform: (T) -> R): Result<R> =
    when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> this
        is Result.Loading -> this
    }

/**
 * Chains two [Result]-producing operations. If the receiver is [Result.Success], [transform] is
 * called with the data and its result is returned. Otherwise the original failure/loading is
 * propagated.
 */
inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> =
    when (this) {
        is Result.Success -> transform(data)
        is Result.Error -> this
        is Result.Loading -> this
    }

/** Returns the success data, or `null` if the result is [Result.Error] or [Result.Loading]. */
fun <T> Result<T>.getOrNull(): T? = (this as? Result.Success)?.data

/** Returns the success data, or [default] if the result is [Result.Error] or [Result.Loading]. */
fun <T> Result<T>.getOrDefault(default: T): T = (this as? Result.Success)?.data ?: default

/** Returns `true` if and only if this is a [Result.Success]. */
val Result<*>.isSuccess: Boolean
    get() = this is Result.Success

/** Returns `true` if and only if this is a [Result.Error]. */
val Result<*>.isError: Boolean
    get() = this is Result.Error
