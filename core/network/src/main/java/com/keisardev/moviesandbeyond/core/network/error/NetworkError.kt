package com.keisardev.moviesandbeyond.core.network.error

import java.io.IOException

/** Typed representation of network-layer failures mapped from HTTP status codes and exceptions. */
sealed class NetworkError(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /** HTTP 401 – missing or invalid credentials. */
    class Unauthorized(cause: Throwable? = null) : NetworkError("Unauthorized (401)", cause)

    /** HTTP 403 – authenticated but not permitted. */
    class Forbidden(cause: Throwable? = null) : NetworkError("Forbidden (403)", cause)

    /** HTTP 404 – requested resource does not exist. */
    class NotFound(cause: Throwable? = null) : NetworkError("Not found (404)", cause)

    /** HTTP 429 – request rate limit exceeded. */
    class RateLimited(cause: Throwable? = null) : NetworkError("Rate limited (429)", cause)

    /** HTTP 5xx – server-side failure. */
    class ServerError(val code: Int, cause: Throwable? = null) :
        NetworkError("Server error ($code)", cause)

    /** [IOException] – no network connectivity or socket error. */
    class ConnectionError(cause: IOException) :
        NetworkError("Connection error: ${cause.message}", cause)

    /** Any other HTTP error not covered by the cases above. */
    class Unknown(val code: Int, override val message: String) :
        NetworkError("Unknown error $code: $message")
}
