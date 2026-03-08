package com.keisardev.moviesandbeyond.core.network.ktor

import com.keisardev.moviesandbeyond.core.network.error.NetworkError
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import java.io.IOException

fun HttpClientConfig<*>.installNetworkErrorMapping() {
    HttpResponseValidator {
        validateResponse { response ->
            val statusCode = response.status.value
            if (statusCode in 400..599) {
                throw when (statusCode) {
                    HttpStatusCode.Unauthorized.value -> NetworkError.Unauthorized()
                    HttpStatusCode.Forbidden.value -> NetworkError.Forbidden()
                    HttpStatusCode.NotFound.value -> NetworkError.NotFound()
                    HttpStatusCode.TooManyRequests.value -> NetworkError.RateLimited()
                    in 500..599 -> NetworkError.ServerError(statusCode)
                    else -> NetworkError.Unknown(statusCode, response.bodyAsText())
                }
            }
        }
        handleResponseExceptionWithRequest { cause, _ ->
            if (cause is IOException) {
                throw NetworkError.ConnectionError(cause)
            }
        }
    }
}
