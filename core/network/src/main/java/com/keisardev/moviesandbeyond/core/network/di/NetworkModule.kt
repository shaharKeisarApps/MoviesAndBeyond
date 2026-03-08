package com.keisardev.moviesandbeyond.core.network.di

import com.keisardev.moviesandbeyond.core.network.BuildConfig
import com.keisardev.moviesandbeyond.core.network.ktor.TmdbApi
import com.keisardev.moviesandbeyond.core.network.ktor.installNetworkErrorMapping
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {
    @Singleton
    @Provides
    fun provideHttpClient(): HttpClient =
        HttpClient(Android) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            if (BuildConfig.DEBUG) {
                install(Logging) { level = LogLevel.BODY }
            }
            defaultRequest {
                url(BuildConfig.BASE_URL)
                contentType(ContentType.Application.Json)
                headers.append("Authorization", BuildConfig.ACCESS_TOKEN)
            }
            installNetworkErrorMapping()
        }

    @Singleton @Provides fun provideTmdbApi(client: HttpClient): TmdbApi = TmdbApi(client)
}
