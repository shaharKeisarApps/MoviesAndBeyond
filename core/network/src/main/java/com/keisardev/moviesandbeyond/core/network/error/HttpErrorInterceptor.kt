package com.keisardev.moviesandbeyond.core.network.error

import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp [Interceptor] that maps unsuccessful HTTP responses to typed [NetworkError] exceptions.
 *
 * Responses with `isSuccessful == true` (2xx) pass through unmodified. All other status codes are
 * translated and thrown so callers receive a [NetworkError] instead of a raw
 * [retrofit2.HttpException].
 */
internal class HttpErrorInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.isSuccessful) return response

        val code = response.code
        response.close()
        throw when (code) {
            401 -> NetworkError.Unauthorized()
            403 -> NetworkError.Forbidden()
            404 -> NetworkError.NotFound()
            429 -> NetworkError.RateLimited()
            in 500..599 -> NetworkError.ServerError(code)
            else -> NetworkError.Unknown(code, response.message)
        }
    }
}
