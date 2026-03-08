package com.keisardev.moviesandbeyond.core.network

import com.keisardev.moviesandbeyond.core.network.error.NetworkError
import com.keisardev.moviesandbeyond.core.network.ktor.TmdbApi
import com.keisardev.moviesandbeyond.core.network.ktor.installNetworkErrorMapping
import com.keisardev.moviesandbeyond.core.network.model.auth.LoginRequest
import com.keisardev.moviesandbeyond.core.network.model.content.NetworkContentItem
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test

class TmdbApiTest {
    private val jsonHeaders =
        headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private fun createMockClient(
        responseBody: String,
        statusCode: HttpStatusCode = HttpStatusCode.OK,
    ): HttpClient {
        val mockEngine = MockEngine {
            respond(content = responseBody, status = statusCode, headers = jsonHeaders)
        }
        return HttpClient(mockEngine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            installNetworkErrorMapping()
        }
    }

    @Test
    fun `test deserialization`() = runTest {
        val json = resourceReader(this, "/content.json")
        val client = createMockClient(json)
        val tmdbApi = TmdbApi(client)

        val content = tmdbApi.getMovieLists(category = "", page = 1)

        assertEquals(
            NetworkContentItem(
                id = 640146,
                title = "Ant-Man and the Wasp: Quantumania",
                name = null,
                posterPath = "/ngl2FKBlU4fhbdsrtdom9LVLBXw.jpg",
                backdropPath = "/gMJngTNfaqCSCqGD4y8lVMZXKDn.jpg",
                voteAverage = 6.5,
                releaseDate = "2023-02-15",
                firstAirDate = null,
                overview =
                    "Super-Hero partners Scott Lang and Hope van Dyne, along with with Hope's parents Janet van Dyne and Hank Pym, and Scott's daughter Cassie Lang, find themselves exploring the Quantum Realm, interacting with strange new creatures and embarking on an adventure that will push them beyond the limits of what they thought possible.",
            ),
            content.results.first(),
        )
    }

    @Test
    fun `test error response throws NetworkError`() = runTest {
        val errorJson = """{"status_message":"error occurred","status_code":7}"""
        val client = createMockClient(errorJson, HttpStatusCode.Unauthorized)
        val tmdbApi = TmdbApi(client)

        try {
            tmdbApi.validateWithLogin(LoginRequest("", "", ""))
            assert(false) { "Expected NetworkError.Unauthorized to be thrown" }
        } catch (e: NetworkError.Unauthorized) {
            // Expected
        }
    }
}

private inline fun <reified T> resourceReader(caller: T, filepath: String): String {
    return caller!!::class.java.getResource(filepath)!!.readText()
}
